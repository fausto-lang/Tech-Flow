package operaciones;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServicioInventario {

    private final MotorCSV motorCSV;

    public ServicioInventario(MotorCSV motorCSV) {
        this.motorCSV = motorCSV;
    }

    public List<Producto> buscarProducto(List<Producto> stock, String nombre, String marca) {
        return stock.stream()
            .filter(p -> (nombre == null || p.getNombre().toLowerCase().contains(nombre.toLowerCase())))
            .filter(p -> (marca == null || p.getMarca().toLowerCase().contains(marca.toLowerCase())))
            .collect(Collectors.toList());
    }

    public void registrarNuevoProducto(Producto p, List<Producto> stockActual) throws IOException {
        boolean existe = stockActual.stream().anyMatch(prod -> prod.getIdProducto().equals(p.getIdProducto()));
        if (existe) {
            throw new IllegalArgumentException("El ID del producto ya existe.");
        }

        stockActual.add(p);
        
        guardarInventarioEnCSV(stockActual);
    }

    public void registrarIngresoStock(Producto p, int cantidadIngresada, List<Producto> stockActual) throws IOException {
        p.setStock(p.getStock() + cantidadIngresada);
        
        guardarInventarioEnCSV(stockActual);
    }

    private void guardarInventarioEnCSV(List<Producto> inventario) throws IOException {
        List<Map<String, String>> filas = new java.util.ArrayList<>();
        
        for (Producto p : inventario) {
            Map<String, String> fila = new java.util.LinkedHashMap<>();
            fila.put("idProducto", p.getIdProducto());
            fila.put("nombre", p.getNombre());
            fila.put("marca", p.getMarca());
            fila.put("categoria", p.getCategoria());
            fila.put("descripcion", p.getDescripcion());
            fila.put("precio", String.valueOf(p.getPrecioVenta())); 
            fila.put("stock", String.valueOf(p.getStock()));
            
            filas.add(fila);
        }
        
        motorCSV.escribirFilas(ConfiguracionCSV.RUTA_INVENTARIO, filas, ConfiguracionCSV.INVENTARIO_COLUMNAS);
    }

    public List<Producto> obtenerInventario() throws IOException {
        List<Map<String, String>> filas = motorCSV.leerFilas(ConfiguracionCSV.RUTA_INVENTARIO, ConfiguracionCSV.INVENTARIO_COLUMNAS);
        List<Producto> stock = new java.util.ArrayList<>();

        for (Map<String, String> fila : filas) {
            Producto p = new Producto(
                fila.get("idProducto"),
                fila.get("marca"),
                0.0,
                Double.parseDouble(fila.get("precio")),
                fila.get("descripcion"),
                Integer.parseInt(fila.get("stock")),
                fila.get("nombre"),
                fila.get("categoria")
            );
            stock.add(p);
        }
        return stock;
    }
}