package user;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.Producto;
import services.MotorCSV;

/** Cajero responsable de preparar proformas, confirmar ventas y emitir facturas. */
public class Vendedor extends Empleado {

    private static final String ARCHIVO_INVENTARIO = "inventario.csv";
    private static final String ARCHIVO_VENTAS = "ventas.csv";
    private static final String ARCHIVO_CLIENTES = "clientes.csv";
        private static final String ARCHIVO_GARANTIAS = "garantias.csv";
        private static final int MESES_GARANTIA = 12;
    private static final String[] CABECERA_VENTAS = {"idVenta", "idOrden", "estado", "ciCliente",
            "nombreCliente", "idProducto", "nombreProducto", "cantidad", "precioUnitario",
            "total", "fechaVenta", "ciEmpleado", "idCliente", "telefonoCliente", "idGarantia"};
        private static final String[] CABECERA_GARANTIAS = {"idGarantia", "idVenta", "idOrden", "idCliente",
            "ciCliente", "nombreCliente", "telefonoCliente", "idProducto", "nombreProducto",
            "fechaInicio", "fechaFin", "estado", "ciEmpleado"};

    private final MotorCSV motorCSV;

    public Vendedor(String ci, String nombre, String contrasena) {
        this(ci, nombre, contrasena, new MotorCSV());
    }

    public Vendedor(String ci, String nombre, String contrasena, Path directorioDatos) {
        this(ci, nombre, contrasena, new MotorCSV(directorioDatos));
    }

    private Vendedor(String ci, String nombre, String contrasena, MotorCSV motorCSV) {
        super(ci, nombre, contrasena, Rol.CAJERO);
        this.motorCSV = motorCSV;
    }

    /** Ejecuta una venta de una línea; el segundo argumento es el nombre del cliente. */
    public boolean venderProducto(String codigoProducto, String nombreCliente, int cantidad) {
        if (!cajaAbierta() || nombreCliente == null || nombreCliente.isBlank() || cantidad <= 0) {
            return false;
        }
        Map<String, Integer> productos = new LinkedHashMap<>();
        productos.put(codigoProducto, cantidad);
        String idOrden = generarOrdenDeVenta(nombreCliente, productos);
        return idOrden != null && confirmarVenta(idOrden);
    }

    /** Crea y persiste una proforma con uno o varios productos seleccionados. */
    public String generarOrdenDeVenta(String nombreCliente, Map<String, Integer> productos) {
        return generarOrdenDeVenta(nombreCliente, "", "", productos);
    }

    /** Crea una proforma conservando los datos necesarios para identificar al cliente en la venta. */
    public String generarOrdenDeVenta(String nombreCliente, String ciCliente, String telefonoCliente,
                                      Map<String, Integer> productos) {
        if (nombreCliente == null || nombreCliente.isBlank() || productos == null || productos.isEmpty()) {
            return null;
        }
        String ci = ciCliente == null ? "" : ciCliente.trim();
        String telefono = telefonoCliente == null ? "" : telefonoCliente.trim();
        List<Producto> seleccion = new ArrayList<>();
        for (Map.Entry<String, Integer> linea : productos.entrySet()) {
            Producto producto = buscarEnInventario(linea.getKey());
            if (producto == null || linea.getValue() == null || linea.getValue() <= 0
                    || producto.getStock() < linea.getValue()) {
                return null;
            }
            seleccion.add(producto);
        }

        String idOrden = generarIdOrden();
        String fecha = LocalDateTime.now().toString();
        List<String[]> ventas = leerVentas();
        for (int i = 0; i < seleccion.size(); i++) {
            Producto producto = seleccion.get(i);
            int cantidad = productos.get(producto.getIdProducto());
            double precio = ponerCosto(producto.getPrecioEntrada(), 5.0, producto.getPrecioEntrada() * 0.13);
            double total = precio * cantidad;
                ventas.add(new String[]{generarIdVenta(), idOrden, "PROFORMA", ci, nombreCliente.trim(),
                    producto.getIdProducto(), producto.getNombre(), String.valueOf(cantidad),
                    String.valueOf(precio), String.valueOf(total), fecha, getCi(), "", telefono, ""});
        }
        return motorCSV.escribirCSV(ruta(ARCHIVO_VENTAS), ventas, false) ? idOrden : null;
    }

