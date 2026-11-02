package com.overbyte;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage ventana;

    @Override
    public void start(Stage stage) {
        ventana = stage;

        Label titulo = new Label("TECH-FLOW");
        titulo.setStyle(
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2563eb;"
        );

        Label subtitulo = new Label(
            "Sistema de gestión de inventario y ventas"
        );
        subtitulo.setStyle("-fx-font-size: 14px;");

        Label indicacion = new Label("Pulsa cualquier tecla para continuar");
        indicacion.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #64748b;"
        );

        VBox contenido = new VBox(12, titulo, subtitulo, indicacion);
        contenido.setAlignment(Pos.CENTER);
        contenido.setPadding(new Insets(30));
        contenido.setStyle("-fx-background-color: #f1f5f9;");

        Scene escena = new Scene(contenido, 800, 500);

        escena.setOnKeyPressed(event -> mostrarLogin());

        ventana.setTitle("Tech-Flow");
        ventana.setScene(escena);
        ventana.setMinWidth(600);
        ventana.setMinHeight(400);
        ventana.show();
        contenido.requestFocus();
    }

    private void mostrarLogin() {
    try {
        javafx.fxml.FXMLLoader cargador =
            new javafx.fxml.FXMLLoader(
                getClass().getResource(
                    "/com/overbyte/vista/login.fxml"
                )
            );

        javafx.scene.Parent raiz = cargador.load();

        Scene escenaLogin = new Scene(raiz, 800, 650);

        ventana.setScene(escenaLogin);
        ventana.setTitle("Tech-Flow | Iniciar sesión");
        ventana.centerOnScreen();

    } catch (Exception e) {
        e.printStackTrace();

        javafx.scene.control.Alert alerta =
            new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
            );

        alerta.setTitle("Error al abrir el login");
        alerta.setHeaderText("No se pudo cargar la pantalla");
        alerta.setContentText(
            "Revisa login.fxml, estilos.css y LoginController.java. " +
            "Consulta la terminal para ver el error exacto."
        );

        alerta.showAndWait();
    }
}

    public static void main(String[] args) {
        launch(args);
    }
}