# Tech-Flow

dentro de carpeta gestion debe estar la clase Gestion (logan)

dentro de carpeta opercaiones deben estar dos clase _
      -Entrada.java (Fausto ,eunice)--> se encarga de la entrada de profducots
      -Provedor.java (Fausti,Eeunicee)
      -Salida.class (YO MERO) -----> venta de productos 
## yas tienen llos csv para trabajr con ellos y un maven de dependencias si queires usar librerias externas 

***************************************************************************************************************
Etapa 2 refactorizar codigo 
añadir manejio de excwspciones 
añadir test 
Blindar codigo al amximo 

## Clientes (data/clientes.csv)

La clase `Cliente` (nombre, CI y lista de compras) se registra automaticamente cuando se
registra una venta (`Venta.registarVenta()`, opcion 2 del menu, que pide nombre y CI del cliente).
El csv guarda **una fila por cada producto comprado**, sin acumular cantidades entre fechas:

```
ci,nombreCliente,idProducto,nombreProducto,cantidad,fechaCompra
```

`Cliente.cargarClientesCSV("data/clientes.csv")` agrupa esas filas por CI y el menu las
muestra en la opcion 4. Los Precios y marcas no se guardan en este csv.
