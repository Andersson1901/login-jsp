// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import conexion.ConexionBD;
import modelo.Consulta;
import modelo.Pagina;
import java.sql.*;
import java.util.*;

// Arma y ejecuta la consulta de una tabla paginada. Las columnas de búsqueda y orden salen solo de la lista blanca
// que cada repositorio entrega; los valores de la persona viajan siempre como parámetros.
public final class Paginador {
    public interface Mapeador<T> { T mapear(ResultSet r) throws SQLException; }

    // Sentencias y valores ya armados, separados de la ejecución para poder probarlos sin base de datos.
    public static final class Plan {
        private final String conteo, pagina;
        private final List<Object> valores;
        Plan(String conteo, String pagina, List<Object> valores) { this.conteo = conteo; this.pagina = pagina; this.valores = valores; }
        public String getConteo() { return conteo; }
        public String getPagina() { return pagina; }
        public List<Object> getValores() { return valores; }
    }

    private static final String ESCAPE = " LIKE ? ESCAPE '!'";
    private final String base, ordenInicial, desempate;
    private final Map<String, String> columnas;
    private String[] general = new String[0];
    private String fijo = "";
    private Object[] valoresFijos = new Object[0];

    // base: SELECT ... FROM ... JOIN ... sin WHERE ni ORDER BY. ordenInicial: orden cuando no se elige columna.
    public Paginador(String base, Map<String, String> columnas, String ordenInicial, String desempate) {
        this.base = base; this.columnas = columnas; this.ordenInicial = ordenInicial; this.desempate = desempate;
    }
    // Expresiones donde busca el texto libre (sin "Columna:").
    public Paginador general(String... expresiones) { this.general = expresiones; return this; }
    // Condición que siempre se aplica (por ejemplo el usuario dueño de los avisos o el rango de fechas).
    public Paginador fijo(String condicion, Object... valores) { this.fijo = condicion; this.valoresFijos = valores; return this; }

    // Pares palabra clave / expresión SQL, en el orden en que se muestran en el aviso de columna desconocida.
    public static Map<String, String> columnas(String... pares) {
        Map<String, String> mapa = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) mapa.put(pares[i], pares[i + 1]);
        return mapa;
    }
    // Los comodines que escribe la persona se toman como texto: ! es el carácter de escape.
    public static String comodin(String valor) { return "%" + valor.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%"; }

    public Plan plan(Consulta q) {
        List<String> condiciones = new ArrayList<>();
        List<Object> valores = new ArrayList<>();
        if (!fijo.isEmpty()) { condiciones.add(fijo); valores.addAll(Arrays.asList(valoresFijos)); }
        if (!q.getColumna().isEmpty()) {
            String expresion = columnas.get(q.getColumna());
            if (expresion == null) throw new Consulta.ColumnaDesconocida("Columna no reconocida. Usa: " + String.join(", ", columnas.keySet()) + ".");
            if (!q.getValor().isEmpty()) { condiciones.add(expresion + ESCAPE); valores.add(comodin(q.getValor())); }
        } else if (!q.getTexto().isEmpty() && general.length > 0) {
            StringJoiner alternativas = new StringJoiner(" OR ", "(", ")");
            for (String expresion : general) { alternativas.add(expresion + ESCAPE); valores.add(comodin(q.getTexto())); }
            condiciones.add(alternativas.toString());
        }
        String donde = condiciones.isEmpty() ? "" : " WHERE " + String.join(" AND ", condiciones);
        String direccion = q.isDescendente() ? "DESC" : "ASC", elegida = columnas.get(q.getOrden());
        String orden = elegida == null ? ordenInicial : elegida + " " + direccion + "," + desempate + " " + direccion;
        // El conteo se acota al límite: nunca recorre más filas que las que la persona pidió traer.
        return new Plan("SELECT COUNT(*) FROM (SELECT 1" + base.substring(base.lastIndexOf(" FROM ")) + donde + " LIMIT ?) t",
                base + donde + " ORDER BY " + orden + " LIMIT ? OFFSET ?", valores);
    }

    public <T> Pagina<T> buscar(Consulta q, Mapeador<T> mapeador) throws SQLException {
        Plan plan = plan(q);
        try (Connection c = ConexionBD.getInstancia().getConexion()) {
            int total;
            try (PreparedStatement p = c.prepareStatement(plan.getConteo())) {
                int i = enlazar(p, plan.getValores()); p.setInt(i, q.getLimite());
                try (ResultSet r = p.executeQuery()) { r.next(); total = r.getInt(1); }
            }
            int paginas = Math.max(1, (total + Consulta.TAMANO - 1) / Consulta.TAMANO), pagina = Math.min(q.getPagina(), paginas);
            List<T> filas = new ArrayList<>();
            try (PreparedStatement p = c.prepareStatement(plan.getPagina())) {
                int i = enlazar(p, plan.getValores()); p.setInt(i, Consulta.TAMANO); p.setInt(i + 1, (pagina - 1) * Consulta.TAMANO);
                try (ResultSet r = p.executeQuery()) { while (r.next()) filas.add(mapeador.mapear(r)); }
            }
            return new Pagina<>(filas, pagina, paginas, total, q.getLimite());
        }
    }

    private static int enlazar(PreparedStatement p, List<Object> valores) throws SQLException {
        int i = 1;
        for (Object valor : valores) p.setObject(i++, valor);
        return i;
    }
}
