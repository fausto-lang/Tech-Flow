package user;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import models.Producto;
import models.Proveedor;
import services.MotorCSV;

/** Administrador con acceso a caja, reportes, personal, proveedores e inventario. */
public class Gestor extends Empleado {

    private static final String ARCHIVO_CAJA = "caja.csv";
    private static final String ARCHIVO_INVENTARIO = "inventario.csv";
    private static final String ARCHIVO_VENTAS = "ventas.csv";
    private static final String ARCHIVO_EMPLEADOS = "empleado.csv";
    private static final String ARCHIVO_PROVEEDORES = "proveedores.csv";
    private static final String ARCHIVO_ENTRADAS = "entradas.csv";
    private static final String[] CABECERA_EMPLEADOS = {"ci", "nombre", "rol", "contrasena"};
    private static final String[] CABECERA_PROVEEDORES = {"idProveedor", "nombreProveedor", "contactoProveedor"};
    private static final String[] CABECERA_INVENTARIO = {"idProducto", "nombre", "marca", "categoria",
            "descripcion", "precioEntrada", "precioVenta", "stock"};
    private static final int STOCK_CRITICO = 5;

    private final MotorCSV motorCSV;

    public Gestor(String ci, String nombre, String contrasena) {
        this(ci, nombre, contrasena, Paths.get(System.getProperty("techflow.data.dir", "data")));
    }

    public Gestor(String ci, String nombre, String contrasena, Path directorioDatos) {
        super(ci, nombre, contrasena, Rol.ADMINISTRADOR);
        this.motorCSV = new MotorCSV(directorioDatos);
    }

    public boolean abrirCaja() {
        return escribirEstadoCaja("ABIERTA");
    }

    public boolean cerrarCaja() {
        return escribirEstadoCaja("CERRADA");
    }

    /** Muestra ventas confirmadas del día, independientemente del estado de la caja. */
    public void verVentasEIngresosDiarios() {
        List<String[]> ventas = motorCSV.leerCSV(ruta(ARCHIVO_VENTAS));
        double ingresos = 0;
        LocalDate hoy = LocalDate.now();
        System.out.println("--- VENTAS DEL " + hoy + " ---");
        for (int i = 1; i < ventas.size(); i++) {
            String[] fila = ventas.get(i);
            try {
                String estado = valor(fila, 2, "CONFIRMADA");
                String fecha = valor(fila, 10, valor(fila, 6, ""));
                if (estado.equalsIgnoreCase("CONFIRMADA") && fecha.startsWith(hoy.toString())) {
                    String id = valor(fila, 0, "");
                    String cliente = valor(fila, 4, valor(fila, 1, ""));
                    double total = Double.parseDouble(valor(fila, 9, valor(fila, 2, "0")));
                    System.out.println("Venta " + id + " | Cliente: " + cliente + " | Total: $" + total);
                    ingresos += total;
                }
            } catch (NumberFormatException ignored) {
                // Se omiten filas mal formadas sin detener el reporte.
            }
        }
        System.out.println("Ingresos confirmados: $" + ingresos);
    }

    public void verInventario() {
        List<String[]> productos = motorCSV.leerCSV(ruta(ARCHIVO_INVENTARIO));
        System.out.println("--- INVENTARIO ---");
        for (int i = 1; i < productos.size(); i++) {
            String[] fila = productos.get(i);
            System.out.println("ID: " + valor(fila, 0, "") + " | " + valor(fila, 1, "")
                    + " | Marca: " + valor(fila, 2, "") + " | Stock: " + valor(fila, 7, "0"));
        }
    }

    /** Registra una recepción. En el modelo actual, stock representa la cantidad recibida. */
    public boolean ingresarEntradaProducto(Producto entrada) {
        if (entrada == null || entrada.getStock() <= 0) {
            return false;
        }
        Almacen almacen = new Almacen(getCi(), getNombre(), getContrasena(), directorioDatos());
        Producto existente = leerProducto(entrada.getIdProducto());
        if (existente == null) {
            return almacen.ingresarProductoNuevo(entrada);
        }
        return almacen.ingresarProductoExistente(entrada.getIdProducto(), entrada.getStock());
    }

    public boolean ingresarEntradasDesdeCSV(String rutaArchivo) {
        return new Almacen(getCi(), getNombre(), getContrasena(), directorioDatos())
                .ingresarEntradaDesdeArchivo(rutaArchivo);
    }

