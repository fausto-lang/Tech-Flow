# Tech-Flow

Aplicación de escritorio para apoyar la gestión de inventario, entradas y ventas
de una tienda. Está desarrollada en Java 17 con JavaFX y guarda sus datos locales
en archivos CSV dentro de `data/`.

## Funcionalidades

- Acceso de empleados con los roles administrador, cajero y almacenero.
- Administración de caja, productos, empleados y proveedores.
- Consulta de inventario y alerta visual para productos con cinco unidades o menos.
- Registro e importación de entradas de almacén.
- Búsqueda de productos, carrito, proformas, ventas, anulaciones y garantías.
- Exportación de resúmenes e informes de texto desde las pantallas correspondientes.

## Requisitos

- JDK 17.
- Maven instalado y disponible como `mvn`.
- Entorno gráfico de escritorio para ejecutar JavaFX.

## Compilar, probar y ejecutar

Desde esta carpeta, donde se encuentra `pom.xml`:

```powershell
mvn test
mvn javafx:run
```

Para limpiar y ejecutar las pruebas:

```powershell
mvn clean test
```

Las pruebas usan JUnit 5. Los resultados detallados se guardan en
`target/surefire-reports/`.

El punto de entrada configurado por Maven es la interfaz JavaFX (`com.overbyte.Main`).
El código también conserva un menú de consola en `src/main/java/Main.java`; ese
archivo no es el punto de entrada que usa `mvn javafx:run`.

## Datos y copias de seguridad

La aplicación usa los CSV de `data/` como almacenamiento de trabajo. Haz una
copia de esa carpeta antes de ejecutar operaciones que modifiquen datos o de
reemplazar archivos. Las contraseñas se guardan en texto legible en
`data/empleado.csv`; los datos de muestra no deben reutilizarse como credenciales
de producción.

## Documentación

La carpeta [`documentacion/`](documentacion/DOCUMENTACION.md) contiene el informe técnico,
el manual de instalación y el manual de usuario, junto con instrucciones para
generar sus PDF.
