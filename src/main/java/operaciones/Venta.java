package operaciones;

import java.time.LocalDate;
import java.util.List;

public class Venta {
    private String idVenta;
    private LocalDate fechaVenta;
    private String clienteCi;
    private String nombreCliente;
    private List<Producto> productosVendidos;

    public Venta(String idVenta, LocalDate fechaVenta, String clienteCi, String nombreCliente, List<Producto> productosVendidos) {
        this.idVenta = idVenta;
        this.fechaVenta = fechaVenta;
        this.clienteCi = clienteCi;
        this.nombreCliente = nombreCliente;
        this.productosVendidos = productosVendidos;
    }

    public String getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(String idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDate getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDate fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public String getClienteCi() {
        return clienteCi;
    }

    public void setClienteCi(String clienteCi) {
        this.clienteCi = clienteCi;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public List<Producto> getProductosVendidos() {
        return productosVendidos;
    }

    public void setProductosVendidos(List<Producto> productosVendidos) {
        this.productosVendidos = productosVendidos;
    }
}