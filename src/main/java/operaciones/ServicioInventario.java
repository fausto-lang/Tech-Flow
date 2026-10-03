package operaciones;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ServicioInventario {
    // ... (Tus dependencias actuales: MotorCSV) ...
    private final GestorCajaDiaria caja;

    public ServicioInventario(MotorCSV motorCSV, GestorCajaDiaria caja) {
        // ...
        this.caja = caja;
    }

    // 1. Buscar por Nombre, Marca o ambos
    public List<Producto> buscarProducto(List<Producto> stock, String nombre, String marca) {
        return stock.stream()
            .filter(p -> (nombre == null || p.getNombre().toLowerCase().contains(nombre.toLowerCase())))
            .filter(p -> (marca == null || p.getMarca().toLowerCase().contains(marca.toLowerCase())))
            .collect(Collectors.toList());
    }

    // 2. Registrar producto NUEVO (no existe en CSV)
    public void registrarNuevoProducto(Producto p, List<Producto> stockActual) throws IOException {
        boolean existe = stockActual.stream().anyMatch(prod -> prod.getIdProducto().equals(p.getIdProducto()));
        if (existe) throw new IllegalArgumentException("El ID del producto ya existe.");
        
        stockActual.add(p);
        // Aquí llamas a tu MotorCSV para reescribir inventario.csv completo
    }

    // 3. Registrar INGRESO de stock (Producto ya existe) y guardar en CSV jerárquico
    public void registrarIngresoStock(Producto p, int cantidadIngresada) throws IOException {
        p.setStock(p.getStock() + cantidadIngresada);
        
        // Guardar reporte en data/entradas/año/mes/dia/entradas.csv
        String rutaDiaria = caja.obtenerRutaEntradasDia();
        // Lógica de MotorCSV para hacer "append" a ese archivo con: ID, Cantidad, Fecha, Proveedor
    }
}