package operaciones;

public class Empleado {
    private final String id;
    private final String nombre;
    private final String password;
    private final Rol rol;

    public Empleado(String id, String nombre, String password, Rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.password = password;
        this.rol = rol;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContrasena() {
        return password;
    }

    public boolean verificarPassword(String pwd) { return this.password.equals(pwd); }
    public Rol getRol() { return rol; }
}
