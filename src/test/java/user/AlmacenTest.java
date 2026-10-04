package user;

import models.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Almacen")
class AlmacenTest {

    private Almacen almacen() {
        return new Almacen("CI-ALM", "Alma", "pw");
    }

    private Producto producto() {
        return new Producto("P1", "Producto", 10.0, 15.0, "desc", 5, "Marca", "Cat");
    }

    @Test
    @DisplayName("El constructor asigna el rol ALMACENERO")
    void constructorAsignaRolAlmacenero() {
        assertEquals(Rol.ALMACENERO, almacen().getRol());
    }

    @Test
    @DisplayName("El constructor asigna ci y nombre")
    void constructorAsignaDatos() {
        Almacen a = almacen();
        assertEquals("CI-ALM", a.getCi());
        assertEquals("Alma", a.getNombre());
    }

    @Test
    @DisplayName("Almacen es un Empleado (herencia)")
    void esUnEmpleado() {
        assertTrue(almacen() instanceof Empleado);
    }

    @Test
    @DisplayName("ingresarProductoNuevo devuelve true (contrato base)")
    void ingresarProductoNuevoDevuelveTrue() {
        assertTrue(almacen().ingresarProductoNuevo(producto()));
    }

    @Test
    @DisplayName("ingresarProductoExistente devuelve true (contrato base)")
    void ingresarProductoExistenteDevuelveTrue() {
        assertTrue(almacen().ingresarProductoExistente("P1", 5));
    }

    @Test
    @DisplayName("ingresarEntradaDesdeArchivo devuelve true (contrato base)")
    void ingresarEntradaDesdeArchivoDevuelveTrue() {
        assertTrue(almacen().ingresarEntradaDesdeArchivo("entradas.csv"));
    }
}
