<!-- Autor: Rodriguez Alvarado Andersson Eduardo -->
# Análisis de la reportería de ventas de un vendedor o de la tienda

Desde el panel de inicio, el botón «Reporte de ventas» abre `/reportes`. Recorrido: `ReporteServlet` (rol y fechas) → `ReporteRepository.ventas` → `ReporteVentas` → `reportes.jsp` (tarjetas, tablas y gráficos de Apache ECharts) o, con `formato=xlsx`, un libro Excel con las mismas cifras y el detalle de cada venta y pedido.

Decisiones: el VENDEDOR solo ve sus propios pedidos y ventas (el id sale de la sesión, nunca del navegador); ADMIN y DUENO eligen un vendedor o toda la tienda. No se cambia el esquema ni se agrega un script SQL: se usan solo las tablas existentes. Las ventas anuladas se muestran aparte y no suman a los ingresos. Los gráficos se dibujan desde las mismas tablas de la pantalla (valores en `data-valor`, sin JSON incrustado), de modo que gráfico y tabla no pueden diferir. ECharts se sirve desde el propio proyecto, sin CDN. El Excel se escribe con una clase propia de `java.util.zip`, sin agregar librerías.

Nombre de archivo: ReporteVentas.java

Bloque 1:
```java
public class ReporteVentas {
    public static class Punto {
        private final String etiqueta;
        private final BigDecimal cantidad;
        private final BigDecimal importe;
        public Punto(String etiqueta,BigDecimal cantidad,BigDecimal importe) { this.etiqueta=etiqueta; this.cantidad=cantidad; this.importe=importe; }
        public String getEtiqueta() { return etiqueta; }
        public BigDecimal getCantidad() { return cantidad; }
        public BigDecimal getImporte() { return importe; }
    }
    private String vendedor;
    private LocalDate desde;
    private LocalDate hasta;
    private final Map<String,Integer> pedidosPorEstado=new LinkedHashMap<>();
    private int ventasEmitidas;
    private int ventasAnuladas;
    private BigDecimal ingresos=BigDecimal.ZERO.setScale(2);
    private BigDecimal montoAnulado=BigDecimal.ZERO.setScale(2);
    private final List<Punto> porDia=new ArrayList<>();
    private final List<Punto> productos=new ArrayList<>();
    private final List<Punto> clientes=new ArrayList<>();
    private final List<Object[]> ventas=new ArrayList<>();
    private final List<Object[]> pedidos=new ArrayList<>();
```
Explicación: Resultado del reporte. Punto agrupa etiqueta, cantidad e importe para los gráficos y tablas; los totales parten en cero con dos decimales y los pedidos por estado conservan el orden del flujo.

Bloque 2:
```java
    public int getTotalPedidos() { return pedidosPorEstado.values().stream().mapToInt(Integer::intValue).sum(); }
    public BigDecimal getTicketPromedio() {
        if(ventasEmitidas==0) return BigDecimal.ZERO.setScale(2);
        return ingresos.divide(BigDecimal.valueOf(ventasEmitidas),2,RoundingMode.HALF_UP);
```
Explicación: Dos valores derivados: el total de pedidos y el ticket promedio, que evita dividir entre cero cuando no hay ventas.

Nombre de archivo: ReporteRepository.java

Bloque 1:
```java
public interface ReporteRepository {
    // idVendedor null: toda la tienda. Las fechas son el rango inclusivo del reporte.
    ReporteVentas ventas(Integer idVendedor, LocalDate desde, LocalDate hasta) throws SQLException;
```
Explicación: Contrato del repositorio, igual que los demás: el servlet depende de la interfaz. Un idVendedor nulo significa toda la tienda.

Nombre de archivo: ReporteRepositoryJDBC.java

Bloque 1:
```java
    // %s recibe el filtro opcional por vendedor; todas las consultas usan el mismo rango de fechas.
    static final String PEDIDOS_ESTADO="SELECT s.estado, COUNT(*) FROM solped s WHERE DATE(s.fecha) BETWEEN ? AND ?%s GROUP BY s.estado";
```
Explicación: Consultas con parámetros. El único texto que se concatena es el filtro opcional por vendedor, fijo en el código; los valores van siempre como parámetros.

