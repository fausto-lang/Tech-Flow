package models;

import java.util.ArrayList;
import java.util.List;

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

    public void entregarProducto(Producto producto) {
        if (producto != null) {
            this.productos.add(producto);
        }
    }
}