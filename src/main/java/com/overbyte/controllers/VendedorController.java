package com.overbyte.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import user.Empleado;
import user.Vendedor;

public class VendedorController {

    @FXML
    private TextField campoCodigo;

    @FXML
    private TextField campoCantidad;

    @FXML
    private TextField campoCliente;

    @FXML
    private Label etiquetaVendedor;

    @FXML
    private Label mensaje;

    private Empleado empleado;
    private Vendedor vendedor;

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
        this.vendedor = new Vendedor(
            empleado.getCi(),
            empleado.getNombre(),
            empleado.getContrasena()
        );
    }

    @FXML
    private void initialize() {
        mensaje.setText("");
        if (empleado != null) {
            etiquetaVendedor.setText("Vendedor: " + empleado.getNombre());
        }
    }

    @FXML
    private void venderProducto() {
        mensaje.setText("");
        String codigo = campoCodigo.getText().trim();
        String nombreCliente = campoCliente.getText().trim();
        String cantidadTexto = campoCantidad.getText().trim();

        if (codigo.isEmpty()) {
            mensaje.setText("Escribe el código del producto.");
            campoCodigo.requestFocus();
            return;
        }
        if (nombreCliente.isEmpty()) {
            nombreCliente = "Consumidor final";
        }
        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadTexto);
            if (cantidad <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            mensaje.setText("La cantidad debe ser un número mayor a cero.");
            campoCantidad.requestFocus();
            return;
        }

        boolean ok = vendedor.venderProducto(codigo, nombreCliente, cantidad);
        if (ok) {
            mensaje.setText("-fx-text-fill: #4ade80;");
            mensaje.setText("Venta registrada correctamente.");
            campoCodigo.clear();
            campoCantidad.clear();
            campoCliente.clear();
        } else {
            mensaje.setText("No se pudo completar la venta. Verifica el código, el stock y que la caja esté abierta.");
        }
    }

    @FXML
    private void cerrarSesion() {
        try {
            FXMLLoader cargador = new FXMLLoader(
                getClass().getResource("/com/overbyte/vista/login.fxml")
            );
            Parent vistaLogin = cargador.load();
            Scene escenaLogin = new Scene(vistaLogin, 800, 650);

            Stage ventana = (Stage) campoCodigo.getScene().getWindow();
            ventana.setScene(escenaLogin);
            ventana.setTitle("Tech-Flow | Iniciar sesión");
            ventana.setWidth(800);
            ventana.setHeight(650);
            ventana.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
