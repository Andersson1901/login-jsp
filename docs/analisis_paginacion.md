<!-- Autor: Rodriguez Alvarado Andersson Eduardo -->
# Análisis de la paginación, el orden por columna y la búsqueda automática

Antes, cada tabla de búsqueda traía todas las filas de la base (`SELECT ... ORDER BY id` sin `LIMIT`) y el navegador las dibujaba todas; con 10 000 productos eso no escala. Ahora cada tabla muestra una página de 10 filas dentro de un tope de filas traídas (50, 100, 500 o 1000; por defecto 100), con el nombre de cada columna como enlace para ordenar y, en las pantallas con buscador, una búsqueda que se actualiza sola.

Recorrido: la petición GET trae `q`, `orden`, `dir`, `pagina` y `limite` → el servlet las lee con `util.Paginacion` → `Repositorio.buscar(Consulta)` arma la sentencia con `Paginador` → el servlet deja el resultado en el atributo `pagina` (y sus filas en `registros`, como antes) → la vista dibuja la tabla con `ordenable.tag`, `limite.tag` y `paginador.tag`.

Decisiones: el orden y la columna de búsqueda salen solo de una lista blanca por repositorio; el valor escrito viaja siempre como parámetro y los comodines `%` y `_` se toman como texto. El tope se aplica después del orden: con límite 100 y orden descendente se pueden recorrer las 100 primeras filas de ese orden, no 100 al azar. La página pedida fuera de rango se lleva a la última. Los productos no tienen columna de negocio, así que se paginan todos, igual que antes se listaban todos. `listar(String)` se conserva para los formularios de pedido, que siguen cargando todas las filas en sus listas desplegables (con muchos registros convendría reemplazarlas por un buscador, fuera de esta etapa). No se cambian tablas; `sql/07_paginacion.sql` solo agrega índices.

Nombre de archivo: Consulta.java

Bloque 1:
```java
public static final int TAMANO = 10, LIMITE_INICIAL = 100;
public static final int[] LIMITES = {50, 100, 500, 1000};
public static class ColumnaDesconocida extends IllegalArgumentException { ... }
```
Explicación: Lo que se pide a una tabla: búsqueda, orden, página y tope. El tamaño de página es fijo (10) y los topes permitidos son una lista cerrada. `ColumnaDesconocida` distingue el error de «Columna: valor» de cualquier otro dato inválido.

Nombre de archivo: Pagina.java

Bloque 1:
```java
public int getDesde() { return filas.isEmpty() ? 0 : (pagina - 1) * Consulta.TAMANO + 1; }
public int getHasta() { return filas.isEmpty() ? 0 : getDesde() + filas.size() - 1; }
```
Explicación: Una página de resultados. El total ya está acotado por el tope; `getDesde` y `getHasta` alimentan el texto «filas 21 a 30 de 100 traídas».

Nombre de archivo: Paginacion.java

Bloque 1:
```java
static void interpretar(Consulta c, String q) {
    c.setBusqueda(q);
    int i = q.indexOf(':');
    String columna = i > 0 ? q.substring(0, i).trim().split("\\s+")[0] : "";
    if (columna.isEmpty()) c.setTexto(q);
    else { c.setColumna(normalizar(columna)); c.setValor(q.substring(i + 1).trim()); }
}
```
Explicación: «Columna: valor» busca solo en esa columna y, si el encabezado tiene varias palabras, se usa la primera («Precio sin IGV: 3» es `precio`). Sin dos puntos queda la búsqueda libre de siempre. Solo el primer `:` separa columna y valor.

Bloque 2:
```java
public static String normalizar(String texto) {
    return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
}
```
Explicación: «Código», «CODIGO» y «codigo» son la misma columna. El valor buscado no se normaliza aquí: la colación `utf8mb4_unicode_ci` de la base ya ignora mayúsculas y tildes.

Bloque 3:
```java
try { pagina = busqueda.ejecutar(consulta); }
catch (Consulta.ColumnaDesconocida e) {
    request.setAttribute("avisoBusqueda", e.getMessage());
    pagina = new Pagina<>(List.of(), 1, 1, 0, consulta.getLimite());
}
```
Explicación: Una columna que la pantalla no ofrece no consulta la base: se muestra el aviso con las columnas válidas y una tabla vacía.

Nombre de archivo: Paginador.java

