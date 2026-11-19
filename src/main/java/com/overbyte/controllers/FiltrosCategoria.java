package com.overbyte.controllers;

import javafx.scene.control.ComboBox;

import models.Categoria;

/** Utilidades compartidas para los filtros por categoría de las tres pantallas. */
final class FiltrosCategoria {

    static final String TODAS = "Todas";

    private FiltrosCategoria() {}

    /** Crea un ComboBox con "Todas" + todas las categorías conocidas. */
    static ComboBox<String> crear() {
        ComboBox<String> combo = new ComboBox<>();
        configurar(combo);
        return combo;
    }

    /** Rellena un ComboBox ya creado (por ejemplo, uno inyectado vía FXML). */
    static void configurar(ComboBox<String> combo) {
        combo.getStyleClass().setAll("campo");
        combo.getItems().add(TODAS);
        for (Categoria categoria : Categoria.values()) {
            combo.getItems().add(categoria.toString());
        }
        combo.setValue(TODAS);
        combo.setPrefWidth(200);
        combo.setMaxWidth(Double.MAX_VALUE);
    }

    /** Indica si una categoría del CSV pasa el filtro seleccionado (sin distinguir tildes ni mayúsculas). */
    static boolean coincide(String categoriaCsv, String seleccion) {
        if (seleccion == null || seleccion.isBlank() || TODAS.equals(seleccion)) {
            return true;
        }
        return Categoria.normalizar(categoriaCsv).equals(Categoria.normalizar(seleccion));
    }
}
