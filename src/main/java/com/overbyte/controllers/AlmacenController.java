package com.overbyte.controllers;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Producto;
import user.Empleado;
import user.Almacen;
import user.Gestor;
import services.MotorCSV;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AlmacenController {

    @FXML
    private Label etiquetaAlmacen;

    @FXML
    private Label mensaje;

    @FXML
    private Label lblTituloSeccion;

    @FXML
    private VBox paginaInventario;


    @FXML
    private Button navInventario;

    @FXML
    private Button navCerrarSesion;

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
        configurarAnimacionNavegacion(navInventario);
        configurarAnimacionNavegacion(navCerrarSesion);
        mostrarInventario();
        if (empleado != null) {
            etiquetaAlmacen.setText("Almacenero: " + empleado.getNombre());
        }
    }

    @FXML
    private void mostrarInventario() {
        paginaInventario.setVisible(true);
        paginaInventario.setManaged(true);
        lblTituloSeccion.setText("Inventario");
        if (!navInventario.getStyleClass().contains("nav-button-activo")) {
            navInventario.getStyleClass().add("nav-button-activo");
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

    /* ===================== INVENTARIO ===================== */

    private void cargarInventario() {
        tablaInventario.getChildren().clear();

        List<String[]> filas = motorCSV.leerCSV(motorCSV.rutaDatos("inventario.csv").toString());
        GridPane cabecera = crearCuadriculaInventario();
        cabecera.getStyleClass().add("fila-cabecera");
        agregarCeldaInventario(cabecera, new Label("ID"), 0);
        agregarCeldaInventario(cabecera, new Label("Producto"), 1);
        agregarCeldaInventario(cabecera, new Label("Marca"), 2);
        agregarCeldaInventario(cabecera, new Label("Categoría"), 3);
        agregarCeldaInventario(cabecera, new Label("Precio compra"), 4);
        agregarCeldaInventario(cabecera, new Label("Precio venta"), 5);
        agregarCeldaInventario(cabecera, new Label("Existencias"), 6);
        tablaInventario.getChildren().add(cabecera);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length < 8 || fila[0].equalsIgnoreCase("idProducto")) {
                continue;
            }
            GridPane filaH = crearCuadriculaInventario();
            filaH.getStyleClass().addAll("fila-datos", "almacen-producto-fila");
            Label id = new Label(fila[0]);
            id.getStyleClass().add("almacen-producto-id");
            Label nombre = new Label(fila[1]);
            nombre.getStyleClass().add("almacen-producto-nombre");
            nombre.setTooltip(new Tooltip(fila[1]));
            nombre.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
            Label marca = new Label(fila[2]);
            marca.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
            Label categoria = new Label(fila[3]);
            categoria.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
            Label precioE = new Label(formatearPrecio(fila[5]));
            precioE.getStyleClass().add("almacen-precio");
            Label precioV = new Label(formatearPrecio(fila[6]));
            precioV.getStyleClass().add("almacen-precio-venta");
            Label stock = new Label(fila[7]);
            stock.getStyleClass().add("almacen-stock");

            try {
                int existencias = Integer.parseInt(fila[7]);
                stock.getStyleClass().add(existencias <= 5 ? "almacen-stock-bajo" : "almacen-stock-normal");
                stock.setTooltip(new Tooltip(existencias <= 5
                    ? "Existencias bajas: quedan " + existencias + " unidades"
                    : existencias + " unidades disponibles"));
            } catch (NumberFormatException e) {
                stock.getStyleClass().add("almacen-stock-invalido");
                stock.setTooltip(new Tooltip("El valor de existencias no es válido"));
            }

            agregarCeldaInventario(filaH, id, 0);
            agregarCeldaInventario(filaH, nombre, 1);
            agregarCeldaInventario(filaH, marca, 2);
            agregarCeldaInventario(filaH, categoria, 3);
            agregarCeldaInventario(filaH, precioE, 4);
            agregarCeldaInventario(filaH, precioV, 5);
            agregarCeldaInventario(filaH, stock, 6);
            tablaInventario.getChildren().add(filaH);
        }
    }

    private GridPane crearCuadriculaInventario() {
        GridPane cuadricula = new GridPane();
        cuadricula.setAlignment(Pos.CENTER_LEFT);
        cuadricula.setMaxWidth(Double.MAX_VALUE);
        cuadricula.setHgap(12);
        double[] anchos = {13, 23, 13, 16, 13, 12, 10};
        for (double ancho : anchos) {
            ColumnConstraints columna = new ColumnConstraints();
            columna.setPercentWidth(ancho);
            columna.setHgrow(Priority.ALWAYS);
            columna.setMinWidth(0);
            cuadricula.getColumnConstraints().add(columna);
        }
        return cuadricula;
    }

    private void agregarCeldaInventario(GridPane cuadricula, Region nodo, int columna) {
        nodo.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(nodo, Priority.ALWAYS);
        GridPane.setFillWidth(nodo, true);
        cuadricula.add(nodo, columna, 0);
    }

    private String formatearPrecio(String precio) {
        try {
            return String.format(Locale.ROOT, "$%.2f", Double.parseDouble(precio));
        } catch (NumberFormatException e) {
            return precio;
        }
    }

    /* ===================== REGISTRAR PRODUCTO ===================== */

    @FXML
    private void mostrarDialogoRegistrarProducto() {
        TextField id = new TextField(new Gestor(
                empleado.getCi(), empleado.getNombre(), empleado.getContrasena()
        ).generarIdProducto());
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

        HBox precios = new HBox(12,
                campoDialogo("Precio de compra", precioEntrada),
                campoDialogo("Precio de venta", precioVenta));
        HBox.setHgrow(precios.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(precios.getChildren().get(1), Priority.ALWAYS);
        VBox formulario = new VBox(10,
                campoDialogo("ID generado automáticamente", id),
                campoDialogo("Nombre", nombre),
                campoDialogo("Marca", marca),
                campoDialogo("Categoría", categoria),
                campoDialogo("Descripción", descripcion),
                precios,
                campoDialogo("Stock inicial", stock));
        formulario.getStyleClass().add("empleado-dialog-content");
        Button botonGuardar = new Button("Guardar producto");
        botonGuardar.getStyleClass().add("boton-mas");
        Button botonCancelarA = new Button("Cancelar");
        botonCancelarA.getStyleClass().add("boton-rol");
        HBox botonesA = new HBox(10, botonGuardar, botonCancelarA);
        botonesA.setAlignment(Pos.CENTER_RIGHT);
        formulario.getChildren().add(botonesA);
        botonCancelarA.setOnAction(e -> com.overbyte.Navegador.cerrarOverlay());
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
            if (almacen.ingresarProductoNuevo(producto)) {
                cargarInventario();
                mostrarMensaje("El producto se registró correctamente.", true);
                mostrarAlerta("Producto añadido", "El producto se registró correctamente.");
            } else {
                mostrarMensaje("No se pudo registrar el producto. Verifica los datos.", false);
                mostrarAlerta("No se pudo añadir", "Verifica los datos e inténtalo nuevamente.");
            }
        });
        com.overbyte.Navegador.mostrarOverlay(
            com.overbyte.Navegador.tarjeta("Registrar producto", "Completa los datos para agregar un producto al inventario.", formulario));
    }

    /* ===================== ENTRADAS Y STOCK ===================== */

    @FXML
    private void mostrarDialogoEntradas() {
        TextField busqueda = new TextField();
        busqueda.setPromptText("ID, nombre, marca, categoría o descripción");
        busqueda.getStyleClass().add("campo");
        Label estadoBusqueda = new Label();
        estadoBusqueda.getStyleClass().add("texto-secundario");
        VBox resultados = new VBox(5);
        resultados.getStyleClass().add("lista-productos-contenido");
        ScrollPane scrollResultados = new ScrollPane(resultados);
        scrollResultados.setFitToWidth(true);
        scrollResultados.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollResultados.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollResultados.getStyleClass().add("lista-venta");
        VBox.setVgrow(scrollResultados, Priority.ALWAYS);

        VBox carrito = new VBox(7);
        carrito.getStyleClass().add("lista-carrito-contenido");
        ScrollPane scrollCarrito = new ScrollPane(carrito);
        scrollCarrito.setFitToWidth(true);
        scrollCarrito.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollCarrito.getStyleClass().add("lista-carrito");
        VBox.setVgrow(scrollCarrito, Priority.ALWAYS);
        Map<String, Integer> seleccionados = new LinkedHashMap<>();
        List<Producto> productos = new ArrayList<>();

        Runnable buscar = () -> {
            int invalidos = cargarProductosInventario(productos);
            mostrarResultadosEntradas(resultados, productos, busqueda.getText(), carrito, seleccionados);
            estadoBusqueda.setText(resultados.getChildren().size() <= 1
                    ? "No se encontraron productos."
                    : (resultados.getChildren().size() - 1) + " productos encontrados."
                      + (invalidos > 0 ? " Algunos registros tienen datos inválidos y no se muestran." : ""));
        };
        Button botonBuscar = new Button("BUSCAR");
        botonBuscar.getStyleClass().add("boton-principal");
        botonBuscar.setOnAction(event -> buscar.run());
        busqueda.setOnAction(event -> buscar.run());
        HBox barraBusqueda = new HBox(8, busqueda, botonBuscar);
        HBox.setHgrow(busqueda, Priority.ALWAYS);

        Label tituloResultados = new Label("PRODUCTOS DISPONIBLES");
        tituloResultados.getStyleClass().add("venta-subtitulo");
        Label tituloCarrito = new Label("ENTRADAS SELECCIONADAS");
        tituloCarrito.getStyleClass().add("venta-subtitulo");
        VBox panelResultados = new VBox(8, tituloResultados, scrollResultados);
        panelResultados.getStyleClass().add("entrada-panel");
        panelResultados.setMinWidth(0);
        panelResultados.setMaxWidth(Double.MAX_VALUE);
        VBox panelCarrito = new VBox(8, tituloCarrito, scrollCarrito);
        panelCarrito.getStyleClass().add("entrada-panel");
        panelCarrito.setMinWidth(0);
        panelCarrito.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(panelResultados, Priority.ALWAYS);
        HBox.setHgrow(panelCarrito, Priority.ALWAYS);
        panelResultados.setPrefWidth(Region.USE_PREF_SIZE);
        HBox listas = new HBox(12, panelResultados, panelCarrito);
        listas.setMaxWidth(Double.MAX_VALUE);
        listas.setMinWidth(0);
        VBox.setVgrow(listas, Priority.ALWAYS);
        listas.setMinHeight(300);

        Button importar = new Button("IMPORTAR ENTRADAS CSV");
        importar.getStyleClass().add("boton-rol");
        importar.setOnAction(event -> {
            if (importarEntradasDesdeArchivo()) {
                buscar.run();
                actualizarCarritoEntradas(carrito, productos, seleccionados);
            }
        });
        Button registrar = new Button("REGISTRAR ENTRADAS");
        registrar.getStyleClass().add("boton-principal");
        registrar.setOnAction(event -> registrarEntradasSeleccionadas(
                productos, seleccionados, carrito, resultados, busqueda.getText()));
        HBox acciones = new HBox(10, importar, registrar);
        acciones.setAlignment(Pos.CENTER_RIGHT);

        VBox contenido = new VBox(12,
                new Label("Buscar producto"),
                barraBusqueda,
                estadoBusqueda,
                listas,
                acciones);
        contenido.getStyleClass().add("entrada-dialog-content");
        Button cerrar = new Button("Cerrar");
        cerrar.getStyleClass().add("boton-rol");
        cerrar.setOnAction(e -> com.overbyte.Navegador.cerrarOverlay());
        acciones.getChildren().add(cerrar);

        actualizarCarritoEntradas(carrito, productos, seleccionados);
        buscar.run();
        VBox tarjeta = com.overbyte.Navegador.tarjeta("Entradas y stock",
            "Busca productos por ID, nombre, marca, categoría o descripción y añádelos al carrito.", contenido);
        tarjeta.getStyleClass().remove("overlay-tarjeta");
        tarjeta.getStyleClass().add("overlay-tarjeta--ancha");
        contenido.setMaxWidth(Double.MAX_VALUE);
        contenido.setMinWidth(0);
        double anchoVentana = com.overbyte.Navegador.ventana() != null
                ? com.overbyte.Navegador.ventana().getWidth() : 1100;
        double altoVentana = com.overbyte.Navegador.ventana() != null
                ? com.overbyte.Navegador.ventana().getHeight() : 620;
        double ancho = Math.max(880, anchoVentana * 0.92);
        double alto = Math.max(460, altoVentana * 0.86);
        tarjeta.setMinWidth(ancho);
        tarjeta.setPrefWidth(ancho);
        tarjeta.setMaxWidth(ancho);
        tarjeta.setPrefHeight(alto);
        tarjeta.setMaxHeight(alto);
        panelResultados.setPrefWidth(ancho * 0.63);
        panelCarrito.setPrefWidth(ancho * 0.37);
        scrollResultados.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollCarrito.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        com.overbyte.Navegador.mostrarOverlay(tarjeta);
    }

    private void mostrarResultadosEntradas(VBox contenedor, List<Producto> productos, String termino,
            VBox carrito, Map<String, Integer> seleccionados) {
        contenedor.getChildren().clear();
        GridPane cabecera = crearCuadriculaEntradas();
        cabecera.getStyleClass().add("fila-cabecera");
        String[] titulos = {"ID", "Producto", "Marca", "Categoría", "Compra", "Stock", "Añadir"};
        for (int i = 0; i < titulos.length; i++) {
            agregarCeldaEntrada(cabecera, new Label(titulos[i]), i);
        }
        contenedor.getChildren().add(cabecera);
        String consulta = termino == null ? "" : termino.trim().toLowerCase(Locale.ROOT);
        for (Producto producto : productos) {
            String textoProducto = String.join(" ",
                    producto.getIdProducto(), producto.getNombre(), producto.getMarca(),
                    producto.getCategoria(), producto.getDescripcion()).toLowerCase(Locale.ROOT);
            if (!consulta.isEmpty() && !textoProducto.contains(consulta)) {
                continue;
            }
            GridPane fila = crearCuadriculaEntradas();
            fila.getStyleClass().addAll("fila-datos", "entrada-producto-fila");
            Label id = new Label(producto.getIdProducto());
            id.getStyleClass().add("venta-producto-id");
            Label nombre = new Label(producto.getNombre());
            nombre.setTooltip(new Tooltip(producto.getNombre()));
            nombre.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
            Label marca = new Label(producto.getMarca());
            Label categoria = new Label(producto.getCategoria());
            Label precio = new Label(String.format(Locale.ROOT, "$%.2f", producto.getPrecioEntrada()));
            Label stock = new Label(String.valueOf(producto.getStock()));
            stock.getStyleClass().add("venta-stock-disponible");
            TextField cantidad = new TextField("1");
            cantidad.setPromptText("Cant.");
            cantidad.getStyleClass().add("venta-cantidad");
            Button agregar = new Button("+");
            agregar.getStyleClass().add("boton-anadir-carrito");
            agregar.setTooltip(new Tooltip("Añadir al carrito de entradas"));
            agregar.setOnAction(event -> {
                try {
                    int unidades = Integer.parseInt(cantidad.getText().trim());
                    if (unidades <= 0) {
                        mostrarAlerta("Cantidad inválida", "La cantidad debe ser mayor a cero.");
                        return;
                    }
                    int cantidadSeleccionada = seleccionados.getOrDefault(producto.getIdProducto(), 0);
                    if (producto.getStock() < 0
                            || unidades > Integer.MAX_VALUE - producto.getStock()
                            || unidades > Integer.MAX_VALUE - cantidadSeleccionada) {
                        mostrarAlerta("Cantidad fuera de rango",
                                "La cantidad excede el máximo permitido para el stock de este producto.");
                        return;
                    }
                    seleccionados.put(producto.getIdProducto(), cantidadSeleccionada + unidades);
                    actualizarCarritoEntradas(carrito, productos, seleccionados);
                } catch (NumberFormatException e) {
                    mostrarAlerta("Cantidad inválida", "Escribe un número entero mayor a cero.");
                }
            });
            agregarCeldaEntrada(fila, id, 0);
            agregarCeldaEntrada(fila, nombre, 1);
            agregarCeldaEntrada(fila, marca, 2);
            agregarCeldaEntrada(fila, categoria, 3);
            agregarCeldaEntrada(fila, precio, 4);
            agregarCeldaEntrada(fila, stock, 5);
            HBox controles = new HBox(4, cantidad, agregar);
            controles.setAlignment(Pos.CENTER);
            agregarCeldaEntrada(fila, controles, 6);
            contenedor.getChildren().add(fila);
        }
    }

    private GridPane crearCuadriculaEntradas() {
        GridPane cuadricula = new GridPane();
        cuadricula.setAlignment(Pos.CENTER_LEFT);
        cuadricula.setMaxWidth(Double.MAX_VALUE);
        cuadricula.setHgap(8);
        double[] anchos = {14, 20, 12, 14, 12, 10, 18};
        for (double ancho : anchos) {
            ColumnConstraints columna = new ColumnConstraints();
            columna.setPercentWidth(ancho);
            columna.setHgrow(Priority.ALWAYS);
            columna.setMinWidth(0);
            cuadricula.getColumnConstraints().add(columna);
        }
        return cuadricula;
    }

    private void agregarCeldaEntrada(GridPane cuadricula, Region nodo, int columna) {
        nodo.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(nodo, Priority.ALWAYS);
        GridPane.setFillWidth(nodo, true);
        cuadricula.add(nodo, columna, 0);
    }

    private int cargarProductosInventario(List<Producto> productos) {
        productos.clear();
        List<String[]> filas = motorCSV.leerCSV(motorCSV.rutaDatos("inventario.csv").toString());
        int invalidos = 0;
        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length < 8 || fila[0].equalsIgnoreCase("idProducto")) {
                invalidos++;
                continue;
            }
            try {
                productos.add(new Producto(fila[0], fila[1],
                        Double.parseDouble(fila[5].trim().replace(',', '.')),
                        Double.parseDouble(fila[6].trim().replace(',', '.')),
                        fila[4], Integer.parseInt(fila[7].trim()), fila[2], fila[3]));
            } catch (NumberFormatException e) {
                invalidos++;
            }
        }
        return invalidos;
    }

    private void actualizarCarritoEntradas(VBox contenedor, List<Producto> productos,
                                           Map<String, Integer> seleccionados) {
        contenedor.getChildren().clear();
        if (seleccionados.isEmpty()) {
            contenedor.getChildren().add(new Label("Aún no agregas productos."));
            return;
        }
        for (Map.Entry<String, Integer> entrada : seleccionados.entrySet()) {
            Producto producto = buscarProducto(productos, entrada.getKey());
            if (producto == null) {
                continue;
            }
            HBox fila = new HBox(8);
            fila.getStyleClass().addAll("fila-datos", "entrada-carrito-fila");
            VBox detalles = new VBox(3,
                    new Label(producto.getNombre()),
                    new Label(producto.getIdProducto() + " · " + entrada.getValue() + " unidades"));
            detalles.getChildren().get(0).getStyleClass().add("almacen-producto-nombre");
            detalles.getChildren().get(1).getStyleClass().add("texto-secundario");
            HBox.setHgrow(detalles, Priority.ALWAYS);
            Button quitar = new Button("Quitar");
            quitar.getStyleClass().add("boton-eliminar-empleado");
            quitar.setOnAction(event -> {
                seleccionados.remove(entrada.getKey());
                actualizarCarritoEntradas(contenedor, productos, seleccionados);
            });
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.getChildren().addAll(detalles, quitar);
            contenedor.getChildren().add(fila);
        }
    }

    private Producto buscarProducto(List<Producto> productos, String id) {
        for (Producto producto : productos) {
            if (producto.getIdProducto().equalsIgnoreCase(id)) {
                return producto;
            }
        }
        return null;
    }

    private void registrarEntradasSeleccionadas(List<Producto> productos,
            Map<String, Integer> seleccionados, VBox carrito, VBox resultados, String termino) {
        if (seleccionados.isEmpty()) {
            mostrarAlerta("Carrito vacío", "Añade al menos un producto antes de registrar entradas.");
            return;
        }
        int registrados = 0;
        List<String> errores = new ArrayList<>();
        for (Map.Entry<String, Integer> entrada : new LinkedHashMap<>(seleccionados).entrySet()) {
            Producto producto = buscarProducto(productos, entrada.getKey());
            if (producto != null
                    && almacen.ingresarProductoExistente(producto.getIdProducto(), entrada.getValue())) {
                seleccionados.remove(entrada.getKey());
                registrados++;
            } else {
                errores.add(entrada.getKey());
            }
        }
        cargarInventario();
        cargarProductosInventario(productos);
        mostrarResultadosEntradas(resultados, productos, termino, carrito, seleccionados);
        actualizarCarritoEntradas(carrito, productos, seleccionados);
        if (!errores.isEmpty()) {
            mostrarAlerta(registrados == 0 ? "No se registraron entradas" : "Entradas parciales",
                    (registrados > 0 ? registrados + " producto(s) registrados. " : "")
                    + "No se pudieron procesar: " + String.join(", ", errores) + ".");
            return;
        }
        mostrarMensaje("Se registraron las entradas correctamente.", true);
        mostrarAlerta("Entradas registradas", "Se actualizaron las existencias de "
                + registrados + " producto(s).");
        com.overbyte.Navegador.cerrarOverlay();
    }

    private boolean importarEntradasDesdeArchivo() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Seleccionar archivo de entradas");
        selector.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos CSV y texto", "*.csv", "*.txt"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));
        File archivo = selector.showOpenDialog(com.overbyte.Navegador.ventana());
        if (archivo == null) {
            return false;
        }
        mostrarVistaPreviaEntradas(archivo);
        return true;
    }

    /** Preview GUI-only: muestra las filas leidas antes de confirmar. No escribe nada. */
    private void mostrarVistaPreviaEntradas(File archivo) {
        List<String[]> filas = motorCSV.leerCSV(archivo.getAbsolutePath());
        if (filas.size() < 2) {
            mostrarAlerta("Archivo vacío", "El archivo no tiene datos para importar.");
            return;
        }
        String[] encabezado = filas.get(0);
        int colProveedor = indiceColumna(encabezado, "idProveedor");
        int colProducto = indiceColumna(encabezado, "idProducto");
        int colCantidad = indiceColumna(encabezado, "cantidad");
        java.util.Set<String> proveedores = new java.util.HashSet<>();
        for (String[] fila : motorCSV.leerCSV(motorCSV.rutaDatos("proveedores.csv").toString())) {
            if (fila.length > 0) proveedores.add(fila[0].trim().toLowerCase(Locale.ROOT));
        }
        TableView<String[]> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(280);
        int columnas = Math.min(encabezado.length, 7);
        for (int c = 0; c < columnas; c++) {
            final int idx = c;
            TableColumn<String[], String> col = new TableColumn<>(encabezado[c]);
            col.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(
                    idx < p.getValue().length ? p.getValue()[idx] : "—"));
            tabla.getColumns().add(col);
        }
        int invalidas = 0;
        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            tabla.getItems().add(fila);
            if (!filaEntradaValida(fila, colProveedor, colProducto, colCantidad, proveedores)) invalidas++;
        }
        Label resumen = new Label((filas.size() - 1) + " filas leídas · " + invalidas + " con formato inválido");
        resumen.getStyleClass().add(invalidas > 0 ? "mensaje" : "mensaje-ok");
        Button confirmar = new Button("CONFIRMAR IMPORTACIÓN");
        confirmar.getStyleClass().add("boton-principal");
        Button cancelar = new Button("Cancelar");
        cancelar.getStyleClass().add("boton-rol");
        confirmar.setOnAction(e -> {
            com.overbyte.Navegador.cerrarOverlay();
            if (almacen.ingresarEntradaDesdeArchivo(archivo.getAbsolutePath())) {
                cargarInventario();
                mostrarMensaje("Entradas importadas correctamente.", true);
                mostrarAlerta("Importación completada", "Las entradas se importaron correctamente.");
            } else {
                mostrarMensaje("No se pudo importar el archivo.", false);
                mostrarAlerta("No se pudo importar",
                        "Verifica el formato y las columnas del archivo seleccionado.");
            }
        });
        cancelar.setOnAction(e -> com.overbyte.Navegador.cerrarOverlay());
        HBox botones = new HBox(10, confirmar, cancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);
        VBox cuerpo = new VBox(10, resumen, tabla, botones);
        cuerpo.setMaxWidth(Double.MAX_VALUE);
        VBox tarjeta = com.overbyte.Navegador.tarjeta("Vista previa de importación",
                "Archivo: " + archivo.getName() + ". Revisa si está completo y confirma para guardar.", cuerpo);
        tarjeta.getStyleClass().remove("overlay-tarjeta");
        tarjeta.getStyleClass().add("overlay-tarjeta--ancha");
        double anchoVentana = com.overbyte.Navegador.ventana() != null
                ? com.overbyte.Navegador.ventana().getWidth() : 1100;
        double ancho = Math.max(880, anchoVentana * 0.92);
        tarjeta.setMinWidth(ancho);
        tarjeta.setPrefWidth(ancho);
        tarjeta.setMaxWidth(ancho);
        com.overbyte.Navegador.mostrarOverlay(tarjeta);
    }

    private int indiceColumna(String[] encabezado, String nombre) {
        for (int i = 0; i < encabezado.length; i++) {
            if (encabezado[i].trim().equalsIgnoreCase(nombre)) return i;
        }
        return -1;
    }

    private boolean filaEntradaValida(String[] fila, int colProveedor, int colProducto,
                                       int colCantidad, java.util.Set<String> proveedores) {
        if (colProducto < 0 || colCantidad < 0 || colProducto >= fila.length || colCantidad >= fila.length) return false;
        if (fila[colProducto].isBlank()) return false;
        try {
            if (Integer.parseInt(fila[colCantidad].trim()) <= 0) return false;
        } catch (NumberFormatException ex) {
            return false;
        }
        if (colProveedor >= 0 && colProveedor < fila.length && !fila[colProveedor].isBlank()
                && !proveedores.contains(fila[colProveedor].trim().toLowerCase(Locale.ROOT))) return false;
        return true;
    }

    private VBox campoDialogo(String titulo, TextInputControl control) {
        VBox campo = new VBox(5, new Label(titulo), control);
        campo.getChildren().get(0).getStyleClass().add("etiqueta");
        control.getStyleClass().add("campo");
        VBox.setVgrow(control, Priority.NEVER);
        return campo;
    }

    private void mostrarAlerta(String titulo, String contenido) {
        com.overbyte.Navegador.avisar(titulo, contenido);
    }

    private void mostrarMensaje(String texto, boolean correcto) {
        mensaje.setText(texto);
        mensaje.getStyleClass().remove("mensaje-ok");
        if (correcto && !mensaje.getStyleClass().contains("mensaje-ok")) {
            mensaje.getStyleClass().add("mensaje-ok");
        }
    }

    /* ===================== CERRAR SESIÓN ===================== */

    @FXML
    private void cerrarSesion() {
        com.overbyte.Navegador.irAlLogin();
    }

}
