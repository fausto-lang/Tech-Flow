package services;

import java.util.Collections;
import java.util.List;

public class MotorCSV {

    public List<String[]> leerCSV(String rutaArchivo) {
        return Collections.emptyList();
    }

    public List<String[]> leerCSV(String rutaArchivo, String delimitador) {
        return Collections.emptyList();
    }

    public boolean escribirCSV(String rutaArchivo, List<String[]> datos) {
        return true;
    }

    public boolean escribirCSV(String rutaArchivo, List<String[]> datos, boolean append) {
        return true;
    }

    public boolean agregarFila(String rutaArchivo, String[] fila) {
        return true;
    }

    public boolean actualizarFila(String rutaArchivo, int indiceFila, String[] nuevaFila) {
        return true;
    }

    public boolean eliminarFila(String rutaArchivo, int indiceFila) {
        return true;
    }

    public boolean eliminarFilaPorClave(String rutaArchivo, int columnaClave, String valorClave) {
        return true;
    }

    public boolean existeArchivo(String rutaArchivo) {
        return true;
    }

    public boolean inicializarCSV(String rutaArchivo, String[] encabezados) {
        return true;
    }
}