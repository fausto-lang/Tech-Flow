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

    public void setPrecio(double NuevoPrecio) {
        this.precio = NuevoPrecio;
    }

    public void setStock(int NuevoStock) {
        this.stock = NuevoStock;
    }

    public int modificarStock(int cantidadParaModificar){
        return 1;
    }

}
