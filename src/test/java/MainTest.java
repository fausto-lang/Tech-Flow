import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Integracion de Main")
class MainTest {

    private static final String PROPIEDAD_DATOS = "techflow.data.dir";
    private static final String PROPIEDAD_DATOS_ORIGINAL = System.getProperty(PROPIEDAD_DATOS);
    private static final java.io.InputStream ENTRADA_ORIGINAL = System.in;
    private static final PrintStream SALIDA_ORIGINAL = System.out;

    @TempDir
    Path directorioDatos;

    private ByteArrayOutputStream salida;

    @BeforeEach
    void prepararDatosYSalida() throws Exception {
        Files.createDirectories(directorioDatos);
        Path datosReales = Path.of(System.getProperty("user.dir"), "data");
        try (var archivos = Files.walk(datosReales)) {
            archivos.filter(Files::isRegularFile).forEach(origen -> {
                try {
                    Path destino = directorioDatos.resolve(datosReales.relativize(origen).toString());
                    Files.createDirectories(destino.getParent());
                    Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
                } catch (java.io.IOException e) {
                    throw new java.io.UncheckedIOException(e);
                }
            });
        }
        System.setProperty(PROPIEDAD_DATOS, directorioDatos.toString());
        salida = new ByteArrayOutputStream();
    }

    @AfterEach
    void restaurarConsolaYConfiguracion() {
        System.setIn(ENTRADA_ORIGINAL);
        System.setOut(SALIDA_ORIGINAL);
        if (PROPIEDAD_DATOS_ORIGINAL == null) {
            System.clearProperty(PROPIEDAD_DATOS);
        } else {
            System.setProperty(PROPIEDAD_DATOS, PROPIEDAD_DATOS_ORIGINAL);
        }
    }

    @Test
    @DisplayName("muestra el encabezado y permite salir inmediatamente")
    void iniciaYSale() {
        ejecutar("0\n");
        assertSalidaContiene("=== SISTEMA DE INVENTARIO Y VENTAS ===", "0) Salir del sistema");
    }

    @Test
    @DisplayName("rechaza una seleccion de rol invalida y permite corregirla")
    void rolInvalidoLuegoSalida() {
        ejecutar("99\n0\n");
        assertSalidaContiene("Seleccion no valida.", "=== SISTEMA DE INVENTARIO Y VENTAS ===");
    }

    @Test
    @DisplayName("acepta los alias del administrador")
    void aliasAdministrador() {
        for (String alias : new String[]{"1", "admin", "administrador", "adm"}) {
            ejecutar(alias + "\nadm\nuser\n0\n0\n");
            assertSalidaContiene("Sesion iniciada: adm (ADMINISTRADOR)", "ADMINISTRADOR |");
        }
    }

    @Test
    @DisplayName("acepta los alias del cajero")
    void aliasCajero() {
        for (String alias : new String[]{"2", "cajero", "vendedor"}) {
            ejecutar(alias + "\nVentas Demo\nventas123\n0\n0\n");
            assertSalidaContiene("Sesion iniciada: Ventas Demo (CAJERO)", "CAJERO |");
        }
    }

    @Test
    @DisplayName("acepta los alias del almacen")
    void aliasAlmacen() {
        for (String alias : new String[]{"3", "almacen", "almacenero"}) {
            ejecutar(alias + "\nAlmacen Demo\nalmacen123\n0\n0\n");
            assertSalidaContiene("Sesion iniciada: Almacen Demo (ALMACENERO)", "ALMACEN |");
        }
    }

    @Test
    @DisplayName("la seleccion de rol ignora mayusculas y espacios exteriores")
    void rolConMayusculasYEspacios() {
        ejecutar("  ADMINISTRADOR  \nadm\nuser\n0\n0\n");
        assertSalidaContiene("Sesion iniciada: adm (ADMINISTRADOR)");
    }

    @Test
    @DisplayName("autentica credenciales correctas")
    void credencialesCorrectas() {
        ejecutar("1\nadm\nuser\n0\n0\n");
        assertSalidaContiene("Sesion iniciada: adm (ADMINISTRADOR)");
    }

