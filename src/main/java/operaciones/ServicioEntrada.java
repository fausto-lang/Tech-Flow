package operaciones;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServicioEntrada {

    private final MotorCSV motorCSV;
    private final GestorCajaDiaria caja;
    
    private final ServicioInventario servicioInventario;
    private final ServicioProveedor servicioProveedor;

    public ServicioEntrada(MotorCSV motorCSV, GestorCajaDiaria caja, 
                           ServicioInventario servicioInventario, 
                           ServicioProveedor servicioProveedor) {
        this.motorCSV = motorCSV;
        this.caja = caja;
        this.servicioInventario = servicioInventario;
        this.servicioProveedor = servicioProveedor;
    }

    public void registrarEntrada(Proveedor proveedor, Producto producto, int cantidadIngresada, double costoTotal) throws IOException {
        
        if (cantidadIngresada <= 0) {
            throw new IllegalArgumentException("La cantidad ingresada debe ser mayor a cero.");
        }

        if (!caja.isCajaAbierta()) {
            throw new IllegalStateException("Debe abrir la caja para registrar entradas del día.");
        }

        List<Proveedor> listaProveedores = servicioProveedor.proveedores();
        servicioProveedor.registrarProveedorSiNoExiste(listaProveedores, proveedor);

        List<Producto> inventarioActual = servicioInventario.obtenerInventario(); // O el método que uses para listar tu inventario
        servicioInventario.registrarIngresoStock(producto, cantidadIngresada, inventarioActual);

        guardarRegistroEntradaDiaria(proveedor, producto, cantidadIngresada, costoTotal);
    }

    private void guardarRegistroEntradaDiaria(Proveedor proveedor, Producto producto, int cantidad, double costoTotal) throws IOException {
        String rutaEntradasDia = caja.obtenerRutaEntradasDia();
        File archivoEntradas = new File(rutaEntradasDia);
        
        List<Map<String, String>> filasDiarias = new ArrayList<>();
        
        if (archivoEntradas.exists()) {
            filasDiarias = motorCSV.leerFilas(rutaEntradasDia, ConfiguracionCSV.ENTRADAS_COLUMNAS);
        }

        Map<String, String> nuevaEntrada = new LinkedHashMap<>();
        nuevaEntrada.put("idProveedor", proveedor.getCodigoProveedor());
        nuevaEntrada.put("nombreProveedor", proveedor.getNombreProveedor());
        nuevaEntrada.put("idProducto", producto.getIdProducto());
        nuevaEntrada.put("nombreProducto", producto.getNombre());
        nuevaEntrada.put("cantidad", String.valueOf(cantidad));
        nuevaEntrada.put("costoTotal", String.valueOf(costoTotal));
        
        filasDiarias.add(nuevaEntrada);
        motorCSV.escribirFilas(rutaEntradasDia, filasDiarias, ConfiguracionCSV.ENTRADAS_COLUMNAS);
    }
}