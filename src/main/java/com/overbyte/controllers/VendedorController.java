package com.overbyte.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Producto;
import user.Empleado;
import user.Vendedor;
import services.MotorCSV;

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
    }

    @FXML
    private void initialize() {
        mensaje.setText("");
        actualizarTotalCarrito();
        if (empleado != null) {
            etiquetaVendedor.setText("Vendedor: " + empleado.getNombre());
        }
    }

    /* ===================== BUSCAR PRODUCTOS ===================== */

    @FXML
    private void buscarProducto() {
        tablaProductos.getChildren().clear();

        String termino = campoBusqueda.getText().trim();
        if (termino.isEmpty()) {
            mensaje.setText("Escribe un código, nombre o marca para buscar.");
            return;
        }

        // Cabecera
        HBox cabecera = new HBox(15);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        cabecera.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2563eb;");
        Label colId = new Label("ID");
        colId.setPrefWidth(80);
        Label colNombre = new Label("Nombre");
        colNombre.setPrefWidth(150);
        Label colMarca = new Label("Marca");
        colMarca.setPrefWidth(100);
        Label colStock = new Label("Stock");
        colStock.setPrefWidth(70);
        Label colPrecio = new Label("Precio");
        colPrecio.setPrefWidth(80);
        cabecera.getChildren().addAll(colId, colNombre, colMarca, colStock, colPrecio);
        tablaProductos.getChildren().add(cabecera);
        tablaProductos.getChildren().add(new Separator());

        // Buscar por razón (nombre o marca)
        Map<String, Object> resultados = vendedor.buscarPorRazon(termino);
        if (resultados.isEmpty()) {
            // También buscar por ID exacto
            Producto p = vendedor.buscarEnInventario(termino);
            if (p != null) {
                agregarFilaProducto(tablaProductos, p);
            } else {
                mensaje.setText("No se encontraron productos.");
            }
        } else {
            for (Map.Entry<String, Object> entrada : resultados.entrySet()) {
                Producto p = vendedor.buscarEnInventario(entrada.getKey());
                if (p != null) {
                    agregarFilaProducto(tablaProductos, p);
                }
            }
        }
    }

    private void agregarFilaProducto(VBox contenedor, Producto producto) {
        HBox fila = new HBox(15);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setStyle("-fx-padding: 4 0;");

        Label id = new Label(producto.getIdProducto());
        id.setPrefWidth(80);
        Label nombre = new Label(producto.getNombre());
        nombre.setPrefWidth(150);
        Label marca = new Label(producto.getMarca());
        marca.setPrefWidth(100);
        Label stock = new Label(String.valueOf(producto.getStock()));
        stock.setPrefWidth(70);
        Label precio = new Label(String.format(Locale.ROOT, "$%.2f", producto.getPrecioVenta()));
        precio.setPrefWidth(80);

        TextField campoCantidad = new TextField("1");
        campoCantidad.setPrefWidth(60);
        campoCantidad.setMaxWidth(60);

        Button btnAgregar = new Button("+");
        btnAgregar.setPrefWidth(30);
        btnAgregar.setStyle("-fx-background-color: #4ade80; -fx-text-fill: white;");
        btnAgregar.setOnAction(e -> {
            try {
                int cant = Integer.parseInt(campoCantidad.getText().trim());
                if (cant <= 0) {
                    mensaje.setText("La cantidad debe ser mayor a cero.");
                    return;
                }
                if (cant > producto.getStock()) {
                    mensaje.setText("Stock insuficiente. Disponible: " + producto.getStock());
                    return;
                }
                carrito.merge(producto.getIdProducto(), cant, Integer::sum);
                actualizarCarrito();
                actualizarTotalCarrito();
                mensaje.setText("-fx-text-fill: #4ade80;");
                mensaje.setText("Agregado al carrito: " + producto.getNombre());
            } catch (NumberFormatException ex) {
                mensaje.setText("Cantidad inválida.");
            }
        });

        fila.getChildren().addAll(id, nombre, marca, stock, precio, campoCantidad, btnAgregar);
        contenedor.getChildren().add(fila);
    }

    /* ===================== CARRITO ===================== */

    private void actualizarCarrito() {
        tablaCarrito.getChildren().clear();

        // Cabecera
        HBox cabecera = new HBox(15);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        cabecera.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2563eb;");
        Label colId = new Label("ID");
        colId.setPrefWidth(80);
        Label colNombre = new Label("Producto");
        colNombre.setPrefWidth(120);
        Label colCant = new Label("Cant");
        colCant.setPrefWidth(50);
        Label colSubtotal = new Label("Subtotal");
        colSubtotal.setPrefWidth(80);
        Label colAccion = new Label("");
        colAccion.setPrefWidth(40);
        cabecera.getChildren().addAll(colId, colNombre, colCant, colSubtotal, colAccion);
        tablaCarrito.getChildren().add(cabecera);
        tablaCarrito.getChildren().add(new Separator());

        double total = 0;
        for (Map.Entry<String, Integer> entrada : carrito.entrySet()) {
            String idProd = entrada.getKey();
            int cantidad = entrada.getValue();
            Producto producto = vendedor.buscarEnInventario(idProd);
            if (producto == null) continue;

            double subtotal = producto.getPrecioVenta() * cantidad;
            total += subtotal;

            HBox fila = new HBox(15);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setStyle("-fx-padding: 4 0;");

            Label id = new Label(idProd);
            id.setPrefWidth(80);
            Label nombre = new Label(producto.getNombre());
            nombre.setPrefWidth(120);
            Label cant = new Label(String.valueOf(cantidad));
            cant.setPrefWidth(50);
            Label sub = new Label(String.format(Locale.ROOT, "$%.2f", subtotal));
            sub.setPrefWidth(80);

            Button btnQuitar = new Button("X");
            btnQuitar.setPrefWidth(30);
            btnQuitar.setStyle("-fx-background-color: #fca5a5; -fx-text-fill: white;");
            btnQuitar.setOnAction(e -> {
                carrito.remove(idProd);
                actualizarCarrito();
                actualizarTotalCarrito();
            });

            fila.getChildren().addAll(id, nombre, cant, sub, btnQuitar);
            tablaCarrito.getChildren().add(fila);
        }
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
        mensaje.setText("Carrito vaciado.");
    }

    /* ===================== VENDER ===================== */

    @FXML
    private void venderCarrito() {
        mensaje.setText("");
        if (carrito.isEmpty()) {
            mensaje.setText("El carrito está vacío. Agrega productos primero.");
            return;
        }

        String nombreCliente = campoCliente.getText().trim();
        if (nombreCliente.isEmpty()) {
            nombreCliente = "Consumidor final";
        }

        // Generar orden de venta
        String idOrden = vendedor.generarOrdenDeVenta(nombreCliente, carrito);
        if (idOrden == null) {
            mensaje.setText("No se pudo generar la orden. Verifica que la caja esté abierta y haya stock.");
            return;
        }

        // Confirmar venta (descuenta stock y genera factura)
        boolean confirmada = vendedor.confirmarVenta(idOrden);
        if (confirmada) {
            mensaje.setText("-fx-text-fill: #4ade80;");
            mensaje.setText("Venta confirmada: " + idOrden);
            carrito.clear();
            actualizarCarrito();
            actualizarTotalCarrito();
            campoCliente.clear();
        } else {
            mensaje.setText("La venta no se confirmó. La proforma queda pendiente.");
        }
    }

    /* ===================== OTRAS FUNCIONES ===================== */

    @FXML
    private void eliminarVenta() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Anular venta");
        dialog.setHeaderText("Ingresa el ID de venta u orden a anular:");
        dialog.setContentText("ID:");
        dialog.showAndWait().ifPresent(id -> {
            boolean ok = vendedor.eliminarVenta(id);
            if (ok) {
                mensaje.setText("-fx-text-fill: #4ade80;");
                mensaje.setText("Venta anulada: " + id);
            } else {
                mensaje.setText("No se pudo anular la venta. Verifica el ID.");
            }
        });
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
                mensaje.setText("-fx-text-fill: #4ade80;");
                mensaje.setText("Ordenes importadas correctamente.");
            } else {
                mensaje.setText("No se pudo importar el archivo. Verifica el formato.");
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
