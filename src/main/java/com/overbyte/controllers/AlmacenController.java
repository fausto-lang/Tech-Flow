package com.overbyte.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Producto;
import user.Empleado;
import user.Almacen;

public class AlmacenController {

    @FXML
    private TextField campoId;

    @FXML
    private TextField campoNombre;

    @FXML
    private TextField campoMarca;

    @FXML
    private TextField campoCategoria;

    @FXML
    private TextField campoDescripcion;

    @FXML
    private TextField campoPrecioEntrada;

    @FXML
    private TextField campoPrecioVenta;

    @FXML
    private TextField campoStock;

    @FXML
    private TextField campoCodigoExistente;

    @FXML
    private TextField campoCantidadExistente;

    @FXML
    private Label etiquetaAlmacen;

    @FXML
    private Label mensaje;

    private Empleado empleado;
    private Almacen almacen;

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
        this.almacen = new Almacen(
            empleado.getCi(),
            empleado.getNombre(),
            empleado.getContrasena()
        );
    }

    @FXML
    private void initialize() {
        mensaje.setText("");
        if (empleado != null) {
            etiquetaAlmacen.setText("Almacenero: " + empleado.getNombre());
        }
    }

    @FXML
    private void ingresarProductoNuevo() {
        mensaje.setText("");
        String id = campoId.getText().trim();
        String nombre = campoNombre.getText().trim();
        String marca = campoMarca.getText().trim();
        String categoria = campoCategoria.getText().trim();
        String descripcion = campoDescripcion.getText().trim();

        if (id.isEmpty() || nombre.isEmpty() || marca.isEmpty()
                || categoria.isEmpty() || descripcion.isEmpty()) {
            mensaje.setText("Completa todos los campos del producto.");
            return;
        }

        double precioEntrada;
        double precioVenta;
        int stock;
        try {
            precioEntrada = Double.parseDouble(campoPrecioEntrada.getText().trim());
            precioVenta = Double.parseDouble(campoPrecioVenta.getText().trim());
            stock = Integer.parseInt(campoStock.getText().trim());
            if (precioEntrada < 0 || precioVenta < 0 || stock < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            mensaje.setText("Precios y stock deben ser números válidos (>= 0).");
            return;
        }

        Producto producto = new Producto(id, nombre, precioEntrada, precioVenta,
                descripcion, stock, marca, categoria);

        boolean ok = almacen.ingresarProductoNuevo(producto);
        if (ok) {
            mensaje.setText("-fx-text-fill: #4ade80;");
            mensaje.setText("Producto ingresado correctamente.");
            limpiarCamposProducto();
        } else {
            mensaje.setText("No se pudo ingresar el producto. Verifica que el ID no exista.");
        }
    }

    @FXML
    private void ingresarProductoExistente() {
        mensaje.setText("");
        String codigo = campoCodigoExistente.getText().trim();
        String cantidadTexto = campoCantidadExistente.getText().trim();

        if (codigo.isEmpty()) {
            mensaje.setText("Escribe el código del producto.");
            campoCodigoExistente.requestFocus();
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadTexto);
            if (cantidad <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            mensaje.setText("La cantidad debe ser un número mayor a cero.");
            campoCantidadExistente.requestFocus();
            return;
        }

        boolean ok = almacen.ingresarProductoExistente(codigo, cantidad);
        if (ok) {
            mensaje.setText("-fx-text-fill: #4ade80;");
            mensaje.setText("Stock reabastecido correctamente.");
            campoCodigoExistente.clear();
            campoCantidadExistente.clear();
        } else {
            mensaje.setText("No se pudo reabastecer. Verifica que el código exista.");
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

            Stage ventana = (Stage) campoId.getScene().getWindow();
            ventana.setScene(escenaLogin);
            ventana.setTitle("Tech-Flow | Iniciar sesión");
            ventana.setWidth(800);
            ventana.setHeight(650);
            ventana.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void limpiarCamposProducto() {
        campoId.clear();
        campoNombre.clear();
        campoMarca.clear();
        campoCategoria.clear();
        campoDescripcion.clear();
        campoPrecioEntrada.clear();
        campoPrecioVenta.clear();
        campoStock.clear();
    }
}
