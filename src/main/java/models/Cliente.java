package models;

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
    public boolean isEsFrecuente() { return true; }
}