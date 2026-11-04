package com.overbyte;

import java.util.function.Consumer;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Ventana unica: un solo Stage, un solo tamano fijo, una sola Scene.
 * Las vistas (login/admin/vendedor/almacen) son Parents que se
 * intercambian dentro del mismo contenedor con transicion fade+slide.
 * Los formularios/alertas son overlays dentro de la misma ventana,
 * nunca Dialog/Alert flotantes (asi bspwm lo trata como un solo tile).
 */
public final class Navegador {

    public static final double ANCHO = 1100;
    public static final double ALTO = 620;
    public static final String ESTILOS = "/com/overbyte/styles/estilos.css";

    private static Stage ventana;
    private static StackPane raiz;
    private static StackPane contenedorVista;
    private static StackPane capaOverlay;
    private static Parent vistaActual;
    private static boolean temaOscuro;

    private Navegador() {}

    public static void iniciar(Stage stage) {
        ventana = stage;
        raiz = new StackPane();
        contenedorVista = new StackPane();
        capaOverlay = new StackPane();
        capaOverlay.setVisible(false);
        capaOverlay.setManaged(false);
        raiz.getChildren().addAll(contenedorVista, capaOverlay);
        setTemaOscuro(temaOscuro);
        Scene escena = new Scene(raiz, ANCHO, ALTO);
        escena.getStylesheets().add(
            Navegador.class.getResource(ESTILOS).toExternalForm());
        ventana.setScene(escena);
        ventana.setTitle("Tech-Flow");
        ventana.setMinWidth(960);
        ventana.setMinHeight(540);
        ventana.setWidth(ANCHO);
        ventana.setHeight(ALTO);
        ventana.setResizable(true);
        ventana.show();
    }

    public static Stage ventana() { return ventana; }
    public static StackPane raiz() { return raiz; }

    public static boolean esTemaOscuro() { return temaOscuro; }

    public static void setTemaOscuro(boolean oscuro) {
        temaOscuro = oscuro;
        if (raiz == null) return;
        raiz.getStyleClass().remove("tema-oscuro");
        if (oscuro) raiz.getStyleClass().add("tema-oscuro");
    }

    public static boolean alternarTema() {
        setTemaOscuro(!temaOscuro);
        return temaOscuro;
    }

