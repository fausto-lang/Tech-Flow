package com.overbyte.controllers;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import user.Empleado;
import user.Gestor;
import user.Rol;
import models.Producto;
import models.Proveedor;
import services.MotorCSV;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    private Button navEntradas;

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
        configurarAnimacionNavegacion(navEntradas);
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

        Button botonAccion = new Button(abierta ? "Cerrar caja" : "Abrir caja");
        botonAccion.getStyleClass().add(abierta ? "boton-peligro" : "boton-mas");
        Button botonCancelar = new Button(abierta ? "Volver al panel" : "Ahora no");
        botonCancelar.getStyleClass().add("caja-dialog-cancelar");
        contenido.getChildren().addAll(botonAccion, botonCancelar);

        botonCancelar.setOnAction(e -> com.overbyte.Navegador.cerrarOverlay());
        botonAccion.setOnAction(e -> {
            com.overbyte.Navegador.cerrarOverlay();
            boolean actualizado = abierta ? gestor.cerrarCaja() : gestor.abrirCaja();
            if (!actualizado) {
                mostrarAlerta("Error", "No se pudo actualizar el estado de la caja.");
            }
        });
        com.overbyte.Navegador.mostrarOverlay(
            com.overbyte.Navegador.tarjetaCompacta("Gestión de caja", "", contenido));
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
        configurarTablaAdmin(tabla);
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

        tabla.setPrefHeight(400);
        VBox inventarioCompleto = new VBox(12);
        inventarioCompleto.getStyleClass().add("inventario-apartado");
        Label tituloInventario = new Label("Inventario completo");
        tituloInventario.getStyleClass().add("inventario-titulo");
        Label descripcionInventario = new Label(
            "Todos los productos registrados en el sistema."
        );
        descripcionInventario.getStyleClass().add("inventario-descripcion");
        VBox.setVgrow(tabla, Priority.ALWAYS);
        inventarioCompleto.getChildren().addAll(
            tituloInventario, descripcionInventario, tabla
        );

        List<Producto> bajos = gestor.listarProductosProximosAAgotarse();
        VBox listaBajoStock = new VBox(8);
        listaBajoStock.setAlignment(Pos.TOP_LEFT);
        if (bajos.isEmpty()) {
            Label sinCriticos = new Label("No hay productos con stock crítico.");
            sinCriticos.getStyleClass().add("inventario-vacio");
            listaBajoStock.getChildren().add(sinCriticos);
        } else {
            for (Producto p : bajos) {
                Label nombre = new Label(p.getNombre());
                nombre.getStyleClass().add("critico-producto-nombre");
                Label codigo = new Label(p.getIdProducto() + " · " + p.getCategoria());
                codigo.getStyleClass().add("critico-producto-detalle");
                VBox detalles = new VBox(4, nombre, codigo);
                HBox.setHgrow(detalles, Priority.ALWAYS);

                Label stock = new Label(p.getStock() + " uds.");
                stock.getStyleClass().add("critico-stock");
                HBox filaCritica = new HBox(10, detalles, stock);
                filaCritica.setAlignment(Pos.CENTER_LEFT);
                filaCritica.getStyleClass().add("critico-producto");
                listaBajoStock.getChildren().add(filaCritica);
            }
        }

        VBox inventarioCritico = new VBox(12);
        inventarioCritico.getStyleClass().add("inventario-apartado");
        Label tituloCritico = new Label("Productos por agotarse");
        tituloCritico.getStyleClass().add("inventario-titulo");
        Label descripcionCritico = new Label(
            "Stock crítico: 5 unidades o menos."
        );
        descripcionCritico.getStyleClass().add("inventario-descripcion");
        Label cantidadCritica = new Label(String.valueOf(bajos.size()));
        cantidadCritica.getStyleClass().add("critico-contador");
        HBox encabezadoCritico = new HBox(10, tituloCritico, cantidadCritica);
        encabezadoCritico.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scrollCritico = new ScrollPane(listaBajoStock);
        scrollCritico.setFitToWidth(true);
        scrollCritico.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollCritico.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollCritico, Priority.ALWAYS);
        inventarioCritico.getChildren().addAll(
            encabezadoCritico, descripcionCritico, scrollCritico
        );

        Button selectorCompleto = crearSelectorInventario(
            "Inventario completo", filasProductosValidas(filas) + " productos", "▦"
        );
        Button selectorCritico = crearSelectorInventario(
            "Productos por agotarse", bajos.size() + " productos", "!"
        );
        HBox selectores = new HBox(12, selectorCompleto, selectorCritico);
        selectorCompleto.setMaxWidth(Double.MAX_VALUE);
        selectorCritico.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(selectorCompleto, Priority.ALWAYS);
        HBox.setHgrow(selectorCritico, Priority.ALWAYS);

        VBox contenidoApartado = new VBox(inventarioCompleto, inventarioCritico);
        VBox.setVgrow(contenidoApartado, Priority.ALWAYS);
        VBox.setVgrow(inventarioCompleto, Priority.ALWAYS);
        VBox.setVgrow(inventarioCritico, Priority.ALWAYS);
        inventarioCompleto.setMaxHeight(Double.MAX_VALUE);
        inventarioCritico.setMaxHeight(Double.MAX_VALUE);
        inventarioCritico.setVisible(false);
        inventarioCritico.setManaged(false);
        selectorCompleto.getStyleClass().add("selector-inventario-activo");
        selectorCompleto.setOnAction(event -> seleccionarApartadoInventario(
            selectorCompleto, inventarioCompleto, selectorCritico, inventarioCritico
        ));
        selectorCritico.setOnAction(event -> seleccionarApartadoInventario(
            selectorCritico, inventarioCritico, selectorCompleto, inventarioCompleto
        ));

        seccion.getChildren().addAll(selectores, contenidoApartado);
        VBox.setVgrow(seccion, Priority.ALWAYS);
        seccion.setFillWidth(true);

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

        VBox seccion = new VBox(10);
        seccion.setAlignment(Pos.TOP_LEFT);
        seccion.setMaxWidth(Double.MAX_VALUE);

        List<String[]> filas = motorCSV.leerCSV(motorCSV.rutaDatos("ventas.csv").toString());

        double totalDia = 0;
        int count = 0;

        TableView<String[]> tabla = new TableView<>();
        configurarTablaAdmin(tabla);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<String[], String> colFolio = new TableColumn<>("Folio");
        colFolio.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[0]));
        colFolio.setPrefWidth(110);

        TableColumn<String[], String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[4]));
        colCliente.setPrefWidth(120);

        TableColumn<String[], String> colProducto = new TableColumn<>("Producto");
        colProducto.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[6]));
        colProducto.setPrefWidth(160);

        TableColumn<String[], String> colFecha = new TableColumn<>("Fecha y hora");
        colFecha.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(
                formatearFechaVenta(param.getValue()[10])));
        colFecha.setPrefWidth(125);

        TableColumn<String[], String> colCantidad = new TableColumn<>("Unidades");
        colCantidad.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[7]));
        colCantidad.setPrefWidth(75);

        TableColumn<String[], String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(param -> {
            try {
                double t = Double.parseDouble(param.getValue()[9]);
                return new javafx.beans.property.SimpleStringProperty(String.format(Locale.ROOT, "$%.2f", t));
            } catch (NumberFormatException e) {
                return new javafx.beans.property.SimpleStringProperty("—");
            }
        });
        colTotal.setPrefWidth(85);

        tabla.getColumns().addAll(colFolio, colCliente, colProducto, colFecha, colCantidad, colTotal);

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

        Label lblTotal = new Label(String.format(Locale.ROOT, "$%.2f", totalDia));
        lblTotal.getStyleClass().add("ventas-total-valor");
        Label lblCantidadVentas = new Label(count + (count == 1 ? " venta confirmada" : " ventas confirmadas"));
        lblCantidadVentas.getStyleClass().add("ventas-total-detalle");
        Label etiquetaTotal = new Label("TOTAL DE HOY");
        etiquetaTotal.getStyleClass().add("ventas-total-etiqueta");
        VBox resumenVentas = new VBox(5, etiquetaTotal, lblTotal, lblCantidadVentas);
        resumenVentas.getStyleClass().add("ventas-resumen");

        Button btnExportar = new Button("Exportar informe");
        btnExportar.getStyleClass().add("boton-exportar");
        double totalDelDia = totalDia;
        int cantidadDelDia = count;
        btnExportar.setOnAction(event ->
            exportarInformeVentas(tabla, totalDelDia, cantidadDelDia)
        );

        HBox encabezadoSeccion = new HBox(12);
        encabezadoSeccion.setAlignment(Pos.CENTER_LEFT);
        Label tituloVentas = new Label("Ventas del día");
        tituloVentas.getStyleClass().add("ventas-titulo");
        Label subtituloVentas = new Label("Ventas confirmadas registradas hoy");
        subtituloVentas.getStyleClass().add("ventas-subtitulo");
        VBox textoEncabezado = new VBox(4, tituloVentas, subtituloVentas);
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        encabezadoSeccion.getChildren().addAll(textoEncabezado, espacio, btnExportar);

        VBox contenedorTabla = new VBox(10, resumenVentas, tabla);
        contenedorTabla.getStyleClass().add("ventas-tabla-card");
        contenedorTabla.setMaxWidth(Double.MAX_VALUE);
        tabla.setMaxWidth(Double.MAX_VALUE);
        tabla.setPrefHeight(280);
        tabla.setMinHeight(140);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        VBox.setVgrow(contenedorTabla, Priority.NEVER);
        seccion.getChildren().addAll(encabezadoSeccion, contenedorTabla);
        seccion.setFillWidth(true);
        seccion.setMaxWidth(Double.MAX_VALUE);
        ScrollPane scroll = new ScrollPane(seccion);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setMaxWidth(Double.MAX_VALUE);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        contenidoCentro.getChildren().add(scroll);
    }

    @FXML
    private void mostrarEntradas() {
        seleccionarPestana(navEntradas);
        lblTituloSeccion.setText("Entradas de productos");
        contenidoCentro.getChildren().clear();

        VBox seccion = new VBox(15);
        seccion.setAlignment(Pos.TOP_LEFT);
        List<String[]> entradas = motorCSV.leerCSV(motorCSV.rutaDatos("entradas.csv").toString());
        List<String[]> proveedores = motorCSV.leerCSV(motorCSV.rutaDatos("proveedores.csv").toString());
        TableView<String[]> tabla = new TableView<>();
        configurarTablaAdmin(tabla);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        agregarColumnaEntrada(tabla, "ID de entrada", 0, 130);
        agregarColumnaEntrada(tabla, "Proveedor", 1, 150);
        agregarColumnaEntrada(tabla, "ID producto", 2, 110);
        agregarColumnaEntrada(tabla, "Producto", 3, 210);
        agregarColumnaEntrada(tabla, "Fecha", 4, 110);
        agregarColumnaEntrada(tabla, "Unidades", 5, 90);
        agregarColumnaEntrada(tabla, "Precio unitario", 6, 120);
        agregarColumnaEntrada(tabla, "Total de entrada", 7, 130);
        agregarColumnaEntrada(tabla, "Registrado por", 8, 120);

        int cantidadEntradas = 0;
        int unidadesTotales = 0;
        double costoTotal = 0;
        for (int i = 1; i < entradas.size(); i++) {
            String[] fila = entradas.get(i);
            if (fila.length < 8 || fila[0].equalsIgnoreCase("idEntrada")) {
                continue;
            }

            String proveedorId = valorVenta(fila, 1);
            String proveedorNombre = buscarNombreProveedor(proveedores, proveedorId);
            String unidadesTexto = valorVenta(fila, 4);
            String precioTexto = formatearTotalVenta(valorVenta(fila, 5));
            String totalTexto = "—";
            try {
                int unidades = Integer.parseInt(fila[4]);
                double precio = Double.parseDouble(fila[5]);
                double total = unidades * precio;
                unidadesTotales += unidades;
                costoTotal += total;
                totalTexto = String.format(Locale.ROOT, "$%.2f", total);
            } catch (NumberFormatException ignored) {
                // Mantiene la entrada visible aunque sus datos numéricos estén dañados.
            }

            tabla.getItems().add(new String[]{
                valorVenta(fila, 0), proveedorNombre, valorVenta(fila, 2),
                valorVenta(fila, 3), formatearFechaEntrada(fila[6]),
                unidadesTexto, precioTexto, totalTexto, valorVenta(fila, 7)
            });
            cantidadEntradas++;
        }

        Label etiquetaResumen = new Label("ENTRADAS REGISTRADAS");
        etiquetaResumen.getStyleClass().add("ventas-total-etiqueta");
        Label valorResumen = new Label(cantidadEntradas + " registros · " + unidadesTotales + " unidades");
        valorResumen.getStyleClass().add("ventas-total-valor");
        Label detalleResumen = new Label(
            "Costo de compra acumulado: " + String.format(Locale.ROOT, "$%.2f", costoTotal)
        );
        detalleResumen.getStyleClass().add("ventas-total-detalle");
        VBox resumen = new VBox(5, etiquetaResumen, valorResumen, detalleResumen);
        resumen.getStyleClass().add("ventas-resumen");

        Button btnExportar = new Button("Exportar informe");
        btnExportar.getStyleClass().add("boton-exportar");
        double costoInforme = costoTotal;
        int registrosInforme = cantidadEntradas;
        int unidadesInforme = unidadesTotales;
        btnExportar.setOnAction(event ->
            exportarInformeEntradas(tabla, costoInforme, registrosInforme, unidadesInforme)
        );

        Label titulo = new Label("Historial de entradas");
        titulo.getStyleClass().add("ventas-titulo");
        Label subtitulo = new Label("Productos recibidos e ingresados al inventario");
        subtitulo.getStyleClass().add("ventas-subtitulo");
        VBox textoEncabezado = new VBox(4, titulo, subtitulo);
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox encabezado = new HBox(12, textoEncabezado, espacio, btnExportar);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(14, resumen, tabla);
        tarjeta.getStyleClass().add("ventas-tabla-card");
        VBox.setVgrow(tabla, Priority.ALWAYS);
        VBox.setVgrow(tarjeta, Priority.ALWAYS);
        seccion.getChildren().addAll(encabezado, tarjeta);

        ScrollPane scroll = new ScrollPane(seccion);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        contenidoCentro.getChildren().add(scroll);
    }

    private void agregarColumnaEntrada(
            TableView<String[]> tabla, String titulo, int indice, double ancho) {
        TableColumn<String[], String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(
            valorVenta(param.getValue(), indice)
        ));
        columna.setPrefWidth(ancho);
        tabla.getColumns().add(columna);
    }

    private String buscarNombreProveedor(List<String[]> proveedores, String idProveedor) {
        if (idProveedor.equals("—")) {
            return "Ingreso manual";
        }
        for (int i = 1; i < proveedores.size(); i++) {
            String[] proveedor = proveedores.get(i);
            if (proveedor.length > 1 && proveedor[0].equalsIgnoreCase(idProveedor)) {
                return proveedor[1];
            }
        }
        return idProveedor;
    }

    private String formatearFechaEntrada(String fecha) {
        try {
            return LocalDate.parse(fecha).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            return fecha;
        }
    }

    private void exportarInformeEntradas(
            TableView<String[]> tabla, double costoTotal, int registros, int unidades) {
        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar informe de entradas");
        selector.setInitialFileName("informe-entradas-" + LocalDate.now() + ".txt");
        selector.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Informe de texto (*.txt)", "*.txt")
        );
        java.io.File archivo = selector.showSaveDialog(contenidoCentro.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        try {
            Path ruta = archivo.toPath();
            if (!ruta.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".txt")) {
                ruta = ruta.resolveSibling(ruta.getFileName() + ".txt");
            }
            Files.writeString(
                ruta,
                crearContenidoInformeEntradas(tabla.getItems(), costoTotal, registros, unidades),
                StandardCharsets.UTF_8
            );
            mostrarAlerta("Informe exportado", "El informe se guardó correctamente en:\n" + ruta);
        } catch (IOException e) {
            mostrarAlerta("Error al exportar", "No se pudo guardar el informe: " + e.getMessage());
        }
    }

    private String crearContenidoInformeEntradas(
            List<String[]> entradas, double costoTotal, int registros, int unidades) {
        int[] anchos = {14, 22, 14, 28, 12, 9, 15, 16, 16};
        String[] encabezados = {
            "ID ENTRADA", "PROVEEDOR", "ID PRODUCTO", "PRODUCTO",
            "FECHA", "UNIDADES", "PRECIO UNIT.", "TOTAL", "REGISTRADO POR"
        };
        String borde = crearBordeTabla(anchos);
        StringBuilder informe = new StringBuilder()
            .append("TECH-FLOW | INFORME DE ENTRADAS DE PRODUCTOS\n")
            .append("Fecha del informe: ").append(LocalDate.now()).append('\n')
            .append("Generado: ").append(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            )).append('\n')
            .append("Registros: ").append(registros)
            .append("    Unidades recibidas: ").append(unidades)
            .append("    Costo acumulado: ")
            .append(String.format(Locale.ROOT, "$%.2f", costoTotal)).append("\n\n")
            .append(borde).append('\n')
            .append(filaInforme(encabezados, anchos)).append('\n')
            .append(borde).append('\n');
        for (String[] entrada : entradas) {
            informe.append(filaInforme(entrada, anchos)).append('\n');
        }
        return informe.append(borde).append('\n').append("Fin del informe").toString();
    }

    private void exportarInformeVentas(TableView<String[]> tabla, double totalDia, int cantidadVentas) {
        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar informe de ventas");
        selector.setInitialFileName("informe-ventas-" + LocalDate.now() + ".txt");
        selector.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Informe de texto (*.txt)", "*.txt")
        );
        java.io.File archivo = selector.showSaveDialog(contenidoCentro.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        try {
            Path ruta = archivo.toPath();
            if (!ruta.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".txt")) {
                ruta = ruta.resolveSibling(ruta.getFileName() + ".txt");
            }
            Files.writeString(
                ruta,
                crearContenidoInforme(tabla.getItems(), totalDia, cantidadVentas),
                StandardCharsets.UTF_8
            );
            mostrarAlerta("Informe exportado", "El informe se guardó correctamente en:\n" + ruta);
        } catch (IOException e) {
            mostrarAlerta("Error al exportar", "No se pudo guardar el informe: " + e.getMessage());
        }
    }

    private String crearContenidoInforme(List<String[]> ventas, double totalDia, int cantidadVentas) {
        int[] anchos = {14, 20, 26, 16, 8, 12};
        String[] encabezados = {"FOLIO", "CLIENTE", "PRODUCTO", "FECHA Y HORA", "UNIDADES", "TOTAL"};
        String borde = crearBordeTabla(anchos);
        StringBuilder informe = new StringBuilder();
        String titulo = "TECH-FLOW | INFORME DE VENTAS";
        informe.append(titulo).append('\n');
        informe.append("Fecha del informe: ").append(LocalDate.now()).append('\n');
        informe.append("Generado: ").append(LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        )).append('\n');
        informe.append("Ventas confirmadas: ").append(cantidadVentas)
                .append("    Total del día: ")
                .append(String.format(Locale.ROOT, "$%.2f", totalDia)).append("\n\n");
        informe.append(borde).append('\n');
        informe.append(filaInforme(encabezados, anchos)).append('\n');
        informe.append(borde).append('\n');
        for (String[] venta : ventas) {
            String[] columnas = {
                valorVenta(venta, 0),
                valorVenta(venta, 4),
                valorVenta(venta, 6),
                formatearFechaVenta(valorVenta(venta, 10)),
                valorVenta(venta, 7),
                formatearTotalVenta(valorVenta(venta, 9))
            };
            informe.append(filaInforme(columnas, anchos)).append('\n');
        }
        informe.append(borde).append('\n');
        informe.append("Fin del informe");
        return informe.toString();
    }

    private String crearBordeTabla(int[] anchos) {
        StringBuilder borde = new StringBuilder("+");
        for (int ancho : anchos) {
            borde.append("-".repeat(ancho + 2)).append('+');
        }
        return borde.toString();
    }

    private String filaInforme(String[] columnas, int[] anchos) {
        StringBuilder fila = new StringBuilder("|");
        for (int i = 0; i < anchos.length; i++) {
            fila.append(' ').append(ajustarTexto(columnas[i], anchos[i])).append(" |");
        }
        return fila.toString();
    }

    private String ajustarTexto(String texto, int ancho) {
        String limpio = texto.replace('\n', ' ').replace('\r', ' ').replace('|', '/').trim();
        if (limpio.length() > ancho) {
            return limpio.substring(0, ancho - 1) + "…";
        }
        return String.format("%-" + ancho + "s", limpio);
    }

    private String valorVenta(String[] fila, int indice) {
        return indice < fila.length && !fila[indice].isBlank() ? fila[indice].trim() : "—";
    }

    private String formatearFechaVenta(String fecha) {
        try {
            return LocalDateTime.parse(fecha).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (DateTimeParseException e) {
            return fecha.length() > 16 ? fecha.substring(0, 16) : fecha;
        }
    }

    private String formatearTotalVenta(String total) {
        try {
            return String.format(Locale.ROOT, "$%.2f", Double.parseDouble(total));
        } catch (NumberFormatException e) {
            return "—";
        }
    }

    /* ----------- EMPLEADOS ----------- */

    @FXML
    private void mostrarEmpleados() {
        seleccionarPestana(navEmpleados);
        lblTituloSeccion.setText("Empleados");
        contenidoCentro.getChildren().clear();

        VBox seccion = new VBox(15);
        seccion.setAlignment(Pos.TOP_LEFT);
        List<String[]> filas = motorCSV.leerCSV(motorCSV.rutaDatos("empleado.csv").toString());

        TableView<String[]> tabla = new TableView<>();
        configurarTablaAdmin(tabla);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<String[], String> colCi = new TableColumn<>("CI");
        colCi.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[0]));
        colCi.setPrefWidth(180);

        TableColumn<String[], String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[1]));
        colNombre.setPrefWidth(260);

        TableColumn<String[], String> colRol = new TableColumn<>("Rol");
        colRol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[2]));
        colRol.setPrefWidth(180);

        tabla.getColumns().addAll(colCi, colNombre, colRol);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length < 4 || fila[0].equalsIgnoreCase("ci")) {
                continue;
            }
            tabla.getItems().add(fila);
        }

        Label titulo = new Label("Equipo de trabajo");
        titulo.getStyleClass().add("ventas-titulo");
        Label subtitulo = new Label("Consulta y administra los usuarios del sistema.");
        subtitulo.getStyleClass().add("ventas-subtitulo");
        Label cantidadEmpleados = new Label(tabla.getItems().size() + " empleados");
        cantidadEmpleados.getStyleClass().add("critico-contador");

        Button btnAnadir = new Button("Añadir empleado");
        btnAnadir.getStyleClass().add("boton-exportar");
        btnAnadir.setOnAction(event -> mostrarDialogoAnadirEmpleado());

        Button btnEliminar = new Button("Eliminar seleccionado");
        btnEliminar.getStyleClass().add("boton-eliminar-empleado");
        btnEliminar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        btnEliminar.setOnAction(event -> eliminarEmpleadoSeleccionado(tabla));

        HBox acciones = new HBox(10, btnAnadir, btnEliminar);
        acciones.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(titulo, Priority.ALWAYS);
        VBox encabezado = new VBox(5, titulo, subtitulo);
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox cabecera = new HBox(12, encabezado, cantidadEmpleados, espacio, acciones);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(12, tabla);
        tarjeta.getStyleClass().add("ventas-tabla-card");
        VBox.setVgrow(tabla, Priority.ALWAYS);
        VBox.setVgrow(tarjeta, Priority.ALWAYS);
        seccion.getChildren().addAll(cabecera, tarjeta);
        VBox.setVgrow(seccion, Priority.ALWAYS);
        seccion.setFillWidth(true);

        ScrollPane scroll = new ScrollPane(seccion);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        contenidoCentro.getChildren().add(scroll);
    }

    private void mostrarDialogoAnadirEmpleado() {
        TextField ci = new TextField();
        ci.setPromptText("Ej. 12345678");
        TextField nombre = new TextField();
        nombre.setPromptText("Nombre completo");
        ComboBox<Rol> rol = new ComboBox<>();
        rol.getItems().setAll(Rol.values());
        rol.setPromptText("Selecciona un rol");
        rol.setMaxWidth(Double.MAX_VALUE);
        rol.setConverter(new StringConverter<>() {
            @Override
            public String toString(Rol valor) {
                if (valor == null) {
                    return "";
                }
                return switch (valor) {
                    case ADMINISTRADOR -> "Administrador";
                    case CAJERO -> "Cajero";
                    case ALMACENERO -> "Almacenero";
                };
            }

            @Override
            public Rol fromString(String valor) {
                return null;
            }
        });
        PasswordField contrasena = new PasswordField();
        contrasena.setPromptText("Contraseña");

        VBox formulario = new VBox(8,
            etiquetaCampo("Cédula / identificador"), ci,
            etiquetaCampo("Nombre completo"), nombre,
            etiquetaCampo("Rol"), rol,
            etiquetaCampo("Contraseña"), contrasena
        );
        formulario.getStyleClass().add("producto-dialog-content");
        Button botonGuardar = new Button("Guardar empleado");
        botonGuardar.getStyleClass().add("boton-mas");
        Button botonCancelar = new Button("Cancelar");
        botonCancelar.getStyleClass().add("boton-rol");
        HBox botones = new HBox(10, botonGuardar, botonCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);
        formulario.getChildren().add(botones);
        botonCancelar.setOnAction(e -> com.overbyte.Navegador.cerrarOverlay());
        botonGuardar.setOnAction(e -> {
            if (ci.getText().isBlank() || nombre.getText().isBlank()
                    || contrasena.getText().isBlank() || rol.getValue() == null) {
                mostrarAlerta("Datos incompletos", "Completa todos los campos y selecciona un rol.");
                return;
            }
            Empleado nuevo = new Empleado(ci.getText().trim(), nombre.getText().trim(), contrasena.getText(), rol.getValue());
            com.overbyte.Navegador.cerrarOverlay();
            if (gestor.anadirEmpleado(nuevo)) {
                mostrarEmpleados();
                mostrarAlerta("Empleado añadido", "El empleado se registró correctamente.");
            } else {
                mostrarAlerta("No se pudo añadir", "Verifica los datos y que el identificador no exista.");
            }
        });
        com.overbyte.Navegador.mostrarOverlay(
            com.overbyte.Navegador.tarjeta("Añadir empleado", "Completa los datos del nuevo usuario.", formulario));
    }

    private Label etiquetaCampo(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("etiqueta");
        return etiqueta;
    }

    private void eliminarEmpleadoSeleccionado(TableView<String[]> tabla) {
        String[] seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null || seleccionado.length == 0) {
            return;
        }
        com.overbyte.Navegador.confirmar("Eliminar empleado",
            "¿Eliminar a " + seleccionado[1] + "? Se quitará el acceso de este empleado al sistema. Esta acción no se puede deshacer.",
            () -> {
                if (gestor.eliminarEmpleado(seleccionado[0])) {
                    mostrarEmpleados();
                } else {
                    mostrarAlerta(
                        "No se pudo eliminar",
                        "No se puede eliminar el último administrador o el registro cambió."
                    );
                }
            });
    }

    /* ----------- PRODUCTOS ----------- */

    @FXML
    private void mostrarProductos() {
        seleccionarPestana(navProductos);
        lblTituloSeccion.setText("Productos");
        contenidoCentro.getChildren().clear();

        VBox seccion = new VBox(15);
        seccion.setAlignment(Pos.TOP_LEFT);
        List<String[]> filas = motorCSV.leerCSV(motorCSV.rutaDatos("inventario.csv").toString());

        TableView<String[]> tabla = new TableView<>();
        configurarTablaAdmin(tabla);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<String[], String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[0]));
        colId.setPrefWidth(140);

        TableColumn<String[], String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[1]));
        colNombre.setPrefWidth(220);

        TableColumn<String[], String> colMarca = new TableColumn<>("Marca");
        colMarca.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[2]));
        colMarca.setPrefWidth(130);

        TableColumn<String[], String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[3]));
        colCategoria.setPrefWidth(130);

        TableColumn<String[], String> colPrecioEntrada = new TableColumn<>("Precio compra");
        colPrecioEntrada.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(
                formatearTotalVenta(param.getValue()[5])));
        colPrecioEntrada.setPrefWidth(120);

        TableColumn<String[], String> colPrecioVenta = new TableColumn<>("Precio Venta");
        colPrecioVenta.setCellValueFactory(param -> {
            return new javafx.beans.property.SimpleStringProperty(formatearTotalVenta(param.getValue()[6]));
        });
        colPrecioVenta.setPrefWidth(120);

        TableColumn<String[], String> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[7]));
        colStock.setPrefWidth(90);

        tabla.getColumns().addAll(
            colId, colNombre, colMarca, colCategoria,
            colPrecioEntrada, colPrecioVenta, colStock
        );

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length < 8 || fila[0].equalsIgnoreCase("idProducto")) {
                continue;
            }
            tabla.getItems().add(fila);
        }

        Label titulo = new Label("Catálogo de productos");
        titulo.getStyleClass().add("ventas-titulo");
        Label subtitulo = new Label("Administra los productos registrados en el inventario.");
        subtitulo.getStyleClass().add("ventas-subtitulo");
        VBox encabezado = new VBox(5, titulo, subtitulo);

        Label cantidadProductos = new Label(tabla.getItems().size() + " productos");
        cantidadProductos.getStyleClass().add("critico-contador");

        Button btnAnadir = new Button("Añadir producto");
        btnAnadir.getStyleClass().add("boton-exportar");
        btnAnadir.setOnAction(event -> mostrarDialogoAnadirProducto());

        Button btnEliminar = new Button("Eliminar seleccionado");
        btnEliminar.getStyleClass().add("boton-eliminar-empleado");
        btnEliminar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        btnEliminar.setOnAction(event -> eliminarProductoSeleccionado(tabla));

        HBox acciones = new HBox(10, btnAnadir, btnEliminar);
        acciones.setAlignment(Pos.CENTER_RIGHT);
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox cabecera = new HBox(12, encabezado, cantidadProductos, espacio, acciones);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(12, tabla);
        tarjeta.getStyleClass().add("ventas-tabla-card");
        VBox.setVgrow(tabla, Priority.ALWAYS);
        VBox.setVgrow(tarjeta, Priority.ALWAYS);
        seccion.getChildren().addAll(cabecera, tarjeta);
        VBox.setVgrow(seccion, Priority.ALWAYS);
        seccion.setFillWidth(true);

        ScrollPane scroll = new ScrollPane(seccion);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        contenidoCentro.getChildren().add(scroll);
    }

    private void mostrarDialogoAnadirProducto() {
        TextField id = new TextField(gestor.generarIdProducto());
        id.setEditable(false);
        id.getStyleClass().add("identificador-generado");
        TextField nombre = new TextField();
        nombre.setPromptText("Nombre del producto");
        TextField marca = new TextField();
        marca.setPromptText("Marca");
        TextField categoria = new TextField();
        categoria.setPromptText("Categoría");
        TextField descripcion = new TextField();
        descripcion.setPromptText("Descripción (opcional)");
        TextField precioEntrada = new TextField();
        precioEntrada.setPromptText("0.00");
        TextField precioVenta = new TextField();
        precioVenta.setPromptText("0.00");
        TextField stock = new TextField();
        stock.setPromptText("0");

        HBox precios = new HBox(10,
            campoEnDialogo("Precio de compra", precioEntrada),
            campoEnDialogo("Precio de venta", precioVenta)
        );
        HBox.setHgrow(precios.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(precios.getChildren().get(1), Priority.ALWAYS);
        VBox formulario = new VBox(8,
            campoEnDialogo("ID generado automáticamente", id),
            campoEnDialogo("Nombre", nombre),
            campoEnDialogo("Marca", marca),
            campoEnDialogo("Categoría", categoria),
            campoEnDialogo("Descripción", descripcion),
            precios,
            campoEnDialogo("Stock inicial", stock)
        );
        formulario.getStyleClass().add("empleado-dialog-content");
        Button botonGuardar = new Button("Guardar producto");
        botonGuardar.getStyleClass().add("boton-mas");
        Button botonCancelarP = new Button("Cancelar");
        botonCancelarP.getStyleClass().add("boton-rol");
        HBox botonesP = new HBox(10, botonGuardar, botonCancelarP);
        botonesP.setAlignment(Pos.CENTER_RIGHT);
        formulario.getChildren().add(botonesP);
        botonCancelarP.setOnAction(e -> com.overbyte.Navegador.cerrarOverlay());
        botonGuardar.setOnAction(e -> {
            if (nombre.getText().isBlank()
                    || precioEntrada.getText().isBlank()
                    || precioVenta.getText().isBlank() || stock.getText().isBlank()) {
                mostrarAlerta("Datos incompletos", "Completa el nombre, precios y stock.");
                return;
            }
            Producto producto;
            try {
                double compra = Double.parseDouble(precioEntrada.getText().trim().replace(',', '.'));
                double venta = Double.parseDouble(precioVenta.getText().trim().replace(',', '.'));
                int unidades = Integer.parseInt(stock.getText().trim());
                if (!Double.isFinite(compra) || !Double.isFinite(venta)
                        || compra < 0 || venta < 0 || unidades < 0) {
                    throw new NumberFormatException();
                }
                producto = new Producto(id.getText().trim(), nombre.getText().trim(), compra, venta,
                    descripcion.getText().trim(), unidades, marca.getText().trim(), categoria.getText().trim());
            } catch (NumberFormatException ex) {
                mostrarAlerta("Datos inválidos",
                    "Los precios deben ser números válidos y el stock un entero igual o mayor que cero.");
                return;
            }
            com.overbyte.Navegador.cerrarOverlay();
            if (gestor.anadirProducto(producto)) {
                mostrarProductos();
                mostrarAlerta("Producto añadido", "El producto se registró correctamente.");
            } else {
                mostrarAlerta("No se pudo añadir", "Verifica los datos e inténtalo nuevamente.");
            }
        });
        com.overbyte.Navegador.mostrarOverlay(
            com.overbyte.Navegador.tarjeta("Añadir producto", "Completa los datos del nuevo producto.", formulario));
    }

    private VBox campoEnDialogo(String titulo, TextField campo) {
        VBox contenedor = new VBox(5, etiquetaCampo(titulo), campo);
        HBox.setHgrow(contenedor, Priority.ALWAYS);
        return contenedor;
    }

    private void eliminarProductoSeleccionado(TableView<String[]> tabla) {
        String[] seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null || seleccionado.length == 0) {
            return;
        }
        com.overbyte.Navegador.confirmar("Eliminar producto",
            "¿Eliminar " + seleccionado[1] + "? Se quitará este producto del inventario. Esta acción no se puede deshacer.",
            () -> {
                if (gestor.eliminarProducto(seleccionado[0])) {
                    mostrarProductos();
                } else {
                    mostrarAlerta("No se pudo eliminar", "El producto ya no existe en el inventario.");
                }
            });
    }

    /* ----------- PROVEEDORES ----------- */

    @FXML
    private void mostrarProveedores() {
        seleccionarPestana(navProveedores);
        lblTituloSeccion.setText("Proveedores");
        contenidoCentro.getChildren().clear();

        VBox seccion = new VBox(15);
        seccion.setAlignment(Pos.TOP_LEFT);
        List<String[]> filas = motorCSV.leerCSV(motorCSV.rutaDatos("proveedores.csv").toString());

        TableView<String[]> tabla = new TableView<>();
        configurarTablaAdmin(tabla);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<String[], String> colId = new TableColumn<>("ID Proveedor");
        colId.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[0]));
        colId.setPrefWidth(190);

        TableColumn<String[], String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[1]));
        colNombre.setPrefWidth(320);

        TableColumn<String[], String> colContacto = new TableColumn<>("Contacto");
        colContacto.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[2]));
        colContacto.setPrefWidth(200);

        tabla.getColumns().addAll(colId, colNombre, colContacto);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length < 3 || fila[0].equalsIgnoreCase("idProveedor")) {
                continue;
            }
            tabla.getItems().add(fila);
        }

        Label titulo = new Label("Directorio de proveedores");
        titulo.getStyleClass().add("ventas-titulo");
        Label subtitulo = new Label("Consulta y administra tus proveedores.");
        subtitulo.getStyleClass().add("ventas-subtitulo");
        VBox encabezado = new VBox(5, titulo, subtitulo);

        Label cantidadProveedores = new Label(tabla.getItems().size() + " proveedores");
        cantidadProveedores.getStyleClass().add("critico-contador");

        Button btnAnadir = new Button("Añadir proveedor");
        btnAnadir.getStyleClass().add("boton-exportar");
        btnAnadir.setOnAction(event -> mostrarDialogoAnadirProveedor());

        Button btnEliminar = new Button("Eliminar seleccionado");
        btnEliminar.getStyleClass().add("boton-eliminar-empleado");
        btnEliminar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        btnEliminar.setOnAction(event -> eliminarProveedorSeleccionado(tabla));

        HBox acciones = new HBox(10, btnAnadir, btnEliminar);
        acciones.setAlignment(Pos.CENTER_RIGHT);
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox cabecera = new HBox(12, encabezado, cantidadProveedores, espacio, acciones);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(12, tabla);
        tarjeta.getStyleClass().add("ventas-tabla-card");
        VBox.setVgrow(tabla, Priority.ALWAYS);
        VBox.setVgrow(tarjeta, Priority.ALWAYS);
        seccion.getChildren().addAll(cabecera, tarjeta);
        VBox.setVgrow(seccion, Priority.ALWAYS);
        seccion.setFillWidth(true);

        ScrollPane scroll = new ScrollPane(seccion);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        contenidoCentro.getChildren().add(scroll);
    }

    private void mostrarDialogoAnadirProveedor() {
        TextField codigo = new TextField();
        codigo.setText(gestor.generarIdProveedor());
        codigo.setEditable(false);
        codigo.getStyleClass().add("identificador-generado");
        TextField nombre = new TextField();
        nombre.setPromptText("Nombre o razón social");
        TextField contacto = new TextField();
        contacto.setPromptText("Teléfono de contacto");

        VBox formulario = new VBox(8,
            campoEnDialogo("ID generado automáticamente", codigo),
            campoEnDialogo("Nombre", nombre),
            campoEnDialogo("Teléfono de contacto", contacto)
        );
        formulario.getStyleClass().add("empleado-dialog-content");
        Button botonGuardar = new Button("Guardar proveedor");
        botonGuardar.getStyleClass().add("boton-mas");
        Button botonCancelarPr = new Button("Cancelar");
        botonCancelarPr.getStyleClass().add("boton-rol");
        HBox botonesPr = new HBox(10, botonGuardar, botonCancelarPr);
        botonesPr.setAlignment(Pos.CENTER_RIGHT);
        formulario.getChildren().add(botonesPr);
        botonCancelarPr.setOnAction(e -> com.overbyte.Navegador.cerrarOverlay());
        botonGuardar.setOnAction(e -> {
            if (nombre.getText().isBlank() || contacto.getText().isBlank()) {
                mostrarAlerta("Datos incompletos", "Completa el nombre y teléfono.");
                return;
            }
            Proveedor proveedor;
            try {
                int telefono = Integer.parseInt(contacto.getText().trim());
                if (telefono <= 0) throw new NumberFormatException();
                proveedor = new Proveedor(nombre.getText().trim(), codigo.getText().trim(), telefono);
            } catch (NumberFormatException ex) {
                mostrarAlerta("Teléfono inválido",
                    "Ingresa un teléfono numérico positivo dentro del rango permitido.");
                return;
            }
            com.overbyte.Navegador.cerrarOverlay();
            if (gestor.anadirProveedor(proveedor)) {
                mostrarProveedores();
                mostrarAlerta("Proveedor añadido", "El proveedor se registró correctamente.");
            } else {
                mostrarAlerta("No se pudo añadir", "Verifica los datos e inténtalo nuevamente.");
            }
        });
        com.overbyte.Navegador.mostrarOverlay(
            com.overbyte.Navegador.tarjeta("Añadir proveedor", "Completa los datos del proveedor.", formulario));
    }

    private void eliminarProveedorSeleccionado(TableView<String[]> tabla) {
        String[] seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null || seleccionado.length == 0) {
            return;
        }
        com.overbyte.Navegador.confirmar("Eliminar proveedor",
            "¿Eliminar a " + seleccionado[1] + "? Se quitará este proveedor del directorio. Esta acción no se puede deshacer.",
            () -> {
                if (gestor.eliminarProveedor(seleccionado[0])) {
                    mostrarProveedores();
                } else {
                    mostrarAlerta("No se pudo eliminar", "El proveedor ya no existe en el directorio.");
                }
            });
    }

    /* ===================== CERRAR SESIÓN ===================== */

    @FXML
    private void cerrarSesion() {
        com.overbyte.Navegador.irAlLogin();
    }

    /* ===================== UTILIDADES ===================== */

    private void configurarTablaAdmin(TableView<String[]> tabla) {
        tabla.getStyleClass().add("tabla-admin");
        tabla.setFixedCellSize(42);
        tabla.setPlaceholder(new Label("No hay registros para mostrar."));
    }

    private Button crearSelectorInventario(String titulo, String detalle, String icono) {
        Label iconoSelector = new Label(icono);
        iconoSelector.getStyleClass().add("selector-inventario-icono");
        Label tituloSelector = new Label(titulo);
        tituloSelector.getStyleClass().add("selector-inventario-titulo");
        Label detalleSelector = new Label(detalle);
        detalleSelector.getStyleClass().add("selector-inventario-detalle");

        VBox contenidoSelector = new VBox(4, iconoSelector, tituloSelector, detalleSelector);
        contenidoSelector.setAlignment(Pos.CENTER_LEFT);

        Button selector = new Button();
        selector.setGraphic(contenidoSelector);
        selector.setMaxWidth(Double.MAX_VALUE);
        selector.setMinHeight(96);
        selector.getStyleClass().add("selector-inventario");
        return selector;
    }

    private void seleccionarApartadoInventario(
            Button seleccionado, VBox vistaSeleccionada,
            Button alternativo, VBox vistaAlternativa) {
        vistaSeleccionada.setVisible(true);
        vistaSeleccionada.setManaged(true);
        vistaAlternativa.setVisible(false);
        vistaAlternativa.setManaged(false);
        seleccionado.getStyleClass().add("selector-inventario-activo");
        alternativo.getStyleClass().remove("selector-inventario-activo");
    }

    private int filasProductosValidas(List<String[]> filas) {
        int cantidad = 0;
        for (int i = 1; i < filas.size(); i++) {
            if (filas.get(i).length >= 8
                    && !filas.get(i)[0].equalsIgnoreCase("idProducto")) {
                cantidad++;
            }
        }
        return cantidad;
    }

    private void seleccionarPestana(Button seleccionada) {
        for (Button pestana : List.of(
                navCaja, navInventario, navVentas, navEntradas,
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
        com.overbyte.Navegador.avisar(titulo, mensaje);
    }
}
