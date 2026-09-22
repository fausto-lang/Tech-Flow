package operaciones;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class Cliente {
    private String nombre;
    private String ci;
    private List<Producto> compras;

    public Cliente(String nombre, String ci) {
        this(nombre, ci, new ArrayList<>());
    }

    public Cliente(String nombre, String ci, List<Producto> compras) {
        this.nombre = nombre;
        this.ci = ci;
        this.compras = compras;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCi() {
        return ci;
    }

    public List<Producto> getCompras() {
        return compras;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    //esta es la logica para registrar un producto en las compras del cliente
    public void agregarCompra(Producto producto) {
        if (producto != null) {
            this.compras.add(producto);
        }
    }

    //metodo que carga los clientes de clientes.csv agrupando sus compras por CI
    public static List<Cliente> cargarClientesCSV(String ruta) {
        List<Cliente> clientes = new ArrayList<>();
        File archivo = new File(ruta);
        if (!archivo.exists() || archivo.length() == 0) {
            return clientes;
        }

        CsvSchema esquema = CsvSchema.emptySchema().withHeader();

        try (MappingIterator<Map<String, String>> registros = new CsvMapper()
                .readerFor(Map.class)
                .with(esquema)
                .readValues(archivo)) {
            while (registros.hasNext()) {
                Map<String, String> fila = normalizar(registros.next());
                String ci = fila.getOrDefault("ci", "");
                String nombre = fila.getOrDefault("nombreCliente", "");

                Cliente cliente = buscarPorCi(clientes, ci);
                if (cliente == null) {
                    cliente = new Cliente(nombre, ci);
                    clientes.add(cliente);
                } else if (cliente.getNombre().isBlank()) {
                    cliente.setNombre(nombre);
                }

                //las columnas de clientes.csv no guardan marca, precio ni categoria
                cliente.agregarCompra(new Producto(
                        fila.getOrDefault("idProducto", ""),
                        "",
                        0.0,
                        "",
                        parsearCantidad(fila.getOrDefault("cantidad", "")),
                        fila.getOrDefault("nombreProducto", ""),
                        ""));
            }
        } catch (IOException e) {
            System.out.println("Error al cargar clientes: " + e.getMessage());
        }

        return clientes;
    }

    private static Cliente buscarPorCi(List<Cliente> clientes, String ci) {
        for (Cliente cliente : clientes) {
            if (cliente.getCi().equals(ci)) {
                return cliente;
            }
        }
        return null;
    }

    //normaliza las claves y valores de la fila por si el csv trae espacios sobrantes
    private static Map<String, String> normalizar(Map<String, String> fila) {
        Map<String, String> valores = new LinkedHashMap<>();
        for (Map.Entry<String, String> entrada : fila.entrySet()) {
            String valor = entrada.getValue() == null ? "" : entrada.getValue().trim();
            valores.put(entrada.getKey().trim(), valor);
        }
        return valores;
    }

    private static int parsearCantidad(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
