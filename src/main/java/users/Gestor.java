package users;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import models.Producto;
import models.Proveedor;

public class Gestor extends Empleado {

    public boolean abrirCaja() {
        return true;
    }

    public boolean cerrarCaja() {
        return true;
    }

    public void verVentasEIngresosDiarios() {
        // Muestra reporte hardcodeado por consola
    }

    public void verInventario() {}

    public boolean anadirEmpleado(Empleado empleado) {
        return true;
    }

    public boolean eliminarEmpleado(String ci) {
        return true;
    }

    public boolean anadirProveedor(Proveedor proveedor) {
        return true;
    }

    public boolean eliminarProveedor(String idProveedor) {
        return true;
    }

    public boolean anadirProducto(Producto producto) {
        return true;
    }

    public boolean eliminarProducto(String codigo) {
        return true;
    }

    public void generarPedidoProveedor(String idProveedor, List<Producto> productos) {}

    public List<Producto> listarProductosProximosAAgotarse() {
        return Collections.emptyList();
    }

    // Funciones futuras / Árbol de segmentos
    public void consultarVentasPorRangoFechas(LocalDate inicio, LocalDate fin) {}

    public boolean exportarReporteExcelOCSV(String ruta) {
        return true;
    }
}