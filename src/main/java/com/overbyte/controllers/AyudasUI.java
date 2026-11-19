package com.overbyte.controllers;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.util.Duration;

/** Animaciones de hover compartidas por los controladores. */
final class AyudasUI {

    private AyudasUI() {}

    /** Hover con escala leve y desplazamiento a la derecha (botones de navegación). */
    static void animarNavegacion(Button boton) {
        ScaleTransition escala = new ScaleTransition(Duration.millis(140), boton);
        TranslateTransition desplazamiento =
                new TranslateTransition(Duration.millis(140), boton);
        boton.setOnMouseEntered(event -> {
            escala.stop();
            escala.setToX(1.015);
            escala.setToY(1.015);
            escala.playFromStart();
            desplazamiento.stop();
            desplazamiento.setToX(3);
            desplazamiento.playFromStart();
        });
        boton.setOnMouseExited(event -> {
            escala.stop();
            escala.setToX(1);
            escala.setToY(1);
            escala.playFromStart();
            desplazamiento.stop();
            desplazamiento.setToX(0);
            desplazamiento.playFromStart();
        });
    }

    /** Hover con solo escala (botones de rol del login). */
    static void animarHover(Button boton) {
        ScaleTransition animacion = new ScaleTransition(Duration.millis(130), boton);
        boton.setOnMouseEntered(event -> {
            animacion.setToX(1.04);
            animacion.setToY(1.04);
            animacion.playFromStart();
        });
        boton.setOnMouseExited(event -> {
            animacion.setToX(1);
            animacion.setToY(1);
            animacion.playFromStart();
        });
    }
}