Bloque 1:
```java
String elegida = columnas.get(q.getOrden());
String orden = elegida == null ? ordenInicial : elegida + " " + direccion + "," + desempate + " " + direccion;
return new Plan("SELECT COUNT(*) FROM (SELECT 1" + base.substring(base.lastIndexOf(" FROM ")) + donde + " LIMIT ?) t",
        base + donde + " ORDER BY " + orden + " LIMIT ? OFFSET ?", valores);
```
Explicación: Lo único que llega a la sentencia desde el navegador es una clave que debe existir en la lista blanca; la dirección es `ASC` o `DESC` fija. El desempate por identificador evita que una fila se repita o se salte entre páginas. El conteo se acota con el mismo tope, así que nunca recorre más filas de las pedidas.

Bloque 2:
```java
public static String comodin(String valor) { return "%" + valor.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%"; }
```
Explicación: Con `LIKE ? ESCAPE '!'` lo escrito se busca como texto: «50%» no trae todo.

Nombre de archivo: ProductoRepositoryJDBC.java

Bloque 1:
```java
private static final Map<String,String> COLUMNAS=Paginador.columnas("codigo","codigo","nombre","nombre","tipo","tipo","categoria","categoria","precio","precio_venta","stock","stock","estado","estado");
@Override public Pagina<Producto> buscar(Consulta consulta) throws SQLException {
    return new Paginador("SELECT * FROM productos",COLUMNAS,"id_producto","id_producto").general("codigo","nombre","categoria").buscar(consulta,this::mapear);
}
```
Explicación: Cada repositorio declara su lista blanca (primera palabra del encabezado → columna SQL) y su orden inicial; el catálogo público usa otra lista más corta (código, nombre y categoría) y solo productos activos. Clientes, proveedores, usuarios, SOLPED, ventas, comprobantes, guías, notificaciones y el detalle del reporte siguen el mismo molde.

Nombre de archivo: ordenable.tag

Bloque 1:
```jsp
<c:set var="activa" value="${param[po] == columna}"/><c:set var="descendente" value="${param[pd] != 'asc'}"/>
<c:param name="${pd}" value="${activa and descendente ? 'asc' : 'desc'}"/>
```
Explicación: El primer clic en una columna ordena de forma descendente; si esa columna ya está ordenada de forma descendente, el clic siguiente la pasa a ascendente, y vuelve a descendente en el tercero. El enlace conserva el resto de los parámetros (búsqueda, límite y, en el reporte, fechas y vendedor) y omite `pagina`, así que vuelve a la primera. El encabezado activo lleva ▼ o ▲ y `aria-sort`.

Nombre de archivo: paginador.tag y limite.tag

Bloque 1:
```jsp
Página ${pagina.pagina} de ${pagina.paginas} · filas ${pagina.desde} a ${pagina.hasta} de ${pagina.total} traídas
```
Explicación: Pie de la tabla con enlaces de página (primera, última y dos vecinas de la actual). Si el total llega al tope se avisa que hay que afinar la búsqueda. `limite.tag` dibuja el selector del tope y el aviso de columna desconocida; cambiar el tope vuelve a la página 1. Con el prefijo `v_` o `p_` dos tablas conviven en la pantalla de reportes sin pisarse.

Nombre de archivo: app.js

Bloque 1:
```js
setInterval(function () {
    var texto = campo.value.trim(), destino = document.getElementById("resultados");
    if (texto === "" || texto === ultimo || !destino) return;
    ...
}, 470);
```
Explicación: Cada 470 ms el campo con `data-buscar` revisa lo escrito; si está vacío o es igual al último texto buscado no hace nada. Si hay texto nuevo pide la misma pantalla por GET con los campos ocultos (orden, dirección y tope), cancela el pedido anterior si seguía en curso, cambia solo `#resultados` y actualiza la dirección con `history.replaceState`. Si la respuesta no trae `#resultados` (por ejemplo, la sesión venció) navega a esa dirección. Sin JavaScript sigue funcionando el botón Buscar.

Nombre de archivo: 07_paginacion.sql

Bloque 1:
```sql
CREATE INDEX ix_productos_nombre ON productos(nombre);
CREATE INDEX ix_venta_fecha ON venta(fecha);
```
Explicación: Índices en columnas que se ordenan o filtran por rango. Los filtros `LIKE '%valor%'` no los aprovechan; el tope de filas traídas es lo que limita su costo.
