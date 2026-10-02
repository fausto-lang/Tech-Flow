package operaciones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cliente {
    private String nombre;
    private final String ci;
    private final List<Producto> compras;

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
    return Collections.unmodifiableList(compras);
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void agregarCompra(Producto producto) {
        if (producto != null) {
            this.compras.add(producto);
        }
    }
}