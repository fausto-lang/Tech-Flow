package services;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import user.Empleado;
import user.Gestor;
import user.Rol;

@DisplayName("Verificador")
class VerificadorTest {

    @TempDir
    Path directorio;

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
    @DisplayName("El primer uso crea el administrador adm/user")
    void administradorInicial() {
        Verificador verificador = new Verificador(directorio);
        Empleado empleado = verificador.iniciarSesion("adm", "user", Rol.ADMINISTRADOR);
        assertInstanceOf(Gestor.class, empleado);
    }

    @Test
    @DisplayName("El rol seleccionado debe coincidir con el rol registrado")
    void validaRolSeleccionado() {
        MotorCSV csv = new MotorCSV(directorio);
        csv.escribirCSV(csv.rutaDatos("empleado.csv").toString(), List.of(
                new String[]{"ci", "nombre", "rol", "contrasena"},
                new String[]{"CI-1", "Cajero", "CAJERO", "pw"}));
        Verificador verificador = new Verificador(directorio);
        assertEquals(Rol.CAJERO, verificador.iniciarSesion("Cajero", "pw", Rol.CAJERO).getRol());
        assertNull(verificador.iniciarSesion("Cajero", "pw", Rol.ADMINISTRADOR));
        assertNull(verificador.iniciarSesion("Cajero", "incorrecta", Rol.CAJERO));
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
