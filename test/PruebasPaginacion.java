// Autor: Rodriguez Alvarado Andersson Eduardo
import modelo.*;
import repositorio.*;
import util.Paginacion;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

// La primera parte no usa la base; con --sin-bd solo se ejecuta esa parte.
// La segunda parte, solo sobre la base de desarrollo, crea 120 productos de prueba y los elimina al terminar.
public class PruebasPaginacion {
    static void exige(boolean valor,String mensaje) { if(!valor) throw new AssertionError(mensaje); }
    static HttpServletRequest peticion(String... pares) {
        Map<String,String> parametros=new HashMap<>();
        for(int i=0;i<pares.length;i+=2) parametros.put(pares[i],pares[i+1]);
        return (HttpServletRequest)Proxy.newProxyInstance(PruebasPaginacion.class.getClassLoader(),new Class<?>[]{HttpServletRequest.class},
            (proxy,metodo,args) -> { if("getParameter".equals(metodo.getName())) return parametros.get((String)args[0]); throw new UnsupportedOperationException(metodo.getName()); });
    }
    static void lectura() {
        Consulta c=Paginacion.consulta(peticion("q","Codigo: 0021"),"");
        exige("codigo".equals(c.getColumna()) && "0021".equals(c.getValor()) && c.getTexto().isEmpty(),"Columna: valor mal interpretado");
        c=Paginacion.consulta(peticion("q","CÓDIGO:x"),"");
        exige("codigo".equals(c.getColumna()) && "x".equals(c.getValor()),"Mayúsculas y tildes no deben importar en el nombre de la columna");
        c=Paginacion.consulta(peticion("q","Precio sin IGV: 3"),"");
        exige("precio".equals(c.getColumna()) && "3".equals(c.getValor()),"Con varias palabras se usa solo la primera");
        c=Paginacion.consulta(peticion("q","polo azul"),"");
        exige(c.getColumna().isEmpty() && "polo azul".equals(c.getTexto()),"Sin dos puntos es búsqueda libre");
        c=Paginacion.consulta(peticion("q",": suelto"),"");
        exige(c.getColumna().isEmpty() && ": suelto".equals(c.getTexto()),"Dos puntos sin nombre de columna es búsqueda libre");
        c=Paginacion.consulta(peticion("q","Codigo:"),"");
        exige("codigo".equals(c.getColumna()) && c.getValor().isEmpty(),"Columna sin valor no filtra");
        c=Paginacion.consulta(peticion("q","a:b:c"),"");
        exige("a".equals(c.getColumna()) && "b:c".equals(c.getValor()),"Solo el primer signo ':' separa columna y valor");
        c=Paginacion.consulta(peticion(),"");
        exige(c.getPagina()==1 && c.getLimite()==100 && c.isDescendente() && c.getOrden().isEmpty() && c.getBusqueda().isEmpty(),"Valores por defecto");
        c=Paginacion.consulta(peticion("pagina","-3","limite","7","dir","asc","orden","Precio"),"");
        exige(c.getPagina()==1 && c.getLimite()==100 && !c.isDescendente() && "precio".equals(c.getOrden()),"Página y límite inválidos vuelven al valor por defecto");
        c=Paginacion.consulta(peticion("pagina","abc","limite","500","dir","raro"),"");
        exige(c.getPagina()==1 && c.getLimite()==500 && c.isDescendente(),"Texto no numérico y dirección desconocida");
        c=Paginacion.consulta(peticion("orden","x".repeat(21)),"");
        exige(c.getOrden().isEmpty(),"Un orden demasiado largo se ignora");
        c=Paginacion.consulta(peticion("v_pagina","4","v_orden","total","pagina","9"),"v_");
        exige(c.getPagina()==4 && "total".equals(c.getOrden()),"El prefijo separa dos tablas de una misma pantalla");
        try { Paginacion.consulta(peticion("q","x".repeat(101)),""); throw new AssertionError("Debe rechazar q de más de 100 caracteres"); }
        catch(IllegalArgumentException esperado) { }
        System.out.println("Lectura: Columna: valor, primera palabra, mayúsculas y tildes, página, límite, orden y prefijo correctos.");
    }
    static void sentencias() {
        Map<String,String> columnas=Paginador.columnas("codigo","codigo","precio","precio_venta","nombre","nombre");
        Paginador listado=new Paginador("SELECT * FROM productos",columnas,"id_producto","id_producto").general("codigo","nombre");
        Consulta q=new Consulta();
        Paginador.Plan plan=listado.plan(q);
        exige("SELECT * FROM productos ORDER BY id_producto LIMIT ? OFFSET ?".equals(plan.getPagina()),"Sin filtro ni orden: "+plan.getPagina());
        exige("SELECT COUNT(*) FROM (SELECT 1 FROM productos LIMIT ?) t".equals(plan.getConteo()) && plan.getValores().isEmpty(),"El conteo se acota al límite: "+plan.getConteo());
        q.setColumna("codigo"); q.setValor("0021");
        plan=listado.plan(q);
        exige(plan.getPagina().contains(" WHERE codigo LIKE ? ESCAPE '!' ") && plan.getValores().equals(List.of("%0021%")),"Filtro por columna: "+plan.getPagina()+plan.getValores());
        exige(plan.getConteo().contains(" WHERE codigo LIKE ? ESCAPE '!' LIMIT ?"),"El conteo usa el mismo filtro");
        q.setValor("50%_!");
        exige(listado.plan(q).getValores().equals(List.of("%50!%!_!!%")),"Los comodines escritos se toman como texto");
        q.setValor("' OR 1=1 --");
        plan=listado.plan(q);
        exige(!plan.getPagina().contains("1=1") && plan.getValores().equals(List.of("%' OR 1=1 --%")),"El valor nunca se concatena a la sentencia");
        q.setColumna("precio"); q.setValor(""); q.setOrden("precio"); q.setDescendente(true);
        plan=listado.plan(q);
        exige(!plan.getPagina().contains("WHERE") && plan.getPagina().endsWith("ORDER BY precio_venta DESC,id_producto DESC LIMIT ? OFFSET ?"),"Columna sin valor no filtra y ordena descendente: "+plan.getPagina());
        q.setDescendente(false);
        exige(listado.plan(q).getPagina().contains("ORDER BY precio_venta ASC,id_producto ASC"),"Orden ascendente");
        q=new Consulta(); q.setOrden("precio_venta; DROP TABLE productos"); q.setDescendente(false);
        exige("SELECT * FROM productos ORDER BY id_producto LIMIT ? OFFSET ?".equals(listado.plan(q).getPagina()),"Un orden fuera de la lista blanca vuelve al orden inicial");
        q=new Consulta(); q.setTexto("polo");
        plan=listado.plan(q);
        exige(plan.getPagina().contains(" WHERE (codigo LIKE ? ESCAPE '!' OR nombre LIKE ? ESCAPE '!') ") && plan.getValores().equals(List.of("%polo%","%polo%")),"Búsqueda libre en varias columnas: "+plan.getPagina());
        q=new Consulta(); q.setColumna("marca"); q.setValor("x");
        try { listado.plan(q); throw new AssertionError("Debe rechazar una columna desconocida"); }
        catch(Consulta.ColumnaDesconocida esperado) { exige(esperado.getMessage().equals("Columna no reconocida. Usa: codigo, precio, nombre."),"Aviso: "+esperado.getMessage()); }
        Paginador activos=new Paginador("SELECT * FROM productos",columnas,"nombre","id_producto").general("nombre").fijo("estado='ACTIVO'");
        q=new Consulta(); q.setColumna("codigo"); q.setValor("a");
        plan=activos.plan(q);
        exige(plan.getPagina().contains(" WHERE estado='ACTIVO' AND codigo LIKE ? ESCAPE '!' ORDER BY nombre "),"La condición fija siempre se aplica: "+plan.getPagina());
        Paginador pedidos=new Paginador("SELECT s.nro_solped, (SELECT COALESCE(SUM(d.subtotal),0) FROM solped_detalle d WHERE d.id_solped=s.id_solped) FROM solped s JOIN clientes c ON c.id_cliente=s.id_cliente",columnas,"s.id_solped DESC","s.id_solped").fijo("DATE(s.fecha) BETWEEN ? AND ?",1,2);
        plan=pedidos.plan(new Consulta());
        exige(plan.getConteo().startsWith("SELECT COUNT(*) FROM (SELECT 1 FROM solped s JOIN clientes c ON c.id_cliente=s.id_cliente WHERE DATE(s.fecha) BETWEEN ? AND ? LIMIT ?)") && plan.getValores().equals(List.of(1,2)),"Un subselect en la lista de columnas no rompe el conteo: "+plan.getConteo());
        Pagina<String> pagina=new Pagina<>(Collections.nCopies(10,"x"),3,10,100,100);
        exige(pagina.getDesde()==21 && pagina.getHasta()==30,"Filas 21 a 30 de la página 3");
        pagina=new Pagina<>(Collections.nCopies(4,"x"),2,2,14,100);
        exige(pagina.getDesde()==11 && pagina.getHasta()==14,"Última página incompleta");
        pagina=new Pagina<>(new ArrayList<String>(),1,1,0,100);
        exige(pagina.getDesde()==0 && pagina.getHasta()==0,"Página vacía");
        System.out.println("Sentencias: filtro, escape de comodines, lista blanca, orden, conteo acotado, condición fija y aviso de columna desconocida correctos.");
    }
    static void conBd() throws Exception {
        ProductoRepository repositorio=Repositorios.productos();
        List<Integer> ids=new ArrayList<>();
        try {
            for(int i=1;i<=120;i++) {
                Producto p=new Producto(); p.setCodigo(String.format("PGT-%03d",i)); p.setNombre("Producto paginación "+i); p.setDescripcion("prueba");
                p.setTipo("MERCADERIA"); p.setCategoria("PruebaPaginacion"); p.setUnidadMedida("UND"); p.setPrecioCompra(new BigDecimal("1.00"));
                p.setPrecioVenta(new BigDecimal(i).setScale(2)); p.setStock(new BigDecimal("5.000")); p.setStockMinimo(new BigDecimal("0.000")); p.setEstado("ACTIVO");
                repositorio.guardar(p); ids.add(p.getIdProducto());
            }
            Consulta q=new Consulta(); q.setColumna("categoria"); q.setValor("PruebaPaginacion"); q.setLimite(100);
            Pagina<Producto> pagina=repositorio.buscar(q);
            exige(pagina.getTotal()==100 && pagina.getPaginas()==10 && pagina.getFilas().size()==10,"Con límite 100 hay 10 páginas de 10 filas aunque existan 120");
            q.setOrden("precio"); q.setDescendente(true);
            pagina=repositorio.buscar(q);
            exige(pagina.getFilas().get(0).getPrecioVenta().intValue()==120 && pagina.getFilas().get(9).getPrecioVenta().intValue()==111,"Descendente: la página 1 va de 120 a 111");
            q.setPagina(10); pagina=repositorio.buscar(q);
            exige(pagina.getFilas().get(0).getPrecioVenta().intValue()==30 && pagina.getFilas().get(9).getPrecioVenta().intValue()==21,"El tope se aplica después del orden: la página 10 va de 30 a 21");
            q.setDescendente(false); q.setPagina(1); pagina=repositorio.buscar(q);
            exige(pagina.getFilas().get(0).getPrecioVenta().intValue()==1,"Ascendente: la página 1 empieza en 1");
            q.setPagina(99); pagina=repositorio.buscar(q);
            exige(pagina.getPagina()==10,"Una página fuera de rango se lleva a la última");
            q.setLimite(50); q.setPagina(1); pagina=repositorio.buscar(q);
            exige(pagina.getTotal()==50 && pagina.getPaginas()==5,"Con límite 50 hay 5 páginas");
            Set<String> vistos=new HashSet<>();
            for(int n=1;n<=5;n++) { q.setPagina(n); for(Producto p:repositorio.buscar(q).getFilas()) exige(vistos.add(p.getCodigo()),"Fila repetida entre páginas: "+p.getCodigo()); }
            exige(vistos.size()==50,"Las 5 páginas suman 50 filas distintas");
            q=new Consulta(); q.setColumna("codigo"); q.setValor("PGT-01");
            exige(repositorio.buscar(q).getTotal()==10,"Codigo: PGT-01 trae PGT-010 a PGT-019");
            q.setValor("PGT-0_");
            exige(repositorio.buscar(q).getTotal()==0,"El guion bajo escrito se toma como texto, no como comodín");
            q=new Consulta(); q.setColumna("marca"); q.setValor("x");
            try { repositorio.buscar(q); throw new AssertionError("Debe rechazar una columna desconocida"); } catch(Consulta.ColumnaDesconocida esperado) { }
            q=new Consulta(); q.setColumna("estado"); q.setValor("zzz");
            try { repositorio.buscarActivos(q); throw new AssertionError("El catálogo no debe ofrecer la columna estado"); }
            catch(Consulta.ColumnaDesconocida esperado) { exige("Columna no reconocida. Usa: codigo, nombre, categoria.".equals(esperado.getMessage()),"Aviso del catálogo: "+esperado.getMessage()); }
        } finally { for(int id:ids) repositorio.eliminar(id); }
        System.out.println("Base de datos: tope, páginas, orden, filtros y catálogo correctos con 120 productos de prueba.");
    }
    public static void main(String[] args) throws Exception {
        lectura(); sentencias();
        if(args.length>0 && "--sin-bd".equals(args[0])) return;
        conBd();
    }
}
