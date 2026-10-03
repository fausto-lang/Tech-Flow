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

    public String procesarVenta(OrdenVenta orden, Cliente cliente, boolean conFactura) throws IOException {
        if (!caja.isCajaAbierta()) {
            throw new IllegalStateException("Abra la caja primero.");
        }

        for (Map.Entry<Producto, Integer> item : orden.getItems().entrySet()) {
            if (item.getKey().getStock() < item.getValue()) {
                throw new IllegalArgumentException("Stock insuficiente para: " + item.getKey().getNombre());
            }
        }

        double totalBase = orden.calcularTotal();
        double totalFinal = finanzas.calcularCobroFinal(totalBase, conFactura);
        
        String idFactura = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        String rutaVentasDia = caja.obtenerRutaVentasDia();

        List<Map<String, String>> filasDiarias = new java.util.ArrayList<>();
        java.io.File archivoVentas = new java.io.File(rutaVentasDia);
        if (archivoVentas.exists()) {
            filasDiarias = motorCSV.leerFilas(rutaVentasDia, ConfiguracionCSV.COLUMNAS_VENTA);
        }

        for (Map.Entry<Producto, Integer> item : orden.getItems().entrySet()) {
            Producto p = item.getKey();
            int cantidadVendida = item.getValue();

            p.setStock(p.getStock() - cantidadVendida);
            
            Map<String, String> filaVenta = new java.util.LinkedHashMap<>();
            filaVenta.put("idFactura", idFactura);
            filaVenta.put("ciCliente", cliente != null ? cliente.getCi() : "Sin Registro");
            filaVenta.put("idProducto", p.getIdProducto());
            filaVenta.put("precioEntrada", String.valueOf(p.getPrecioEntrada())); 
            filaVenta.put("precioVenta", String.valueOf(p.getPrecioVenta()));
            filaVenta.put("cantidad", String.valueOf(cantidadVendida));
            
            double subtotalItemBase = p.getPrecioVenta() * cantidadVendida;
            double subtotalItemFinal = finanzas.calcularCobroFinal(subtotalItemBase, conFactura);
            filaVenta.put("totalCobrado", String.valueOf(subtotalItemFinal));

            filasDiarias.add(filaVenta);
        }

        motorCSV.escribirFilas(rutaVentasDia, filasDiarias, ConfiguracionCSV.COLUMNAS_VENTA);

        return "Venta procesada con éxito. Factura N°: " + idFactura + " | Total cobrado: Bs. " + String.format("%.2f", totalFinal);
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

        for (Map<String, String> fila : ventasDelDia) {
            try {
                double precioVenta = motorCSV.parsearDouble(fila.get("precioVenta"),
                    fila.get("idProducto"));
                double precioEntrada = motorCSV.parsearDouble(fila.get("precioEntrada"),
                    fila.get("idProducto"));
                int cantidad = motorCSV.parsearEntero(fila.get("cantidad"),
                    fila.get("idProducto"));
                
                gananciaNeta += (precioVenta - precioEntrada) * cantidad;
                
            } catch (NumberFormatException e) {
                System.err.println("Advertencia: Se encontró un valor no numérico. Fila ignorada.");
            }
        }
        
        return gananciaNeta;
    }
}