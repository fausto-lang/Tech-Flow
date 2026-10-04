package user;

import java.util.Objects;

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

    /**
     * Compara dos empleados por valor (ci, nombre, contraseña y rol).
     * Necesario para poder usar aserciones de igualdad en los tests.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Empleado)) return false;
        Empleado otro = (Empleado) o;
        return Objects.equals(ci, otro.ci)
                && Objects.equals(nombre, otro.nombre)
                && Objects.equals(contrasena, otro.contrasena)
                && rol == otro.rol;
    }

    /** Hash coherente con {@link #equals(Object)}. */
    @Override
    public int hashCode() {
        return Objects.hash(ci, nombre, contrasena, rol);
    }

    /** Representación legible para depuración. */
    @Override
    public String toString() {
        return "Empleado{ci='" + ci + "', nombre='" + nombre + "', rol=" + rol + "}";
    }
}
