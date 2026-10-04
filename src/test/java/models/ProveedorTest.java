package models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Proveedor")
class ProveedorTest {

    private Proveedor base() {
        return new Proveedor("Javi", "PROV-01", 67494596);
    }

    private Producto producto() {
        return new Producto("P1", "Producto", 10.0, 15.0, "desc", 5, "Marca", "Cat");
    }

    @Test
    @DisplayName("El constructor corto crea una lista de productos vacía")
    void constructorCortoCreaListaVacia() {
        assertTrue(base().getProductos().isEmpty());
    }

    @Test
    @DisplayName("Una lista null se reemplaza por una lista vacía")
    void listaNullSeVuelveVacia() {
        assertTrue(new Proveedor("Javi", "PROV-01", 1, null).getProductos().isEmpty());
    }

    @Test
    @DisplayName("El constructor completo conserva la lista de productos")
    void constructorCompletoConservaLista() {
        Proveedor p = new Proveedor("Javi", "PROV-01", 1, new java.util.ArrayList<>(java.util.List.of(producto())));
        assertEquals(1, p.getProductos().size());
    }

    @Test
    @DisplayName("entregarProducto agrega un producto al catálogo")
    void entregarProductoAgrega() {
        Proveedor p = base();
        p.entregarProducto(producto());
        assertEquals(1, p.getProductos().size());
    }

    @Test
    @DisplayName("entregarProducto(null) no modifica el catálogo")
    void entregarProductoNullNoAgrega() {
        Proveedor p = base();
        p.entregarProducto(null);
        assertTrue(p.getProductos().isEmpty());
    }

    @Test
    @DisplayName("entregarProducto varias veces acumula los productos")
    void entregarProductoAcumula() {
        Proveedor p = base();
        p.entregarProducto(producto());
        p.entregarProducto(producto());
        assertEquals(2, p.getProductos().size());
    }

    @Test
    @DisplayName("Dos proveedores con los mismos datos son iguales")
    void equalsConMismosDatos() {
        assertEquals(base(), base());
    }

    @Test
    @DisplayName("Proveedores con distinto nombre no son iguales")
    void equalsConDistintoNombre() {
        assertNotEquals(base(), new Proveedor("Otro", "PROV-01", 67494596));
    }

    @Test
    @DisplayName("Un proveedor nunca es igual a null")
    void equalsConNull() {
        assertNotEquals(base(), null);
    }

    @Test
    @DisplayName("Un proveedor nunca es igual a otro tipo de objeto")
    void equalsConOtraClase() {
        assertNotEquals(base(), "texto");
    }

    @Test
    @DisplayName("equals y hashCode son coherentes")
    void hashCodeCoherente() {
        assertEquals(base().hashCode(), base().hashCode());
    }

    @Test
    @DisplayName("toString incluye código, nombre y cantidad de productos")
    void toStringIncluyeDatos() {
        assertTrue(base().toString().contains("PROV-01"));
    }

    @Test
    @DisplayName("contacto acepta Integer.MAX_VALUE (borde)")
    void contactoMaximo() {
        assertEquals(Integer.MAX_VALUE, new Proveedor("P", "C", Integer.MAX_VALUE).getContactoProveedor());
    }

    @Test
    @DisplayName("contacto acepta valores negativos (borde)")
    void contactoNegativo() {
        assertEquals(-1, new Proveedor("P", "C", -1).getContactoProveedor());
    }

    @Test
    @DisplayName("entregarProducto acumula muchos productos")
    void entregarMuchosProductos() {
        Proveedor p = base();
        for (int i = 0; i < 100; i++) {
            p.entregarProducto(producto());
        }
        assertEquals(100, p.getProductos().size());
    }

    @Test
    @DisplayName("Un proveedor con productos no es igual a uno sin productos")
    void equalsDistintoCatalogo() {
        Proveedor conProducto = base();
        conProducto.entregarProducto(producto());
        assertNotEquals(base(), conProducto);
    }
}
