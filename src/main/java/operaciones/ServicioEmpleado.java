package operaciones;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServicioEmpleado {

    private final MotorCSV motorCSV;

    public ServicioEmpleado(MotorCSV motorCSV) {
        this.motorCSV = motorCSV;
    }

    public List<Empleado> obtenerEmpleados() throws IOException {
        List<Empleado> lista = new ArrayList<>();
        List<Map<String, String>> filas = motorCSV.leerFilas(ConfiguracionCSV.RUTA_EMPLEADOS, ConfiguracionCSV.EMPLEADOS_COLUMNAS);
        
        for (Map<String, String> fila : filas) {
            String id = fila.get("id");
            String nombre = fila.get("nombre");
            String password = fila.get("password");
            String rolStr = fila.get("rol");
            
            if (id != null && !id.isBlank()) {
                try {
                    Rol rol = Rol.valueOf(rolStr.trim().toUpperCase());
                    lista.add(new Empleado(id, nombre, password, rol));
                } catch (IllegalArgumentException e) {
                    System.err.println("Advertencia: Rol inválido ('" + rolStr + "') para el empleado ID: " + id);
                }
            }
        }
        return lista;
    }

    public void registrarEmpleado(Empleado nuevo, List<Empleado> empleadosActuales) throws IOException {
        boolean existe = empleadosActuales.stream()
                .anyMatch(e -> e.getId().equals(nuevo.getId()));
        
        if (existe) {
            throw new IllegalArgumentException("Ya existe un empleado registrado con el ID: " + nuevo.getId());
        }

        empleadosActuales.add(nuevo);
        guardarEmpleadosEnCSV(empleadosActuales);
    }

    private void guardarEmpleadosEnCSV(List<Empleado> empleados) throws IOException {
        List<Map<String, String>> filas = new ArrayList<>();
        
        for (Empleado e : empleados) {
            Map<String, String> fila = new LinkedHashMap<>();
            fila.put("id", e.getId());
            fila.put("nombre", e.getNombre());
            fila.put("password", e.getContrasena());
            fila.put("rol", e.getRol().name());
            
            filas.add(fila);
        }
        
        motorCSV.escribirFilas(ConfiguracionCSV.RUTA_EMPLEADOS, filas, ConfiguracionCSV.EMPLEADOS_COLUMNAS);
    }
}