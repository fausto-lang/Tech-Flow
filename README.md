# NICOLE KIDMAN 3.0 - Plan de Desarrollo y Estructura

## 1. Arquitectura y Motores de Datos (`MotorCSV` / `MotorExcel`)
* **Transición Tecnológica:** Por decisión de Fausto, se elimina la lógica exclusiva de archivos planos para migrar al uso de **Excel** (o un motor mixto `MotorCSV` / `MotorExcel`).
* **Conversión:** Implementar un transformador bidireccional (Excel $\leftrightarrow$ CSV) o lectura directa manejando macros o integraciones de Visual Basic (`VBA`) si fuese necesario.
* **Parsing Robusto:**
  * Uso de `BufferedReader` y `StringTokenizer` para la lectura y procesamiento eficiente de registros.
  * Manejo adecuado de descripciones multilínea (ej. campos de texto extensos envueltos con saltos de línea `"\n"` entre comillas).
* **Estructuras de Datos Core:**
  * `map<marca, lista<map>>` para la categorización y anidamiento jerárquico de productos.
  * `map<codigo, contraseña>` para autenticación rápida de personal.

## 2. Módulo de Empleados, Seguridad y Turnos
* **Clase `Empleado`:**
  * **Atributos:** Código (CI), nombre, cargo, contraseña.
  * **Cargos (Enum):** `Administrador` (gestión global), `AdmAlmacen` (control de entradas), `Vendedor` (registro de ventas).
* **Control de Apertura y Cierre:**
  * Funciones para prender (abrir tienda) y apagar (cerrar tienda).
  * Registro automático en los reportes de ventas y entradas especificando qué empleados están de turno.
* **Validación Avanzada:**
  * Implementación de una función con **Bitmasks** para validar permisos y estados de forma eficiente.

## 3. Interfaz de Usuario y Flujo de Ventas
* **Interfaz Visual:**
  * Componentes `listbox` independientes para categorías y productos. Al seleccionar un producto en la lista, se despliega automáticamente su descripción detallada.
* **Procesamiento de Ventas y Compras:**
  * Estructuras de datos tipo `pila` para el procesamiento de compras.
  * Transacciones de `ListaVenta()` y `confirmarVenta()`.
* **Reportes Diarios:**
  * Generación automática de ventas del día respaldadas en un archivo independiente (Excel/CSV diario).
  * Búsqueda por fecha con complejidad temporal $O(n)$ (dejando los árboles de segmentos como un añadido teórico avanzado para otra clase).
  * *Nota de investigación:* Fausto investigará librerías externas o métodos óptimos para el manejo de fechas.

## 4. Plan de Testing y Cronograma (Deadline: Viernes)
* **Casos de Prueba (Happy Tests):**
  * Diseño de pruebas unitarias ideales en conjunto con Fausto y Eunice (ej. verificar que operaciones tipo $(1, 2, 3) = 6$ ).
  * Un caso de prueba ideal por cada función principal del sistema.
* **Hitos Inmediatos y Entregas:**
  * **Mañana (8:00 PM):** Demo de la tabla visual. Enviar capturas/fotos del funcionamiento de la tabla, junto con la implementación de Fausto en la clase `MotorCSV` o `MotorExcel`.
  * **Limpieza de Código:** Asegurarse de que el `main` esté completamente limpio y ordenado, invocando únicamente a los módulos correspondientes.
  * **Meta Final:** Dejar todo listo para el viernes con el `main` funcionando perfectamente con los "happy tests".
