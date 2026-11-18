package services;

public final class ValidadorDatos {
    private ValidadorDatos() {}

    public static boolean textoValido(String texto) {
        if (texto == null) return false;
        String valor = texto.trim();
        return !valor.isEmpty()
                && valor.matches("[\\p{L}\\p{N}][\\p{L}\\p{N} '\\-.]*")
                && valor.matches(".*\\p{L}.*");
    }

    public static boolean identificadorValido(String identificador) {
        if (identificador == null) return false;
        String valor = identificador.trim();
        return valor.matches("[A-Za-z0-9-]{2,}");
    }

    public static boolean ciValida(String ci) {
        if (ci == null) return false;
        String valor = ci.trim();
        return valor.matches("[A-Za-z0-9-]{2,}");
    }

    public static boolean telefonoValido(String telefono) {
        if (telefono == null) return false;
        String valor = telefono.trim();
        if (valor.startsWith("+")) valor = valor.substring(1).trim();
        String digitos = valor.replaceAll("[^0-9]", "");
        return digitos.length() >= 7 && digitos.length() <= 15;
    }

    public static boolean telefonoValido(int telefono) {
        return telefono >= 1_000_000 && telefono <= 9_999_999_999L;
    }

    public static boolean contrasenaValida(String contrasena) {
        return contrasena != null && contrasena.length() >= 6;
    }
}
