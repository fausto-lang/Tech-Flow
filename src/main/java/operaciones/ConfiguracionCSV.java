package operaciones;

public class ConfiguracionCSV {
    public static final String RUTA_INVENTARIO = "data/inventario.csv";
    public static final String RUTA_PROVEEDORES = "data/proveedores.csv";
    public static final String RUTA_SALIDAS = "data/salidas.csv";
    public static final String RUTA_CLIENTES = "data/clientes.csv";
    public static final String RUTA_EMPLEADOS = "data/empleado.csv";

    public static final String[] INVENTARIO_COLUMNAS = {
        "idProducto", "nombre", "marca", "categoria", "descripcion", "precioEntrada", "precioVenta", "stock"
    };
    
    public static final String[] PROVEEDORES_COLUMNAS = {
        "codigoProveedor", "nombreProveedor", "contactoProveedor", "fechaEntrega"
    };
    
    public static final String[] CLIENTES_COLUMNAS = {
        "ci", "nombreCliente"
    };

    public static final String[] COLUMNAS_VENTA = {
        "idFactura", "ci", "idProducto", "precioEntrada", "precioVenta", "cantidad", "totalCobrado"
    };

    public static final String[] ENTRADAS_COLUMNAS = {
        "idProveedor", "nombreProveedor", "idProducto", "nombreProducto", "cantidad", "costoTotal"
    };

    public static final String[] EMPLEADOS_COLUMNAS = {
        "id", "nombre", "password", "rol"
    };

    public static final String[] SALIDAS_COLUMNAS = {
        "idProducto", "nombreProducto", "cantidad", "motivo", "fechaSalida"
    };
}