// Autor: Rodriguez Alvarado Andersson Eduardo
package modelo;

// Lo que se pide a una tabla: búsqueda, orden, página y tope de filas traídas de la base.
public class Consulta {
    public static final int TAMANO = 10, LIMITE_INICIAL = 100;
    public static final int[] LIMITES = {50, 100, 500, 1000};
    // "Columna: valor" nombró una columna que la pantalla no ofrece.
    public static class ColumnaDesconocida extends IllegalArgumentException {
        public ColumnaDesconocida(String mensaje) { super(mensaje); }
    }
    private String busqueda = "", texto = "", columna = "", valor = "", orden = "";
    private boolean descendente = true;
    private int pagina = 1, limite = LIMITE_INICIAL;
    public String getBusqueda() { return busqueda; }
    public void setBusqueda(String busqueda) { this.busqueda = busqueda; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public String getColumna() { return columna; }
    public void setColumna(String columna) { this.columna = columna; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public String getOrden() { return orden; }
    public void setOrden(String orden) { this.orden = orden; }
    public boolean isDescendente() { return descendente; }
    public void setDescendente(boolean descendente) { this.descendente = descendente; }
    public int getPagina() { return pagina; }
    public void setPagina(int pagina) { this.pagina = Math.max(1, pagina); }
    public int getLimite() { return limite; }
    public void setLimite(int limite) { this.limite = limite; }
}
