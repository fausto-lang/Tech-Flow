package user;

import config.ArchivosCSV;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import models.Producto;
import services.MotorCSV;
import services.ValidadorDatos;
import java.util.Locale;

/** Operador responsable de productos, existencias y registro de entradas. */
public class Almacen extends Empleado {

    private static final String[] CABECERA_ENTRADAS = {"idEntrada", "idProveedor", "idProducto",
            "nombreProducto", "cantidad", "precioUnitario", "fechaEntrada", "ciEmpleado"};

    private final MotorCSV motorCSV;

    public Almacen(String ci, String nombre, String contrasena) {
        this(ci, nombre, contrasena, new MotorCSV(Paths.get(System.getProperty("techflow.data.dir", "data"))));
    }

    public Almacen(String ci, String nombre, String contrasena, Path directorioDatos) {
        this(ci, nombre, contrasena, new MotorCSV(directorioDatos));
    }

    private Almacen(String ci, String nombre, String contrasena, MotorCSV motorCSV) {
        super(ci, nombre, contrasena, Rol.ALMACENERO);
        this.motorCSV = motorCSV;
    }

    /** Registra un producto nuevo, sin permitir identificadores duplicados. */
    public boolean ingresarProductoNuevo(Producto producto) {
        if (!productoValido(producto)) {
            return false;
        }
        List<String[]> inventario = leerInventario();
        if (buscarFila(inventario, producto.getIdProducto()) != null) {
            return false;
        }
        inventario.add(filaProducto(producto));
        if (!motorCSV.escribirCSV(ruta(ArchivosCSV.INVENTARIO), inventario, false)) {
            return false;
        }
        return producto.getStock() == 0 || registrarEntrada("", producto.getIdProducto(), producto.getNombre(),
                producto.getStock(), String.valueOf(producto.getPrecioEntrada()));
    }

    /** Incrementa el stock de un producto ya registrado y deja constancia de la entrada. */
    public boolean ingresarProductoExistente(String codigo, int cantidad) {
        if (codigo == null || codigo.isBlank() || cantidad <= 0) {
            return false;
        }
        List<String[]> inventario = leerInventario();
        String[] producto = buscarFila(inventario, codigo);
        if (producto == null) {
            return false;
        }
        long nuevoStock;
        try {
            nuevoStock = (long) Integer.parseInt(producto[7]) + (long) cantidad;
            if (nuevoStock < 0 || nuevoStock > Integer.MAX_VALUE) return false;
        } catch (NumberFormatException e) {
            return false;
        }
        producto[7] = String.valueOf((int) nuevoStock);
        if (!motorCSV.escribirCSV(ruta(ArchivosCSV.INVENTARIO), inventario, false)) {
            return false;
        }
        return registrarEntrada("", producto[0], producto[1], cantidad, producto[5]);
    }

    /** Importa recepciones con columnas idProveedor,idProducto,nombreProducto,marca,categoria,precio,cantidad. */
    public boolean ingresarEntradaDesdeArchivo(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.isBlank()) {
            return false;
        }
        List<String[]> filas = motorCSV.leerCSV(rutaArchivo);
        if (filas.size() < 2) {
            return false;
        }
        String[] encabezado = filas.get(0);
        int idProveedor = columna(encabezado, "idProveedor");
        int idProducto = columna(encabezado, "idProducto");
        int nombreProducto = columna(encabezado, "nombreProducto");
        int marca = columna(encabezado, "marca");
        int categoria = columna(encabezado, "categoria");
        int precio = columna(encabezado, "precio");
        int cantidad = columna(encabezado, "cantidad");
        if (idProveedor < 0 || idProducto < 0 || nombreProducto < 0 || marca < 0
                || categoria < 0 || precio < 0 || cantidad < 0) {
            return false;
        }

        List<String[]> inventario = leerInventario();
        List<String[]> entradas = leerEntradas();
        List<String[]> nuevasEntradas = new ArrayList<>();
        List<Recepcion> recepciones = new ArrayList<>();
        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            int mayorColumna = Math.max(idProducto, cantidad);
            if (fila.length <= mayorColumna) {
                return false;
            }
            String id = fila[idProducto].trim();
            try {
                int unidades = Integer.parseInt(fila[cantidad]);
                if (id.isEmpty() || unidades <= 0) {
                    return false;
                }
                if (fila[idProveedor].isBlank() || fila[nombreProducto].isBlank()
                        || fila[marca].isBlank() || fila[categoria].isBlank()
                        || !ValidadorDatos.textoValido(fila[nombreProducto])
                        || !ValidadorDatos.textoValido(fila[marca])
                        || !ValidadorDatos.textoValido(fila[categoria])) {
                    return false;
                }
                String[] producto = buscarFila(inventario, id);
                double precioEntrada = producto == null ? 0 : Double.parseDouble(producto[5]);
                if (fila.length <= precio) {
                    return false;
                }
                    precioEntrada = Double.parseDouble(fila[precio]);
                    if (!Double.isFinite(precioEntrada) || precioEntrada <= 0) {
                        return false;
                    }
                if (fila.length <= idProveedor) {
                    return false;
                }
                String proveedor = fila[idProveedor].trim();
                if (proveedor.isEmpty() || !existeProveedor(proveedor)) {
                    return false;
                }
                if (producto == null) {
                    if (nombreProducto < 0 || marca < 0 || categoria < 0
                            || Math.max(nombreProducto, Math.max(marca, categoria)) >= fila.length) {
                        return false;
                    }
                    String precioEntradaTexto = String.format(java.util.Locale.ROOT, "%.2f", precioEntrada);
                    String precioVentaTexto = String.format(java.util.Locale.ROOT, "%.2f", precioEntrada * 1.3);
                    producto = new String[]{id, fila[nombreProducto], fila[marca], fila[categoria], "Sin descripcion",
                            precioEntradaTexto, precioVentaTexto, "0"};
                    inventario.add(producto);
                } else if (precio >= 0) {
                    producto[5] = String.format(java.util.Locale.ROOT, "%.2f", precioEntrada);
                    producto[6] = String.format(java.util.Locale.ROOT, "%.2f", precioEntrada * 1.3);
                }
                recepciones.add(new Recepcion(proveedor, producto[0], producto[1], unidades,
                        String.valueOf(precioEntrada)));
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (recepciones.isEmpty()) {
            return false;
        }
        for (Recepcion recepcion : recepciones) {
            String[] producto = buscarFila(inventario, recepcion.idProducto);
            long nuevoStock = (long) Integer.parseInt(producto[7]) + (long) recepcion.cantidad;
            if (nuevoStock > Integer.MAX_VALUE) {
                return false;
            }
            producto[7] = String.valueOf((int) nuevoStock);
            nuevasEntradas.add(filaEntrada(recepcion));
        }
        entradas.addAll(nuevasEntradas);
        return motorCSV.escribirCSV(ruta(ArchivosCSV.INVENTARIO), inventario, false)
                && motorCSV.escribirCSV(ruta(ArchivosCSV.ENTRADAS), entradas, false);
    }

