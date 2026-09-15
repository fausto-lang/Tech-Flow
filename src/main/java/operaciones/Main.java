package operaciones;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String RUTA_INVENTARIO = "data/inventario.csv";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        EntradaProducto entrada = new EntradaProducto();
        GestionProducto gestion = new GestionProducto(RUTA_INVENTARIO);

        while (true) {
            System.out.println("========== TECH-FLOW ==========");
            System.out.println("1. Entrada ");
            System.out.println("2. Venta ");
            System.out.println("3. Gestion (Ver )");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            String opcion = scanner.nextLine().trim();

            switch (opcion) {
                case "1":
                    registrarEntrada(scanner, entrada);
                    break;
                case "2":
                    registrarVenta(scanner);
                    break;
                case "3":
                    listarProductos(scanner, gestion);
                    break;
                case "0":
                    scanner.close();
                    return;
                default:
                    System.out.println("Opcion no valida");
                    break;
            }
        }
    }

    private static void registrarEntrada(Scanner scanner, EntradaProducto entrada) {
        try {
            System.out.print("Codigo proveedor: ");
            String codigoProv = scanner.nextLine().trim();
            System.out.print("Nombre proveedor: ");
            String nombreProv = scanner.nextLine().trim();
            System.out.print("Contacto proveedor (numero): ");
            int contacto = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("ID producto: ");
            String idProd = scanner.nextLine().trim();
            System.out.print("Nombre producto: ");
            String nombreProd = scanner.nextLine().trim();
            System.out.print("Marca: ");
            String marca = scanner.nextLine().trim();
            System.out.print("Categoria: ");
            String categoria = scanner.nextLine().trim();
            System.out.print("Descripcion: ");
            String descripcion = scanner.nextLine().trim();
            System.out.print("Precio: ");
            double precio = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Cantidad: ");
            int cantidad = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Fecha (yyyy-MM-dd, vacio=hoy): ");
            String fechaTxt = scanner.nextLine().trim();
            LocalDate fecha = fechaTxt.isEmpty() ? LocalDate.now() : LocalDate.parse(fechaTxt);

            Proveedor proveedor = new Proveedor(nombreProv, codigoProv, contacto);
            Producto producto = new Producto(idProd, marca, precio, descripcion, 0, nombreProd, categoria);
            entrada.registrarPedido(proveedor, producto, cantidad, fecha);
            System.out.println(" stock actualizado");
        } catch (NumberFormatException e) {
            System.out.println("fecha invalida: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error registar entrada: " + e.getMessage());
        }
    }

    private static void registrarVenta(Scanner scanner) {
        try {
            System.out.print("CI cliente: ");
            String ci = scanner.nextLine().trim();
            System.out.print("ID producto: ");
            String idProd = scanner.nextLine().trim();
            System.out.print("Cantidad: ");
            int cantidad = Integer.parseInt(scanner.nextLine().trim());

            if (cantidad <= 0) {
                System.out.println("Cantidad debe ser mayor que cero");
                return;
            }

            GestionProducto gestion = new GestionProducto(RUTA_INVENTARIO);
            gestion.cargarProductosCSV();
            Producto base = gestion.buscarPorId(idProd);

            if (base == null) {
                System.out.println("Producto no existe en inventario: " + idProd);
                return;
            }
            if (base.getStock() < cantidad) {
                System.out.println("Sin stock suficiente, disponible: " + base.getStock());
                return;
            }

            Producto vendido = new Producto(
                    base.getIdProducto(),
                    base.getMarca(),
                    base.getPrecio(),
                    base.getDescripcion(),
                    cantidad,
                    base.getNombre(),
                    base.getCategoria());
            Venta venta = new Venta(ci, List.of(vendido));
            venta.registarVenta();
            System.out.println("Venta registrada, stock actualizado");
        } catch (NumberFormatException e) {
            System.out.println("Cantidad invalida: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error al registrar venta: " + e.getMessage());
        }
    }

    private static void listarProductos(Scanner scanner, GestionProducto gestion) {
        gestion.getProductos().clear();
        gestion.cargarProductosCSV();
        List<Producto> productos = gestion.getProductos();

        if (productos.isEmpty()) {
            System.out.println("Sin productos en inventario");
        } else {
            System.out.println("========== INVENTARIO ==========");
            System.out.printf("%-10s %-30s %-12s %-12s %8s %6s%n", "ID", "NOMBRE", "MARCA", "CATEGORIA", "PRECIO", "STOCK");
            for (Producto p : productos) {
                System.out.printf("%-10s %-30s %-12s %-12s %8.2f %6d%n",
                        p.getIdProducto(),
                        recortar(p.getNombre(), 30),
                        recortar(p.getMarca(), 12),
                        recortar(p.getCategoria(), 12),
                        p.getPrecio(),
                        p.getStock());
            }
        }

        System.out.println(" q volver al menu");
        while (true) {
            String tecla = scanner.nextLine().trim();
            if (tecla.equalsIgnoreCase("q")) {
                break;
            }
            System.out.println(" q volver al menu");
        }
    }

    private static String recortar(String texto, int max) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= max ? texto : texto.substring(0, max);
    }
}
