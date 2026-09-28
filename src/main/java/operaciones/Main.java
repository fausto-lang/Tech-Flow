package operaciones;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        EntradaProducto servicioEntrada = new EntradaProducto();
        GestionProducto gestionProducto = new GestionProducto();

        int opcion = -1;
        while (opcion != 0) {
            mostrarMenu();
            try {
                System.out.print("Seleccione una opción: ");
                opcion = Integer.parseInt(scanner.nextLine().trim());

                switch (opcion) {
                    case 1:
                        registrarEntrada(servicioEntrada, scanner);
                        break;
                    case 2:
                        registrarVenta(gestionProducto, scanner);
                        break;
                    case 3:
                        verStockActual(gestionProducto);
                        break;
                    case 4:
                        verProveedores(gestionProducto);
                        break;
                    case 5:
                        verVentasDia(gestionProducto, scanner);
                        break;
                    case 6:
                        verComprasCliente(gestionProducto, scanner);
                        break;
                    case 0:
                        System.out.println("Saliendo del sistema...");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Ingrese un número válido.");
            } catch (Exception e) {
                System.out.println("Error inesperado: " + e.getMessage());
            }
            System.out.println();
        }
        scanner.close();
    }

    private static void mostrarMenu() {
        System.out.println("=================================");
        System.out.println("    SISTEMA DE GESTIÓN (TECH-FLOW)");
        System.out.println("=================================");
        System.out.println("1. Registrar Entrada de Producto");
        System.out.println("2. Registrar Venta");
        System.out.println("3. Ver Stock Actual");
        System.out.println("4. Ver Lista de Proveedores");
        System.out.println("5. Ver Ventas del Día");
        System.out.println("6. Ver Compras por Cliente");
        System.out.println("0. Salir");
        System.out.println("=================================");
    }

    private static void registrarEntrada(EntradaProducto servicioEntrada, Scanner scanner) {
        try {
            System.out.println("\n--- REGISTRAR ENTRADA DE PRODUCTO ---");

            // 1. PROVEEDOR
            System.out.print("Código del proveedor: ");
            String codigoProveedor = scanner.nextLine().trim();

            Proveedor proveedor = servicioEntrada.buscarProveedor(codigoProveedor);

            if (proveedor != null) {
                System.out.println("-> Proveedor encontrado: " + proveedor.getNombreProveedor());
            } else {
                System.out.println("-> Proveedor nuevo. Ingrese sus datos:");
                System.out.print("Nombre del proveedor: ");
                String nombreProveedor = scanner.nextLine().trim();
                System.out.print("Contacto (Teléfono): ");
                int contacto = Integer.parseInt(scanner.nextLine().trim());

                proveedor = new Proveedor(nombreProveedor, codigoProveedor, contacto);
            }

            // 2. PRODUCTO
            System.out.print("ID del producto: ");
            String idProducto = scanner.nextLine().trim();

            Producto productoExistente = servicioEntrada.buscarProducto(idProducto);
            Producto producto;

            if (productoExistente != null) {
                System.out.println("-> Producto encontrado: " + productoExistente.getNombre());
                producto = productoExistente;
            } else {
                System.out.println("-> Producto nuevo. Ingrese sus datos:");
                System.out.print("Nombre del producto: ");
                String nombreProducto = scanner.nextLine().trim();
                System.out.print("Marca: ");
                String marca = scanner.nextLine().trim();
                System.out.print("Categoría: ");
                String categoria = scanner.nextLine().trim();
                System.out.print("Descripción: ");
                String descripcion = scanner.nextLine().trim();
                System.out.print("Precio unitario: ");
                double precio = Double.parseDouble(scanner.nextLine().trim());

                producto = new Producto(idProducto, marca, precio, descripcion, 0, nombreProducto, categoria);
            }

            // 3. CANTIDAD Y FECHA
            System.out.print("Cantidad a ingresar: ");
            int cantidad = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Fecha de entrega (AAAA-MM-DD) [Enter para fecha actual]: ");
            String fechaInput = scanner.nextLine().trim();
            LocalDate fechaEntrega = fechaInput.isEmpty() ? LocalDate.now() : LocalDate.parse(fechaInput);

            servicioEntrada.registrarPedido(proveedor, producto, cantidad, fechaEntrega);
            System.out.println("✔ Entrada registrada con éxito.");

        } catch (Exception e) {
            System.out.println("❌ Error al registrar entrada: " + e.getMessage());
        }
    }

    private static void registrarVenta(GestionProducto gestionProducto, Scanner scanner) {
        try {
            System.out.println("\n--- REGISTRAR VENTA ---");
            
            // 1. VERIFICACIÓN Y BÚSQUEDA DE CLIENTE
            System.out.print("CI del Cliente: ");
            String ci = scanner.nextLine().trim();

            Cliente clienteExistente = gestionProducto.historialCliente(ci);
            String nombreCliente;

            if (clienteExistente != null && clienteExistente.getNombre() != null && !clienteExistente.getNombre().isBlank()) {
                nombreCliente = clienteExistente.getNombre();
                System.out.println("-> Cliente registrado encontrado: " + nombreCliente);
            } else {
                System.out.println("-> Cliente no encontrado. Ingrese datos del nuevo cliente:");
                System.out.print("Nombre del Cliente: ");
                nombreCliente = scanner.nextLine().trim();
            }

            // 2. SELECCIÓN DE PRODUCTOS
            List<Producto> productosVenta = new ArrayList<>();
            Map<String, Producto> inventario = gestionProducto.obtenerInventarioCompleto();

            boolean agregarMas = true;
            while (agregarMas) {
                System.out.print("ID del producto a vender: ");
                String idProd = scanner.nextLine().trim();

                if (!inventario.containsKey(idProd)) {
                    System.out.println("El producto con ID " + idProd + " no existe en el inventario.");
                } else {
                    Producto pBase = inventario.get(idProd);
                    System.out.print("Cantidad a vender (Stock actual: " + pBase.getStock() + "): ");
                    int cant = Integer.parseInt(scanner.nextLine().trim());

                    if (cant > pBase.getStock()) {
                        System.out.println("Stock insuficiente.");
                    } else {
                        Producto pVenta = new Producto(pBase.getIdProducto(), pBase.getMarca(),
                                pBase.getPrecio(), pBase.getDescripcion(), cant, pBase.getNombre(), pBase.getCategoria());
                        productosVenta.add(pVenta);
                    }
                }

                System.out.print("¿Desea agregar otro producto? (s/n): ");
                String resp = scanner.nextLine().trim();
                agregarMas = resp.equalsIgnoreCase("s");
            }

            // 3. PROCESAR Y REGISTRAR VENTA
            if (!productosVenta.isEmpty()) {
                Venta venta = new Venta(ci, nombreCliente, productosVenta);
                venta.registarVenta();
            } else {
                System.out.println("Venta cancelada (sin productos).");
            }
        } catch (Exception e) {
            System.out.println("❌ Error al registrar la venta: " + e.getMessage());
        }
    }

    private static void verStockActual(GestionProducto gestionProducto) {
        System.out.println("\n--- STOCK ACTUAL DE INVENTARIO ---");
        try {
            List<Producto> productos = gestionProducto.stockActual();
            if (productos.isEmpty()) {
                System.out.println("El inventario está vacío.");
            } else {
                for (Producto p : productos) {
                    System.out.println("ID: " + p.getIdProducto() + " | Stock: " + p.getStock() + " | Nombre: " + p.getNombre());
                }
            }
        } catch (Exception e) {
            System.out.println("Error al leer el stock: " + e.getMessage());
        }
    }
    private static void verProveedores(GestionProducto gestionProducto) {
        System.out.println("\n--- LISTA DE PROVEEDORES ---");
        try {
            List<Proveedor> lista = gestionProducto.proveedores();
            if (lista.isEmpty()) {
                System.out.println("No hay proveedores registrados.");
            } else {
                for (Proveedor p : lista) {
                    System.out.println("Código: " + p.getCodigoProveedor() +
                            " | Nombre: " + p.getNombreProveedor() +
                            " | Contacto: " + p.getContactoProveedor());
                }
            }
        } catch (Exception e) {
            System.out.println("Error al obtener proveedores: " + e.getMessage());
        }
    }

    private static void verVentasDia(GestionProducto gestionProducto, Scanner scanner) {
        System.out.println("\n--- VENTAS POR FECHA ---");
        System.out.print("Ingrese fecha (AAAA-MM-DD) [Enter para hoy]: ");
        String fechaInput = scanner.nextLine().trim();
        LocalDate fecha = fechaInput.isEmpty() ? LocalDate.now() : LocalDate.parse(fechaInput);

        try {
            List<Venta> ventas = gestionProducto.ventasDia(fecha);
            if (ventas.isEmpty()) {
                System.out.println("No hay ventas registradas para la fecha " + fecha);
            } else {
                for (Venta v : ventas) {
                    System.out.println("CI CLIENTE: " + v.getCliente().getCi() + " | Fecha: " + fecha);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al consultar ventas: " + e.getMessage());
        }
    }

    private static void verComprasCliente(GestionProducto gestionProducto, Scanner scanner) {
        System.out.println("\n--- COMPRAS POR CLIENTE ---");
        System.out.print("Ingrese CI del Cliente: ");
        String ci = scanner.nextLine().trim();

        try {
            Cliente cliente = gestionProducto.historialCliente(ci);
            if (cliente == null || cliente.getCompras().isEmpty()) {
                System.out.println("No se encontraron compras para el CI: " + ci);
            } else {
                System.out.println("Cliente: " + cliente.getNombre() + " (CI: " + cliente.getCi() + ")");
                for (Producto p : cliente.getCompras()) {
                    System.out.println(" - " + p.getNombre() + " | Cantidad: " + p.getStock());
                }
            }
        } catch (Exception e) {
            System.out.println("Error al consultar historial: " + e.getMessage());
        }
    }
}