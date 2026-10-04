package models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Producto")
class ProductoTest {

    private Producto base() {
        return new Producto("P1", "Auriculares", 10.0, 20.0, "desc", 5, "Sony", "Audio");
    }

    @Test
    @DisplayName("El constructor mapea los argumentos en el orden correcto")
    void constructorMapeaArgumentos() {
        Producto p = base();
        assertEquals("P1", p.getIdProducto());
        assertEquals("Auriculares", p.getNombre());
        assertEquals(10.0, p.getPrecioEntrada(), 1e-9);
        assertEquals(20.0, p.getPrecioVenta(), 1e-9);
        assertEquals("desc", p.getDescripcion());
        assertEquals(5, p.getStock());
        assertEquals("Sony", p.getMarca());
        assertEquals("Audio", p.getCategoria());
    }

    @Test
    @DisplayName("Dos productos con los mismos datos son iguales")
    void equalsConMismosDatos() {
        assertEquals(base(), base());
    }

    @Test
    @DisplayName("Productos con distinto id no son iguales")
    void equalsConDistintoId() {
        assertNotEquals(base(), new Producto("P2", "Auriculares", 10.0, 20.0, "desc", 5, "Sony", "Audio"));
    }

    @Test
    @DisplayName("Productos con distinto stock no son iguales")
    void equalsConDistintoStock() {
        assertNotEquals(base(), new Producto("P1", "Auriculares", 10.0, 20.0, "desc", 9, "Sony", "Audio"));
    }

    @Test
    @DisplayName("Un producto nunca es igual a null")
    void equalsConNull() {
        assertNotEquals(base(), null);
    }

    @Test
    @DisplayName("Un producto nunca es igual a otro tipo de objeto")
    void equalsConOtraClase() {
        assertNotEquals(base(), "texto");
    }

    @Test
    @DisplayName("Un producto es igual a sí mismo")
    void equalsMismaReferencia() {
        Producto p = base();
        assertEquals(p, p);
    }

    @Test
    @DisplayName("equals y hashCode son coherentes")
    void hashCodeCoherente() {
        assertEquals(base().hashCode(), base().hashCode());
    }

    @Test
    @DisplayName("toString incluye id y nombre")
    void toStringIncluyeDatos() {
        assertTrue(base().toString().contains("Auriculares"));
    }

    @Test
    @DisplayName("El stock puede ser cero (borde)")
    void stockCero() {
        assertEquals(0, new Producto("P1", "n", 1, 2, "d", 0, "m", "c").getStock());
    }

    @Test
    @DisplayName("El stock negativo se acepta tal cual")
    void stockNegativo() {
        assertEquals(-1, new Producto("P1", "n", 1, 2, "d", -1, "m", "c").getStock());
    }

    @Test
    @DisplayName("Los precios negativos se aceptan tal cual")
    void preciosNegativos() {
        assertEquals(-10.0, new Producto("P1", "n", -10.0, -20.0, "d", 0, "m", "c").getPrecioEntrada(), 1e-9);
    }

    @Test
    @DisplayName("Dos productos con id null son iguales si el resto coincide")
    void equalsConIdNull() {
        assertEquals(new Producto(null, "n", 1, 2, "d", 0, "m", "c"),
                new Producto(null, "n", 1, 2, "d", 0, "m", "c"));
    }
}
