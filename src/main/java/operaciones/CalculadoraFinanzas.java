package operaciones;

public class CalculadoraFinanzas {
    
    private static final double IVA = 0.13;
    
    public double sugerirPrecioVenta(double precioEntrada, double margenGananciaDeseado) {
        return precioEntrada + (precioEntrada * margenGananciaDeseado);
    }

    public double calcularCobroFinal(double precioBaseTotal, boolean conFactura) {
        if (conFactura) {
            return precioBaseTotal + (precioBaseTotal * IVA);
        }
        return precioBaseTotal;
    }
}