package operaciones;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // 1. Inicialización de Motor y Caja
        MotorCSV motorCSV = new MotorCSV();
        GestorCajaDiaria caja = new GestorCajaDiaria();

        // 2. Inicialización de Servicios
        ServicioInventario servicioInventario = new ServicioInventario(motorCSV);
        ServicioProveedor servicioProveedor = new ServicioProveedor(motorCSV);
        ServicioCliente servicioCliente = new ServicioCliente(motorCSV);
        ServicioEmpleado servicioEmpleado = new ServicioEmpleado(motorCSV);
        ServicioEntrada servicioEntrada = new ServicioEntrada(motorCSV, caja, servicioInventario, servicioProveedor);
        ServicioSalida servicioSalida = new ServicioSalida(motorCSV, caja, servicioInventario);
        ServicioVenta servicioVenta = new ServicioVenta(motorCSV, caja);
        ServicioSeguridad servicioSeguridad = new ServicioSeguridad();

        Scanner scanner = new Scanner(System.in);

        try {
            // 3. Carga de datos iniciales
            List<Empleado> empleadosBD = servicioEmpleado.obtenerEmpleados();
            
            // Si no hay empleados, creamos el Administrador por defecto
            if (empleadosBD.isEmpty()) {
                System.out.println("[INFO] Base de datos de empleados vacía. Creando administrador por defecto...");
                Empleado admin = new Empleado("admin", "Administrador", "admin123", Rol.ADMINISTRADOR);
                servicioEmpleado.registrarEmpleado(admin, empleadosBD);
            }

            // 4. Pantalla de Login
            System.out.println("=========================================");
            System.out.println("      SISTEMA POS & INVENTARIO v1.0      ");
            System.out.println("=========================================");
            System.out.print("ID Usuario: ");
            String idUsuario = scanner.nextLine();
            System.out.print("Contraseña: ");
            String password = scanner.nextLine();

            if (!servicioSeguridad.login(empleadosBD, idUsuario, password)) {
                System.out.println("[ERROR] Credenciales incorrectas. El programa se cerrará.");
                return;
            }

            Empleado sesion = servicioSeguridad.getSesionActual();
            System.out.println("\n¡Bienvenido, " + sesion.getNombre() + "! (Rol: " + sesion.getRol() + ")");

            // 5. Bucle del Menú Principal
            boolean salir = false;
            while (!salir) {
                System.out.println("\n-----------------------------------------");
                System.out.println(" ESTADO CAJA: " + (caja.isCajaAbierta() ? "[ABIERTA]" : "[CERRADA]"));
                System.out.println("-----------------------------------------");
                System.out.println("1. Abrir Caja Diaria");
                System.out.println("2. Cerrar Caja Diaria");
                System.out.println("3. Módulo de Inventario (Ver / Añadir)");
                System.out.println("4. Registrar Entrada (Ingreso de Stock)");
                System.out.println("5. Registrar Salida (Merma / Pérdida)");
                System.out.println("6. Módulo de Ventas");
                System.out.println("7. Salir del Sistema");
                System.out.print("Seleccione una opción: ");

                String opcion = scanner.nextLine();

                switch (opcion) {
                    case "1":
                        if (!caja.isCajaAbierta()) {
                            caja.abrirCaja();
                            System.out.println("[ÉXITO] Caja abierta. Archivos del día inicializados.");
                        } else {
                            System.out.println("[AVISO] La caja ya está abierta.");
                        }
                        break;

                    case "2":
                        if (caja.isCajaAbierta()) {
                            caja.cerrarCaja();
                            System.out.println("[ÉXITO] Caja cerrada.");
                        } else {
                            System.out.println("[AVISO] La caja ya está cerrada.");
                        }
                        break;

                    case "3":
                        moduloInventario(scanner, servicioInventario);
                        break;

                    case "4":
                        moduloEntradas(scanner, servicioEntrada, servicioInventario);
                        break;

                    case "5":
                        moduloSalidas(scanner, servicioSalida, servicioInventario);
                        break;

                    case "6":
                        moduloVentas(scanner, servicioVenta, servicioInventario, servicioCliente, caja);
                        break;

                    case "7":
                        salir = true;
                        System.out.println("Cerrando sesión... ¡Hasta pronto!");
                        break;

                    default:
                        System.out.println("[ERROR] Opción no válida.");
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR CRÍTICO] Ocurrió un problema: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    // --- MÉTODOS DE LOS MÓDULOS ---

    private static void moduloInventario(Scanner scanner, ServicioInventario servicioInventario) throws IOException {
        List<Producto> inventario = servicioInventario.obtenerInventario();
        System.out.println("\n--- MÓDULO DE INVENTARIO ---");
        System.out.println("1. Ver catálogo completo");
        System.out.println("2. Registrar nuevo producto (0 stock)");
        System.out.print("Opción: ");
        String op = scanner.nextLine();

        if (op.equals("1")) {
            System.out.println("\n--- CATÁLOGO ---");
            if (inventario.isEmpty()) System.out.println("El inventario está vacío.");
            for (Producto p : inventario) {
                System.out.printf("ID: %s | %s %s | Venta: Bs.%.2f | Stock: %d\n", 
                        p.getIdProducto(), p.getMarca(), p.getNombre(), p.getPrecioVenta(), p.getStock());
            }
        } else if (op.equals("2")) {
            System.out.print("ID Producto (Ej. P001): "); String id = scanner.nextLine();
            System.out.print("Marca: "); String marca = scanner.nextLine();
            System.out.print("Nombre: "); String nombre = scanner.nextLine();
            System.out.print("Categoría: "); String categoria = scanner.nextLine();
            System.out.print("Descripción: "); String descripcion = scanner.nextLine();
            System.out.print("Precio Base de Entrada: "); double precioEnt = Double.parseDouble(scanner.nextLine());
            System.out.print("Precio de Venta al Público: "); double precioVent = Double.parseDouble(scanner.nextLine());

            Producto nuevo = new Producto(id, marca, precioEnt, precioVent, descripcion, 0, nombre, categoria);
            try {
                servicioInventario.registrarNuevoProducto(nuevo, inventario);
                System.out.println("[ÉXITO] Producto registrado correctamente.");
            } catch (IllegalArgumentException e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    private static void moduloEntradas(Scanner scanner, ServicioEntrada servicioEntrada, ServicioInventario servicioInventario) throws IOException {
        System.out.println("\n--- REGISTRO DE ENTRADAS (COMPRA A PROVEEDOR) ---");
        List<Producto> inventario = servicioInventario.obtenerInventario();
        
        System.out.print("Ingrese ID del producto a abastecer: ");
        String idProd = scanner.nextLine();
        
        Producto productoEncontrado = inventario.stream().filter(p -> p.getIdProducto().equals(idProd)).findFirst().orElse(null);
        
        if (productoEncontrado == null) {
            System.out.println("[ERROR] Producto no encontrado. Regístrelo primero en el módulo de Inventario.");
            return;
        }

        System.out.print("Cantidad a ingresar: "); int cantidad = Integer.parseInt(scanner.nextLine());
        System.out.print("Costo Total de esta compra (Bs): "); double costoTotal = Double.parseDouble(scanner.nextLine());
        
        System.out.println("- Datos del Proveedor -");
        System.out.print("Código Proveedor: "); String codProv = scanner.nextLine();
        System.out.print("Nombre Proveedor: "); String nomProv = scanner.nextLine();
        System.out.print("Teléfono Proveedor: "); int telProv = Integer.parseInt(scanner.nextLine());

        Proveedor prov = new Proveedor(nomProv, codProv, telProv);

        try {
            servicioEntrada.registrarEntrada(prov, productoEncontrado, cantidad, costoTotal);
            System.out.println("[ÉXITO] Entrada registrada y stock actualizado.");
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void moduloSalidas(Scanner scanner, ServicioSalida servicioSalida, ServicioInventario servicioInventario) throws IOException {
        System.out.println("\n--- REGISTRO DE SALIDAS (MERMAS) ---");
        List<Producto> inventario = servicioInventario.obtenerInventario();
        
        System.out.print("Ingrese ID del producto: ");
        String idProd = scanner.nextLine();
        Producto productoEncontrado = inventario.stream().filter(p -> p.getIdProducto().equals(idProd)).findFirst().orElse(null);
        
        if (productoEncontrado == null) {
            System.out.println("[ERROR] Producto no encontrado.");
            return;
        }

        System.out.print("Cantidad a retirar: "); int cantidad = Integer.parseInt(scanner.nextLine());
        System.out.print("Motivo (Ej. Dañado, Vencido): "); String motivo = scanner.nextLine();

        try {
            servicioSalida.registrarSalida(productoEncontrado, cantidad, motivo, inventario);
            System.out.println("[ÉXITO] Salida registrada y stock reducido.");
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void moduloVentas(Scanner scanner, ServicioVenta servicioVenta, ServicioInventario servicioInventario, ServicioCliente servicioCliente, GestorCajaDiaria caja) throws IOException {
        if (!caja.isCajaAbierta()) {
            System.out.println("[ERROR] Debe abrir la caja primero para vender.");
            return;
        }

        System.out.println("\n--- MÓDULO DE VENTAS ---");
        OrdenVenta orden = new OrdenVenta();
        List<Producto> inventario = servicioInventario.obtenerInventario();
        List<Cliente> clientes = servicioCliente.clientes();

        boolean agregando = true;
        while (agregando) {
            System.out.print("ID del Producto a vender (o 'fin' para cobrar): ");
            String input = scanner.nextLine();
            
            if (input.equalsIgnoreCase("fin")) {
                agregando = false;
                continue;
            }

            Producto productoEncontrado = inventario.stream().filter(p -> p.getIdProducto().equals(input)).findFirst().orElse(null);
            if (productoEncontrado == null) {
                System.out.println("[ERROR] Producto no existe.");
            } else if (productoEncontrado.getStock() <= 0) {
                System.out.println("[ERROR] Sin stock disponible.");
            } else {
                System.out.print("Cantidad: ");
                int cant = Integer.parseInt(scanner.nextLine());
                if (cant > productoEncontrado.getStock()) {
                    System.out.println("[ERROR] Solo hay " + productoEncontrado.getStock() + " en stock.");
                } else {
                    orden.agregarProducto(productoEncontrado, cant);
                    System.out.println("Añadido al carrito: " + productoEncontrado.getNombre() + " x" + cant);
                }
            }
        }

        if (orden.getItems().isEmpty()) {
            System.out.println("Venta cancelada. Carrito vacío.");
            return;
        }

        System.out.println("\n" + orden.generarProforma());

        System.out.print("CI del Cliente (Deje en blanco si es Consumidor Final): ");
        String ciCliente = scanner.nextLine();
        Cliente clienteFinal = null;

        if (!ciCliente.isBlank()) {
            clienteFinal = servicioCliente.buscarPorCi(clientes, ciCliente);
            if (clienteFinal == null) {
                System.out.print("Cliente nuevo. Ingrese el nombre: ");
                String nombreC = scanner.nextLine();
                clienteFinal = new Cliente(nombreC, ciCliente);
                servicioCliente.registrarClienteSiNoExiste(clientes, clienteFinal);
            } else {
                System.out.println("Cliente encontrado: " + clienteFinal.getNombre());
            }
        }

        System.out.print("¿Emitir con factura? (S/N): ");
        boolean conFactura = scanner.nextLine().equalsIgnoreCase("S");

        try {
            String mensaje = servicioVenta.procesarVenta(orden, clienteFinal, conFactura);
            System.out.println("\n" + mensaje);
        } catch (Exception e) {
            System.out.println("[ERROR AL PROCESAR VENTA] " + e.getMessage());
        }
    }
}