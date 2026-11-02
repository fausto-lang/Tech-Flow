package com.overbyte.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Producto;
import user.Empleado;
import user.Almacen;
import services.MotorCSV;

import java.util.List;

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

    @FXML
    private VBox tablaInventario;

    private Empleado empleado;
    private Almacen almacen;
    private MotorCSV motorCSV;

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
        this.almacen = new Almacen(
            empleado.getCi(),
            empleado.getNombre(),
            empleado.getContrasena()
        );
        this.motorCSV = new MotorCSV();
        cargarInventario();
    }

    @FXML
    private void initialize() {
        mensaje.setText("");
        if (empleado != null) {
            etiquetaAlmacen.setText("Almacenero: " + empleado.getNombre());
        }
    }

    /* ===================== INVENTARIO ===================== */

    private void cargarInventario() {
        tablaInventario.getChildren().clear();

        List<String[]> filas = motorCSV.leerCSV(motorCSV.rutaDatos("inventario.csv").toString());

        // Cabecera de la tabla
        HBox cabecera = new HBox(15);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        cabecera.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2563eb;");

        Label colId = new Label("ID");
        colId.setPrefWidth(80);
        Label colNombre = new Label("Nombre");
        colNombre.setPrefWidth(160);
        Label colMarca = new Label("Marca");
        colMarca.setPrefWidth(100);
        Label colCategoria = new Label("Categoría");
        colCategoria.setPrefWidth(100);
        Label colPrecioE = new Label("P. Entrada");
        colPrecioE.setPrefWidth(90);
        Label colPrecioV = new Label("P. Venta");
        colPrecioV.setPrefWidth(80);
        Label colStock = new Label("Stock");
        colStock.setPrefWidth(70);

        cabecera.getChildren().addAll(colId, colNombre, colMarca, colCategoria, colPrecioE, colPrecioV, colStock);
        tablaInventario.getChildren().add(cabecera);

        Separator sep = new Separator();
        tablaInventario.getChildren().add(sep);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length < 8 || fila[0].equalsIgnoreCase("idProducto")) {
                continue;
            }
            HBox filaH = new HBox(15);
            filaH.setAlignment(Pos.CENTER_LEFT);
            filaH.setStyle("-fx-padding: 4 0;");

            Label id = new Label(fila[0]);
            id.setPrefWidth(80);
            Label nombre = new Label(fila[1]);
            nombre.setPrefWidth(160);
            Label marca = new Label(fila[2]);
            marca.setPrefWidth(100);
            Label categoria = new Label(fila[3]);
            categoria.setPrefWidth(100);
            Label precioE = new Label(fila[5]);
            precioE.setPrefWidth(90);
            Label precioV = new Label(fila[6]);
            precioV.setPrefWidth(80);
            Label stock = new Label(fila[7]);
            stock.setPrefWidth(70);

            // Resaltar stock bajo
            try {
                int s = Integer.parseInt(fila[7]);
                if (s <= 5) {
                    stock.setStyle("-fx-text-fill: #fca5a5; -fx-font-weight: bold;");
                }
            } catch (NumberFormatException ignored) {}

            filaH.getChildren().addAll(id, nombre, marca, categoria, precioE, precioV, stock);
            tablaInventario.getChildren().add(filaH);
        }
    }

    /* ===================== FORMULARIOS ===================== */

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
            cargarInventario();
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
            cargarInventario();
        } else {
            mensaje.setText("No se pudo reabastecer. Verifica que el código exista.");
        }
    }

    /* ===================== IMPORTAR ENTRADAS ===================== */

    @FXML
    private void importarEntradasDesdeArchivo() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Importar entradas");
        dialog.setHeaderText("Ingresa la ruta del archivo CSV/TXT de recepciones:");
        dialog.setContentText("Ruta:");
        dialog.showAndWait().ifPresent(ruta -> {
            boolean ok = almacen.ingresarEntradaDesdeArchivo(ruta);
            if (ok) {
                mensaje.setText("-fx-text-fill: #4ade80;");
                mensaje.setText("Entradas importadas correctamente.");
                cargarInventario();
            } else {
                mensaje.setText("No se pudo importar el archivo. Verifica el formato y columnas.");
            }
        });
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

            Stage ventana = (Stage) campoId.getScene().getWindow();
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
