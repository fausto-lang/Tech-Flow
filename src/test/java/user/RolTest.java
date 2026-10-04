package user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Rol")
class RolTest {

    @Test
    @DisplayName("El enum tiene exactamente 3 roles")
    void tieneTresRoles() {
        assertEquals(3, Rol.values().length);
    }

    @Test
    @DisplayName("valueOf('ADMINISTRADOR') devuelve ADMINISTRADOR")
    void valueOfAdministrador() {
        assertEquals(Rol.ADMINISTRADOR, Rol.valueOf("ADMINISTRADOR"));
    }

    @Test
    @DisplayName("valueOf('CAJERO') devuelve CAJERO")
    void valueOfCajero() {
        assertEquals(Rol.CAJERO, Rol.valueOf("CAJERO"));
    }

    @Test
    @DisplayName("valueOf('ALMACENERO') devuelve ALMACENERO")
    void valueOfAlmacenero() {
        assertEquals(Rol.ALMACENERO, Rol.valueOf("ALMACENERO"));
    }

    @Test
    @DisplayName("valueOf de un rol inexistente lanza IllegalArgumentException")
    void valueOfInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> Rol.valueOf("VENDEDOR"));
    }

    @Test
    @DisplayName("values() respeta el orden de declaración")
    void valuesRespetaOrden() {
        assertEquals(Rol.ADMINISTRADOR, Rol.values()[0]);
    }

    @Test
    @DisplayName("name() devuelve el nombre exacto del rol")
    void nameDevuelveElNombre() {
        assertEquals("CAJERO", Rol.CAJERO.name());
    }
}
