package operaciones;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServicioSalida {

    private final MotorCSV motorCSV;
    private final GestorCajaDiaria caja;
    private final ServicioInventario servicioInventario;

    public ServicioSalida(MotorCSV motorCSV, GestorCajaDiaria caja, ServicioInventario servicioInventario) {
        this.motorCSV = motorCSV;
        this.caja = caja;
        this.servicioInventario = servicioInventario;
    }

    public void registrarSalida(Producto producto, int cantidad, String motivo, List<Producto> inventarioActual) throws IOException {
        
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        if (!caja.isCajaAbierta()) {
            throw new IllegalStateException("Debe abrir la caja para registrar salidas del día.");
        }

        if (producto.getStock() < cantidad) {
            throw new IllegalArgumentException("No hay stock suficiente. Stock actual: " + producto.getStock());
        }

        servicioInventario.registrarIngresoStock(producto, -cantidad, inventarioActual);

        String rutaSalidasDia = caja.obtenerRutaSalidasDia();
        File archivoSalidas = new File(rutaSalidasDia);
        
        List<Map<String, String>> filasDiarias = new ArrayList<>();
        
        if (archivoSalidas.exists()) {
            filasDiarias = motorCSV.leerFilas(rutaSalidasDia, ConfiguracionCSV.SALIDAS_COLUMNAS);
        }

        Map<String, String> nuevaSalida = new LinkedHashMap<>();
        nuevaSalida.put("idProducto", producto.getIdProducto());
        nuevaSalida.put("nombreProducto", producto.getNombre());
        nuevaSalida.put("cantidad", String.valueOf(cantidad));
        nuevaSalida.put("motivo", motivo); // Ej: "Dañado", "Vencido", "Uso Interno"
        nuevaSalida.put("fechaSalida", LocalDate.now().toString());
        
        filasDiarias.add(nuevaSalida);
        motorCSV.escribirFilas(rutaSalidasDia, filasDiarias, ConfiguracionCSV.SALIDAS_COLUMNAS);
    }
}