    /** Muestra un resumen de entradas recibidas hoy. */
    public void verAnalisisEntradasDiarias() {
        LocalDate hoy = LocalDate.now();
        int unidades = 0;
        int registros = 0;
        for (String[] fila : motorCSV.leerCSV(ruta(ARCHIVO_ENTRADAS))) {
            if (fila.length > 6 && fila[6].startsWith(hoy.toString())) {
                try {
                    unidades += Integer.parseInt(fila[4]);
                    registros++;
                } catch (NumberFormatException ignored) {
                    // Ignora registros dañados del archivo para preservar el resto del reporte.
                }
            }
        }
        System.out.println("Entradas hoy: " + registros + " registros, " + unidades + " unidades.");
    }

    public boolean anadirEmpleado(Empleado empleado) {
        if (empleado == null || vacio(empleado.getCi()) || vacio(empleado.getNombre())
                || vacio(empleado.getContrasena()) || empleado.getRol() == null) {
            return false;
        }
        List<String[]> filas = leerConCabecera(ARCHIVO_EMPLEADOS, CABECERA_EMPLEADOS);
        if (existeValor(filas, 0, empleado.getCi())) {
            return false;
        }
        filas.add(new String[]{empleado.getCi(), empleado.getNombre(), empleado.getRol().name(), empleado.getContrasena()});
        return motorCSV.escribirCSV(ruta(ARCHIVO_EMPLEADOS), filas, false);
    }

    public boolean eliminarEmpleado(String ci) {
        return eliminarPorClave(ARCHIVO_EMPLEADOS, ci);
    }

    public boolean anadirProveedor(Proveedor proveedor) {
        if (proveedor == null || vacio(proveedor.getCodigoProveedor()) || vacio(proveedor.getNombreProveedor())) {
            return false;
        }
        List<String[]> filas = leerConCabecera(ARCHIVO_PROVEEDORES, CABECERA_PROVEEDORES);
        if (existeValor(filas, 0, proveedor.getCodigoProveedor())) {
            return false;
        }
        filas.add(new String[]{proveedor.getCodigoProveedor(), proveedor.getNombreProveedor(),
                String.valueOf(proveedor.getContactoProveedor())});
        return motorCSV.escribirCSV(ruta(ARCHIVO_PROVEEDORES), filas, false);
    }

    public boolean eliminarProveedor(String idProveedor) {
        return eliminarPorClave(ARCHIVO_PROVEEDORES, idProveedor);
    }

    public boolean anadirProducto(Producto producto) {
        if (producto == null || vacio(producto.getIdProducto()) || vacio(producto.getNombre())
                || producto.getStock() < 0 || producto.getPrecioEntrada() < 0 || producto.getPrecioVenta() < 0) {
            return false;
        }
        List<String[]> filas = leerConCabecera(ARCHIVO_INVENTARIO, CABECERA_INVENTARIO);
        if (existeValor(filas, 0, producto.getIdProducto())) {
            return false;
        }
        filas.add(filaProducto(producto));
        return motorCSV.escribirCSV(ruta(ARCHIVO_INVENTARIO), filas, false);
    }

    public boolean eliminarProducto(String codigo) {
        return eliminarPorClave(ARCHIVO_INVENTARIO, codigo);
    }

    /** Persiste un pedido de proveedor y devuelve silenciosamente si los datos no son válidos. */
    public void generarPedidoProveedor(String idProveedor, List<Producto> productos) {
        if (vacio(idProveedor) || productos == null || productos.isEmpty() || !existeProveedor(idProveedor)) {
            return;
        }
        String idPedido = "PED-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String rutaPedidos = ruta("pedidos.csv");
        List<String[]> pedidos = leerConCabecera("pedidos.csv",
                new String[]{"idPedido", "idProveedor", "idProducto", "cantidad", "fechaPedido", "ciEmpleado"});
        for (Producto producto : productos) {
            if (producto != null && producto.getStock() > 0) {
                pedidos.add(new String[]{idPedido, idProveedor, producto.getIdProducto(),
                        String.valueOf(producto.getStock()), LocalDate.now().toString(), getCi()});
            }
        }
        motorCSV.escribirCSV(rutaPedidos, pedidos, false);
    }

