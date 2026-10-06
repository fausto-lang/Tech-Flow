package user;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import models.Producto;
import services.MotorCSV;

@DisplayName("Almacen")
class AlmacenTest {

    @TempDir
    Path directorio;

    private Almacen almacen() {
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("inventario.csv").toString(), List.of(
                new String[]{"idProducto", "nombre", "marca", "categoria", "descripcion", "precioEntrada", "precioVenta", "stock"},
                new String[]{"P1", "Producto", "Marca", "Cat", "desc", "10", "15", "5"}));
        csv.escribirCSV(csv.rutaDatos("proveedores.csv").toString(), List.of(
                new String[]{"idProveedor", "nombreProveedor", "contactoProveedor"},
                new String[]{"PROV-01", "Proveedor", "123"}));
        return new Almacen("CI-ALM", "Alma", "pw", directorio);
    }

    private Producto producto(String id) {
        return new Producto(id, "Producto nuevo", 10.0, 15.0, "desc", 5, "Marca", "Cat");
    }

    @Test
    @DisplayName("El constructor asigna rol y datos")
    void constructorAsignaDatos() {
        Almacen almacen = almacen();
        assertEquals(Rol.ALMACENERO, almacen.getRol());
        assertEquals("CI-ALM", almacen.getCi());
        assertEquals("Alma", almacen.getNombre());
        assertTrue(almacen instanceof Empleado);
    }

    @Test
    @DisplayName("Registra producto nuevo y rechaza ids duplicados")
    void ingresarProductoNuevo() {
        Almacen almacen = almacen();
        assertTrue(almacen.ingresarProductoNuevo(producto("P2")));
        assertFalse(almacen.ingresarProductoNuevo(producto("P2")));
        assertFalse(almacen.ingresarProductoNuevo(null));
    }

    @Test
    @DisplayName("Incrementa stock y deja registro de entrada")
    void ingresarProductoExistente() {
        Almacen almacen = almacen();
        assertTrue(almacen.ingresarProductoExistente("P1", 4));
        assertFalse(almacen.ingresarProductoExistente("NO-EXISTE", 4));
        assertFalse(almacen.ingresarProductoExistente("P1", 0));
        MotorCSV csv = new MotorCSV(directorio);
        assertEquals("9", csv.leerCSV(csv.rutaDatos("inventario.csv").toString()).get(1)[7]);
        assertEquals(2, csv.leerCSV(csv.rutaDatos("entradas.csv").toString()).size());
    }

    @Test
    @DisplayName("Importa recepciones de CSV actualizando inventario e historial")
    void importaEntradas() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("carga.txt").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-01", "P1", "Producto", "Marca", "Cat", "11", "3"},
                new String[]{"PROV-01", "P2", "Nuevo", "Marca2", "Cat2", "20", "2"}));
        assertTrue(almacen.ingresarEntradaDesdeArchivo(ruta));
        assertEquals("8", csv.leerCSV(csv.rutaDatos("inventario.csv").toString()).get(1)[7]);
        assertEquals("2", csv.leerCSV(csv.rutaDatos("inventario.csv").toString()).get(2)[7]);
        assertEquals(3, csv.leerCSV(csv.rutaDatos("entradas.csv").toString()).size());
    }

    @Test
    @DisplayName("No importa filas mal formadas ni proveedor inexistente")
    void rechazaEntradaInvalida() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("invalida.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "cantidad"},
                new String[]{"PROV-X", "P1", "4"}));
        assertFalse(almacen.ingresarEntradaDesdeArchivo(ruta));
        assertFalse(almacen.ingresarEntradaDesdeArchivo(""));
    }
}