    /** Anula una orden pendiente o una venta confirmada, restituyendo su inventario. */
    public boolean eliminarVenta(String idVenta) {
        if (idVenta == null || idVenta.isBlank()) {
            return false;
        }
        List<String[]> ventas = leerVentas();
        boolean encontrada = false;
        boolean confirmada = false;
        for (String[] fila : ventas) {
            if (fila.length > 1 && (fila[0].equals(idVenta) || fila[1].equals(idVenta))) {
                encontrada = true;
                confirmada |= fila[2].equals("CONFIRMADA");
            }
        }
        if (!encontrada) {
            return false;
        }
        if (confirmada && !actualizarStockDesdeVentas(ventas, idVenta, true)) {
            return false;
        }
        for (String[] fila : ventas) {
            if (fila.length > 1 && (fila[0].equals(idVenta) || fila[1].equals(idVenta))) {
                fila[2] = "ANULADA";
            }
        }
        return motorCSV.escribirCSV(ruta(ARCHIVO_VENTAS), ventas, false);
    }

    /** Genera un identificador de proforma para compatibilidad con el contrato anterior. */
    public String generarOrdenDeVenta(Producto producto) {
        return producto == null ? null : generarIdOrden();
    }

    /** Importa órdenes desde CSV/TXT con columnas idOrden (opcional), nombreCliente, idProducto y cantidad. */
    public boolean ingresarOrdenDesdeArchivo(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.isBlank()) {
            return false;
        }
        List<String[]> filas = motorCSV.leerCSV(rutaArchivo);
        if (filas.size() < 2) {
            return false;
        }
        Map<String, Map<String, Integer>> ordenes = new LinkedHashMap<>();
        Map<String, String> clientes = new HashMap<>();
        String[] encabezado = filas.get(0);
        int colOrden = columna(encabezado, "idOrden");
        int colCliente = columna(encabezado, "nombreCliente");
        int colProducto = columna(encabezado, "idProducto");
        int colCantidad = columna(encabezado, "cantidad");
        if (colCliente < 0 || colProducto < 0 || colCantidad < 0) {
            return false;
        }
        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (Math.max(colCliente, Math.max(colProducto, colCantidad)) >= fila.length) {
                return false;
            }
            String id = colOrden >= 0 && colOrden < fila.length && !fila[colOrden].isBlank()
                    ? fila[colOrden] : "IMPORT-" + i;
            try {
                int cantidad = Integer.parseInt(fila[colCantidad]);
                if (cantidad <= 0 || fila[colCliente].isBlank() || fila[colProducto].isBlank()) {
                    return false;
                }
                ordenes.computeIfAbsent(id, clave -> new LinkedHashMap<>())
                        .merge(fila[colProducto], cantidad, Integer::sum);
                clientes.put(id, fila[colCliente]);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        for (Map.Entry<String, Map<String, Integer>> orden : ordenes.entrySet()) {
            if (generarOrdenDeVenta(clientes.get(orden.getKey()), orden.getValue()) == null) {
                return false;
            }
        }
        return !ordenes.isEmpty();
    }

    /** Confirma todas las líneas de una proforma y descuenta las existencias. */
    public boolean confirmarVenta(String idOrden) {
        if (!cajaAbierta() || idOrden == null || idOrden.isBlank()) {
            return false;
        }
        List<String[]> ventas = leerVentas();
        boolean encontrada = false;
        for (String[] fila : ventas) {
            if (fila.length > 1 && fila[1].equals(idOrden)) {
                if (!fila[2].equals("PROFORMA")) {
                    return false;
                }
                encontrada = true;
            }
        }
        if (!encontrada || !actualizarStockDesdeVentas(ventas, idOrden, false)) {
            return false;
        }
        String nombreCliente = nombreCliente(ventas, idOrden);
        String ciCliente = datoCliente(ventas, idOrden, 3);
        String telefonoCliente = datoCliente(ventas, idOrden, 13);
        String idCliente = guardarCliente(nombreCliente, ciCliente, telefonoCliente);
        if (idCliente == null) {
            return false;
        }
        for (String[] fila : ventas) {
            if (fila.length > 1 && fila[1].equals(idOrden)) {
                fila[2] = "CONFIRMADA";
                fila[10] = LocalDateTime.now().toString();
                fila[12] = idCliente;
                fila[13] = telefonoCliente;
            }
        }
        if (!crearGarantias(ventas, idOrden, idCliente, ciCliente, nombreCliente, telefonoCliente)) {
            return false;
        }
        if (!motorCSV.escribirCSV(ruta(ARCHIVO_VENTAS), ventas, false)) {
            return false;
        }
        System.out.println(generarFactura(idOrden, nombreCliente, lineasFactura(ventas, idOrden)));
        return true;
    }

