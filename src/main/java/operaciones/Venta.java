package operaciones;

import java.time.LocalDate;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.File;
import java.util.List;
import java.util.Map;



public class Venta {
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
        File archivo = new File("salidas.csv");
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
    public void registarVenta() {
        // Lógica para registrar la venta en el archivo CSV
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

    }
    ///metodo que añade las ventas al csv ventas o salida
    void escribirEnventaCsv(){

    }
    //metodo que actuañliza eel stock en el csv de inventario
    void actualizarStockProductos(){

    }
    //metodo par avalidar si stock en inventario.csv es 0 pues reurn false
    boolean validarStockProductos() {
      
      return false;

        // Lógica para validar si hay suficiente stock de los productos vendidos
    }

   
        
   




}

    
    
  