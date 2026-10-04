package user;

import models.Producto;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Vendedor")
class VendedorTest {

    private Vendedor vendedor() {
        return new Vendedor("CI-VEN", "Vende", "pw");
    }

    private Producto producto() {
        return new Producto("P1", "Producto", 100.0, 150.0, "desc", 10, "Marca", "Cat");
    }

    @Test
    @DisplayName("El constructor asigna el rol CAJERO")
    void constructorAsignaRolCajero() {
        assertEquals(Rol.CAJERO, vendedor().getRol());
    }

    @Test
    @DisplayName("Vendedor es un Empleado (herencia)")
    void esUnEmpleado() {
        assertTrue(vendedor() instanceof Empleado);
    }

    @Test
    @DisplayName("venderProducto devuelve true (contrato base)")
    void venderProductoDevuelveTrue() {
        assertTrue(vendedor().venderProducto("P1", "CLI-1", 1));
    }

    @Test
    @DisplayName("eliminarVenta devuelve true (contrato base)")
    void eliminarVentaDevuelveTrue() {
        assertTrue(vendedor().eliminarVenta("V-001"));
    }

    @Test
    @DisplayName("generarOrdenDeVenta devuelve un texto no vacío")
    void generarOrdenDeVentaNoEsVacia() {
        assertFalse(vendedor().generarOrdenDeVenta(producto()).isBlank());
    }

    @Test
    @DisplayName("ingresarOrdenDesdeArchivo devuelve true (contrato base)")
    void ingresarOrdenDesdeArchivoDevuelveTrue() {
        assertTrue(vendedor().ingresarOrdenDesdeArchivo("ordenes.csv"));
    }

    @Test
    @DisplayName("confirmarVenta devuelve true (contrato base)")
    void confirmarVentaDevuelveTrue() {
        assertTrue(vendedor().confirmarVenta("ORDEN-1"));
    }

    @Test
    @DisplayName("buscarEnInventario no devuelve null")
    void buscarEnInventarioNoEsNull() {
        assertNotNull(vendedor().buscarEnInventario("P1"));
    }

    @Test
    @DisplayName("generarFactura devuelve un texto no vacío")
    void generarFacturaNoEsVacia() {
        assertFalse(vendedor().generarFactura(producto()).isBlank());
    }

    @Test
    @DisplayName("calcularCosto aplica precio + 20% operativo + IVA")
    void calcularCostoAplicaFormula() {
        // 100 + (50 * 0.20) + 10 = 120
        assertEquals(120.0, vendedor().calcularCosto(100, 50, 10), 1e-9);
    }

    @Test
    @DisplayName("calcularCosto con todo en cero devuelve cero")
    void calcularCostoConTodoCero() {
        assertEquals(0.0, vendedor().calcularCosto(0, 0, 0), 1e-9);
    }

    @Test
    @DisplayName("calcularCosto aplica el 20% solo al costo operativo")
    void calcularCostoSoloOperativo() {
        assertEquals(20.0, vendedor().calcularCosto(0, 100, 0), 1e-9);
    }

    @Test
    @DisplayName("calcularCosto con solo precio base no agrega nada más")
    void calcularCostoSoloPrecioBase() {
        assertEquals(100.0, vendedor().calcularCosto(100, 0, 0), 1e-9);
    }

    @Test
    @DisplayName("calcularCosto con solo IVA lo suma directo")
    void calcularCostoSoloIva() {
        assertEquals(10.0, vendedor().calcularCosto(0, 0, 10), 1e-9);
    }

    @Test
    @DisplayName("buscarPorRazon no devuelve null")
    void buscarPorRazonNoEsNull() {
        Map<String, Object> resultado = vendedor().buscarPorRazon("razon");
        assertNotNull(resultado);
    }

    @Test
    @DisplayName("No debería vender más unidades que el stock disponible")
    @Disabled("Pendiente de lógica: descontar stock y validar existencia/cantidad del producto")
    void venderProductoSinStockSuficiente() {
        assertFalse(vendedor().venderProducto("P1", "CLI-1", 999));
    }

    @Test
    @DisplayName("No debería permitir cantidad negativa o cero")
    @Disabled("Pendiente de lógica: validar que la cantidad a vender sea mayor que cero")
    void venderProductoConCantidadInvalida() {
        assertFalse(vendedor().venderProducto("P1", "CLI-1", 0));
    }

    @Test
    @DisplayName("calcularCosto con valores decimales respeta la fórmula")
    void calcularCostoDecimales() {
        assertEquals(25.5, vendedor().calcularCosto(10.0, 50.0, 5.5), 1e-9);
    }

    @Test
    @DisplayName("calcularCosto con costo operativo negativo resta el 20%")
    void calcularCostoNegativos() {
        assertEquals(80.0, vendedor().calcularCosto(100, -100, 0), 1e-9);
    }

    @Test
    @DisplayName("calcularCosto con números grandes no desborda")
    void calcularCostoGrandes() {
        assertEquals(1_200_000_000.0, vendedor().calcularCosto(1_000_000_000.0, 1_000_000_000.0, 0.0), 1e-9);
    }

    @Test
    @DisplayName("buscarEnInventario con código null no lanza excepción")
    void buscarEnInventarioConNullNoLanza() {
        assertDoesNotThrow(() -> vendedor().buscarEnInventario(null));
    }

    @Test
    @DisplayName("generarFactura con producto null no lanza excepción")
    void generarFacturaConNullNoLanza() {
        assertDoesNotThrow(() -> vendedor().generarFactura(null));
    }

    @Test
    @DisplayName("Vender exactamente el stock disponible debería ser válido")
    @Disabled("Pendiente de lógica: borde exacto de stock (cantidad == stock)")
    void venderExactamenteElStock() {
        assertTrue(vendedor().venderProducto("P1", "CLI-1", 10));
    }
}
