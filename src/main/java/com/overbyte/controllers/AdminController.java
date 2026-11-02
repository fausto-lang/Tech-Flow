package com.overbyte.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import user.Empleado;
import user.Gestor;
import services.MotorCSV;

import java.util.List;
import java.util.Locale;

public class AdminController {

    @FXML
    private VBox contenidoCentro;

    @FXML
    private Label lblTituloSeccion;

    private Empleado empleado;
    private Gestor gestor;
    private MotorCSV motorCSV;

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
        this.gestor = new Gestor(
            empleado.getCi(),
            empleado.getNombre(),
            empleado.getContrasena()
        );
        this.motorCSV = new MotorCSV();
        mostrarCaja();
    }

    @FXML
    private void initialize() {
        lblTituloSeccion.setText("Caja");
    }

    /* ===================== SECCIONES ===================== */

    @FXML
    private void mostrarCaja() {
        lblTituloSeccion.setText("Caja");
        contenidoCentro.getChildren().clear();

        List<String[]> caja = motorCSV.leerCSV(motorCSV.rutaDatos("caja.csv").toString());
        boolean abierta = caja.size() > 1
                && caja.get(1).length > 0
                && caja.get(1)[0].equalsIgnoreCase("ABIERTA");

        Label lblEstado = new Label(
            abierta ? "CAJA ABIERTA" : "CAJA CERRADA"
        );
        lblEstado.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: bold; " +
            (abierta ? "-fx-text-fill: #4ade80;" : "-fx-text-fill: #fca5a5;")
        );

        Button btnAccion = new Button(
            abierta ? "CERRAR CAJA" : "ABRIR CAJA"
        );
        btnAccion.setStyle("-fx-padding: 10px 20px; -fx-font-size: 14px;");
        btnAccion.setOnAction(event -> {
            if (abierta) {
                gestor.cerrarCaja();
            } else {
                gestor.abrirCaja();
            }
            mostrarCaja();
        });

        VBox contenedor = new VBox(15, lblEstado, btnAccion);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setPrefHeight(450);

        contenidoCentro.getChildren().add(contenedor);
    }

    @FXML
    private void mostrarInventario() {
        lblTituloSeccion.setText("Inventario");
        contenidoCentro.getChildren().clear();

        List<String[]> filas = motorCSV.leerCSV(motorCSV.rutaDatos("inventario.csv").toString());

        TableView<String[]> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<String[], String> colId = new TableColumn<>("ID Producto");
        colId.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[0]));

        TableColumn<String[], String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[1]));

        TableColumn<String[], String> colMarca = new TableColumn<>("Marca");
        colMarca.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[2]));

        TableColumn<String[], String> colCategoria = new TableColumn<>("Categoria");
        colCategoria.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[3]));

        TableColumn<String[], String> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[7]));

        TableColumn<String[], String> colPrecioVenta = new TableColumn<>("Precio Venta");
        colPrecioVenta.setCellValueFactory(param -> {
            try {
                double p = Double.parseDouble(param.getValue()[6]);
                return new javafx.beans.property.SimpleStringProperty(String.format(Locale.ROOT, "$%.2f", p));
            } catch (NumberFormatException e) {
                return new javafx.beans.property.SimpleStringProperty("$0.00");
            }
        });

        tabla.getColumns().addAll(colId, colNombre, colMarca, colCategoria, colStock, colPrecioVenta);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length < 8 || fila[0].equalsIgnoreCase("idProducto")) {
                continue;
            }
            tabla.getItems().add(fila);
        }

        tabla.setPrefHeight(450);

        ScrollPane scroll = new ScrollPane(tabla);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPrefHeight(450);

        contenidoCentro.getChildren().add(scroll);
    }

    @FXML
    private void mostrarVentas() {
        lblTituloSeccion.setText("Ventas");
        contenidoCentro.getChildren().clear();

        List<String[]> filas = motorCSV.leerCSV(motorCSV.rutaDatos("ventas.csv").toString());

        double totalDia = 0;
        int count = 0;

        TableView<String[]> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<String[], String> colFolio = new TableColumn<>("Folio");
        colFolio.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[0]));

        TableColumn<String[], String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[4]));

        TableColumn<String[], String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(param -> {
            try {
                double t = Double.parseDouble(param.getValue()[9]);
                return new javafx.beans.property.SimpleStringProperty(String.format(Locale.ROOT, "$%.2f", t));
            } catch (NumberFormatException e) {
                return new javafx.beans.property.SimpleStringProperty("$0.00");
            }
        });

        tabla.getColumns().addAll(colFolio, colCliente, colTotal);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length < 11 || !fila[2].equalsIgnoreCase("CONFIRMADA")) {
                continue;
            }
            String fecha = fila[10];
            String hoy = java.time.LocalDate.now().toString();
            if (!fecha.startsWith(hoy)) {
                continue;
            }
            try {
                totalDia += Double.parseDouble(fila[9]);
                count++;
            } catch (NumberFormatException ignored) {}
            tabla.getItems().add(fila);
        }

        Label lblTotal = new Label(
            "Total del dia: " + String.format(Locale.ROOT, "$%.2f", totalDia) + "  |  "
            + count + " ventas"
        );
        lblTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");

        tabla.setPrefHeight(380);

        VBox contenedor = new VBox(15, lblTotal, tabla);
        contenedor.setAlignment(Pos.TOP_LEFT);

        ScrollPane scroll = new ScrollPane(contenedor);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPrefHeight(450);

        contenidoCentro.getChildren().add(scroll);
    }

    @FXML
    private void mostrarEmpleados() {
        lblTituloSeccion.setText("Empleados");
        contenidoCentro.getChildren().clear();

        List<String[]> filas = motorCSV.leerCSV(motorCSV.rutaDatos("empleado.csv").toString());

        TableView<String[]> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<String[], String> colCi = new TableColumn<>("CI");
        colCi.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[0]));

        TableColumn<String[], String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[1]));

        TableColumn<String[], String> colRol = new TableColumn<>("Rol");
        colRol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[2]));

        tabla.getColumns().addAll(colCi, colNombre, colRol);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length < 4) continue;
            tabla.getItems().add(fila);
        }

        tabla.setPrefHeight(450);

        ScrollPane scroll = new ScrollPane(tabla);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPrefHeight(450);

        contenidoCentro.getChildren().add(scroll);
    }

    @FXML
    private void mostrarProductos() {
        lblTituloSeccion.setText("Productos");
        contenidoCentro.getChildren().clear();

        List<String[]> filas = motorCSV.leerCSV(motorCSV.rutaDatos("inventario.csv").toString());

        TableView<String[]> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<String[], String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[0]));

        TableColumn<String[], String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[1]));

        TableColumn<String[], String> colPrecioVenta = new TableColumn<>("Precio Venta");
        colPrecioVenta.setCellValueFactory(param -> {
            try {
                double p = Double.parseDouble(param.getValue()[6]);
                return new javafx.beans.property.SimpleStringProperty(String.format(Locale.ROOT, "$%.2f", p));
            } catch (NumberFormatException e) {
                return new javafx.beans.property.SimpleStringProperty("$0.00");
            }
        });

        TableColumn<String[], String> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[7]));

        tabla.getColumns().addAll(colId, colNombre, colPrecioVenta, colStock);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length < 8 || fila[0].equalsIgnoreCase("idProducto")) continue;
            tabla.getItems().add(fila);
        }

        tabla.setPrefHeight(450);

        ScrollPane scroll = new ScrollPane(tabla);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPrefHeight(450);

        contenidoCentro.getChildren().add(scroll);
    }

    /* ===================== CERRAR SESIÓN ===================== */

    @FXML
    private void cerrarSesion() {
        try {
            FXMLLoader cargador = new FXMLLoader(
                getClass().getResource("/com/overbyte/vista/login.fxml")
            );
            Parent vistaLogin = cargador.load();
            Scene escenaLogin = new Scene(vistaLogin, 800, 650);

            Stage ventana = (Stage) contenidoCentro.getScene().getWindow();
            ventana.setScene(escenaLogin);
            ventana.setTitle("Tech-Flow | Iniciar sesion");
            ventana.setMinWidth(600);
            ventana.setMinHeight(400);
            ventana.setWidth(800);
            ventana.setHeight(650);
            ventana.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