    /** Vuelve al login dentro de la misma ventana. */
    public static void irAlLogin() {
        try {
            Parent login = javafx.fxml.FXMLLoader.load(Navegador.class.getResource("/com/overbyte/vista/login.fxml"));
            cerrarOverlay();
            mostrar(login, "Iniciar sesión");
        } catch (Exception e) {
            e.printStackTrace();
            avisar("Error al abrir el login", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /** Muestra una vista dentro de la ventana unica, con fade+slide. */
    public static void mostrar(Parent vista, String titulo) {
        if (vista instanceof javafx.scene.layout.Region r) {
            r.setMaxWidth(Double.MAX_VALUE);
            r.setMaxHeight(Double.MAX_VALUE);
        }
        if (titulo != null) ventana.setTitle("Tech-Flow | " + titulo);
        Parent anterior = vistaActual;
        vistaActual = vista;
        if (anterior == null) {
            contenedorVista.getChildren().setAll(vista);
            animarEntrada(vista);
            return;
        }
        FadeTransition salida = new FadeTransition(Duration.millis(140), anterior);
        salida.setFromValue(1);
        salida.setToValue(0);
        salida.setOnFinished(e -> {
            contenedorVista.getChildren().setAll(vista);
            animarEntrada(vista);
        });
        salida.play();
    }

    private static void animarEntrada(Parent vista) {
        if (vista.getOpacity() != 1.0) vista.setOpacity(0);
        vista.setTranslateY(14);
        FadeTransition entrada = new FadeTransition(Duration.millis(260), vista);
        entrada.setFromValue(0);
        entrada.setToValue(1);
        TranslateTransition desliza = new TranslateTransition(Duration.millis(260), vista);
        desliza.setFromY(14);
        desliza.setToY(0);
        entrada.play();
        desliza.play();
    }

    /** Overlay interno: dim + tarjeta centrada dentro de la misma ventana. */
    public static void mostrarOverlay(Parent contenido) {
        StackPane fondo = new StackPane();
        fondo.getStyleClass().add("overlay-fondo");
        fondo.setAlignment(Pos.CENTER);
        fondo.getChildren().add(contenido);
        fondo.setOnMouseClicked(e -> cerrarOverlay());
        contenido.setOnMouseClicked(e -> e.consume());
        capaOverlay.getChildren().setAll(fondo);
        capaOverlay.setVisible(true);
        capaOverlay.setManaged(true);
        fondo.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(160), fondo);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    public static void cerrarOverlay() {
        if (capaOverlay == null) return;
        capaOverlay.getChildren().clear();
        capaOverlay.setVisible(false);
        capaOverlay.setManaged(false);
    }

    public static boolean hayOverlay() {
        return capaOverlay != null && capaOverlay.isVisible();
    }

    /** Tarjeta generica para formularios overlay. */
    public static VBox tarjeta(String titulo, String subtitulo, Parent... cuerpo) {
        Label t = new Label(titulo);
        t.getStyleClass().add("titulo-formulario");
        Label s = new Label(subtitulo);
        s.getStyleClass().add("texto-secundario");
        s.setWrapText(true);
        VBox tarjeta = new VBox(12);
        tarjeta.getStyleClass().addAll("tarjeta-login", "overlay-tarjeta");
        tarjeta.getChildren().addAll(t, s);
        if (cuerpo != null) tarjeta.getChildren().addAll(cuerpo);
        return tarjeta;
    }

    /** Tarjeta compacta para overlays de poco contenido (ej. caja). */
    public static VBox tarjetaCompacta(String titulo, String subtitulo, Parent... cuerpo) {
        VBox tarjeta = tarjeta(titulo, subtitulo, cuerpo);
        tarjeta.getStyleClass().add("overlay-tarjeta--compacta");
        tarjeta.setSpacing(8);
        return tarjeta;
    }

    /** Aviso inline (reemplaza Alert.showAndWait). */
    public static void avisar(String titulo, String mensaje) {
        Label m = new Label(mensaje);
        m.setWrapText(true);
        m.getStyleClass().add("texto-secundario");
        Button ok = new Button("Entendido");
        ok.getStyleClass().add("boton-principal");
        ok.setMaxWidth(Double.MAX_VALUE);
        ok.setOnAction(e -> cerrarOverlay());
        mostrarOverlay(tarjetaCompacta(titulo, "", m, ok));
    }

    /** Confirmacion inline (reemplaza Alert CONFIRMATION). */
    public static void confirmar(String titulo, String mensaje, Runnable alConfirmar) {
        Label m = new Label(mensaje);
        m.setWrapText(true);
        Button ok = new Button("Confirmar");
        ok.getStyleClass().add("boton-peligro");
        Button cancelar = new Button("Cancelar");
        cancelar.getStyleClass().add("boton-rol");
        ok.setOnAction(e -> { cerrarOverlay(); alConfirmar.run(); });
        cancelar.setOnAction(e -> cerrarOverlay());
        javafx.scene.layout.HBox botones = new javafx.scene.layout.HBox(10, ok, cancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);
        mostrarOverlay(tarjetaCompacta(titulo, "", m, botones));
    }

    /** Pide un texto inline (reemplaza TextInputDialog). */
    public static void pedirTexto(String titulo, String encabezado, String prompt, Consumer<String> alAceptar) {
        TextField campo = new TextField();
        campo.setPromptText(prompt);
        campo.getStyleClass().add("campo");
        Button ok = new Button("Aceptar");
        ok.getStyleClass().add("boton-principal");
        Button cancelar = new Button("Cancelar");
        cancelar.getStyleClass().add("boton-rol");
        ok.setOnAction(e -> { String v = campo.getText(); cerrarOverlay(); alAceptar.accept(v); });
        campo.setOnAction(e -> ok.fire());
        cancelar.setOnAction(e -> cerrarOverlay());
        javafx.scene.layout.HBox botones = new javafx.scene.layout.HBox(10, ok, cancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);
        mostrarOverlay(tarjeta(titulo, encabezado, campo, botones));
        campo.requestFocus();
    }

    /** Accion al presionar ESC. */
    public static void conectarEscape(Runnable accion) {
        ventana.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE && hayOverlay()) {
                accion.run();
            }
        });
    }
}
