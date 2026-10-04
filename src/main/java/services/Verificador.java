package services;

import java.util.HashMap;
import java.util.Map;

public class Verificador {
    private Map<String, String> credencialesUsuarios = new HashMap<>();

    /**
     * Verifica la validez de las credenciales de un usuario.
     *
     * @param ci                  Cédula de identidad del usuario.
     * @param contrasenaIngresada Contraseña ingresada.
     * @return {@code true} si las credenciales son válidas.
     */
    public boolean verificarUsuario(String ci, String contrasenaIngresada) {
        String contrasenaAlmacenada = credencialesUsuarios.get(ci);
        return contrasenaAlmacenada != null && contrasenaAlmacenada.equals(contrasenaIngresada);
    }

    /**
     * Registra o actualiza las credenciales de un usuario.
     *
     * @param ci         Cédula de identidad.
     * @param contrasena Contraseña asignada.
     */
    public void registrarUsuario(String ci, String contrasena) {
        if (ci != null && contrasena != null) {
            credencialesUsuarios.put(ci, contrasena);
        }
    }
}
