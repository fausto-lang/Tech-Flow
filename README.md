# NICOLE KIDMAN 6.00
Aplicacion de consola para administrar inventario, recepciones de proveedores y ventas de una tienda. Requiere JDK 21 y Maven.

## Ejecutar

Desde la raiz del proyecto:

```powershell
mvn test
mvn compile
java -cp target/classes Main
```

El primer inicio crea `data/empleado.csv` y un administrador si el archivo aun no existe. En los datos incluidos, el acceso inicial solicitado es `adm` / `user`. Tambien hay operadores de demostracion: `Admin Demo` / `admin123`, `Ventas Demo` / `ventas123` y `Almacen Demo` / `almacen123`. El rol seleccionado al entrar debe coincidir con el rol guardado para ese usuario.

Los datos se guardan en `data/`. Se puede usar otro directorio configurando `-Dtechflow.data.dir=ruta`; por ejemplo, para iniciar con un directorio de prueba:

```powershell
java -Dtechflow.data.dir=./tmp-data -cp target/classes Main
```

## Operacion

- El administrador abre la caja; solo entonces se pueden confirmar ventas. Se pueden emitir proformas con la caja cerrada. Los informes siguen disponibles en ambos estados.
- El cajero busca por identificador, nombre o marca y administra el carrito: anade productos, cambia cantidades o elimina lineas. Puede generar una venta o guardar una proforma. La proforma no es una factura final ni descuenta existencias; al confirmar la venta con la caja abierta, el sistema persiste la factura y descuenta stock una sola vez.
- El nombre que proporciona el cliente se guarda en la venta y se imprime en la factura; no se requiere cedula del cliente.
- El operador de almacen registra productos o entradas. Las importaciones validan sus filas y el proveedor indicado antes de actualizar inventario.
- Las ventas confirmadas se pueden anular; la anulacion devuelve al inventario las unidades vendidas.

El precio unitario de la proforma aplica la formula definida en el proyecto: `precioEntrada + (costoOperativo * 0.20) + IVA`. Actualmente la aplicacion usa un costo operativo de 5 y un IVA equivalente al 13% del costo de entrada.

## Archivos CSV

Los archivos usan UTF-8, coma como delimitador y comillas dobles para campos con comas, comillas o saltos de linea. No se deben reordenar columnas sin actualizar el codigo que las consume.

| Archivo | Columnas |
| --- | --- |
| `empleado.csv` | `ci,nombre,rol,contrasena` |
| `proveedores.csv` | `idProveedor,nombreProveedor,contactoProveedor` |
| `inventario.csv` | `idProducto,nombre,marca,categoria,descripcion,precioEntrada,precioVenta,stock` |
| `entradas.csv` | `idEntrada,idProveedor,idProducto,nombreProducto,cantidad,precioUnitario,fechaEntrada,ciEmpleado` |
| `ventas.csv` | `idVenta,idOrden,estado,ciCliente,nombreCliente,idProducto,nombreProducto,cantidad,precioUnitario,total,fechaVenta,ciEmpleado,idCliente,telefonoCliente,idGarantia` |
| `caja.csv` | `estado,fechaApertura,fechaCierre,ciEmpleado` |
| `pedidos.csv` | `idPedido,idProveedor,idProducto,cantidad,fechaPedido,ciEmpleado` |
| `clientes.csv` | `idCliente,nombreCliente,ci,telefono` |
| `garantias.csv` | `idGarantia,idVenta,idOrden,idCliente,ciCliente,nombreCliente,telefonoCliente,idProducto,nombreProducto,fechaInicio,fechaFin,estado,ciEmpleado` |

Los estados de venta son `PROFORMA`, `CONFIRMADA` y `ANULADA`; la caja usa `ABIERTA` o `CERRADA`. Los archivos de orden para importacion llevan encabezado y las columnas `idOrden` (opcional), `nombreCliente`, `idProducto` y `cantidad`. Los archivos de entradas llevan `idProveedor`, `idProducto`, `nombreProducto`, `marca`, `categoria`, `precio` y `cantidad`; proveedor, nombre, marca, categoria y precio son requeridos cuando se importa un producto nuevo. Se admite `.csv` y `.txt` con el mismo formato delimitado por comas.

Para confirmar la venta, el cajero registra nombre, CI/documento y telefono del cliente. `clientes.csv` guarda esos datos y un ID interno; `ciCliente` conserva la CI, mientras `idCliente` referencia al registro. Los clientes con la misma CI se reutilizan. Las proformas no guardan un cliente definitivo ni crean garantias.

Cada linea de venta confirmada genera una garantia independiente de 12 meses desde la fecha de confirmacion, con su propio codigo. En la factura se imprimen los codigos y vencimientos. El cajero puede consultar su vigencia por codigo de garantia, ID de venta u orden. Una garantia solo se informa vigente cuando su venta esta confirmada y la fecha actual no supera su vencimiento. El plazo se configura en `MESES_GARANTIA` de `Vendedor`.

Las contrasenas del CSV estan en texto plano para mantener el alcance academico y la compatibilidad con los archivos actuales. No usar estas credenciales ni esta persistencia como sistema de produccion sin incorporar almacenamiento seguro de secretos, control de acceso del sistema operativo y copias de seguridad.
