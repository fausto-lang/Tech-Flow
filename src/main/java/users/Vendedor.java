package users;

import java.util.Collections;
import java.util.Map;

import models.Producto;

public class Vendedor extends Empleado {

    public Vendedor() {
        super();
    }

    public Vendedor(String ci, String nombre, String contrasena) {
        super(ci, nombre, contrasena, Rol.CAJERO);
    }


    public boolean venderProducto(String codigoProducto, String ciCliente, int cantidad){
        return true;
    }

    public boolean eliminarVenta(String idVenta) {
        return true;
    }

    public String generarOrdenDeVenta(Producto venta) {
        return "ORDEN-MOCK-001";
    }

    public boolean ingresarOrdenDesdeArchivo(String rutaArchivo) {
        return true;
    }

    public boolean confirmarVenta(String idOrden) {
        return true;
    }

    public Producto buscarEnInventario(String codigo) {
        return new Producto(codigo, "Producto Simulado", 100.0, 10.0,"lol", 50,"tu pinche marca","tu pinche categoria");
    }

    public String generarFactura(Producto venta) {
        return "=== FACTURA DE VENTA SIMULADA ===";
    }

    public double calcularCosto(double pCosto, double oCosto, double iva) {
        return pCosto + (oCosto * 0.20) + iva;
    }

    public Map<String, Object> buscarPorRazon(String razon) {
        return Collections.emptyMap();
    }
}