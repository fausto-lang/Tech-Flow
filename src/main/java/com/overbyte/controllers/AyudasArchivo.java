package com.overbyte.controllers;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** Utilidades compartidas para guardar archivos de texto desde la interfaz. */
final class AyudasArchivo {

    private AyudasArchivo() {}

    /**
     * Pide al usuario dónde guardar un .txt. Devuelve la ruta con la extensión garantizada,
     * o {@code null} si el usuario cancela.
     */
    static Path elegirRutaTxt(Window ventana, String titulo, String nombreInicial) {
        FileChooser selector = new FileChooser();
        selector.setTitle(titulo);
        selector.setInitialFileName(nombreInicial);
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo de texto (*.txt)", "*.txt"));
        java.io.File archivo = selector.showSaveDialog(ventana);
        if (archivo == null) {
            return null;
        }
        Path ruta = archivo.toPath();
        if (!ruta.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".txt")) {
            ruta = ruta.resolveSibling(ruta.getFileName() + ".txt");
        }
        return ruta;
    }

    /** Escribe el contenido en UTF-8. Devuelve {@code true} si tuvo éxito; ante error lo registra y devuelve {@code false}. */
    static boolean escribirTxt(Path ruta, String contenido) {
        try {
            Files.writeString(ruta, contenido, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            System.err.println("No se pudo guardar " + ruta + ": " + e.getMessage());
            return false;
        }
    }
}
