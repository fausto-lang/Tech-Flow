package operaciones;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EntradaProducto {

    private static final String[] ENTRADAS_COLUMNAS = {
        "idProveedor", "nombreProveedor", "idProducto", "nombreProducto",
        "marca", "categoria", "precio", "cantidad", "fechaEntrada"
    };
    private static final String[] INVENTARIO_COLUMNAS = {
        "idProducto", "nombre", "marca", "categoria", "descripcion", "precio", "stock"
    };
    private static final String[] PROVEEDORES_COLUMNAS = {
        "codigoProveedor", "nombreProveedor", "contactoProveedor", "fechaEntrega"
    };

    private final Path rutaEntradas;
    private final Path rutaInventario;
    private final Path rutaProveedores;
    private final MotorCSV motorCSV;

    public EntradaProducto() {
        this("data/entradas.csv", "data/inventario.csv", "data/proveedores.csv");
    }

    public EntradaProducto(String rutaEntradas, String rutaProveedores) {
        this(rutaEntradas, "data/inventario.csv", rutaProveedores);
    }

    public EntradaProducto(String rutaEntradas, String rutaInventario, String rutaProveedores) {
        this.rutaEntradas = Path.of(rutaEntradas);
        this.rutaInventario = Path.of(rutaInventario);
        this.rutaProveedores = Path.of(rutaProveedores);
        this.motorCSV = new MotorCSV();
    }

    public Proveedor buscarProveedor(String codigoProveedor) throws IOException {
        if (codigoProveedor == null || codigoProveedor.isBlank()) return null;

        List<Map<String, String>> proveedores = motorCSV.leerFilas(rutaProveedores.toString(), PROVEEDORES_COLUMNAS);
        for (Map<String, String> fila : proveedores) {
            if (codigoProveedor.trim().equalsIgnoreCase(fila.get("codigoProveedor").trim())) {
                int contacto = motorCSV.parsearEntero(fila.get("contactoProveedor"));
                return new Proveedor(fila.get("nombreProveedor"), fila.get("codigoProveedor"), contacto);
            }
        }
        return null;
    }

    public Producto buscarProducto(String idProducto) throws IOException {
        if (idProducto == null || idProducto.isBlank()) return null;

        List<Map<String, String>> inventario = motorCSV.leerFilas(rutaInventario.toString(), INVENTARIO_COLUMNAS);
        for (Map<String, String> fila : inventario) {
            if (idProducto.trim().equalsIgnoreCase(fila.get("idProducto").trim())) {
                double precio = motorCSV.parsearDouble(fila.get("precio"));
                int stock = motorCSV.parsearEntero(fila.get("stock"));
                return new Producto(
                    fila.get("idProducto"),
                    fila.get("marca"),
                    precio,
                    fila.get("descripcion"),
                    stock,
                    fila.get("nombre"),
                    fila.get("categoria")
                );
            }
        }
        return null;
    }

    public void registrarPedido(Proveedor proveedor, Producto producto, int cantidad,
            LocalDate fechaEntrega) throws IOException {
        if (proveedor == null || producto == null) {
            throw new IllegalArgumentException("El proveedor y el producto son obligatorios");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        if (fechaEntrega == null) {
            throw new IllegalArgumentException("La fecha de entrega es obligatoria");
        }

        List<Map<String, String>> entradas = motorCSV.leerFilas(rutaEntradas.toString(), ENTRADAS_COLUMNAS);
        boolean productoEncontrado = false;

        for (Map<String, String> entrada : entradas) {
            if (producto.getIdProducto().equals(entrada.get("idProducto"))) {
                int cantidadActual = motorCSV.parsearEntero(entrada.get("cantidad"));
                entrada.put("cantidad", String.valueOf(cantidadActual + cantidad));
                entrada.put("fechaEntrada", fechaEntrega.toString());
                productoEncontrado = true;
                break;
            }
        }

        if (!productoEncontrado) {
            Map<String, String> nuevaEntrada = new LinkedHashMap<>();
            nuevaEntrada.put("idProveedor", proveedor.getCodigoProveedor());
            nuevaEntrada.put("nombreProveedor", proveedor.getNombreProveedor());
            nuevaEntrada.put("idProducto", producto.getIdProducto());
            nuevaEntrada.put("nombreProducto", producto.getNombre());
            nuevaEntrada.put("marca", producto.getMarca());
            nuevaEntrada.put("categoria", producto.getCategoria());
            nuevaEntrada.put("precio", String.valueOf(producto.getPrecio()));
            nuevaEntrada.put("cantidad", String.valueOf(cantidad));
            nuevaEntrada.put("fechaEntrada", fechaEntrega.toString());
            entradas.add(nuevaEntrada);
        }

        motorCSV.escribirFilas(rutaEntradas.toString(), entradas, ENTRADAS_COLUMNAS);

        actualizarInventario(producto, cantidad);
        registrarProveedor(proveedor, fechaEntrega);
    }

    private void actualizarInventario(Producto producto, int cantidad) throws IOException {
        List<Map<String, String>> inventario = motorCSV.leerFilas(rutaInventario.toString(), INVENTARIO_COLUMNAS);
        for (Map<String, String> fila : inventario) {
            if (producto.getIdProducto().equals(fila.get("idProducto"))) {
                int stockActual = motorCSV.parsearEntero(fila.get("stock"));
                fila.put("stock", String.valueOf(stockActual + cantidad));
                motorCSV.escribirFilas(rutaInventario.toString(), inventario, INVENTARIO_COLUMNAS);
                return;
            }
        }

        Map<String, String> nuevaFila = new LinkedHashMap<>();
        nuevaFila.put("idProducto", producto.getIdProducto());
        nuevaFila.put("nombre", producto.getNombre());
        nuevaFila.put("marca", producto.getMarca());
        nuevaFila.put("categoria", producto.getCategoria());
        nuevaFila.put("descripcion", producto.getDescripcion());
        nuevaFila.put("precio", String.valueOf(producto.getPrecio()));
        nuevaFila.put("stock", String.valueOf(cantidad));
        inventario.add(nuevaFila);
        motorCSV.escribirFilas(rutaInventario.toString(), inventario, INVENTARIO_COLUMNAS);
    }

    private void registrarProveedor(Proveedor proveedor, LocalDate fechaEntrega) throws IOException {
        List<Map<String, String>> proveedores = motorCSV.leerFilas(rutaProveedores.toString(), PROVEEDORES_COLUMNAS);
        for (Map<String, String> fila : proveedores) {
            if (proveedor.getCodigoProveedor().equals(fila.get("codigoProveedor"))) {
                fila.put("fechaEntrega", fechaEntrega.toString());
                motorCSV.escribirFilas(rutaProveedores.toString(), proveedores, PROVEEDORES_COLUMNAS);
                return;
            }
        }

        Map<String, String> entrega = new LinkedHashMap<>();
        entrega.put("codigoProveedor", proveedor.getCodigoProveedor());
        entrega.put("nombreProveedor", proveedor.getNombreProveedor());
        entrega.put("contactoProveedor", String.valueOf(proveedor.getContactoProveedor()));
        entrega.put("fechaEntrega", fechaEntrega.toString());
        proveedores.add(entrega);
        motorCSV.escribirFilas(rutaProveedores.toString(), proveedores, PROVEEDORES_COLUMNAS);
    }
}