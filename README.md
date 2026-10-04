# Nicole Kidman 4.0
fausto vende humo

## Clases

- **Entrada**
- **Venta**
- **Gestion**
- **Inventario**
  - `Map<codigo, objeto(caracteristicas)>` *(por ver)*
- **Producto**
- **MotorCsv**
  - Funciones respectivas
- **Empleado**
  - **Vendedor**
    - Verificar garantía:
      - `idCliente + fechaActual -> boolean`
  - **Gestor**
  - **Almacen**
- **Proveedor**
- **Cliente**
- **ConfiguracionCsv**
- **Verificador**
  - `Map<CI, contraseña>`
  - Verificar usuario:
    - `user.get(CI) == contraseñaIngresada`
- **Errores personalizados**
  - `Exception`
  - `VentaSinInventarioException`

---

# CSV históricos

- **Entradas**
- **Ventas**
  - Atributos:
    - Nombre o CI del cliente
    - Empleado encargado
    - Código
    - Garantía en días
- **Inventario**
- **Empleados**
- **Proveedores**

---

# CSV diarios

## `csvDiarioVentas`

De aquí nace el análisis de ventas del día:

- Ganancia
- Ventas realizadas
- Productos vendidos
- Etc.

## `csvDiarioEntradas`

Análisis similar al de ventas:

- Productos ingresados
- Cantidades
- Costos
- Proveedores
- Etc.

---

# Menús

## Login

- Ingreso mediante usuario y contraseña.
- Seleccionar el rol.

---

## Administrador

Acceso a todos los CSV.

### Funciones

- Abrir / cerrar cajas
- Ver ventas e ingresos diarios
- Ver inventario
- Añadir / eliminar empleados
- Añadir / eliminar proveedores
- Añadir / eliminar productos

---

## Almacén

Acceso a:

- Entradas
- Inventario
- Proveedores

### Funciones privadas

- Ingresar producto nuevo
  - Agregar nueva fila
- Ingresar producto existente
- Actualizar inventario en positivo
- Ingresar entrada desde CSV o TXT
  - Filas × columnas

---

## Vendedor

### Funciones privadas

- Vender un producto
- Generar orden de venta
- Ingresar orden desde CSV o TXT
- Confirmar venta
- Buscar en inventario
- Generar factura
  - Generar el texto correspondiente
- Calcular costo:
  - `P.costo + O.costo * 0.20 + IVA`

### Función pública

- Buscar por razón
  - `Map<key = razon, values = ...>`

---

# Funciones futuras

## Árbol de segmentos

- Consultas desde una fecha `X` hasta una fecha `Y`
- Acceso para el administrador
- Generar un Excel o CSV con los resultados

## Popularidad de productos

- Analizar frecuencia de ventas
- Utilizar un `Map`
- Determinar productos:
  - Populares
  - Despopulares

## Liquidación de productos

- Liquidar productos según:
  - Fecha de entrada
  - Fecha actual
  - Una razón determinada

## Descuentos

- Descuento para clientes frecuentes
- Descuento para pedidos grandes
- Aplicar según una razón o condición determinada

## Pedidos a proveedores

- Generar pedido al proveedor desde el administrador

## Productos próximos a agotarse

- Listar productos que se están acabando
- Mostrar la razón / causa
- El administrador trabaja con la información del inventario CSV