Bloque 2:
```java
    private static PreparedStatement preparar(Connection c,String sql,String alias,Integer vendedor,LocalDate desde,LocalDate hasta) throws SQLException {
        PreparedStatement p=c.prepareStatement(String.format(sql,vendedor==null?"":" AND "+alias+".id_usuario=?"));
        p.setDate(1,java.sql.Date.valueOf(desde)); p.setDate(2,java.sql.Date.valueOf(hasta));
        if(vendedor!=null) p.setInt(3,vendedor);
        return p;
```
Explicación: Arma cada consulta con el rango de fechas y, si corresponde, el vendedor.

Bloque 3:
```java
    @Override public ReporteVentas ventas(Integer idVendedor,LocalDate desde,LocalDate hasta) throws SQLException {
        ReporteVentas reporte=new ReporteVentas(); reporte.setDesde(desde); reporte.setHasta(hasta);
        for(String estado:List.of("PENDIENTE","APROBADA","ATENDIDA","ANULADA")) reporte.getPedidosPorEstado().put(estado,0);
        try(Connection c=ConexionBD.getInstancia().getConexion()) {
            c.setAutoCommit(false); // una sola lectura consistente para todas las consultas
            try(PreparedStatement p=preparar(c,PEDIDOS_ESTADO,"s",idVendedor,desde,hasta); ResultSet r=p.executeQuery()) {
                while(r.next()) reporte.getPedidosPorEstado().put(r.getString(1),r.getInt(2));
            }
            try(PreparedStatement p=preparar(c,VENTAS_ESTADO,"v",idVendedor,desde,hasta); ResultSet r=p.executeQuery()) {
                while(r.next()) {
                    if("EMITIDA".equals(r.getString(1))) { reporte.setVentasEmitidas(r.getInt(2)); reporte.setIngresos(r.getBigDecimal(3)); }
                    else { reporte.setVentasAnuladas(r.getInt(2)); reporte.setMontoAnulado(r.getBigDecimal(3)); }
                }
            }
            try(PreparedStatement p=preparar(c,POR_DIA,"v",idVendedor,desde,hasta); ResultSet r=p.executeQuery()) {
                while(r.next()) reporte.getPorDia().add(new Punto(r.getDate(1).toString(),r.getBigDecimal(2),r.getBigDecimal(3)));
            }
            try(PreparedStatement p=preparar(c,PRODUCTOS,"v",idVendedor,desde,hasta); ResultSet r=p.executeQuery()) {
                while(r.next()) reporte.getProductos().add(new Punto(r.getString(1),r.getBigDecimal(2),r.getBigDecimal(3)));
            }
            try(PreparedStatement p=preparar(c,CLIENTES,"v",idVendedor,desde,hasta); ResultSet r=p.executeQuery()) {
                while(r.next()) reporte.getClientes().add(new Punto(r.getString(1),r.getBigDecimal(2),r.getBigDecimal(3)));
            }
            try(PreparedStatement p=preparar(c,VENTAS,"v",idVendedor,desde,hasta); ResultSet r=p.executeQuery()) {
                while(r.next()) reporte.getVentas().add(new Object[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getBigDecimal(6),r.getBigDecimal(7),r.getBigDecimal(8)});
            }
            try(PreparedStatement p=preparar(c,PEDIDOS,"s",idVendedor,desde,hasta); ResultSet r=p.executeQuery()) {
                while(r.next()) reporte.getPedidos().add(new Object[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getBigDecimal(6)});
            }
            c.commit();
```
Explicación: Una sola conexión y una sola transacción para todas las consultas, de modo que las cifras sean consistentes entre sí. Los cuatro estados de pedido aparecen siempre, aunque estén en cero.

Nombre de archivo: Excel.java

