package com.overbyte.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
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

    private Rol rolSeleccionado;

    private final Verificador verificador = new Verificador();

    @FXML
    private void initialize() {
        rolSeleccionado = null;
        mensaje.setText("");

        campoNombre.setOnAction(event -> campoContrasena.requestFocus());
        campoContrasena.setOnAction(event -> iniciarSesion());
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

            if (empleado.getRol() == Rol.ADMINISTRADOR) {

                FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource(
                        "/com/overbyte/vista/admin.fxml"
                    )
                );

                Parent vistaAdmin = cargador.load();

                Scene escenaAdmin = new Scene(vistaAdmin, 1400, 900);

                ventana.setScene(escenaAdmin);
                ventana.setTitle("Tech-Flow | Administrador");
                ventana.setMinWidth(1100);
                ventana.setMinHeight(700);
                ventana.setWidth(1400);
                ventana.setHeight(900);
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