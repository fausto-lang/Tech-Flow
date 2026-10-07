package user;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import models.Producto;
import models.Proveedor;
import services.MotorCSV;

@DisplayName("Gestor")
class GestorTest {

    @TempDir
    Path directorio;

    private Gestor gestor() {
        return new Gestor("CI-ADM", "Admin", "pw", directorio);
    }

    private Producto producto(String id, int stock) {
        return new Producto(id, "Producto " + id, 10.0, 15.0, "desc", stock, "Marca", "Cat");
    }

    private MotorCSV csv() {
        return new MotorCSV(directorio);
    }

    @Test
    @DisplayName("Asigna rol de administrador")
    void constructorAsignaRol() {
        assertEquals(Rol.ADMINISTRADOR, gestor().getRol());
        assertTrue(gestor() instanceof Empleado);
    }

    @Test
    @DisplayName("Caja abre y cierra persistiendo el estado")
    void cajaPersisteEstado() {
        Gestor gestor = gestor();
        assertTrue(gestor.abrirCaja());
        assertEquals("ABIERTA", csv().leerCSV(csv().rutaDatos("caja.csv").toString()).get(1)[0]);
        assertTrue(gestor.cerrarCaja());
        assertEquals("CERRADA", csv().leerCSV(csv().rutaDatos("caja.csv").toString()).get(1)[0]);
    }

    @Test
    @DisplayName("Reportes toleran archivos vacíos y fechas no válidas")
    void reportesNoLanzan() {
        Gestor gestor = gestor();
        assertDoesNotThrow(gestor::verVentasEIngresosDiarios);
        assertDoesNotThrow(gestor::verInventario);
        assertDoesNotThrow(gestor::verAnalisisEntradasDiarias);
        assertDoesNotThrow(() -> gestor.consultarVentasPorRangoFechas(null, null));
        assertDoesNotThrow(() -> gestor.consultarVentasPorRangoFechas(LocalDate.now(), LocalDate.now().minusDays(1)));
    }

    @Test
    @DisplayName("Registra entrada de producto y actualiza inventario")
    void ingresoProducto() {
        Gestor gestor = gestor();
        assertTrue(gestor.ingresarEntradaProducto(producto("P1", 4)));
        assertFalse(gestor.ingresarEntradaProducto(null));
        assertEquals("4", csv().leerCSV(csv().rutaDatos("inventario.csv").toString()).get(1)[7]);
        assertEquals(2, csv().leerCSV(csv().rutaDatos("entradas.csv").toString()).size());
    }

