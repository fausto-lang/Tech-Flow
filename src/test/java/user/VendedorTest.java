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
    @DisplayName("Una venta confirmada descuenta stock y persiste el estado")
    void venderProductoConfirmaYActualizaStock() {
        Vendedor vendedor = vendedor();
        assertTrue(vendedor.venderProducto("P1", "Cliente", 1));
        assertEquals(9, vendedor.buscarEnInventario("P1").getStock());
        List<String[]> ventas = new MotorCSV(directorio).leerCSV(
                new MotorCSV(directorio).rutaDatos("ventas.csv").toString());
        assertEquals("CONFIRMADA", ventas.get(1)[2]);
        assertEquals("Cliente", ventas.get(1)[4]);
        List<String[]> clientes = new MotorCSV(directorio).leerCSV(
            new MotorCSV(directorio).rutaDatos("clientes.csv").toString());
        assertEquals(2, clientes.size());
        assertEquals(clientes.get(1)[0], ventas.get(1)[12]);
        assertEquals("Cliente", clientes.get(1)[1]);
    }

    @Test
    @DisplayName("Una proforma no descuenta stock hasta ser confirmada")
    void proformaYConfirmacion() {
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 2));
        assertNotNull(orden);
        assertEquals(10, vendedor.buscarEnInventario("P1").getStock());
        MotorCSV csv = new MotorCSV(directorio);
        assertEquals(0, csv.leerCSV(csv.rutaDatos("clientes.csv").toString()).size());
        assertTrue(vendedor.confirmarVenta(orden));
        assertEquals(8, vendedor.buscarEnInventario("P1").getStock());
        assertFalse(vendedor.confirmarVenta(orden));
        assertEquals(2, csv.leerCSV(csv.rutaDatos("clientes.csv").toString()).size());
    }

    @Test
    @DisplayName("Reutiliza el registro del cliente en compras posteriores")
    void reutilizaClienteExistente() {
        Vendedor vendedor = vendedor();
        assertTrue(vendedor.venderProducto("P1", "Cliente", 1));
        assertTrue(vendedor.venderProducto("P1", "cliente", 1));

        MotorCSV csv = new MotorCSV(directorio);
        assertEquals(2, csv.leerCSV(csv.rutaDatos("clientes.csv").toString()).size());
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertEquals(ventas.get(1)[3], ventas.get(2)[3]);
    }

    @Test
    @DisplayName("Guarda datos del cliente y emite garantía por producto confirmado")
    void emiteYGestionaGarantia() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.agregarFila(csv.rutaDatos("inventario.csv").toString(),
            new String[]{"P2", "Producto 2", "Marca", "Cat", "desc", "50", "75", "4"});
        String orden = vendedor.generarOrdenDeVenta(
            "Ana Cliente", "CI-98765", "+591 70123456", Map.of("P1", 2, "P2", 1));
        assertNotNull(orden);
        assertTrue(vendedor.confirmarVenta(orden));

        List<String[]> clientes = csv.leerCSV(csv.rutaDatos("clientes.csv").toString());
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        List<String[]> garantias = csv.leerCSV(csv.rutaDatos("garantias.csv").toString());
        assertEquals(4, clientes.get(0).length);
        assertEquals("CI-98765", clientes.get(1)[2]);
        assertEquals("+591 70123456", clientes.get(1)[3]);
        assertEquals(clientes.get(1)[0], ventas.get(1)[12]);
        assertEquals("CI-98765", ventas.get(1)[3]);
        assertEquals(3, garantias.size());
        assertEquals(ventas.get(1)[0], garantias.get(1)[1]);
        assertEquals(LocalDate.now().plusMonths(12).toString(), garantias.get(1)[10]);
        assertTrue(vendedor.consultarGarantia(garantias.get(1)[0]).get(0).contains("VIGENTE"));
        assertTrue(vendedor.consultarGarantia(ventas.get(1)[0]).get(0).contains("VIGENTE"));
        assertTrue(vendedor.consultarGarantia(orden).get(0).contains("VIGENTE"));
        assertNotNull(ventas.get(2)[14]);
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
    @DisplayName("Amplía clientes CSV preservando filas anteriores")
    void migraClientesExistentesSinPerderDatos() {
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("clientes.csv").toString(), List.of(
                new String[]{"idCliente", "nombreCliente"},
                new String[]{"CLI-ANTERIOR", "Cliente existente"}));
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta(
                "Cliente nuevo", "CI-NUEVO", "70123456", Map.of("P1", 1));
        assertTrue(vendedor.confirmarVenta(orden));

        List<String[]> clientes = csv.leerCSV(csv.rutaDatos("clientes.csv").toString());
        assertEquals("Cliente existente", clientes.get(1)[1]);
        assertEquals(4, clientes.get(0).length);
        assertEquals("CI-NUEVO", clientes.get(2)[2]);
    }

    @Test
    @DisplayName("Actualiza el esquema de ventas sin borrar ventas históricas")
    void migraVentasExistentesSinPerderDatos() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("ventas.csv").toString(), List.of(
                new String[]{"idVenta", "idOrden", "estado", "ciCliente", "nombreCliente", "idProducto",
                        "nombreProducto", "cantidad", "precioUnitario", "total", "fechaVenta", "ciEmpleado"},
                new String[]{"VEN-HIST", "ORD-HIST", "CONFIRMADA", "CI-HIST", "Cliente historico",
                        "P1", "Producto", "1", "100", "100", "2026-01-01T00:00:00", "CI-VEN"}));

        assertTrue(vendedor.venderProducto("P1", "Cliente nuevo", 1));

        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertEquals(3, ventas.size());
        assertEquals(15, ventas.get(0).length);
        assertEquals("VEN-HIST", ventas.get(1)[0]);
        assertEquals("Cliente historico", ventas.get(1)[4]);
        assertEquals("", ventas.get(1)[14]);
    }

    @Test
    @DisplayName("No permite cantidades inválidas ni exceder stock")
    void validaCantidadYStock() {
        Vendedor vendedor = vendedor();
        assertFalse(vendedor.venderProducto("P1", "Cliente", 0));
        assertFalse(vendedor.venderProducto("P1", "Cliente", -1));
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
    @DisplayName("Permite cotizar con caja cerrada, pero no confirmar la venta")
    void proformaConCajaCerrada() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("caja.csv").toString(), List.of(
                new String[]{"estado", "fechaApertura"}, new String[]{"CERRADA", "2026-10-06"}));

        String orden = vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 2));

        assertNotNull(orden);
        assertFalse(vendedor.confirmarVenta(orden));
        assertEquals(10, vendedor.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("Importa órdenes CSV como proformas")
    void importaOrdenDesdeArchivo() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("ordenes.csv").toString();
        csv.escribirCSV(ruta, List.of(new String[]{"idOrden", "nombreCliente", "idProducto", "cantidad"},
                new String[]{"EXT-1", "Cliente", "P1", "3"}));
        assertTrue(vendedor.ingresarOrdenDesdeArchivo(ruta));
        assertEquals(10, vendedor.buscarEnInventario("P1").getStock());
        assertTrue(csv.leerCSV(csv.rutaDatos("ventas.csv").toString()).get(1)[2].equals("PROFORMA"));
    }

    @Test
    @DisplayName("Anula proformas y rechaza ventas inexistentes")
    void anulaOrden() {
        Vendedor vendedor = vendedor();
        assertFalse(vendedor.eliminarVenta("NO-EXISTE"));
        String orden = vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 1));
        assertTrue(vendedor.eliminarVenta(orden));
        assertFalse(vendedor.confirmarVenta(orden));
    }

    @Test
    @DisplayName("Anular una venta confirmada restaura el stock y no permite anularla dos veces")
    void anulaVentaConfirmadaUnaSolaVez() {
        Vendedor vendedor = vendedor();
        assertTrue(vendedor.venderProducto("P1", "Cliente", 2));
        assertEquals(8, vendedor.buscarEnInventario("P1").getStock());

        List<String[]> ventas = new MotorCSV(directorio).leerCSV(
                new MotorCSV(directorio).rutaDatos("ventas.csv").toString());
        String idVenta = ventas.get(1)[0];

        assertTrue(vendedor.eliminarVenta(idVenta));
        assertEquals(10, vendedor.buscarEnInventario("P1").getStock());
        assertFalse(vendedor.eliminarVenta(idVenta));
        assertEquals(10, vendedor.buscarEnInventario("P1").getStock());
        assertEquals("ANULADA", new MotorCSV(directorio)
                .leerCSV(new MotorCSV(directorio).rutaDatos("ventas.csv").toString()).get(1)[2]);
    }

    @Test
    @DisplayName("Una importacion con una fila invalida no persiste las filas anteriores")
    void importacionInvalidaNoPersisteParcialmente() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("ordenes-invalidas.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idOrden", "nombreCliente", "idProducto", "cantidad"},
                new String[]{"EXT-1", "Cliente", "P1", "1"},
                new String[]{"EXT-2", "Cliente", "NO-EXISTE", "1"}));

        assertFalse(vendedor.ingresarOrdenDesdeArchivo(ruta));
        assertEquals(0, csv.leerCSV(csv.rutaDatos("ventas.csv").toString()).size());
        assertEquals(10, vendedor.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("La importacion de ordenes usa nombres de columnas aunque cambie el orden")
    void importacionAceptaColumnasReordenadas() {
        Vendedor vendedor = vendedor();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("ordenes-reordenadas.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"cantidad", "idProducto", "nombreCliente", "idOrden"},
                new String[]{"2", "P1", "Cliente", "EXT-REORDENADA"}));

        assertTrue(vendedor.ingresarOrdenDesdeArchivo(ruta));
        List<String[]> ventas = csv.leerCSV(csv.rutaDatos("ventas.csv").toString());
        assertTrue(ventas.get(1)[1].startsWith("ORD-"));
        assertEquals("PROFORMA", ventas.get(1)[2]);
        assertEquals("2", ventas.get(1)[7]);
    }

    @Test
    @DisplayName("Una confirmacion rechazada por stock no modifica ventas ni inventario")
    void confirmacionSinStockNoPersisteCambios() {
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta("Cliente", Map.of("P1", 11));

        assertNull(orden);
        MotorCSV csv = new MotorCSV(directorio);
        assertTrue(csv.leerCSV(csv.rutaDatos("ventas.csv").toString()).isEmpty());
        assertEquals(10, vendedor.buscarEnInventario("P1").getStock());
    }

    @Test
    @DisplayName("Genera identificador de orden")
    void generarOrdenDeVentaNoEsVacia() {
        assertFalse(vendedor().generarOrdenDeVenta(producto()).isBlank());
    }

    @Test
    @DisplayName("Busca por id, nombre y marca")
    void buscarEnInventario() {
        Vendedor vendedor = vendedor();
        assertNotNull(vendedor.buscarEnInventario("P1"));
        assertNotNull(vendedor.buscarEnInventario("producto"));
        assertNotNull(vendedor.buscarEnInventario("marca"));
        assertNull(vendedor.buscarEnInventario(null));
    }

    @Test
    @DisplayName("Factura válida incluye cliente y detalle")
    void generarFactura() {
        String factura = vendedor().generarFactura("ORD-1", "Cliente", List.of("Producto x2 | 20.00"));
        assertTrue(factura.contains("Cliente"));
        assertTrue(factura.contains("Producto x2"));
        assertTrue(factura.contains("20.00"));
        assertDoesNotThrow(() -> vendedor().generarFactura((Producto) null));
    }

    @Test
    @DisplayName("calcularCosto aplica precio + 20% operativo + IVA")
    void calcularCostoAplicaFormula() {
        assertEquals(120.0, vendedor().calcularCosto(100, 50, 10), 1e-9);
        assertEquals(0.0, vendedor().calcularCosto(0, 0, 0), 1e-9);
        assertEquals(20.0, vendedor().calcularCosto(0, 100, 0), 1e-9);
        assertEquals(25.5, vendedor().calcularCosto(10.0, 50.0, 5.5), 1e-9);
        assertEquals(1_200_000_000.0,
                vendedor().calcularCosto(1_000_000_000.0, 1_000_000_000.0, 0.0), 1e-9);
    }

    @Test
    @DisplayName("Busca productos por razón, nombre o marca")
    void buscarPorRazon() {
        Vendedor vendedor = vendedor();
        assertTrue(vendedor.buscarPorRazon("razon").isEmpty());
        assertTrue(vendedor.buscarPorRazon("mar").containsKey("P1"));
        assertNotNull(vendedor.buscarPorRazon(null));
    }
}