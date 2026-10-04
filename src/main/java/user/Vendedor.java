package user;

import java.util.Collections;
import java.util.Map;

import models.Producto;

/**
 * Representa al usuario Cajero/Vendedor responsable de procesar ventas e imprimir comprobantes.
 */
public class Vendedor extends Empleado {

    /**
     * Constructor para instanciar un Vendedor con el rol CAJERO.
     *
     * @param ci         Cédula de identidad.
     * @param nombre     Nombre completo.
     * @param contrasena Contraseña de acceso.
     */
    public Vendedor(String ci, String nombre, String contrasena) {
        super(ci, nombre, contrasena, Rol.CAJERO);
    }

    /**
     * Procesa la venta de un producto a un cliente determinado.
     *
     * @param codigoProducto Código del producto.
     * @param ciCliente      Cédula de identidad del cliente.
     * @param cantidad       Unidades a vender.
     * @return {@code true} si la venta se ejecutó.
     */
    public boolean venderProducto(String codigoProducto, String ciCliente, int cantidad){
        return true;
    }

    /**
     * Cancela o anula un registro de venta.
     *
     * @param idVenta Código identificador de la venta.
     * @return {@code true} si fue eliminada.
     */
    public boolean eliminarVenta(String idVenta) {
        return true;
    }

    /**
     * Genera un código de orden de venta preliminar para la transacción.
     *
     * @param venta Objeto {@link Producto} involucrado.
     * @return Cadena con el identificador de la orden.
     */
    public String generarOrdenDeVenta(Producto venta) {
        return "ORDEN-MOCK-001";
    }

    /**
     * Lee un conjunto de órdenes enviadas en un archivo externo.
     *
     * @param rutaArchivo Ruta del archivo conteniendo las órdenes.
     * @return {@code true} si la lectura fue exitosa.
     */
    public boolean ingresarOrdenDesdeArchivo(String rutaArchivo) {
        return true;
    }

    /**
     * Confirma y concreta el cobro de una orden de venta.
     *
     * @param idOrden Código de la orden a confirmar.
     * @return {@code true} si la venta fue confirmada.
     */
    public boolean confirmarVenta(String idOrden) {
        return true;
    }

    /**
     * Consulta información de un producto directamente en el catálogo.
     *
     * @param codigo Código del producto.
     * @return Objeto {@link Producto} con la información.
     */
    public Producto buscarEnInventario(String codigo) {
        return new Producto(codigo, "Producto Simulado", 100.0, 10.0,"lol", 50,"tu pinche marca","tu pinche categoria");
    }

    /**
     * Construye y formatea una factura para la compra efectuada.
     *
     * @param venta Producto comercializado.
     * @return Texto con el contenido de la factura.
     */
    public String generarFactura(Producto venta) {
        return "=== FACTURA DE VENTA SIMULADA ===";
    }

    /**
     * Calcula el monto total calculando costos, comisiones e impuestos.
     *
     * @param pCosto Precio de costo básico.
     * @param oCosto Gastos o costos operacionales adicionales.
     * @param iva    Importe correspondiente al impuesto del valor agregado (IVA).
     * @return Valor total calculado.
     */
    public double calcularCosto(double pCosto, double oCosto, double iva) {
        return pCosto + (oCosto * 0.20) + iva;
    }

    /**
     * Busca comprobantes o datos asociados por razón social o criterio específico.
     *
     * @param razon Razón social o término de búsqueda.
     * @return Mapa con la información resultante.
     */
    public Map<String, Object> buscarPorRazon(String razon) {
        return Collections.emptyMap();
    }
}