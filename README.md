# I Feel Like Nicole........

Tech-Flow — Sistema de gestión de inventario y ventas con interfaz gráfica JavaFX.

## Descripción

Fausto vende humo ...............................
## Arquitectura

El proyecto sigue una estructura por capas con separación de responsabilidades:

```
src/main/java/com/overbyte/
├── Main.java                 # Punto de entrada JavaFX y login
├── controllers/
│   ├── LoginController.java  # Autenticación y redirección por rol
│   ├── MenuController.java   # Menú genérico (consola/legacy)
│   ├── AdminController.java  # Panel de administración (Gestor)
│   ├── VendedorController.java # Punto de venta (Cajero)
│   └── AlmacenController.java  # Gestión de inventario (Almacén)
├── config/                   # Configuración de CSV
├── models/                   # Modelos de dominio (Producto, Proveedor, Cliente, Inventario)
├── services/                 # Servicios (MotorCSV, Verificador)
└── user/                     # Dominio de usuarios (Empleado, Gestor, Vendedor, Almacen, Rol)

src/main/resources/com/overbyte/
├── vista/
│   ├── login.fxml            # Pantalla de inicio de sesión
│   ├── admin.fxml            # Panel de administrador
│   ├── vendedor.fxml         # Punto de venta
│   └── almacen.fxml          # Gestión de almacén
└── styles/
    └── estilos.css           # Hojas de estilo JavaFX
```

### Capas

1. **Capa de presentación (GUI)**: Pantallas JavaFX con FXML y controladores asociados.
2. **Capa de dominio**: Clases de negocio (`Gestor`, `Vendedor`, `Almacen`, `Producto`, `Proveedor`, `Empleado`).
3. **Capa de persistencia**: Lectura y escritura de archivos CSV mediante `MotorCSV`.
4. **Capa de autenticación**: `Verificador` valida credenciales y devuelve el objeto de usuario según su rol.

## GUI por Roles

### Pantalla de Login (`login.fxml`)
- Campo de nombre de usuario y contraseña.
- Selección de rol: Administrador, Cajero, Almacén.
- Botón "INICIAR SESIÓN".
- Autenticación por nombre de usuario (no por CI).

### Panel de Administrador (`admin.fxml`)
- Ventana: 1200×800.
- Menú lateral con secciones: Caja, Inventario, Ventas, Empleados, Productos, Proveedores, Cerrar sesión.
- Área de contenido dinámica que cambia según la sección seleccionada.
- Mini formularios para agregar/eliminar empleados, productos y proveedores.
- Tabla de inventario con stock bajo resaltado.
- Tabla de ventas del día con total acumulado.
- Botón de exportar reporte CSV.

Métodos de `Gestor` conectados:
- `abrirCaja()` / `cerrarCaja()`
- `anadirEmpleado()` / `eliminarEmpleado()`
- `anadirProducto()` / `eliminarProducto()`
- `anadirProveedor()` / `eliminarProveedor()`
- `listarProductosProximosAAgotarse()`
- `exportarReporteExcelOCSV()`

### Punto de Venta / Vendedor (`vendedor.fxml`)
- Ventana: 1100×750.
- Panel izquierdo: búsqueda de productos por código, nombre o marca, con tabla de resultados y botón para agregar al carrito.
- Panel derecho: carrito de compra con nombre del cliente, items, total, botón "VENDER CARRITO", "VACIAR CARRITO".
- Otras operaciones: Eliminar venta, Consultar garantía, Importar orden CSV.
- Botón "Cerrar sesión".

Métodos de `Vendedor` conectados:
- `buscarEnInventario()` / `buscarPorRazon()`
- `generarOrdenDeVenta()` / `confirmarVenta()`
- `eliminarVenta()`
- `consultarGarantia()`
- `ingresarOrdenDesdeArchivo()`
- `generarFactura()`

### Almacén / Almacenero (`almacen.fxml`)
- Ventana: 1100×750.
- Panel izquierdo: formulario para registrar producto nuevo (ID, nombre, marca, categoría, descripción, precios, stock), reabastecer producto existente, importar entradas CSV.
- Panel derecho: tabla de inventario actual con ID, nombre, marca, categoría, precios y stock.
- Botón "Cerrar sesión".

Métodos de `Almacen` conectados:
- `ingresarProductoNuevo()`
- `ingresarProductoExistente()`
- `ingresarEntradaDesdeArchivo()`

## Accesos de Prueba

Los usuarios vienen precargados en `data/empleado.csv`:

| Rol | Usuario | Contraseña | Seleccionar en login |
|-----|---------|------------|---------------------|
| Administrador | `adm` | `user` | Administrador |
| Cajero / Vendedor | `Ventas Demo` | `ventas123` | Cajero |
| Almacenero | `Almacen Demo` | `almacen123` | Almacén |
| Almacenero (extra) | `Logan el mas capito` | `logan` | Almacén |

**Importante**: El login valida por **nombre de usuario** y **contraseña**, y requiere seleccionar el rol correcto antes de pulsar "INICIAR SESIÓN".

## Cómo Ejecutar

### Requisitos
- Java 17 o superior
- Maven 3.6+

### Opción 1: Script de lanzamiento (recomendado)

```bash
cd /home/jorge/Documentos/Tech-Flow
./run-gui.sh
```

Este script compila el proyecto y lanza la interfaz gráfica con el plugin de JavaFX.

### Opción 2: Comandos manuales

```bash
# Compilar
mvn compile

# Ejecutar la GUI
mvn javafx:run
```

### Opción 3: Modo consola (terminal)

El proyecto incluye un menú de consola en `src/main/java/Main.java` con una opción `g` para lanzar la GUI desde la terminal.

```bash
mvn exec:java -Dexec.mainClass="Main"
```

## Tecnologías

- **Java 17**
- **JavaFX 17.0.2** (Controls, FXML)
- **Maven** (gestión de dependencias y build)
- **CSV** (persistencia de datos)
- **JUnit 5** (tests unitarios en `src/test/`)

## Estructura de Datos

Los archivos CSV se almacenan en el directorio `data/`:
- `empleado.csv` — Usuarios y credenciales
- `inventario.csv` — Productos y stock
- `ventas.csv` — Registros de ventas
- `caja.csv` — Estado de la caja (abierta/cerrada)
- `clientes.csv` — Clientes registrados
- `proveedores.csv` — Proveedores
- `entradas.csv` — Entradas de inventario
- `garantias.csv` — Garantías de productos
- `pedidos.csv` — Pedidos a proveedores

## Licencia

Proyecto académico / de gestión interna. Sin licencia específica.
