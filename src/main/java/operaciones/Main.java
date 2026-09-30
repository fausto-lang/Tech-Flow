package operaciones;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        EntradaProducto servicioEntrada =
                new EntradaProducto();

        GestionProducto gestionProducto =
                new GestionProducto();

        int opcion = -1;

        while (opcion != 0) {

            mostrarMenu();

            try {

                System.out.print(
                        "Seleccione una opción: "
                );

                opcion = Integer.parseInt(
                        scanner.nextLine().trim()
                );

                switch (opcion) {

                    case 1:
                        registrarEntrada(
                                servicioEntrada,
                                scanner
                        );
                        break;

                    case 2:
                        registrarVenta(
                                gestionProducto,
                                scanner
                        );
                        break;

                    case 3:
                        verStockActual(
                                gestionProducto
                        );
                        break;

                    case 4:
                        verProveedores(
                                gestionProducto
                        );
                        break;

                    case 5:
                        verVentasDia(
                                gestionProducto
                        );
                        break;

                    case 6:
                        calcularVentasDia(
                                gestionProducto
                        );
                        break;

                    case 0:
                        System.out.println(
                                "Saliendo del sistema..."
                        );
                        break;

                    default:
                        System.out.println(
                                "Opción no válida."
                        );
                }

            } catch (NumberFormatException e) {

                System.out.println(
                        "Error: Ingrese un número válido."
                );

            } catch (Exception e) {

                System.out.println(
                        "Error inesperado: "
                                + e.getMessage()
                );
            }

            System.out.println();
        }

        scanner.close();
    }

    // ==========================================================
    // MENÚ
    // ==========================================================

    private static void mostrarMenu() {

        System.out.println(
                "================================="
        );

        System.out.println(
                "    SISTEMA DE GESTIÓN (TECH-FLOW)"
        );

        System.out.println(
                "================================="
        );

        System.out.println(
                "1. Registrar Ingreso"
        );

        System.out.println(
                "2. Registrar Venta"
        );

        System.out.println(
                "3. Ver Stock"
        );

        System.out.println(
                "4. Ver Proveedores"
        );

        System.out.println(
                "5. Ver Ventas del Día"
        );

        System.out.println(
                "6. Calcular Ventas del Día"
        );

        System.out.println(
                "0. Salir"
        );

        System.out.println(
                "================================="
        );
    }

    // ==========================================================
    // REGISTRAR INGRESO
    // ==========================================================

    private static void registrarEntrada(
            EntradaProducto servicioEntrada,
            Scanner scanner) {

        try {

            System.out.println(
                    "\n--- REGISTRAR INGRESO ---"
            );

            // --------------------------------------------------
            // PROVEEDOR
            // --------------------------------------------------

            System.out.print(
                    "Código del proveedor: "
            );

            String codigoProveedor =
                    scanner.nextLine()
                           .trim()
                           .toUpperCase();

            Proveedor proveedor =
                    servicioEntrada.buscarProveedor(
                            codigoProveedor
                    );

            if (proveedor != null) {

                System.out.println(
                        "-> Proveedor encontrado: "
                                + proveedor.getNombreProveedor()
                );

            } else {

                System.out.println(
                        "-> Proveedor no encontrado."
                );

                System.out.println(
                        "Se registrará como proveedor nuevo."
                );

                String nuevoCodigo =
                        servicioEntrada.generarCodigoProveedor();

                System.out.println(
                        "Nuevo código generado: "
                                + nuevoCodigo
                );

                System.out.print(
                        "Nombre del proveedor: "
                );

                String nombreProveedor =
                        scanner.nextLine()
                               .trim()
                               .toUpperCase();

                System.out.print(
                        "Contacto (Teléfono): "
                );

                int contacto =
                        Integer.parseInt(
                                scanner.nextLine()
                                       .trim()
                        );

                proveedor =
                        new Proveedor(
                                nombreProveedor,
                                nuevoCodigo,
                                contacto
                        );
            }

            // --------------------------------------------------
            // TIPO DE PRODUCTO
            // --------------------------------------------------

            System.out.println();
            System.out.println(
                    "1. Registrar producto nuevo"
            );

            System.out.println(
                    "2. Registrar producto existente"
            );

            System.out.print(
                    "Seleccione una opción: "
            );

            int tipoProducto =
                    Integer.parseInt(
                            scanner.nextLine()
                                   .trim()
                    );

            // ==================================================
            // PRODUCTO NUEVO
            // ==================================================

            if (tipoProducto == 1) {

                String idProducto =
                        servicioEntrada.generarIdProducto();

                System.out.println(
                        "ID generado automáticamente: "
                                + idProducto
                );

                System.out.print(
                        "Nombre del producto: "
                );

                String nombre =
                        scanner.nextLine()
                               .trim()
                               .toUpperCase();

                System.out.print(
                        "Marca: "
                );

                String marca =
                        scanner.nextLine()
                               .trim()
                               .toUpperCase();

                System.out.print(
                        "Categoría: "
                );

                String categoria =
                        scanner.nextLine()
                               .trim()
                               .toUpperCase();

                System.out.print(
                        "Descripción: "
                );

                String descripcion =
                        scanner.nextLine()
                               .trim();

                System.out.print(
                        "Cantidad a ingresar: "
                );

                int cantidad =
                        Integer.parseInt(
                                scanner.nextLine()
                                       .trim()
                        );

                System.out.print(
                        "Precio de entrada: "
                );

                double precio =
                        Double.parseDouble(
                                scanner.nextLine()
                                       .trim()
                        );

                Producto producto =
                        new Producto(
                                idProducto,
                                marca,
                                precio,
                                descripcion,
                                0,
                                nombre,
                                categoria
                        );

                servicioEntrada.registrarPedido(
                        proveedor,
                        producto,
                        cantidad,
                        precio
                );

                System.out.println(
                        "✔ Producto registrado correctamente."
                );

                System.out.println(
                        "ID: " + idProducto
                );

            }

            // ==================================================
            // PRODUCTO EXISTENTE
            // ==================================================

            else if (tipoProducto == 2) {

                System.out.print(
                        "Buscar producto por ID o nombre: "
                );

                String busqueda =
                        scanner.nextLine()
                               .trim();

                Producto producto =
                        servicioEntrada.buscarProducto(
                                busqueda
                        );

                if (producto == null) {

                    producto =
                            servicioEntrada
                                    .buscarProductoPorNombre(
                                            busqueda
                                    );
                }

                if (producto == null) {

                    System.out.println(
                            "❌ No se encontró el producto."
                    );

                    return;
                }

                System.out.println();
                System.out.println(
                        "Producto encontrado:"
                );

                System.out.println(
                        "ID: "
                                + producto.getIdProducto()
                );

                System.out.println(
                        "Nombre: "
                                + producto.getNombre()
                );

                System.out.println(
                        "Precio actual: "
                                + producto.getPrecio()
                );

                System.out.println();

                System.out.print(
                        "Cantidad a ingresar: "
                );

                int cantidad =
                        Integer.parseInt(
                                scanner.nextLine()
                                       .trim()
                        );

                System.out.print(
                        "¿Desea cambiar el precio? (s/n): "
                );

                String respuesta =
                        scanner.nextLine()
                               .trim();

                double precio =
                        producto.getPrecio();

                if (respuesta.equalsIgnoreCase("s")) {

                    System.out.print(
                            "Nuevo precio de entrada: "
                    );

                    precio =
                            Double.parseDouble(
                                    scanner.nextLine()
                                           .trim()
                            );
                }

                servicioEntrada.registrarPedido(
                        proveedor,
                        producto,
                        cantidad,
                        precio
                );

                System.out.println(
                        "✔ Entrada registrada correctamente."
                );

            } else {

                System.out.println(
                        "❌ Opción de producto no válida."
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "❌ Error al registrar ingreso: "
                            + e.getMessage()
            );
        }
    }

    // ==========================================================
    // REGISTRAR VENTA
    // ==========================================================

    private static void registrarVenta(
            GestionProducto gestionProducto,
            Scanner scanner) {

        try {

            System.out.println(
                    "\n--- REGISTRAR VENTA ---"
            );

            System.out.print(
                    "CI del Cliente: "
            );

            String ci =
                    scanner.nextLine().trim();

            System.out.print(
                    "Nombre del Cliente: "
            );

            String nombreCliente =
                    scanner.nextLine()
                           .trim()
                           .toUpperCase();

            List<Producto> productosVenta =
                    new ArrayList<>();

            Map<String, Producto> inventario =
                    gestionProducto
                            .obtenerInventarioCompleto();

            boolean agregarMas = true;

            while (agregarMas) {

                System.out.print(
                        "ID del producto a vender: "
                );

                String idProd =
                        scanner.nextLine()
                               .trim()
                               .toUpperCase();

                                Producto pBase = null;

                                for (Producto producto : inventario.values()) {
                                        if (producto.getIdProducto()
                                                        .equalsIgnoreCase(idProd)) {
                                                pBase = producto;
                                                break;
                                        }
                                }

                if (pBase == null) {

                    System.out.println(
                            "❌ El producto no existe."
                    );

                } else {

                    System.out.println(
                            "Producto: "
                                    + pBase.getNombre()
                    );

                    System.out.println(
                            "Precio: "
                                    + pBase.getPrecio()
                    );

                    System.out.print(
                            "Cantidad a vender (Stock actual: "
                                    + pBase.getStock()
                                    + "): "
                    );

                    int cantidad =
                            Integer.parseInt(
                                    scanner.nextLine()
                                           .trim()
                            );

                    if (cantidad <= 0) {

                        System.out.println(
                                "La cantidad debe ser mayor a 0."
                        );

                    } else if (
                            cantidad > pBase.getStock()) {

                        System.out.println(
                                "❌ Stock insuficiente."
                        );

                    } else {

                        Producto pVenta =
                                new Producto(
                                        pBase.getIdProducto(),
                                        pBase.getMarca(),
                                        pBase.getPrecio(),
                                        pBase.getDescripcion(),
                                        cantidad,
                                        pBase.getNombre(),
                                        pBase.getCategoria()
                                );

                        productosVenta.add(
                                pVenta
                        );

                        System.out.println(
                                "✔ Producto agregado a la venta."
                        );
                    }
                }

                System.out.print(
                        "¿Desea agregar otro producto? (s/n): "
                );

                String respuesta =
                        scanner.nextLine()
                               .trim();

                agregarMas =
                        respuesta.equalsIgnoreCase("s");
            }

            if (!productosVenta.isEmpty()) {

                Venta venta =
                        new Venta(
                                ci,
                                nombreCliente,
                                productosVenta
                        );

                venta.registarVenta();

            } else {

                System.out.println(
                        "Venta cancelada."
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "❌ Error al registrar la venta: "
                            + e.getMessage()
            );
        }
    }

    // ==========================================================
    // VER STOCK
    // ==========================================================

    private static void verStockActual(
            GestionProducto gestionProducto) {

        System.out.println(
                "\n--- STOCK ACTUAL ---"
        );

        try {

            Map<String, Integer> stock =
                    gestionProducto.stockActual();

            if (stock.isEmpty()) {

                System.out.println(
                        "El inventario está vacío."
                );

            } else {

                stock.forEach(
                        (id, cantidad) ->
                                System.out.println(
                                        "ID: " + id
                                                + " | Stock: "
                                                + cantidad
                                )
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al leer el stock: "
                            + e.getMessage()
            );
        }
    }

    // ==========================================================
    // VER PROVEEDORES
    // ==========================================================

    private static void verProveedores(
            GestionProducto gestionProducto) {

        System.out.println(
                "\n--- LISTA DE PROVEEDORES ---"
        );

        try {

            List<Proveedor> lista =
                    gestionProducto.proveedores();

            if (lista.isEmpty()) {

                System.out.println(
                        "No hay proveedores registrados."
                );

            } else {

                for (Proveedor p : lista) {

                    System.out.println(
                            "Código: "
                                    + p.getCodigoProveedor()
                                    + " | Nombre: "
                                    + p.getNombreProveedor()
                                    + " | Contacto: "
                                    + p.getContactoProveedor()
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al obtener proveedores: "
                            + e.getMessage()
            );
        }
    }

    // ==========================================================
    // VER VENTAS DEL DÍA
    // ==========================================================

    private static void verVentasDia(
            GestionProducto gestionProducto) {

        System.out.println(
                "\n--- VENTAS DEL DÍA ---"
        );

        try {

            List<Venta> ventas =
                    gestionProducto.ventasDia(
                            java.time.LocalDate.now()
                    );

            if (ventas.isEmpty()) {

                System.out.println(
                        "No hay ventas registradas hoy."
                );

            } else {

                for (Venta v : ventas) {

                    System.out.println(
                            "Cliente CI: "
                                    + v.getCliente().getCi()
                    );

                    System.out.printf(
                            "Total: %.2f%n",
                            v.calcularTotal()
                    );

                    System.out.println(
                            "-------------------------"
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al consultar ventas: "
                            + e.getMessage()
            );
        }
    }

    // ==========================================================
    // CALCULAR TOTAL DE VENTAS DEL DÍA
    // ==========================================================

    private static void calcularVentasDia(
            GestionProducto gestionProducto) {

        System.out.println(
                "\n--- TOTAL DE VENTAS DEL DÍA ---"
        );

        try {

            double total =
                    gestionProducto
                            .calcularVentasDelDia();

            System.out.printf(
                    "TOTAL VENDIDO HOY: %.2f%n",
                    total
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al calcular las ventas: "
                            + e.getMessage()
            );
        }
    }
}