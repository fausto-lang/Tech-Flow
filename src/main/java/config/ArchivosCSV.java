package config;

/**
 * Nombres canónicos de los archivos CSV que persiste el sistema y
 * encabezados compartidos, para no repetir literales en dominio y vistas.
 */
public final class ArchivosCSV {

    private ArchivosCSV() {}

    public static final String INVENTARIO = "inventario.csv";
    public static final String VENTAS = "ventas.csv";
    public static final String CLIENTES = "clientes.csv";
    public static final String GARANTIAS = "garantias.csv";
    public static final String CAJA = "caja.csv";
    public static final String EMPLEADOS = "empleado.csv";
    public static final String PROVEEDORES = "proveedores.csv";
    public static final String ENTRADAS = "entradas.csv";
    public static final String PEDIDOS = "pedidos.csv";

    /** Encabezado canónico del inventario (compartido por Gestor y Almacen). */
    public static final String[] INVENTARIO_ENCABEZADO = {"idProducto", "nombre", "marca", "categoria",
            "descripcion", "precioEntrada", "precioVenta", "stock"};
}
