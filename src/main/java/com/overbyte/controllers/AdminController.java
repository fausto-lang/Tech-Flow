package com.overbyte.controllers;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminController {

    @FXML
    private void cerrarSesion(ActionEvent event) {

        try {
            FXMLLoader cargador = new FXMLLoader(
                getClass().getResource(
                    "/com/overbyte/vista/login.fxml"
                )
            );

            Parent vistaLogin = cargador.load();

            Scene escenaLogin = new Scene(vistaLogin, 800, 650);

            Node origen = (Node) event.getSource();

            Stage ventana = (Stage) origen
                .getScene()
                .getWindow();

            ventana.setScene(escenaLogin);
            ventana.setTitle("Tech-Flow | Iniciar sesión");

            ventana.setMinWidth(600);
            ventana.setMinHeight(400);

            ventana.setWidth(800);
            ventana.setHeight(650);

            ventana.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}