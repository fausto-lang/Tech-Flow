package com.overbyte.controllers;

import java.util.Locale;

/**
 * Genera las tablas de texto plano que se guardan en los informes .txt:
 * bordes ASCII, filas rellenadas al ancho de cada columna y recorte con «…».
 */
final class InformeTexto {

    private InformeTexto() {}

    static String borde(int[] anchos) {
        StringBuilder borde = new StringBuilder("+");
        for (int ancho : anchos) {
            borde.append("-".repeat(ancho + 2)).append('+');
        }
        return borde.toString();
    }

    static String fila(String[] columnas, int[] anchos) {
        StringBuilder fila = new StringBuilder("|");
        for (int i = 0; i < anchos.length; i++) {
            String valor = i < columnas.length ? columnas[i] : "";
            fila.append(' ').append(ajustar(valor, anchos[i])).append(" |");
        }
        return fila.toString();
    }

    static String ajustar(String texto, int ancho) {
        if (ancho < 1) {
            return "";
        }
        String limpio = String.valueOf(texto)
                .replace('\n', ' ').replace('\r', ' ').replace('|', '/').trim();
        if (limpio.length() > ancho) {
            return ancho == 1 ? "…" : limpio.substring(0, ancho - 1) + "…";
        }
        return String.format(Locale.ROOT, "%-" + ancho + "s", limpio);
    }
}