Bloque 1:
```java
    private static String hoja(List<Object[]> filas) {
        int columnas=filas.stream().mapToInt(f -> f.length).max().orElse(1);
        StringBuilder x=new StringBuilder(CABECERA+"<worksheet xmlns=\""+ESPACIO+"\"><cols><col min=\"1\" max=\""+columnas+"\" width=\"24\" customWidth=\"1\"/></cols><sheetData>");
        for(int f=0;f<filas.size();f++) {
            x.append("<row r=\""+(f+1)+"\">");
            Object[] fila=filas.get(f);
            for(int c=0;c<fila.length;c++) {
                Object valor=fila[c]; if(valor==null) continue;
                String ref=columna(c)+(f+1);
                if(valor instanceof BigDecimal) x.append("<c r=\""+ref+"\" s=\"2\"><v>"+((BigDecimal)valor).toPlainString()+"</v></c>");
                else if(valor instanceof Number) x.append("<c r=\""+ref+"\"><v>"+valor+"</v></c>");
                else x.append("<c r=\""+ref+"\" t=\"inlineStr\""+(f==0?" s=\"1\"":"")+"><is><t xml:space=\"preserve\">"+xml(texto(valor.toString(),32767))+"</t></is></c>");
            }
            x.append("</row>");
        }
        return x.append("</sheetData></worksheet>").toString();
```
Explicación: Cada hoja es XML generado a mano. Los textos se guardan como texto en línea, nunca como fórmula, y los BigDecimal como números con formato de dos decimales.

Bloque 2:
```java
    // Excel limita el nombre de la hoja a 31 caracteres y no admite estos símbolos.
    private static String nombreHoja(String nombre) {
        String limpio=nombre.replaceAll("[\\[\\]:*?/\\\\]"," ").trim();
        return limpio.isEmpty() ? "Hoja" : limpio.substring(0,Math.min(31,limpio.length()));
    }
    private static String texto(String valor,int maximo) { return valor.length()>maximo ? valor.substring(0,maximo) : valor; }
```
Explicación: Saneamiento: nombres de hoja de hasta 31 caracteres sin símbolos prohibidos, y caracteres de control eliminados antes de escapar el XML.

Nombre de archivo: ReporteServlet.java

Bloque 1:
```java
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        HttpSession sesion = request.getSession();
        boolean elige = !"VENDEDOR".equals(sesion.getAttribute("rol"));
        LocalDate hoy = LocalDate.now(ZoneOffset.ofHours(-5)); // misma zona horaria que usa ConexionBD
        LocalDate desde = hoy.minusDays(29), hasta = hoy;
        Integer vendedor = elige ? null : (Integer) sesion.getAttribute("idUsuario");
        try {
            if (elige) request.setAttribute("vendedores",vendedores()); // el selector se muestra aunque haya un error de fechas
            if (!Web.texto(request,"desde",10).isEmpty()) desde = fecha(Web.texto(request,"desde",10));
            if (!Web.texto(request,"hasta",10).isEmpty()) hasta = fecha(Web.texto(request,"hasta",10));
            if (desde.isAfter(hasta)) throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
            if (ChronoUnit.DAYS.between(desde,hasta) >= MAXIMO_DIAS) throw new IllegalArgumentException("El rango máximo es de " + MAXIMO_DIAS + " días.");
            if (elige && !Web.texto(request,"vendedor",10).isEmpty()) vendedor = Web.id(request,"vendedor");
            request.setAttribute("desde",desde); request.setAttribute("hasta",hasta); request.setAttribute("vendedor",vendedor);
            ReporteVentas reporte = null;
            if (vendedor != null) {
                Usuario u = usuarios.buscarPorId(vendedor);
                if (u == null || !Set.of("ADMIN","DUENO","VENDEDOR").contains(u.getRol())) throw new IllegalArgumentException("Selecciona un vendedor válido.");
                reporte = reportes.ventas(vendedor,desde,hasta);
                reporte.setVendedor(u.getNombres() + " " + u.getApellidos() + " (" + u.getUsuario() + ")");
            } else reporte = reportes.ventas(null,desde,hasta);
            if ("xlsx".equals(Web.texto(request,"formato",10))) { descargar(response,reporte); return; }
            request.setAttribute("reporte",reporte);
        } catch (IllegalArgumentException e) {
            request.setAttribute("desde",desde); request.setAttribute("hasta",hasta); request.setAttribute("vendedor",vendedor);
            request.setAttribute("error",e.getMessage());
        } catch (SQLException e) {
            log("Error al generar el reporte de ventas",e);
            request.setAttribute("desde",desde); request.setAttribute("hasta",hasta); request.setAttribute("vendedor",vendedor);
            request.setAttribute("error","No se pudo generar el reporte. Intenta nuevamente.");
        }
        request.getRequestDispatcher("/WEB-INF/vistas/reportes.jsp").forward(request,response);
```
Explicación: El rol decide el alcance: el VENDEDOR siempre usa su id de sesión y se ignora el parámetro vendedor. Las fechas se validan (orden y máximo de 366 días) y los errores vuelven a la misma pantalla sin consultar la base.

