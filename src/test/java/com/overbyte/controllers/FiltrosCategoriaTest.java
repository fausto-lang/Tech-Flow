package com.overbyte.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FiltrosCategoria")
class FiltrosCategoriaTest {

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
}
