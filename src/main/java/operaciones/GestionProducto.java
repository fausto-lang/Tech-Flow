package operaciones;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class GestionProducto {

    private static final String RUTA_INVENTARIO =
            "data/inventario.csv";

    private static final String RUTA_PROVEEDORES =
            "data/proveedores.csv";

    private static final String RUTA_SALIDAS =
            "data/salidas.csv";

    private static final String RUTA_CLIENTES =
            "data/clientes.csv";

    private static final String[] INVENTARIO_COLUMNAS = {
        "idProducto",
        "nombre",
        "marca",
        "categoria",
        "descripcion",
        "precio",
        "stock"
    };

    private static final String[] PROVEEDORES_COLUMNAS = {
        "codigoProveedor",
        "nombreProveedor",
        "contactoProveedor",
        "fechaEntrega"
    };

    private static final String[] SALIDAS_COLUMNAS = {
        "idVenta",
        "idProducto",
        "cliente(CI)",
        "nombreProducto",
        "cantidad",
        "precioUnitario",
        "fechaVenta"
    };

    private static final String[] CLIENTES_COLUMNAS = {
        "ci",
        "nombreCliente",
        "idProducto",
        "nombreProducto",
        "cantidad",
        "fechaCompra"
    };

    // ==========================================================
    // STOCK ACTUAL
    // ==========================================================

    public Map<String, Integer> stockActual() throws IOException {

        Map<String, Integer> stock =
                new LinkedHashMap<>();

        List<Map<String, String>> filas =
                leerFilas(
                        RUTA_INVENTARIO,
                        INVENTARIO_COLUMNAS
                );

        for (Map<String, String> fila : filas) {

            try {
                stock.put(
                        fila.get("idProducto"),
                        Integer.parseInt(
                                fila.get("stock")
                        )
                );
            } catch (Exception e) {
                stock.put(
                        fila.get("idProducto"),
                        0
                );
            }
        }

        return stock;
    }

    // ==========================================================
    // INVENTARIO COMPLETO
    // ==========================================================

    public Map<String, Producto> obtenerInventarioCompleto()
            throws IOException {

        Map<String, Producto> inventario =
                new LinkedHashMap<>();

        List<Map<String, String>> filas =
                leerFilas(
                        RUTA_INVENTARIO,
                        INVENTARIO_COLUMNAS
                );

        for (Map<String, String> fila : filas) {

            double precio = 0;
            int stock = 0;

            try {
                precio = Double.parseDouble(
                        fila.get("precio")
                );
            } catch (Exception e) {
                precio = 0;
            }

            try {
                stock = Integer.parseInt(
                        fila.get("stock")
                );
            } catch (Exception e) {
                stock = 0;
            }

            Producto producto = new Producto(
                    fila.get("idProducto"),
                    fila.get("marca"),
                    precio,
                    fila.get("descripcion"),
                    stock,
                    fila.get("nombre"),
                    fila.get("categoria")
            );

            inventario.put(
                    fila.get("idProducto"),
                    producto
            );
        }

        return inventario;
    }

    // ==========================================================
    // PROVEEDORES
    // ==========================================================

    public List<Proveedor> proveedores()
            throws IOException {

        List<Proveedor> lista =
                new ArrayList<>();

        List<Map<String, String>> filas =
                leerFilas(
                        RUTA_PROVEEDORES,
                        PROVEEDORES_COLUMNAS
                );

        for (Map<String, String> fila : filas) {

            int contacto = 0;

            try {
                contacto = Integer.parseInt(
                        fila.get("contactoProveedor")
                );
            } catch (Exception e) {
                contacto = 0;
            }

            lista.add(
                    new Proveedor(
                            fila.get("nombreProveedor"),
                            fila.get("codigoProveedor"),
                            contacto
                    )
            );
        }

        return lista;
    }

    // ==========================================================
    // VENTAS DEL DÍA
    // ==========================================================

    public List<Venta> ventasDia(
            LocalDate fecha) throws IOException {

        List<Venta> ventas =
                new ArrayList<>();

        List<Map<String, String>> filas =
                leerFilas(
                        RUTA_SALIDAS,
                        SALIDAS_COLUMNAS
                );

        Map<String, Venta> ventasAgrupadas =
                new LinkedHashMap<>();

        for (Map<String, String> fila : filas) {

            String fechaVenta =
                    fila.get("fechaVenta");

            if (!fecha.toString().equals(fechaVenta)) {
                continue;
            }

            String idVenta =
                    fila.get("idVenta");

            Venta venta =
                    ventasAgrupadas.get(idVenta);

            if (venta == null) {

                List<Producto> productos =
                        new ArrayList<>();

                venta = new Venta(
                        idVenta,
                        fecha,
                        fila.get("cliente(CI)"),
                        productos
                );

                ventasAgrupadas.put(
                        idVenta,
                        venta
                );
            }

            double precio = 0;
            int cantidad = 0;

            try {
                precio = Double.parseDouble(
                        fila.get("precioUnitario")
                );
            } catch (Exception e) {
                precio = 0;
            }

            try {
                cantidad = Integer.parseInt(
                        fila.get("cantidad")
                );
            } catch (Exception e) {
                cantidad = 0;
            }

            Producto producto =
                    new Producto(
                            fila.get("idProducto"),
                            "",
                            precio,
                            "",
                            cantidad,
                            fila.get("nombreProducto"),
                            ""
                    );

            venta.getProductosVendidos()
                    .add(producto);
        }

        ventas.addAll(
                ventasAgrupadas.values()
        );

        return ventas;
    }

    // ==========================================================
    // CALCULAR VENTAS DEL DÍA
    // ==========================================================

    public double calcularVentasDelDia()
            throws IOException {

        double total = 0;

        List<Venta> ventas =
                ventasDia(LocalDate.now());

        for (Venta venta : ventas) {
            total += venta.calcularTotal();
        }

        return total;
    }

    // ==========================================================
    // HISTORIAL DEL CLIENTE
    // ==========================================================

    public Cliente historialCliente(
            String ci) throws IOException {

        if (ci == null || ci.isBlank()) {
            return null;
        }

        List<Map<String, String>> filas =
                leerFilas(
                        RUTA_CLIENTES,
                        CLIENTES_COLUMNAS
                );

        Cliente cliente = null;

        for (Map<String, String> fila : filas) {

            if (!ci.equalsIgnoreCase(
                    fila.get("ci"))) {
                continue;
            }

            if (cliente == null) {
                cliente = new Cliente(
                        fila.get("nombreCliente"),
                        fila.get("ci")
                );
            }

            int cantidad = 0;

            try {
                cantidad = Integer.parseInt(
                        fila.get("cantidad")
                );
            } catch (Exception e) {
                cantidad = 0;
            }

            Producto producto =
                    new Producto(
                            fila.get("idProducto"),
                            "",
                            0,
                            "",
                            cantidad,
                            fila.get("nombreProducto"),
                            ""
                    );

            cliente.agregarCompra(producto);
        }

        return cliente;
    }

    // ==========================================================
    // LEER CSV
    // ==========================================================

    private List<Map<String, String>> leerFilas(
            String ruta,
            String[] columnas) throws IOException {

        List<Map<String, String>> filas =
                new ArrayList<>();

        File archivo = new File(ruta);

        if (!archivo.exists() ||
            archivo.length() == 0) {
            return filas;
        }

        CsvSchema esquema =
                CsvSchema.emptySchema().withHeader();

        try (MappingIterator<Map<String, String>> registros =
                new CsvMapper()
                        .readerFor(Map.class)
                        .with(esquema)
                        .readValues(archivo)) {

            while (registros.hasNext()) {

                Map<?, ?> original =
                        registros.next();

                Map<String, String> normalizada =
                        new LinkedHashMap<>();

                for (String columna : columnas) {

                    Object valor =
                            original.get(columna);

                    normalizada.put(
                            columna,
                            valor == null
                                    ? ""
                                    : valor.toString().trim()
                    );
                }

                filas.add(normalizada);
            }
        }

        return filas;
    }
}