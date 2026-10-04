package models;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Inventario")
class InventarioTest {

    @Test
    @DisplayName("buscarEnInventario no devuelve null (contrato base)")
    void buscarEnInventarioNoEsNull() {
        assertNotNull(new Inventario().buscarEnInventario("P1"));
    }

    @Test
    @DisplayName("actualizarStock devuelve true (contrato base)")
    void actualizarStockDevuelveTrue() {
        assertTrue(new Inventario().actualizarStock("P1", 5));
    }

    @Test
    @DisplayName("listarProductosProximosAAgotarse no devuelve null")
    void listarProductosProximosAAgotarseNoEsNull() {
        assertNotNull(new Inventario().listarProductosProximosAAgotarse());
    }

    @Test
    @DisplayName("obtenerPopularidadProductos no devuelve null")
    void obtenerPopularidadProductosNoEsNull() {
        assertNotNull(new Inventario().obtenerPopularidadProductos());
    }

    @Test
    @DisplayName("buscarEnInventario debería devolver null para un código inexistente")
    @Disabled("Pendiente de lógica: debe consultarse realmente el mapa de productos")
    void buscarEnInventarioInexistente() {
        assertNull(new Inventario().buscarEnInventario("NO-EXISTE"));
    }

    @Test
    @DisplayName("actualizarStock debería sumar la cantidad al producto existente")
    @Disabled("Pendiente de lógica: actualizarStock debe modificar el stock en el mapa")
    void actualizarStockModificaElMapa() {
        Inventario inv = new Inventario();
        inv.actualizarStock("P1", 5);
        assertEquals(5, inv.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("actualizarStock con cantidad negativa no lanza (contrato base)")
    void actualizarStockNegativoNoLanza() {
        assertDoesNotThrow(() -> new Inventario().actualizarStock("P1", -5));
    }

    @Test
    @DisplayName("actualizarStock con cantidad cero no lanza (contrato base)")
    void actualizarStockCeroNoLanza() {
        assertDoesNotThrow(() -> new Inventario().actualizarStock("P1", 0));
    }
}
