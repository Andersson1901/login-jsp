// Autor: Rodriguez Alvarado Andersson Eduardo
package util;
import modelo.Consulta;
import modelo.Pagina;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

// Lee y valida los parámetros de una tabla paginada. Nada de lo leído llega a la consulta sin pasar por la lista blanca del repositorio.
public final class Paginacion {
    private Paginacion() { }
    public interface Busqueda<T> { Pagina<T> ejecutar(Consulta consulta) throws SQLException; }

    // Con prefijo se distinguen dos tablas de una misma pantalla (por ejemplo "v_" y "p_" en el reporte).
    public static Consulta consulta(HttpServletRequest request, String prefijo) {
        Consulta c = new Consulta();
        interpretar(c, Web.texto(request, prefijo + "q", 100));
        String orden = request.getParameter(prefijo + "orden");
        c.setOrden(orden == null || orden.length() > 20 ? "" : normalizar(orden));
        c.setDescendente(!"asc".equals(request.getParameter(prefijo + "dir")));
        c.setPagina(entero(request.getParameter(prefijo + "pagina"), 1));
        int limite = entero(request.getParameter(prefijo + "limite"), Consulta.LIMITE_INICIAL);
        c.setLimite(java.util.stream.IntStream.of(Consulta.LIMITES).anyMatch(l -> l == limite) ? limite : Consulta.LIMITE_INICIAL);
        return c;
    }

    // "Columna: valor" busca solo en esa columna; se usa la primera palabra del nombre. Sin dos puntos, búsqueda libre.
    static void interpretar(Consulta c, String q) {
        c.setBusqueda(q);
        int i = q.indexOf(':');
        String columna = i > 0 ? q.substring(0, i).trim().split("\\s+")[0] : "";
        if (columna.isEmpty()) c.setTexto(q);
        else { c.setColumna(normalizar(columna)); c.setValor(q.substring(i + 1).trim()); }
    }

    // Minúsculas y sin tildes: "Código", "CODIGO" y "codigo" son la misma columna.
    public static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
    }

    private static int entero(String valor, int porDefecto) {
        try { return valor == null ? porDefecto : Integer.parseInt(valor.trim()); }
        catch (NumberFormatException e) { return porDefecto; }
    }

    // Deja la página en la petición. Una columna desconocida no consulta: se muestra el aviso y una tabla vacía.
    public static <T> Pagina<T> listar(HttpServletRequest request, Consulta consulta, Busqueda<T> busqueda) throws SQLException {
        Pagina<T> pagina;
        try { pagina = busqueda.ejecutar(consulta); }
        catch (Consulta.ColumnaDesconocida e) {
            request.setAttribute("avisoBusqueda", e.getMessage());
            pagina = new Pagina<>(List.of(), 1, 1, 0, consulta.getLimite());
        }
        request.setAttribute("pagina", pagina); request.setAttribute("registros", pagina.getFilas());
        return pagina;
    }
}