    /** Busca garantías por su código, ID de venta u orden e informa si siguen vigentes. */
    public List<String> consultarGarantia(String referencia) {
        List<String> resultado = new ArrayList<>();
        if (referencia == null || referencia.isBlank()) {
            return List.of("Debe indicar un código de garantía, venta u orden.");
        }
        List<String[]> garantias = motorCSV.leerCSV(ruta(ARCHIVO_GARANTIAS));
        List<String[]> ventas = motorCSV.leerCSV(ruta(ARCHIVO_VENTAS));
        for (int i = 1; i < garantias.size(); i++) {
            String[] garantia = garantias.get(i);
            if (garantia.length < CABECERA_GARANTIAS.length
                    || !(garantia[0].equalsIgnoreCase(referencia.trim())
                    || garantia[1].equalsIgnoreCase(referencia.trim())
                    || garantia[2].equalsIgnoreCase(referencia.trim()))) {
                continue;
            }
            boolean ventaConfirmada = ventas.stream().skip(1)
                    .anyMatch(fila -> fila.length > 2 && fila[0].equals(garantia[1])
                            && fila[2].equalsIgnoreCase("CONFIRMADA"));
            LocalDate inicio;
            LocalDate fin;
            try {
                inicio = LocalDate.parse(garantia[9]);
                fin = LocalDate.parse(garantia[10]);
            } catch (RuntimeException e) {
                resultado.add(garantia[0] + " | Fechas de garantía invalidas.");
                continue;
            }
            boolean vigente = ventaConfirmada && garantia[11].equalsIgnoreCase("VIGENTE")
                    && !LocalDate.now().isBefore(inicio) && !LocalDate.now().isAfter(fin);
            resultado.add(garantia[0] + " | " + garantia[8] + " | Cliente: " + garantia[5]
                    + " | CI: " + garantia[4] + " | Inicio: " + inicio + " | Vence: " + fin
                    + " | Estado: " + (vigente ? "VIGENTE" : "NO VIGENTE"));
        }
        if (resultado.isEmpty()) {
            resultado.add("No se encontraron garantías para: " + referencia);
        }
        return resultado;
    }

