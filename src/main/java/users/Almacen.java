package users;

import models.Producto;

public class Almacen extends Empleado {

    public boolean ingresarProductoNuevo(Producto producto) {
        return true;
    }

    public boolean ingresarProductoExistente(String codigo, int cantidad) {
        return true;
    }

    public boolean ingresarEntradaDesdeArchivo(String rutaArchivo) {
        return true;
    }
}