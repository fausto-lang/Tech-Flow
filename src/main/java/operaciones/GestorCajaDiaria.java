package operaciones;

import java.io.File;
import java.time.LocalDate;

public class GestorCajaDiaria {
    
    private boolean cajaAbierta = false;
    private LocalDate fechaCajaActual;

    public void abrirCaja() {
        this.fechaCajaActual = LocalDate.now();
        this.cajaAbierta = true;
        crearEstructuraDirectoriosSiNoExiste();
    }

    public void cerrarCaja() {
        this.cajaAbierta = false;
    }

    public boolean isCajaAbierta() {
        return cajaAbierta;
    }

    public String obtenerRutaVentasDia() {
        return generarRutaJerarquica("ventas");
    }

    public String obtenerRutaEntradasDia() {
        return generarRutaJerarquica("entradas");
    }

    private String generarRutaJerarquica(String modulo) {
        if (!cajaAbierta) throw new IllegalStateException("La caja está cerrada.");
        
        return String.format("data/%s/%04d/%02d/%02d/%s.csv", 
            modulo, 
            fechaCajaActual.getYear(), 
            fechaCajaActual.getMonthValue(), 
            fechaCajaActual.getDayOfMonth(),
            modulo);
    }

    private void crearEstructuraDirectoriosSiNoExiste() {
        new File(obtenerRutaVentasDia()).getParentFile().mkdirs();
        new File(obtenerRutaEntradasDia()).getParentFile().mkdirs();
    }
}