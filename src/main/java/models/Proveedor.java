package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Proveedor {
    private String nombre;
    private String codigo;
    private int contacto;
    private List<Producto> productos;

    public Proveedor(String nombre, String codigo, int contacto, List<Producto> productos) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.contacto = contacto;
        this.productos = productos != null ? productos : new ArrayList<>();
    }

    public Proveedor(String nombre, String codigo, int contacto) {
        this(nombre, codigo, contacto, new ArrayList<>());
    }

    public String getNombreProveedor() {
        return nombre;
    }

    public String getCodigoProveedor() {
        return codigo;
    }

    public int getContactoProveedor() {
        return contacto;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setContactoProveedor(int contactoProveedor) {
        this.contacto = contactoProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombre = nombreProveedor;
    }

    public void setCodigoProveedor(String codigoProveedor) {
        this.codigo = codigoProveedor;
    }

    /**
     * Registra la entrega de un nuevo producto por parte del proveedor.
     *
     * @param producto Producto entregado.
     */
    public void entregarProducto(Producto producto) {
        if (producto != null) {
            this.productos.add(producto);
        }
    }

    /**
     * Compara dos proveedores por valor (nombre, código, contacto y catálogo).
     * Necesario para aserciones de igualdad en los tests.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Proveedor)) return false;
        Proveedor otro = (Proveedor) o;
        return contacto == otro.contacto
                && Objects.equals(nombre, otro.nombre)
                && Objects.equals(codigo, otro.codigo)
                && Objects.equals(productos, otro.productos);
    }

    /** Hash coherente con {@link #equals(Object)}. */
    @Override
    public int hashCode() {
        return Objects.hash(nombre, codigo, contacto, productos);
    }

    /** Representación legible para depuración. */
    @Override
    public String toString() {
        return "Proveedor{codigo='" + codigo + "', nombre='" + nombre + "', contacto=" + contacto
                + ", productos=" + productos.size() + "}";
    }
}
