package operaciones;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ServicioVenta {
    private final MotorCSV motorCSV;
    private final GestorCajaDiaria caja;
    private final CalculadoraFinanzas finanzas;

    public ServicioVenta(MotorCSV motorCSV, GestorCajaDiaria caja) {
        this.motorCSV = motorCSV;
        this.caja = caja;
        this.finanzas = new CalculadoraFinanzas();
    }

    // 1. Generar Factura real y descontar stock
    public String procesarVenta(OrdenVenta orden, Cliente cliente, boolean conFactura) throws IOException {
        if (!caja.isCajaAbierta()) throw new IllegalStateException("Abra la caja primero.");

        // Verificar stock antes de vender
        for (Map.Entry<Producto, Integer> item : orden.getItems().entrySet()) {
            if (item.getKey().getStock() < item.getValue()) {
                throw new IllegalArgumentException("Stock insuficiente para: " + item.getKey().getNombre());
            }
        }

        double totalBase = orden.calcularTotal();
        double totalFinal = finanzas.calcularCobroFinal(totalBase, conFactura);

        // Descontar inventario
        for (Map.Entry<Producto, Integer> item : orden.getItems().entrySet()) {
            Producto p = item.getKey();
            p.setStock(p.getStock() - item.getValue());
            
            // Guardar en data/ventas/año/mes/dia/ventas.csv (usando el GestorCajaDiaria)
            String rutaVentasDia = caja.obtenerRutaVentasDia();
            // Lógica de MotorCSV para anexar fila: idFactura, CI, idProducto, precioEntrada (histórico), precioVenta, cantidad
        }

        return "Venta procesada con éxito. Total cobrado: Bs. " + totalFinal;
    }

    public double calcularGananciasDia(java.time.LocalDate fecha) throws IOException {
        
        String rutaVentasDia = String.format("data/ventas/%04d/%02d/%02d/ventas.csv", 
            fecha.getYear(), 
            fecha.getMonthValue(), 
            fecha.getDayOfMonth());
            
        java.io.File archivoVentas = new java.io.File(rutaVentasDia);
        
        if (!archivoVentas.exists()) {
            return 0.0; 
        }

        List<Map<String, String>> ventasDelDia = motorCSV.leerFilas(rutaVentasDia, ConfiguracionCSV.COLUMNAS_VENTA);
        double gananciaNeta = 0.0;

        // 4. Calcular la ganancia
        for (Map<String, String> fila : ventasDelDia) {
            try {
                double precioVenta = motorCSV.parsearDouble(fila.get("precioVenta"));
                double precioEntrada = motorCSV.parsearDouble(fila.get("precioEntrada"));
                int cantidad = motorCSV.parsearEntero(fila.get("cantidad"));
                
                gananciaNeta += (precioVenta - precioEntrada) * cantidad;
                
            } catch (NumberFormatException e) {
                System.err.println("Advertencia: Se encontró un valor no numérico. Fila ignorada.");
            }
        }
        
        return gananciaNeta;
    }
}