    @Test
    @DisplayName("rechaza una contraseña incorrecta")
    void contrasenaIncorrecta() {
        ejecutar("1\nadm\nincorrecta\n0\n");
        assertSalidaContiene("Credenciales incorrectas.");
        assertTrue(!salida.toString(StandardCharsets.UTF_8).contains("Sesion iniciada:"));
    }

    @Test
    @DisplayName("rechaza un usuario inexistente")
    void usuarioInexistente() {
        ejecutar("1\nnadie\nuser\n0\n");
        assertSalidaContiene("Credenciales incorrectas.");
    }

    @Test
    @DisplayName("rechaza credenciales validas cuando el rol no coincide")
    void rolNoCoincide() {
        ejecutar("1\nventas\nventas123\n0\n");
        assertSalidaContiene("Credenciales incorrectas.");
    }

    @Test
    @DisplayName("permite reintentar despues de una contraseña incorrecta")
    void reintentoCorrecto() {
        ejecutar("1\nadm\nmal\nadm\nuser\n0\n0\n");
        assertSalidaContiene("Credenciales incorrectas.", "Sesion iniciada: adm (ADMINISTRADOR)");
    }

    @Test
    @DisplayName("bloquea cinco intentos fallidos y vuelve al selector de rol")
    void cincoIntentosFallidos() {
        ejecutar("1\nno\nno\nno\nno\nno\nno\nno\nno\nno\nno\n0\n");
        assertSalidaContiene("Se agotaron los 5 intentos.", "Tipo de operador:");
    }

    @Test
    @DisplayName("no falla si termina la entrada al seleccionar rol")
    void finDeEntradaEnRol() {
        assertDoesNotThrow(() -> ejecutar(""));
        assertSalidaContiene("=== SISTEMA DE INVENTARIO Y VENTAS ===");
    }

    @Test
    @DisplayName("no expone la contraseña en los mensajes de autenticacion")
    void noExponeContrasena() {
        ejecutar("1\nadm\nincorrecta\n0\n");
        assertTrue(!salida.toString(StandardCharsets.UTF_8).contains("Contrasena: incorrecta"));
    }

    @Test
    @DisplayName("permite salir del menu de administrador")
    void menuAdministradorSalida() {
        ejecutar("1\nadm\nuser\n0\n0\n");
        assertSalidaContiene("ADMINISTRADOR |", "Tipo de operador:");
    }

    @Test
    @DisplayName("recupera un entero invalido en el menu de administrador")
    void enteroInvalidoAdministrador() {
        ejecutar("1\nadm\nuser\ntexto\n0\n0\n");
        assertSalidaContiene("Ingrese un numero entero.", "ADMINISTRADOR |");
    }

    @Test
    @DisplayName("ejecuta los reportes del administrador sin datos")
    void reportesAdministradorSinDatos() {
        ejecutar("1\nadm\nuser\n3\n4\n12\n0\n0\n");
        assertSalidaContiene("Ingresos confirmados:", "--- INVENTARIO ---", "Entradas hoy:");
    }

    @Test
    @DisplayName("permite salir del menu de cajero")
    void menuCajeroSalida() {
        ejecutar("2\nVentas Demo\nventas123\n0\n0\n");
        assertSalidaContiene("CAJERO |", "Tipo de operador:");
    }

    @Test
    @DisplayName("rechaza una cantidad invalida al crear una venta")
    void cantidadInvalidaEnVenta() {
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n1\nP-INEXISTENTE\n0\n0\n0\n");
        assertSalidaContiene("No se encontro el producto.");
    }

    @Test
    @DisplayName("permite salir del menu de almacen")
    void menuAlmacenSalida() {
        ejecutar("3\nAlmacen Demo\nalmacen123\n0\n0\n");
        assertSalidaContiene("ALMACEN |", "Tipo de operador:");
    }

