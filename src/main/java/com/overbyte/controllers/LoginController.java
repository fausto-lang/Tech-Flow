package com.overbyte.controllers;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import com.overbyte.Navegador;
import services.Verificador;
import user.Empleado;
import user.Rol;

public class LoginController {

    @FXML
    private TextField campoNombre;

    @FXML
    private PasswordField campoContrasena;

    @FXML
    private Label etiquetaRol;

    @FXML
    private Label mensaje;

    @FXML
    private Button btnAdministrador;

    @FXML
    private Button btnCajero;

    @FXML
    private Button btnAlmacen;

    @FXML
    private Button btnIniciarSesion;

    @FXML
    private Button btnTema;

    private Rol rolSeleccionado;

    private final Verificador verificador = new Verificador();

    @FXML
    private void initialize() {
        rolSeleccionado = null;
        mensaje.setText("");
        sincronizarBotonTema();

        campoNombre.setOnAction(event -> btnAdministrador.requestFocus());
        campoContrasena.setOnAction(event -> iniciarSesion());

        configurarBotonRol(btnAdministrador, Rol.ADMINISTRADOR, "Administrador");
        configurarBotonRol(btnCajero, Rol.CAJERO, "Cajero");
        configurarBotonRol(btnAlmacen, Rol.ALMACENERO, "Almacén");
        configurarAnimacionHover(btnAdministrador);
        configurarAnimacionHover(btnCajero);
        configurarAnimacionHover(btnAlmacen);
        configurarAnimacionHover(btnIniciarSesion);
    }

    @FXML
    private void alternarTema() {
        Navegador.alternarTema();
        sincronizarBotonTema();
    }

    private void sincronizarBotonTema() {
        if (btnTema != null) {
            btnTema.setText(Navegador.esTemaOscuro() ? "☀" : "🌙");
        }
    }

    @FXML
    private void seleccionarAdministrador() {
        seleccionarRol(Rol.ADMINISTRADOR, "Administrador");
    }

    @FXML
    private void seleccionarCajero() {
        seleccionarRol(Rol.CAJERO, "Cajero");
    }

    @FXML
    private void seleccionarAlmacen() {
        seleccionarRol(Rol.ALMACENERO, "Almacén");
    }

    private void seleccionarRol(Rol rol, String nombreRol) {
        rolSeleccionado = rol;
        etiquetaRol.setText("Rol seleccionado: " + nombreRol);
        mensaje.setText("");
        actualizarEstiloRol(btnAdministrador, rol == Rol.ADMINISTRADOR);
        actualizarEstiloRol(btnCajero, rol == Rol.CAJERO);
        actualizarEstiloRol(btnAlmacen, rol == Rol.ALMACENERO);
        campoContrasena.requestFocus();
    }

    private void configurarBotonRol(Button boton, Rol rol, String nombreRol) {
        boton.addEventHandler(KeyEvent.KEY_PRESSED, evento -> {
            if (evento.getCode() == KeyCode.ENTER) {
                seleccionarRol(rol, nombreRol);
                evento.consume();
            }
        });
    }

    private void actualizarEstiloRol(Button boton, boolean seleccionado) {
        if (seleccionado) {
            if (!boton.getStyleClass().contains("boton-rol-activo")) {
                boton.getStyleClass().add("boton-rol-activo");
            }
        } else {
            boton.getStyleClass().remove("boton-rol-activo");
        }
    }

    private void configurarAnimacionHover(Button boton) {
        ScaleTransition animacion = new ScaleTransition(Duration.millis(130), boton);
        boton.setOnMouseEntered(event -> {
            animacion.setToX(1.04);
            animacion.setToY(1.04);
            animacion.playFromStart();
        });
        boton.setOnMouseExited(event -> {
            animacion.setToX(1);
            animacion.setToY(1);
            animacion.playFromStart();
        });
    }

    @FXML
    private void iniciarSesion() {
        String nombre = campoNombre.getText().trim();
        String contrasena = campoContrasena.getText();

        if (nombre.isEmpty()) {
            mensaje.setText("Por favor, escribe tu nombre de usuario.");
            campoNombre.requestFocus();
            return;
        }

        if (rolSeleccionado == null) {
            mensaje.setText("Selecciona un rol para continuar.");
            return;
        }

        if (contrasena == null || contrasena.isEmpty()) {
            mensaje.setText("Por favor, escribe tu contraseña.");
            campoContrasena.requestFocus();
            return;
        }

        Empleado empleado = verificador.iniciarSesion(
            nombre,
            contrasena,
            rolSeleccionado
        );

        if (empleado == null) {
            mensaje.setText("Nombre, contraseña o rol incorrectos.");
            campoContrasena.clear();
            campoContrasena.requestFocus();
            return;
        }

        abrirVistaSegunRol(empleado);
    }

    private void abrirVistaSegunRol(Empleado empleado) {
        try {
            String fxml;
            String titulo;
            Rol rol = empleado.getRol();

            if (rol == Rol.ADMINISTRADOR) {
                fxml = "/com/overbyte/vista/admin.fxml";
                titulo = "Administrador";
            } else if (rol == Rol.CAJERO) {
                fxml = "/com/overbyte/vista/vendedor.fxml";
                titulo = "Punto de Venta";
            } else if (rol == Rol.ALMACENERO) {
                fxml = "/com/overbyte/vista/almacen.fxml";
                titulo = "Almacén";
            } else {
                Navegador.avisar("Rol no soportado", "No se pudo abrir el panel: " + rol);
                return;
            }

            FXMLLoader cargador = new FXMLLoader(getClass().getResource(fxml));
            Parent vista = cargador.load();
            Object controlador = cargador.getController();
            if (controlador instanceof AdminController admin) admin.setEmpleado(empleado);
            else if (controlador instanceof VendedorController vendedor) vendedor.setEmpleado(empleado);
            else if (controlador instanceof AlmacenController almacen) almacen.setEmpleado(empleado);

            Navegador.cerrarOverlay();
            Navegador.mostrar(vista, titulo);

        } catch (Exception e) {
            e.printStackTrace();
            Navegador.avisar("Error al abrir la pantalla",
                    e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}