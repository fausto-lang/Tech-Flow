//Esta sección maneja la información de contacto de los distribuidores de la tienda.

package operaciones;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServicioProveedor {

    private final MotorCSV motorCSV;

    public ServicioProveedor(MotorCSV motorCSV) {
        this.motorCSV = motorCSV;
    }

    public List<Proveedor> proveedores() throws IOException {
        List<Proveedor> lista = new ArrayList<>();
        List<Map<String, String>> filas = motorCSV.leerFilas(ConfiguracionCSV.RUTA_PROVEEDORES, ConfiguracionCSV.PROVEEDORES_COLUMNAS);
        
        for (Map<String, String> fila : filas) {
            String codigo = fila.get("codigoProveedor");
            String nombre = fila.get("nombreProveedor");
            int contacto = Integer.parseInt(fila.getOrDefault("contactoProveedor", "0"));            
            if (codigo != null && !codigo.isBlank()) {
                lista.add(new Proveedor(nombre, codigo, contacto));
            }
        }
        return lista;
    }

    public List<Proveedor> buscarPorNombre(List<Proveedor> proveedores, String nombre) {
        return proveedores.stream()
            .filter(p -> p.getNombreProveedor().toLowerCase().contains(nombre.toLowerCase()))
            .collect(Collectors.toList());
    }

   public void registrarProveedorSiNoExiste(List<Proveedor> proveedores, Proveedor nuevo) throws IOException {
        boolean existe = proveedores.stream().anyMatch(p -> p.getCodigoProveedor().equals(nuevo.getCodigoProveedor()));
        
        if (!existe) {
            proveedores.add(nuevo);
            
            List<Map<String, String>> filasProveedores = new java.util.ArrayList<>();
            
            for (Proveedor p : proveedores) {
                Map<String, String> fila = new java.util.LinkedHashMap<>();
                fila.put("codigoProveedor", p.getCodigoProveedor());
                fila.put("nombreProveedor", p.getNombreProveedor());
                fila.put("contactoProveedor", String.valueOf(p.getContactoProveedor())); 
                fila.put("fechaEntrega", java.time.LocalDate.now().toString());                 
                filasProveedores.add(fila);
            }
            
            motorCSV.escribirFilas(ConfiguracionCSV.RUTA_PROVEEDORES, filasProveedores, ConfiguracionCSV.PROVEEDORES_COLUMNAS);
        }
    }
}