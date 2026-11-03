package com.overbyte.controllers;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
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
import models.Producto;
import user.Empleado;
import user.Vendedor;
import services.MotorCSV;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VendedorController {

    @FXML
    private TextField campoBusqueda;

    @FXML
    private TextField campoCliente;

    @FXML
    private Label lblTotalCarrito;

    @FXML
    private Label etiquetaVendedor;

    @FXML
    private Label mensaje;

    @FXML
    private Label lblTituloSeccion;

    @FXML
    private VBox vistaNuevaVenta;

    @FXML
    private VBox vistaCarrito;

    @FXML
    private Button navNuevaVenta;

    @FXML
    private Button navCarrito;

    @FXML
    private Button navCerrarSesion;

    @FXML
    private VBox tablaProductos;

    @FXML
    private VBox tablaCarrito;

    private Empleado empleado;
    private Vendedor vendedor;
    private MotorCSV motorCSV;

    private final Map<String, Integer> carrito = new LinkedHashMap<>();

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
        this.vendedor = new Vendedor(
            empleado.getCi(),
            empleado.getNombre(),
            empleado.getContrasena()
        );
        this.motorCSV = new MotorCSV();
        etiquetaVendedor.setText("Cajero: " + empleado.getNombre());
    }

    @FXML
    private void initialize() {
        mostrarMensaje("");
        actualizarTotalCarrito();
        configurarAnimacionNavegacion(navNuevaVenta);
        configurarAnimacionNavegacion(navCarrito);
        configurarAnimacionNavegacion(navCerrarSesion);
        mostrarNuevaVenta();
        if (empleado != null) {
            etiquetaVendedor.setText("Cajero: " + empleado.getNombre());
        }
    }

    @FXML
    private void mostrarNuevaVenta() {
        mostrarSeccion(vistaNuevaVenta, navNuevaVenta, "Nueva venta");
    }

    @FXML
    private void mostrarCarrito() {
        mostrarSeccion(vistaCarrito, navCarrito, "Carrito");
    }

    private void mostrarSeccion(VBox vista, Button pestana, String titulo) {
        vistaNuevaVenta.setVisible(vista == vistaNuevaVenta);
        vistaNuevaVenta.setManaged(vista == vistaNuevaVenta);
        vistaCarrito.setVisible(vista == vistaCarrito);
        vistaCarrito.setManaged(vista == vistaCarrito);
        lblTituloSeccion.setText(titulo);
        navNuevaVenta.getStyleClass().remove("nav-button-activo");
        navCarrito.getStyleClass().remove("nav-button-activo");
        if (!pestana.getStyleClass().contains("nav-button-activo")) {
            pestana.getStyleClass().add("nav-button-activo");
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

    /* ===================== BUSCAR PRODUCTOS ===================== */

    @FXML
    private void buscarProducto() {
        tablaProductos.getChildren().clear();
        mostrarMensaje("");

        String termino = campoBusqueda.getText().trim();
        if (termino.isEmpty()) {
            mostrarMensaje("Escribe un código, nombre o marca para buscar.");
            return;
        }

        // Cabecera
        GridPane cabecera = crearCuadriculaProductos();
        cabecera.getStyleClass().add("fila-cabecera");
        agregarCelda(cabecera, new Label("ID"), 0);
        agregarCelda(cabecera, new Label("Nombre"), 1);
        agregarCelda(cabecera, new Label("Marca"), 2);
        agregarCelda(cabecera, new Label("Disponible"), 3);
        agregarCelda(cabecera, new Label("Precio"), 4);
        agregarCelda(cabecera, new Label("Cant."), 5);
        agregarCelda(cabecera, new Label(""), 6);
        tablaProductos.getChildren().add(cabecera);

        Map<String, Object> resultados = vendedor.buscarPorRazon(termino);
        if (resultados.isEmpty()) {
            mostrarMensaje("No se encontraron productos.");
            return;
        }
        for (String idProducto : resultados.keySet()) {
            Producto producto = vendedor.buscarEnInventario(idProducto);
            if (producto != null) {
                agregarFilaProducto(tablaProductos, producto);
            }
        }
    }

    private void agregarFilaProducto(VBox contenedor, Producto producto) {
        GridPane fila = crearCuadriculaProductos();
        fila.getStyleClass().addAll("fila-datos", "venta-producto-fila");

        Label id = new Label(producto.getIdProducto());
        id.getStyleClass().add("venta-producto-id");
        Label nombre = new Label(producto.getNombre());
        nombre.setTooltip(new Tooltip(producto.getNombre()));
        nombre.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        nombre.getStyleClass().add("venta-producto-nombre");
        Label marca = new Label(producto.getMarca());
        marca.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        marca.getStyleClass().add("venta-producto-marca");
        Label stock = new Label(String.valueOf(
            producto.getStock() - carrito.getOrDefault(producto.getIdProducto(), 0)
        ));
        stock.getStyleClass().add("venta-stock-disponible");
        Label precio = new Label(String.format(Locale.ROOT, "$%.2f", producto.getPrecioVenta()));
        precio.getStyleClass().add("venta-producto-precio");

        TextField campoCantidad = new TextField("1");
        campoCantidad.setPromptText("Cant.");
        campoCantidad.getStyleClass().add("venta-cantidad");

        Button btnAgregar = new Button("+");
        btnAgregar.setTooltip(new Tooltip("Agregar al carrito"));
        btnAgregar.getStyleClass().add("boton-anadir-carrito");
        btnAgregar.setOnAction(e -> {
            try {
                int cant = Integer.parseInt(campoCantidad.getText().trim());
                if (cant <= 0) {
                    mostrarMensaje("La cantidad debe ser mayor a cero.");
                    return;
                }
                int cantidadEnCarrito = carrito.getOrDefault(producto.getIdProducto(), 0);
                int cantidadDisponible = producto.getStock() - cantidadEnCarrito;
                if (cant > cantidadDisponible) {
                    mostrarAlertaStockInsuficiente(producto, cantidadDisponible);
                    return;
                }
                carrito.merge(producto.getIdProducto(), cant, Integer::sum);
                stock.setText(String.valueOf(
                    producto.getStock() - carrito.get(producto.getIdProducto())
                ));
                actualizarCarrito();
                actualizarTotalCarrito();
                mostrarMensajeExito("Agregado al carrito: " + producto.getNombre());
            } catch (NumberFormatException ex) {
                mostrarMensaje("Cantidad inválida.");
            }
        });

        agregarCelda(fila, id, 0);
        agregarCelda(fila, nombre, 1);
        agregarCelda(fila, marca, 2);
        agregarCelda(fila, stock, 3);
        agregarCelda(fila, precio, 4);
        agregarCelda(fila, campoCantidad, 5);
        agregarCelda(fila, btnAgregar, 6);
        contenedor.getChildren().add(fila);
    }

    private GridPane crearCuadriculaProductos() {
        GridPane cuadricula = new GridPane();
        cuadricula.setAlignment(Pos.CENTER_LEFT);
        cuadricula.setMaxWidth(Double.MAX_VALUE);
        cuadricula.setHgap(10);
        double[] anchos = {11, 21, 14, 18, 12, 10, 14};
        for (double ancho : anchos) {
            ColumnConstraints columna = new ColumnConstraints();
            columna.setPercentWidth(ancho);
            columna.setHgrow(Priority.ALWAYS);
            columna.setMinWidth(0);
            cuadricula.getColumnConstraints().add(columna);
        }
        return cuadricula;
    }

    private void agregarCelda(GridPane cuadricula, Region nodo, int columna) {
        nodo.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(nodo, Priority.ALWAYS);
        GridPane.setFillWidth(nodo, true);
        cuadricula.add(nodo, columna, 0);
    }

    /* ===================== CARRITO ===================== */

    private void actualizarCarrito() {
        tablaCarrito.getChildren().clear();

        // Cabecera
        GridPane cabecera = crearCuadriculaCarrito();
        cabecera.getStyleClass().add("fila-cabecera");
        agregarCelda(cabecera, new Label("ID"), 0);
        agregarCelda(cabecera, new Label("Producto"), 1);
        agregarCelda(cabecera, new Label("Cant."), 2);
        agregarCelda(cabecera, new Label("Subtotal"), 3);
        agregarCelda(cabecera, new Label(""), 4);
        tablaCarrito.getChildren().add(cabecera);

        double total = 0;
        for (Map.Entry<String, Integer> entrada : carrito.entrySet()) {
            String idProd = entrada.getKey();
            int cantidad = entrada.getValue();
            Producto producto = vendedor.buscarEnInventario(idProd);
            if (producto == null) continue;

            double subtotal = producto.getPrecioVenta() * cantidad;
            total += subtotal;

            GridPane fila = crearCuadriculaCarrito();
            fila.getStyleClass().addAll("fila-datos", "carrito-producto-fila");

            Label id = new Label(idProd);
            Label nombre = new Label(producto.getNombre());
            nombre.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
            nombre.setTooltip(new Tooltip(producto.getNombre()));
            Label cant = new Label(String.valueOf(cantidad));
            Label sub = new Label(String.format(Locale.ROOT, "$%.2f", subtotal));

            Button btnQuitar = new Button("Quitar");
            btnQuitar.setTooltip(new Tooltip("Quitar este producto del carrito"));
            btnQuitar.getStyleClass().add("boton-quitar-linea");
            btnQuitar.setOnAction(e -> {
                carrito.remove(idProd);
                actualizarCarrito();
                actualizarTotalCarrito();
                if (!campoBusqueda.getText().isBlank()) {
                    buscarProducto();
                }
                mostrarMensajeExito("Producto retirado del carrito: " + producto.getNombre());
            });

            agregarCelda(fila, id, 0);
            agregarCelda(fila, nombre, 1);
            agregarCelda(fila, cant, 2);
            agregarCelda(fila, sub, 3);
            agregarCelda(fila, btnQuitar, 4);
            tablaCarrito.getChildren().add(fila);
        }
    }

    private GridPane crearCuadriculaCarrito() {
        GridPane cuadricula = new GridPane();
        cuadricula.setAlignment(Pos.CENTER_LEFT);
        cuadricula.setMaxWidth(Double.MAX_VALUE);
        cuadricula.setHgap(10);
        double[] anchos = {18, 38, 13, 19, 12};
        for (double ancho : anchos) {
            ColumnConstraints columna = new ColumnConstraints();
            columna.setPercentWidth(ancho);
            columna.setHgrow(Priority.ALWAYS);
            columna.setMinWidth(0);
            cuadricula.getColumnConstraints().add(columna);
        }
        return cuadricula;
    }

    private void actualizarTotalCarrito() {
        double total = 0;
        for (Map.Entry<String, Integer> entrada : carrito.entrySet()) {
            Producto p = vendedor.buscarEnInventario(entrada.getKey());
            if (p != null) {
                total += p.getPrecioVenta() * entrada.getValue();
            }
        }
        lblTotalCarrito.setText("Total: " + String.format(Locale.ROOT, "$%.2f", total));
    }

    @FXML
    private void vaciarCarrito() {
        carrito.clear();
        actualizarCarrito();
        actualizarTotalCarrito();
        mostrarMensaje("Carrito vaciado.");
    }

    /* ===================== VENDER ===================== */

    @FXML
    private void confirmarVenta() {
        mostrarMensaje("");
        if (carrito.isEmpty()) {
            mostrarMensaje("El carrito está vacío. Agrega productos primero.");
            return;
        }

        String nombreCliente = validarNombreComprador();
        if (nombreCliente == null || !validarStockCarrito()) {
            return;
        }

        String idOrden = vendedor.generarOrdenDeVenta(nombreCliente, carrito);
        if (idOrden == null) {
            mostrarMensaje("No se pudo preparar la venta. Verifica los datos y que haya stock.");
            return;
        }

        boolean confirmada = vendedor.confirmarVenta(idOrden);
        if (confirmada) {
            mostrarMensajeExito("Venta confirmada: " + idOrden);
            carrito.clear();
            actualizarCarrito();
            actualizarTotalCarrito();
            campoCliente.clear();
        } else {
            mostrarMensaje("La venta no se confirmó. La proforma queda pendiente.");
        }
    }

    @FXML
    private void hacerProforma() {
        mostrarMensaje("");
        if (carrito.isEmpty()) {
            mostrarMensaje("El carrito está vacío. Agrega productos primero.");
            return;
        }

        String nombreCliente = validarNombreComprador();
        if (nombreCliente == null || !validarStockCarrito()) {
            return;
        }

        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar proforma");
        selector.setInitialFileName("proforma-" + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")
        ) + ".txt");
        selector.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivo de texto (*.txt)", "*.txt")
        );
        java.io.File archivo = selector.showSaveDialog(campoBusqueda.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        Path ruta = archivo.toPath();
        if (!ruta.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".txt")) {
            ruta = ruta.resolveSibling(ruta.getFileName() + ".txt");
        }

        String idOrden = vendedor.generarOrdenDeVenta(nombreCliente, carrito);
        if (idOrden == null) {
            mostrarMensaje("No se pudo generar la proforma. Verifica los datos y el stock.");
            return;
        }

        String contenidoProforma = crearContenidoProforma(idOrden, nombreCliente);
        if (contenidoProforma == null) {
            mostrarMensaje("La proforma se registró, pero no se encontraron sus productos para exportar.");
            return;
        }

        try {
            Files.writeString(ruta, contenidoProforma, StandardCharsets.UTF_8);
            mostrarMensajeExito("Proforma " + idOrden + " guardada en: " + ruta);
        } catch (IOException e) {
            mostrarMensaje("La proforma se registró, pero no se pudo guardar el TXT: " + e.getMessage());
        }
    }

    private String validarNombreComprador() {
        String nombreCliente = campoCliente.getText().trim();
        if (nombreCliente.isEmpty()) {
            mostrarMensaje("El nombre del comprador es obligatorio.");
            campoCliente.requestFocus();
            return null;
        }
        return nombreCliente;
    }

    private boolean validarStockCarrito() {
        for (Map.Entry<String, Integer> entrada : carrito.entrySet()) {
            Producto producto = vendedor.buscarEnInventario(entrada.getKey());
            if (producto == null || entrada.getValue() > producto.getStock()) {
                mostrarAlertaStockInsuficiente(producto, producto == null ? 0 : producto.getStock());
                return false;
            }
        }
        return true;
    }

    private String crearContenidoProforma(String idOrden, String nombreCliente) {
        List<String[]> ventas = motorCSV.leerCSV(motorCSV.rutaDatos("ventas.csv").toString());
        List<String[]> lineas = new ArrayList<>();
        double total = 0;
        for (String[] venta : ventas) {
            if (venta.length > 9 && venta[1].equals(idOrden) && venta[2].equals("PROFORMA")) {
                lineas.add(venta);
                total += Double.parseDouble(venta[9]);
            }
        }

        int[] anchos = {12, 30, 8, 14, 14};
        String[] encabezados = {"ID", "PRODUCTO", "CANT.", "PRECIO UNIT.", "SUBTOTAL"};
        for (String[] venta : lineas) {
            anchos[0] = Math.max(anchos[0], venta[5].length());
            anchos[1] = Math.max(anchos[1], venta[6].length());
        }

        if (lineas.isEmpty()) {
            return null;
        }

        String borde = crearBordeProforma(anchos);
        StringBuilder contenido = new StringBuilder()
            .append("TECH-FLOW | PROFORMA DE VENTA\n")
            .append("Proforma: ").append(idOrden).append('\n')
            .append("Comprador: ").append(nombreCliente).append('\n')
            .append("Fecha: ").append(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            )).append("\n\n")
            .append(borde).append('\n')
            .append(filaProforma(encabezados, anchos)).append('\n')
            .append(borde).append('\n');

        for (String[] venta : lineas) {
            String[] columnas = {
                venta[5], venta[6], venta[7],
                String.format(Locale.ROOT, "$%.2f", Double.parseDouble(venta[8])),
                String.format(Locale.ROOT, "$%.2f", Double.parseDouble(venta[9]))
            };
            contenido.append(filaProforma(columnas, anchos)).append('\n');
        }
        int anchoTabla = java.util.Arrays.stream(anchos).sum() + anchos.length * 4 + 1;
        contenido.append(borde).append('\n')
            .append(String.format(Locale.ROOT, "%" + anchoTabla + "s",
                "TOTAL: " + String.format(Locale.ROOT, "$%.2f", total)))
            .append("\n\nEsta proforma es una cotización y no confirma una venta.");
        return contenido.toString();
    }

    private String crearBordeProforma(int[] anchos) {
        StringBuilder borde = new StringBuilder("+");
        for (int ancho : anchos) {
            borde.append("-".repeat(ancho + 2)).append('+');
        }
        return borde.toString();
    }

    private String filaProforma(String[] columnas, int[] anchos) {
        StringBuilder fila = new StringBuilder("|");
        for (int i = 0; i < anchos.length; i++) {
            fila.append(' ').append(String.format("%-" + anchos[i] + "s", columnas[i])).append(" |");
        }
        return fila.toString();
    }

    private void mostrarAlertaStockInsuficiente(Producto producto, int cantidadDisponible) {
        String nombreProducto = producto == null ? "El producto seleccionado" : producto.getNombre();
        String mensajeAlerta = nombreProducto + " no tiene suficientes unidades. "
                + "Cantidad disponible para vender: " + cantidadDisponible + ".";
        mostrarMensaje(mensajeAlerta);

        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Stock insuficiente");
        alerta.setHeaderText("No hay suficientes productos");
        alerta.setContentText(mensajeAlerta);
        if (campoBusqueda.getScene() != null && campoBusqueda.getScene().getWindow() != null) {
            alerta.initOwner(campoBusqueda.getScene().getWindow());
        }
        alerta.showAndWait();
    }

    /* ===================== OTRAS FUNCIONES ===================== */

    @FXML
    private void eliminarVenta() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Anular compra o proforma");
        dialog.setHeaderText("Ingresa el ID de venta u orden que deseas anular:");
        dialog.setContentText("ID:");
        dialog.getEditor().setPromptText("Ej. VEN-12345678 u ORD-12345678");
        if (campoBusqueda.getScene() != null) {
            dialog.initOwner(campoBusqueda.getScene().getWindow());
        }
        dialog.showAndWait().ifPresent(id -> {
            String referencia = id.trim();
            if (referencia.isEmpty()) {
                mostrarMensaje("Ingresa el ID de la compra o proforma.");
                return;
            }

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar anulación");
            confirmacion.setHeaderText("¿Anular la compra o proforma " + referencia + "?");
            confirmacion.setContentText(
                "Las compras confirmadas devolverán sus unidades al inventario. Esta acción no se puede deshacer."
            );
            if (campoBusqueda.getScene() != null) {
                confirmacion.initOwner(campoBusqueda.getScene().getWindow());
            }
            confirmacion.showAndWait().filter(respuesta -> respuesta == ButtonType.OK).ifPresent(respuesta -> {
                if (vendedor.eliminarVenta(referencia)) {
                    if (!campoBusqueda.getText().isBlank()) {
                        buscarProducto();
                    }
                    mostrarMensajeExito("Compra o proforma anulada: " + referencia);
                } else {
                    mostrarMensaje("No se pudo anular. Verifica el ID o el estado de la compra.");
                }
            });
        });
    }

    private void mostrarMensaje(String texto) {
        mensaje.getStyleClass().remove("mensaje-ok");
        mensaje.setText(texto);
    }

    private void mostrarMensajeExito(String texto) {
        if (!mensaje.getStyleClass().contains("mensaje-ok")) {
            mensaje.getStyleClass().add("mensaje-ok");
        }
        mensaje.setText(texto);
    }

    @FXML
    private void consultarGarantia() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Consultar garantía");
        dialog.setHeaderText("Ingresa el código de garantía, venta u orden:");
        dialog.setContentText("Código:");
        dialog.showAndWait().ifPresent(ref -> {
            List<String> resultado = vendedor.consultarGarantia(ref);
            StringBuilder sb = new StringBuilder();
            for (String linea : resultado) {
                sb.append(linea).append("\n");
            }
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Garantía");
            alerta.setHeaderText("Resultado de consulta");
            alerta.setContentText(sb.toString());
            alerta.showAndWait();
        });
    }

    @FXML
    private void importarOrdenDesdeArchivo() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Importar orden");
        dialog.setHeaderText("Ingresa la ruta del archivo CSV/TXT:");
        dialog.setContentText("Ruta:");
        dialog.showAndWait().ifPresent(ruta -> {
            boolean ok = vendedor.ingresarOrdenDesdeArchivo(ruta);
            if (ok) {
                mostrarMensajeExito("Órdenes importadas correctamente.");
            } else {
                mostrarMensaje("No se pudo importar el archivo. Verifica el formato.");
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

            Stage ventana = (Stage) campoBusqueda.getScene().getWindow();
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