    private boolean productoValido(Producto producto) {
        return producto != null && producto.getIdProducto() != null && !producto.getIdProducto().isBlank()
                && producto.getNombre() != null && !producto.getNombre().isBlank()
                && ValidadorDatos.textoValido(producto.getNombre())
                && ValidadorDatos.textoValido(producto.getMarca())
                && ValidadorDatos.textoValido(producto.getCategoria())
                && ValidadorDatos.textoValido(producto.getDescripcion())
                && producto.getStock() >= 0 && producto.getStock() <= Integer.MAX_VALUE
                && Double.isFinite(producto.getPrecioEntrada()) && Double.isFinite(producto.getPrecioVenta())
                && producto.getPrecioEntrada() > 0 && producto.getPrecioVenta() > 0
                && producto.getPrecioVenta() > producto.getPrecioEntrada();
    }

    private List<String[]> leerInventario() {
        List<String[]> filas = motorCSV.leerCSV(ruta(ArchivosCSV.INVENTARIO));
        if (filas.isEmpty()) {
            filas.add(ArchivosCSV.INVENTARIO_ENCABEZADO.clone());
        }
        return filas;
    }

    private List<String[]> leerEntradas() {
        List<String[]> filas = motorCSV.leerCSV(ruta(ArchivosCSV.ENTRADAS));
        if (filas.isEmpty()) {
            filas.add(CABECERA_ENTRADAS.clone());
        }
        return filas;
    }

    private String[] buscarFila(List<String[]> filas, String codigo) {
        for (int i = 1; i < filas.size(); i++) {
            if (filas.get(i).length >= 8 && filas.get(i)[0].equalsIgnoreCase(codigo)) {
                return filas.get(i);
            }
        }
        return null;
    }

    private String[] filaProducto(Producto producto) {
        return new String[]{producto.getIdProducto(), producto.getNombre(), producto.getMarca(),
                producto.getCategoria(), producto.getDescripcion(),
                String.format(java.util.Locale.ROOT, "%.2f", producto.getPrecioEntrada()),
                String.format(java.util.Locale.ROOT, "%.2f", producto.getPrecioVenta()),
                String.valueOf(producto.getStock())};
    }

    private boolean registrarEntrada(String proveedor, String idProducto, String nombre, int cantidad, String precio) {
        List<String[]> entradas = leerEntradas();
        if (proveedor == null || proveedor.isBlank()) {
            proveedor = primerProveedor();
        }
        if (proveedor == null || proveedor.isBlank()) {
            proveedor = "PROV-01";
        }
        entradas.add(filaEntrada(new Recepcion(proveedor, idProducto, nombre, cantidad, precio)));
        return motorCSV.escribirCSV(ruta(ArchivosCSV.ENTRADAS), entradas, false);
    }

    private String primerProveedor() {
        List<String[]> proveedores = motorCSV.leerCSV(ruta(ArchivosCSV.PROVEEDORES));
        if (proveedores.size() > 1 && proveedores.get(1).length > 0) {
            return proveedores.get(1)[0].trim();
        }
        return "";
    }

    private String[] filaEntrada(Recepcion recepcion) {
        return new String[]{"ENT-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                recepcion.idProveedor, recepcion.idProducto, recepcion.nombre, String.valueOf(recepcion.cantidad),
                recepcion.precio, LocalDate.now().toString(), getCi()};
    }

    private boolean existeProveedor(String codigo) {
        for (String[] fila : motorCSV.leerCSV(ruta(ArchivosCSV.PROVEEDORES))) {
            if (fila.length > 0 && fila[0].equalsIgnoreCase(codigo)) {
                return true;
            }
        }
        return false;
    }

    private int columna(String[] encabezado, String nombre) {
        for (int i = 0; i < encabezado.length; i++) {
            if (encabezado[i].equalsIgnoreCase(nombre)) {
                return i;
            }
        }
        return -1;
    }

    private String ruta(String archivo) {
        return motorCSV.rutaDatos(archivo).toString();
    }

    private static final class Recepcion {
        private final String idProveedor;
        private final String idProducto;
        private final String nombre;
        private final int cantidad;
        private final String precio;

        private Recepcion(String idProveedor, String idProducto, String nombre, int cantidad, String precio) {
            this.idProveedor = idProveedor;
            this.idProducto = idProducto;
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precio = precio;
        }
    }
}