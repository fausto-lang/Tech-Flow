package com.overbyte.controllers;


import javafx.fxml.FXML;
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

        Empleado empleado = verificador.iniciarSesion(nombre, contrasena, rolSeleccionado);

        if (empleado == null) {
            mensaje.setText(
                "Nombre, contraseña o rol incorrectos."
            );
            campoContrasena.clear();
            campoContrasena.requestFocus();
            return;
        }

        Stage ventana = (Stage) campoNombre.getScene().getWindow();

        MenuController menu = new MenuController();
        menu.mostrarMenu(ventana, empleado);
    }
}