Bloque 2:
```java
    // Mismos datos que se ven en pantalla, uno por hoja.
    private void descargar(HttpServletResponse response,ReporteVentas r) throws IOException {
        Map<String,List<Object[]>> hojas = new LinkedHashMap<>();
        List<Object[]> resumen = new ArrayList<>();
        resumen.add(new Object[]{"Dato","Valor"});
        resumen.add(new Object[]{"Alcance", r.getVendedor() == null ? "Toda la tienda" : r.getVendedor()});
        resumen.add(new Object[]{"Desde", r.getDesde().toString()});
        resumen.add(new Object[]{"Hasta", r.getHasta().toString()});
        for (Map.Entry<String,Integer> e : r.getPedidosPorEstado().entrySet()) resumen.add(new Object[]{"Pedidos " + e.getKey(), e.getValue()});
        resumen.add(new Object[]{"Pedidos (total)", r.getTotalPedidos()});
        resumen.add(new Object[]{"Ventas emitidas", r.getVentasEmitidas()});
        resumen.add(new Object[]{"Ingresos (S/)", r.getIngresos()});
        resumen.add(new Object[]{"Ticket promedio (S/)", r.getTicketPromedio()});
        resumen.add(new Object[]{"Ventas anuladas", r.getVentasAnuladas()});
        resumen.add(new Object[]{"Monto anulado (S/)", r.getMontoAnulado()});
        hojas.put("Resumen",resumen);
        hojas.put("Ventas por día",puntos("Fecha","Ventas","Importe (S/)",r.getPorDia(),true));
        hojas.put("Productos",puntos("Producto","Unidades","Importe (S/)",r.getProductos(),false));
        hojas.put("Clientes",puntos("Cliente","Compras","Importe (S/)",r.getClientes(),true));
        List<Object[]> ventas = new ArrayList<>();
        ventas.add(new Object[]{"N° venta","Fecha","Cliente","Vendedor","Estado","Subtotal (S/)","IGV (S/)","Total (S/)"});
        ventas.addAll(r.getVentas());
        hojas.put("Ventas",ventas);
        List<Object[]> pedidos = new ArrayList<>();
        pedidos.add(new Object[]{"N° SOLPED","Fecha","Cliente","Vendedor","Estado","Subtotal (S/)"});
        pedidos.addAll(r.getPedidos());
        hojas.put("Pedidos",pedidos);
        response.setContentType(XLSX);
        response.setHeader("Content-Disposition","attachment; filename=\"reporte-ventas-" + r.getDesde() + "_" + r.getHasta() + ".xlsx\"");
        Excel.escribir(response.getOutputStream(),hojas);
```
Explicación: La descarga reutiliza el mismo objeto ReporteVentas: Resumen, Ventas por día, Productos, Clientes, Ventas y Pedidos, una hoja cada uno.

Nombre de archivo: reportes.jsp

