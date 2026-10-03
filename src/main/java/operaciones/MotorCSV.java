//Esta es la funcionalidad base (el motor) que permite que los datos no se borren al cerrar el programa.

package operaciones;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class MotorCSV {

    private final CsvMapper csvMapper;

    public MotorCSV() {
        this.csvMapper = new CsvMapper();
    }

    public List<Map<String, String>> leerFilas(String ruta, String[] columnas) throws IOException {
        List<Map<String, String>> filas = new ArrayList<>();
        File archivo = new File(ruta);
        if (!archivo.exists() || archivo.length() == 0) {
            return filas;
        }

        CsvSchema esquema = CsvSchema.emptySchema().withHeader();
        try (MappingIterator<Map<String, String>> registros = csvMapper
                .readerFor(Map.class)
                .with(esquema)
                .readValues(archivo)) {
            while (registros.hasNext()) {
                Map<?, ?> original = registros.next();
                Map<String, String> normalizada = new LinkedHashMap<>();
                for (String columna : columnas) {
                    Object valor = original.get(columna);
                    normalizada.put(columna, valor == null ? "" : valor.toString().trim());
                }
                filas.add(normalizada);
            }
        }
        return filas;
    }

    public void escribirFilas(String ruta, List<Map<String, String>> filas, String[] columnas) throws IOException {
        File archivo = new File(ruta);
        File padre = archivo.getParentFile();
        if (padre != null && !padre.exists()) {
            padre.mkdirs();
        }

        CsvSchema.Builder constructorEsquema = CsvSchema.builder();
        for (String columna : columnas) {
            constructorEsquema.addColumn(columna);
        }
        CsvSchema esquema = constructorEsquema.setUseHeader(true).build();

        csvMapper.writer(esquema).writeValues(archivo).writeAll(filas);
    }
public int parsearEntero(String valor, String idProducto) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Error crítico: El stock del producto con ID [" + idProducto + "] está vacío.");
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error de formato: El stock '" + valor + "' del producto ID [" + idProducto + "] no es un número entero válido.");
        }
    }

    public double parsearDouble(String valor, String idProducto) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Error crítico: El precio del producto con ID [" + idProducto + "] está vacío.");
        }
        try {
            return Double.parseDouble(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error de formato: El precio '" + valor + "' del producto ID [" + idProducto + "] no es un número válido.");
        }
    }
}