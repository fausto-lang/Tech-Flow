package user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Empleado")
class EmpleadoTest {

    private Empleado base() {
        return new Empleado("CI-1", "Ana", "pw", Rol.CAJERO);
    }

    @Test
    @DisplayName("El constructor vacío deja todos los campos en null")
    void constructorVacio() {
        assertNull(new Empleado().getRol());
    }

    @Test
    @DisplayName("El constructor completo asigna ci, nombre, contraseña y rol")
    void constructorCompleto() {
        Empleado e = base();
        assertEquals("CI-1", e.getCi());
        assertEquals("Ana", e.getNombre());
        assertEquals("pw", e.getContrasena());
        assertEquals(Rol.CAJERO, e.getRol());
    }

    @Test
    @DisplayName("Dos empleados con los mismos datos son iguales")
    void equalsConMismosDatos() {
        assertEquals(base(), base());
    }

    @Test
    @DisplayName("Empleados con distinto rol no son iguales")
    void equalsConDistintoRol() {
        assertNotEquals(base(), new Empleado("CI-1", "Ana", "pw", Rol.ALMACENERO));
    }

    @Test
    @DisplayName("Empleados con distinta ci no son iguales")
    void equalsConDistintaCi() {
        assertNotEquals(base(), new Empleado("CI-2", "Ana", "pw", Rol.CAJERO));
    }

    @Test
    @DisplayName("Un empleado nunca es igual a null")
    void equalsConNull() {
        assertNotEquals(base(), null);
    }

    @Test
    @DisplayName("Un empleado nunca es igual a otro tipo de objeto")
    void equalsConOtraClase() {
        assertNotEquals(base(), "texto");
    }

    @Test
    @DisplayName("Un empleado es igual a sí mismo")
    void equalsMismaReferencia() {
        Empleado e = base();
        assertEquals(e, e);
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
    @DisplayName("Dos empleados vacíos son iguales (todos los campos null)")
    void dosEmpleadosVaciosSonIguales() {
        assertEquals(new Empleado(), new Empleado());
    }
}
