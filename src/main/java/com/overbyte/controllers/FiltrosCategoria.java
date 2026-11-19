package com.overbyte.controllers;

import javafx.scene.control.ComboBox;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import config.ArchivosCSV;
import services.MotorCSV;

/**
 * Filtro por categoría compartido entre las pantallas. La lista se deriva
 * de los valores presentes en el inventario, sin lista fija ni enum: si el
 * CSV trae una categoría nueva, aparece sola en el filtro.
 */
final class FiltrosCategoria {

    static final String TODAS = "Todas";

    private FiltrosCategoria() {}

    /** ComboBox con "Todas" + las categorías presentes en el inventario. */
    static ComboBox<String> crear() {
        ComboBox<String> combo = new ComboBox<>();
        configurar(combo, true);
        return combo;
    }

    /** ComboBox editable con las categorías existentes, para el alta de productos. */
    static ComboBox<String> crearAlta() {
        ComboBox<String> combo = new ComboBox<>();
        configurar(combo, false);
        combo.setEditable(true);
        combo.setPromptText("Selecciona o escribe una categoría");
        return combo;
    }

    /** Rellena un ComboBox ya creado (por ejemplo, uno inyectado vía FXML) con "Todas" + categorías. */
    static void configurar(ComboBox<String> combo) {
        configurar(combo, true);
    }

    private static void configurar(ComboBox<String> combo, boolean conTodas) {
        combo.getStyleClass().setAll("campo");
        combo.getItems().setAll(categoriasEnDisco());
        if (conTodas) {
            combo.getItems().add(0, TODAS);
            combo.setValue(TODAS);
        }
        combo.setPrefWidth(200);
        combo.setMaxWidth(Double.MAX_VALUE);
    }

    /** Lee las categorías distintas del inventario actual, ignorando tildes y mayúsculas al deduplicar. */
    static List<String> categoriasEnDisco() {
        MotorCSV motor = new MotorCSV();
        List<String[]> filas = motor.leerCSV(motor.rutaDatos(ArchivosCSV.INVENTARIO).toString());
        return categoriasDe(filas);
    }

    /** Extrae las categorías distintas (ordenadas) de unas filas de inventario. */
    static List<String> categoriasDe(List<String[]> filas) {
        Map<String, String> porClave = new TreeMap<>();
        if (filas != null && !filas.isEmpty()) {
            int columna = columnaCategoria(filas.get(0));
            for (int i = 1; i < filas.size(); i++) {
                String[] fila = filas.get(i);
                if (fila.length <= columna) {
                    continue;
                }
                String valor = fila[columna] == null ? "" : fila[columna].trim();
                if (valor.isEmpty()) {
                    continue;
                }
                porClave.putIfAbsent(normalizar(valor), valor);
            }
        }
        return new ArrayList<>(porClave.values());
    }

    /** Localiza la columna de categoría en el encabezado; por defecto, la 4ª (índice 3) del esquema canónico. */
    private static int columnaCategoria(String[] encabezado) {
        for (int i = 0; i < encabezado.length; i++) {
            if (encabezado[i].trim().equalsIgnoreCase("categoria")) {
                return i;
            }
        }
        return 3;
    }

    /** Indica si una categoría del CSV pasa el filtro seleccionado (sin distinguir tildes ni mayúsculas). */
    static boolean coincide(String categoriaCsv, String seleccion) {
        if (seleccion == null || seleccion.isBlank() || TODAS.equals(seleccion)) {
            return true;
        }
        return normalizar(categoriaCsv).equals(normalizar(seleccion));
    }

    /** Normaliza texto para comparaciones: minúsculas, sin marcas diacríticas y sin espacios extremos. */
    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }
}
