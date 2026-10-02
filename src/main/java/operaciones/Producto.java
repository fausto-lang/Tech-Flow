package operaciones;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Producto {

    private static final int MAX_LONGITUD_FRASE_CORTA = 30;

    // Precompilado estático para evitar la sobrecarga de recompilar el patrón en cada llamada
    private static final Pattern PATRON_ESPECIFICACIONES = Pattern.compile(
        "\\b(\\d+\\s*(GB|TB|pulgadas|hz|mAh|MP|W)|i[3579]|Ryzen\\s*\\d+|OLED|AMOLED|4K|FHD|Bluetooth|Inalámbrico|Garantía)\\b",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );

    private final String idProducto;
    private final String marca;
    private double precioEntrada;
    private double precioVenta;
    private int stock;
    private final String nombre;
    private final String descripcion;
    private final String categoria;

    public Producto(String idProducto, String marca, double precioEntrada, double precioVenta, 
                    String descripcion, int stock, String nombre, String categoria) {
        this.idProducto = idProducto;
        this.marca = marca;
        this.precioEntrada = precioEntrada;
        this.precioVenta = precioVenta;
        this.stock = stock;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
    }
    public String getIdProducto() {
        return idProducto;
    }
    public String getMarca() {
        return marca;
    }
    public double getPrecioEntrada() {
        return precioEntrada;
    }
    public double getPrecioVenta() {
        return precioVenta;
    }
    public int getStock() {
        return stock;
    }
    public String getNombre() {
        return nombre;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public String getCategoria() {
        return categoria;
    }
    public void setPrecioEntrada(double nuevoPrecioEntrada) {
        this.precioEntrada = nuevoPrecioEntrada;
    }
    public void setPrecioVenta(double nuevoPrecioVenta) {
        this.precioVenta = nuevoPrecioVenta;
    }
    public void setStock(int nuevoStock) {
        this.stock = nuevoStock;
    }

    /**
     * Extrae palabras clave o especificaciones de venta desde la descripción del producto.
     *
     * @return Lista de características encontradas.
     */
    public List<String> obtenerCaracteristicasVenta() {
        if (descripcion == null || descripcion.isBlank()) {
            return List.of();
        }
        Set<String> caracteristicas = new LinkedHashSet<>();
        Matcher matcher = PATRON_ESPECIFICACIONES.matcher(descripcion);

        while (matcher.find()) {
            caracteristicas.add(matcher.group());
        }

        if (caracteristicas.isEmpty()) {
            extraerFrasesCortas(caracteristicas);
        }

        return new ArrayList<>(caracteristicas);
    }

    private void extraerFrasesCortas(Set<String> destino) {
        String[] frases = descripcion.split("[,;.\\n]");
        for (String frase : frases) {
            String limpia = frase.trim();
            if (!limpia.isEmpty() && limpia.length() <= MAX_LONGITUD_FRASE_CORTA) {
                destino.add(limpia);
            }
        }
    }
}