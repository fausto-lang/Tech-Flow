package config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConfiguracionCSV")
class ConfiguracionCSVTest {

    @Test
    @DisplayName("El delimitador por defecto es la coma")
    void delimitadorPorDefecto() {
        assertEquals(",", ConfiguracionCSV.DELIMITADOR_POR_DEFECTO);
    }

    @Test
    @DisplayName("La codificación es UTF-8")
    void codificacion() {
        assertEquals("UTF-8", ConfiguracionCSV.CODIFICACION);
    }

    @Test
    @DisplayName("Se puede instanciar")
    void sePuedeInstanciar() {
        assertNotNull(new ConfiguracionCSV());
    }
}
