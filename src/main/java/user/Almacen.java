package user;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import models.Producto;
import services.MotorCSV;

/** Operador responsable de productos, existencias y registro de entradas. */
public class Almacen extends Empleado {

    private static final String ARCHIVO_INVENTARIO = "inventario.csv";
    private static final String ARCHIVO_ENTRADAS = "entradas.csv";
    private static final String[] CABECERA_INVENTARIO = {"idProducto", "nombre", "marca", "categoria",
            "descripcion", "precioEntrada", "precioVenta", "stock"};
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
        if (!motorCSV.escribirCSV(ruta(ARCHIVO_INVENTARIO), inventario, false)) {
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
        int nuevoStock;
        try {
            nuevoStock = Integer.parseInt(producto[7]) + cantidad;
        } catch (NumberFormatException e) {
            return false;
        }
        producto[7] = String.valueOf(nuevoStock);
        if (!motorCSV.escribirCSV(ruta(ARCHIVO_INVENTARIO), inventario, false)) {
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
        if (idProveedor < 0 || idProducto < 0 || cantidad < 0) {
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
                String[] producto = buscarFila(inventario, id);
                double precioEntrada = producto == null ? 0 : Double.parseDouble(producto[5]);
                if (precio >= 0) {
                    if (fila.length <= precio) {
                        return false;
                    }
                    precioEntrada = Double.parseDouble(fila[precio]);
                    if (precioEntrada < 0) {
                        return false;
                    }
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
                    producto = new String[]{id, fila[nombreProducto], fila[marca], fila[categoria], "",
                            String.valueOf(precioEntrada), String.valueOf(precioEntrada * 1.3), "0"};
                    inventario.add(producto);
                } else if (precio >= 0) {
                    producto[5] = String.valueOf(precioEntrada);
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
            producto[7] = String.valueOf(Integer.parseInt(producto[7]) + recepcion.cantidad);
            nuevasEntradas.add(filaEntrada(recepcion));
        }
        entradas.addAll(nuevasEntradas);
        return motorCSV.escribirCSV(ruta(ARCHIVO_INVENTARIO), inventario, false)
                && motorCSV.escribirCSV(ruta(ARCHIVO_ENTRADAS), entradas, false);
    }

    private boolean productoValido(Producto producto) {
        return producto != null && producto.getIdProducto() != null && !producto.getIdProducto().isBlank()
                && producto.getNombre() != null && !producto.getNombre().isBlank()
                && producto.getMarca() != null && producto.getCategoria() != null
                && producto.getDescripcion() != null && producto.getStock() >= 0
                && producto.getPrecioEntrada() >= 0 && producto.getPrecioVenta() >= 0;
    }

    private List<String[]> leerInventario() {
        List<String[]> filas = motorCSV.leerCSV(ruta(ARCHIVO_INVENTARIO));
        if (filas.isEmpty()) {
            filas.add(CABECERA_INVENTARIO.clone());
        }
        return filas;
    }

    private List<String[]> leerEntradas() {
        List<String[]> filas = motorCSV.leerCSV(ruta(ARCHIVO_ENTRADAS));
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
                producto.getCategoria(), producto.getDescripcion(), String.valueOf(producto.getPrecioEntrada()),
                String.valueOf(producto.getPrecioVenta()), String.valueOf(producto.getStock())};
    }

    private boolean registrarEntrada(String proveedor, String idProducto, String nombre, int cantidad, String precio) {
        List<String[]> entradas = leerEntradas();
        entradas.add(filaEntrada(new Recepcion(proveedor, idProducto, nombre, cantidad, precio)));
        return motorCSV.escribirCSV(ruta(ARCHIVO_ENTRADAS), entradas, false);
    }

    private String[] filaEntrada(Recepcion recepcion) {
        return new String[]{"ENT-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                recepcion.idProveedor, recepcion.idProducto, recepcion.nombre, String.valueOf(recepcion.cantidad),
                recepcion.precio, LocalDate.now().toString(), getCi()};
    }

    private boolean existeProveedor(String codigo) {
        for (String[] fila : motorCSV.leerCSV(ruta("proveedores.csv"))) {
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