package user;

/**
 * Clase base que define un empleado del sistema.
 */
public class Empleado {
    private String ci;
    private String nombre;
    private String contrasena;
    private Rol rol;

    /** Constructor por defecto. */
    public Empleado() {}

    /**
     * Constructor con todos los atributos del empleado.
     *
     * @param ci         Cédula de identidad.
     * @param nombre     Nombre completo.
     * @param contrasena Contraseña de acceso.
     * @param rol        Rol asignado en el sistema.
     */
    public Empleado(String ci, String nombre, String contrasena, Rol rol) {
        this.ci = ci;
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    /** @return Cédula de identidad del empleado. */
    public String getCi() { return ci; }

    /** @return Nombre completo del empleado. */
    public String getNombre() { return nombre; }

    /** @return Contraseña del empleado. */
    public String getContrasena() { return contrasena; }

    /** @return Rol asignado al empleado. */
    public Rol getRol() { return rol; }
}