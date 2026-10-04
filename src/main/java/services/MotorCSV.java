package services;

import config.ConfiguracionCSV;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de la manipulación y persistencia de archivos en formato CSV.
 * Implementación mínima funcional: lectura/escritura con soporte para campos
 * entrecomillados y delimitador configurable.
 */
public class MotorCSV {

    /**
     * Lee un archivo CSV usando el delimitador por defecto (coma).
     *
     * @param rutaArchivo Ruta del archivo CSV a leer.
     * @return Lista de arreglos de cadenas con los datos leídos.
     */
    public List<String[]> leerCSV(String rutaArchivo) {
        return leerCSV(rutaArchivo, ConfiguracionCSV.DELIMITADOR_POR_DEFECTO);
    }

    /**
     * Lee un archivo CSV utilizando un delimitador específico.
     *
     * @param rutaArchivo Ruta del archivo CSV.
     * @param delimitador Separador de columnas utilizado en el archivo.
     * @return Lista con el contenido leído por filas.
     */
    public List<String[]> leerCSV(String rutaArchivo, String delimitador) {
        Path ruta = Paths.get(rutaArchivo);
        if (!Files.exists(ruta)) {
            return new ArrayList<>();
        }
        try {
            String contenido = Files.readString(ruta, Charset.forName(ConfiguracionCSV.CODIFICACION));
            return parsearContenido(contenido, delimitador);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Escribe un conjunto de datos en un archivo CSV (sobrescribiendo).
     *
     * @param rutaArchivo Ruta de destino.
     * @param datos       Lista de arreglos con los datos a escribir.
     * @return {@code true} si la escritura fue exitosa.
     */
    public boolean escribirCSV(String rutaArchivo, List<String[]> datos) {
        return escribirCSV(rutaArchivo, datos, false);
    }

    /**
     * Escribe datos en un archivo CSV, especificando si se anexa información al final.
     *
     * @param rutaArchivo Ruta de destino.
     * @param datos       Lista de filas a escribir.
     * @param append      {@code true} para anexar, {@code false} para sobrescribir.
     * @return {@code true} si se completó la operación.
     */
    public boolean escribirCSV(String rutaArchivo, List<String[]> datos, boolean append) {
        try {
            Path ruta = Paths.get(rutaArchivo);
            if (ruta.getParent() != null) {
                Files.createDirectories(ruta.getParent());
            }
            StandardOpenOption[] opciones = append
                    ? new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                    : new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};
            try (BufferedWriter bw = Files.newBufferedWriter(ruta, Charset.forName(ConfiguracionCSV.CODIFICACION), opciones)) {
                for (String[] fila : datos) {
                    bw.write(formatearFila(fila, ConfiguracionCSV.DELIMITADOR_POR_DEFECTO));
                    bw.newLine();
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Anexa una nueva fila de datos al final del archivo CSV.
     *
     * @param rutaArchivo Ruta del archivo.
     * @param fila        Arreglo de valores que conforman la fila.
     * @return {@code true} si se agregó la fila.
     */
    public boolean agregarFila(String rutaArchivo, String[] fila) {
        List<String[]> unaFila = new ArrayList<>();
        unaFila.add(fila);
        return escribirCSV(rutaArchivo, unaFila, true);
    }

    /**
     * Actualiza la información de una fila específica por su número de índice.
     *
     * @param rutaArchivo Ruta del archivo.
     * @param indiceFila  Posición de la fila a actualizar.
     * @param nuevaFila   Nuevos valores para la fila.
     * @return {@code true} si la actualización tuvo éxito.
     */
    public boolean actualizarFila(String rutaArchivo, int indiceFila, String[] nuevaFila) {
        List<String[]> filas = leerCSV(rutaArchivo);
        if (indiceFila < 0 || indiceFila >= filas.size()) {
            return false;
        }
        filas.set(indiceFila, nuevaFila);
        return escribirCSV(rutaArchivo, filas, false);
    }

    /**
     * Elimina una fila del archivo CSV indicando su índice.
     *
     * @param rutaArchivo Ruta del archivo.
     * @param indiceFila  Posición de la fila a eliminar.
     * @return {@code true} si se eliminó con éxito.
     */
    public boolean eliminarFila(String rutaArchivo, int indiceFila) {
        List<String[]> filas = leerCSV(rutaArchivo);
        if (indiceFila < 0 || indiceFila >= filas.size()) {
            return false;
        }
        filas.remove(indiceFila);
        return escribirCSV(rutaArchivo, filas, false);
    }

    /**
     * Elimina una fila buscando un valor coincidente en una columna clave.
     *
     * @param rutaArchivo  Ruta del archivo CSV.
     * @param columnaClave Índice de la columna a evaluar.
     * @param valorClave   Valor buscado para eliminar la fila.
     * @return {@code true} si la fila fue eliminada.
     */
    public boolean eliminarFilaPorClave(String rutaArchivo, int columnaClave, String valorClave) {
        List<String[]> filas = leerCSV(rutaArchivo);
        boolean eliminada = false;
        for (int i = filas.size() - 1; i >= 0; i--) {
            String[] fila = filas.get(i);
            if (columnaClave >= 0 && columnaClave < fila.length
                    && fila[columnaClave].equals(valorClave)) {
                filas.remove(i);
                eliminada = true;
            }
        }
        if (!eliminada) {
            return false;
        }
        return escribirCSV(rutaArchivo, filas, false);
    }

    /**
     * Comprueba si el archivo existe en la ruta especificada.
     *
     * @param rutaArchivo Ruta del archivo.
     * @return {@code true} si el archivo existe.
     */
    public boolean existeArchivo(String rutaArchivo) {
        return Files.exists(Paths.get(rutaArchivo));
    }

    /**
     * Crea un archivo CSV inicializando sus columnas/encabezados.
     *
     * @param rutaArchivo Ruta del archivo.
     * @param encabezados Arreglo con los títulos de las columnas.
     * @return {@code true} si la inicialización fue exitosa.
     */
    public boolean inicializarCSV(String rutaArchivo, String[] encabezados) {
        List<String[]> encabezado = new ArrayList<>();
        encabezado.add(encabezados);
        return escribirCSV(rutaArchivo, encabezado, false);
    }

    // ------------------------------------------------------------------
    // Helpers internos de parseo/formateo
    // ------------------------------------------------------------------

    /**
     * Parsea el contenido completo respetando los campos entrecomillados.
     * Es stateful sobre todo el archivo, por lo que soporta saltos de línea
     * dentro de un campo entrecomillado (a diferencia de un parser por líneas).
     */
    private List<String[]> parsearContenido(String contenido, String delimitador) {
        List<String[]> filas = new ArrayList<>();
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean enComillas = false;
        for (int i = 0; i < contenido.length(); i++) {
            char c = contenido.charAt(i);
            if (enComillas) {
                if (c == '"') {
                    if (i + 1 < contenido.length() && contenido.charAt(i + 1) == '"') {
                        actual.append('"');
                        i++;
                    } else {
                        enComillas = false;
                    }
                } else {
                    actual.append(c);
                }
            } else if (c == '"') {
                enComillas = true;
            } else if (contenido.startsWith(delimitador, i)) {
                campos.add(actual.toString());
                actual.setLength(0);
                i += delimitador.length() - 1;
            } else if (c == '\n' || c == '\r') {
                campos.add(actual.toString());
                actual.setLength(0);
                if (!(campos.size() == 1 && campos.get(0).isEmpty())) {
                    filas.add(campos.toArray(new String[0]));
                }
                campos = new ArrayList<>();
                if (c == '\r' && i + 1 < contenido.length() && contenido.charAt(i + 1) == '\n') {
                    i++;
                }
            } else {
                actual.append(c);
            }
        }
        // Última fila sin salto de línea final
        if (actual.length() > 0 || !campos.isEmpty()) {
            campos.add(actual.toString());
            if (!(campos.size() == 1 && campos.get(0).isEmpty())) {
                filas.add(campos.toArray(new String[0]));
            }
        }
        return filas;
    }

    /** Convierte una fila en línea CSV, entrecomillando los campos que lo requieran. */
    private String formatearFila(String[] fila, String delimitador) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fila.length; i++) {
            if (i > 0) {
                sb.append(delimitador);
            }
            String valor = fila[i] == null ? "" : fila[i];
            if (valor.contains(delimitador) || valor.contains("\"")
                    || valor.contains("\n") || valor.contains("\r")) {
                sb.append('"').append(valor.replace("\"", "\"\"")).append('"');
            } else {
                sb.append(valor);
            }
        }
        return sb.toString();
    }
}
