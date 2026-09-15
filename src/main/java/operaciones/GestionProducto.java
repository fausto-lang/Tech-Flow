package operaciones;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class GestionProducto {

    private List<Producto> productos;
    private String rutaArchivoCSV;

    public GestionProducto(String rutaArchivoCSV) {
        this.rutaArchivoCSV = rutaArchivoCSV;
        this.productos = new ArrayList<>();
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void cargarProductosCSV() {

       try {
           CsvMapper mapper = new CsvMapper();

           CsvSchema schema = CsvSchema.emptySchema().withHeader();

           List<?> filas = mapper
                .readerFor(Map.class)
                .with(schema)
                .readValues(new File(rutaArchivoCSV))
                .readAll();

            for (Object filaObjeto : filas) {
                Map<?, ?> fila = (Map<?, ?>) filaObjeto;

                Producto producto = new Producto(
                    (String) fila.get("idProducto"),
                    (String) fila.get("marca"),
                    Double.parseDouble((String) fila.get("precio")),
                    (String) fila.get("descripcion"),
                    Integer.parseInt((String) fila.get("stock")),
                    (String) fila.get("nombre"),
                    (String) fila.get("categoria")
                    );
                 productos.add(producto);
             }

        } catch (Exception e) {
             System.out.println("Error al cargar productos: " + e.getMessage());
    }
}

    public Producto buscarPorId(String idProducto) {

        for (Producto p : productos) {
            if (p.getIdProducto().equals(idProducto)) {
                return p;
            }
        }

        return null;
    }

    public List<Venta> ventasDia() {
        Path rutaSalidas = Path.of(rutaArchivoCSV).resolveSibling("salidas.csv");
        Map<String, List<Producto>> productosPorVenta = new LinkedHashMap<>();
        Map<String, String> clientesPorVenta = new LinkedHashMap<>();
        Map<String, LocalDate> fechasPorVenta = new LinkedHashMap<>();

        for (Map<String, String> fila : leerFilas(rutaSalidas)) {
            LocalDate fecha = LocalDate.parse(fila.get("fechaVenta"));
            if (!LocalDate.now().equals(fecha)) {
                continue;
            }

            String idVenta = fila.get("idVenta");
            Producto productoInventario = buscarPorId(fila.get("idProducto"));
            if (productoInventario == null) {
                throw new IllegalStateException(
                    "El producto de la venta no existe en el inventario: " + fila.get("idProducto"));
            }

            int cantidad = parsearEntero(fila.get("cantidad"), "cantidad");
            Producto productoVendido = new Producto(
                productoInventario.getIdProducto(),
                productoInventario.getMarca(),
                Double.parseDouble(fila.get("precioUnitario")),
                productoInventario.getDescripcion(),
                cantidad,
                fila.get("nombreProducto"),
                productoInventario.getCategoria()
            );

            productosPorVenta.computeIfAbsent(idVenta, clave -> new ArrayList<>()).add(productoVendido);
            clientesPorVenta.putIfAbsent(idVenta, fila.get("cliente(CI)"));
            fechasPorVenta.putIfAbsent(idVenta, fecha);
        }

        List<Venta> ventas = new ArrayList<>();
        for (Map.Entry<String, List<Producto>> entrada : productosPorVenta.entrySet()) {
            ventas.add(new Venta(
                entrada.getKey(),
                fechasPorVenta.get(entrada.getKey()),
                clientesPorVenta.get(entrada.getKey()),
                entrada.getValue()
            ));
        }
        return ventas;
    }

    public double gananciasTotalesDia() {
        double ganancias = 0.0;
        Path rutaSalidas = Path.of(rutaArchivoCSV).resolveSibling("salidas.csv");

        for (Map<String, String> fila : leerFilas(rutaSalidas)) {
            if (LocalDate.now().equals(LocalDate.parse(fila.get("fechaVenta")))) {
                int cantidad = parsearEntero(fila.get("cantidad"), "cantidad");
                ganancias += cantidad * Double.parseDouble(fila.get("precioUnitario"));
            }
        }
        return ganancias;
    }

    public List<Proveedor> proveedores() {
        Path rutaEntradas = Path.of(rutaArchivoCSV).resolveSibling("entradas.csv");
        Path rutaProveedores = Path.of(rutaArchivoCSV).resolveSibling("proveedores.csv");
        Map<String, Integer> contactos = new LinkedHashMap<>();

        for (Map<String, String> fila : leerFilas(rutaProveedores)) {
            contactos.put(fila.get("codigoProveedor"),
                parsearEnteroVacio(fila.get("contactoProveedor")));
        }

        Map<String, Proveedor> proveedores = new LinkedHashMap<>();
        for (Map<String, String> fila : leerFilas(rutaEntradas)) {
            String codigo = fila.get("idProveedor");
            Proveedor proveedor = proveedores.computeIfAbsent(codigo, clave ->
                new Proveedor(
                    fila.get("nombreProveedor"),
                    clave,
                    contactos.getOrDefault(clave, 0)
                )
            );

            Producto producto = buscarPorId(fila.get("idProducto"));
            if (producto == null) {
                throw new IllegalStateException(
                    "El producto de la entrada no existe en el inventario: " + fila.get("idProducto"));
            }
            proveedor.entregarProducto(producto);
        }
        return new ArrayList<>(proveedores.values());
    }

    private List<Map<String, String>> leerFilas(Path ruta) {
        if (!ruta.toFile().exists() || ruta.toFile().length() == 0) {
            return new ArrayList<>();
        }

        try {
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            List<?> leidas = new CsvMapper()
                .readerFor(Map.class)
                .with(schema)
                .readValues(ruta.toFile())
                .readAll();

            List<Map<String, String>> filas = new ArrayList<>();
            for (Object filaObjeto : leidas) {
                Map<?, ?> original = (Map<?, ?>) filaObjeto;
                Map<String, String> fila = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entrada : original.entrySet()) {
                    fila.put(String.valueOf(entrada.getKey()).trim(),
                        entrada.getValue() == null ? "" : entrada.getValue().toString().trim());
                }
                filas.add(fila);
            }
            return filas;
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer el archivo CSV: " + ruta, e);
        }
    }

    private int parsearEntero(String valor, String campo) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Valor inválido para " + campo + ": " + valor, e);
        }
    }

    private int parsearEnteroVacio(String valor) {
        return valor == null || valor.isBlank() ? 0 : parsearEntero(valor, "contactoProveedor");
    }

}