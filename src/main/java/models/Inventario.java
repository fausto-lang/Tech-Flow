package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Inventario en memoria para reglas de dominio y operaciones de stock. */
public class Inventario {

    private static final int STOCK_CRITICO = 5;
    private final Map<String, Producto> productos = new HashMap<>();
    private final Map<String, Integer> unidadesVendidas = new HashMap<>();

    public boolean registrarProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() == null || producto.getIdProducto().isBlank()
                || producto.getStock() < 0 || productos.containsKey(producto.getIdProducto())) {
            return false;
        }
        productos.put(producto.getIdProducto(), producto);
        return true;
    }

    public Producto buscarEnInventario(String codigo) {
        return codigo == null ? null : productos.get(codigo);
    }

    /** Suma unidades recibidas al stock de un producto existente. */
    public boolean actualizarStock(String codigo, int cantidad) {
        Producto producto = buscarEnInventario(codigo);
        if (producto == null || cantidad <= 0) {
            return false;
        }
        producto.setStock(producto.getStock() + cantidad);
        return true;
    }

    /** Descuenta stock y registra unidades vendidas; falla si no hay existencias. */
    public boolean registrarVenta(String codigo, int cantidad) {
        Producto producto = buscarEnInventario(codigo);
        if (producto == null || cantidad <= 0 || producto.getStock() < cantidad) {
            return false;
        }
        producto.setStock(producto.getStock() - cantidad);
        unidadesVendidas.merge(codigo, cantidad, Integer::sum);
        return true;
    }

    public List<Producto> listarProductosProximosAAgotarse() {
        List<Producto> resultado = new ArrayList<>();
        for (Producto producto : productos.values()) {
            if (producto.getStock() <= STOCK_CRITICO) {
                resultado.add(producto);
            }
        }
        return Collections.unmodifiableList(resultado);
    }

    public Map<String, Integer> obtenerPopularidadProductos() {
        return Collections.unmodifiableMap(new HashMap<>(unidadesVendidas));
    }
}