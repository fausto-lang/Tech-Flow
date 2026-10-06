package services;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import user.Almacen;
import user.Empleado;
import user.Gestor;
import user.Rol;
import user.Vendedor;

/** Autentica operadores usando el registro persistente de empleados. */
public class Verificador {

    private static final String ARCHIVO_EMPLEADOS = "empleado.csv";
    private final MotorCSV motorCSV;
    private final Map<String, String> credencialesPrueba = new HashMap<>();

    public Verificador() {
        this(Paths.get(System.getProperty("techflow.data.dir", "data")));
    }

    public Verificador(Path directorioDatos) {
        this.motorCSV = new MotorCSV(directorioDatos);
        inicializarEmpleadosSiNoExisten();
    }

    /** Valida usuario y contraseña sin exigir un rol previamente seleccionado. */
    public Empleado iniciarSesion(String nombre, String contrasenaIngresada) {
        return iniciarSesion(nombre, contrasenaIngresada, null);
    }

    /** Valida además que el rol seleccionado corresponda al operador autenticado. */
    public Empleado iniciarSesion(String nombre, String contrasenaIngresada, Rol rolSolicitado) {
        if (nombre == null || contrasenaIngresada == null || nombre.isBlank()) {
            return null;
        }
        List<String[]> empleados = motorCSV.leerCSV(rutaEmpleados());
        if (empleados.isEmpty()) {
            return null;
        }
        String[] encabezado = empleados.get(0);
        int ciCol = columna(encabezado, "ci");
        int nombreCol = columna(encabezado, "nombre");
        int contrasenaCol = columna(encabezado, "contrasena");
        int rolCol = columna(encabezado, "rol");
        if (ciCol < 0 || nombreCol < 0 || contrasenaCol < 0 || rolCol < 0) {
            return null;
        }
        for (int i = 1; i < empleados.size(); i++) {
            String[] fila = empleados.get(i);
            if (Math.max(Math.max(ciCol, nombreCol), Math.max(contrasenaCol, rolCol)) >= fila.length
                    || !fila[nombreCol].equalsIgnoreCase(nombre)
                    || !fila[contrasenaCol].equals(contrasenaIngresada)) {
                continue;
            }
            try {
                Rol rol = Rol.valueOf(fila[rolCol].trim().toUpperCase());
                if (rolSolicitado != null && rol != rolSolicitado) {
                    return null;
                }
                return switch (rol) {
                    case ADMINISTRADOR -> new Gestor(fila[ciCol], fila[nombreCol], contrasenaIngresada,
                            motorCSV.rutaDatos("").toAbsolutePath());
                    case CAJERO -> new Vendedor(fila[ciCol], fila[nombreCol], contrasenaIngresada,
                            motorCSV.rutaDatos("").toAbsolutePath());
                    case ALMACENERO -> new Almacen(fila[ciCol], fila[nombreCol], contrasenaIngresada,
                            motorCSV.rutaDatos("").toAbsolutePath());
                };
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    /** In-memory credential helpers retained for isolated service-level checks. */
    public void registrarUsuario(String ci, String contrasena) {
        if (ci != null && contrasena != null) {
            credencialesPrueba.put(ci, contrasena);
        }
    }

    public boolean verificarUsuario(String ci, String contrasena) {
        return ci != null && contrasena != null && contrasena.equals(credencialesPrueba.get(ci));
    }

    public static boolean isCajaAbierta() {
        MotorCSV motor = new MotorCSV();
        return isCajaAbierta(motor.rutaDatos("caja.csv"));
    }

    public static boolean isCajaAbierta(Path rutaCaja) {
        List<String[]> caja = new MotorCSV().leerCSV(rutaCaja.toString());
        return caja.size() > 1 && caja.get(1).length > 0 && caja.get(1)[0].equalsIgnoreCase("ABIERTA");
    }

    private void inicializarEmpleadosSiNoExisten() {
        if (!motorCSV.existeArchivo(rutaEmpleados())) {
            motorCSV.inicializarCSV(rutaEmpleados(), new String[]{"ci", "nombre", "rol", "contrasena"});
            motorCSV.agregarFila(rutaEmpleados(), new String[]{"ADM-001", "adm", "ADMINISTRADOR", "user"});
        }
    }

    private String rutaEmpleados() {
        return motorCSV.rutaDatos(ARCHIVO_EMPLEADOS).toString();
    }

    private int columna(String[] encabezado, String nombre) {
        for (int i = 0; i < encabezado.length; i++) {
            if (encabezado[i].trim().equalsIgnoreCase(nombre)) {
                return i;
            }
        }
        return -1;
    }
}