Bloque 1:
```jsp
<form method="get" action="reportes" class="card mb-4"><div class="card-body row g-3 align-items-end">
<c:if test="${sessionScope.rol != 'VENDEDOR'}"><div class="col-md-4"><label class="form-label" for="vendedor">Vendedor</label><select class="form-select" id="vendedor" name="vendedor"><option value="">Toda la tienda</option><c:forEach var="u" items="${vendedores}"><option value="${u.idUsuario}"${u.idUsuario == vendedor ? ' selected' : ''}><c:out value="${u.nombres} ${u.apellidos} (${u.usuario})"/></option></c:forEach></select></div></c:if>
<div class="col-md-3"><label class="form-label" for="desde">Desde</label><input class="form-control" type="date" id="desde" name="desde" value="<c:out value='${desde}'/>" required></div>
<div class="col-md-3"><label class="form-label" for="hasta">Hasta</label><input class="form-control" type="date" id="hasta" name="hasta" value="<c:out value='${hasta}'/>" required></div>
<div class="col-md-2 acciones"><button class="btn btn-primary">Ver reporte</button><c:if test="${not empty reporte}"><button class="btn btn-dorado" name="formato" value="xlsx">Descargar Excel</button></c:if></div>
</div></form>
```
Explicación: Filtros por GET (el reporte no modifica datos, por lo que no necesita CSRF). El selector de vendedor solo aparece para ADMIN y DUENO; el botón de Excel aparece cuando ya hay un reporte.

Bloque 2:
```jsp
<div class="col-lg-5"><div class="card h-100"><div class="card-body"><h2 class="h5">Pedidos por estado</h2><div class="grafico" data-tabla="tPedidos" data-tipo="pie"></div>
<table class="table table-sm mt-3" id="tPedidos"><thead><tr><th>Estado</th><th>Pedidos</th></tr></thead><tbody><c:forEach var="e" items="${reporte.pedidosPorEstado}"><tr data-dato><td><c:out value="${e.key}"/></td><td data-valor="<c:out value='${e.value}'/>"><c:out value="${e.value}"/></td></tr></c:forEach></tbody></table></div></div></div>
```
Explicación: Cada gráfico es un div que apunta a la tabla que lo alimenta; las celdas llevan el valor sin formato local en data-valor. Todo texto de la base sale con c:out.

Nombre de archivo: reportes.js

Bloque 1:
```javascript
document.addEventListener("DOMContentLoaded", function () {
    if (!window.echarts) return;
    const colores = ["#0d1f4d", "#e0b83c", "#3f6fb5", "#9aa7c2", "#b5883a", "#6c7a96"];
    function leer(tabla, columna) {
        const etiquetas = [], valores = [];
        tabla.querySelectorAll("tbody tr[data-dato]").forEach(function (fila) {
            const celda = fila.querySelectorAll("td")[columna];
            etiquetas.push(fila.cells[0].textContent.trim());
            valores.push(Number(celda.dataset.valor) || 0);
        });
        return { etiquetas: etiquetas, valores: valores };
    }
    document.querySelectorAll("div.grafico").forEach(function (contenedor) {
```
Explicación: Lee etiquetas y valores desde la tabla indicada, no desde datos incrustados en la página.

Bloque 2:
```javascript
        const tabla = document.getElementById(contenedor.dataset.tabla);
        if (!tabla) return;
        const datos = leer(tabla, Number(contenedor.dataset.columna) || 1);
        if (!datos.etiquetas.length) { contenedor.textContent = "Sin datos en el rango."; contenedor.classList.add("tabla-vacia"); return; }
        const tipo = contenedor.dataset.tipo || "bar";
        const grafico = echarts.init(contenedor, null, { renderer: "svg" });
        const opcion = { color: colores, tooltip: { trigger: tipo === "pie" ? "item" : "axis" }, grid: { left: 50, right: 20, top: 20, bottom: 50, containLabel: true } };
        if (tipo === "pie") {
            opcion.legend = { bottom: 0 };
            opcion.series = [{ type: "pie", radius: ["40%", "70%"], data: datos.etiquetas.map(function (n, i) { return { name: n, value: datos.valores[i] }; }) }];
            delete opcion.grid;
        } else {
            opcion.xAxis = { type: "category", data: datos.etiquetas, axisLabel: { interval: 0, rotate: datos.etiquetas.length > 6 ? 30 : 0 } };
            opcion.yAxis = { type: "value" };
            opcion.series = [{ type: tipo, data: datos.valores, name: contenedor.dataset.nombre || "" }];
        }
        grafico.setOption(opcion);
```
Explicación: Dibuja pie, línea o barras con el renderizador SVG de ECharts, y muestra «Sin datos en el rango» cuando la tabla está vacía.

