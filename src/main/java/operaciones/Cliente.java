package operaciones;

import java.util.ArrayList;
import java.util.List;

public class Cliente {
    private String nombre;
    private String ci;
    private List<Producto> compras;

    public Cliente(String nombre, String ci) {
        this.nombre = nombre;
        this.ci = ci;
        this.compras = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public String getCi() {
        return ci;
    }

    public List<Producto> getCompras() {
        return compras;
    }

    public void agregarCompra(Producto producto) {
        if (producto != null) {
            this.compras.add(producto);
        }
    }
}