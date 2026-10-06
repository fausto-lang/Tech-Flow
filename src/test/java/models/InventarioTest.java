package models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Inventario")
class InventarioTest {

    private Producto producto(String id, int stock) {
        return new Producto(id, "Producto " + id, 10, 15, "desc", stock, "Marca", "Cat");
    }

    @Test
    @DisplayName("Registra productos únicos y busca por id")
    void registraYBuscaProductos() {
        Inventario inventario = new Inventario();
        Producto producto = producto("P1", 10);
        assertTrue(inventario.registrarProducto(producto));
        assertSame(producto, inventario.buscarEnInventario("P1"));
        assertFalse(inventario.registrarProducto(producto("P1", 5)));
        assertFalse(inventario.registrarProducto(null));
        assertNull(inventario.buscarEnInventario("NO-EXISTE"));
        assertNull(inventario.buscarEnInventario(null));
    }

    @Test
    @DisplayName("Actualiza existencias recibidas")
    void actualizarStock() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        assertTrue(inventario.actualizarStock("P1", 3));
        assertEquals(8, inventario.buscarEnInventario("P1").getStock());
        assertFalse(inventario.actualizarStock("P1", 0));
        assertFalse(inventario.actualizarStock("NO-EXISTE", 2));
    }

    @Test
    @DisplayName("Venta descuenta existencias y actualiza popularidad")
    void registraVentas() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        assertTrue(inventario.registrarVenta("P1", 2));
        assertFalse(inventario.registrarVenta("P1", 4));
        assertFalse(inventario.registrarVenta("P1", 0));
        assertEquals(3, inventario.buscarEnInventario("P1").getStock());
        assertEquals(2, inventario.obtenerPopularidadProductos().get("P1"));
    }

    @Test
    @DisplayName("Lista productos con cinco unidades o menos")
    void listarStockBajo() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        inventario.registrarProducto(producto("P2", 6));
        assertEquals(1, inventario.listarProductosProximosAAgotarse().size());
        assertNotNull(assertThrows(UnsupportedOperationException.class,
            () -> inventario.listarProductosProximosAAgotarse().clear()));
    }
}