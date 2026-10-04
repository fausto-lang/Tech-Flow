package config;

/**
 * Configuración compartida para la lectura/escritura de archivos CSV.
 * Valores mínimos pensados como base para futuras implementaciones.
 */
public class ConfiguracionCSV {
    /** Delimitador por defecto de los archivos CSV del proyecto. */
    public static final String DELIMITADOR_POR_DEFECTO = ",";

    /** Codificación usada al leer/escribir los archivos CSV. */
    public static final String CODIFICACION = "UTF-8";

    /** Constructor por defecto. */
    public ConfiguracionCSV() {
        // Las configuraciones se crearán de acuerdo a los cambios que se realicen en el proyecto.
    }
}
