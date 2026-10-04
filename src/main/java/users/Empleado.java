package users;

public class Empleado {
    private String ci;
    private String nombre;
    private String contrasena;
    private Rol rol;

    public Empleado() {}

    public Empleado(String ci, String nombre, String contrasena, Rol rol) {
        this.ci = ci;
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public String getCi() { return ci; }
    public String getNombre() { return nombre; }
    public String getContrasena() { return contrasena; }
    public Rol getRol() { return rol; }
}