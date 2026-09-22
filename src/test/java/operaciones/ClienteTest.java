package operaciones;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ClienteTest {

    @TempDir
    Path tempDir;

    @Test
    void clienteGuardaNombreCiYListaDeComprasTest() {
        Cliente cliente = new Cliente("Ana Perez", "12345678");

        assertEquals("Ana Perez", cliente.getNombre());
        assertEquals("12345678", cliente.getCi());
        assertNotNull(cliente.getCompras());
        assertTrue(cliente.getCompras().isEmpty());
    }

    @Test
    void agregarCompraAgregaProductoALaListaTest() {
        Cliente cliente = new Cliente("Ana Perez", "12345678");
        Producto producto = new Producto("Prod-01", "Sony", 299.99, "Desc", 2, "Auriculares", "Audio");

        cliente.agregarCompra(producto);

        assertEquals(1, cliente.getCompras().size());
        assertEquals("Prod-01", cliente.getCompras().get(0).getIdProducto());
        assertEquals(2, cliente.getCompras().get(0).getStock());
    }

    @Test
    void agregarCompraNullNoHaceNadaTest() {
        Cliente cliente = new Cliente("Ana Perez", "12345678");

        cliente.agregarCompra(null);

        assertTrue(cliente.getCompras().isEmpty());
    }

    @Test
    void cargarClientesCSVAgrupaComprasPorCiTest() throws IOException {
        File archivo = tempDir.resolve("clientes.csv").toFile();
        try (FileWriter writer = new FileWriter(archivo)) {
            writer.write("ci,nombreCliente,idProducto,nombreProducto,cantidad,fechaCompra\n");
            writer.write("12345678,Ana Perez,Prod-01,Auriculares,2,2026-09-15\n");
            writer.write("12345678,Ana Perez,Prod-02,Smartphone,1,2026-09-15\n");
            writer.write("87654321,Luis Gomez,Prod-09,Parlante,1,2026-09-16\n");
        }

        List<Cliente> clientes = Cliente.cargarClientesCSV(archivo.getPath());

        assertEquals(2, clientes.size());
        assertEquals("Ana Perez", clientes.get(0).getNombre());
        assertEquals(2, clientes.get(0).getCompras().size());
        assertEquals("87654321", clientes.get(1).getCi());
        assertEquals(1, clientes.get(1).getCompras().size());
    }

    @Test
    void cargarClientesCSVArchivoInexistenteDevuelveListaVaciaTest() {
        List<Cliente> clientes = Cliente.cargarClientesCSV("no_existe.csv");

        assertNotNull(clientes);
        assertTrue(clientes.isEmpty());
    }
}
