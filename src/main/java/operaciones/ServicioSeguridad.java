package operaciones;

import java.util.List;
import java.util.Optional;

public class ServicioSeguridad {
    
    private Empleado empleadoActualSesion = null;

    public boolean login(List<Empleado> empleadosBD, String id, String password) {
        Optional<Empleado> emp = empleadosBD.stream()
                .filter(e -> e.getId().equals(id) && e.verificarPassword(password))
                .findFirst();
        
        if (emp.isPresent()) {
            this.empleadoActualSesion = emp.get();
            return true;
        }
        return false;
    }

    public void cerrarSesion() { this.empleadoActualSesion = null; }

    public Empleado getSesionActual() { return empleadoActualSesion; }

    public void validarPermisoAdmin() {
        if (empleadoActualSesion == null || empleadoActualSesion.getRol() != Rol.ADMINISTRADOR) {
            throw new SecurityException("Acceso Denegado: Requiere permisos de Administrador.");
        }
    }

    public void validarPermisoVentas() {
        if (empleadoActualSesion == null || 
           (empleadoActualSesion.getRol() != Rol.CAJERO && empleadoActualSesion.getRol() != Rol.ADMINISTRADOR)) {
            throw new SecurityException("Acceso Denegado: Solo Caja y Administrador pueden vender.");
        }
    }
}