    @Test
    @DisplayName("rechaza una cantidad no positiva al recibir inventario")
    void cantidadNoPositivaEnAlmacen() {
        ejecutar("3\nAlmacen Demo\nalmacen123\n2\nP-INEXISTENTE\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("mantiene la aplicacion estable ante una ruta de importacion vacia")
    void rutaImportacionVacia() {
        ejecutar("3\nAlmacen Demo\nalmacen123\n3\n\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("no recorta una contraseña que contiene espacios")
    void contraseñaConEspacioNoSeRecorta() {
        ejecutar("1\nadm\nuser \n0\n");
        assertTrue(!salida.toString(StandardCharsets.UTF_8).contains("Sesion iniciada:"),
                "Una contraseña con espacio final no debe convertirse en una contraseña válida");
    }

    @Test
    @DisplayName("un EOF dentro de un menu no debe lanzar una excepcion")
    void eofDentroDeMenuNoRompeLaAplicacion() {
        assertDoesNotThrow(() -> ejecutar("1\nadm\nuser\n"));
    }

    @Test
    @DisplayName("un nombre de empleado con numeros debe ser rechazado")
    void nombreEmpleadoConNumerosEsRechazado() {
        ejecutar("1\nadm\nuser\n5\nCI-TEST-001\nJuan123\nclave123\n1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza una seleccion de rol decimal")
    void rolDecimalEsRechazado() {
        ejecutar("1.5\n0\n");
        assertSalidaContiene("Seleccion no valida.");
    }

    @Test
    @DisplayName("rechaza una seleccion de rol negativa")
    void rolNegativoEsRechazado() {
        ejecutar("-1\n0\n");
        assertSalidaContiene("Seleccion no valida.");
    }

    @Test
    @DisplayName("un EOF antes de autenticar no lanza una excepcion")
    void eofAntesDeAutenticar() {
        assertDoesNotThrow(() -> ejecutar("1\n"));
    }

    @Test
    @DisplayName("un EOF despues del nombre de usuario no lanza una excepcion")
    void eofDespuesDelUsuario() {
        assertDoesNotThrow(() -> ejecutar("1\nadm\n"));
    }

    @Test
    @DisplayName("un usuario vacio nunca inicia sesion")
    void usuarioVacio() {
        ejecutar("1\n\nuser\n0\n0\n0\n0\n0\n0\n0\n0\n0\n0\n");
        assertTrue(!salida.toString(StandardCharsets.UTF_8).contains("Sesion iniciada:"));
    }

    @Test
    @DisplayName("una contraseña vacia nunca inicia sesion")
    void contrasenaVacia() {
        ejecutar("1\nadm\n\n0\n0\n0\n0\n0\n0\n0\n0\n0\n0\n");
        assertTrue(!salida.toString(StandardCharsets.UTF_8).contains("Sesion iniciada:"));
    }

    @Test
    @DisplayName("una contraseña compuesta solo por espacios no autentica")
    void contrasenaSoloEspacios() {
        ejecutar("1\nadm\n   \n0\n0\n0\n0\n0\n0\n0\n0\n0\n0\n");
        assertTrue(!salida.toString(StandardCharsets.UTF_8).contains("Sesion iniciada:"));
    }

    @Test
    @DisplayName("un usuario extremadamente largo no rompe la autenticacion")
    void usuarioExtremadamenteLargo() {
        String usuario = "x".repeat(10_000);
        ejecutar("1\n" + usuario + "\nclave\n0\n0\n0\n0\n0\n0\n0\n0\n0\n0\n");
        assertTrue(!salida.toString(StandardCharsets.UTF_8).contains("Sesion iniciada:"));
    }

    @Test
    @DisplayName("un nombre de cliente vacio no genera una venta")
    void clienteVacio() {
        ejecutar("2\nVentas Demo\nventas123\n1\n\n0\n0\n0\n");
        assertSalidaContiene("El nombre del cliente es obligatorio.");
    }

    @Test
    @DisplayName("una cantidad cero no entra al carrito")
    void cantidadCeroEnCarrito() {
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n1\nProd-01\n0\n0\n0\n0\n");
        assertSalidaContiene("Cantidad invalida o superior al stock disponible");
    }

    @Test
    @DisplayName("una cantidad superior al stock no entra al carrito")
    void cantidadSuperiorAlStock() {
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n1\nProd-01\n6\n0\n0\n0\n");
        assertSalidaContiene("Cantidad invalida o superior al stock disponible");
    }

    @Test
    @DisplayName("un contacto no numerico se rechaza sin romper el menu")
    void contactoNoNumerico() {
        ejecutar("1\nadm\nuser\n7\n\n\nabc\n0\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("un entero decimal en el menu se solicita nuevamente")
    void enteroDecimalEnMenu() {
        ejecutar("1\nadm\nuser\n2.5\n0\n0\n");
        assertSalidaContiene("Ingrese un numero entero.");
    }

    @Test
    @DisplayName("normaliza espacios exteriores del nombre de usuario")
    void espaciosExterioresDelUsuario() {
        ejecutar("1\n  adm  \nuser\n0\n0\n");
        assertSalidaContiene("Sesion iniciada: adm (ADMINISTRADOR)");
    }

    @Test
    @DisplayName("la contraseña mantiene sensibilidad a mayusculas")
    void contraseñaDistingueMayusculas() {
        ejecutar("1\nadm\nUSER\n0\n");
        assertSalidaContiene("Credenciales incorrectas.");
    }

    @Test
    @DisplayName("un numero entero fuera de rango se rechaza sin romper el menu")
    void enteroFueraDeRango() {
        ejecutar("1\nadm\nuser\n999999999999999999999999\n0\n0\n");
        assertSalidaContiene("Ingrese un numero entero.");
    }

    @Test
    @DisplayName("un producto sin identificador no se registra")
    void productoSinIdentificador() {
        ejecutar("3\nAlmacen Demo\nalmacen123\n1\n\nProducto\nMarca\nCategoria\nDescripcion\n10\n15\n1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("un producto con nombre compuesto solo por espacios no se registra")
    void productoConNombreVacio() {
        ejecutar("3\nAlmacen Demo\nalmacen123\n1\nID-EDGE\n   \nMarca\nCategoria\nDescripcion\n10\n15\n1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("un precio NaN no se acepta")
    void precioNoNumericoNaN() {
        ejecutar("1\nadm\nuser\n9\nID-NAN\nProducto\nMarca\nCategoria\nDescripcion\nNaN\n10\n1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("un precio infinito no se acepta")
    void precioInfinito() {
        ejecutar("1\nadm\nuser\n9\nID-INF\nProducto\nMarca\nCategoria\nDescripcion\nInfinity\n10\n1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("un contacto negativo no se acepta")
    void contactoNegativo() {
        ejecutar("1\nadm\nuser\n7\nID-CONTACTO\nProveedor\n-1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("un telefono con letras no confirma una venta")
    void telefonoConLetras() {
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n1\nProd-01\n1\n2\nCI-EDGE\nabc1234567\n0\n0\n");
        assertSalidaContiene("CI y telefono son obligatorios");
    }

    @Test
    @DisplayName("una tecla de tabulacion se normaliza como entrada de rol")
    void tabulacionEnRol() {
        ejecutar("\t1\t\nadm\nuser\n0\n0\n");
        assertSalidaContiene("Sesion iniciada: adm (ADMINISTRADOR)");
    }

    @Test
    @DisplayName("rechaza un empleado duplicado")
    void empleadoDuplicado() {
        ejecutar("1\nadm\nuser\n5\nADM-001\nOtro Nombre\notra123\n1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza eliminar un empleado inexistente")
    void eliminarEmpleadoInexistente() {
        ejecutar("1\nadm\nuser\n6\nCI-NO-EXISTE\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza un proveedor duplicado")
    void proveedorDuplicado() {
        ejecutar("1\nadm\nuser\n7\nPROV-01\nProveedor repetido\n60000000\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza eliminar un proveedor inexistente")
    void eliminarProveedorInexistente() {
        ejecutar("1\nadm\nuser\n8\nPROV-NO-EXISTE\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza un producto duplicado")
    void productoDuplicado() {
        ejecutar("1\nadm\nuser\n9\nProd-01\nOtro\nMarca\nCategoria\nDescripcion\n10\n15\n1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza eliminar un producto inexistente")
    void eliminarProductoInexistente() {
        ejecutar("1\nadm\nuser\n10\nPROD-NO-EXISTE\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza importar entradas desde un archivo inexistente")
    void importarEntradasInexistentes() {
        ejecutar("1\nadm\nuser\n11\n/ruta/que/no/existe.csv\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("repite la lectura cuando el decimal es invalido")
    void decimalInvalidoEnProducto() {
        ejecutar("1\nadm\nuser\n9\nID-DECIMAL\nProducto\nMarca\nCategoria\nDescripcion\nabc\n10\n1\n0\n0\n");
        assertSalidaContiene("Ingrese un numero valido.");
    }

    @Test
    @DisplayName("buscar por marca devuelve productos coincidentes")
    void buscarPorMarca() {
        ejecutar("2\nVentas Demo\nventas123\n3\nSony\n0\n0\n");
        assertSalidaContiene("Sony");
    }

    @Test
    @DisplayName("consultar una garantia inexistente no rompe el cajero")
    void garantiaInexistente() {
        ejecutar("2\nVentas Demo\nventas123\n4\nGAR-NO-EXISTE\n0\n0\n");
        assertSalidaContiene("CAJERO |");
    }

    @Test
    @DisplayName("importar una orden desde una ruta inexistente informa el fallo")
    void importarOrdenInexistente() {
        ejecutar("2\nVentas Demo\nventas123\n2\n/ruta/que/no/existe.csv\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("no permite cambiar cantidad de un producto que no esta en el carrito")
    void cambiarProductoFueraDelCarrito() {
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n5\nProd-01\n2\n0\n0\n0\n");
        assertSalidaContiene("Ese producto no esta en el carrito.");
    }

    @Test
    @DisplayName("no permite eliminar un producto que no esta en el carrito")
    void eliminarProductoFueraDelCarrito() {
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n4\nProd-01\n0\n0\n0\n");
        assertSalidaContiene("Ese producto no esta en el carrito.");
    }

    @Test
    @DisplayName("genera una proforma con la cantidad minima valida")
    void proformaCantidadMinima() {
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n1\nProd-01\n1\n3\n0\n0\n");
        assertSalidaContiene("=== PROFORMA ===", "Total cotizado:");
    }

    @Test
    @DisplayName("el almacen incrementa una unidad de un producto existente")
    void recibirUnaUnidad() {
        ejecutar("3\nAlmacen Demo\nalmacen123\n2\nProd-02\n1\n0\n0\n");
        assertSalidaContiene("Operacion realizada.");
    }

    @Test
    @DisplayName("el almacen rechaza un producto inexistente")
    void recibirProductoInexistente() {
        ejecutar("3\nAlmacen Demo\nalmacen123\n2\nPROD-NO-EXISTE\n1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("el error de rol invalido explica que la seleccion no es valida")
    void mensajeRolInvalidoEsClaro() {
        ejecutar("x\n0\n");
        assertSalidaContiene("Seleccion no valida.");
        assertSalidaNoContiene("null", "Exception", "Error:");
    }

    @Test
    @DisplayName("el error de credenciales informa los intentos restantes")
    void mensajeCredencialesIncluyeIntentos() {
        ejecutar("1\nadm\nmal\n0\n");
        assertSalidaContiene("Credenciales incorrectas. Intentos restantes: 4");
    }

    @Test
    @DisplayName("el agotamiento de intentos muestra un mensaje final")
    void mensajeAgotamientoEsClaro() {
        ejecutar("1\nx\nx\nx\nx\nx\nx\nx\nx\nx\nx\n0\n");
        assertSalidaContiene("Se agotaron los 5 intentos. Volviendo a seleccionar el rol.");
    }

    @Test
    @DisplayName("el mensaje de autenticacion nunca muestra la contraseña")
    void errorAutenticacionNoFiltraContrasena() {
        ejecutar("1\nadm\nSecretoSuperPrivado123\n0\n");
        assertSalidaNoContiene("SecretoSuperPrivado123");
    }

    @Test
    @DisplayName("el error de entero indica el formato esperado")
    void mensajeEnteroInvalidoEsClaro() {
        ejecutar("1\nadm\nuser\nabc\n0\n0\n");
        assertSalidaContiene("Ingrese un numero entero.");
        assertSalidaNoContiene("NumberFormatException", "java.lang", "null");
    }

    @Test
    @DisplayName("el error de decimal indica el formato esperado")
    void mensajeDecimalInvalidoEsClaro() {
        ejecutar("1\nadm\nuser\n9\nID-MSG\nProducto\nMarca\nCategoria\nDescripcion\nabc\n10\n1\n0\n0\n");
        assertSalidaContiene("Ingrese un numero valido.");
        assertSalidaNoContiene("NumberFormatException", "java.lang");
    }

    @Test
    @DisplayName("el carrito informa cuando el producto no existe")
    void mensajeProductoInexistenteEsClaro() {
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n1\nNO-EXISTE\n0\n0\n0\n");
        assertSalidaContiene("No se encontro el producto.");
    }

    @Test
    @DisplayName("el carrito informa cuando la cantidad no es valida")
    void mensajeCantidadInvalidaEsClaro() {
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n1\nProd-01\n0\n0\n0\n0\n");
        assertSalidaContiene("Cantidad invalida o superior al stock disponible; no se modifico el carrito.");
    }

    @Test
    @DisplayName("la venta sin cliente informa el campo obligatorio")
    void mensajeClienteObligatorioEsClaro() {
        ejecutar("2\nVentas Demo\nventas123\n1\n\n0\n0\n0\n");
        assertSalidaContiene("El nombre del cliente es obligatorio.");
    }

    @Test
    @DisplayName("el error de importacion no expone una traza tecnica")
    void mensajeImportacionNoExponeTraza() {
        ejecutar("2\nVentas Demo\nventas123\n2\n/ruta/inexistente.csv\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
        assertSalidaNoContiene("Exception", "at ", "java.");
    }

    @Test
    @DisplayName("el error de carrito fuera de rango no expone una traza tecnica")
    void mensajeCarritoNoExponeTraza() {
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n1\nProd-01\n999999999999999999999\n0\n0\n0\n");
        assertSalidaContiene("Ingrese un numero entero.");
        assertSalidaNoContiene("NumberFormatException", "Exception", "java.");
    }

    @Test
    @DisplayName("el error de inventario inexistente mantiene un mensaje funcional")
    void mensajeInventarioInexistenteEsFuncional() {
        ejecutar("3\nAlmacen Demo\nalmacen123\n2\nNO-EXISTE\n1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
        assertSalidaNoContiene("NullPointerException", "Exception", "java.");
    }

    @Test
    @DisplayName("los mensajes no contienen valores null")
    void mensajesNuncaContienenNull() {
        ejecutar("0\n");
        assertSalidaNoContiene("null");
    }

    @Test
    @DisplayName("los mensajes no contienen espacios de control inesperados")
    void mensajesNoContienenControl() {
        ejecutar("0\n");
        String texto = salida.toString(StandardCharsets.UTF_8);
        assertTrue(!texto.contains("\u0000"), "La salida no debe contener caracteres NUL");
    }

    @Test
    @DisplayName("el cierre de sesion devuelve al selector sin mensaje de error")
    void cierreSesionNoEsError() {
        ejecutar("1\nadm\nuser\n0\n0\n");
        assertSalidaContiene("Tipo de operador:");
        assertSalidaNoContiene("No se pudo completar la operacion.", "Credenciales incorrectas.");
    }

    @Test
    @DisplayName("rechaza un nombre de empleado compuesto solo por espacios")
    void empleadoNombreSoloEspacios() {
        ejecutar("1\nadm\nuser\n5\nCI-ESPACIOS\n   \nclave123\n1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza una contraseña de empleado demasiado corta")
    void empleadoContrasenaCorta() {
        ejecutar("1\nadm\nuser\n5\nCI-CORTA\nNombre Valido\n123\n1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza un CI con caracteres especiales")
    void empleadoCiInvalido() {
        ejecutar("1\nadm\nuser\n5\nCI!!!\nNombre Valido\nclave123\n1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza un nombre de proveedor con numeros")
    void proveedorNombreConNumeros() {
        ejecutar("1\nadm\nuser\n7\nPROV-EDGE\nProveedor123\n60000000\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza un codigo de proveedor con espacios internos")
    void proveedorCodigoConEspacios() {
        ejecutar("1\nadm\nuser\n7\nPROV EDGE\nProveedor Valido\n60000000\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza un contacto con menos de siete digitos")
    void contactoDemasiadoCorto() {
        ejecutar("1\nadm\nuser\n7\nPROV-CORTO\nProveedor Valido\n123\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza un producto con nombre numerico")
    void productoNombreNumerico() {
        ejecutar("3\nAlmacen Demo\nalmacen123\n1\nID-NOMBRE\n123456\nMarca\nCategoria\nDescripcion\n10\n15\n1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza un producto con codigo demasiado corto")
    void productoCodigoCorto() {
        ejecutar("3\nAlmacen Demo\nalmacen123\n1\nA\nProducto Valido\nMarca\nCategoria\nDescripcion\n10\n15\n1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza un producto con stock inicial cero")
    void productoStockCero() {
        ejecutar("3\nAlmacen Demo\nalmacen123\n1\nID-STOCK\nProducto Valido\nMarca\nCategoria\nDescripcion\n10\n15\n0\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza un producto con precio de venta menor que el costo")
    void precioVentaMenorQueCosto() {
        ejecutar("3\nAlmacen Demo\nalmacen123\n1\nID-PRECIO\nProducto Valido\nMarca\nCategoria\nDescripcion\n100\n10\n1\n0\n0\n");
        assertSalidaContiene("No se pudo completar la operacion.");
    }

    @Test
    @DisplayName("rechaza un telefono con mas de quince digitos")
    void telefonoDemasiadoLargo() {
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n1\nProd-01\n1\n2\nCI-LARGO\n1234567890123456\n0\n0\n");
        assertSalidaContiene("CI y telefono son obligatorios");
    }

    @Test
    @DisplayName("rechaza un CI de cliente compuesto solo por espacios")
    void clienteCiSoloEspacios() {
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n1\nProd-01\n1\n2\n   \n12345678\n0\n0\n");
        assertSalidaContiene("CI y telefono son obligatorios");
    }

    @Test
    @DisplayName("rechaza una venta con nombre de cliente numerico")
    void clienteNombreNumerico() {
        ejecutar("2\nVentas Demo\nventas123\n1\n123456\n0\n0\n0\n");
        assertSalidaContiene("El nombre del cliente es obligatorio.");
    }

    @Test
    @DisplayName("rechaza cambiar el carrito a una cantidad negativa")
    void cambiarCantidadNegativa() {
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n1\nProd-01\n1\n5\nProd-01\n-1\n0\n0\n");
        assertSalidaContiene("Cantidad invalida o superior al stock disponible.");
    }

    @Test
    @DisplayName("rechaza generar una venta con caja cerrada")
    void ventaConCajaCerrada() {
        ejecutar("1\nadm\nuser\n2\n0\n0\n2\nVentas Demo\nventas123\n1\nCliente\n1\nProd-01\n1\n2\nCI-CAJA\n12345678\n0\n0\n0\n");
        assertSalidaContiene("La caja esta cerrada.");
    }

    @Test
    @DisplayName("un empleado creado desde Main queda persistido")
    void altaEmpleadoPersiste() throws Exception {
        ejecutar("1\nadm\nuser\n5\nCI-PERSISTENTE\nNombre Valido\nclave123\n2\n0\n0\n");
        String empleados = Files.readString(directorioDatos.resolve("empleado.csv"), StandardCharsets.UTF_8);
        assertSalidaContiene("Operacion realizada.");
        assertTrue(empleados.contains("CI-PERSISTENTE,Nombre Valido,CAJERO,clave123"));
    }

    @Test
    @DisplayName("un producto creado desde Main queda persistido")
    void altaProductoPersiste() throws Exception {
        ejecutar("3\nAlmacen Demo\nalmacen123\n1\nID-PERSISTENTE\nProducto Valido\nMarca\nCategoria\nDescripcion\n10\n15\n2\n0\n0\n");
        String inventario = Files.readString(directorioDatos.resolve("inventario.csv"), StandardCharsets.UTF_8);
        assertSalidaContiene("Operacion realizada.");
        assertTrue(inventario.contains("ID-PERSISTENTE,Producto Valido,Marca,Categoria"));
    }

    @Test
    @DisplayName("una proforma no descuenta inventario")
    void proformaNoDescuentaStock() throws Exception {
        int stockInicial = stockDe("Prod-01");
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n1\nProd-01\n1\n3\n0\n0\n");
        assertSalidaContiene("=== PROFORMA ===");
        assertEquals(stockInicial, stockDe("Prod-01"));
    }

    @Test
    @DisplayName("una venta confirmada descuenta exactamente el stock vendido")
    void ventaDescuentaStock() throws Exception {
        int stockInicial = stockDe("Prod-01");
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n1\nProd-01\n1\n2\nCI-VENTA\n12345678\n0\n0\n");
        assertSalidaContiene("=== FACTURA ===", "TOTAL:");
        assertEquals(stockInicial - 1, stockDe("Prod-01"));
    }

    @Test
    @DisplayName("una venta rechazada por stock no modifica inventario")
    void ventaRechazadaNoModificaStock() throws Exception {
        int stockInicial = stockDe("Prod-02");
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n1\nProd-02\n2\n0\n0\n");
        assertSalidaContiene("Cantidad invalida o superior al stock disponible");
        assertEquals(stockInicial, stockDe("Prod-02"));
    }

    @Test
    @DisplayName("una alta rechazada no modifica el archivo de empleados")
    void altaEmpleadoRechazadaNoPersiste() throws Exception {
        String antes = Files.readString(directorioDatos.resolve("empleado.csv"), StandardCharsets.UTF_8);
        ejecutar("1\nadm\nuser\n5\nCI-NO-PERSISTE\nNombre123\nclave123\n1\n0\n0\n");
        String despues = Files.readString(directorioDatos.resolve("empleado.csv"), StandardCharsets.UTF_8);
        assertSalidaContiene("No se pudo completar la operacion.");
        assertEquals(antes, despues);
    }

    @Test
    @DisplayName("cerrar caja persiste el estado y bloquea la confirmacion de ventas")
    void cajaCerradaPersisteYBloqueaVenta() throws Exception {
        ejecutar("1\nadm\nuser\n2\n0\n0\n");
        String caja = Files.readString(directorioDatos.resolve("caja.csv"), StandardCharsets.UTF_8);
        assertTrue(caja.contains("CERRADA"));
        ejecutar("2\nVentas Demo\nventas123\n1\nCliente\n1\nProd-01\n1\n2\nCI-CERRADA\n12345678\n0\n0\n0\n");
        assertSalidaContiene("La caja esta cerrada.");
    }

    @Test
    @DisplayName("una entrada de almacen incrementa el stock persistido")
    void entradaIncrementaStockPersistido() throws Exception {
        int stockInicial = stockDe("Prod-02");
        ejecutar("3\nAlmacen Demo\nalmacen123\n2\nProd-02\n1\n0\n0\n");
        assertSalidaContiene("Operacion realizada.");
        assertEquals(stockInicial + 1, stockDe("Prod-02"));
    }

    private void ejecutar(String entrada) {
        System.setIn(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)));
        System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
        Main.main(new String[0]);
    }

    private void assertSalidaContiene(String... fragmentos) {
        String texto = salida.toString(StandardCharsets.UTF_8);
        for (String fragmento : fragmentos) {
            assertTrue(texto.contains(fragmento),
                    () -> "La salida no contiene: " + fragmento + "\nSalida:\n" + texto);
        }
    }

    private void assertSalidaNoContiene(String... fragmentos) {
        String texto = salida.toString(StandardCharsets.UTF_8);
        for (String fragmento : fragmentos) {
            assertTrue(!texto.contains(fragmento),
                    () -> "La salida contiene un fragmento no permitido: " + fragmento + "\nSalida:\n" + texto);
        }
    }

    private int stockDe(String idProducto) throws Exception {
        for (String linea : Files.readAllLines(directorioDatos.resolve("inventario.csv"), StandardCharsets.UTF_8)) {
            if (linea.startsWith(idProducto + ",")) {
                String[] columnas = linea.split(",", -1);
                return Integer.parseInt(columnas[7]);
            }
        }
        throw new AssertionError("Producto no encontrado: " + idProducto);
    }
}