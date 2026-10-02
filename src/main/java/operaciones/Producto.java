package operaciones;

public class Producto {
    private final String idProducto;
    private final String marca;
    private double precioEntrada;
    private double precioVenta;
    private int stock;
    private final String nombre;
    private final String descripcion;
    private final String categoria;

    public Producto(String idProducto, String marca, double precioEntrada, double precioVenta, String descripcion, int stock,
            String nombre, String categoria) {
        this.idProducto = idProducto;
        this.marca = marca;
        this.precioEntrada = precioEntrada;
        this.precioVenta = precioVenta;
        this.stock = stock;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public String getMarca() {
        return marca;
    }

    public double getPrecioEntrada() {
        return precioEntrada;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public int getStock() {
        return stock;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setPrecioEntrada(double nuevoPrecioEntrada) {
        this.precioEntrada = nuevoPrecioEntrada;
    }

    public void setPrecioVenta(double nuevoPrecioVenta) {
        this.precioVenta = nuevoPrecioVenta;
    }

    public void setStock(int nuevoStock) {
        this.stock = nuevoStock;
    }
}