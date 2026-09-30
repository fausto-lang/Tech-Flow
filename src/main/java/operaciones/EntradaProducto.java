package operaciones;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class EntradaProducto {

    private static final String RUTA_ENTRADAS = "data/entradas.csv";
    private static final String RUTA_INVENTARIO = "data/inventario.csv";
    private static final String RUTA_PROVEEDORES = "data/proveedores.csv";

    private static final String[] ENTRADAS_COLUMNAS = {
        "idProveedor",
        "nombreProveedor",
        "idProducto",
        "nombreProducto",
        "marca",
        "categoria",
        "precio",
        "cantidad",
        "fechaEntrada"
    };

    private static final String[] INVENTARIO_COLUMNAS = {
        "idProducto",
        "nombre",
        "marca",
        "categoria",
        "descripcion",
        "precio",
        "stock"
    };

    private static final String[] PROVEEDORES_COLUMNAS = {
        "codigoProveedor",
        "nombreProveedor",
        "contactoProveedor",
        "fechaEntrega"
    };

    // ==========================================================
    // BUSCAR PROVEEDOR
    // ==========================================================

    public Proveedor buscarProveedor(String codigoProveedor) throws IOException {

        if (codigoProveedor == null || codigoProveedor.isBlank()) {
            return null;
        }

        codigoProveedor = codigoProveedor.trim().toUpperCase();

        List<Map<String, String>> proveedores =
                leerFilas(RUTA_PROVEEDORES, PROVEEDORES_COLUMNAS);

        for (Map<String, String> fila : proveedores) {

            String codigo = fila.get("codigoProveedor");

            if (codigo != null &&
                codigo.trim().equalsIgnoreCase(codigoProveedor)) {

                int contacto = 0;

                try {
                    contacto = Integer.parseInt(
                            fila.get("contactoProveedor")
                    );
                } catch (Exception e) {
                    contacto = 0;
                }

                return new Proveedor(
                        fila.get("nombreProveedor"),
                        codigo,
                        contacto
                );
            }
        }

        return null;
    }

    // ==========================================================
    // BUSCAR PRODUCTO POR ID
    // ==========================================================

    public Producto buscarProducto(String idProducto) throws IOException {

        if (idProducto == null || idProducto.isBlank()) {
            return null;
        }

        idProducto = idProducto.trim().toUpperCase();

        List<Map<String, String>> inventario =
                leerFilas(RUTA_INVENTARIO, INVENTARIO_COLUMNAS);

        for (Map<String, String> fila : inventario) {

            String id = fila.get("idProducto");

            if (id != null && id.equalsIgnoreCase(idProducto)) {

                return convertirProducto(fila);
            }
        }

        return null;
    }

    // ==========================================================
    // BUSCAR PRODUCTO POR NOMBRE
    // ==========================================================

    public Producto buscarProductoPorNombre(String nombre) throws IOException {

        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        nombre = nombre.trim();

        List<Map<String, String>> inventario =
                leerFilas(RUTA_INVENTARIO, INVENTARIO_COLUMNAS);

        for (Map<String, String> fila : inventario) {

            String nombreGuardado = fila.get("nombre");

            if (nombreGuardado != null &&
                nombreGuardado.trim().equalsIgnoreCase(nombre)) {

                return convertirProducto(fila);
            }
        }

        return null;
    }

    // ==========================================================
    // GENERAR ID DE PRODUCTO
    // ==========================================================

    public String generarIdProducto() throws IOException {

        List<Map<String, String>> inventario =
                leerFilas(RUTA_INVENTARIO, INVENTARIO_COLUMNAS);

        int mayor = 0;

        for (Map<String, String> fila : inventario) {

            String id = fila.get("idProducto");

            if (id == null) {
                continue;
            }

            id = id.trim().toUpperCase();

            if (id.startsWith("PROD-")) {

                try {
                    int numero = Integer.parseInt(id.substring(5));

                    if (numero > mayor) {
                        mayor = numero;
                    }

                } catch (NumberFormatException e) {
                    // Ignorar IDs que no tengan formato correcto
                }
            }
        }

        return String.format("PROD-%05d", mayor + 1);
    }

    // ==========================================================
    // GENERAR ID DE PROVEEDOR
    // ==========================================================

    public String generarCodigoProveedor() throws IOException {

        List<Map<String, String>> proveedores =
                leerFilas(RUTA_PROVEEDORES, PROVEEDORES_COLUMNAS);

        int mayor = 0;

        for (Map<String, String> fila : proveedores) {

            String codigo = fila.get("codigoProveedor");

            if (codigo == null) {
                continue;
            }

            codigo = codigo.trim().toUpperCase();

            if (codigo.startsWith("PROV-")) {

                try {
                    int numero = Integer.parseInt(codigo.substring(5));

                    if (numero > mayor) {
                        mayor = numero;
                    }

                } catch (NumberFormatException e) {
                    // Ignorar códigos incorrectos
                }
            }
        }

        return String.format("PROV-%05d", mayor + 1);
    }

    // ==========================================================
    // REGISTRAR PEDIDO / ENTRADA
    // ==========================================================

    public void registrarPedido(
            Proveedor proveedor,
            Producto producto,
            int cantidad,
            double precio) throws IOException {

        if (proveedor == null) {
            throw new IllegalArgumentException("El proveedor no puede ser nulo.");
        }

        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
        }

        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }

        // La fecha se genera automáticamente
        LocalDate fechaEntrada = LocalDate.now();

        List<Map<String, String>> entradas =
                leerFilas(RUTA_ENTRADAS, ENTRADAS_COLUMNAS);

        boolean encontrado = false;

        for (Map<String, String> fila : entradas) {

            String id = fila.get("idProducto");

            if (id != null &&
                id.equalsIgnoreCase(producto.getIdProducto())) {

                int cantidadActual = 0;

                try {
                    cantidadActual = Integer.parseInt(
                            fila.get("cantidad")
                    );
                } catch (Exception e) {
                    cantidadActual = 0;
                }

                fila.put(
                        "cantidad",
                        String.valueOf(cantidadActual + cantidad)
                );

                fila.put(
                        "precio",
                        String.valueOf(precio)
                );

                fila.put(
                        "fechaEntrada",
                        fechaEntrada.toString()
                );

                encontrado = true;
                break;
            }
        }

        // Si no existe la entrada, se crea
        if (!encontrado) {

            Map<String, String> nuevaEntrada =
                    new LinkedHashMap<>();

            nuevaEntrada.put(
                    "idProveedor",
                    proveedor.getCodigoProveedor()
            );

            nuevaEntrada.put(
                    "nombreProveedor",
                    proveedor.getNombreProveedor()
            );

            nuevaEntrada.put(
                    "idProducto",
                    producto.getIdProducto()
            );

            nuevaEntrada.put(
                    "nombreProducto",
                    producto.getNombre()
            );

            nuevaEntrada.put(
                    "marca",
                    producto.getMarca()
            );

            nuevaEntrada.put(
                    "categoria",
                    producto.getCategoria()
            );

            nuevaEntrada.put(
                    "precio",
                    String.valueOf(precio)
            );

            nuevaEntrada.put(
                    "cantidad",
                    String.valueOf(cantidad)
            );

            nuevaEntrada.put(
                    "fechaEntrada",
                    fechaEntrada.toString()
            );

            entradas.add(nuevaEntrada);
        }

        escribirFilas(
                RUTA_ENTRADAS,
                entradas,
                ENTRADAS_COLUMNAS
        );

        // Actualizamos inventario
        actualizarInventario(
                producto,
                cantidad,
                precio
        );

        // Registramos proveedor
        registrarProveedor(
                proveedor,
                fechaEntrada
        );
    }

    // ==========================================================
    // ACTUALIZAR INVENTARIO
    // ==========================================================

    private void actualizarInventario(
            Producto producto,
            int cantidad,
            double precio) throws IOException {

        List<Map<String, String>> inventario =
                leerFilas(RUTA_INVENTARIO, INVENTARIO_COLUMNAS);

        boolean encontrado = false;

        for (Map<String, String> fila : inventario) {

            String id = fila.get("idProducto");

            if (id != null &&
                id.equalsIgnoreCase(producto.getIdProducto())) {

                int stockActual = 0;

                try {
                    stockActual = Integer.parseInt(
                            fila.get("stock")
                    );
                } catch (Exception e) {
                    stockActual = 0;
                }

                fila.put(
                        "stock",
                        String.valueOf(stockActual + cantidad)
                );

                // Único precio del sistema
                fila.put(
                        "precio",
                        String.valueOf(precio)
                );

                encontrado = true;
                break;
            }
        }

        if (!encontrado) {

            Map<String, String> nuevaFila =
                    new LinkedHashMap<>();

            nuevaFila.put(
                    "idProducto",
                    producto.getIdProducto()
            );

            nuevaFila.put(
                    "nombre",
                    producto.getNombre()
            );

            nuevaFila.put(
                    "marca",
                    producto.getMarca()
            );

            nuevaFila.put(
                    "categoria",
                    producto.getCategoria()
            );

            nuevaFila.put(
                    "descripcion",
                    producto.getDescripcion()
            );

            nuevaFila.put(
                    "precio",
                    String.valueOf(precio)
            );

            nuevaFila.put(
                    "stock",
                    String.valueOf(cantidad)
            );

            inventario.add(nuevaFila);
        }

        escribirFilas(
                RUTA_INVENTARIO,
                inventario,
                INVENTARIO_COLUMNAS
        );
    }

    // ==========================================================
    // REGISTRAR PROVEEDOR
    // ==========================================================

    private void registrarProveedor(
            Proveedor proveedor,
            LocalDate fechaEntrega) throws IOException {

        List<Map<String, String>> proveedores =
                leerFilas(RUTA_PROVEEDORES, PROVEEDORES_COLUMNAS);

        boolean encontrado = false;

        for (Map<String, String> fila : proveedores) {

            String codigo = fila.get("codigoProveedor");

            if (codigo != null &&
                codigo.equalsIgnoreCase(
                        proveedor.getCodigoProveedor())) {

                fila.put(
                        "nombreProveedor",
                        proveedor.getNombreProveedor()
                );

                fila.put(
                        "contactoProveedor",
                        String.valueOf(
                                proveedor.getContactoProveedor()
                        )
                );

                fila.put(
                        "fechaEntrega",
                        fechaEntrega.toString()
                );

                encontrado = true;
                break;
            }
        }

        if (!encontrado) {

            Map<String, String> nuevaFila =
                    new LinkedHashMap<>();

            nuevaFila.put(
                    "codigoProveedor",
                    proveedor.getCodigoProveedor()
            );

            nuevaFila.put(
                    "nombreProveedor",
                    proveedor.getNombreProveedor()
            );

            nuevaFila.put(
                    "contactoProveedor",
                    String.valueOf(
                            proveedor.getContactoProveedor()
                    )
            );

            nuevaFila.put(
                    "fechaEntrega",
                    fechaEntrega.toString()
            );

            proveedores.add(nuevaFila);
        }

        escribirFilas(
                RUTA_PROVEEDORES,
                proveedores,
                PROVEEDORES_COLUMNAS
        );
    }

    // ==========================================================
    // CONVERTIR FILA A PRODUCTO
    // ==========================================================

    private Producto convertirProducto(
            Map<String, String> fila) {

        double precio = 0;
        int stock = 0;

        try {
            precio = Double.parseDouble(
                    fila.get("precio")
            );
        } catch (Exception e) {
            precio = 0;
        }

        try {
            stock = Integer.parseInt(
                    fila.get("stock")
            );
        } catch (Exception e) {
            stock = 0;
        }

        return new Producto(
                fila.get("idProducto"),
                fila.get("marca"),
                precio,
                fila.get("descripcion"),
                stock,
                fila.get("nombre"),
                fila.get("categoria")
        );
    }

    // ==========================================================
    // LEER CSV
    // ==========================================================

    private List<Map<String, String>> leerFilas(
            String ruta,
            String[] columnas) throws IOException {

        List<Map<String, String>> filas =
                new ArrayList<>();

        File archivo = new File(ruta);

        if (!archivo.exists() || archivo.length() == 0) {
            return filas;
        }

        CsvSchema esquema =
                CsvSchema.emptySchema().withHeader();

        CsvMapper mapper = new CsvMapper();

        try (MappingIterator<Map<String, String>> registros =
                mapper.readerFor(Map.class)
                      .with(esquema)
                      .readValues(archivo)) {

            while (registros.hasNext()) {

                Map<?, ?> original = registros.next();

                Map<String, String> normalizada =
                        new LinkedHashMap<>();

                for (String columna : columnas) {

                    Object valor = original.get(columna);

                    normalizada.put(
                            columna,
                            valor == null
                                    ? ""
                                    : valor.toString().trim()
                    );
                }

                filas.add(normalizada);
            }
        }

        return filas;
    }

    // ==========================================================
    // ESCRIBIR CSV
    // ==========================================================

    private void escribirFilas(
            String ruta,
            List<Map<String, String>> filas,
            String[] columnas) throws IOException {

        File archivo = new File(ruta);

        File carpeta =
                archivo.getAbsoluteFile().getParentFile();

        if (carpeta != null) {
            carpeta.mkdirs();
        }

        CsvSchema.Builder constructorEsquema =
                CsvSchema.builder();

        for (String columna : columnas) {
            constructorEsquema.addColumn(columna);
        }

        CsvSchema esquema =
                constructorEsquema
                        .setUseHeader(true)
                        .build();

        try (SequenceWriter escritor =
                new CsvMapper()
                        .writer(esquema)
                        .writeValues(archivo)) {

            escritor.writeAll(filas);
        }
    }
}