Nombre de archivo: bienvenido.jsp

Bloque 1:
```jsp
<div class="d-flex justify-content-between align-items-center mb-4"><div><p class="text-muted mb-1">Panel principal</p><h1>Hola, <c:out value="${sessionScope.nombreUsuario}"/></h1><p>Organiza tu tienda desde un solo lugar.</p></div><div class="d-flex align-items-center gap-3"><a class="btn btn-dorado" href="reportes">Reporte de ventas</a><span class="badge text-bg-light">ModaExpress Perú</span></div></div>
```
Explicación: El botón simple del panel de inicio, visible para todos los roles internos; el filtro ya impide el acceso a los compradores.

Nombre de archivo: PruebasReporte.java

Bloque 1:
```java
    static void excel() throws Exception {
        Map<String,List<Object[]>> hojas=new LinkedHashMap<>();
        List<Object[]> uno=new ArrayList<>();
        uno.add(new Object[]{"Dato","Valor"}); uno.add(new Object[]{"Ingresos",new BigDecimal("1234.5")}); uno.add(new Object[]{"Ventas",3});
        uno.add(new Object[]{"<b>&\"'</b> =1+1 \u0001\u0008fin",null}); uno.add(new Object[]{"emoji 😀 ñ",new BigDecimal("0")});
        hojas.put("Resumen",uno);
        hojas.put("Nombre: [muy] largo/con*símbolos?\\ y más de treinta y un caracteres",Collections.singletonList(new Object[]{"A"}));
        hojas.put("Vacía",new ArrayList<>());
        Map<String,byte[]> p=partes(libro(hojas));
        for(String nombre:List.of("[Content_Types].xml","_rels/.rels","xl/workbook.xml","xl/_rels/workbook.xml.rels","xl/styles.xml","xl/worksheets/sheet1.xml","xl/worksheets/sheet2.xml","xl/worksheets/sheet3.xml"))
            exige(p.containsKey(nombre),"Falta la parte "+nombre);
        var fabrica=DocumentBuilderFactory.newInstance(); fabrica.setNamespaceAware(true);
        for(Map.Entry<String,byte[]> e:p.entrySet()) fabrica.newDocumentBuilder().parse(new ByteArrayInputStream(e.getValue())); // todo debe ser XML válido
        String hoja1=new String(p.get("xl/worksheets/sheet1.xml"),"UTF-8");
        exige(hoja1.contains("<v>1234.5</v>") && hoja1.contains("<v>3</v>"),"Números mal escritos");
        exige(hoja1.contains("&lt;b&gt;&amp;&quot;'&lt;/b&gt; =1+1 fin"),"Escape o limpieza de caracteres incorrectos");
        exige(hoja1.contains("t=\"inlineStr\"") && !hoja1.contains("<f>"),"Los textos deben guardarse como texto, nunca como fórmula");
        exige(hoja1.contains("😀"),"Se perdió un carácter fuera del plano básico");
        String libro=new String(p.get("xl/workbook.xml"),"UTF-8");
        exige(libro.contains("name=\"Resumen\"") && libro.contains("name=\"Nombre   muy  largo con símbolo\""),"Nombre de hoja sin sanear: "+libro);
        System.out.println("Excel: partes, XML válido, escape, números, texto sin fórmulas y nombres de hoja correctos.");
```
Explicación: Prueba sin base de datos del Excel: partes obligatorias, XML válido, escape, números, textos sin fórmulas y nombres de hoja.

