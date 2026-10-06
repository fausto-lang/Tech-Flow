import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import models.Producto;
import services.Verificador;
import user.Almacen;
import user.Empleado;
import user.Gestor;
import user.Rol;
import user.Vendedor;

/** Punto de entrada de la aplicación de inventario y ventas. */
public class Main {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Verificador verificador = new Verificador();
            System.out.println("=== SISTEMA DE INVENTARIO Y VENTAS ===");
            while (scanner.hasNextLine()) {
                Rol rol = seleccionarRol(scanner);
                if (rol == null) {
                    return;
                }

                Empleado operador = autenticar(scanner, verificador, rol);
                if (operador == null) {
                    continue;
                }

                System.out.println("Sesion iniciada: " + operador.getNombre() + " (" + operador.getRol() + ")");
                switch (operador.getRol()) {
                    case ADMINISTRADOR -> menuAdministrador(scanner, (Gestor) operador);
                    case CAJERO -> menuVendedor(scanner, (Vendedor) operador);
                    case ALMACENERO -> menuAlmacen(scanner, (Almacen) operador);
                }
            }
        }
    }

    private static Empleado autenticar(Scanner scanner, Verificador verificador, Rol rol) {
        for (int intento = 1; intento <= 5; intento++) {
            if (!scanner.hasNextLine()) {
                return null;
            }
            String nombre = leerTexto(scanner, "Nombre de usuario: ");
            if (!scanner.hasNextLine()) {
                return null;
            }
            String contrasena = leerTexto(scanner, "Contrasena: ");
            Empleado operador = verificador.iniciarSesion(nombre, contrasena, rol);
            if (operador != null) {
                return operador;
            }
            int restantes = 5 - intento;
            if (restantes > 0) {
                System.out.println("Credenciales incorrectas. Intentos restantes: " + restantes);
            } else {
                System.out.println("Se agotaron los 5 intentos. Volviendo a seleccionar el rol.");
            }
        }
        return null;
    }

    private static Rol seleccionarRol(Scanner scanner) {
        while (true) {
            if (!scanner.hasNextLine()) {
                return null;
            }
            System.out.println("Tipo de operador: 1) Administrador  2) Cajero  3) Almacen  0) Salir del sistema");
            String seleccion = leerTexto(scanner, "> ").trim().toLowerCase();
            switch (seleccion) {
                case "1", "admin", "administrador", "adm" -> { return Rol.ADMINISTRADOR; }
                case "2", "cajero", "vendedor" -> { return Rol.CAJERO; }
                case "3", "almacen", "almacenero" -> { return Rol.ALMACENERO; }
                case "0", "salir" -> { return null; }
                default -> System.out.println("Seleccion no valida.");
            }
        }
    }

    private static void menuAdministrador(Scanner scanner, Gestor gestor) {
        boolean activo = true;
        while (activo) {
                System.out.println("""
                    ADMINISTRADOR | 1 Abrir caja | 2 Cerrar caja | 3 Ventas diarias
                    4 Inventario | 5 Alta empleado | 6 Baja empleado | 7 Alta proveedor
                    8 Baja proveedor | 9 Alta producto | 10 Baja producto | 11 Entradas CSV/TXT
                    12 Entradas de hoy | 0 Cerrar sesion
                    """);
            switch (leerEntero(scanner, "> ")) {
                case 1 -> informar(gestor.abrirCaja());
                case 2 -> informar(gestor.cerrarCaja());
                case 3 -> gestor.verVentasEIngresosDiarios();
                case 4 -> gestor.verInventario();
                case 5 -> altaEmpleado(scanner, gestor);
                case 6 -> informar(gestor.eliminarEmpleado(leerTexto(scanner, "CI del empleado: ")));
                case 7 -> altaProveedor(scanner, gestor);
                case 8 -> informar(gestor.eliminarProveedor(leerTexto(scanner, "ID del proveedor: ")));
                case 9 -> altaProducto(scanner, gestor);
                case 10 -> informar(gestor.eliminarProducto(leerTexto(scanner, "ID del producto: ")));
                case 11 -> informar(gestor.ingresarEntradasDesdeCSV(leerTexto(scanner, "Ruta del CSV/TXT: ")));
                case 12 -> gestor.verAnalisisEntradasDiarias();
                case 0 -> activo = false;
                default -> System.out.println("Opcion no valida.");
            }
        }
    }

    private static void menuAlmacen(Scanner scanner, Almacen almacen) {
        boolean activo = true;
        while (activo) {
                System.out.println("""
                    ALMACEN | 1 Producto nuevo | 2 Recibir producto existente
                    3 Importar entradas CSV/TXT | 0 Cerrar sesion
                    """);
            switch (leerEntero(scanner, "> ")) {
                case 1 -> {
                    Producto producto = pedirProducto(scanner);
                    informar(almacen.ingresarProductoNuevo(producto));
                }
                case 2 -> {
                    String id = leerTexto(scanner, "ID del producto: ");
                    int cantidad = leerEntero(scanner, "Cantidad recibida: ");
                    informar(almacen.ingresarProductoExistente(id, cantidad));
                }
                case 3 -> informar(almacen.ingresarEntradaDesdeArchivo(leerTexto(scanner, "Ruta del CSV/TXT: ")));
                case 0 -> activo = false;
                default -> System.out.println("Opcion no valida.");
            }
        }
    }

    private static void menuVendedor(Scanner scanner, Vendedor vendedor) {
        boolean activo = true;
        while (activo) {
                System.out.println("""
                    CAJERO | 1 Nueva proforma/venta | 2 Importar orden CSV/TXT
                        3 Buscar nombre o marca | 4 Consultar garantia | 0 Cerrar sesion
                    """);
            switch (leerEntero(scanner, "> ")) {
                case 1 -> crearVenta(scanner, vendedor);
                case 2 -> informar(vendedor.ingresarOrdenDesdeArchivo(leerTexto(scanner, "Ruta del CSV/TXT: ")));
                case 3 -> System.out.println(vendedor.buscarPorRazon(leerTexto(scanner, "Nombre o marca: ")));
                case 4 -> consultarGarantia(scanner, vendedor);
                case 0 -> activo = false;
                default -> System.out.println("Opcion no valida.");
            }
        }
    }

    private static void crearVenta(Scanner scanner, Vendedor vendedor) {
        String cliente = leerTexto(scanner, "Nombre del cliente para la factura: ");
        if (cliente.isBlank()) {
            System.out.println("El nombre del cliente es obligatorio.");
            return;
        }
        Map<String, Integer> productos = new LinkedHashMap<>();
        while (true) {
            mostrarCarrito(vendedor, productos);
            System.out.println("1 Anadir producto | 2 Generar venta | 3 Generar proforma"
                    + " | 4 Eliminar producto | 5 Cambiar cantidad | 0 Cancelar");
            switch (leerEntero(scanner, "> ")) {
                case 1 -> anadirAlCarrito(scanner, vendedor, productos);
                case 2 -> {
                    if (productos.isEmpty()) {
                        System.out.println("Anada al menos un producto antes de vender.");
                    } else if (!Verificador.isCajaAbierta()) {
                        System.out.println("La caja esta cerrada. Un administrador debe abrirla para confirmar ventas.");
                    } else {
                        String ciCliente = leerTexto(scanner, "CI o documento del cliente: ");
                        String telefonoCliente = leerTexto(scanner, "Telefono de contacto: ");
                        if (!datosClienteValidos(ciCliente, telefonoCliente)) {
                            System.out.println("CI y telefono son obligatorios; el telefono debe tener entre 7 y 15 digitos.");
                            continue;
                        }
                        String idOrden = vendedor.generarOrdenDeVenta(
                                cliente, ciCliente, telefonoCliente, productos);
                        if (idOrden == null) {
                            System.out.println("No se genero la venta: revise los productos y el stock disponible.");
                        } else if (!vendedor.confirmarVenta(idOrden)) {
                            System.out.println("La venta no se confirmo. La proforma queda pendiente.");
                        }
                        return;
                    }
                }
                case 3 -> {
                    if (productos.isEmpty()) {
                        System.out.println("Anada al menos un producto para generar la proforma.");
                        continue;
                    }
                    String idOrden = vendedor.generarOrdenDeVenta(cliente, productos);
                    if (idOrden == null) {
                        System.out.println("No se genero la proforma: revise los productos y el stock disponible.");
                    } else {
                        imprimirProforma(vendedor, idOrden, cliente, productos);
                    }
                    return;
                }
                case 4 -> eliminarDelCarrito(scanner, vendedor, productos);
                case 5 -> cambiarCantidadCarrito(scanner, vendedor, productos);
                case 0 -> {
                    System.out.println("Carrito cancelado; no se guardo una venta ni una proforma.");
                    return;
                }
                default -> System.out.println("Opcion no valida.");
            }
        }
    }

    private static void mostrarCarrito(Vendedor vendedor, Map<String, Integer> productos) {
        System.out.println("--- CARRITO DE COMPRA ---");
        if (productos.isEmpty()) {
            System.out.println("(vacio)");
            return;
        }
        double total = 0;
        for (Map.Entry<String, Integer> linea : productos.entrySet()) {
            Producto producto = vendedor.buscarEnInventario(linea.getKey());
            if (producto == null) {
                System.out.println(linea.getKey() + " | producto ya no disponible");
                continue;
            }
            double precio = precioUnitario(vendedor, producto);
            double subtotal = precio * linea.getValue();
            total += subtotal;
            System.out.printf(java.util.Locale.ROOT, "%s | %s | %d x $%.2f = $%.2f%n",
                    producto.getIdProducto(), producto.getNombre(), linea.getValue(), precio, subtotal);
        }
        System.out.printf(java.util.Locale.ROOT, "Total estimado: $%.2f%n", total);
    }

    private static void anadirAlCarrito(Scanner scanner, Vendedor vendedor, Map<String, Integer> productos) {
        String busqueda = leerTexto(scanner, "Buscar producto por ID, nombre o marca: ");
        Producto producto = vendedor.buscarEnInventario(busqueda);
        if (producto == null) {
            System.out.println("No se encontro el producto.");
            return;
        }
        int actual = productos.getOrDefault(producto.getIdProducto(), 0);
        System.out.println(producto.getIdProducto() + " | " + producto.getNombre()
                + " | Stock disponible: " + producto.getStock() + " | En carrito: " + actual);
        int cantidad = leerEntero(scanner, "Cantidad a anadir: ");
        if (cantidad <= 0 || actual + cantidad > producto.getStock()) {
            System.out.println("Cantidad invalida o superior al stock disponible; no se modifico el carrito.");
            return;
        }
        productos.merge(producto.getIdProducto(), cantidad, Integer::sum);
    }

    private static void eliminarDelCarrito(Scanner scanner, Vendedor vendedor, Map<String, Integer> productos) {
        String busqueda = leerTexto(scanner, "ID, nombre o marca del producto a eliminar: ");
        Producto producto = vendedor.buscarEnInventario(busqueda);
        if (producto != null && productos.remove(producto.getIdProducto()) != null) {
            System.out.println("Producto eliminado del carrito.");
        } else {
            System.out.println("Ese producto no esta en el carrito.");
        }
    }

    private static void cambiarCantidadCarrito(Scanner scanner, Vendedor vendedor, Map<String, Integer> productos) {
        String busqueda = leerTexto(scanner, "ID, nombre o marca del producto: ");
        Producto producto = vendedor.buscarEnInventario(busqueda);
        if (producto == null || !productos.containsKey(producto.getIdProducto())) {
            System.out.println("Ese producto no esta en el carrito.");
            return;
        }
        int cantidad = leerEntero(scanner, "Nueva cantidad (0 elimina el producto): ");
        if (cantidad == 0) {
            productos.remove(producto.getIdProducto());
        } else if (cantidad < 0 || cantidad > producto.getStock()) {
            System.out.println("Cantidad invalida o superior al stock disponible.");
        } else {
            productos.put(producto.getIdProducto(), cantidad);
        }
    }

    private static void imprimirProforma(Vendedor vendedor, String idOrden, String cliente,
                                         Map<String, Integer> productos) {
        System.out.println("=== PROFORMA ===");
        System.out.println("Orden: " + idOrden + " | Cliente: " + cliente);
        for (Map.Entry<String, Integer> linea : productos.entrySet()) {
            Producto producto = vendedor.buscarEnInventario(linea.getKey());
            if (producto != null) {
                double precio = precioUnitario(vendedor, producto);
                System.out.printf(java.util.Locale.ROOT, "%s x%d | Unitario: $%.2f | Subtotal: $%.2f%n",
                        producto.getNombre(), linea.getValue(), precio, precio * linea.getValue());
            }
        }
        System.out.printf(java.util.Locale.ROOT, "Total cotizado: $%.2f%n", totalCarrito(vendedor, productos));
        System.out.println("La proforma no es una factura final y no descuenta inventario.");
    }

    private static double totalCarrito(Vendedor vendedor, Map<String, Integer> productos) {
        double total = 0;
        for (Map.Entry<String, Integer> linea : productos.entrySet()) {
            Producto producto = vendedor.buscarEnInventario(linea.getKey());
            if (producto != null) {
                total += precioUnitario(vendedor, producto) * linea.getValue();
            }
        }
        return total;
    }

    private static double precioUnitario(Vendedor vendedor, Producto producto) {
        return vendedor.calcularCosto(producto.getPrecioEntrada(), 5.0, producto.getPrecioEntrada() * 0.13);
    }

    private static void consultarGarantia(Scanner scanner, Vendedor vendedor) {
        String referencia = leerTexto(scanner, "Codigo de garantia, ID de venta u orden: ");
        for (String resultado : vendedor.consultarGarantia(referencia)) {
            System.out.println(resultado);
        }
    }

    private static boolean datosClienteValidos(String ci, String telefono) {
        if (ci == null || ci.isBlank() || telefono == null) {
            return false;
        }
        long digitosTelefono = telefono.chars().filter(Character::isDigit).count();
        return digitosTelefono >= 7 && digitosTelefono <= 15;
    }

    private static void altaEmpleado(Scanner scanner, Gestor gestor) {
        String ci = leerTexto(scanner, "CI: ");
        String nombre = leerTexto(scanner, "Nombre: ");
        String contrasena = leerTexto(scanner, "Contrasena: ");
        Rol rol = seleccionarRol(scanner);
        informar(gestor.anadirEmpleado(new Empleado(ci, nombre, contrasena, rol)));
    }

    private static void altaProveedor(Scanner scanner, Gestor gestor) {
        String id = leerTexto(scanner, "ID del proveedor: ");
        String nombre = leerTexto(scanner, "Nombre del proveedor: ");
        int contacto = leerEntero(scanner, "Telefono/contacto numerico: ");
        informar(gestor.anadirProveedor(new models.Proveedor(nombre, id, contacto)));
    }

    private static void altaProducto(Scanner scanner, Gestor gestor) {
        informar(gestor.anadirProducto(pedirProducto(scanner)));
    }

    private static Producto pedirProducto(Scanner scanner) {
        String id = leerTexto(scanner, "ID: ");
        String nombre = leerTexto(scanner, "Nombre: ");
        String marca = leerTexto(scanner, "Marca: ");
        String categoria = leerTexto(scanner, "Categoria: ");
        String descripcion = leerTexto(scanner, "Descripcion: ");
        double costo = leerDecimal(scanner, "Precio de entrada: ");
        double venta = leerDecimal(scanner, "Precio de venta: ");
        int stock = leerEntero(scanner, "Stock inicial: ");
        return new Producto(id, nombre, costo, venta, descripcion, stock, marca, categoria);
    }

    private static String leerTexto(Scanner scanner, String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    private static int leerEntero(Scanner scanner, String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(scanner, mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un numero entero.");
            }
        }
    }

    private static double leerDecimal(Scanner scanner, String mensaje) {
        while (true) {
            try {
                return Double.parseDouble(leerTexto(scanner, mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un numero valido.");
            }
        }
    }

    private static void informar(boolean resultado) {
        System.out.println(resultado ? "Operacion realizada." : "No se pudo completar la operacion.");
    }
}