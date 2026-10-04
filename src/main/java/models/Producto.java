package models;
public class Producto {

    private final String idProducto;
    private final String marca;
    private double precioEntrada;
    private double precioVenta;
    private int stock;
    private final String nombre;
    private final String descripcion;
    private final String categoria;

    public Producto(String idProducto, String nombre, double precioEntrada, double precioVenta, 
                    String descripcion, int stock, String marca, String categoria) {
        this.idProducto = idProducto;
        this.marca = marca;
        this.precioEntrada = precioEntrada;
        this.precioVenta = precioVenta;
        this.stock = stock;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
    }

    // Getters
    public String getIdProducto() { return idProducto; }
    public String getMarca() { return marca; }
    public double getPrecioEntrada() { return precioEntrada; }
    public double getPrecioVenta() { return precioVenta; }
    public int getStock() { return stock; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }

    // Setters
    public void setPrecioEntrada(double precioEntrada) { this.precioEntrada = precioEntrada; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }
    public void setStock(int stock) { this.stock = stock; }

    
}