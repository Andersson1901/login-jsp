// Autor: Rodriguez Alvarado Andersson Eduardo
import modelo.*;
import repositorio.*;
import util.Excel;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.zip.*;
import javax.xml.parsers.DocumentBuilderFactory;

// La primera parte no usa la base; con --sin-bd solo se ejecuta esa parte.
// La segunda parte, solo sobre la base de desarrollo, crea dos vendedores con ventas propias y elimina todo al terminar.
public class PruebasReporte {
    static void exige(boolean valor,String mensaje) { if(!valor) throw new AssertionError(mensaje); }
    static void igual(BigDecimal actual,String esperado) {
        if(actual.compareTo(new BigDecimal(esperado))!=0) throw new AssertionError(actual+" != "+esperado);
    }
    static byte[] libro(Map<String,List<Object[]>> hojas) throws Exception {
        ByteArrayOutputStream salida=new ByteArrayOutputStream(); Excel.escribir(salida,hojas); return salida.toByteArray();
    }
    static Map<String,byte[]> partes(byte[] datos) throws Exception {
        Map<String,byte[]> mapa=new LinkedHashMap<>();
        try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(datos))) {
            for(ZipEntry e=zip.getNextEntry();e!=null;e=zip.getNextEntry()) mapa.put(e.getName(),zip.readAllBytes());
        }
        return mapa;
    }
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
    }
    static void modelo() {
        ReporteVentas r=new ReporteVentas();
        igual(r.getTicketPromedio(),"0.00");
        r.getPedidosPorEstado().put("PENDIENTE",2); r.getPedidosPorEstado().put("ATENDIDA",3);
        exige(r.getTotalPedidos()==5,"Total de pedidos incorrecto");
        r.setVentasEmitidas(3); r.setIngresos(new BigDecimal("100.00")); igual(r.getTicketPromedio(),"33.33");
        r.setVentasEmitidas(2); r.setIngresos(new BigDecimal("0.01")); igual(r.getTicketPromedio(),"0.01");
        System.out.println("Modelo: ticket promedio (sin ventas, redondeo) y total de pedidos correctos.");
    }
    static Usuario vendedor(String indice,String sufijo) throws Exception {
        Usuario u=new Usuario(); u.setNombres("Reporte"); u.setApellidos("QA "+indice); u.setDni(indice+sufijo.substring(1));
        u.setCorreo("repqa"+indice+sufijo+"@ejemplo.test"); u.setUsuario("repqa"+indice+sufijo); u.setRol("VENDEDOR"); u.setEstado("ACTIVO");
        u.setClaveHash(util.Seguridad.generarHash("clave-prueba")); Repositorios.usuarios().guardar(u); return u;
    }
    static Solped pedido(Cliente c,Producto p,String cantidad,int usuario,boolean aprobar) throws Exception {
        Detalle d=new Detalle(); d.setIdProducto(p.getIdProducto()); d.setCantidad(new BigDecimal(cantidad));
        Solped s=new Solped(); s.setIdCliente(c.getIdCliente()); s.setIdUsuario(usuario); s.setObservacion("Prueba automática del reporte");
        s.setDetalles(new ArrayList<>(List.of(d))); Repositorios.solped().guardar(s);
        if(aprobar) Repositorios.solped().cambiarEstado(s.getIdSolped(),"APROBADA"); return s;
    }
    static void borrarPedido(Solped s) throws Exception {
        Repositorios.solped().cambiarEstado(s.getIdSolped(),"ANULADA"); Repositorios.solped().eliminar(s.getIdSolped());
    }
    public static void main(String[] args) throws Exception {
        excel(); modelo();
        if(args.length>0 && "--sin-bd".equals(args[0])) return;
        UsuarioRepository usuarios=Repositorios.usuarios(); VentaRepository ventas=Repositorios.ventas(); ReporteRepository reportes=Repositorios.reportes();
        ClienteRepository clientes=Repositorios.clientes(); ProductoRepository productos=Repositorios.productos();
        String sufijo=String.format("%08d",Math.floorMod(System.nanoTime(),100000000));
        LocalDate hoy=LocalDate.now(ZoneOffset.ofHours(-5));
        ReporteVentas antes=reportes.ventas(null,hoy,hoy); // la tienda puede tener otros datos del día: se compara contra esta línea base
        Usuario a=vendedor("1",sufijo),b=vendedor("2",sufijo);
        Cliente cliente=new Cliente(); cliente.setTipoDocumento("DNI"); cliente.setNroDocumento(sufijo); cliente.setNombresRazonSocial("Cliente Reporte "+sufijo);
        cliente.setDireccion("Dirección de prueba"); cliente.setTelefono("900000000"); cliente.setCorreo("repqa@ejemplo.test"); cliente.setEstado("ACTIVO"); clientes.guardar(cliente);
        Producto producto=new Producto(); producto.setCodigo("RP-"+sufijo); producto.setNombre("RP-"+sufijo); producto.setDescripcion("Artículo creado por la prueba del reporte");
        producto.setTipo("MERCADERIA"); producto.setCategoria("Pruebas"); producto.setUnidadMedida("UND"); producto.setPrecioCompra(BigDecimal.ZERO);
        producto.setPrecioVenta(new BigDecimal("10")); producto.setStock(new BigDecimal("100")); producto.setStockMinimo(BigDecimal.ZERO); producto.setEstado("ACTIVO"); productos.guardar(producto);
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
        exige(despues.getTotalPedidos()-antes.getTotalPedidos()==4,"Pedidos de la tienda incorrectos");
        ReporteVentas vacio=reportes.ventas(a.getIdUsuario(),LocalDate.of(2000,1,1),LocalDate.of(2000,1,2));
        exige(vacio.getTotalPedidos()==0 && vacio.getVentasEmitidas()==0 && vacio.getPorDia().isEmpty() && vacio.getProductos().isEmpty() && vacio.getVentas().isEmpty(),"Un rango sin datos debe quedar vacío");
        exige(vacio.getPedidosPorEstado().size()==4,"Los cuatro estados deben aparecer aunque estén en cero"); igual(vacio.getTicketPromedio(),"0");
        Map<String,List<Object[]>> hojas=new LinkedHashMap<>(); hojas.put("Ventas",new ArrayList<>(ra.getVentas()));
        exige(libro(hojas).length>0,"No se pudo generar el Excel con datos reales");
        for(int v:new int[]{v1,v3}) ventas.anular(v);
        for(int v:new int[]{v1,v2,v3}) ventas.eliminar(v);
        for(Solped s:List.of(a1,a2,a3,b1)) borrarPedido(s);
        productos.eliminar(producto.getIdProducto()); clientes.eliminar(cliente.getIdCliente());
        usuarios.eliminar(a.getIdUsuario()); usuarios.eliminar(b.getIdUsuario());
        System.out.println("Reporte de ventas: filtro por vendedor, tienda completa, anuladas fuera de ingresos, rango vacío y limpieza correctos.");
    }
}
