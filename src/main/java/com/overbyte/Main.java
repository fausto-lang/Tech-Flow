package com.overbyte;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

/** Punto de entrada GUI: una sola ventana desde el inicio. */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Navegador.iniciar(stage);
        Navegador.conectarEscape(Navegador::cerrarOverlay);
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/com/overbyte/vista/login.fxml"));
            Navegador.mostrar(login, "Iniciar sesión");
        } catch (Exception e) {
            e.printStackTrace();
            Navegador.avisar("Error al abrir el login", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
