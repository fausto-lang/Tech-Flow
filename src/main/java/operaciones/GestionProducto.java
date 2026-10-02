package operaciones;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GestionProducto {

    private static final String RUTA_INVENTARIO = "data/inventario.csv";
    private static final String RUTA_PROVEEDORES = "data/proveedores.csv";
    private static final String RUTA_SALIDAS = "data/salidas.csv";
    private static final String RUTA_CLIENTES = "data/clientes.csv";

    private static final String[] INVENTARIO_COLUMNAS = {
        "idProducto", "nombre", "marca", "categoria", "descripcion", "precio", "stock"
    };
    private static final String[] PROVEEDORES_COLUMNAS = {
        "codigoProveedor", "nombreProveedor", "contactoProveedor", "fechaEntrega"
    };
    private static final String[] CLIENTES_COLUMNAS = {
        "ci", "nombreCliente", "idProducto", "nombreProducto", "cantidad", "fechaCompra"
    };

    private final MotorCSV motorCSV;

    public GestionProducto() {
        this.motorCSV = new MotorCSV();
    }

    public List<Producto> stockActual() throws IOException {
        List<Producto> lista = new ArrayList<>();
        List<Map<String, String>> inventario = motorCSV.leerFilas(RUTA_INVENTARIO, INVENTARIO_COLUMNAS);
        for (Map<String, String> fila : inventario) {
            String id = fila.get("idProducto");
            String nombre = fila.get("nombre");
            int stock = motorCSV.parsearEntero(fila.get("stock"));
            
            if (id != null && !id.isBlank()) {
                //Producto p = new Producto(id, fila.get("marca"), parsearDouble(fila.get("precio")), 
                //                           fila.get("descripcion"), stock, nombre, fila.get("categoria"));
                //lista.add(p);
            }
        }
        return lista;
    }

    public Map<String, Producto> obtenerInventarioCompleto() throws IOException {
        Map<String, Producto> mapa = new HashMap<>();
        List<Map<String, String>> inventario = motorCSV.leerFilas(RUTA_INVENTARIO, INVENTARIO_COLUMNAS);
        for (Map<String, String> fila : inventario) {
            String id = fila.get("idProducto");
            if (id != null && !id.isBlank()) {
                //double precio = parsearDouble(fila.get("precio"));
                //int stock = parsearEntero(fila.get("stock"));
                //Producto p = new Producto(
                  //  id,
                //fila.get("marca"),
                //    precio,
            //    fila.get("descripcion"),
              //      stock,
                    //fila.get("nombre"),
                //    fila.get("categoria")
               // );
              //  mapa.put(id, p);
            }
        }
        return mapa;
    }

    public List<Proveedor> proveedores() throws IOException {
        List<Proveedor> lista = new ArrayList<>();
        List<Map<String, String>> filas = motorCSV.leerFilas(RUTA_PROVEEDORES, PROVEEDORES_COLUMNAS);
        for (Map<String, String> fila : filas) {
            String codigo = fila.get("codigoProveedor");
            String nombre = fila.get("nombreProveedor");
            int contacto = motorCSV.parsearEntero(fila.get("contactoProveedor"));
            if (codigo != null && !codigo.isBlank()) {
                lista.add(new Proveedor(nombre, codigo, contacto));
            }
        }
        return lista;
    }

    public List<Venta> ventasDia(LocalDate fecha) throws IOException {
        List<Venta> lista = new ArrayList<>();
        List<Map<String, String>> salidas = motorCSV.leerFilas(RUTA_SALIDAS, new String[]{
            "idVenta", "idProducto", "cliente(CI)", "nombreProducto", "cantidad", "precioUnitario", "fechaVenta"
        });

        Map<String, List<Producto>> productosPorVenta = new LinkedHashMap<>();
        Map<String, String> clientePorVenta = new LinkedHashMap<>();

        for (Map<String, String> fila : salidas) {
            String fechaFila = fila.get("fechaVenta");
            if (fechaFila != null && fechaFila.equalsIgnoreCase(fecha.toString())) {
                String idVenta = fila.get("idVenta");
                String ci = fila.get("cliente(CI)");
                double precio = motorCSV.parsearDouble(fila.get("precioUnitario"));
                int cantidad = motorCSV.parsearEntero(fila.get("cantidad"));

             //   Producto p = new Producto(fila.get("idProducto"), "", precio, "", cantidad, fila.get("nombreProducto"), "");
                
               // productosPorVenta.computeIfAbsent(idVenta, k -> new ArrayList<>()).add(p);
                clientePorVenta.putIfAbsent(idVenta, ci);
            }
        }

        for (String idVenta : productosPorVenta.keySet()) {
            String ci = clientePorVenta.get(idVenta);
            lista.add(new Venta(idVenta, fecha, ci, productosPorVenta.get(idVenta)));
        }

        return lista;
    }

    public Cliente historialCliente(String ci) throws IOException {
        List<Map<String, String>> filas = motorCSV.leerFilas(RUTA_CLIENTES, CLIENTES_COLUMNAS);
        Cliente cliente = null;

        for (Map<String, String> fila : filas) {
            if (ci.equalsIgnoreCase(fila.get("ci"))) {
                if (cliente == null) {
                    cliente = new Cliente(fila.get("nombreCliente"), ci);
                }
                //int cantidad = parsearEntero(fila.get("cantidad"));
                // Producto p = new Producto(fila.get("idProducto"), "", 0.0, "", cantidad, fila.get("nombreProducto"), "");
                // cliente.agregarCompra(p);
            }
        }
        return cliente;
    }
}