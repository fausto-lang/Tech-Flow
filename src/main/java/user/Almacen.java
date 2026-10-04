package user;

import models.Producto;

/**
 * Representa al usuario responsable del módulo de almacén.
 */
public class Almacen extends Empleado {

    /**
     * Registra un nuevo producto en el catálogo del inventario.
     *
     * @param producto Objeto {@link Producto} a ingresar.
     * @return {@code true} si se registró correctamente.
     */
    public boolean ingresarProductoNuevo(Producto producto) {
        return true;
    }

    /**
     * Incrementa la cantidad de stock de un producto ya existente.
     *
     * @param codigo   Código del producto.
     * @param cantidad Cantidad de stock a ingresar.
     * @return {@code true} si el incremento fue exitoso.
     */
    public boolean ingresarProductoExistente(String codigo, int cantidad) {
        return true;
    }

    /**
     * Carga múltiples registros de entradas desde un archivo externo.
     *
     * @param rutaArchivo Ruta del archivo de carga.
     * @return {@code true} si la lectura del archivo fue procesada.
     */
    public boolean ingresarEntradaDesdeArchivo(String rutaArchivo) {
        return true;
    }
}