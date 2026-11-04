package com.overbyte.controllers;

import com.overbyte.Navegador;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import user.Almacen;
import user.Empleado;
import user.Gestor;
import user.Vendedor;

public class MenuController {

    public void mostrarMenu(Stage ventana, Empleado empleado) {
        Label titulo = new Label("TECH-FLOW");
        titulo.setStyle("-fx-font-size: 30px;-fx-font-weight: bold;-fx-text-fill: #a78bfa;");
        Label bienvenida = new Label("Bienvenido, " + empleado.getNombre());
        bienvenida.setStyle("-fx-font-size: 18px;-fx-text-fill: white;");
        Label rol = new Label("Rol: " + empleado.getRol());
        rol.setStyle("-fx-text-fill: #cbd5e1;");
        Label descripcion = new Label(obtenerDescripcion(empleado));
        descripcion.setStyle("-fx-text-fill: white;");

        Button cerrarSesion = new Button("Cerrar sesión");
        cerrarSesion.setOnAction(event -> Navegador.irAlLogin());

        VBox contenido = new VBox(20, titulo, bienvenida, rol, descripcion, cerrarSesion);
        contenido.setAlignment(Pos.CENTER);
        contenido.setPadding(new Insets(30));
        contenido.setStyle("-fx-background-color: #0f172a;");
        Navegador.mostrar(contenido, String.valueOf(empleado.getRol()));
    }

    private String obtenerDescripcion(Empleado empleado) {
        if (empleado instanceof Gestor) return "Panel de administración";
        if (empleado instanceof Vendedor) return "Panel de ventas";
        if (empleado instanceof Almacen) return "Panel de inventario y almacén";
        return "Panel principal";
    }
}
