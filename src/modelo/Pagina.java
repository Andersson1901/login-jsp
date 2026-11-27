// Autor: Rodriguez Alvarado Andersson Eduardo
package modelo;
import java.util.List;

// Una página de resultados. El total ya está acotado por el límite de filas traídas.
public class Pagina<T> {
    private final List<T> filas;
    private final int pagina, paginas, total, limite;
    public Pagina(List<T> filas, int pagina, int paginas, int total, int limite) {
        this.filas = filas; this.pagina = pagina; this.paginas = paginas; this.total = total; this.limite = limite;
    }
    public List<T> getFilas() { return filas; }
    public int getPagina() { return pagina; }
    public int getPaginas() { return paginas; }
    public int getTotal() { return total; }
    public int getLimite() { return limite; }
    public int getDesde() { return filas.isEmpty() ? 0 : (pagina - 1) * Consulta.TAMANO + 1; }
    public int getHasta() { return filas.isEmpty() ? 0 : getDesde() + filas.size() - 1; }
}
