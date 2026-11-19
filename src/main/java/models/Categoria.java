package models;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

/**
 * Categorías de producto conocidas por el sistema (las que aparecen en el inventario).
 * El CSV y {@code Producto} siguen en {@code String}; este enum es la fuente de verdad
 * para el filtro y los formularios, evitando variantes por tipeo.
 */
public enum Categoria {

    ACCESORIOS("Accesorios"),
    ALMACENAMIENTO("Almacenamiento"),
    AUDIO("Audio"),
    CAMARAS("Cámaras"),
    CELULARES("Celulares"),
    COMPONENTES("Componentes"),
    COMPUTACION("Computación"),
    ENERGIA("Energía"),
    FOTOGRAFIA("Fotografía"),
    IMPRESION("Impresión"),
    MOBILIARIO("Mobiliario"),
    MONITORES("Monitores"),
    PERIFERICOS("Periféricos"),
    REDES("Redes"),
    WEARABLES("Wearables");

    private final String nombreVisible;

    Categoria(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    /** @return Nombre para mostrar en la interfaz. */
    public String nombreVisible() {
        return nombreVisible;
    }

    @Override
    public String toString() {
        return nombreVisible;
    }

    /** Busca una categoría a partir de texto libre, ignorando mayúsculas, tildes y espacios extremos. */
    public static Optional<Categoria> desde(String texto) {
        String objetivo = normalizar(texto);
        if (objetivo.isEmpty()) {
            return Optional.empty();
        }
        for (Categoria categoria : values()) {
            if (normalizar(categoria.nombreVisible).equals(objetivo)
                    || normalizar(categoria.name()).equals(objetivo)) {
                return Optional.of(categoria);
            }
        }
        return Optional.empty();
    }

    /** Normaliza texto para comparaciones: minúsculas, sin marcas diacríticas y sin espacios extremos. */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }
}
