// Autor: Rodriguez Alvarado Andersson Eduardo
package util;

import javax.servlet.http.HttpServletRequest;

public final class Web {
    private Web() { }
    public static String texto(HttpServletRequest request, String campo, int maximo) {
        String valor = request.getParameter(campo);
        valor = valor == null ? "" : valor.trim();
        if (valor.length() > maximo) throw new IllegalArgumentException("El campo " + campo + " supera " + maximo + " caracteres.");
        return valor;
    }
    public static String requerido(HttpServletRequest request, String campo, int maximo) {
        String valor = texto(request, campo, maximo);
        if (valor.isEmpty()) throw new IllegalArgumentException("Completa el campo " + campo + ".");
        return valor;
    }
    public static int id(HttpServletRequest request, String campo) {
        try {
            int valor = Integer.parseInt(request.getParameter(campo));
            if (valor <= 0) throw new NumberFormatException();
            return valor;
        } catch (NumberFormatException e) { throw new IllegalArgumentException("Selecciona un registro válido."); }
    }
    public static String opcion(String valor, String... opciones) {
        for (String opcion : opciones) if (opcion.equals(valor)) return valor;
        throw new IllegalArgumentException("Selecciona una opción válida.");
    }
    public static void correo(String valor) {
        if (valor.chars().anyMatch(Character::isWhitespace) || !valor.matches("[^@ ]+@[^@ ]+[.][^@ ]+"))
            throw new IllegalArgumentException("Escribe un correo válido.");
    }
    public static String clave(HttpServletRequest request) {
        String valor = request.getParameter("clave");
        if (valor == null || valor.length() < 4 || valor.length() > 128)
            throw new IllegalArgumentException("La clave debe tener de 4 a 128 caracteres.");
        return valor;
    }
    public static java.math.BigDecimal decimal(HttpServletRequest request, String campo, int escala) {
        try {
            java.math.BigDecimal valor = new java.math.BigDecimal(requerido(request,campo,20));
            valor = valor.setScale(escala, java.math.RoundingMode.UNNECESSARY);
            if (valor.signum() < 0 || valor.compareTo(new java.math.BigDecimal("99999999")) > 0) throw new NumberFormatException();
            return valor;
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException("El campo " + campo + " requiere un número no negativo de hasta " + escala + " decimales.");
        }
    }
    public static void mensaje(HttpServletRequest request, String mensaje) {
        request.getSession().setAttribute("mensaje", mensaje);
    }
}
