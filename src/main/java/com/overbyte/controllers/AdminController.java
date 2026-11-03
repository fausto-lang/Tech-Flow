package com.overbyte.controllers;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import user.Empleado;
import user.Gestor;
import user.Rol;
import models.Producto;
import models.Proveedor;
import services.MotorCSV;

import java.util.List;
import java.util.Locale;

public class AdminController {

    @FXML
    private VBox contenidoCentro;

    @FXML
    private Label lblTituloSeccion;

    @FXML
    private Button navCaja;

    @FXML
    private Button navInventario;

    @FXML
    private Button navVentas;

    @FXML
    private Button navEmpleados;

    @FXML
    private Button navProductos;

    @FXML
    private Button navProveedores;

    @FXML
    private Button navCerrarSesion;

    private Empleado empleado;
    private Gestor gestor;
    private MotorCSV motorCSV;
    private boolean dialogoCajaMostrado;

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
        this.gestor = new Gestor(
            empleado.getCi(),
            empleado.getNombre(),
            empleado.getContrasena()
        );
        this.motorCSV = new MotorCSV();
        mostrarVentas();
        if (!dialogoCajaMostrado) {
            dialogoCajaMostrado = true;
            Platform.runLater(this::mostrarDialogoCaja);
        }
    }

    @FXML
    private void initialize() {
        lblTituloSeccion.setText("Caja");
        configurarAnimacionNavegacion(navCaja);
        configurarAnimacionNavegacion(navInventario);
        configurarAnimacionNavegacion(navVentas);
        configurarAnimacionNavegacion(navEmpleados);
        configurarAnimacionNavegacion(navProductos);
        configurarAnimacionNavegacion(navProveedores);
        configurarAnimacionNavegacion(navCerrarSesion);
        seleccionarPestana(navVentas);
    }

    /* ===================== SECCIONES ===================== */

    @FXML
    private void mostrarCaja() {
        seleccionarPestana(navCaja);
        mostrarDialogoCaja();
    }

    private void mostrarDialogoCaja() {
        List<String[]> filasCaja = motorCSV.leerCSV(
            motorCSV.rutaDatos("caja.csv").toString()
        );
        boolean abierta = filasCaja.size() > 1
                && filasCaja.get(1).length > 0
                && filasCaja.get(1)[0].equalsIgnoreCase("ABIERTA");
        ButtonType accion = new ButtonType(
            abierta ? "Cerrar caja" : "Abrir caja",
            ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelar = new ButtonType(
            abierta ? "Volver al panel" : "Ahora no",
            ButtonBar.ButtonData.CANCEL_CLOSE
        );

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Gestión de caja");
        dialogo.initOwner(contenidoCentro.getScene().getWindow());
        dialogo.getDialogPane().getButtonTypes().addAll(accion, cancelar);
        dialogo.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/overbyte/styles/estilos.css").toExternalForm()
        );
        dialogo.getDialogPane().getStyleClass().add("caja-dialog");

        VBox contenido = new VBox(12);
        contenido.setAlignment(Pos.CENTER);
        contenido.getStyleClass().add("caja-dialog-content");

        Label icono = new Label(abierta ? "✓" : "$");
        icono.getStyleClass().addAll(
            "caja-dialog-icon",
            abierta ? "caja-dialog-icon-abierta" : "caja-dialog-icon-cerrada"
        );
        Label titulo = new Label(abierta ? "La caja está abierta" : "¿Abrimos la caja?");
        titulo.getStyleClass().add("caja-dialog-title");
        Label descripcion = new Label(abierta
            ? "El punto de venta ya puede registrar operaciones."
            : "Confirma para habilitar las operaciones de venta.");
        descripcion.getStyleClass().add("caja-dialog-description");
        descripcion.setWrapText(true);

        Label estado = new Label(abierta ? "ESTADO ACTUAL · ABIERTA" : "ESTADO ACTUAL · CERRADA");
        estado.getStyleClass().addAll(
            "caja-dialog-status",
            abierta ? "caja-dialog-status-abierta" : "caja-dialog-status-cerrada"
        );

        contenido.getChildren().addAll(icono, titulo, descripcion, estado);
        dialogo.getDialogPane().setContent(contenido);

        Button botonAccion = (Button) dialogo.getDialogPane().lookupButton(accion);
        botonAccion.getStyleClass().add(abierta ? "boton-peligro" : "boton-mas");
        Button botonCancelar = (Button) dialogo.getDialogPane().lookupButton(cancelar);
        botonCancelar.getStyleClass().add("caja-dialog-cancelar");

        dialogo.showAndWait().ifPresent(resultado -> {
            if (resultado == accion) {
                boolean actualizado = abierta ? gestor.cerrarCaja() : gestor.abrirCaja();
                if (!actualizado) {
                    mostrarAlerta("Error", "No se pudo actualizar el estado de la caja.");
                }
            }
        });
    }

    /* ----------- INVENTARIO ----------- */

    @FXML
    private void mostrarInventario() {
        seleccionarPestana(navInventario);
        lblTituloSeccion.setText("Inventario");
        contenidoCentro.getChildren().clear();

        VBox seccion = new VBox(15);
        seccion.setAlignment(Pos.TOP_LEFT);

        // Tabla de inventario
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

        tabla.setPrefHeight(320);

        ScrollPane scrollTabla = new ScrollPane(tabla);
        scrollTabla.setFitToWidth(true);
        scrollTabla.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollTabla.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollTabla.setPrefHeight(340);

        // Mini formulario: productos proximos a agotarse
        Label lblBajoStock = new Label("Productos con stock bajo (<= 5):");
        lblBajoStock.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #fca5a5;");

        List<Producto> bajos = gestor.listarProductosProximosAAgotarse();
        VBox listaBajoStock = new VBox(6);
        listaBajoStock.setAlignment(Pos.TOP_LEFT);
        if (bajos.isEmpty()) {
            listaBajoStock.getChildren().add(new Label("Sin productos con stock bajo."));
        } else {
            for (Producto p : bajos) {
                Label lbl = new Label(p.getIdProducto() + " | " + p.getNombre() + " | Stock: " + p.getStock());
                lbl.setStyle("-fx-font-size: 12px;");
                listaBajoStock.getChildren().add(lbl);
            }
        }

        seccion.getChildren().addAll(scrollTabla, lblBajoStock, listaBajoStock);
        seccion.setPrefHeight(550);

        ScrollPane scroll = new ScrollPane(seccion);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        contenidoCentro.getChildren().add(scroll);
    }

    /* ----------- VENTAS ----------- */

    @FXML
    private void mostrarVentas() {
        seleccionarPestana(navVentas);
        lblTituloSeccion.setText("Ventas");
        contenidoCentro.getChildren().clear();

        VBox seccion = new VBox(15);
        seccion.setAlignment(Pos.TOP_LEFT);

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

        tabla.setPrefHeight(320);

        VBox contenedorTabla = new VBox(15, lblTotal, tabla);
        contenedorTabla.setAlignment(Pos.TOP_LEFT);

        ScrollPane scrollTabla = new ScrollPane(contenedorTabla);
        scrollTabla.setFitToWidth(true);
        scrollTabla.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollTabla.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollTabla.setPrefHeight(380);

        // Boton exportar reporte
        Button btnExportar = new Button("Exportar reporte CSV");
        btnExportar.setStyle("-fx-padding: 8px 16px; -fx-font-size: 13px;");
        btnExportar.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog("reporte.csv");
            dialog.setTitle("Exportar reporte");
            dialog.setHeaderText("Ingresa la ruta del archivo de salida:");
            dialog.setContentText("Ruta:");
            dialog.showAndWait().ifPresent(ruta -> {
                boolean ok = gestor.exportarReporteExcelOCSV(ruta);
                if (ok) {
                    mostrarAlerta("Exito", "Reporte exportado a: " + ruta);
                } else {
                    mostrarAlerta("Error", "No se pudo exportar el reporte.");
                }
            });
        });

        seccion.getChildren().addAll(scrollTabla, btnExportar);
        seccion.setPrefHeight(550);

        ScrollPane scroll = new ScrollPane(seccion);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        contenidoCentro.getChildren().add(scroll);
    }

    /* ----------- EMPLEADOS ----------- */

    @FXML
    private void mostrarEmpleados() {
        seleccionarPestana(navEmpleados);
        lblTituloSeccion.setText("Empleados");
        contenidoCentro.getChildren().clear();

        VBox seccion = new VBox(15);
        seccion.setAlignment(Pos.TOP_LEFT);

        // Tabla de empleados
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

        tabla.setPrefHeight(260);

        ScrollPane scrollTabla = new ScrollPane(tabla);
        scrollTabla.setFitToWidth(true);
        scrollTabla.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollTabla.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollTabla.setPrefHeight(280);

        // Mini formulario: agregar empleado
        VBox formAgregar = new VBox(8);
        formAgregar.setAlignment(Pos.TOP_LEFT);
        formAgregar.setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8;");

        Label lblForm = new Label("Agregar empleado");
        lblForm.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        TextField tfCi = new TextField();
        tfCi.setPromptText("CI");
        tfCi.setPrefWidth(120);

        TextField tfNombre = new TextField();
        tfNombre.setPromptText("Nombre");
        tfNombre.setPrefWidth(200);

        TextField tfRol = new TextField();
        tfRol.setPromptText("Rol (ADMINISTRADOR, CAJERO, ALMACENERO)");
        tfRol.setPrefWidth(260);

        TextField tfContrasena = new TextField();
        tfContrasena.setPromptText("Contrasena");
        tfContrasena.setPrefWidth(150);

        Button btnAgregar = new Button("Agregar");
        btnAgregar.setStyle("-fx-padding: 6px 14px; -fx-font-size: 12px;");
        btnAgregar.setOnAction(event -> {
            try {
                Rol rol = Rol.valueOf(tfRol.getText().trim().toUpperCase());
                Empleado nuevo = new Empleado(tfCi.getText().trim(), tfNombre.getText().trim(),
                        tfContrasena.getText().trim(), rol);
                boolean ok = gestor.anadirEmpleado(nuevo);
                if (ok) {
                    mostrarAlerta("Exito", "Empleado agregado correctamente.");
                    tfCi.clear(); tfNombre.clear(); tfRol.clear(); tfContrasena.clear();
                    mostrarEmpleados();
                } else {
                    mostrarAlerta("Error", "No se pudo agregar el empleado. Verifica los datos.");
                }
            } catch (IllegalArgumentException e) {
                mostrarAlerta("Error", "Rol invalido. Usa: ADMINISTRADOR, CAJERO o ALMACENERO.");
            }
        });

        HBox filaForm = new HBox(8, tfCi, tfNombre, tfRol, tfContrasena, btnAgregar);
        filaForm.setAlignment(Pos.CENTER_LEFT);

        formAgregar.getChildren().addAll(lblForm, filaForm);

        // Mini formulario: eliminar empleado
        VBox formEliminar = new VBox(8);
        formEliminar.setAlignment(Pos.TOP_LEFT);
        formEliminar.setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8;");

        Label lblElim = new Label("Eliminar empleado");
        lblElim.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #fca5a5;");

        TextField tfElimCi = new TextField();
        tfElimCi.setPromptText("CI del empleado");
        tfElimCi.setPrefWidth(200);

        Button btnElim = new Button("Eliminar");
        btnElim.setStyle("-fx-padding: 6px 14px; -fx-font-size: 12px; -fx-background-color: #fca5a5; -fx-text-fill: white;");
        btnElim.setOnAction(event -> {
            boolean ok = gestor.eliminarEmpleado(tfElimCi.getText().trim());
            if (ok) {
                mostrarAlerta("Exito", "Empleado eliminado correctamente.");
                tfElimCi.clear();
                mostrarEmpleados();
            } else {
                mostrarAlerta("Error", "No se pudo eliminar. Verifica el CI o que sea el ultimo administrador.");
            }
        });

        HBox filaElim = new HBox(8, tfElimCi, btnElim);
        filaElim.setAlignment(Pos.CENTER_LEFT);

        formEliminar.getChildren().addAll(lblElim, filaElim);

        seccion.getChildren().addAll(scrollTabla, formAgregar, formEliminar);
        seccion.setPrefHeight(550);

        ScrollPane scroll = new ScrollPane(seccion);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        contenidoCentro.getChildren().add(scroll);
    }

    /* ----------- PRODUCTOS ----------- */

    @FXML
    private void mostrarProductos() {
        seleccionarPestana(navProductos);
        lblTituloSeccion.setText("Productos");
        contenidoCentro.getChildren().clear();

        VBox seccion = new VBox(15);
        seccion.setAlignment(Pos.TOP_LEFT);

        // Tabla de productos
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

        tabla.setPrefHeight(240);

        ScrollPane scrollTabla = new ScrollPane(tabla);
        scrollTabla.setFitToWidth(true);
        scrollTabla.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollTabla.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollTabla.setPrefHeight(260);

        // Mini formulario: agregar producto
        VBox formAgregar = new VBox(8);
        formAgregar.setAlignment(Pos.TOP_LEFT);
        formAgregar.setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8;");

        Label lblForm = new Label("Agregar producto");
        lblForm.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        TextField tfId = new TextField();
        tfId.setPromptText("ID producto");
        tfId.setPrefWidth(110);

        TextField tfNombre = new TextField();
        tfNombre.setPromptText("Nombre");
        tfNombre.setPrefWidth(150);

        TextField tfMarca = new TextField();
        tfMarca.setPromptText("Marca");
        tfMarca.setPrefWidth(110);

        TextField tfCategoria = new TextField();
        tfCategoria.setPromptText("Categoria");
        tfCategoria.setPrefWidth(110);

        TextField tfDescripcion = new TextField();
        tfDescripcion.setPromptText("Descripcion");
        tfDescripcion.setPrefWidth(150);

        TextField tfPrecioE = new TextField();
        tfPrecioE.setPromptText("Precio entrada");
        tfPrecioE.setPrefWidth(100);

        TextField tfPrecioV = new TextField();
        tfPrecioV.setPromptText("Precio venta");
        tfPrecioV.setPrefWidth(100);

        TextField tfStock = new TextField();
        tfStock.setPromptText("Stock");
        tfStock.setPrefWidth(70);

        Button btnAgregar = new Button("Agregar");
        btnAgregar.setStyle("-fx-padding: 6px 14px; -fx-font-size: 12px;");
        btnAgregar.setOnAction(event -> {
            try {
                Producto producto = new Producto(
                    tfId.getText().trim(),
                    tfNombre.getText().trim(),
                    Double.parseDouble(tfPrecioE.getText().trim()),
                    Double.parseDouble(tfPrecioV.getText().trim()),
                    tfDescripcion.getText().trim(),
                    Integer.parseInt(tfStock.getText().trim()),
                    tfMarca.getText().trim(),
                    tfCategoria.getText().trim()
                );
                boolean ok = gestor.anadirProducto(producto);
                if (ok) {
                    mostrarAlerta("Exito", "Producto agregado correctamente.");
                    tfId.clear(); tfNombre.clear(); tfMarca.clear(); tfCategoria.clear();
                    tfDescripcion.clear(); tfPrecioE.clear(); tfPrecioV.clear(); tfStock.clear();
                    mostrarProductos();
                } else {
                    mostrarAlerta("Error", "No se pudo agregar el producto. Verifica que el ID no exista y los datos sean validos.");
                }
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "Precios y stock deben ser numeros validos.");
            }
        });

        HBox fila1 = new HBox(8, tfId, tfNombre, tfMarca, tfCategoria);
        fila1.setAlignment(Pos.CENTER_LEFT);
        HBox fila2 = new HBox(8, tfDescripcion, tfPrecioE, tfPrecioV, tfStock, btnAgregar);
        fila2.setAlignment(Pos.CENTER_LEFT);

        formAgregar.getChildren().addAll(lblForm, fila1, fila2);

        // Mini formulario: eliminar producto
        VBox formEliminar = new VBox(8);
        formEliminar.setAlignment(Pos.TOP_LEFT);
        formEliminar.setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8;");

        Label lblElim = new Label("Eliminar producto");
        lblElim.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #fca5a5;");

        TextField tfElimId = new TextField();
        tfElimId.setPromptText("ID producto");
        tfElimId.setPrefWidth(180);

        Button btnElim = new Button("Eliminar");
        btnElim.setStyle("-fx-padding: 6px 14px; -fx-font-size: 12px; -fx-background-color: #fca5a5; -fx-text-fill: white;");
        btnElim.setOnAction(event -> {
            boolean ok = gestor.eliminarProducto(tfElimId.getText().trim());
            if (ok) {
                mostrarAlerta("Exito", "Producto eliminado correctamente.");
                tfElimId.clear();
                mostrarProductos();
            } else {
                mostrarAlerta("Error", "No se pudo eliminar el producto. Verifica el ID.");
            }
        });

        HBox filaElim = new HBox(8, tfElimId, btnElim);
        filaElim.setAlignment(Pos.CENTER_LEFT);

        formEliminar.getChildren().addAll(lblElim, filaElim);

        seccion.getChildren().addAll(scrollTabla, formAgregar, formEliminar);
        seccion.setPrefHeight(550);

        ScrollPane scroll = new ScrollPane(seccion);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        contenidoCentro.getChildren().add(scroll);
    }

    /* ----------- PROVEEDORES ----------- */

    @FXML
    private void mostrarProveedores() {
        seleccionarPestana(navProveedores);
        lblTituloSeccion.setText("Proveedores");
        contenidoCentro.getChildren().clear();

        VBox seccion = new VBox(15);
        seccion.setAlignment(Pos.TOP_LEFT);

        // Tabla de proveedores
        List<String[]> filas = motorCSV.leerCSV(motorCSV.rutaDatos("proveedores.csv").toString());

        TableView<String[]> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<String[], String> colId = new TableColumn<>("ID Proveedor");
        colId.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[0]));

        TableColumn<String[], String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[1]));

        TableColumn<String[], String> colContacto = new TableColumn<>("Contacto");
        colContacto.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[2]));

        tabla.getColumns().addAll(colId, colNombre, colContacto);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length < 3) continue;
            tabla.getItems().add(fila);
        }

        tabla.setPrefHeight(240);

        ScrollPane scrollTabla = new ScrollPane(tabla);
        scrollTabla.setFitToWidth(true);
        scrollTabla.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollTabla.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollTabla.setPrefHeight(260);

        // Mini formulario: agregar proveedor
        VBox formAgregar = new VBox(8);
        formAgregar.setAlignment(Pos.TOP_LEFT);
        formAgregar.setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8;");

        Label lblForm = new Label("Agregar proveedor");
        lblForm.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        TextField tfId = new TextField();
        tfId.setPromptText("ID proveedor");
        tfId.setPrefWidth(150);

        TextField tfNombre = new TextField();
        tfNombre.setPromptText("Nombre proveedor");
        tfNombre.setPrefWidth(220);

        TextField tfContacto = new TextField();
        tfContacto.setPromptText("Contacto (telefono)");
        tfContacto.setPrefWidth(150);

        Button btnAgregar = new Button("Agregar");
        btnAgregar.setStyle("-fx-padding: 6px 14px; -fx-font-size: 12px;");
        btnAgregar.setOnAction(event -> {
            try {
                Proveedor proveedor = new Proveedor(
                    tfNombre.getText().trim(),
                    tfId.getText().trim(),
                    Integer.parseInt(tfContacto.getText().trim())
                );
                boolean ok = gestor.anadirProveedor(proveedor);
                if (ok) {
                    mostrarAlerta("Exito", "Proveedor agregado correctamente.");
                    tfId.clear(); tfNombre.clear(); tfContacto.clear();
                    mostrarProveedores();
                } else {
                    mostrarAlerta("Error", "No se pudo agregar el proveedor. Verifica los datos.");
                }
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El contacto debe ser un numero valido.");
            }
        });

        HBox filaForm = new HBox(8, tfId, tfNombre, tfContacto, btnAgregar);
        filaForm.setAlignment(Pos.CENTER_LEFT);

        formAgregar.getChildren().addAll(lblForm, filaForm);

        // Mini formulario: eliminar proveedor
        VBox formEliminar = new VBox(8);
        formEliminar.setAlignment(Pos.TOP_LEFT);
        formEliminar.setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8;");

        Label lblElim = new Label("Eliminar proveedor");
        lblElim.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #fca5a5;");

        TextField tfElimId = new TextField();
        tfElimId.setPromptText("ID proveedor");
        tfElimId.setPrefWidth(180);

        Button btnElim = new Button("Eliminar");
        btnElim.setStyle("-fx-padding: 6px 14px; -fx-font-size: 12px; -fx-background-color: #fca5a5; -fx-text-fill: white;");
        btnElim.setOnAction(event -> {
            boolean ok = gestor.eliminarProveedor(tfElimId.getText().trim());
            if (ok) {
                mostrarAlerta("Exito", "Proveedor eliminado correctamente.");
                tfElimId.clear();
                mostrarProveedores();
            } else {
                mostrarAlerta("Error", "No se pudo eliminar el proveedor. Verifica el ID.");
            }
        });

        HBox filaElim = new HBox(8, tfElimId, btnElim);
        filaElim.setAlignment(Pos.CENTER_LEFT);

        formEliminar.getChildren().addAll(lblElim, filaElim);

        seccion.getChildren().addAll(scrollTabla, formAgregar, formEliminar);
        seccion.setPrefHeight(550);

        ScrollPane scroll = new ScrollPane(seccion);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

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

    /* ===================== UTILIDADES ===================== */

    private void seleccionarPestana(Button seleccionada) {
        for (Button pestana : List.of(
                navCaja, navInventario, navVentas,
                navEmpleados, navProductos, navProveedores)) {
            pestana.getStyleClass().remove("nav-button-activo");
        }
        if (!seleccionada.getStyleClass().contains("nav-button-activo")) {
            seleccionada.getStyleClass().add("nav-button-activo");
        }
    }

    private void configurarAnimacionNavegacion(Button boton) {
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

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