    /** Lista productos con cinco unidades o menos; el umbral queda centralizado para cambiarlo fácilmente. */
    public List<Producto> listarProductosProximosAAgotarse() {
        List<Producto> resultado = new ArrayList<>();
        for (String[] fila : motorCSV.leerCSV(ruta(ARCHIVO_INVENTARIO))) {
            if (fila.length < 8 || fila[0].equalsIgnoreCase("idProducto")) {
                continue;
            }
            try {
                if (Integer.parseInt(fila[7]) <= STOCK_CRITICO) {
                    resultado.add(new Producto(fila[0], fila[1], Double.parseDouble(fila[5]),
                            Double.parseDouble(fila[6]), fila[4], Integer.parseInt(fila[7]), fila[2], fila[3]));
                }
            } catch (NumberFormatException ignored) {
                // Omite producto cuyo registro numérico no es válido.
            }
        }
        return Collections.unmodifiableList(resultado);
    }

    public void consultarVentasPorRangoFechas(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null || inicio.isAfter(fin)) {
            return;
        }
        for (String[] fila : motorCSV.leerCSV(ruta(ARCHIVO_VENTAS))) {
            if (fila.length <= 10 || !fila[2].equalsIgnoreCase("CONFIRMADA")) {
                continue;
            }
            try {
                LocalDate fecha = LocalDate.parse(fila[10].substring(0, 10));
                if (!fecha.isBefore(inicio) && !fecha.isAfter(fin)) {
                    System.out.println("Venta " + fila[0] + " | " + fila[4] + " | $" + fila[9] + " | " + fecha);
                }
            } catch (RuntimeException ignored) {
                // Omite fila cuya fecha no respete el esquema.
            }
        }
    }

    public boolean exportarReporteExcelOCSV(String ruta) {
        return motorCSV.exportarReporte(ruta);
    }

    private boolean escribirEstadoCaja(String estado) {
        List<String[]> filas = new ArrayList<>();
        filas.add(new String[]{"estado", "fechaApertura", "fechaCierre", "ciEmpleado"});
        filas.add(new String[]{estado, estado.equals("ABIERTA") ? LocalDate.now().toString() : "",
                estado.equals("CERRADA") ? LocalDate.now().toString() : "", getCi()});
        return motorCSV.escribirCSV(ruta(ARCHIVO_CAJA), filas, false);
    }

    private boolean eliminarPorClave(String archivo, String clave) {
        if (vacio(clave)) {
            return false;
        }
        return motorCSV.eliminarFilaPorClave(ruta(archivo), 0, clave);
    }

    private boolean existeValor(List<String[]> filas, int columna, String valor) {
        for (int i = 1; i < filas.size(); i++) {
            if (filas.get(i).length > columna && filas.get(i)[columna].equalsIgnoreCase(valor)) {
                return true;
            }
        }
        return false;
    }

    private List<String[]> leerConCabecera(String archivo, String[] cabecera) {
        List<String[]> filas = motorCSV.leerCSV(ruta(archivo));
        if (filas.isEmpty()) {
            filas.add(cabecera.clone());
        }
        return filas;
    }

    private boolean existeProveedor(String codigo) {
        return existeValor(motorCSV.leerCSV(ruta(ARCHIVO_PROVEEDORES)), 0, codigo);
    }

    private Producto leerProducto(String codigo) {
        for (String[] fila : motorCSV.leerCSV(ruta(ARCHIVO_INVENTARIO))) {
            if (fila.length >= 8 && fila[0].equalsIgnoreCase(codigo)) {
                try {
                    return new Producto(fila[0], fila[1], Double.parseDouble(fila[5]), Double.parseDouble(fila[6]),
                            fila[4], Integer.parseInt(fila[7]), fila[2], fila[3]);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private String[] filaProducto(Producto producto) {
        return new String[]{producto.getIdProducto(), producto.getNombre(), producto.getMarca(), producto.getCategoria(),
                producto.getDescripcion(), String.valueOf(producto.getPrecioEntrada()),
                String.valueOf(producto.getPrecioVenta()), String.valueOf(producto.getStock())};
    }

    private String ruta(String archivo) {
        return motorCSV.rutaDatos(archivo).toString();
    }

    private Path directorioDatos() {
        return Paths.get(ruta("")).normalize();
    }

    private String valor(String[] fila, int indice, String porDefecto) {
        return fila.length > indice ? fila[indice] : porDefecto;
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}