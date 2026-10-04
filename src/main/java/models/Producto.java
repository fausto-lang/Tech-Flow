package models;

import java.util.Objects;

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

    /**
     * Compara dos productos por valor (todos sus campos), no por identidad.
     * Útil para aserciones en los tests y como clave/valor en colecciones.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto)) return false;
        Producto otro = (Producto) o;
        return Double.compare(otro.precioEntrada, precioEntrada) == 0
                && Double.compare(otro.precioVenta, precioVenta) == 0
                && stock == otro.stock
                && Objects.equals(idProducto, otro.idProducto)
                && Objects.equals(nombre, otro.nombre)
                && Objects.equals(marca, otro.marca)
                && Objects.equals(descripcion, otro.descripcion)
                && Objects.equals(categoria, otro.categoria);
    }

    /** Hash coherente con {@link #equals(Object)}. */
    @Override
    public int hashCode() {
        return Objects.hash(idProducto, nombre, marca, precioEntrada, precioVenta, descripcion, stock, categoria);
    }

    /** Representación legible para depuración y reportes. */
    @Override
    public String toString() {
        return "Producto{idProducto='" + idProducto + "', nombre='" + nombre + "', marca='" + marca
                + "', categoria='" + categoria + "', precioEntrada=" + precioEntrada
                + ", precioVenta=" + precioVenta + ", stock=" + stock + "}";
    }
}