Bloque 2:
```java
        // Vendedor A: una venta emitida (2 u.), una anulada (1 u.) y un pedido pendiente. Vendedor B: una venta emitida (3 u.).
        Solped a1=pedido(cliente,producto,"2",a.getIdUsuario(),true),a2=pedido(cliente,producto,"1",a.getIdUsuario(),true),a3=pedido(cliente,producto,"1",a.getIdUsuario(),false),b1=pedido(cliente,producto,"3",b.getIdUsuario(),true);
        int v1=ventas.crearDesdeSolped(a1.getIdSolped(),a.getIdUsuario()),v2=ventas.crearDesdeSolped(a2.getIdSolped(),a.getIdUsuario()),v3=ventas.crearDesdeSolped(b1.getIdSolped(),b.getIdUsuario());
        ventas.anular(v2);
        ReporteVentas ra=reportes.ventas(a.getIdUsuario(),hoy,hoy);
        exige(ra.getVentasEmitidas()==1 && ra.getVentasAnuladas()==1,"Conteo de ventas del vendedor A incorrecto");
        igual(ra.getIngresos(),"23.60"); igual(ra.getMontoAnulado(),"11.80"); igual(ra.getTicketPromedio(),"23.60");
        exige(ra.getTotalPedidos()==3 && ra.getPedidosPorEstado().get("ATENDIDA")==1 && ra.getPedidosPorEstado().get("APROBADA")==1 && ra.getPedidosPorEstado().get("PENDIENTE")==1 && ra.getPedidosPorEstado().get("ANULADA")==0,"Pedidos por estado de A incorrectos: "+ra.getPedidosPorEstado());
        exige(ra.getPorDia().size()==1 && ra.getPorDia().get(0).getEtiqueta().equals(hoy.toString()),"Ventas por día incorrectas");
        igual(ra.getPorDia().get(0).getImporte(),"23.60");
        exige(ra.getProductos().size()==1 && ra.getProductos().get(0).getEtiqueta().equals("RP-"+sufijo),"Productos incorrectos");
        igual(ra.getProductos().get(0).getCantidad(),"2"); igual(ra.getProductos().get(0).getImporte(),"20");
        exige(ra.getClientes().size()==1 && ra.getClientes().get(0).getEtiqueta().equals("Cliente Reporte "+sufijo),"Clientes incorrectos");
        exige(ra.getVentas().size()==2 && ra.getPedidos().size()==3,"El detalle debe incluir todas las ventas y pedidos del vendedor");
        exige(ra.getVentas().stream().allMatch(f -> ("repqa1"+sufijo).equals(f[3])) && ra.getPedidos().stream().allMatch(f -> ("repqa1"+sufijo).equals(f[3])),"El reporte de A incluye datos de otro vendedor");
        ReporteVentas rb=reportes.ventas(b.getIdUsuario(),hoy,hoy);
        exige(rb.getVentasEmitidas()==1 && rb.getVentasAnuladas()==0 && rb.getTotalPedidos()==1,"Reporte del vendedor B incorrecto");
        igual(rb.getIngresos(),"35.40"); igual(rb.getMontoAnulado(),"0");
        ReporteVentas despues=reportes.ventas(null,hoy,hoy);
        exige(despues.getVentasEmitidas()-antes.getVentasEmitidas()==2 && despues.getVentasAnuladas()-antes.getVentasAnuladas()==1,"Conteo de la tienda incorrecto");
        igual(despues.getIngresos().subtract(antes.getIngresos()),"59.00"); igual(despues.getMontoAnulado().subtract(antes.getMontoAnulado()),"11.80");
```
Explicación: Prueba sobre la base de desarrollo con dos vendedores: cada reporte incluye solo lo suyo, las anuladas no suman a los ingresos y la tienda se compara contra una línea base.

Análisis crítico del código fuente: El reporte reutiliza las tablas existentes y no cambia el esquema, pero por eso tiene límites: no hay separación por tienda (la tienda completa suma todos los vendedores), solo se informan los pedidos y ventas con su fecha de registro, y los rankings se limitan a 10 filas y el detalle a 5000 por consulta. El rango se acota a 366 días para proteger la base. El filtro por fecha usa DATE(columna), lo que impide aprovechar un índice por fecha; con el volumen de un proyecto académico es aceptable, y con volumen real convendría comparar contra un rango de fechas completo. El Excel se escribe en memoria y no admite fórmulas ni estilos propios; a cambio no agrega dependencias. Todo texto proveniente de la base se escapa (c:out en la página, XML en el Excel). El VENDEDOR no puede ver datos de otro porque el id sale de la sesión. La verificación del JSP y de las consultas sobre MariaDB depende de ejecutar la aplicación en Tomcat.
