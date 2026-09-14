package operaciones;
import java.util.ArrayList;
import java.util.List;

public class Proveedor {
    private String nombreProveedor;
    private String codigoProveedor;
    private int contactoProveedor;
    private List<Producto> productos;

    public Proveedor(String nombreProveedor, String codigoProveedor, int contactoProveedor, List<Producto> productos) {
        this.nombreProveedor = nombreProveedor;
        this.codigoProveedor = codigoProveedor;
        this.contactoProveedor = contactoProveedor;
        this.productos = productos;
    }

    public Proveedor(String nombreProveedor, String codigoProveedor, int contactoProveedor) {
        this(nombreProveedor, codigoProveedor, contactoProveedor, new ArrayList<>());
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public String getCodigoProveedor() {
        return codigoProveedor;
    }

    public int getContactoProveedor() {
        return contactoProveedor;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setContactoProveedor(int contactoProveedor) {
        this.contactoProveedor = contactoProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public void setCodigoProveedor(String codigoProveedor) {
        this.codigoProveedor = codigoProveedor;
    }
    //esta es la logica para entregar un producto si la lista de productos esta vacia.
    public void entregarProducto(Producto producto) {
        if (producto != null) {
            this.productos.add(producto);
        }
    }
}
