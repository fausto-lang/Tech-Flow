package operaciones;

import java.util.ArrayList;
import java.util.List;

public class TestHappyPath {

    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBAS AUTOMATIZADAS (HAPPY PATH) ===");
        
        try {
            // 1. Inicialización de la infraestructura
            MotorCSV motorCSV = new MotorCSV();
            GestorCajaDiaria caja = new GestorCajaDiaria();
            ServicioInventario servicioInventario = new ServicioInventario(motorCSV);
            ServicioProveedor servicioProveedor = new ServicioProveedor(motorCSV);
            ServicioEntrada servicioEntrada = new ServicioEntrada(motorCSV, caja, servicioInventario, servicioProveedor);
            ServicioSalida servicioSalida = new ServicioSalida(motorCSV, caja, servicioInventario);
            ServicioVenta servicioVenta = new ServicioVenta(motorCSV, caja);
            ServicioSeguridad servicioSeguridad = new ServicioSeguridad();

            // 2. TEST: Inicio de Sesión
            System.out.println("\n[TEST 1] Verificando Inicio de Sesión...");
            List<Empleado> empleadosBD = new ArrayList<>();
            empleadosBD.add(new Empleado("adminTest", "Administrador de Prueba", "123", Rol.ADMINISTRADOR));
            
            boolean loginOk = servicioSeguridad.login(empleadosBD, "adminTest", "123");
            if (loginOk) System.out.println("✓ Login exitoso.");
            else throw new Exception("Fallo el sistema de login.");

            // 3. TEST: Abrir Caja
            System.out.println("\n[TEST 2] Abriendo Caja Diaria...");
            caja.abrirCaja();
            if (caja.isCajaAbierta()) System.out.println("✓ Caja abierta correctamente. Rutas de archivos preparadas.");
            else throw new Exception("La caja no cambió su estado a 'abierta'.");

            // 4. TEST: Crear Nuevo Producto
            System.out.println("\n[TEST 3] Registrando Nuevo Producto...");
            List<Producto> inventarioTest = new ArrayList<>();
            Producto nuevoProd = new Producto("TEST-01", "MarcaX", 100.0, 150.0, "Producto de prueba", 0, "Laptop Test", "Computación");
            servicioInventario.registrarNuevoProducto(nuevoProd, inventarioTest);
            System.out.println("✓ Producto creado en el inventario. Stock inicial: " + nuevoProd.getStock());

            // 5. TEST: Registrar Entrada (Compra)
            System.out.println("\n[TEST 4] Ingresando Stock al Almacén...");
            Proveedor prov = new Proveedor("Proveedor Test SA", "PRV-001", 12345678);
            // Compramos 50 unidades a un costo total de 5000 Bs
            servicioEntrada.registrarEntrada(prov, nuevoProd, 50, 5000.0);
            System.out.println("✓ Entrada registrada exitosamente. Nuevo stock: " + nuevoProd.getStock());
            if (nuevoProd.getStock() != 50) throw new Exception("El stock no se sumó correctamente.");

            // 6. TEST: Procesar Venta
            System.out.println("\n[TEST 5] Realizando una Venta...");
            OrdenVenta orden = new OrdenVenta();
            // Vendemos 5 unidades
            orden.agregarProducto(nuevoProd, 5); 
            Cliente cliente = new Cliente("Juan Cliente", "9876543");
            
            String resultadoVenta = servicioVenta.procesarVenta(orden, cliente, true);
            System.out.println("✓ Venta completada:\n  --> " + resultadoVenta);
            System.out.println("✓ Stock restante tras la venta: " + nuevoProd.getStock());
            if (nuevoProd.getStock() != 45) throw new Exception("El stock no se descontó correctamente en la venta.");

            // 7. TEST: Registrar Salida (Merma)
            System.out.println("\n[TEST 6] Registrando Merma / Pérdida...");
            // Retiramos 2 unidades por daño
            servicioSalida.registrarSalida(nuevoProd, 2, "Dañado en almacén", inventarioTest);
            System.out.println("✓ Salida registrada exitosamente. Stock final: " + nuevoProd.getStock());
            if (nuevoProd.getStock() != 43) throw new Exception("El stock no se descontó correctamente en la salida.");

            // 8. TEST: Cerrar Caja
            System.out.println("\n[TEST 7] Cerrando Caja Diaria...");
            caja.cerrarCaja();
            if (!caja.isCajaAbierta()) System.out.println("✓ Caja cerrada correctamente.");
            else throw new Exception("La caja no cambió su estado a 'cerrada'.");

            System.out.println("\n=======================================================");
            System.out.println(" ✨ RESULTADO: TODAS LAS PRUEBAS PASARON CON ÉXITO ✨ ");
            System.out.println("=======================================================");

        } catch (Exception e) {
            System.err.println("\n❌ [FALLO CRÍTICO EN EL TEST] " + e.getMessage());
            e.printStackTrace();
        }
    }
}