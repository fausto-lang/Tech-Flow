//Esta es una funcionalidad automatizada que procesa los textos descriptivos.

package operaciones;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalizadorProducto {

    private static final int longitudMaximaFraseCorta = 50;

    private static final Pattern patronEspecificaciones = Pattern.compile(
        "\\b(\\d+\\s*(GB|TB|pulgadas|hz|mAh|MP|W)|i[3579]|Ryzen\\s*\\d+|OLED|AMOLED|4K|FHD|Bluetooth|Inalámbrico|Garantía)\\b",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );

    public List<String> obtenerCaracteristicasVenta(Producto producto) {
        String descripcion = producto.getDescripcion();
        if (descripcion == null || descripcion.isBlank()) {
            return List.of();
        }
        
        Set<String> caracteristicas = new LinkedHashSet<>();
        Matcher matcher = patronEspecificaciones.matcher(descripcion);

        while (matcher.find()) {
            caracteristicas.add(matcher.group());
        }

        if (caracteristicas.isEmpty()) {
            extraerFrasesCortas(descripcion, caracteristicas);
        }

        return new ArrayList<>(caracteristicas);
    }

    private void extraerFrasesCortas(String descripcion, Set<String> destino) {
        String[] frases = descripcion.split("[,;.\\n]");
        for (String frase : frases) {
            String limpia = frase.trim();
            if (!limpia.isEmpty() && limpia.length() <= longitudMaximaFraseCorta) {
                destino.add(limpia);
            }
        }
    }
}