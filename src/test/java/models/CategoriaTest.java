package models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Categoria")
class CategoriaTest {

    @Test
    @DisplayName("desde reconoce el nombre visible exacto")
    void desdeReconoceNombreVisibleExacto() {
        assertEquals(Optional.of(Categoria.AUDIO), Categoria.desde("Audio"));
    }

    @Test
    @DisplayName("desde ignora mayusculas")
    void desdeIgnoraMayusculas() {
        assertEquals(Optional.of(Categoria.AUDIO), Categoria.desde("AUDIO"));
    }

    @Test
    @DisplayName("desde ignora tildes")
    void desdeIgnoraTildes() {
        assertEquals(Optional.of(Categoria.CAMARAS), Categoria.desde("Camaras"));
    }

    @Test
    @DisplayName("desde reconoce el nombre del enum")
    void desdeReconoceNombreEnum() {
        assertEquals(Optional.of(Categoria.PERIFERICOS), Categoria.desde("PERIFERICOS"));
    }

    @Test
    @DisplayName("desde devuelve vacio para categoria desconocida")
    void desdeDesconocidoVacio() {
        assertTrue(Categoria.desde("Jardineria").isEmpty());
    }

    @Test
    @DisplayName("desde devuelve vacio para null")
    void desdeNullVacio() {
        assertTrue(Categoria.desde(null).isEmpty());
    }

    @Test
    @DisplayName("toString devuelve el nombre visible")
    void toStringDevuelveNombreVisible() {
        assertEquals("Cámaras", Categoria.CAMARAS.toString());
    }

    @Test
    @DisplayName("normalizar ignora tildes y mayusculas")
    void normalizarIgnoraTildesYMayusculas() {
        assertEquals("camaras", Categoria.normalizar("Cámaras"));
    }

    @Test
    @DisplayName("normalizar devuelve cadena vacia para null")
    void normalizarNullDevuelveVacio() {
        assertEquals("", Categoria.normalizar(null));
    }

    @Test
    @DisplayName("normalizar recorta espacios extremos")
    void normalizarRecortaEspacios() {
        assertEquals("audio", Categoria.normalizar("  Audio  "));
    }

    @Test
    @DisplayName("values contiene las 15 categorias del inventario")
    void valuesContieneLas15Categorias() {
        assertEquals(15, Categoria.values().length);
    }
}
