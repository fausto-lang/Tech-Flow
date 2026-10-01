package operaciones;

public class Empleado {

    private final String ci;
    private final String nombre;
    private final String cargo;
    private final String salt;
    private final String contrasenaHash;

    public Empleado(String ci, String nombre, String cargo, String salt, String contrasenaHash) {
        this.ci = ci;
        this.nombre = nombre;
        this.cargo = cargo;
        this.salt = salt;
        this.contrasenaHash = contrasenaHash;
    }

    public String getCi() {
        return ci;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCargo() {
        return cargo;
    }

    public String getSalt() {
        return salt;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }
}
