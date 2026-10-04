package models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cliente")
class ClienteTest {

    private Cliente base() {
        return new Cliente("CLI-1", "Ana");
    }

    @Test
    @DisplayName("El constructor vacío deja la ci en null")
    void constructorVacioCiNull() {
        assertNull(new Cliente().getCi());
    }

    @Test
    @DisplayName("El constructor vacío deja el nombre en null")
    void constructorVacioNombreNull() {
        assertNull(new Cliente().getNombre());
    }

    @Test
    @DisplayName("El constructor completo asigna ci y nombre")
    void constructorCompleto() {
        Cliente c = base();
        assertEquals("CLI-1", c.getCi());
        assertEquals("Ana", c.getNombre());
    }

    @Test
    @DisplayName("isEsFrecuente devuelve true por defecto")
    void esFrecuentePorDefecto() {
        assertTrue(base().isEsFrecuente());
    }

    @Test
    @DisplayName("Dos clientes con los mismos datos son iguales")
    void equalsConMismosDatos() {
        assertEquals(base(), base());
    }

    @Test
    @DisplayName("Clientes con distinta ci no son iguales")
    void equalsConDistintaCi() {
        assertNotEquals(base(), new Cliente("CLI-2", "Ana"));
    }

    @Test
    @DisplayName("Un cliente nunca es igual a null")
    void equalsConNull() {
        assertNotEquals(base(), null);
    }

    @Test
    @DisplayName("Un cliente nunca es igual a otro tipo de objeto")
    void equalsConOtraClase() {
        assertNotEquals(base(), "texto");
    }

    @Test
    @DisplayName("equals y hashCode son coherentes")
    void hashCodeCoherente() {
        assertEquals(base().hashCode(), base().hashCode());
    }

    @Test
    @DisplayName("toString incluye ci y nombre")
    void toStringIncluyeDatos() {
        assertTrue(base().toString().contains("Ana"));
    }

    @Test
    @DisplayName("Dos clientes vacíos son iguales (todos los campos null)")
    void dosClientesVaciosSonIguales() {
        assertEquals(new Cliente(), new Cliente());
    }

    @Test
    @DisplayName("El nombre vacío se conserva")
    void nombreVacio() {
        assertEquals("", new Cliente("CLI-1", "").getNombre());
    }

    @Test
    @DisplayName("Un cliente con ci null no es igual a uno con ci vacía")
    void ciNullVsCiVacia() {
        assertNotEquals(new Cliente(null, "Ana"), new Cliente("", "Ana"));
    }
}
