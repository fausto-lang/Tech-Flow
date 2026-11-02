package user;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import models.Producto;
import services.MotorCSV;

@DisplayName("Vendedor")
class VendedorTest {

    @TempDir
    Path directorio;

    private Vendedor vendedor() {
        MotorCSV csv = new MotorCSV(directorio);
        csv.inicializarCSV(csv.rutaDatos("inventario.csv").toString(),
                new String[]{"idProducto", "nombre", "marca", "categoria", "descripcion", "precioEntrada", "precioVenta", "stock"});
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
                new String[]{"P1", "Producto", "Marca", "Cat", "desc", "100", "130", "10"});
        csv.escribirCSV(csv.rutaDatos("caja.csv").toString(), List.of(
                new String[]{"estado", "fechaApertura"}, new String[]{"ABIERTA", "2026-10-06"}));
        return new Vendedor("CI-VEN", "Vende", "pw", directorio);
    }

    private Producto producto() {
        return new Producto("P1", "Producto", 100.0, 130.0, "desc", 10, "Marca", "Cat");
    }

    @Test
    @DisplayName("El constructor asigna el rol CAJERO")
    void constructorAsignaRolCajero() {
        assertEquals(Rol.CAJERO, vendedor().getRol());
    }

    @Test
    @DisplayName("Vendedor es un Empleado")
    void esUnEmpleado() {
        assertTrue(vendedor() instanceof Empleado);
    }

    @Test
    @DisplayName("Una venta confirmada devuelve true")
    void venderProductoConfirmadoDevuelveTrue() {
        Vendedor vendedor = vendedor();
        assertTrue(vendedor.venderProducto("P1", "Cliente", 1));
    }

    @Test
    @DisplayName("Una venta confirmada descuenta el stock del producto")
    void venderProductoConfirmadoDescuentaStock() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 1);
        assertEquals(9, vendedor.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("Una venta confirmada registra el estado como confirmada")
    void venderProductoConfirmadoRegistraEstadoConfirmada() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 1);
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertEquals("CONFIRMADA", ventas.get(1)[2]);
    }

    @Test
    @DisplayName("Una venta confirmada registra el nombre del cliente")
    void venderProductoConfirmadoRegistraNombreCliente() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 1);
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertEquals("Cliente", ventas.get(1)[4]);
    }

    @Test
    @DisplayName("Una venta confirmada agrega el cliente al archivo de clientes")
    void venderProductoConfirmadoAgregaCliente() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 1);
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> clientes = csv.leerCSV(csv.rutaDatos("clientes.csv").toString());
        assertEquals(2, clientes.size());
    }

    @Test
    @DisplayName("Una venta confirmada vincula el cliente con la venta")
    void venderProductoConfirmadoVinculaClienteConVenta() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 1);
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> clientes = csv.leerCSV(csv.rutaDatos("clientes.csv").toString());
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertEquals(clientes.get(1)[0], ventas.get(1)[12]);
    }

    @Test
    @DisplayName("Una venta confirmada registra el nombre del cliente en el CSV")
    void venderProductoConfirmadoRegistraNombreClienteEnCSV() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 1);
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> clientes = csv.leerCSV(csv.rutaDatos("clientes.csv").toString());
        assertEquals("Cliente", clientes.get(1)[1]);
    }

    @Test
    @DisplayName("Generar una orden de venta no devuelve nulo")
    void generarOrdenDeVentaNoEsNulo() {
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 2));
        assertNotNull(orden);
    }

    @Test
    @DisplayName("Una proforma no descuenta el stock del producto")
    void proformaNoDescuentaStock() {
        Vendedor vendedor = vendedor();
        vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 2));
        assertEquals(10, vendedor.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("Una proforma no agrega clientes al archivo de clientes")
    void proformaNoAgregaClientesAlCSV() {
        Vendedor vendedor = vendedor();
        vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 2));
        MotorCSV csv = new MotorCSV(directorio);
        assertEquals(0, csv.leerCSV(csv.rutaDatos("clientes.csv").toString()).size());
    }

    @Test
    @DisplayName("Confirmar una orden de venta devuelve true")
    void confirmarOrdenDeVentaDevuelveTrue() {
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 2));
        assertTrue(vendedor.confirmarVenta(orden));
    }

    @Test
    @DisplayName("Confirmar una orden de venta descuenta el stock")
    void confirmarOrdenDeVentaDescuentaStock() {
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 2));
        vendedor.confirmarVenta(orden);
        assertEquals(8, vendedor.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("Confirmar la misma orden de venta dos veces devuelve false")
    void confirmarOrdenDeVentaDosVecesDevuelveFalse() {
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 2));
        vendedor.confirmarVenta(orden);
        assertFalse(vendedor.confirmarVenta(orden));
    }

    @Test
    @DisplayName("Confirmar una orden de venta agrega clientes al archivo de clientes")
    void confirmarOrdenDeVentaAgregaClientesAlCSV() {
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 2));
        vendedor.confirmarVenta(orden);
        MotorCSV csv = new MotorCSV(directorio);
        assertEquals(2, csv.leerCSV(csv.rutaDatos("clientes.csv").toString()).size());
    }

    @Test
    @DisplayName("Vender un producto a un cliente nuevo devuelve true")
    void venderProductoClienteNuevoDevuelveTrue() {
        Vendedor vendedor = vendedor();
        assertTrue(vendedor.venderProducto("P1", "Cliente", 1));
    }

    @Test
    @DisplayName("Vender un producto a un cliente con nombre diferente devuelve true")
    void venderProductoClienteNombreDiferenteDevuelveTrue() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 1);
        assertTrue(vendedor.venderProducto("P1", "cliente", 1));
    }

    @Test
    @DisplayName("Vender a dos clientes registra dos filas en el archivo de clientes")
    void venderAClientesRegistraDosFilasEnClientesCSV() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 1);
        vendedor.venderProducto("P1", "cliente", 1);
        MotorCSV csv = new MotorCSV(directorio);
        assertEquals(2, csv.leerCSV(csv.rutaDatos("clientes.csv").toString()).size());
    }

    @Test
    @DisplayName("Vender a dos clientes vincula ambas ventas con el mismo id de cliente")
    void venderAClientesVinculaConMismoIdCliente() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 1);
        vendedor.venderProducto("P1", "cliente", 1);
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertEquals(ventas.get(1)[3], ventas.get(2)[3]);
    }

    @Test
    @DisplayName("Generar una orden con productos no devuelve nulo")
    void generarOrdenConProductosDevuelveOrdenNoNula() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        assertNotNull(orden);
    }

    @Test
    @DisplayName("Confirmar una orden valida devuelve true")
    void confirmarOrdenValidaDevuelveTrue() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        assertTrue(vendedor.confirmarVenta(orden));
    }

    @Test
    @DisplayName("El archivo de clientes tiene cuatro columnas")
    void archivoClientesTieneCuatroColumnas() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> clientes = csv.leerCSV(csv.rutaDatos("clientes.csv").toString());
        assertEquals(4, clientes.get(0).length);
    }

    @Test
    @DisplayName("El cliente registrado conserva su ci")
    void clienteRegistraCi() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> clientes = csv.leerCSV(csv.rutaDatos("clientes.csv").toString());
        assertEquals("CI-98765", clientes.get(1)[2]);
    }

    @Test
    @DisplayName("El cliente registrado conserva su telefono")
    void clienteRegistraTelefono() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> clientes = csv.leerCSV(csv.rutaDatos("clientes.csv").toString());
        assertEquals("+591 70123456", clientes.get(1)[3]);
    }

    @Test
    @DisplayName("La venta vincula al cliente")
    void ventaVinculaAlCliente() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> clientes = csv.leerCSV(csv.rutaDatos("clientes.csv").toString());
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertEquals(clientes.get(1)[0], ventas.get(1)[12]);
    }

    @Test
    @DisplayName("La venta registra el ci del cliente")
    void ventaRegistraCiDelCliente() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertEquals("CI-98765", ventas.get(1)[3]);
    }

    @Test
    @DisplayName("La venta confirmada registra tres garantias")
    void ventaConfirmadaRegistraTresGarantias() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> garantias = csv.leerCSV(csv.rutaDatos("garantias.csv").toString());
        assertEquals(3, garantias.size());
    }

    @Test
    @DisplayName("La garantia vincula con la venta")
    void garantiaVinculaConVenta() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        List<String[]> garantias = csv.leerCSV(csv.rutaDatos("garantias.csv").toString());
        assertEquals(ventas.get(1)[0], garantias.get(1)[1]);
    }

    @Test
    @DisplayName("La garantia tiene fecha de fin a los doce meses")
    void garantiaTieneFechaFinDoceMeses() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> garantias = csv.leerCSV(csv.rutaDatos("garantias.csv").toString());
        assertEquals(LocalDate.now().plusMonths(12).toString(), garantias.get(1)[10]);
    }

    @Test
    @DisplayName("La garantia del id de garantia es vigente")
    void garantiaPorIdGarantiaEsVigente() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> garantias = csv.leerCSV(csv.rutaDatos("garantias.csv").toString());
        assertTrue(vendedor.consultarGarantia(garantias.get(1)[0]).get(0).contains("VIGENTE"));
    }

    @Test
    @DisplayName("La garantia por id de venta es vigente")
    void garantiaPorIdVentaEsVigente() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertTrue(vendedor.consultarGarantia(ventas.get(1)[0]).get(0).contains("VIGENTE"));
    }

    @Test
    @DisplayName("La garantia por orden de venta es vigente")
    void garantiaPorOrdenDeVentaEsVigente() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        vendedor.confirmarVenta(orden);
        assertTrue(vendedor.consultarGarantia(orden).get(0).contains("VIGENTE"));
    }

    @Test
    @DisplayName("La segunda venta tiene un id de garantia")
    void segundaVentaTieneIdGarantia() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertNotNull(ventas.get(2)[14]);
    }

    @Test
    @DisplayName("Las ventas tienen ids de garantia distintos")
    void ventasTienenIdsGarantiaDistintos() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertNotEquals(ventas.get(1)[14], ventas.get(2)[14]);
    }

    @Test
    @DisplayName("Una garantía vencida se informa como no vigente")
    void garantiaVencida() {
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta(
                "Ana Cliente", "CI-98765", "70123456", Map.of("P1", 1));
        assertTrue(vendedor.confirmarVenta(orden));
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = csv.rutaDatos("garantias.csv").toString();
        List<String[]> garantias = csv.leerCSV(ruta);
        garantias.get(1)[10] = LocalDate.now().minusDays(1).toString();
        assertTrue(csv.escribirCSV(ruta, garantias, false));

        assertTrue(vendedor.consultarGarantia(orden).get(0).contains("NO VIGENTE"));
    }

    @Test
    @DisplayName("Importar clientes existentes conserva el nombre del cliente anterior")
    void importarClientesExistentesConservaNombreAnterior() {
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("clientes.csv").toString(), List.of(
                new String[]{"idCliente", "nombreCliente"},
                new String[]{"CLI-ANTERIOR", "Cliente existente"}));
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta(
                "Cliente nuevo", "CI-NUEVO", "70123456", Map.of("P1", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> clientes = csv.leerCSV(csv.rutaDatos("clientes.csv").toString());
        assertEquals("Cliente existente", clientes.get(1)[1]);
    }

    @Test
    @DisplayName("Importar clientes existentes preserva el esquema de cuatro columnas")
    void importarClientesExistentesPreservaEsquemaCuatroColumnas() {
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("clientes.csv").toString(), List.of(
                new String[]{"idCliente", "nombreCliente"},
                new String[]{"CLI-ANTERIOR", "Cliente existente"}));
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta(
                "Cliente nuevo", "CI-NUEVO", "70123456", Map.of("P1", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> clientes = csv.leerCSV(csv.rutaDatos("clientes.csv").toString());
        assertEquals(4, clientes.get(0).length);
    }

    @Test
    @DisplayName("Importar clientes existentes registra el ci del cliente nuevo")
    void importarClientesExistentesRegistraCiNuevo() {
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("clientes.csv").toString(), List.of(
                new String[]{"idCliente", "nombreCliente"},
                new String[]{"CLI-ANTERIOR", "Cliente existente"}));
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta(
                "Cliente nuevo", "CI-NUEVO", "70123456", Map.of("P1", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> clientes = csv.leerCSV(csv.rutaDatos("clientes.csv").toString());
        assertEquals("CI-NUEVO", clientes.get(2)[2]);
    }

    @Test
    @DisplayName("Vender un producto tras migrar esquema de ventas devuelve true")
    void venderProductoTrasMigrarEsquemaDevuelveTrue() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("ventas.csv").toString(), List.of(
                new String[]{"idVenta", "idOrden", "estado", "ciCliente", "nombreCliente", "idProducto",
                        "nombreProducto", "cantidad", "precioUnitario", "total", "fechaVenta", "ciEmpleado"},
                new String[]{"VEN-HIST", "ORD-HIST", "CONFIRMADA", "CI-HIST", "Cliente historico",
                        "P1", "Producto", "1", "100", "100", "2026-01-01T00:00:00", "CI-VEN"}));
        assertTrue(vendedor.venderProducto("P1", "Cliente nuevo", 1));
    }

    @Test
    @DisplayName("Migrar esquema de ventas conserva las ventas historicas")
    void migrarEsquemaVentasConservaVentasHistoricas() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("ventas.csv").toString(), List.of(
                new String[]{"idVenta", "idOrden", "estado", "ciCliente", "nombreCliente", "idProducto",
                        "nombreProducto", "cantidad", "precioUnitario", "total", "fechaVenta", "ciEmpleado"},
                new String[]{"VEN-HIST", "ORD-HIST", "CONFIRMADA", "CI-HIST", "Cliente historico",
                        "P1", "Producto", "1", "100", "100", "2026-01-01T00:00:00", "CI-VEN"}));
        vendedor.venderProducto("P1", "Cliente nuevo", 1);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertEquals(3, ventas.size());
    }

    @Test
    @DisplayName("El esquema actualizado de ventas tiene quince columnas")
    void esquemaActualizadoVentasTieneQuinceColumnas() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("ventas.csv").toString(), List.of(
                new String[]{"idVenta", "idOrden", "estado", "ciCliente", "nombreCliente", "idProducto",
                        "nombreProducto", "cantidad", "precioUnitario", "total", "fechaVenta", "ciEmpleado"},
                new String[]{"VEN-HIST", "ORD-HIST", "CONFIRMADA", "CI-HIST", "Cliente historico",
                        "P1", "Producto", "1", "100", "100", "2026-01-01T00:00:00", "CI-VEN"}));
        vendedor.venderProducto("P1", "Cliente nuevo", 1);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertEquals(15, ventas.get(0).length);
    }

    @Test
    @DisplayName("La venta historica conserva su id de venta")
    void ventaHistoricaConservaIdVenta() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("ventas.csv").toString(), List.of(
                new String[]{"idVenta", "idOrden", "estado", "ciCliente", "nombreCliente", "idProducto",
                        "nombreProducto", "cantidad", "precioUnitario", "total", "fechaVenta", "ciEmpleado"},
                new String[]{"VEN-HIST", "ORD-HIST", "CONFIRMADA", "CI-HIST", "Cliente historico",
                        "P1", "Producto", "1", "100", "100", "2026-01-01T00:00:00", "CI-VEN"}));
        vendedor.venderProducto("P1", "Cliente nuevo", 1);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertEquals("VEN-HIST", ventas.get(1)[0]);
    }

    @Test
    @DisplayName("La venta historica conserva el nombre del cliente")
    void ventaHistoricaConservaNombreCliente() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("ventas.csv").toString(), List.of(
                new String[]{"idVenta", "idOrden", "estado", "ciCliente", "nombreCliente", "idProducto",
                        "nombreProducto", "cantidad", "precioUnitario", "total", "fechaVenta", "ciEmpleado"},
                new String[]{"VEN-HIST", "ORD-HIST", "CONFIRMADA", "CI-HIST", "Cliente historico",
                        "P1", "Producto", "1", "100", "100", "2026-01-01T00:00:00", "CI-VEN"}));
        vendedor.venderProducto("P1", "Cliente nuevo", 1);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertEquals("Cliente historico", ventas.get(1)[4]);
    }

    @Test
    @DisplayName("La venta historica tiene el id de garantia vacio")
    void ventaHistoricaTieneIdGarantiaVacio() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("ventas.csv").toString(), List.of(
                new String[]{"idVenta", "idOrden", "estado", "ciCliente", "nombreCliente", "idProducto",
                        "nombreProducto", "cantidad", "precioUnitario", "total", "fechaVenta", "ciEmpleado"},
                new String[]{"VEN-HIST", "ORD-HIST", "CONFIRMADA", "CI-HIST", "Cliente historico",
                        "P1", "Producto", "1", "100", "100", "2026-01-01T00:00:00", "CI-VEN"}));
        vendedor.venderProducto("P1", "Cliente nuevo", 1);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertEquals("", ventas.get(1)[14]);
    }

    @Test
    @DisplayName("Vender cantidad cero devuelve false")
    void venderCantidadCeroDevuelveFalse() {
        Vendedor vendedor = vendedor();
        assertFalse(vendedor.venderProducto("P1", "Cliente", 0));
    }

    @Test
    @DisplayName("Vender cantidad negativa devuelve false")
    void venderCantidadNegativaDevuelveFalse() {
        Vendedor vendedor = vendedor();
        assertFalse(vendedor.venderProducto("P1", "Cliente", -1));
    }

    @Test
    @DisplayName("Vender cantidad superior al stock devuelve false")
    void venderCantidadSuperiorAlStockDevuelveFalse() {
        Vendedor vendedor = vendedor();
        assertFalse(vendedor.venderProducto("P1", "Cliente", 11));
    }

    @Test
    @DisplayName("No vende con caja cerrada")
    void noVendeConCajaCerrada() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("caja.csv").toString(), List.of(
                new String[]{"estado", "fechaApertura"}, new String[]{"CERRADA", "2026-10-06"}));
        assertFalse(vendedor.venderProducto("P1", "Cliente", 1));
    }

    @Test
    @DisplayName("Con caja cerrada la proforma no devuelve nulo")
    void proformaConCajaCerradaNoEsNula() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("caja.csv").toString(), List.of(
                new String[]{"estado", "fechaApertura"}, new String[]{"CERRADA", "2026-10-06"}));
        String orden = vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 2));
        assertNotNull(orden);
    }

    @Test
    @DisplayName("Con caja cerrada no se puede confirmar la venta")
    void conCajaCerradaNoSeConfirmaVenta() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("caja.csv").toString(), List.of(
                new String[]{"estado", "fechaApertura"}, new String[]{"CERRADA", "2026-10-06"}));
        String orden = vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 2));
        assertFalse(vendedor.confirmarVenta(orden));
    }

    @Test
    @DisplayName("Con caja cerrada la proforma no descuenta stock")
    void proformaConCajaCerradaNoDescuentaStock() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("caja.csv").toString(), List.of(
                new String[]{"estado", "fechaApertura"}, new String[]{"CERRADA", "2026-10-06"}));
        vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 2));
        assertEquals(10, vendedor.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("Importar una orden desde archivo devuelve true")
    void importarOrdenDesdeArchivoDevuelveTrue() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("ordenes.csv").toString();
        csv.escribirCSV(ruta, List.of(new String[]{"idOrden", "nombreCliente", "idProducto", "cantidad"},
                new String[]{"EXT-1", "Cliente", "P1", "3"}));
        assertTrue(vendedor.ingresarOrdenDesdeArchivo(ruta));
    }

    @Test
    @DisplayName("Importar una orden no descuenta stock")
    void importarOrdenNoDescuentaStock() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("ordenes.csv").toString();
        csv.escribirCSV(ruta, List.of(new String[]{"idOrden", "nombreCliente", "idProducto", "cantidad"},
                new String[]{"EXT-1", "Cliente", "P1", "3"}));
        vendedor.ingresarOrdenDesdeArchivo(ruta);
        assertEquals(10, vendedor.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("Importar una orden crea una venta de tipo proforma")
    void importarOrdenCreaVentaProforma() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("ordenes.csv").toString();
        csv.escribirCSV(ruta, List.of(new String[]{"idOrden", "nombreCliente", "idProducto", "cantidad"},
                new String[]{"EXT-1", "Cliente", "P1", "3"}));
        vendedor.ingresarOrdenDesdeArchivo(ruta);
        assertTrue(csv.leerCSV(csv.rutaDatos("ventas.csv").toString()).get(1)[2].equals("PROFORMA"));
    }

    @Test
    @DisplayName("Anular una venta inexistente devuelve false")
    void anularVentaInexistenteDevuelveFalse() {
        Vendedor vendedor = vendedor();
        assertFalse(vendedor.eliminarVenta("NO-EXISTE"));
    }

    @Test
    @DisplayName("Anular una proforma devuelve true")
    void anularProformaDevuelveTrue() {
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 1));
        assertTrue(vendedor.eliminarVenta(orden));
    }

    @Test
    @DisplayName("No se puede confirmar una orden anulada")
    void noSeConfirmaOrdenAnulada() {
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 1));
        vendedor.eliminarVenta(orden);
        assertFalse(vendedor.confirmarVenta(orden));
    }

    @Test
    @DisplayName("Vender dos unidades de un producto devuelve true")
    void venderDosUnidadesDevuelveTrue() {
        Vendedor vendedor = vendedor();
        assertTrue(vendedor.venderProducto("P1", "Cliente", 2));
    }

    @Test
    @DisplayName("Vender dos unidades descuenta el stock")
    void venderDosUnidadesDescuentaStock() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 2);
        assertEquals(8, vendedor.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("Anular una venta confirmada devuelve true")
    void anularVentaConfirmadaDevuelveTrue() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 2);
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        String idVenta = ventas.get(1)[0];
        assertTrue(vendedor.eliminarVenta(idVenta));
    }

    @Test
    @DisplayName("Anular una venta confirmada restaura el stock")
    void anularVentaConfirmadaRestauraStock() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 2);
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        String idVenta = ventas.get(1)[0];
        vendedor.eliminarVenta(idVenta);
        assertEquals(10, vendedor.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("Anular dos veces la misma venta devuelve false")
    void anularDosVecesLaMismaVentaDevuelveFalse() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 2);
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        String idVenta = ventas.get(1)[0];
        vendedor.eliminarVenta(idVenta);
        assertFalse(vendedor.eliminarVenta(idVenta));
    }

    @Test
    @DisplayName("Una venta anulada tiene estado anulada")
    void ventaAnuladaTieneEstadoAnulada() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 2);
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        String idVenta = ventas.get(1)[0];
        vendedor.eliminarVenta(idVenta);
        assertEquals("ANULADA", csv.leerCSV(csv.rutaDatos("ventas.csv").toString()).get(1)[2]);
    }

    @Test
    @DisplayName("Importar ordenes con una fila invalida devuelve false")
    void importarOrdenesConFilaInvalidaDevuelveFalse() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("ordenes-invalidas.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idOrden", "nombreCliente", "idProducto", "cantidad"},
                new String[]{"EXT-1", "Cliente", "P1", "1"},
                new String[]{"EXT-2", "Cliente", "NO-EXISTE", "1"}));
        assertFalse(vendedor.ingresarOrdenDesdeArchivo(ruta));
    }

    @Test
    @DisplayName("Importar ordenes invalidas no crea ventas")
    void importarOrdenesInvalidasNoCreaVentas() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("ordenes-invalidas.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idOrden", "nombreCliente", "idProducto", "cantidad"},
                new String[]{"EXT-1", "Cliente", "P1", "1"},
                new String[]{"EXT-2", "Cliente", "NO-EXISTE", "1"}));
        vendedor.ingresarOrdenDesdeArchivo(ruta);
        assertEquals(0, csv.leerCSV(csv.rutaDatos("ventas.csv").toString()).size());
    }

    @Test
    @DisplayName("Importar ordenes invalidas no modifica el stock")
    void importarOrdenesInvalidasNoModificaStock() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("ordenes-invalidas.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idOrden", "nombreCliente", "idProducto", "cantidad"},
                new String[]{"EXT-1", "Cliente", "P1", "1"},
                new String[]{"EXT-2", "Cliente", "NO-EXISTE", "1"}));
        vendedor.ingresarOrdenDesdeArchivo(ruta);
        assertEquals(10, vendedor.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("Importar ordenes con columnas reordenadas devuelve true")
    void importarOrdenesColumnasReordenadasDevuelveTrue() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("ordenes-reordenadas.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"cantidad", "idProducto", "nombreCliente", "idOrden"},
                new String[]{"2", "P1", "Cliente", "EXT-REORDENADA"}));
        assertTrue(vendedor.ingresarOrdenDesdeArchivo(ruta));
    }

    @Test
    @DisplayName("Importar ordenes con columnas reordenadas genera id de orden")
    void importarOrdenesColumnasReordenadasGeneraIdOrden() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("ordenes-reordenadas.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"cantidad", "idProducto", "nombreCliente", "idOrden"},
                new String[]{"2", "P1", "Cliente", "EXT-REORDENADA"}));
        vendedor.ingresarOrdenDesdeArchivo(ruta);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertTrue(ventas.get(1)[1].startsWith("ORD-"));
    }

    @Test
    @DisplayName("Importar ordenes con columnas reordenadas crea proforma")
    void importarOrdenesColumnasReordenadasCreaProforma() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("ordenes-reordenadas.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"cantidad", "idProducto", "nombreCliente", "idOrden"},
                new String[]{"2", "P1", "Cliente", "EXT-REORDENADA"}));
        vendedor.ingresarOrdenDesdeArchivo(ruta);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertEquals("PROFORMA", ventas.get(1)[2]);
    }

    @Test
    @DisplayName("Importar ordenes con columnas reordenadas guarda la cantidad")
    void importarOrdenesColumnasReordenadasGuardaCantidad() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("ordenes-reordenadas.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"cantidad", "idProducto", "nombreCliente", "idOrden"},
                new String[]{"2", "P1", "Cliente", "EXT-REORDENADA"}));
        vendedor.ingresarOrdenDesdeArchivo(ruta);
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertEquals("2", ventas.get(1)[7]);
    }

    @Test
    @DisplayName("Generar orden con stock insuficiente devuelve nulo")
    void generarOrdenStockInsuficienteDevuelveNulo() {
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 11));
        assertNull(orden);
    }

    @Test
    @DisplayName("Orden con stock insuficiente no crea ventas")
    void ordenStockInsuficienteNoCreaVentas() {
        Vendedor vendedor = vendedor();
        vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 11));
        MotorCSV csv = new MotorCSV(directorio);
        assertTrue(csv.leerCSV(csv.rutaDatos("ventas.csv").toString()).isEmpty());
    }

    @Test
    @DisplayName("Orden con stock insuficiente no modifica el inventario")
    void ordenStockInsuficienteNoModificaInventario() {
        Vendedor vendedor = vendedor();
        vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 11));
        assertEquals(10, vendedor.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("Genera identificador de orden")
    void generarOrdenDeVentaNoEsVacia() {
        assertFalse(vendedor().generarOrdenDeVenta(producto()).isBlank());
    }

    @Test
    @DisplayName("Buscar producto por id devuelve un resultado")
    void buscarProductoPorIdDevuelveResultado() {
        Vendedor vendedor = vendedor();
        assertNotNull(vendedor.buscarEnInventario("P1"));
    }

    @Test
    @DisplayName("Buscar producto por nombre devuelve un resultado")
    void buscarProductoPorNombreDevuelveResultado() {
        Vendedor vendedor = vendedor();
        assertNotNull(vendedor.buscarEnInventario("producto"));
    }

    @Test
    @DisplayName("Buscar producto por marca devuelve un resultado")
    void buscarProductoPorMarcaDevuelveResultado() {
        Vendedor vendedor = vendedor();
        assertNotNull(vendedor.buscarEnInventario("marca"));
    }

    @Test
    @DisplayName("Buscar producto con id nulo devuelve null")
    void buscarProductoConIdNuloDevuelveNull() {
        Vendedor vendedor = vendedor();
        assertNull(vendedor.buscarEnInventario(null));
    }

    @Test
    @DisplayName("La factura incluye el nombre del cliente")
    void facturaIncluyeNombreCliente() {
        String factura = vendedor().generarFactura("ORD-1", "Cliente", List.of("Producto x2 | 20.00"));
        assertTrue(factura.contains("Cliente"));
    }

    @Test
    @DisplayName("La factura incluye el detalle del producto")
    void facturaIncluyeDetalleProducto() {
        String factura = vendedor().generarFactura("ORD-1", "Cliente", List.of("Producto x2 | 20.00"));
        assertTrue(factura.contains("Producto x2"));
    }

    @Test
    @DisplayName("La factura incluye el precio del producto")
    void facturaIncluyePrecioProducto() {
        String factura = vendedor().generarFactura("ORD-1", "Cliente", List.of("Producto x2 | 20.00"));
        assertTrue(factura.contains("20.00"));
    }

    @Test
    @DisplayName("Generar factura con producto nulo no lanza excepcion")
    void generarFacturaProductoNuloNoLanzaExcepcion() {
        assertDoesNotThrow(() -> vendedor().generarFactura((Producto) null));
    }

    @Test
    @DisplayName("Calcular costo con precio operativo e iva aplica la formula")
    void calcularCostoAplicaFormula() {
        assertEquals(120.0, vendedor().calcularCosto(100, 50, 10), 1e-9);
    }

    @Test
    @DisplayName("Calcular costo con todos los valores en cero devuelve cero")
    void calcularCostoTodosCerosDevuelveCero() {
        assertEquals(0.0, vendedor().calcularCosto(0, 0, 0), 1e-9);
    }

    @Test
    @DisplayName("Calcular costo con precio cero aplica solo operativo e iva")
    void calcularCostoPrecioCeroAplicaOperativoEIva() {
        assertEquals(20.0, vendedor().calcularCosto(0, 100, 0), 1e-9);
    }

    @Test
    @DisplayName("Calcular costo con valores decimales aplica la formula")
    void calcularCostoValoresDecimalesAplicaFormula() {
        assertEquals(25.5, vendedor().calcularCosto(10.0, 50.0, 5.5), 1e-9);
    }

    @Test
    @DisplayName("Calcular costo con valores grandes aplica la formula")
    void calcularCostoValoresGrandesAplicaFormula() {
        assertEquals(1_200_000_000.0,
                vendedor().calcularCosto(1_000_000_000.0, 1_000_000_000.0, 0.0), 1e-9);
    }

    @Test
    @DisplayName("Buscar por razon que no coincide devuelve mapa vacio")
    void buscarPorRazonQueNoCoincideDevuelveMapaVacio() {
        Vendedor vendedor = vendedor();
        assertTrue(vendedor.buscarPorRazon("razon").isEmpty());
    }

    @Test
    @DisplayName("Buscar por razon que coincide devuelve el producto")
    void buscarPorRazonQueCoincideDevuelveProducto() {
        Vendedor vendedor = vendedor();
        assertTrue(vendedor.buscarPorRazon("mar").containsKey("P1"));
    }

    @Test
    @DisplayName("Buscar por razon nula no devuelve null")
    void buscarPorRazonNulaNoDevuelveNull() {
        Vendedor vendedor = vendedor();
        assertNotNull(vendedor.buscarPorRazon(null));
    }

    @Test
    @DisplayName("Vender un producto inexistente devuelve false")
    public void venderProductoInexistenteDevuelveFalse() {
        Vendedor vendedor = vendedor();
        assertFalse(vendedor.venderProducto("NO-EXISTE", "Cliente", 1));
    }

    @Test
    @DisplayName("Vender con nombre de cliente en blanco devuelve false")
    public void venderConClienteEnBlancoDevuelveFalse() {
        Vendedor vendedor = vendedor();
        assertFalse(vendedor.venderProducto("P1", "   ", 1));
    }

    @Test
    @DisplayName("Generar orden de venta con mapa de productos vacio devuelve nulo")
    public void generarOrdenDeVentaConProductosVaciosDevuelveNulo() {
        Vendedor vendedor = vendedor();
        assertNull(vendedor.generarOrdenDeVenta("Cliente", Map.of()));
    }
}