    @Test
    @DisplayName("Importa recepciones CSV y valida proveedor")
    void importarEntradas() {
        Gestor gestor = gestor();
        csv().escribirCSV(csv().rutaDatos("proveedores.csv").toString(), List.of(
                new String[]{"idProveedor", "nombreProveedor", "contactoProveedor"},
                new String[]{"PROV-1", "Proveedor", "123"}));
        String fuente = directorio.resolve("entradas.txt").toString();
        csv().escribirCSV(fuente, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-1", "P1", "Nuevo", "Marca", "Cat", "10", "3"}));
        assertTrue(gestor.ingresarEntradasDesdeCSV(fuente));
        assertFalse(gestor.ingresarEntradasDesdeCSV(""));
        assertEquals("3", csv().leerCSV(csv().rutaDatos("inventario.csv").toString()).get(1)[7]);
    }

    @Test
    @DisplayName("Alta y baja de empleados valida duplicados e inexistentes")
    void crudEmpleados() {
        Gestor gestor = gestor();
        Empleado empleado = new Empleado("CI-2", "Bea", "pw", Rol.CAJERO);
        assertTrue(gestor.anadirEmpleado(empleado));
        assertFalse(gestor.anadirEmpleado(empleado));
        assertTrue(gestor.eliminarEmpleado("CI-2"));
        assertFalse(gestor.eliminarEmpleado("NO-EXISTE"));
        assertFalse(gestor.anadirEmpleado(null));
    }

    @Test
    @DisplayName("Alta y baja de proveedores valida duplicados e inexistentes")
    void crudProveedores() {
        Gestor gestor = gestor();
        Proveedor proveedor = new Proveedor("Proveedor", "PROV-01", 123);
        assertTrue(gestor.anadirProveedor(proveedor));
        assertFalse(gestor.anadirProveedor(proveedor));
        assertTrue(gestor.eliminarProveedor("PROV-01"));
        assertFalse(gestor.eliminarProveedor("NO-EXISTE"));
    }

    @Test
    @DisplayName("Alta y baja de productos persiste el esquema de inventario")
    void crudProductos() {
        Gestor gestor = gestor();
        assertTrue(gestor.anadirProducto(producto("P1", 4)));
        assertFalse(gestor.anadirProducto(producto("P1", 4)));
        assertTrue(gestor.eliminarProducto("P1"));
        assertFalse(gestor.eliminarProducto("NO-EXISTE"));
    }

    @Test
    @DisplayName("Crea pedido para proveedor registrado")
    void generarPedido() {
        Gestor gestor = gestor();
        assertDoesNotThrow(() -> gestor.generarPedidoProveedor("PROV-1", List.of(producto("P1", 3))));
        csv().escribirCSV(csv().rutaDatos("proveedores.csv").toString(), List.of(
                new String[]{"idProveedor", "nombreProveedor", "contactoProveedor"},
                new String[]{"PROV-1", "Proveedor", "123"}));
        gestor.generarPedidoProveedor("PROV-1", List.of(producto("P1", 3)));
        assertEquals(2, csv().leerCSV(csv().rutaDatos("pedidos.csv").toString()).size());
    }

    @Test
    @DisplayName("Lista existencias críticas y exporta reporte")
    void listarYExportar() {
        Gestor gestor = gestor();
        gestor.anadirProducto(producto("P1", 3));
        gestor.anadirProducto(producto("P2", 20));
        assertEquals(1, gestor.listarProductosProximosAAgotarse().size());
        String salida = directorio.resolve("reporte.csv").toString();
        assertTrue(gestor.exportarReporteExcelOCSV(salida));
        assertEquals("tipo", csv().leerCSV(salida).get(0)[0]);
    }

    @Test
    @DisplayName("No elimina al unico administrador registrado")
    void noEliminaAlUltimoAdministrador() {
        csv().escribirCSV(csv().rutaDatos("empleado.csv").toString(), List.of(
                new String[]{"ci", "nombre", "rol", "contrasena"},
                new String[]{"CI-ADM", "Admin", "ADMINISTRADOR", "pw"}));

        assertFalse(gestor().eliminarEmpleado("CI-ADM"));
        assertEquals(2, csv().leerCSV(csv().rutaDatos("empleado.csv").toString()).size());
    }

    @Test
    @DisplayName("Permite eliminar un administrador si queda otro administrador")
    void eliminaAdministradorSiQuedaOtro() {
        csv().escribirCSV(csv().rutaDatos("empleado.csv").toString(), List.of(
                new String[]{"ci", "nombre", "rol", "contrasena"},
                new String[]{"CI-ADM", "Admin", "ADMINISTRADOR", "pw"},
                new String[]{"CI-ADM-2", "Admin Dos", "ADMINISTRADOR", "pw2"}));

        assertTrue(gestor().eliminarEmpleado("CI-ADM"));
        assertEquals("CI-ADM-2", csv().leerCSV(csv().rutaDatos("empleado.csv").toString()).get(1)[0]);
    }

    @Test
    @DisplayName("No persiste productos con precios no finitos")
    void rechazaPrecioNoFinito() {
        Gestor gestor = gestor();
        Producto producto = new Producto("P-NAN", "Producto", Double.NaN, 15.0,
                "desc", 1, "Marca", "Cat");

        assertFalse(gestor.anadirProducto(producto));
        assertTrue(csv().leerCSV(csv().rutaDatos("inventario.csv").toString()).isEmpty());
    }

    @Test
    @DisplayName("Un pedido invalido no crea un archivo de pedidos")
    void pedidoInvalidoNoPersiste() {
        Gestor gestor = gestor();

        gestor.generarPedidoProveedor("PROV-INEXISTENTE", List.of(producto("P1", 3)));

        assertFalse(csv().existeArchivo(csv().rutaDatos("pedidos.csv").toString()));
    }
}