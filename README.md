clases 
** Entrada ---x
** Venta ---- x
** Gestion ----x
** Inventario --- map<codigo, objeto(carcterizticas)>(por ver)
** Producto
** MotorCsv(funciones repsectiva)
** Empleado()--> * Vendedor () ** verfifvcar garantia(id cliente +  fecah actual -->boolean)
                * Gestor()
                * Almacen()
** Producto()
** Proveedor()
** Cliente()
** ConfiguracionCsv
** Veroficador()---->  map<ci,contraseña> user if (user.get(ci)== ciantarseñal que enmtra )
** Eroroes personaliados 
exception e -------VentasinInventarioExceeeprtiom
                    
***************************************************
-Csv historicos
-Entrada ()
-Ventas ( atributos + nombre o ci cliente+ empleadoEncargaso codigo +garantia dias)
-Inventario
-Empleados
-Proveedores;
**********************************************************************************
 csvDiario ventas --> de ahi nace analisis de ventas del dia gandancia etc
 csvDiario entradas--->lo mimo de anterior;
 *********************************************************************************
    MENUS //
 login *******
 seleccionar el rol 
 -adm ( contraseña  user ) acceso a todos los csv 
   --abrir /cerrar cajas***
   --ver ventas y ingresos diarios *** 
   --ver inventario  ***
   -- añadir/eliminar empleados
   -- añadir/ eliminar provedores
   --añadir productos o eliminar productos 
 -almacen entrada y inventario y provedoor csv 
  -- private Ingrea producto nuevo fila 
  -- private ingresa producto exxisteente
  -- private ACTUALIXA INVEntario en positivo
  -- ingresa entrad ( csv o txt ) filas * columnas***
 - vendedor 
  -- private vende un  producto 
  --proivate genmerar orden de venta ()
  -- ingreso de orden( csv o txt ) ***
  ---prvate confirmar venta
  -- buscar en inventario private 
  -- generarfactura();---->genrar el text 
  -- publico buscar por  xxxxxx razon  map<key= razon , values>() 
  -- private poner costo p.csoto+ o.costo*0.20 + iva;
  --------------------------------------------------------------------------------
************************************************
futuras funciones ()
 --//arbol de segmentos 
   ---- de x fecha hasta y == consultas( administrador) y aquiu podemos genrar un excel o csv 
 --// popularidad  y despopular de producto  analsiis de frecuencia en ventas map 
 --dar liquidacion productos (fecah de entada - actaul con una razon )
 ---dar descuento a cliente frecuente 
 ---dar descuneto a pediodos grandes razon x 
 -- generar pedido en administador a provvedor 
 --  listar productos que se acaban que se acaba y su razon : administaror class trabajar  el inventario csv 
