package user;

import models.Producto;
import models.Proveedor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Gestor")
class GestorTest {

    private Gestor gestor() {
        return new Gestor("CI-ADM", "Admin", "pw");
    }

    private Producto producto() {
        return new Producto("P1", "Producto", 10.0, 15.0, "desc", 5, "Marca", "Cat");
    }

    private Proveedor proveedor() {
        return new Proveedor("Proveedor", "PROV-01", 123);
    }

    private Empleado empleado() {
        return new Empleado("CI-2", "Bea", "pw", Rol.CAJERO);
    }

    @Test
    @DisplayName("El constructor asigna el rol ADMINISTRADOR")
    void constructorAsignaRolAdministrador() {
        assertEquals(Rol.ADMINISTRADOR, gestor().getRol());
    }

    @Test
    @DisplayName("El constructor asigna ci y nombre")
    void constructorAsignaDatos() {
        Gestor g = gestor();
        assertEquals("CI-ADM", g.getCi());
        assertEquals("Admin", g.getNombre());
    }

    @Test
    @DisplayName("Gestor es un Empleado (herencia)")
    void esUnEmpleado() {
        assertTrue(gestor() instanceof Empleado);
    }

    @Test
    @DisplayName("abrirCaja devuelve true")
    void abrirCajaDevuelveTrue() {
        assertTrue(gestor().abrirCaja());
    }

    @Test
    @DisplayName("cerrarCaja devuelve true")
    void cerrarCajaDevuelveTrue() {
        assertTrue(gestor().cerrarCaja());
    }

    @Test
    @DisplayName("verVentasEIngresosDiarios no lanza excepción")
    void verVentasEIngresosDiariosNoLanza() {
        assertDoesNotThrow(() -> gestor().verVentasEIngresosDiarios());
    }

    @Test
    @DisplayName("verInventario no lanza excepción")
    void verInventarioNoLanza() {
        assertDoesNotThrow(() -> gestor().verInventario());
    }

    @Test
    @DisplayName("ingresarEntradaProducto devuelve true")
    void ingresarEntradaProductoDevuelveTrue() {
        assertTrue(gestor().ingresarEntradaProducto(producto()));
    }

    @Test
    @DisplayName("ingresarEntradaProducto con null no lanza")
    void ingresarEntradaProductoConNullNoLanza() {
        assertDoesNotThrow(() -> gestor().ingresarEntradaProducto(null));
    }

    @Test
    @DisplayName("ingresarEntradasDesdeCSV devuelve true")
    void ingresarEntradasDesdeCSVDevuelveTrue() {
        assertTrue(gestor().ingresarEntradasDesdeCSV("entradas.csv"));
    }

    @Test
    @DisplayName("verAnalisisEntradasDiarias no lanza excepción")
    void verAnalisisEntradasDiariasNoLanza() {
        assertDoesNotThrow(() -> gestor().verAnalisisEntradasDiarias());
    }

    @Test
    @DisplayName("anadirEmpleado devuelve true")
    void anadirEmpleadoDevuelveTrue() {
        assertTrue(gestor().anadirEmpleado(empleado()));
    }

    @Test
    @DisplayName("eliminarEmpleado devuelve true")
    void eliminarEmpleadoDevuelveTrue() {
        assertTrue(gestor().eliminarEmpleado("CI-2"));
    }

    @Test
    @DisplayName("anadirProveedor devuelve true")
    void anadirProveedorDevuelveTrue() {
        assertTrue(gestor().anadirProveedor(proveedor()));
    }

    @Test
    @DisplayName("eliminarProveedor devuelve true")
    void eliminarProveedorDevuelveTrue() {
        assertTrue(gestor().eliminarProveedor("PROV-01"));
    }

    @Test
    @DisplayName("anadirProducto devuelve true")
    void anadirProductoDevuelveTrue() {
        assertTrue(gestor().anadirProducto(producto()));
    }

    @Test
    @DisplayName("eliminarProducto devuelve true")
    void eliminarProductoDevuelveTrue() {
        assertTrue(gestor().eliminarProducto("P1"));
    }

    @Test
    @DisplayName("generarPedidoProveedor no lanza excepción")
    void generarPedidoProveedorNoLanza() {
        assertDoesNotThrow(() -> gestor().generarPedidoProveedor("PROV-01", List.of(producto())));
    }

    @Test
    @DisplayName("listarProductosProximosAAgotarse no devuelve null")
    void listarProductosProximosAAgotarseNoEsNull() {
        assertNotNull(gestor().listarProductosProximosAAgotarse());
    }

    @Test
    @DisplayName("consultarVentasPorRangoFechas no lanza excepción")
    void consultarVentasPorRangoFechasNoLanza() {
        assertDoesNotThrow(() -> gestor().consultarVentasPorRangoFechas(LocalDate.now().minusDays(1), LocalDate.now()));
    }

    @Test
    @DisplayName("exportarReporteExcelOCSV devuelve true")
    void exportarReporteExcelOCSVDevuelveTrue() {
        assertTrue(gestor().exportarReporteExcelOCSV("reporte.csv"));
    }

    @Test
    @DisplayName("No debería permitir añadir un empleado con CI duplicado")
    @Disabled("Pendiente de lógica: alta de empleado con validación de CI y persistencia")
    void noDeberiaPermitirCiDuplicado() {
        Gestor g = gestor();
        assertTrue(g.anadirEmpleado(empleado()));
        assertFalse(g.anadirEmpleado(empleado()));
    }

    @Test
    @DisplayName("Eliminar un empleado inexistente debería devolver false")
    @Disabled("Pendiente de lógica: la eliminación debe reflejar si el empleado existía")
    void eliminarEmpleadoInexistente() {
        assertFalse(gestor().eliminarEmpleado("NO-EXISTE"));
    }

    @Test
    @DisplayName("Eliminar un proveedor inexistente debería devolver false")
    @Disabled("Pendiente de lógica: la eliminación debe reflejar si el proveedor existía")
    void eliminarProveedorInexistente() {
        assertFalse(gestor().eliminarProveedor("NO-EXISTE"));
    }

    @Test
    @DisplayName("consultarVentasPorRangoFechas con inicio posterior al fin no lanza")
    void rangoInvertidoNoLanza() {
        assertDoesNotThrow(() -> gestor().consultarVentasPorRangoFechas(LocalDate.now(), LocalDate.now().minusDays(1)));
    }

    @Test
    @DisplayName("consultarVentasPorRangoFechas con fechas null no lanza")
    void rangoNullNoLanza() {
        assertDoesNotThrow(() -> gestor().consultarVentasPorRangoFechas(null, null));
    }

    @Test
    @DisplayName("generarPedidoProveedor con lista vacía no lanza")
    void pedidoListaVaciaNoLanza() {
        assertDoesNotThrow(() -> gestor().generarPedidoProveedor("PROV-01", List.of()));
    }

    @Test
    @DisplayName("ingresarEntradasDesdeCSV con ruta vacía no lanza")
    void ingresarEntradasRutaVaciaNoLanza() {
        assertDoesNotThrow(() -> gestor().ingresarEntradasDesdeCSV(""));
    }
}
