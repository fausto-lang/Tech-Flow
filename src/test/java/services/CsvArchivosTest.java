package services;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Contratos de archivos CSV")
class CsvArchivosTest {

    @TempDir
    Path directorio;

    private static final Map<String, String[]> ESQUEMAS = new LinkedHashMap<>();

    static {
        ESQUEMAS.put("caja.csv", new String[]{"estado", "fechaApertura", "fechaCierre", "ciEmpleado"});
        ESQUEMAS.put("clientes.csv", new String[]{"idCliente", "nombreCliente", "ci", "telefono"});
        ESQUEMAS.put("empleado.csv", new String[]{"ci", "nombre", "rol", "contrasena"});
        ESQUEMAS.put("entradas.csv", new String[]{"idProveedor", "nombreProveedor", "idProducto", "nombreProducto",
                "marca", "categoria", "precio", "cantidad", "fechaEntrada", "ciEmpleado"});
        ESQUEMAS.put("garantias.csv", new String[]{"idGarantia", "idVenta", "idOrden", "idCliente",
                "ciCliente", "nombreCliente", "telefonoCliente", "idProducto", "nombreProducto", "fechaInicio",
                "fechaFin", "estado", "ciEmpleado"});
        ESQUEMAS.put("inventario.csv", new String[]{"idProducto", "nombre", "marca", "categoria", "descripcion",
                "precioEntrada", "precioVenta", "stock"});
        ESQUEMAS.put("pedidos.csv", new String[]{"idPedido", "idProveedor", "idProducto", "cantidad", "fechaPedido",
                "ciEmpleado"});
        ESQUEMAS.put("proveedores.csv", new String[]{"idProveedor", "nombreProveedor", "contactoProveedor"});
        ESQUEMAS.put("ventas.csv", new String[]{"idVenta", "idOrden", "estado", "ciCliente", "nombreCliente",
                "idProducto", "nombreProducto", "cantidad", "precioUnitario", "total", "fechaVenta", "ciEmpleado",
                "idCliente", "telefonoCliente", "idGarantia"});
    }

    @Test
    @DisplayName("Todos los CSV tienen fixture, cabecera estable y filas compatibles")
    void todosLosCsvRespetanSuContrato() throws Exception {
        MotorCSV motorCSV = new MotorCSV(directorio);

        for (Map.Entry<String, String[]> contrato : ESQUEMAS.entrySet()) {
            URL recurso = getClass().getClassLoader().getResource(contrato.getKey());
            assertNotNull(recurso, "Falta fixture para " + contrato.getKey());
            Path origen = Paths.get(recurso.toURI());
            List<String[]> filas = motorCSV.leerCSV(origen.toString());

            assertFalse(filas.isEmpty(), "El fixture esta vacio: " + contrato.getKey());
            assertArrayEquals(contrato.getValue(), filas.get(0), contrato.getKey());
            assertTrue(filas.size() > 1, "El fixture debe tener datos: " + contrato.getKey());
            for (int indice = 1; indice < filas.size(); indice++) {
                int filaActual = indice;
                assertTrue(filas.get(indice).length == contrato.getValue().length,
                        () -> contrato.getKey() + " tiene ancho invalido en la fila " + filaActual);
            }

            Path destino = directorio.resolve(contrato.getKey());
            assertTrue(motorCSV.escribirCSV(destino.toString(), filas),
                    "No se pudo escribir " + contrato.getKey());
            assertArrayEquals(contrato.getValue(), motorCSV.leerCSV(destino.toString()).get(0));
        }
    }
}