    /** Busca por ID exacto o por coincidencia de nombre/marca. */
    public Producto buscarEnInventario(String terminoBusqueda) {
        if (terminoBusqueda == null || terminoBusqueda.isBlank()) {
            return null;
        }
        String termino = terminoBusqueda.trim();
        for (String[] fila : motorCSV.leerCSV(ruta(ARCHIVO_INVENTARIO))) {
            if (fila.length < 8 || fila[0].equalsIgnoreCase("idProducto")) {
                continue;
            }
            if (fila[0].equalsIgnoreCase(termino) || fila[1].toLowerCase().contains(termino.toLowerCase())
                    || fila[2].toLowerCase().contains(termino.toLowerCase())) {
                try {
                    return new Producto(fila[0], fila[1], Double.parseDouble(fila[5]),
                            Double.parseDouble(fila[6]), fila[4], Integer.parseInt(fila[7]), fila[2], fila[3]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /** Construye una factura legible para una orden, incluyendo sus líneas y total. */
    public String generarFactura(String idOrden, String nombreCliente, List<String> lineas) {
        StringBuilder factura = new StringBuilder("=== FACTURA ===\nOrden: ").append(idOrden)
                .append("\nCliente: ").append(nombreCliente).append('\n');
        double total = 0;
        for (String linea : lineas) {
            factura.append(linea).append('\n');
            int separador = linea.lastIndexOf('|');
            if (separador >= 0) {
                try {
                    total += Double.parseDouble(linea.substring(separador + 1).trim());
                } catch (NumberFormatException ignored) {
                    // La línea permanece en la factura aunque un dato externo esté mal formado.
                }
            }
        }
        return factura.append("TOTAL: $").append(String.format(java.util.Locale.ROOT, "%.2f", total)).append('\n')
                .append("================").toString();
    }

    public String generarFactura(Producto venta) {
        if (venta == null) {
            return "Factura no disponible: producto inexistente.";
        }
        return generarFactura("SIN-ORDEN", "Consumidor final",
                List.of(venta.getNombre() + " x1 | " + String.format(java.util.Locale.ROOT, "%.2f", venta.getPrecioVenta())));
    }

    public double calcularCosto(double pCosto, double oCosto, double iva) {
        return pCosto + (oCosto * 0.20) + iva;
    }

    private double ponerCosto(double pCosto, double oCosto, double iva) {
        return calcularCosto(pCosto, oCosto, iva);
    }

    /** Busca productos por nombre o marca y los devuelve indexados por su identificador. */
    public Map<String, Object> buscarPorRazon(String razon) {
        Map<String, Object> resultados = new HashMap<>();
        if (razon == null || razon.isBlank()) {
            return resultados;
        }
        String criterio = razon.trim().toLowerCase();
        for (String[] fila : motorCSV.leerCSV(ruta(ARCHIVO_INVENTARIO))) {
            if (fila.length >= 8 && !fila[0].equalsIgnoreCase("idProducto")
                    && (fila[1].toLowerCase().contains(criterio) || fila[2].toLowerCase().contains(criterio))) {
                resultados.put(fila[0], fila[1] + " | Marca: " + fila[2] + " | Stock: " + fila[7]);
            }
        }
        return resultados;
    }

    private boolean actualizarStockDesdeVentas(List<String[]> ventas, String id, boolean devolver) {
        List<String[]> inventario = motorCSV.leerCSV(ruta(ARCHIVO_INVENTARIO));
        Map<String, Integer> cantidades = new HashMap<>();
        for (String[] fila : ventas) {
            if (fila.length > 7 && (fila[0].equals(id) || fila[1].equals(id))
                    && (!devolver || fila[2].equals("CONFIRMADA"))) {
                cantidades.merge(fila[5], Integer.valueOf(fila[7]), Integer::sum);
            }
        }
        for (Map.Entry<String, Integer> linea : cantidades.entrySet()) {
            String[] producto = buscarFila(inventario, linea.getKey());
            if (producto == null) {
                return false;
            }
            int stock = Integer.parseInt(producto[7]);
            if (!devolver && stock < linea.getValue()) {
                return false;
            }
            producto[7] = String.valueOf(devolver ? stock + linea.getValue() : stock - linea.getValue());
        }
        return !cantidades.isEmpty() && motorCSV.escribirCSV(ruta(ARCHIVO_INVENTARIO), inventario, false);
    }

    private String[] buscarFila(List<String[]> filas, String idProducto) {
        for (int i = 1; i < filas.size(); i++) {
            if (filas.get(i).length > 7 && filas.get(i)[0].equals(idProducto)) {
                return filas.get(i);
            }
        }
        return null;
    }

    private List<String[]> leerVentas() {
        List<String[]> ventas = motorCSV.leerCSV(ruta(ARCHIVO_VENTAS));
        if (ventas.isEmpty()) {
            ventas.add(CABECERA_VENTAS.clone());
            return ventas;
        }
        if (ventas.get(0).length < CABECERA_VENTAS.length) {
            ventas.set(0, CABECERA_VENTAS.clone());
            for (int i = 1; i < ventas.size(); i++) {
                ventas.set(i, Arrays.copyOf(ventas.get(i), CABECERA_VENTAS.length));
            }
        }
        return ventas;
    }

    private boolean cajaAbierta() {
        List<String[]> caja = motorCSV.leerCSV(ruta("caja.csv"));
        return caja.size() > 1 && caja.get(1).length > 0 && caja.get(1)[0].equalsIgnoreCase("ABIERTA");
    }

    private String ruta(String archivo) {
        return motorCSV.rutaDatos(archivo).toString();
    }

    private String generarIdOrden() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generarIdVenta() {
        return "VEN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private int columna(String[] encabezado, String nombre) {
        for (int i = 0; i < encabezado.length; i++) {
            if (encabezado[i].equalsIgnoreCase(nombre)) {
                return i;
            }
        }
        return -1;
    }

    private String nombreCliente(List<String[]> ventas, String idOrden) {
        for (String[] fila : ventas) {
            if (fila.length > 4 && fila[1].equals(idOrden)) {
                return fila[4];
            }
        }
        return "Consumidor final";
    }

    private String guardarCliente(String nombre, String ci, String telefono) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        List<String[]> clientes = motorCSV.leerCSV(ruta(ARCHIVO_CLIENTES));
        if (clientes.isEmpty()) {
            clientes.add(new String[]{"idCliente", "nombreCliente"});
        }
        int columnaId = columna(clientes.get(0), "idCliente");
        int columnaNombre = columna(clientes.get(0), "nombreCliente");
        if (columnaId < 0 || columnaNombre < 0) {
            return null;
        }
        int columnaCi = asegurarColumna(clientes, "ci");
        int columnaTelefono = asegurarColumna(clientes, "telefono");
        int ancho = clientes.get(0).length;
        for (int i = 1; i < clientes.size(); i++) {
            clientes.set(i, Arrays.copyOf(clientes.get(i), ancho));
        }
        for (int i = 1; i < clientes.size(); i++) {
            String[] fila = clientes.get(i);
            boolean mismaIdentidad = !ci.isBlank()
                    ? texto(fila, columnaCi).equalsIgnoreCase(ci.trim())
                    : texto(fila, columnaNombre).trim().equalsIgnoreCase(nombre.trim());
            if (mismaIdentidad && !texto(fila, columnaId).isBlank()) {
                fila[columnaNombre] = nombre.trim();
                if (!ci.isBlank()) {
                    fila[columnaCi] = ci.trim();
                }
                if (!telefono.isBlank()) {
                    fila[columnaTelefono] = telefono.trim();
                }
                return motorCSV.escribirCSV(ruta(ARCHIVO_CLIENTES), clientes, false)
                    ? texto(fila, columnaId) : null;
            }
        }
        String idCliente = "CLI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String[] nuevoCliente = new String[ancho];
        nuevoCliente[columnaId] = idCliente;
        nuevoCliente[columnaNombre] = nombre.trim();
        nuevoCliente[columnaCi] = ci.trim();
        nuevoCliente[columnaTelefono] = telefono.trim();
        clientes.add(nuevoCliente);
        return motorCSV.escribirCSV(ruta(ARCHIVO_CLIENTES), clientes, false) ? idCliente : null;
    }

    private int asegurarColumna(List<String[]> filas, String nombre) {
        String[] encabezado = filas.get(0);
        int existente = columna(encabezado, nombre);
        if (existente >= 0) {
            return existente;
        }
        String[] extendido = Arrays.copyOf(encabezado, encabezado.length + 1);
        extendido[encabezado.length] = nombre;
        filas.set(0, extendido);
        for (int i = 1; i < filas.size(); i++) {
            filas.set(i, Arrays.copyOf(filas.get(i), extendido.length));
        }
        return extendido.length - 1;
    }

    private String texto(String[] fila, int indice) {
        return fila.length > indice && fila[indice] != null ? fila[indice] : "";
    }

    private String datoCliente(List<String[]> ventas, String idOrden, int indice) {
        for (String[] fila : ventas) {
            if (fila.length > 4 && fila[1].equals(idOrden)) {
                return fila.length > indice ? fila[indice] : "";
            }
        }
        return "";
    }

    private boolean crearGarantias(List<String[]> ventas, String idOrden, String idCliente,
                                   String ciCliente, String nombre, String telefono) {
        List<String[]> garantias = motorCSV.leerCSV(ruta(ARCHIVO_GARANTIAS));
        if (garantias.isEmpty()) {
            garantias.add(CABECERA_GARANTIAS.clone());
        }
        if (!Arrays.equals(garantias.get(0), CABECERA_GARANTIAS)) {
            return false;
        }
        for (String[] venta : ventas) {
            if (venta.length < CABECERA_VENTAS.length || !venta[1].equals(idOrden)) {
                continue;
            }
            String[] existente = buscarGarantia(garantias, venta[0]);
            String idGarantia;
            if (existente != null) {
                idGarantia = existente[0];
            } else {
                idGarantia = "GAR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                LocalDate inicio;
                try {
                    inicio = LocalDate.parse(venta[10].substring(0, 10));
                } catch (RuntimeException e) {
                    inicio = LocalDate.now();
                }
                LocalDate fin = inicio.plusMonths(MESES_GARANTIA);
                garantias.add(new String[]{idGarantia, venta[0], idOrden, idCliente, ciCliente, nombre,
                        telefono, venta[5], venta[6], inicio.toString(), fin.toString(), "VIGENTE", getCi()});
            }
            venta[14] = idGarantia;
        }
        return garantias.size() > 1 && motorCSV.escribirCSV(ruta(ARCHIVO_GARANTIAS), garantias, false);
    }

    private String[] buscarGarantia(List<String[]> garantias, String idVenta) {
        for (int i = 1; i < garantias.size(); i++) {
            if (garantias.get(i).length > 1 && garantias.get(i)[1].equals(idVenta)) {
                return garantias.get(i);
            }
        }
        return null;
    }

    private List<String> lineasFactura(List<String[]> ventas, String idOrden) {
        List<String> lineas = new ArrayList<>();
        List<String[]> garantias = motorCSV.leerCSV(ruta(ARCHIVO_GARANTIAS));
        for (String[] fila : ventas) {
            if (fila.length > 9 && fila[1].equals(idOrden)) {
                lineas.add(fila[6] + " x" + fila[7] + " | " + fila[9]);
                if (fila.length > 14) {
                    String[] garantia = buscarGarantia(garantias, fila[0]);
                    if (garantia != null) {
                        lineas.add("Garantia " + garantia[0] + " valida hasta " + garantia[10]);
                    }
                }
            }
        }
        return lineas;
    }
}