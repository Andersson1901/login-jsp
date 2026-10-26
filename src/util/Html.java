// Autor: Rodriguez Alvarado Andersson Eduardo
package util;

public final class Html {
    private Html() { }

    public static String escapar(Object valor) {
        if (valor == null) return "";
        return valor.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
