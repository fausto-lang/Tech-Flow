# Tests pendientes por implementar (Tech-Flow)

Este archivo lista qué se debería cubrir con más tests unitarios (JUnit 5) en la carpeta `src/test/java`. No se modifica código de producción (`src/main`).

## Estilo a seguir
- Un `@Test` por aserción (un solo `assert`).
- Métodos `public`.
- `@DisplayName` descriptivo del escenario.
- Nombre del test tipo `devuelveError()` / `devuelveX()`.
- Variables universales para evitar crear instancias en cada test.

## Clases que ya tienen tests

| Clase | Estado |
|-------|--------|
| `services.MotorCSVTest` | Muy cubierto (lectura/escritura, comillas, delimitadores, CRLF, Unicode, rutas seguras, importar/exportar). |
| `user.VendedorTest` | Muy cubierto (ventas, stock, garantías, caja cerrada, importar órdenes, factura, `calcularCosto`, `buscarPorRazon`). |
| `user.GestorTest` | Bien cubierto (caja, empleados, proveedores, productos, pedidos, stock bajo, reporte). |
| `user.AlmacenTest` | Bien cubierto (ingresar producto nuevo/existente, importar entradas, columnas reordenadas, filas inválidas). |
| `services.VerificadorTest` | Parcial (credenciales in-memory y `iniciarSesion` básico). |
| `models.InventarioTest` | Parcial (registro, búsqueda, stock, ventas, popularidad, bajo stock). |
| `models.ProductoTest` | Bien cubierto (constructor, equals, hashCode, bordes). |
| `models.ClienteTest` | Bien cubierto (constructor, `isEsFrecuente`, equals, bordes). |
| `models.ProveedorTest` | Bien cubierto (constructor, `entregarProducto`, equals, bordes). |
| `user.RolTest` | Bien cubierto (enum, `valueOf`, `values`, `name`). |
| `config.ConfiguracionCSVTest` | Bien cubierto (delimitador, codificación, instanciación). |
| `services.CsvArchivosTest` | Bien cubierto (contratos de todos los CSV fixtures). |
| `MainTest` | Integración del menú principal (tiene 4 fallos preexistentes no relacionados con este archivo). |

## Qué falta testear (por clase)

### `services.VerificadorTest`
- [ ] `iniciarSesion` con rol `ALMACENERO` devuelve instancia de `Almacen`.
- [ ] `iniciarSesion` con empleado que no existe en el CSV devuelve `null`.
- [ ] `iniciarSesion` con archivo de empleados vacío devuelve `null`.
- [ ] `verificarUsuario` con CI inexistente devuelve `false`.
- [ ] `isCajaAbierta()` (método estático sin Path) con `caja.csv` ausente devuelve `false`.

### `user.VendedorTest`
- [ ] `eliminarVenta` sobre venta ya anulada devuelve `false`.
- [ ] `consultarGarantia` con referencia `null` devuelve lista con mensaje de error.
- [ ] `consultarGarantia` con garantía vencida devuelve "NO VIGENTE".
- [ ] `generarFactura` con lista de líneas vacía devuelve factura con total 0.
- [ ] `buscarEnInventario` con término en blanco devuelve `null`.
- [ ] `ingresarOrdenDesdeArchivo` con archivo vacío (solo cabecera) devuelve `false`.

### `user.AlmacenTest`
- [ ] `ingresarProductoExistente` con cantidad negativa devuelve `false`.
- [ ] `ingresarProductoExistente` con producto inexistente no modifica entradas.
- [ ] `ingresarEntradaDesdeArchivo` con archivo vacío devuelve `false`.

### `user.GestorTest`
- [ ] `anadirEmpleado` con CI vacío devuelve `false`.
- [ ] `anadirEmpleado` con contraseña vacía devuelve `false`.
- [ ] `anadirProveedor` con código vacío devuelve `false`.
- [ ] `anadirProducto` con nombre en blanco devuelve `false`.
- [ ] `listarProductosProximosAAgotarse` con stock exactamente 5 lo incluye.
- [ ] `generarPedidoProveedor` con proveedor inexistente no crea archivo de pedidos.

### `models.InventarioTest`
- [ ] `registrarVenta` con producto inexistente devuelve `false`.
- [ ] `actualizarStock` con producto inexistente devuelve `false`.
- [ ] `buscarEnInventario` con cadena vacía devuelve `null`.
- [ ] `obtenerPopularidadProductos` devuelve mapa vacío cuando no hay ventas.

### `services.MotorCSVTest`
- [ ] `eliminarFilaPorClave` con índice de columna fuera de rango no lanza excepción.
- [ ] `rutaDatos` con ruta absoluta devuelve la ruta tal cual.
- [ ] `importarDatos` con destino vacío devuelve `false`.

## Integración (flujo end-to-end)
- [ ] Test que use `System.setProperty("techflow.data.dir", tempDir)` y recorra:
  `Gestor.abrirCaja()` → `Almacen.ingresarProductoNuevo(...)` → `Vendedor.generarOrdenDeVenta(...)` → `confirmarVenta(...)` → `Gestor.listarProductosProximosAAgotarse()`.

## Configuración necesaria
- `pom.xml` ya tiene `junit-jupiter` y `maven-surefire-plugin` para que `mvn test` ejecute todo.
