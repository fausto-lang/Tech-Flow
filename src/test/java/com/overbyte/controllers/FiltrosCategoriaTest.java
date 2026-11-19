package com.overbyte.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FiltrosCategoria")
class FiltrosCategoriaTest {

    private static final String[][] FILAS_INVENTARIO = {
            {"idProducto", "nombre", "marca", "categoria", "descripcion", "precioEntrada", "precioVenta", "stock"},
            {"P1", "Auriculares", "Sony", "Audio", "desc", "10", "15", "5"},
            {"P2", "Cámara", "Canon", "Cámaras", "desc", "10", "15", "5"},
            {"P3", "Tablet", "Apple", "Camaras", "desc", "10", "15", "5"},
            {"P4", "Mouse", "Logitech", "Periféricos", "desc", "10", "15", "5"},
            {"P5", "Cable", "Anker", "audio", "desc", "10", "15", "5"},
    };

    private List<String[]> inventario() {
        List<String[]> filas = new ArrayList<>();
        for (String[] fila : FILAS_INVENTARIO) {
            filas.add(fila.clone());
        }
        return filas;
    }

    @Test
    @DisplayName("coincide devuelve true cuando la seleccion es Todas")
    void coincideTodasDevuelveTrue() {
        assertTrue(FiltrosCategoria.coincide("Audio", FiltrosCategoria.TODAS));
    }

    @Test
    @DisplayName("coincide ignora tildes y mayusculas al comparar")
    void coincideIgnoraTildesYMayusculas() {
        assertTrue(FiltrosCategoria.coincide("Cámaras", "camaras"));
    }

    @Test
    @DisplayName("coincide devuelve false para categorias distintas")
    void coincideCategoriaDistintaDevuelveFalse() {
        assertFalse(FiltrosCategoria.coincide("Audio", "Celulares"));
    }

    @Test
    @DisplayName("coincide devuelve true cuando la seleccion es nula")
    void coincideSeleccionNulaDevuelveTrue() {
        assertTrue(FiltrosCategoria.coincide("Audio", null));
    }

    @Test
    @DisplayName("normalizar ignora tildes y mayusculas")
    void normalizarIgnoraTildesYMayusculas() {
        assertEquals("camaras", FiltrosCategoria.normalizar("Cámaras"));
    }

    @Test
    @DisplayName("normalizar devuelve cadena vacia para null")
    void normalizarNullDevuelveVacio() {
        assertEquals("", FiltrosCategoria.normalizar(null));
    }

    @Test
    @DisplayName("normalizar recorta espacios extremos")
    void normalizarRecortaEspacios() {
        assertEquals("audio", FiltrosCategoria.normalizar("  Audio  "));
    }

    @Test
    @DisplayName("categoriasDe extrae los valores distintos sin repetir variantes")
    void categoriasDeExtraeDistintosSinVariantes() {
        assertEquals(3, FiltrosCategoria.categoriasDe(inventario()).size());
    }

    @Test
    @DisplayName("categoriasDe ordena alfabeticamente")
    void categoriasDeOrdenaAlfabeticamente() {
        assertEquals(List.of("Audio", "Cámaras", "Periféricos"), FiltrosCategoria.categoriasDe(inventario()));
    }

    @Test
    @DisplayName("categoriasDe devuelve lista vacia para un archivo solo con encabezado")
    void categoriasDeSoloEncabezadoDevuelveVacio() {
        List<String[]> filas = new ArrayList<>();
        filas.add(FILAS_INVENTARIO[0].clone());
        assertTrue(FiltrosCategoria.categoriasDe(filas).isEmpty());
    }

    @Test
    @DisplayName("categoriasDe tolera filas mas cortas que el encabezado")
    void categoriasDeToleraFilasCortas() {
        List<String[]> filas = new ArrayList<>();
        filas.add(FILAS_INVENTARIO[0].clone());
        filas.add(new String[]{"P9", "SoloNombre"});
        filas.add(FILAS_INVENTARIO[1].clone());
        assertEquals(1, FiltrosCategoria.categoriasDe(filas).size());
    }
}
