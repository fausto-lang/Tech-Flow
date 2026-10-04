package users;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import models.Producto;
import models.Proveedor;

public class Gestor extends Empleado {

    public Gestor() {
        super();
    }

    public Gestor(String ci, String nombre, String contrasena) {
        super(ci, nombre, contrasena, Rol.ADMINISTRADOR);
    }

    public boolean abrirCaja() {
        return true;
    }

    public boolean cerrarCaja() {
        return true;
    }

    public void verVentasEIngresosDiarios() {
    }

    public void verInventario() {}

    public boolean ingresarEntradaProducto(Producto entrada) {
        return true;
    }

    public boolean ingresarEntradasDesdeCSV(String rutaArchivo) {
        return true;
    }

    public void verAnalisisEntradasDiarias() {
    }

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

    public void consultarVentasPorRangoFechas(LocalDate inicio, LocalDate fin) {}

    public boolean exportarReporteExcelOCSV(String ruta) {
        return true;
    }
}