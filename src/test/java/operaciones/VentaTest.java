package operaciones;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class VentaTest {

    @TempDir
    Path tempDir;

    private File archivoSalidasMock;
    private File archivoInventarioMock;

    @BeforeEach
    void setUp() throws IOException {
        // simulacion de salidas (salidas.csv)
        archivoSalidasMock = tempDir.resolve("salidas.csv").toFile();
        try (FileWriter writer = new FileWriter(archivoSalidasMock)) {
            writer.write("idVenta,idProducto,cliente(CI),nombreProducto,cantidad,precioUnitario,fechaVenta\n");
            writer.write("VEN-1,Prod-01,12345678,Auriculares Inalámbricos Pro,2,299.99,2024-06-05\n");
            writer.write("VEN-2,Prod-02,87654321,Teclado Mecánico RGB,1,150.00,2024-06-06\n");
        }

        // simulacion de  inventario (inventario.csv)
        archivoInventarioMock = tempDir.resolve("inventario.csv").toFile();
        try (FileWriter writer = new FileWriter(archivoInventarioMock)) {
            writer.write("idProducto,nombre,marca,categoria,descripcion,precio,stock\n");
            writer.write("Prod-01,Auriculares Inalámbricos Pro,Sony,Audio,Auriculares de diadema con cancelación de ruido activa y Bluetooth 5.2,299.99,10\n");
            writer.write("Prod-02,Teclado Mecánico RGB,Redragon,Periféricos,Teclado mecánico switches red,150.00,0\n");
        }
    }

    @Test
    void generarIdVentaInicialEnArchivoVacio() throws IOException {
        File archivoVacio = tempDir.resolve("vacio.csv").toFile();
        archivoVacio.createNewFile();

        Venta venta = new Venta();
        String idVenta = venta.generarIdVenta();
        assertEquals("VEN-1", idVenta);
    }

    @Test
    void obtenerUltimoIdVentaDeTablaSimulada() {
        Venta venta = new Venta();
        String ultimoId = venta.obtenerUltimoIdVenta(archivoSalidasMock);
        assertEquals("VEN-2", ultimoId);
    }

    @Test
    void calcularTotalSinDescuentoTest() {
        Producto p1 = new Producto("Prod-01", "Sony", 299.99, "Desc", 2, "Auriculares Inalámbricos Pro", "Audio");
        Producto p2 = new Producto("Prod-02", "Redragon", 150.00, "Desc", 1, "Teclado Mecánico RGB", "Periféricos");
        
        Venta venta = new Venta("12345678", Arrays.asList(p1, p2));
        double total = venta.calcularTotal();
        
        assertEquals(749.98, total, 0.01);
    }

    @Test
    void aplicarDescuentoConMasDeDiezProductosTest() {
        List<Producto> productos = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            productos.add(new Producto("Prod-" + i, "Marca", 100.0, "Desc", 1, "Nombre " + i, "Categoria"));
        }

        Venta venta = new Venta("12345678", productos);
        double totalConDescuento = venta.aplicarDescuento(1000.0);
        assertTrue(totalConDescuento < 1000.0);
    }

    @Test
    void validarStockProductosConInventarioSimuladoTest() {
        Venta venta = new Venta();
        boolean stockValido = venta.validarStockProductos();
        assertFalse(stockValido);
    }

    @Test
    void ventaConClienteYProductosValidosTest() {
        Producto p1 = new Producto("Prod-1", "Sony", 299.99, "Desc", 1, "Auriculares Inalámbricos Pro", "Audio");
        Venta venta = new Venta("12345678", List.of(p1));
        
        assertNotNull(venta.getProductosVendidos());
        assertEquals(1, venta.getProductosVendidos().size());
    }

    @Test
    void constructorVacioTest() {
        Venta venta = new Venta();
        assertNull(venta.getProductosVendidos()) ;
    }

    @Test
    void registrarVentaMockTest() {
        Venta venta = new Venta();
        assertDoesNotThrow(venta::registarVenta);
    }

    @Test
    void generarFacturaMockTest() {
        Venta venta = new Venta();
        assertDoesNotThrow(venta::generarfactura);
    }

    @Test
    void escribirEnVentaCsvMockTest() {
        Venta venta = new Venta();
        assertDoesNotThrow(venta::escribirEnventaCsv);
    }

    @Test
    void actualizarStockProductosMockTest() {
        Venta venta = new Venta();
        assertDoesNotThrow(venta::actualizarStockProductos);
    }

    @Test
    void obtenerUltimoIdVentaArchivoInexistenteTest() {
        Venta venta = new Venta();
        String ultimoId = venta.obtenerUltimoIdVenta(new File("no_existe.csv"));
        assertNull(ultimoId);
    }



} 