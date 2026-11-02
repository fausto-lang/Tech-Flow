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
    @DisplayName("Registrar un producto unico devuelve true")
    void registrarProductoUnicoDevuelveTrue() {
        Inventario inventario = new Inventario();
        Producto producto = producto("P1", 10);
        assertTrue(inventario.registrarProducto(producto));
    }

    @Test
    @DisplayName("Buscar un producto registrado devuelve la misma instancia")
    void buscarProductoRegistradoDevuelveMismaInstancia() {
        Inventario inventario = new Inventario();
        Producto producto = producto("P1", 10);
        inventario.registrarProducto(producto);
        assertSame(producto, inventario.buscarEnInventario("P1"));
    }

    @Test
    @DisplayName("Registrar un producto duplicado devuelve false")
    void registrarProductoDuplicadoDevuelveFalse() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 10));
        assertFalse(inventario.registrarProducto(producto("P1", 5)));
    }

    @Test
    @DisplayName("Registrar un producto nulo devuelve false")
    void registrarProductoNuloDevuelveFalse() {
        assertFalse(new Inventario().registrarProducto(null));
    }

    @Test
    @DisplayName("Buscar un producto inexistente devuelve null")
    void buscarProductoInexistenteDevuelveNull() {
        assertNull(new Inventario().buscarEnInventario("NO-EXISTE"));
    }

    @Test
    @DisplayName("Buscar un producto con id nulo devuelve null")
    void buscarProductoConIdNuloDevuelveNull() {
        assertNull(new Inventario().buscarEnInventario(null));
    }

    @Test
    @DisplayName("Actualizar el stock de un producto existente devuelve true")
    void actualizarStockExistenteDevuelveTrue() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        assertTrue(inventario.actualizarStock("P1", 3));
    }

    @Test
    @DisplayName("Actualizar el stock incrementa las existencias")
    void actualizarStockIncrementaExistencias() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        inventario.actualizarStock("P1", 3);
        assertEquals(8, inventario.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("Actualizar el stock con cantidad cero devuelve false")
    void actualizarStockCantidadCeroDevuelveFalse() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        assertFalse(inventario.actualizarStock("P1", 0));
    }

    @Test
    @DisplayName("Actualizar el stock de un producto inexistente devuelve false")
    void actualizarStockProductoInexistenteDevuelveFalse() {
        assertFalse(new Inventario().actualizarStock("NO-EXISTE", 2));
    }

    @Test
    @DisplayName("Registrar una venta valida devuelve true")
    void registrarVentaValidaDevuelveTrue() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        assertTrue(inventario.registrarVenta("P1", 2));
    }

    @Test
    @DisplayName("Registrar una venta con stock insuficiente devuelve false")
    void registrarVentaConStockInsuficienteDevuelveFalse() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        inventario.registrarVenta("P1", 2);
        assertFalse(inventario.registrarVenta("P1", 4));
    }

    @Test
    @DisplayName("Registrar una venta con cantidad cero devuelve false")
    void registrarVentaCantidadCeroDevuelveFalse() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        assertFalse(inventario.registrarVenta("P1", 0));
    }

    @Test
    @DisplayName("Registrar una venta descuenta el stock")
    void registrarVentaDescuentaStock() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        inventario.registrarVenta("P1", 2);
        assertEquals(3, inventario.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("Registrar una venta actualiza la popularidad")
    void registrarVentaActualizaPopularidad() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        inventario.registrarVenta("P1", 2);
        assertEquals(2, inventario.obtenerPopularidadProductos().get("P1"));
    }

    @Test
    @DisplayName("Lista los productos con cinco unidades o menos")
    void listarProductosStockBajoDevuelveUno() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        inventario.registrarProducto(producto("P2", 6));
        assertEquals(1, inventario.listarProductosProximosAAgotarse().size());
    }

    @Test
    @DisplayName("La lista de stock bajo no permite limpiarse")
    void listarStockBajoNoPermiteLimpiarse() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        assertThrows(UnsupportedOperationException.class,
            () -> inventario.listarProductosProximosAAgotarse().clear());
    }

    @Test
    @DisplayName("Registrar producto con id nulo devuelve false")
    public void registrarProductoConIdNuloDevuelveFalse() {
        assertFalse(new Inventario().registrarProducto(new Producto(null, "P", 1, 2, "d", 1, "m", "c")));
    }

    @Test
    @DisplayName("Registrar producto con id en blanco devuelve false")
    public void registrarProductoConIdEnBlancoDevuelveFalse() {
        assertFalse(new Inventario().registrarProducto(new Producto("   ", "P", 1, 2, "d", 1, "m", "c")));
    }

    @Test
    @DisplayName("Actualizar stock con cantidad negativa devuelve false")
    public void actualizarStockConCantidadNegativaDevuelveFalse() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        assertFalse(inventario.actualizarStock("P1", -1));
    }

    @Test
    @DisplayName("Actualizar stock con cantidad negativa no modifica el stock")
    public void actualizarStockConCantidadNegativaNoModificaStock() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        inventario.actualizarStock("P1", -1);
        assertEquals(5, inventario.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("Registrar varias ventas acumula la popularidad del producto")
    public void registrarVariasVentasAcumulaPopularidad() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 10));
        inventario.registrarVenta("P1", 2);
        inventario.registrarVenta("P1", 3);
        assertEquals(5, inventario.obtenerPopularidadProductos().get("P1"));
    }

    @Test
    @DisplayName("Listar productos proximos a agotarse incluye stock exactamente cinco")
    public void listarStockBajoIncluyeStockExactamenteCinco() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(producto("P1", 5));
        assertEquals(1, inventario.listarProductosProximosAAgotarse().size());
    }

    @Test
    @DisplayName("Obtener popularidad devuelve un mapa inmodificable")
    public void obtenerPopularidadDevuelveMapaInmodificable() {
        Inventario inventario = new Inventario();
        assertThrows(UnsupportedOperationException.class,
            () -> inventario.obtenerPopularidadProductos().clear());
    }
}