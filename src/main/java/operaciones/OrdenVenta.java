package operaciones;

import java.util.HashMap;
import java.util.Map;

public class OrdenVenta {
    private final Map<Producto, Integer> items = new HashMap<>();

    public void agregarProducto(Producto p, int cantidad) {
        items.put(p, items.getOrDefault(p, 0) + cantidad);
    }

    public void modificarCantidad(Producto p, int nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            items.remove(p);
        } else {
            items.put(p, nuevaCantidad);
        }
    }

    public void eliminarProducto(Producto p) {
        items.remove(p);
    }

    public Map<Producto, Integer> getItems() {
        return items;
    }

    public double calcularTotal() {
        return items.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrecioVenta() * entry.getValue())
                .sum();
    }

    public String generarProforma() {
        StringBuilder proforma = new StringBuilder("=== PROFORMA (NO VALIDA COMO FACTURA) ===\n");
        for (Map.Entry<Producto, Integer> entry : items.entrySet()) {
            Producto p = entry.getKey();
            int cant = entry.getValue();
            proforma.append(p.getNombre()).append(" x").append(cant)
                    .append(" - Bs. ").append(p.getPrecioVenta() * cant).append("\n");
        }
        proforma.append("TOTAL A PAGAR: Bs. ").append(calcularTotal()).append("\n");
        return proforma.toString();
    }
}