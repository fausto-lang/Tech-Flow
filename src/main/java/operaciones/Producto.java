package operaciones;

public class Producto {
    private final String idProducto;
    private final String marca;
    private double precio;
    private int stock;
    private final String nombre;
    private final String descripcion;
    private final String categoria;

    public Producto(String idProducto, String marca, double precio, String descripcion, int stock,
            String nombre, String categoria) {
        this.idProducto = idProducto;
        this.marca = marca;
        this.precio = precio;
        this.descripcion = descripcion;
        this.stock = stock;
        this.nombre = nombre;
        this.categoria = categoria;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public String getMarca() {
        return marca;
    }

    public double getPrecio() {
        return precio;
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

    public void setPrecio(double nuevoPrecio) {
        this.precio = nuevoPrecio;
    }

    public void setStock(int nuevoStock) {
        this.stock = nuevoStock;
    }
}