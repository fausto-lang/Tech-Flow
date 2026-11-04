package services;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import user.Almacen;
import user.Vendedor;

@DisplayName("Integridad de filas CSV")
class IntegridadCSVTest {

    private static final String CI_CLIENTE = "12345678";
    private static final String TELEFONO_CLIENTE = "70123456";

    @TempDir
    Path directorio;

    private MotorCSV csv() {
        return new MotorCSV(directorio);
    }

    private Vendedor vendedor() {
        MotorCSV csv = csv();
        csv.escribirCSV(csv.rutaDatos("inventario.csv").toString(), List.of(
                new String[]{"idProducto", "nombre", "marca", "categoria", "descripcion", "precioEntrada", "precioVenta", "stock"},
                new String[]{"P1", "Producto", "Marca", "Cat", "desc", "100", "130", "10"}));
        csv.escribirCSV(csv.rutaDatos("caja.csv").toString(), List.of(
                new String[]{"estado", "fechaApertura"},
                new String[]{"ABIERTA", "2026-11-03"}));
        return new Vendedor("CI-VEN", "Vende", "pw1234", directorio);
    }

    @Test
    @DisplayName("Una venta confirmada no tiene columnas vacias")
    public void ventaConfirmadaNoTieneColumnasVacias() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 1);
        List<String[]> ventas = csv().leerCSV(csv().rutaDatos("ventas.csv").toString());
        assertFalse(Arrays.stream(ventas.get(1)).anyMatch(String::isBlank));
    }

    @Test
    @DisplayName("Una proforma no tiene columnas vacias")
    public void proformaNoTieneColumnasVacias() {
        Vendedor vendedor = vendedor();
        vendedor.generarOrdenDeVenta("Ana Cliente", CI_CLIENTE, TELEFONO_CLIENTE, java.util.Map.of("P1", 1));
        List<String[]> ventas = csv().leerCSV(csv().rutaDatos("ventas.csv").toString());
        assertFalse(Arrays.stream(ventas.get(1)).anyMatch(String::isBlank));
    }

    @Test
    @DisplayName("Un cliente registrado no tiene columnas vacias")
    public void clienteNoTieneColumnasVacias() {
        Vendedor vendedor = vendedor();
        vendedor.venderProducto("P1", "Cliente", 1);
        List<String[]> clientes = csv().leerCSV(csv().rutaDatos("clientes.csv").toString());
        assertFalse(Arrays.stream(clientes.get(1)).anyMatch(String::isBlank));
    }

    @Test
    @DisplayName("Una garantia no tiene columnas vacias")
    public void garantiaNoTieneColumnasVacias() {
        Vendedor vendedor = vendedor();
        String orden = vendedor.generarOrdenDeVenta(
                "Ana Cliente", CI_CLIENTE, TELEFONO_CLIENTE, java.util.Map.of("P1", 1));
        vendedor.confirmarVenta(orden);
        List<String[]> garantias = csv().leerCSV(csv().rutaDatos("garantias.csv").toString());
        assertFalse(Arrays.stream(garantias.get(1)).anyMatch(String::isBlank));
    }

    @Test
    @DisplayName("Una entrada no tiene columnas vacias")
    public void entradaNoTieneColumnasVacias() {
        vendedor();
        Almacen almacen = new Almacen("CI-ALM", "Alma", "pw1234", directorio);
        almacen.ingresarProductoExistente("P1", 2);
        List<String[]> entradas = csv().leerCSV(csv().rutaDatos("entradas.csv").toString());
        assertFalse(Arrays.stream(entradas.get(1)).anyMatch(String::isBlank));
    }
}
