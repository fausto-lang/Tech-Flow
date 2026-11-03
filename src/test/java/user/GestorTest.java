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
    @DisplayName("El constructor asigna el rol ADMINISTRADOR")
    void constructorAsignaRolAdministrador() {
        assertEquals(Rol.ADMINISTRADOR, gestor().getRol());
    }

    @Test
    @DisplayName("El gestor es una instancia de Empleado")
    void gestorEsInstanciaDeEmpleado() {
        assertTrue(gestor() instanceof Empleado);
    }

    @Test
    @DisplayName("Abrir la caja devuelve true")
    void abrirCajaDevuelveTrue() {
        Gestor gestor = gestor();
        assertTrue(gestor.abrirCaja());
    }

    @Test
    @DisplayName("Abrir la caja registra el estado abierta")
    void abrirCajaRegistraEstadoAbierta() {
        Gestor gestor = gestor();
        gestor.abrirCaja();
        assertEquals("ABIERTA", csv().leerCSV(csv().rutaDatos("caja.csv").toString()).get(1)[0]);
    }

    @Test
    @DisplayName("Cerrar la caja devuelve true")
    void cerrarCajaDevuelveTrue() {
        Gestor gestor = gestor();
        assertTrue(gestor.cerrarCaja());
    }

    @Test
    @DisplayName("Cerrar la caja registra el estado cerrada")
    void cerrarCajaRegistraEstadoCerrada() {
        Gestor gestor = gestor();
        gestor.abrirCaja();
        gestor.cerrarCaja();
        assertEquals("CERRADA", csv().leerCSV(csv().rutaDatos("caja.csv").toString()).get(1)[0]);
    }

    @Test
    @DisplayName("Ver ventas e ingresos diarios no lanza excepcion")
    void verVentasEIngresosDiariosNoLanzaExcepcion() {
        Gestor gestor = gestor();
        assertDoesNotThrow(gestor::verVentasEIngresosDiarios);
    }

    @Test
    @DisplayName("Ver inventario no lanza excepcion")
    void verInventarioNoLanzaExcepcion() {
        Gestor gestor = gestor();
        assertDoesNotThrow(gestor::verInventario);
    }

    @Test
    @DisplayName("Ver analisis de entradas diarias no lanza excepcion")
    void verAnalisisEntradasDiariasNoLanzaExcepcion() {
        Gestor gestor = gestor();
        assertDoesNotThrow(gestor::verAnalisisEntradasDiarias);
    }

    @Test
    @DisplayName("Consultar ventas por rango con fechas nulas no lanza excepcion")
    void consultarVentasRangoFechasNulasNoLanzaExcepcion() {
        Gestor gestor = gestor();
        assertDoesNotThrow(() -> gestor.consultarVentasPorRangoFechas(null, null));
    }

    @Test
    @DisplayName("Consultar ventas por rango con fecha fin menor a inicio no lanza excepcion")
    void consultarVentasRangoFechaFinMenorNoLanzaExcepcion() {
        Gestor gestor = gestor();
        assertDoesNotThrow(() -> gestor.consultarVentasPorRangoFechas(LocalDate.now(), LocalDate.now().minusDays(1)));
    }

    @Test
    @DisplayName("Registrar entrada de producto devuelve true")
    void registrarEntradaProductoDevuelveTrue() {
        Gestor gestor = gestor();
        assertTrue(gestor.ingresarEntradaProducto(producto("P1", 4)));
    }

    @Test
    @DisplayName("Registrar entrada de producto nulo devuelve false")
    void registrarEntradaProductoNuloDevuelveFalse() {
        Gestor gestor = gestor();
        assertFalse(gestor.ingresarEntradaProducto(null));
    }

    @Test
    @DisplayName("Registrar entrada de producto actualiza el stock en inventario")
    void registrarEntradaProductoActualizaStock() {
        Gestor gestor = gestor();
        gestor.ingresarEntradaProducto(producto("P1", 4));
        assertEquals("4", csv().leerCSV(csv().rutaDatos("inventario.csv").toString()).get(1)[7]);
    }

    @Test
    @DisplayName("Registrar entrada de producto deja registro en entradas")
    void registrarEntradaProductoDejaRegistroEnEntradas() {
        Gestor gestor = gestor();
        gestor.ingresarEntradaProducto(producto("P1", 4));
        assertEquals(2, csv().leerCSV(csv().rutaDatos("entradas.csv").toString()).size());
    }

    @Test
    @DisplayName("Importar entradas desde CSV devuelve true")
    void importarEntradasDesdeCSVDevuelveTrue() {
        Gestor gestor = gestor();
        csv().escribirCSV(csv().rutaDatos("proveedores.csv").toString(), List.of(
                new String[]{"idProveedor", "nombreProveedor", "contactoProveedor"},
                new String[]{"PROV-1", "Proveedor", "123"}));
        String fuente = directorio.resolve("entradas.txt").toString();
        csv().escribirCSV(fuente, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-1", "P1", "Nuevo", "Marca", "Cat", "10", "3"}));
        assertTrue(gestor.ingresarEntradasDesdeCSV(fuente));
    }

    @Test
    @DisplayName("Importar entradas desde ruta vacia devuelve false")
    void importarEntradasDesdeRutaVaciaDevuelveFalse() {
        Gestor gestor = gestor();
        assertFalse(gestor.ingresarEntradasDesdeCSV(""));
    }

    @Test
    @DisplayName("Importar entradas actualiza el stock del producto")
    void importarEntradasActualizaStock() {
        Gestor gestor = gestor();
        csv().escribirCSV(csv().rutaDatos("proveedores.csv").toString(), List.of(
                new String[]{"idProveedor", "nombreProveedor", "contactoProveedor"},
                new String[]{"PROV-1", "Proveedor", "123"}));
        String fuente = directorio.resolve("entradas.txt").toString();
        csv().escribirCSV(fuente, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-1", "P1", "Nuevo", "Marca", "Cat", "10", "3"}));
        gestor.ingresarEntradasDesdeCSV(fuente);
        assertEquals("3", csv().leerCSV(csv().rutaDatos("inventario.csv").toString()).get(1)[7]);
    }

    @Test
    @DisplayName("Agregar un empleado devuelve true")
    void agregarEmpleadoDevuelveTrue() {
        Gestor gestor = gestor();
        Empleado empleado = new Empleado("CI-2", "Bea", "pw", Rol.CAJERO);
        assertTrue(gestor.anadirEmpleado(empleado));
    }

    @Test
    @DisplayName("Agregar un empleado duplicado devuelve false")
    void agregarEmpleadoDuplicadoDevuelveFalse() {
        Gestor gestor = gestor();
        Empleado empleado = new Empleado("CI-2", "Bea", "pw", Rol.CAJERO);
        gestor.anadirEmpleado(empleado);
        assertFalse(gestor.anadirEmpleado(empleado));
    }

    @Test
    @DisplayName("Eliminar un empleado existente devuelve true")
    void eliminarEmpleadoExistenteDevuelveTrue() {
        Gestor gestor = gestor();
        Empleado empleado = new Empleado("CI-2", "Bea", "pw", Rol.CAJERO);
        gestor.anadirEmpleado(empleado);
        assertTrue(gestor.eliminarEmpleado("CI-2"));
    }

    @Test
    @DisplayName("Eliminar un empleado inexistente devuelve false")
    void eliminarEmpleadoInexistenteDevuelveFalse() {
        Gestor gestor = gestor();
        assertFalse(gestor.eliminarEmpleado("NO-EXISTE"));
    }

    @Test
    @DisplayName("Agregar un empleado nulo devuelve false")
    void agregarEmpleadoNuloDevuelveFalse() {
        Gestor gestor = gestor();
        assertFalse(gestor.anadirEmpleado(null));
    }

    @Test
    @DisplayName("Agregar un proveedor devuelve true")
    void agregarProveedorDevuelveTrue() {
        Gestor gestor = gestor();
        Proveedor proveedor = new Proveedor("Proveedor", "PROV-01", 123);
        assertTrue(gestor.anadirProveedor(proveedor));
    }

    @Test
    @DisplayName("Generar ID de proveedor devuelve un código único y persistible")
    void generarIdProveedorDevuelveCodigoUnicoYPersistible() {
        Gestor gestor = gestor();
        String id = gestor.generarIdProveedor();

        assertTrue(id.matches("PROV-[0-9A-F]{8}"));
        assertTrue(gestor.anadirProveedor(new Proveedor("Proveedor", id, 123)));
        assertFalse(id.equals(gestor.generarIdProveedor()));
    }

    @Test
    @DisplayName("Agregar un proveedor duplicado devuelve false")
    void agregarProveedorDuplicadoDevuelveFalse() {
        Gestor gestor = gestor();
        Proveedor proveedor = new Proveedor("Proveedor", "PROV-01", 123);
        gestor.anadirProveedor(proveedor);
        assertFalse(gestor.anadirProveedor(proveedor));
    }

    @Test
    @DisplayName("Eliminar un proveedor existente devuelve true")
    void eliminarProveedorExistenteDevuelveTrue() {
        Gestor gestor = gestor();
        Proveedor proveedor = new Proveedor("Proveedor", "PROV-01", 123);
        gestor.anadirProveedor(proveedor);
        assertTrue(gestor.eliminarProveedor("PROV-01"));
    }

    @Test
    @DisplayName("Eliminar un proveedor inexistente devuelve false")
    void eliminarProveedorInexistenteDevuelveFalse() {
        Gestor gestor = gestor();
        assertFalse(gestor.eliminarProveedor("NO-EXISTE"));
    }

    @Test
    @DisplayName("Agregar un producto devuelve true")
    void agregarProductoDevuelveTrue() {
        Gestor gestor = gestor();
        assertTrue(gestor.anadirProducto(producto("P1", 4)));
    }

    @Test
    @DisplayName("Generar ID de producto devuelve un código único y persistible")
    void generarIdProductoDevuelveCodigoUnicoYPersistible() {
        Gestor gestor = gestor();
        String id = gestor.generarIdProducto();

        assertTrue(id.matches("PROD-[0-9A-F]{8}"));
        assertTrue(gestor.anadirProducto(producto(id, 4)));
        assertFalse(id.equals(gestor.generarIdProducto()));
    }

    @Test
    @DisplayName("Agregar un producto duplicado devuelve false")
    void agregarProductoDuplicadoDevuelveFalse() {
        Gestor gestor = gestor();
        gestor.anadirProducto(producto("P1", 4));
        assertFalse(gestor.anadirProducto(producto("P1", 4)));
    }

    @Test
    @DisplayName("Eliminar un producto existente devuelve true")
    void eliminarProductoExistenteDevuelveTrue() {
        Gestor gestor = gestor();
        gestor.anadirProducto(producto("P1", 4));
        assertTrue(gestor.eliminarProducto("P1"));
    }

    @Test
    @DisplayName("Eliminar un producto inexistente devuelve false")
    void eliminarProductoInexistenteDevuelveFalse() {
        Gestor gestor = gestor();
        assertFalse(gestor.eliminarProducto("NO-EXISTE"));
    }

    @Test
    @DisplayName("Generar pedido para proveedor registrado no lanza excepcion")
    void generarPedidoProveedorRegistradoNoLanzaExcepcion() {
        Gestor gestor = gestor();
        csv().escribirCSV(csv().rutaDatos("proveedores.csv").toString(), List.of(
                new String[]{"idProveedor", "nombreProveedor", "contactoProveedor"},
                new String[]{"PROV-1", "Proveedor", "123"}));
        assertDoesNotThrow(() -> gestor.generarPedidoProveedor("PROV-1", List.of(producto("P1", 3))));
    }

    @Test
    @DisplayName("Generar pedido registra dos filas en el archivo de pedidos")
    void generarPedidoRegistraDosFilasEnPedidosCSV() {
        Gestor gestor = gestor();
        csv().escribirCSV(csv().rutaDatos("proveedores.csv").toString(), List.of(
                new String[]{"idProveedor", "nombreProveedor", "contactoProveedor"},
                new String[]{"PROV-1", "Proveedor", "123"}));
        gestor.generarPedidoProveedor("PROV-1", List.of(producto("P1", 3)));
        assertEquals(2, csv().leerCSV(csv().rutaDatos("pedidos.csv").toString()).size());
    }

    @Test
    @DisplayName("Listar productos proximos a agotarse devuelve uno")
    void listarProductosProximosAAgotarseDevuelveUno() {
        Gestor gestor = gestor();
        gestor.anadirProducto(producto("P1", 3));
        gestor.anadirProducto(producto("P2", 20));
        assertEquals(1, gestor.listarProductosProximosAAgotarse().size());
    }

    @Test
    @DisplayName("Exportar reporte devuelve true")
    void exportarReporteDevuelveTrue() {
        Gestor gestor = gestor();
        gestor.anadirProducto(producto("P1", 3));
        gestor.anadirProducto(producto("P2", 20));
        String salida = directorio.resolve("reporte.csv").toString();
        assertTrue(gestor.exportarReporteExcelOCSV(salida));
    }

    @Test
    @DisplayName("El reporte exportado tiene la cabecera tipo")
    void reporteExportadoTieneCabeceraTipo() {
        Gestor gestor = gestor();
        gestor.anadirProducto(producto("P1", 3));
        gestor.anadirProducto(producto("P2", 20));
        String salida = directorio.resolve("reporte.csv").toString();
        gestor.exportarReporteExcelOCSV(salida);
        assertEquals("tipo", csv().leerCSV(salida).get(0)[0]);
    }

    @Test
    @DisplayName("No se puede eliminar al unico administrador registrado")
    void noSeEliminaAlUltimoAdministrador() {
        csv().escribirCSV(csv().rutaDatos("empleado.csv").toString(), List.of(
                new String[]{"ci", "nombre", "rol", "contrasena"},
                new String[]{"CI-ADM", "Admin", "ADMINISTRADOR", "pw"}));
        assertFalse(gestor().eliminarEmpleado("CI-ADM"));
    }

    @Test
    @DisplayName("Al intentar eliminar al unico administrador el archivo conserva dos filas")
    void intentoEliminarUltimoAdministradorConservaDosFilas() {
        csv().escribirCSV(csv().rutaDatos("empleado.csv").toString(), List.of(
                new String[]{"ci", "nombre", "rol", "contrasena"},
                new String[]{"CI-ADM", "Admin", "ADMINISTRADOR", "pw"}));
        gestor().eliminarEmpleado("CI-ADM");
        assertEquals(2, csv().leerCSV(csv().rutaDatos("empleado.csv").toString()).size());
    }

    @Test
    @DisplayName("Eliminar un administrador cuando queda otro devuelve true")
    void eliminarAdministradorCuandoQuedaOtroDevuelveTrue() {
        csv().escribirCSV(csv().rutaDatos("empleado.csv").toString(), List.of(
                new String[]{"ci", "nombre", "rol", "contrasena"},
                new String[]{"CI-ADM", "Admin", "ADMINISTRADOR", "pw"},
                new String[]{"CI-ADM-2", "Admin Dos", "ADMINISTRADOR", "pw2"}));
        assertTrue(gestor().eliminarEmpleado("CI-ADM"));
    }

    @Test
    @DisplayName("Tras eliminar un administrador queda el otro en el archivo")
    void trasEliminarAdministradorQuedaElOtro() {
        csv().escribirCSV(csv().rutaDatos("empleado.csv").toString(), List.of(
                new String[]{"ci", "nombre", "rol", "contrasena"},
                new String[]{"CI-ADM", "Admin", "ADMINISTRADOR", "pw"},
                new String[]{"CI-ADM-2", "Admin Dos", "ADMINISTRADOR", "pw2"}));
        gestor().eliminarEmpleado("CI-ADM");
        assertEquals("CI-ADM-2", csv().leerCSV(csv().rutaDatos("empleado.csv").toString()).get(1)[0]);
    }

    @Test
    @DisplayName("Agregar producto con precio no finito devuelve false")
    void agregarProductoPrecioNoFinitoDevuelveFalse() {
        Gestor gestor = gestor();
        Producto producto = new Producto("P-NAN", "Producto", Double.NaN, 15.0,
                "desc", 1, "Marca", "Cat");
        assertFalse(gestor.anadirProducto(producto));
    }

    @Test
    @DisplayName("Producto con precio no finito no se persiste en inventario")
    void productoPrecioNoFinitoNoSePersiste() {
        Gestor gestor = gestor();
        Producto producto = new Producto("P-NAN", "Producto", Double.NaN, 15.0,
                "desc", 1, "Marca", "Cat");
        gestor.anadirProducto(producto);
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