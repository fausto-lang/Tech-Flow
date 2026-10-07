package services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MotorCSV")
class MotorCSVTest {

    @TempDir
    Path tempDir;

    private final MotorCSV motor = new MotorCSV();

    /** Construye una List<String[]> tipada (evita el footgun de varargs de List.of con arrays). */
    private List<String[]> filas(String[]... arreglos) {
        return new ArrayList<>(Arrays.asList(arreglos));
    }

    @Test
    @DisplayName("existeArchivo es false si el archivo no existe")
    void existeArchivoFalsoSiNoExiste() {
        assertFalse(motor.existeArchivo(tempDir.resolve("nope.csv").toString()));
    }

    @Test
    @DisplayName("existeArchivo es true tras escribir")
    void existeArchivoVerdaderoTrasEscribir() {
        String ruta = tempDir.resolve("e.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"1"}));
        assertTrue(motor.existeArchivo(ruta));
    }

    @Test
    @DisplayName("inicializarCSV escribe la cabecera")
    void inicializarCSVEscribeEncabezado() {
        String ruta = tempDir.resolve("inventario.csv").toString();
        motor.inicializarCSV(ruta, new String[]{"id", "nombre", "stock"});
        assertArrayEquals(new String[]{"id", "nombre", "stock"}, motor.leerCSV(ruta).get(0));
    }

    @Test
    @DisplayName("Escribe y lee respetando campos entrecomillados")
    void escribirYLeerConComillas() {
        String ruta = tempDir.resolve("productos.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"id", "descripcion"}, new String[]{"P1", "Con coma, y \"comillas\""}));
        assertEquals("Con coma, y \"comillas\"", motor.leerCSV(ruta).get(1)[1]);
    }

    @Test
    @DisplayName("escribirCSV sobrescribe el contenido anterior")
    void escribirSobrescribe() {
        String ruta = tempDir.resolve("s.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"1"}));
        motor.escribirCSV(ruta, filas(new String[]{"2"}));
        assertEquals(1, motor.leerCSV(ruta).size());
    }

    @Test
    @DisplayName("escribirCSV con append agrega sin borrar")
    void escribirConAppendAgrega() {
        String ruta = tempDir.resolve("ap.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"1"}));
        motor.escribirCSV(ruta, filas(new String[]{"2"}), true);
        assertEquals(2, motor.leerCSV(ruta).size());
    }

    @Test
    @DisplayName("agregarFila anexa al final del archivo")
    void agregarFilaAnexa() {
        String ruta = tempDir.resolve("log.csv").toString();
        motor.inicializarCSV(ruta, new String[]{"a", "b"});
        motor.agregarFila(ruta, new String[]{"1", "2"});
        assertEquals(2, motor.leerCSV(ruta).size());
    }

    @Test
    @DisplayName("Lee con un delimitador personalizado")
    void leerConDelimitadorPersonalizado() throws Exception {
        Path ruta = tempDir.resolve("punto.csv");
        Files.writeString(ruta, "a;b;c\n1;2;3\n");
        assertArrayEquals(new String[]{"1", "2", "3"}, motor.leerCSV(ruta.toString(), ";").get(1));
    }

    @Test
    @DisplayName("Los campos vacíos se preservan")
    void camposVaciosSePreservan() {
        String ruta = tempDir.resolve("v.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"a", "", "c"}));
        assertArrayEquals(new String[]{"a", "", "c"}, motor.leerCSV(ruta).get(0));
    }

    @Test
    @DisplayName("Un campo null se escribe vacío")
    void campoNullSeEscribeVacio() {
        String ruta = tempDir.resolve("n.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"a", null}));
        assertEquals("", motor.leerCSV(ruta).get(0)[1]);
    }

    @Test
    @DisplayName("actualizarFila modifica la posición indicada")
    void actualizarFilaModifica() {
        String ruta = tempDir.resolve("u.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"a"}, new String[]{"1"}));
        motor.actualizarFila(ruta, 1, new String[]{"X"});
        assertEquals("X", motor.leerCSV(ruta).get(1)[0]);
    }

    @Test
    @DisplayName("actualizarFila fuera de rango devuelve false")
    void actualizarFilaFueraDeRango() {
        String ruta = tempDir.resolve("u2.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"a"}));
        assertFalse(motor.actualizarFila(ruta, 99, new String[]{"Y"}));
    }

    @Test
    @DisplayName("eliminarFila borra por índice")
    void eliminarFilaPorIndice() {
        String ruta = tempDir.resolve("d.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"a"}, new String[]{"1"}, new String[]{"2"}));
        motor.eliminarFila(ruta, 1);
        assertEquals("2", motor.leerCSV(ruta).get(1)[0]);
    }

    @Test
    @DisplayName("eliminarFila fuera de rango devuelve false")
    void eliminarFilaFueraDeRango() {
        String ruta = tempDir.resolve("d2.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"a"}));
        assertFalse(motor.eliminarFila(ruta, 5));
    }

    @Test
    @DisplayName("eliminarFilaPorClave borra las filas que coinciden")
    void eliminarFilaPorClave() {
        String ruta = tempDir.resolve("k.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"PROV-01", "A"}, new String[]{"PROV-02", "B"}));
        motor.eliminarFilaPorClave(ruta, 0, "PROV-01");
        assertEquals("PROV-02", motor.leerCSV(ruta).get(0)[0]);
    }

    @Test
    @DisplayName("eliminarFilaPorClave inexistente devuelve false")
    void eliminarFilaPorClaveInexistente() {
        String ruta = tempDir.resolve("k2.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"PROV-01", "A"}));
        assertFalse(motor.eliminarFilaPorClave(ruta, 0, "NO-EXISTE"));
    }

    @Test
    @DisplayName("leerCSV de un archivo inexistente devuelve lista vacía")
    void leerCSVInexistente() {
        assertTrue(motor.leerCSV(tempDir.resolve("nada.csv").toString()).isEmpty());
    }

    @Test
    @DisplayName("Lee un fixture del classpath de test")
    void leeFixtureDelClasspath() throws Exception {
        Path ruta = Paths.get(getClass().getClassLoader().getResource("ventas.csv").toURI());
        List<String[]> filas = motor.leerCSV(ruta.toString());
        assertEquals(3, filas.size()); // cabecera + 2 filas
    }

    @Test
    @DisplayName("El fixture de ventas trae la cabecera esperada")
    void fixtureCabecera() throws Exception {
        Path ruta = Paths.get(getClass().getClassLoader().getResource("ventas.csv").toURI());
        assertEquals("idVenta", motor.leerCSV(ruta.toString()).get(0)[0]);
    }

    @Test
    @DisplayName("Un archivo con solo líneas en blanco se lee vacío")
    void archivoSoloBlanco() throws Exception {
        Path ruta = tempDir.resolve("blanco.csv");
        Files.writeString(ruta, "\n\n\n");
        assertTrue(motor.leerCSV(ruta.toString()).isEmpty());
    }

    @Test
    @DisplayName("Un campo con salto de línea se conserva entre comillas")
    void campoConSaltoDeLinea() {
        String ruta = tempDir.resolve("nl.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"a\nb", "c"}));
        assertEquals("a\nb", motor.leerCSV(ruta).get(0)[0]);
    }

    @Test
    @DisplayName("Un campo que combina coma, comillas y salto de línea")
    void campoComplejo() {
        String ruta = tempDir.resolve("cx.csv").toString();
        String valor = "x, \"y\"\nz";
        motor.escribirCSV(ruta, filas(new String[]{valor}));
        assertEquals(valor, motor.leerCSV(ruta).get(0)[0]);
    }

    @Test
    @DisplayName("Campos vacíos al inicio y al final se preservan")
    void camposBordeVacios() {
        String ruta = tempDir.resolve("borde.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"", "x", ""}));
        assertArrayEquals(new String[]{"", "x", ""}, motor.leerCSV(ruta).get(0));
    }

    @Test
    @DisplayName("Una fila de una sola columna se lee bien")
    void filaUnaColumna() {
        String ruta = tempDir.resolve("uno.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"solo"}));
        assertEquals(1, motor.leerCSV(ruta).get(0).length);
    }

    @Test
    @DisplayName("Escribir una lista vacía deja el archivo sin filas")
    void escribirListaVacia() {
        String ruta = tempDir.resolve("vacio.csv").toString();
        motor.escribirCSV(ruta, new ArrayList<>());
        assertTrue(motor.leerCSV(ruta).isEmpty());
    }

    @Test
    @DisplayName("Los acentos y unicode se conservan (UTF-8)")
    void unicodeSeConserva() {
        String ruta = tempDir.resolve("u.csv").toString();
        motor.escribirCSV(ruta, filas(new String[]{"ñandú", "日本語"}));
        assertArrayEquals(new String[]{"ñandú", "日本語"}, motor.leerCSV(ruta).get(0));
    }

    @Test
    @DisplayName("Lee correctamente archivos con finales de línea CRLF")
    void leeConCrlf() throws Exception {
        Path ruta = tempDir.resolve("crlf.csv");
        Files.writeString(ruta, "a,b\r\n1,2\r\n");
        assertArrayEquals(new String[]{"1", "2"}, motor.leerCSV(ruta.toString()).get(1));
    }

    @Test
    @DisplayName("Una fila con solo espacios se preserva (no se considera vacía)")
    void filaSoloEspaciosSePreserva() throws Exception {
        Path ruta = tempDir.resolve("esp.csv");
        Files.writeString(ruta, " \n");
        assertEquals(" ", motor.leerCSV(ruta.toString()).get(0)[0]);
    }

    @Test
    @DisplayName("No importa un origen inexistente, vacio o con solo cabecera")
    void importarDatosRechazaOrigenSinDatos() {
        String destino = tempDir.resolve("destino.csv").toString();
        assertFalse(motor.importarDatos(null, destino));
        assertFalse(motor.importarDatos("", destino));
        assertFalse(motor.importarDatos(tempDir.resolve("no-existe.csv").toString(), destino));

        String soloCabecera = tempDir.resolve("solo-cabecera.csv").toString();
        motor.inicializarCSV(soloCabecera, new String[]{"id", "nombre"});
        assertFalse(motor.importarDatos(soloCabecera, destino));
    }

    @Test
    @DisplayName("Importar datos conserva la cabecera existente del destino")
    void importarDatosConservaCabeceraDestino() {
        String origen = tempDir.resolve("origen.csv").toString();
        String destino = tempDir.resolve("destino.csv").toString();
        motor.escribirCSV(origen, filas(new String[]{"id", "nombre"}, new String[]{"1", "Origen"}));
        motor.escribirCSV(destino, filas(new String[]{"codigo", "detalle"}, new String[]{"D-1", "Existente"}));

        assertTrue(motor.importarDatos(origen, destino));
        assertArrayEquals(new String[]{"codigo", "detalle"}, motor.leerCSV(destino).get(0));
        assertEquals("1", motor.leerCSV(destino).get(2)[0]);
    }

    @Test
    @DisplayName("Una ruta relativa no puede escapar del directorio de datos")
    void rutaRelativaNoEscapaDelDirectorio() {
        MotorCSV motorLocal = new MotorCSV(tempDir);

        Path ruta = motorLocal.rutaDatos("../fuera.csv");

        assertTrue(ruta.startsWith(tempDir.toAbsolutePath().normalize()));
    }

    @Test
    @DisplayName("Las operaciones con rutas nulas no lanzan excepciones")
    void rutasNulasNoLanzanExcepciones() {
        assertDoesNotThrow(() -> motor.leerCSV(null));
        assertDoesNotThrow(() -> motor.escribirCSV(null, filas(new String[]{"dato"})));
        assertDoesNotThrow(() -> motor.existeArchivo(null));
    }
}
