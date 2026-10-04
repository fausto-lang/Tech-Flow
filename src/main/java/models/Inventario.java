package models;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Inventario {
    private Map<String, Producto> productos;

    /**
     * Busca un producto en el inventario a través de su código identificador.
     *
     * @param codigo Código del producto.
     * @return El objeto {@link Producto} correspondiente.
     */
    public Producto buscarEnInventario(String codigo) {
        return new Producto("PROD01", "Producto Ejemplo", 10.0, 2.0,"es un buen producto", 100, "Marca Ejemplo", "Categoría Ejemplo");
    }

    /**
     * Actualiza la cantidad de stock disponible para un producto.
     *
     * @param codigo   Código del producto.
     * @param cantidad Cantidad a modificar en el stock.
     * @return {@code true} si la actualización fue exitosa.
     */
    public boolean actualizarStock(String codigo, int cantidad) {
        return true;
    }

    /**
     * Obtiene la lista de productos cuyo stock está cercano a agotarse.
     *
     * @return Lista de productos con bajo stock.
     */
    public List<Producto> listarProductosProximosAAgotarse() {
        return Collections.emptyList();
    }

    /**
     * Obtiene la popularidad de los productos en el inventario.
     *
     * @return Mapa con el código del producto y su popularidad.
     */
    public Map<String, Integer> obtenerPopularidadProductos() {
        return Collections.emptyMap();
    }
}