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

public class Venta {

    private static final String RUTA_SALIDAS = "salidas.csv";
    private static final String RUTA_INVENTARIO = "data/inventario.csv";

    private static final String[] SALIDAS_COLUMNAS = {
        "idVenta", "idProducto", "cliente(CI)", "nombreProducto",
        "cantidad", "precioUnitario", "fechaVenta"
    };
    private static final String[] INVENTARIO_COLUMNAS = {
        "idProducto", "nombre", "marca", "categoria", "descripcion", "precio", "stock"
    };

    private String idVenta;
    private LocalDate fechaVenta;
    private String clienteCi;
    private List<Producto> productosVendidos;

    public Venta(){}


    public Venta( String clienteCi, List<Producto> productosVendidos) {
        this.idVenta = generarIdVenta();
        this.fechaVenta = LocalDate.now() ;
        this.clienteCi = clienteCi;
        this.productosVendidos = productosVendidos;
    }
    //metodo que genera el id de venta en base al csv ventas o salida
    String generarIdVenta() {
        File archivo = new File(RUTA_SALIDAS);
        if (!archivo.exists() || archivo.length() == 0) {
            return "VEN-1";
        }

        String ultimoId = obtenerUltimoIdVenta(archivo);

        if (ultimoId == null || ultimoId.isBlank()) {
            return "VEN-1";
        }

        return "VEN-" + (Integer.parseInt(ultimoId.substring(4)) + 1);
    }
    //metodo que obtiene el ultimo id de venta del csv ventas o salida
    String obtenerUltimoIdVenta(File archivo) {
        CsvSchema esquema = CsvSchema.emptySchema().withHeader();
        CsvMapper mapper = new CsvMapper();
        String ultimoId = null;

        try (MappingIterator<Map<String, String>> registros = mapper
                .readerFor(Map.class)
                .with(esquema)
                .readValues(archivo)) {
            while (registros.hasNext()) {
                ultimoId = registros.next().get("idVenta");
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
            return;
        }
        escribirEnventaCsv();
        actualizarStockProductos();
        generarfactura();
    }
    ///metodo para factura
    public double calcularTotal() {
        if (productosVendidos == null) {
            return 0.0;
        }

        double total = 0.0;
        for (Producto producto : productosVendidos) {
            total += producto.getPrecio() * producto.getStock();
        }
        return total;
    }
    //metodo para descuenmto cuando list<prodcuto> .size() es amyor a 10
    public double aplicarDescuento(double total) {
        if (productosVendidos != null && productosVendidos.size() > 10) {
            return total * 0.9;
        }
        return total;
    }
    public  List<Producto> getProductosVendidos() {
        return productosVendidos;
    }
    public void generarfactura(){
        if (productosVendidos == null || productosVendidos.isEmpty()) {
            return;
        }

        double total = calcularTotal();
        double totalConDescuento = aplicarDescuento(total);

        System.out.println("========== FACTURA ==========");
        System.out.println("ID Venta:    " + idVenta);
        System.out.println("Fecha:       " + fechaVenta);
        System.out.println("Cliente CI:  " + clienteCi);
        System.out.println("-----------------------------");
        for (Producto producto : productosVendidos) {
            double subtotal = producto.getPrecio() * producto.getStock();
            System.out.printf("%s x%d ............ %.2f%n",
                    producto.getNombre(), producto.getStock(), subtotal);
        }
        System.out.println("-----------------------------");
        System.out.printf("Total:       %.2f%n", total);
        System.out.printf("A pagar:     %.2f%n", totalConDescuento);
        System.out.println("=============================");
    }
    ///metodo que añade las ventas al csv ventas o salida
    void escribirEnventaCsv() throws IOException {
        if (productosVendidos == null || productosVendidos.isEmpty()) {
            return;
        }

        List<Map<String, String>> salidas = leerFilas(RUTA_SALIDAS, SALIDAS_COLUMNAS);

        for (Producto producto : productosVendidos) {
            Map<String, String> fila = new LinkedHashMap<>();
            fila.put("idVenta", idVenta);
            fila.put("idProducto", producto.getIdProducto());
            fila.put("cliente(CI)", clienteCi);
            fila.put("nombreProducto", producto.getNombre());
            fila.put("cantidad", String.valueOf(producto.getStock()));
            fila.put("precioUnitario", String.valueOf(producto.getPrecio()));
            fila.put("fechaVenta", fechaVenta.toString());
            salidas.add(fila);
        }

        escribirFilas(RUTA_SALIDAS, salidas, SALIDAS_COLUMNAS);
    }
    //metodo que actuañliza eel stock en el csv de inventario
    void actualizarStockProductos() throws IOException {
        if (productosVendidos == null || productosVendidos.isEmpty()) {
            return;
        }

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
    //metodo par avalidar si stock en inventario.csv es 0 pues reurn false
    boolean validarStockProductos() {
        if (productosVendidos == null || productosVendidos.isEmpty()) {
            return false;
        }

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
                if (!encontrado) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    //metodo que lee todas las filas de un csv normalizandolas a las columnas indicadas
    private List<Map<String, String>> leerFilas(String ruta, String[] columnas) throws IOException {
        List<Map<String, String>> filas = new ArrayList<>();
        File archivo = new File(ruta);
        if (!archivo.exists() || archivo.length() == 0) {
            return filas;
        }

        CsvSchema esquema = CsvSchema.emptySchema().withHeader();
        List<?> leidas = new CsvMapper()
                .readerFor(Map.class)
                .with(esquema)
                .readValues(archivo)
                .readAll();

        for (Object filaLeida : leidas) {
            Map<?, ?> original = (Map<?, ?>) filaLeida;
            Map<String, String> normalizada = new LinkedHashMap<>();
            for (String columna : columnas) {
                Object valor = original.get(columna);
                normalizada.put(columna, valor == null ? "" : valor.toString());
            }
            filas.add(normalizada);
        }
        return filas;
    }

    //metodo que escribe las filas en un csv con el esquema de columnas indicado
    private void escribirFilas(String ruta, List<Map<String, String>> filas, String[] columnas)
            throws IOException {
        File archivo = new File(ruta);
        File carpeta = archivo.getAbsoluteFile().getParentFile();
        if (carpeta != null) {
            carpeta.mkdirs();
        }

        CsvSchema.Builder constructorEsquema = CsvSchema.builder();
        for (String columna : columnas) {
            constructorEsquema.addColumn(columna);
        }
        CsvSchema esquema = constructorEsquema.setUseHeader(true).build();

        new CsvMapper().writer(esquema).writeValues(archivo).writeAll(filas);
    }

}
