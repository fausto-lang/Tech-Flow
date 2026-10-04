package models;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Inventario {
    private Map<String, Producto> productos;
    public Producto buscarEnInventario(String codigo) {
        return new Producto("PROD01", "Producto Ejemplo", 10.0, 2.0,"es un buen producto", 100, "Marca Ejemplo", "Categoría Ejemplo");
    }

    public boolean actualizarStock(String codigo, int cantidad) {
        return true;
    }

    public List<Producto> listarProductosProximosAAgotarse() {
        return Collections.emptyList();
    }

    public Map<String, Integer> obtenerPopularidadProductos() {
        return Collections.emptyMap();
    }
}