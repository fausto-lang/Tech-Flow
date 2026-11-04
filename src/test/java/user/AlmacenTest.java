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

    private static final String NOMBRE_INVALIDO = "@@@";
    private static final String PRECIO_CON_DOBLE_SIGNO = "10--20";
    private static final String PRECIO_NO_NUMERICO = "abc";
    private static final String PRECIO_NAN = "NaN";
    private static final String PRECIO_INFINITO = "Infinity";
    private static final String CANTIDAD_CON_LETRAS = "abc";
    private static final int STOCK_MAXIMO = Integer.MAX_VALUE;

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

    private String archivoImportacion(String precio, String cantidad) {
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("importacion-" + System.nanoTime() + ".csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-01", "P1", "Producto", "Marca", "Cat", precio, cantidad}));
        return ruta;
    }

    private String archivoImportacionProductoNuevo(String nombre, String marca, String categoria) {
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("importacion-nuevo-" + System.nanoTime() + ".csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-01", "P2", nombre, marca, categoria, "10", "2"}));
        return ruta;
    }

    @Test
    @DisplayName("El constructor asigna el rol ALMACENERO")
    void constructorAsignaRolAlmacenero() {
        assertEquals(Rol.ALMACENERO, almacen().getRol());
    }

    @Test
    @DisplayName("El constructor asigna la ci del almacen")
    void constructorAsignaCi() {
        assertEquals("CI-ALM", almacen().getCi());
    }

    @Test
    @DisplayName("El constructor asigna el nombre del almacen")
    void constructorAsignaNombre() {
        assertEquals("Alma", almacen().getNombre());
    }

    @Test
    @DisplayName("El almacen es una instancia de Empleado")
    void almacenEsInstanciaDeEmpleado() {
        assertTrue(almacen() instanceof Empleado);
    }

    @Test
    @DisplayName("Registrar un producto nuevo devuelve true")
    void ingresarProductoNuevoDevuelveTrue() {
        Almacen almacen = almacen();
        assertTrue(almacen.ingresarProductoNuevo(producto("P2")));
    }

    @Test
    @DisplayName("Registrar un producto con id duplicado devuelve false")
    void ingresarProductoNuevoDuplicadoDevuelveFalse() {
        Almacen almacen = almacen();
        almacen.ingresarProductoNuevo(producto("P2"));
        assertFalse(almacen.ingresarProductoNuevo(producto("P2")));
    }

    @Test
    @DisplayName("Registrar un producto nulo devuelve false")
    void ingresarProductoNuevoNuloDevuelveFalse() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarProductoNuevo(null));
    }

    @Test
    @DisplayName("Registrar producto nuevo crea dos filas en el CSV de entradas")
    void ingresarProductoNuevoCreaDosFilasEnEntradasCSV() {
        Almacen almacen = almacen();
        almacen.ingresarProductoNuevo(producto("P2"));
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> entradas = csv.leerCSV(csv.rutaDatos("entradas.csv").toString());
        assertEquals(2, entradas.size());
    }

    @Test
    @DisplayName("Registrar producto nuevo guarda el id del producto en entradas")
    void ingresarProductoNuevoGuardaIdProductoEnEntradas() {
        Almacen almacen = almacen();
        almacen.ingresarProductoNuevo(producto("P2"));
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> entradas = csv.leerCSV(csv.rutaDatos("entradas.csv").toString());
        assertEquals("P2", entradas.get(1)[2]);
    }

    @Test
    @DisplayName("Registrar producto nuevo guarda la cantidad en entradas")
    void ingresarProductoNuevoGuardaCantidadEnEntradas() {
        Almacen almacen = almacen();
        almacen.ingresarProductoNuevo(producto("P2"));
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> entradas = csv.leerCSV(csv.rutaDatos("entradas.csv").toString());
        assertEquals("5", entradas.get(1)[4]);
    }

    @Test
    @DisplayName("Ingresar un producto existente devuelve true")
    void ingresarProductoExistenteDevuelveTrue() {
        Almacen almacen = almacen();
        assertTrue(almacen.ingresarProductoExistente("P1", 4));
    }

    @Test
    @DisplayName("Ingresar un producto inexistente devuelve false")
    void ingresarProductoExistenteInexistenteDevuelveFalse() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarProductoExistente("NO-EXISTE", 4));
    }

    @Test
    @DisplayName("Ingresar cantidad cero de un producto existente devuelve false")
    void ingresarProductoExistenteCantidadCeroDevuelveFalse() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarProductoExistente("P1", 0));
    }

    @Test
    @DisplayName("Ingresar producto existente actualiza el stock en el inventario")
    void ingresarProductoExistenteActualizaStockEnInventario() {
        Almacen almacen = almacen();
        almacen.ingresarProductoExistente("P1", 4);
        MotorCSV csv = new MotorCSV(directorio);
        assertEquals("9", csv.leerCSV(csv.rutaDatos("inventario.csv").toString()).get(1)[7]);
    }

    @Test
    @DisplayName("Ingresar producto existente deja registro de entrada")
    void ingresarProductoExistenteDejaRegistroDeEntrada() {
        Almacen almacen = almacen();
        almacen.ingresarProductoExistente("P1", 4);
        MotorCSV csv = new MotorCSV(directorio);
        assertEquals(2, csv.leerCSV(csv.rutaDatos("entradas.csv").toString()).size());
    }

    @Test
    @DisplayName("Importar recepciones de CSV devuelve true")
    void importarEntradasDevuelveTrue() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("carga.txt").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-01", "P1", "Producto", "Marca", "Cat", "11", "3"},
                new String[]{"PROV-01", "P2", "Nuevo", "Marca2", "Cat2", "20", "2"}));
        assertTrue(almacen.ingresarEntradaDesdeArchivo(ruta));
    }

    @Test
    @DisplayName("Importar entradas actualiza el stock del producto existente")
    void importarEntradasActualizaStockProductoExistente() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("carga.txt").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-01", "P1", "Producto", "Marca", "Cat", "11", "3"},
                new String[]{"PROV-01", "P2", "Nuevo", "Marca2", "Cat2", "20", "2"}));
        almacen.ingresarEntradaDesdeArchivo(ruta);
        assertEquals("8", csv.leerCSV(csv.rutaDatos("inventario.csv").toString()).get(1)[7]);
    }

    @Test
    @DisplayName("Importar entradas agrega el nuevo producto al inventario")
    void importarEntradasAgregaNuevoProductoAlInventario() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("carga.txt").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-01", "P1", "Producto", "Marca", "Cat", "11", "3"},
                new String[]{"PROV-01", "P2", "Nuevo", "Marca2", "Cat2", "20", "2"}));
        almacen.ingresarEntradaDesdeArchivo(ruta);
        assertEquals("2", csv.leerCSV(csv.rutaDatos("inventario.csv").toString()).get(2)[7]);
    }

    @Test
    @DisplayName("Importar entradas registra tres filas en el CSV de entradas")
    void importarEntradasRegistraTresFilasEnEntradasCSV() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("carga.txt").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-01", "P1", "Producto", "Marca", "Cat", "11", "3"},
                new String[]{"PROV-01", "P2", "Nuevo", "Marca2", "Cat2", "20", "2"}));
        almacen.ingresarEntradaDesdeArchivo(ruta);
        assertEquals(3, csv.leerCSV(csv.rutaDatos("entradas.csv").toString()).size());
    }

    @Test
    @DisplayName("Rechaza una fila con proveedor inexistente")
    void rechazaEntradaConProveedorInexistente() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("invalida.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "cantidad"},
                new String[]{"PROV-X", "P1", "4"}));
        assertFalse(almacen.ingresarEntradaDesdeArchivo(ruta));
    }

    @Test
    @DisplayName("Rechaza una ruta de archivo vacia")
    void rechazaRutaDeArchivoVacia() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarEntradaDesdeArchivo(""));
    }

    @Test
    @DisplayName("Una fila invalida hace que la importacion devuelva false")
    void importacionInvalidaDevuelveFalse() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("carga-parcialmente-invalida.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-01", "P1", "Producto", "Marca", "Cat", "11", "2"},
                new String[]{"PROV-01", "P1", "Producto", "Marca", "Cat", "11", "no-numero"}));
        assertFalse(almacen.ingresarEntradaDesdeArchivo(ruta));
    }

    @Test
    @DisplayName("Una fila invalida no modifica el stock del inventario")
    void importacionInvalidaNoModificaStockEnInventario() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("carga-parcialmente-invalida.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-01", "P1", "Producto", "Marca", "Cat", "11", "2"},
                new String[]{"PROV-01", "P1", "Producto", "Marca", "Cat", "11", "no-numero"}));
        almacen.ingresarEntradaDesdeArchivo(ruta);
        assertEquals("5", csv.leerCSV(csv.rutaDatos("inventario.csv").toString()).get(1)[7]);
    }

    @Test
    @DisplayName("Una fila invalida no deja filas en el CSV de entradas")
    void importacionInvalidaNoDejaFilasEnEntradasCSV() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("carga-parcialmente-invalida.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-01", "P1", "Producto", "Marca", "Cat", "11", "2"},
                new String[]{"PROV-01", "P1", "Producto", "Marca", "Cat", "11", "no-numero"}));
        almacen.ingresarEntradaDesdeArchivo(ruta);
        assertTrue(csv.leerCSV(csv.rutaDatos("entradas.csv").toString()).isEmpty());
    }

    @Test
    @DisplayName("Una entrada con cantidad negativa devuelve false")
    void cantidadNegativaDevuelveFalse() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("cantidad-negativa.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-01", "P1", "Producto", "Marca", "Cat", "11", "-1"}));
        assertFalse(almacen.ingresarEntradaDesdeArchivo(ruta));
    }

    @Test
    @DisplayName("Una entrada con cantidad negativa no modifica el stock del inventario")
    void cantidadNegativaNoModificaStockEnInventario() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("cantidad-negativa.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-01", "P1", "Producto", "Marca", "Cat", "11", "-1"}));
        almacen.ingresarEntradaDesdeArchivo(ruta);
        assertEquals("5", csv.leerCSV(csv.rutaDatos("inventario.csv").toString()).get(1)[7]);
    }

    @Test
    @DisplayName("Una entrada con cantidad negativa no deja filas en el CSV de entradas")
    void cantidadNegativaNoDejaFilasEnEntradasCSV() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("cantidad-negativa.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "nombreProducto", "marca", "categoria", "precio", "cantidad"},
                new String[]{"PROV-01", "P1", "Producto", "Marca", "Cat", "11", "-1"}));
        almacen.ingresarEntradaDesdeArchivo(ruta);
        assertTrue(csv.leerCSV(csv.rutaDatos("entradas.csv").toString()).isEmpty());
    }

    @Test
    @DisplayName("Importar con columnas reordenadas devuelve true")
    void importarConColumnasReordenadasDevuelveTrue() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("columnas-reordenadas.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"cantidad", "categoria", "idProducto", "idProveedor", "precio", "marca", "nombreProducto"},
                new String[]{"2", "Cat", "P1", "PROV-01", "12", "Marca", "Producto"}));
        assertTrue(almacen.ingresarEntradaDesdeArchivo(ruta));
    }

    @Test
    @DisplayName("Importar con columnas reordenadas actualiza el stock del inventario")
    void importarConColumnasReordenadasActualizaStock() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("columnas-reordenadas.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"cantidad", "categoria", "idProducto", "idProveedor", "precio", "marca", "nombreProducto"},
                new String[]{"2", "Cat", "P1", "PROV-01", "12", "Marca", "Producto"}));
        almacen.ingresarEntradaDesdeArchivo(ruta);
        assertEquals("7", csv.leerCSV(csv.rutaDatos("inventario.csv").toString()).get(1)[7]);
    }

    @Test
    @DisplayName("Importar con columnas reordenadas guarda la cantidad en entradas")
    void importarConColumnasReordenadasGuardaCantidadEnEntradas() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("columnas-reordenadas.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"cantidad", "categoria", "idProducto", "idProveedor", "precio", "marca", "nombreProducto"},
                new String[]{"2", "Cat", "P1", "PROV-01", "12", "Marca", "Producto"}));
        almacen.ingresarEntradaDesdeArchivo(ruta);
        assertEquals("2", csv.leerCSV(csv.rutaDatos("entradas.csv").toString()).get(1)[4]);
    }

    @Test
    @DisplayName("Una fila con columnas insuficientes devuelve false")
    void filaIncompletaDevuelveFalse() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("fila-incompleta.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "cantidad"},
                new String[]{"PROV-01", "P1"}));
        assertFalse(almacen.ingresarEntradaDesdeArchivo(ruta));
    }

    @Test
    @DisplayName("Una fila incompleta no modifica el stock del inventario")
    void filaIncompletaNoModificaStockEnInventario() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("fila-incompleta.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "cantidad"},
                new String[]{"PROV-01", "P1"}));
        almacen.ingresarEntradaDesdeArchivo(ruta);
        assertEquals("5", csv.leerCSV(csv.rutaDatos("inventario.csv").toString()).get(1)[7]);
    }

    @Test
    @DisplayName("Una fila incompleta no deja filas en el CSV de entradas")
    void filaIncompletaNoDejaFilasEnEntradasCSV() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        String ruta = directorio.resolve("fila-incompleta.csv").toString();
        csv.escribirCSV(ruta, List.of(
                new String[]{"idProveedor", "idProducto", "cantidad"},
                new String[]{"PROV-01", "P1"}));
        almacen.ingresarEntradaDesdeArchivo(ruta);
        assertTrue(csv.leerCSV(csv.rutaDatos("entradas.csv").toString()).isEmpty());
    }

    @Test
    @DisplayName("Registrar producto nuevo con stock negativo devuelve false")
    public void ingresarProductoNuevoConStockNegativoDevuelveFalse() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarProductoNuevo(new Producto("P3", "Producto", 10, 15, "desc", -1, "Marca", "Cat")));
    }

    @Test
    @DisplayName("Registrar producto nuevo con precio de entrada negativo devuelve false")
    public void ingresarProductoNuevoConPrecioEntradaNegativoDevuelveFalse() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarProductoNuevo(new Producto("P3", "Producto", -10, 15, "desc", 1, "Marca", "Cat")));
    }

    @Test
    @DisplayName("Registrar producto nuevo con precio de venta negativo devuelve false")
    public void ingresarProductoNuevoConPrecioVentaNegativoDevuelveFalse() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarProductoNuevo(new Producto("P3", "Producto", 10, -15, "desc", 1, "Marca", "Cat")));
    }

    @Test
    @DisplayName("Rechaza un producto nuevo con nombre simbolico")
    public void rechazaProductoNuevoConNombreSimbolico() {
        Almacen almacen = almacen();
        Producto producto = new Producto("P3", NOMBRE_INVALIDO, 10, 15, "desc", 5, "Marca", "Cat");
        assertFalse(almacen.ingresarProductoNuevo(producto));
    }

    @Test
    @DisplayName("Rechaza un producto nuevo con marca vacia")
    public void rechazaProductoNuevoConMarcaVacia() {
        Almacen almacen = almacen();
        Producto producto = new Producto("P3", "Producto Válido", 10, 15, "desc", 5, "", "Cat");
        assertFalse(almacen.ingresarProductoNuevo(producto));
    }

    @Test
    @DisplayName("Rechaza un producto nuevo con categoria vacia")
    public void rechazaProductoNuevoConCategoriaVacia() {
        Almacen almacen = almacen();
        Producto producto = new Producto("P3", "Producto Válido", 10, 15, "desc", 5, "Marca", "");
        assertFalse(almacen.ingresarProductoNuevo(producto));
    }

    @Test
    @DisplayName("Rechaza un producto nuevo con descripcion vacia")
    public void rechazaProductoNuevoConDescripcionVacia() {
        Almacen almacen = almacen();
        Producto producto = new Producto("P3", "Producto Válido", 10, 15, "", 5, "Marca", "Cat");
        assertFalse(almacen.ingresarProductoNuevo(producto));
    }

    @Test
    @DisplayName("Rechaza un producto nuevo con precio de venta menor al de compra")
    public void rechazaProductoNuevoConPrecioVentaMenorAlCosto() {
        Almacen almacen = almacen();
        Producto producto = new Producto("P3", "Producto Válido", 100, 90, "desc", 5, "Marca", "Cat");
        assertFalse(almacen.ingresarProductoNuevo(producto));
    }

    @Test
    @DisplayName("Rechaza una entrada que desborda el stock maximo")
    public void rechazaEntradaQueDesbordaElStock() {
        Almacen almacen = almacen();
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("inventario.csv").toString(), List.of(
                new String[]{"idProducto", "nombre", "marca", "categoria", "descripcion", "precioEntrada", "precioVenta", "stock"},
                new String[]{"P1", "Producto", "Marca", "Cat", "desc", "10", "15", String.valueOf(STOCK_MAXIMO)}));
        assertFalse(almacen.ingresarProductoExistente("P1", 1));
    }

    @Test
    @DisplayName("Rechaza una importacion con precio de doble signo")
    public void rechazaImportacionConPrecioDobleSigno() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarEntradaDesdeArchivo(archivoImportacion(PRECIO_CON_DOBLE_SIGNO, "1")));
    }

    @Test
    @DisplayName("Rechaza una importacion con precio no numerico")
    public void rechazaImportacionConPrecioNoNumerico() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarEntradaDesdeArchivo(archivoImportacion(PRECIO_NO_NUMERICO, "1")));
    }

    @Test
    @DisplayName("Rechaza una importacion con precio NaN")
    public void rechazaImportacionConPrecioNaN() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarEntradaDesdeArchivo(archivoImportacion(PRECIO_NAN, "1")));
    }

    @Test
    @DisplayName("Rechaza una importacion con precio infinito")
    public void rechazaImportacionConPrecioInfinito() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarEntradaDesdeArchivo(archivoImportacion(PRECIO_INFINITO, "1")));
    }

    @Test
    @DisplayName("Rechaza una importacion con cantidad con letras")
    public void rechazaImportacionConCantidadConLetras() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarEntradaDesdeArchivo(archivoImportacion("11", CANTIDAD_CON_LETRAS)));
    }

    @Test
    @DisplayName("Rechaza una importacion con nombre de producto vacio")
    public void rechazaImportacionConNombreProductoVacio() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarEntradaDesdeArchivo(
                archivoImportacionProductoNuevo("", "Marca", "Cat")));
    }

    @Test
    @DisplayName("Rechaza una importacion con marca vacia")
    public void rechazaImportacionConMarcaVacia() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarEntradaDesdeArchivo(
                archivoImportacionProductoNuevo("Producto Nuevo", "", "Cat")));
    }

    @Test
    @DisplayName("Rechaza una importacion con categoria vacia")
    public void rechazaImportacionConCategoriaVacia() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarEntradaDesdeArchivo(
                archivoImportacionProductoNuevo("Producto Nuevo", "Marca", "")));
    }

    @Test
    @DisplayName("Registra la entrada de un producto existente con proveedor no vacio")
    public void registraEntradaConProveedorNoVacio() {
        Almacen almacen = almacen();
        almacen.ingresarProductoExistente("P1", 4);
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> entradas = csv.leerCSV(csv.rutaDatos("entradas.csv").toString());
        assertFalse(entradas.get(1)[1].isBlank());
    }

    @Test
    @DisplayName("Registra la entrada de un producto nuevo con proveedor no vacio")
    public void registraEntradaDeProductoNuevoConProveedorNoVacio() {
        Almacen almacen = almacen();
        almacen.ingresarProductoNuevo(producto("P3"));
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> entradas = csv.leerCSV(csv.rutaDatos("entradas.csv").toString());
        assertFalse(entradas.get(1)[1].isBlank());
    }

    @Test
    @DisplayName("Un producto nuevo importado tiene descripcion no vacia")
    public void productoNuevoImportadoTieneDescripcionNoVacia() {
        Almacen almacen = almacen();
        almacen.ingresarEntradaDesdeArchivo(
                archivoImportacionProductoNuevo("Producto Nuevo", "Marca", "Cat"));
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> inventario = csv.leerCSV(csv.rutaDatos("inventario.csv").toString());
        String[] producto = inventario.stream()
                .filter(fila -> fila[0].equals("P2")).findFirst().orElseThrow();
        assertFalse(producto[4].isBlank());
    }

    @Test
    @DisplayName("Un producto nuevo importado tiene precio de venta con dos decimales")
    public void productoNuevoImportadoTienePrecioVentaConDosDecimales() {
        Almacen almacen = almacen();
        almacen.ingresarEntradaDesdeArchivo(
                archivoImportacionProductoNuevo("Producto Nuevo", "Marca", "Cat"));
        MotorCSV csv = new MotorCSV(directorio);
        List<String[]> inventario = csv.leerCSV(csv.rutaDatos("inventario.csv").toString());
        String[] producto = inventario.stream()
                .filter(fila -> fila[0].equals("P2")).findFirst().orElseThrow();
        assertEquals("13.00", producto[6]);
    }

    @Test
    @DisplayName("Rechaza un producto nuevo con precio de compra cero")
    public void rechazaProductoNuevoConPrecioCompraCero() {
        Almacen almacen = almacen();
        Producto producto = new Producto("P3", "Producto Válido", 0, 15, "desc", 5, "Marca", "Cat");
        assertFalse(almacen.ingresarProductoNuevo(producto));
    }

    @Test
    @DisplayName("Rechaza un producto nuevo con precio de venta cero")
    public void rechazaProductoNuevoConPrecioVentaCero() {
        Almacen almacen = almacen();
        Producto producto = new Producto("P3", "Producto Válido", 10, 0, "desc", 5, "Marca", "Cat");
        assertFalse(almacen.ingresarProductoNuevo(producto));
    }

    @Test
    @DisplayName("Rechaza una importacion con precio cero")
    public void rechazaImportacionConPrecioCero() {
        Almacen almacen = almacen();
        assertFalse(almacen.ingresarEntradaDesdeArchivo(archivoImportacion("0", "1")));
    }

    @Test
    @DisplayName("Rechaza un producto nuevo con precio de venta igual al de compra")
    public void rechazaProductoNuevoConPrecioVentaIgualAlCosto() {
        Almacen almacen = almacen();
        Producto producto = new Producto("P3", "Producto Válido", 10, 10, "desc", 5, "Marca", "Cat");
        assertFalse(almacen.ingresarProductoNuevo(producto));
    }
}