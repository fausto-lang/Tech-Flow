import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class GestionProducto {

    private List<Producto> productos;
    private String rutaArchivoCSV;

    public GestionProducto(String rutaArchivoCSV) {
        this.rutaArchivoCSV = rutaArchivoCSV;
        this.productos = new ArrayList<>();
    }

    public void cargarProductosCSV() {

       try {
           CsvMapper mapper = new CsvMapper();

           CsvSchema schema = CsvSchema.emptySchema().withHeader();

           List<?> filas = mapper
                .readerFor(Map.class)
                .with(schema)
                .readValues(new File(rutaArchivoCSV))
                .readAll();

            for (Object filaObjeto : filas) {
                Map<?, ?> fila = (Map<?, ?>) filaObjeto;

                Producto producto = new Producto(
                    (String) fila.get("idProducto"),
                    (String) fila.get("marca"),
                    Double.parseDouble((String) fila.get("precio")),
                    (String) fila.get("descripcion"),
                    Integer.parseInt((String) fila.get("stock")),
                    (String) fila.get("nombre"),
                    (String) fila.get("categoria")
                    );
                 productos.add(producto);
             }

        } catch (Exception e) {
             System.out.println("Error al cargar productos: " + e.getMessage());
    }
}

    public Producto buscarPorId(String idProducto) {

        for (Producto p : productos) {
            if (p.getIdProducto().equals(idProducto)) {
                return p;
            }
        }

        return null;
    }

    public <T> List<T> ventasDia() {
        return new ArrayList<>();
    }

    public double gananciasTotalesDia() {
        return 0.0;
    }

    public <T> List<T> proveedores() {
        return new ArrayList<>();
    }

}