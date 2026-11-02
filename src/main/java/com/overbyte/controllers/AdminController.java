package com.overbyte.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import user.Empleado;
import user.Gestor;
import services.MotorCSV;
import models.Producto;

import java.util.List;

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
        contenedor.setStyle("-fx-alignment: CENTER;");

        contenidoCentro.getChildren().add(contenedor);
    }

    @FXML
    private void mostrarInventario() {
        lblTituloSeccion.setText("Inventario");
        contenidoCentro.getChildren().clear();

        List<String[]> filas = motorCSV.leerCSV(motorCSV.rutaDatos("inventario.csv").toString());

        VBox tabla = new VBox(8);
        tabla.setStyle("-fx-alignment: CENTER_LEFT;");

        Label lblCabecera = new Label("ID Producto  |  Nombre  |  Marca  |  Categoría  |  Stock");
        lblCabecera.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        tabla.getChildren().add(lblCabecera);

        Separator separador = new Separator();
        tabla.getChildren().add(separador);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length < 8 || fila[0].equalsIgnoreCase("idProducto")) {
                continue;
            }
            HBox filaH = new HBox(15);
            filaH.setAlignment(Pos.CENTER_LEFT);

            Label id = new Label(fila[0]);
            id.setPrefWidth(100);
            Label nombre = new Label(fila[1]);
            nombre.setPrefWidth(150);
            Label marca = new Label(fila[2]);
            marca.setPrefWidth(100);
            Label categoria = new Label(fila[3]);
            categoria.setPrefWidth(100);
            Label stock = new Label(fila[7]);
            stock.setPrefWidth(80);

            int stockNum;
            try {
                stockNum = Integer.parseInt(fila[7]);
                if (stockNum <= 5) {
                    stock.setStyle("-fx-text-fill: #fca5a5; -fx-font-weight: bold;");
                }
            } catch (NumberFormatException ignored) {}

            filaH.getChildren().addAll(id, nombre, marca, categoria, stock);
            tabla.getChildren().add(filaH);
        }

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

        VBox lista = new VBox(8);
        lista.setStyle("-fx-alignment: CENTER_LEFT;");

        Label lblResumen = new Label("Ventas confirmadas hoy:");
        lblResumen.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        lista.getChildren().add(lblResumen);

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

            HBox filaH = new HBox(15);
            filaH.setAlignment(Pos.CENTER_LEFT);
            Label folio = new Label(fila[0]);
            folio.setPrefWidth(100);
            Label cliente = new Label(fila[4]);
            cliente.setPrefWidth(150);
            Label total = new Label(fila[9] + " BS");
            total.setPrefWidth(100);
            filaH.getChildren().addAll(folio, cliente, total);
            lista.getChildren().add(filaH);
        }

        Label lblTotal = new Label(
            "Total del día: " + String.format("%.2f", totalDia) + " BS  |  "
            + count + " ventas"
        );
        lblTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");

        VBox contenedor = new VBox(15, lblTotal, lista);
        contenedor.setStyle("-fx-alignment: CENTER_LEFT;");

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

        VBox lista = new VBox(8);
        lista.setStyle("-fx-alignment: CENTER_LEFT;");

        Label lblCabecera = new Label("CI  |  Nombre  |  Rol");
        lblCabecera.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        lista.getChildren().add(lblCabecera);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length < 4) continue;
            HBox filaH = new HBox(15);
            filaH.setAlignment(Pos.CENTER_LEFT);
            Label ci = new Label(fila[0]);
            ci.setPrefWidth(100);
            Label nombre = new Label(fila[1]);
            nombre.setPrefWidth(150);
            Label rol = new Label(fila[2]);
            rol.setPrefWidth(120);
            filaH.getChildren().addAll(ci, nombre, rol);
            lista.getChildren().add(filaH);
        }

        ScrollPane scroll = new ScrollPane(lista);
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

        VBox lista = new VBox(8);
        lista.setStyle("-fx-alignment: CENTER_LEFT;");

        Label lblCabecera = new Label("ID  |  Nombre  |  Precio Venta  |  Stock");
        lblCabecera.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        lista.getChildren().add(lblCabecera);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length < 8 || fila[0].equalsIgnoreCase("idProducto")) continue;
            HBox filaH = new HBox(15);
            filaH.setAlignment(Pos.CENTER_LEFT);
            Label id = new Label(fila[0]);
            id.setPrefWidth(100);
            Label nombre = new Label(fila[1]);
            nombre.setPrefWidth(150);
            Label precio = new Label(fila[6]);
            precio.setPrefWidth(100);
            Label stock = new Label(fila[7]);
            stock.setPrefWidth(80);
            filaH.getChildren().addAll(id, nombre, precio, stock);
            lista.getChildren().add(filaH);
        }

        ScrollPane scroll = new ScrollPane(lista);
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
            ventana.setTitle("Tech-Flow | Iniciar sesión");
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
