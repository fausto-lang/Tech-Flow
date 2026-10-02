package operaciones;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class Venta {

    private static final String RUTA_SALIDAS = "data/salidas.csv";
    private static final String RUTA_INVENTARIO = "data/inventario.csv";
    private static final String RUTA_CLIENTES = "data/clientes.csv";

    private static final String[] SALIDAS_COLUMNAS = {
        "idVenta", "idProducto", "cliente(CI)", "nombreProducto",
        "cantidad", "precioUnitario", "fechaVenta"
    };
    private static final String[] CLIENTES_COLUMNAS = {
        "ci", "nombreCliente", "idProducto", "nombreProducto", "cantidad", "fechaCompra"
    };
    private static final String[] INVENTARIO_COLUMNAS = {
        "idProducto", "nombre", "marca", "categoria", "descripcion", "precio", "stock"
    };

    private String idVenta;
    private LocalDate fechaVenta;
    private String clienteCi;
    private String nombreCliente;
    private List<Producto> productosVendidos;

    public Venta() {}

    public Venta(String clienteCi, String nombreCliente, List<Producto> productosVendidos) {
        this.idVenta = generarIdVenta();
        this.fechaVenta = LocalDate.now();
        this.clienteCi = clienteCi;
        this.nombreCliente = nombreCliente;
        this.productosVendidos = productosVendidos;
    }

    public Venta(String idVenta, LocalDate fechaVenta, String clienteCi, List<Producto> productosVendidos) {
        this.idVenta = idVenta;
        this.fechaVenta = fechaVenta;
        this.clienteCi = clienteCi;
        this.nombreCliente = null;
        this.productosVendidos = productosVendidos;
    }

    String generarIdVenta() {
        return generarIdVenta(new File(RUTA_SALIDAS));
    }

    String generarIdVenta(File archivo) {
        if (!archivo.exists() || archivo.length() == 0) {
            return "VEN-1";
        }

        String ultimoId = obtenerUltimoIdVenta(archivo);
        if (ultimoId == null || ultimoId.isBlank()) {
            return "VEN-1";
        }

        try {
            return "VEN-" + (Integer.parseInt(ultimoId.substring(4)) + 1);
        } catch (Exception e) {
            return "VEN-1";
        }
    }

    // Llevar a otra clase: MotorCSV
    String obtenerUltimoIdVenta(File archivo) {
        CsvSchema esquema = CsvSchema.emptySchema().withHeader();
        CsvMapper mapper = new CsvMapper();
        String ultimoId = null;

        try (MappingIterator<Map<String, String>> registros = mapper
                .readerFor(Map.class)
                .with(esquema)
                .readValues(archivo)) {
            while (registros.hasNext()) {
                String idLeido = registros.next().get("idVenta");
                if (idLeido != null && !idLeido.isBlank()) {
                    ultimoId = idLeido.trim();
                }
            }
        } catch (Exception e) {
            return null;
        }

        return ultimoId;
    }

    public void registarVenta() throws IOException {
        if (productosVendidos == null || productosVendidos.isEmpty()) {
            return;
        }
        if (!validarStockProductos()) {
            System.out.println("❌ No hay suficiente stock para realizar la venta.");
            return;
        }
        escribirEnventaCsv();
        escribirClientesCsv();
        actualizarStockProductos();
        generarfactura();
    }

    public double calcularTotal() {
        if (productosVendidos == null) return 0.0;
        double total = 0.0;
        for (Producto producto : productosVendidos) {
            total += producto.getPrecioVenta() * producto.getStock();
        }
        return total;
    }

    public double aplicarDescuento(double total) {
        if (productosVendidos != null && productosVendidos.size() > 10) {
            return total * 0.9;
        }
        return total;
    }

    public List<Producto> getProductosVendidos() {
        return productosVendidos;
    }

    public Cliente getCliente() {
        Cliente cliente = new Cliente(nombreCliente, clienteCi);
        if (productosVendidos != null) {
            for (Producto producto : productosVendidos) {
                cliente.agregarCompra(producto);
            }
        }
        return cliente;
    }

    void escribirClientesCsv() throws IOException {
        if (productosVendidos == null || productosVendidos.isEmpty()) return;

        List<Map<String, String>> clientes = leerFilas(RUTA_CLIENTES, CLIENTES_COLUMNAS);
        Cliente cliente = getCliente();

        for (Producto producto : cliente.getCompras()) {
            Map<String, String> fila = new LinkedHashMap<>();
            fila.put("ci", cliente.getCi());
            fila.put("nombreCliente", cliente.getNombre() == null ? "" : cliente.getNombre());
            fila.put("idProducto", producto.getIdProducto());
            fila.put("nombreProducto", producto.getNombre());
            fila.put("cantidad", String.valueOf(producto.getStock()));
            fila.put("fechaCompra", fechaVenta.toString());
            clientes.add(fila);
        }

        escribirFilas(RUTA_CLIENTES, clientes, CLIENTES_COLUMNAS);
    }

    public void generarfactura() {
        if (productosVendidos == null || productosVendidos.isEmpty()) return;

        double total = calcularTotal();
        double totalConDescuento = aplicarDescuento(total);

        System.out.println("\n========== FACTURA ==========");
        System.out.println("ID Venta:    " + idVenta);
        System.out.println("Fecha:       " + fechaVenta);
        System.out.println("Cliente CI:  " + clienteCi);
        System.out.println("-----------------------------");
        for (Producto producto : productosVendidos) {
            double subtotal = producto.getPrecioEntrada() * producto.getStock();
            System.out.printf("%s x%d ............ %.2f%n",
                    producto.getNombre(), producto.getStock(), subtotal);
        }
        System.out.println("-----------------------------");
        System.out.printf("Total:       %.2f%n", total);
        System.out.printf("A pagar:     %.2f%n", totalConDescuento);
        System.out.println("=============================\n");
    }

    void escribirEnventaCsv() throws IOException {
        if (productosVendidos == null || productosVendidos.isEmpty()) return;

        List<Map<String, String>> salidas = leerFilas(RUTA_SALIDAS, SALIDAS_COLUMNAS);

        for (Producto producto : productosVendidos) {
            Map<String, String> fila = new LinkedHashMap<>();
            fila.put("idVenta", idVenta);
            fila.put("idProducto", producto.getIdProducto());
            fila.put("cliente(CI)", clienteCi);
            fila.put("nombreProducto", producto.getNombre());
            fila.put("cantidad", String.valueOf(producto.getStock()));
            fila.put("precioUnitario", String.valueOf(producto.getPrecioVenta()));
            fila.put("fechaVenta", fechaVenta.toString());
            salidas.add(fila);
        }

        escribirFilas(RUTA_SALIDAS, salidas, SALIDAS_COLUMNAS);
    }

    void actualizarStockProductos() throws IOException {
        if (productosVendidos == null || productosVendidos.isEmpty()) return;

        List<Map<String, String>> inventario = leerFilas(RUTA_INVENTARIO, INVENTARIO_COLUMNAS);

        for (Producto producto : productosVendidos) {
            for (Map<String, String> fila : inventario) {
                if (producto.getIdProducto().equals(fila.get("idProducto"))) {
                    int stockActual = Integer.parseInt(fila.get("stock"));
                    fila.put("stock", String.valueOf(stockActual - producto.getStock()));
                    break;
                }
            }
        }

        escribirFilas(RUTA_INVENTARIO, inventario, INVENTARIO_COLUMNAS);
    }

    boolean validarStockProductos() {
        if (productosVendidos == null || productosVendidos.isEmpty()) return false;

        try {
            List<Map<String, String>> inventario = leerFilas(RUTA_INVENTARIO, INVENTARIO_COLUMNAS);

            for (Producto producto : productosVendidos) {
                boolean encontrado = false;
                for (Map<String, String> fila : inventario) {
                    if (producto.getIdProducto().equals(fila.get("idProducto"))) {
                        encontrado = true;
                        if (Integer.parseInt(fila.get("stock")) < producto.getStock()) {
                            return false;
                        }
                        break;
                    }
                }
                if (!encontrado) return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Llevar a otra clase: MotorCSV
    private List<Map<String, String>> leerFilas(String ruta, String[] columnas) throws IOException {
        List<Map<String, String>> filas = new ArrayList<>();
        File archivo = new File(ruta);
        if (!archivo.exists() || archivo.length() == 0) return filas;

        CsvSchema esquema = CsvSchema.emptySchema().withHeader();
        try (MappingIterator<Map<String, String>> registros = new CsvMapper()
                .readerFor(Map.class)
                .with(esquema)
                .readValues(archivo)) {
            while (registros.hasNext()) {
                Map<?, ?> original = registros.next();
                Map<String, String> normalizada = new LinkedHashMap<>();
                for (String columna : columnas) {
                    Object valor = original.get(columna);
                    normalizada.put(columna, valor == null ? "" : valor.toString().trim());
                }
                filas.add(normalizada);
            }
        }
        return filas;
    }

    // Llevar a otra clase: MotorCSV
    private void escribirFilas(String ruta, List<Map<String, String>> filas, String[] columnas) throws IOException {
        File archivo = new File(ruta);
        File carpeta = archivo.getAbsoluteFile().getParentFile();
        if (carpeta != null) carpeta.mkdirs();

        CsvSchema.Builder constructorEsquema = CsvSchema.builder();
        for (String columna : columnas) {
            constructorEsquema.addColumn(columna);
        }
        CsvSchema esquema = constructorEsquema.setUseHeader(true).build();

        try (SequenceWriter escritor = new CsvMapper().writer(esquema).writeValues(archivo)) {
            escritor.writeAll(filas);
        }
    }
}