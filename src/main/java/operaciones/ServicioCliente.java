package operaciones;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServicioCliente {

    private final MotorCSV motorCSV;

    public ServicioCliente(MotorCSV motorCSV) {
        this.motorCSV = motorCSV;
    }

    public List<Cliente> clientes() throws IOException {
        List<Cliente> lista = new ArrayList<>();
        List<Map<String, String>> filas = motorCSV.leerFilas(ConfiguracionCSV.RUTA_CLIENTES, ConfiguracionCSV.CLIENTES_COLUMNAS);
        
        for (Map<String, String> fila : filas) {
            String ci = fila.get("ci");
            String nombre = fila.get("nombreCliente");
            
            if (ci != null && !ci.isBlank()) {
                lista.add(new Cliente(nombre, ci)); 
            }
        }
        return lista;
    }

    public List<Cliente> buscarPorNombre(List<Cliente> clientes, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return new ArrayList<>(); 
        }
        
        return clientes.stream()
            .filter(c -> c.getNombre().toLowerCase().contains(nombre.toLowerCase().trim()))
            .collect(Collectors.toList());
    }

    public Cliente buscarPorCi(List<Cliente> clientes, String ci) {
        if (ci == null || ci.isBlank()) return null;

        return clientes.stream()
            .filter(c -> c.getCi().equals(ci.trim()))
            .findFirst()
            .orElse(null);
    }

    public void registrarClienteSiNoExiste(List<Cliente> clientes, Cliente nuevo) throws IOException {
        boolean existe = clientes.stream()
            .anyMatch(c -> c.getCi().equalsIgnoreCase(nuevo.getCi()));
        
        if (!existe) {
            clientes.add(nuevo);
            guardarClientesEnCSV(clientes);
        }
    }

    private void guardarClientesEnCSV(List<Cliente> clientes) throws IOException {
        List<Map<String, String>> filas = new ArrayList<>();
        
        for (Cliente c : clientes) {
            Map<String, String> fila = new LinkedHashMap<>();
            fila.put("ci", c.getCi());
            fila.put("nombreCliente", c.getNombre());            
            filas.add(fila);
        }
        
        motorCSV.escribirFilas(ConfiguracionCSV.RUTA_CLIENTES, filas, ConfiguracionCSV.CLIENTES_COLUMNAS);
    }
}