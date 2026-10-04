package models;

import java.util.Objects;

public class Cliente {
    private String ci;
    private String nombre;

    public Cliente() {}

    public Cliente(String ci, String nombre) {
        this.ci = ci;
        this.nombre = nombre;
    }

    public String getCi() { return ci; }
    public String getNombre() { return nombre; }

    /**
     * Indica si el cliente es frecuente.
     *
     * @return {@code true} si es un cliente frecuente.
     */
    public boolean isEsFrecuente() { return true; }

    /**
     * Compara dos clientes por valor (ci y nombre).
     * Necesario para aserciones de igualdad en los tests.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente)) return false;
        Cliente otro = (Cliente) o;
        return Objects.equals(ci, otro.ci) && Objects.equals(nombre, otro.nombre);
    }

    /** Hash coherente con {@link #equals(Object)}. */
    @Override
    public int hashCode() {
        return Objects.hash(ci, nombre);
    }

    /** Representación legible para depuración. */
    @Override
    public String toString() {
        return "Cliente{ci='" + ci + "', nombre='" + nombre + "'}";
    }
}
