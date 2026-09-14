package operaciones;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

/** Registra las entregas de los proveedores y actualiza los archivos de inventario. */
public class EntradaProducto {

	private static final String[] ENTRADAS_COLUMNAS = {
		"idProveedor", "nombreProveedor", "idProducto", "nombreProducto",
		"marca", "categoria", "precio", "cantidad", "fechaEntrada"
	};
	private static final String[] INVENTARIO_COLUMNAS = {
		"idProducto", "nombre", "marca", "categoria", "descripcion", "precio", "stock"
	};
	private static final String[] PROVEEDORES_COLUMNAS = {
		"codigoProveedor", "nombreProveedor", "contactoProveedor", "fechaEntrega"
	};

	private final Path rutaEntradas;
	private final Path rutaInventario;
	private final Path rutaProveedores;

	public EntradaProducto() {
		this("data/entradas.csv", "data/inventario.csv", "data/proveedores.csv");
	}

	public EntradaProducto(String rutaEntradas, String rutaProveedores) {
		this(rutaEntradas, "data/inventario.csv", rutaProveedores);
	}

	public EntradaProducto(String rutaEntradas, String rutaInventario, String rutaProveedores) {
		this.rutaEntradas = Path.of(rutaEntradas);
		this.rutaInventario = Path.of(rutaInventario);
		this.rutaProveedores = Path.of(rutaProveedores);
	}

	/**
	 * Añade la cantidad recibida al producto existente o crea su registro si es nuevo.
	 * Cada llamada también deja constancia de la entrega en proveedores.csv.
	 */
	public void registrarPedido(Proveedor proveedor, Producto producto, int cantidad,
			LocalDate fechaEntrega) throws IOException {
		if (proveedor == null || producto == null) {
			throw new IllegalArgumentException("El proveedor y el producto son obligatorios");
		}
		if (cantidad <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
		}
		if (fechaEntrega == null) {
			throw new IllegalArgumentException("La fecha de entrega es obligatoria");
		}

		List<Map<String, String>> entradas = leerFilas(rutaEntradas, ENTRADAS_COLUMNAS);
		boolean productoEncontrado = false;
		for (Map<String, String> entrada : entradas) {
			if (producto.getIdProducto().equals(entrada.get("idProducto"))) {
				int cantidadActual = parsearEntero(entrada.get("cantidad"), "cantidad");
				entrada.put("cantidad", String.valueOf(cantidadActual + cantidad));
				entrada.put("fechaEntrada", fechaEntrega.toString());
				productoEncontrado = true;
				break;
			}
		}

		if (!productoEncontrado) {
			Map<String, String> nuevaEntrada = new LinkedHashMap<>();
			nuevaEntrada.put("idProveedor", proveedor.getCodigoProveedor());
			nuevaEntrada.put("nombreProveedor", proveedor.getNombreProveedor());
			nuevaEntrada.put("idProducto", producto.getIdProducto());
			nuevaEntrada.put("nombreProducto", producto.getNombre());
			nuevaEntrada.put("marca", producto.getMarca());
			nuevaEntrada.put("categoria", producto.getCategoria());
			nuevaEntrada.put("precio", String.valueOf(producto.getPrecio()));
			nuevaEntrada.put("cantidad", String.valueOf(cantidad));
			nuevaEntrada.put("fechaEntrada", fechaEntrega.toString());
			entradas.add(nuevaEntrada);
		}
		escribirFilas(rutaEntradas, entradas, ENTRADAS_COLUMNAS);

		actualizarInventario(producto, cantidad);
		registrarProveedor(proveedor, fechaEntrega);
	}

	private void actualizarInventario(Producto producto, int cantidad) throws IOException {
		List<Map<String, String>> inventario = leerFilas(rutaInventario, INVENTARIO_COLUMNAS);
		for (Map<String, String> fila : inventario) {
			if (producto.getIdProducto().equals(fila.get("idProducto"))) {
				int stockActual = parsearEntero(fila.get("stock"), "stock");
				fila.put("stock", String.valueOf(stockActual + cantidad));
				escribirFilas(rutaInventario, inventario, INVENTARIO_COLUMNAS);
				return;
			}
		}

		Map<String, String> nuevaFila = new LinkedHashMap<>();
		nuevaFila.put("idProducto", producto.getIdProducto());
		nuevaFila.put("nombre", producto.getNombre());
		nuevaFila.put("marca", producto.getMarca());
		nuevaFila.put("categoria", producto.getCategoria());
		nuevaFila.put("descripcion", producto.getDescripcion());
		nuevaFila.put("precio", String.valueOf(producto.getPrecio()));
		nuevaFila.put("stock", String.valueOf(cantidad));
		inventario.add(nuevaFila);
		escribirFilas(rutaInventario, inventario, INVENTARIO_COLUMNAS);
	}

	private void registrarProveedor(Proveedor proveedor, LocalDate fechaEntrega) throws IOException {
		List<Map<String, String>> proveedores = leerFilas(rutaProveedores, PROVEEDORES_COLUMNAS);
		Map<String, String> entrega = new LinkedHashMap<>();
		entrega.put("codigoProveedor", proveedor.getCodigoProveedor());
		entrega.put("nombreProveedor", proveedor.getNombreProveedor());
		entrega.put("contactoProveedor", String.valueOf(proveedor.getContactoProveedor()));
		entrega.put("fechaEntrega", fechaEntrega.toString());
		proveedores.add(entrega);
		escribirFilas(rutaProveedores, proveedores, PROVEEDORES_COLUMNAS);
	}

	private List<Map<String, String>> leerFilas(Path ruta, String[] columnas) throws IOException {
		if (!Files.exists(ruta) || Files.size(ruta) == 0) {
			return new ArrayList<>();
		}
		CsvSchema schema = CsvSchema.emptySchema().withHeader();
		List<Map<String, String>> filas = new ArrayList<>();
		List<?> leidas = new CsvMapper().readerFor(Map.class).with(schema)
				.readValues(ruta.toFile()).readAll();
		for (Object fila : leidas) {
			Map<?, ?> original = (Map<?, ?>) fila;
			Map<String, String> normalizada = new LinkedHashMap<>();
			for (String columna : columnas) {
				Object valor = original.get(columna);
				normalizada.put(columna, valor == null ? "" : valor.toString());
			}
			filas.add(normalizada);
		}
		return filas;
	}

	private void escribirFilas(Path ruta, List<Map<String, String>> filas, String[] columnas)
			throws IOException {
		Path padre = ruta.toAbsolutePath().getParent();
		if (padre != null) {
			Files.createDirectories(padre);
		}
		CsvSchema.Builder esquema = CsvSchema.builder();
		for (String columna : columnas) {
			esquema.addColumn(columna);
		}
		CsvSchema schema = esquema.setUseHeader(true).build();
		new CsvMapper().writer(schema).writeValues(ruta.toFile()).writeAll(filas);
	}

	private int parsearEntero(String valor, String nombreCampo) {
		try {
			return Integer.parseInt(valor);
		} catch (NumberFormatException excepcion) {
			throw new IllegalArgumentException("Valor inválido para " + nombreCampo + ": " + valor,
					excepcion);
		}
	}
}

