package com.overbyte.controllers;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
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

    private Rol rolSeleccionado;

    private final Verificador verificador = new Verificador();

    @FXML
    private void initialize() {
        rolSeleccionado = null;
        mensaje.setText("");

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
            Stage ventana = (Stage) campoNombre
                .getScene()
                .getWindow();

            Rol rol = empleado.getRol();

            if (rol == Rol.ADMINISTRADOR) {

                FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource(
                        "/com/overbyte/vista/admin.fxml"
                    )
                );

                Parent vistaAdmin = cargador.load();

                AdminController controlador = cargador.getController();
                controlador.setEmpleado(empleado);

                Scene escenaAdmin = new Scene(vistaAdmin, 1200, 800);

                ventana.setScene(escenaAdmin);
                ventana.setTitle("Tech-Flow | Administrador");
                ventana.setMinWidth(1000);
                ventana.setMinHeight(700);
                ventana.setWidth(1200);
                ventana.setHeight(800);
                ventana.centerOnScreen();

            } else if (rol == Rol.CAJERO) {

                FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource(
                        "/com/overbyte/vista/vendedor.fxml"
                    )
                );

                Parent vistaVendedor = cargador.load();

                VendedorController controlador = cargador.getController();
                controlador.setEmpleado(empleado);

                Scene escenaVendedor = new Scene(vistaVendedor, 1100, 750);

                ventana.setScene(escenaVendedor);
                ventana.setTitle("Tech-Flow | Punto de Venta");
                ventana.setMinWidth(900);
                ventana.setMinHeight(600);
                ventana.setWidth(1100);
                ventana.setHeight(750);
                ventana.centerOnScreen();

            } else if (rol == Rol.ALMACENERO) {

                FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource(
                        "/com/overbyte/vista/almacen.fxml"
                    )
                );

                Parent vistaAlmacen = cargador.load();

                AlmacenController controlador = cargador.getController();
                controlador.setEmpleado(empleado);

                Scene escenaAlmacen = new Scene(vistaAlmacen, 1100, 750);

                ventana.setScene(escenaAlmacen);
                ventana.setTitle("Tech-Flow | Almacén");
                ventana.setMinWidth(900);
                ventana.setMinHeight(600);
                ventana.setWidth(1100);
                ventana.setHeight(750);
                ventana.centerOnScreen();

            } else {
                MenuController menu = new MenuController();
                menu.mostrarMenu(ventana, empleado);
            }

        } catch (Exception e) {
            e.printStackTrace();

            javafx.scene.control.Alert alerta =
                 new javafx.scene.control.Alert(
                       javafx.scene.control.Alert.AlertType.ERROR);

            alerta.setTitle("Error al abrir la pantalla");
            alerta.setHeaderText("No se pudo cargar el panel administrativo");
            alerta.setContentText(e.getClass().getSimpleName()
                    + ": " + e.getMessage());

            alerta.showAndWait();
        }
    }
}