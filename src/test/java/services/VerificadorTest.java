package services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Verificador")
class VerificadorTest {

    @Test
    @DisplayName("Credenciales correctas son válidas")
    void credencialesCorrectas() {
        Verificador v = new Verificador();
        v.registrarUsuario("CI-1", "pw");
        assertTrue(v.verificarUsuario("CI-1", "pw"));
    }

    @Test
    @DisplayName("Contraseña incorrecta no es válida")
    void contrasenaIncorrecta() {
        Verificador v = new Verificador();
        v.registrarUsuario("CI-1", "pw");
        assertFalse(v.verificarUsuario("CI-1", "otra"));
    }

    @Test
    @DisplayName("Usuario inexistente no es válido")
    void usuarioInexistente() {
        assertFalse(new Verificador().verificarUsuario("nadie", "pw"));
    }

    @Test
    @DisplayName("verificarUsuario con ci null no es válido")
    void verificarConCiNull() {
        assertFalse(new Verificador().verificarUsuario(null, "pw"));
    }

    @Test
    @DisplayName("verificarUsuario con contraseña null no es válido")
    void verificarConContrasenaNull() {
        Verificador v = new Verificador();
        v.registrarUsuario("CI-1", "pw");
        assertFalse(v.verificarUsuario("CI-1", null));
    }

    @Test
    @DisplayName("Registrar de nuevo sobrescribe la contraseña")
    void registrarSobrescribe() {
        Verificador v = new Verificador();
        v.registrarUsuario("CI-1", "pw1");
        v.registrarUsuario("CI-1", "pw2");
        assertTrue(v.verificarUsuario("CI-1", "pw2"));
    }

    @Test
    @DisplayName("Registrar con ci null no registra credenciales")
    void registrarConCiNull() {
        Verificador v = new Verificador();
        v.registrarUsuario(null, "pw");
        assertFalse(v.verificarUsuario(null, "pw"));
    }

    @Test
    @DisplayName("Registrar con contraseña null no registra credenciales")
    void registrarConContrasenaNull() {
        Verificador v = new Verificador();
        v.registrarUsuario("CI-1", null);
        assertFalse(v.verificarUsuario("CI-1", null));
    }

    @Test
    @DisplayName("Un verificador nuevo no conoce a ningún usuario")
    void verificadorNuevoSinUsuarios() {
        assertFalse(new Verificador().verificarUsuario("CI-1", "pw"));
    }

    @Test
    @DisplayName("Varios usuarios se verifican de forma independiente")
    void variosUsuariosIndependientes() {
        Verificador v = new Verificador();
        v.registrarUsuario("CI-1", "pw1");
        v.registrarUsuario("CI-2", "pw2");
        assertTrue(v.verificarUsuario("CI-2", "pw2"));
    }

    @Test
    @DisplayName("Una contraseña vacía es válida si se registró vacía")
    void contrasenaVacia() {
        Verificador v = new Verificador();
        v.registrarUsuario("CI-1", "");
        assertTrue(v.verificarUsuario("CI-1", ""));
    }

    @Test
    @DisplayName("La contraseña distingue mayúsculas y minúsculas")
    void distingueMayusculas() {
        Verificador v = new Verificador();
        v.registrarUsuario("CI-1", "Pw");
        assertFalse(v.verificarUsuario("CI-1", "pw"));
    }

    @Test
    @DisplayName("Los espacios cuentan como parte de la contraseña")
    void espaciosCuentan() {
        Verificador v = new Verificador();
        v.registrarUsuario("CI-1", "pw ");
        assertFalse(v.verificarUsuario("CI-1", "pw"));
    }

    @Test
    @DisplayName("El usuario distingue mayúsculas y minúsculas")
    void usuarioDistingueMayusculas() {
        Verificador v = new Verificador();
        v.registrarUsuario("CI-1", "pw");
        assertFalse(v.verificarUsuario("ci-1", "pw"));
    }
}
