// Autor: Rodriguez Alvarado Andersson Eduardo
import modelo.*;
import repositorio.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

// Ejecutar solo sobre la base de desarrollo. Crea y elimina sus propios registros.
public class PruebasVentas {
    static final ClienteRepository clientes=Repositorios.clientes();
    static final ProductoRepository productos=Repositorios.productos();
    static final SolpedRepository solicitudes=Repositorios.solped();
    static final VentaRepository ventas=Repositorios.ventas();
    static final ComprobanteRepository comprobantes=Repositorios.comprobantes();
    static final GuiaRepository guias=Repositorios.guias();
    static int usuario;
    interface Accion { void ejecutar() throws Exception; }
    static void rechaza(Accion accion) throws Exception {
        try { accion.ejecutar(); } catch(IllegalArgumentException | SQLException e) { return; }
        throw new AssertionError("La operación inválida fue aceptada");
    }
    static void igual(BigDecimal actual,String esperado) {
        if(actual.compareTo(new BigDecimal(esperado))!=0) throw new AssertionError(actual+" != "+esperado);
    }
    static void exige(boolean valor,String mensaje) { if(!valor) throw new AssertionError(mensaje); }
    static Cliente cliente(String tipo,String documento,String nombre) throws Exception {
        Cliente c=new Cliente(); c.setTipoDocumento(tipo); c.setNroDocumento(documento); c.setNombresRazonSocial(nombre);
        c.setDireccion("Dirección de prueba"); c.setTelefono("900000000"); c.setCorreo("ventasqa@ejemplo.test"); c.setEstado("ACTIVO"); clientes.guardar(c); return c;
    }
    static Producto producto(String codigo,String tipo,String stock,String precio) throws Exception {
        Producto p=new Producto(); p.setCodigo(codigo); p.setNombre(codigo); p.setDescripcion("Artículo creado por la prueba de ventas");
        p.setTipo(tipo); p.setCategoria("Pruebas"); p.setUnidadMedida("UND"); p.setPrecioCompra(BigDecimal.ZERO); p.setPrecioVenta(new BigDecimal(precio));
        p.setStock(new BigDecimal(stock)); p.setStockMinimo(BigDecimal.ZERO); p.setEstado("ACTIVO"); productos.guardar(p); return p;
    }
    static Detalle item(Producto p,String cantidad) {
        Detalle d=new Detalle(); d.setIdProducto(p.getIdProducto()); d.setCantidad(new BigDecimal(cantidad)); return d;
    }
    static Solped pedido(Cliente c,Detalle... items) throws Exception {
        Solped s=new Solped(); s.setIdCliente(c.getIdCliente()); s.setIdUsuario(usuario); s.setObservacion("Prueba automática de transacciones");
        s.setDetalles(new ArrayList<>(Arrays.asList(items))); solicitudes.guardar(s); return s;
    }
    static void borrarPedido(Solped s) throws Exception {
        solicitudes.cambiarEstado(s.getIdSolped(),"ANULADA"); solicitudes.eliminar(s.getIdSolped());
    }
    static Comprobante comprobante(int id,String tipo) {
        Comprobante c=new Comprobante(); c.setIdVenta(id); c.setTipo(tipo); return c;
    }
    static GuiaRemision guia(int id) {
        GuiaRemision g=new GuiaRemision(); g.setIdVenta(id); g.setFechaTraslado(java.sql.Date.valueOf("2026-10-26"));
        g.setPuntoPartida("Tienda QA"); g.setPuntoLlegada("Cliente QA"); g.setEstado("PENDIENTE"); return g;
    }
    public static void main(String[] args) throws Exception {
        usuario=Repositorios.usuarios().buscarPorUsuario("admin").getIdUsuario();
        String sufijo=String.format("%08d",Math.floorMod(System.nanoTime(),100000000));
        Cliente dni=cliente("DNI",sufijo,"Cliente QA "+sufijo),ruc=cliente("RUC","201"+sufijo,"Empresa QA "+sufijo);
        Producto ropa=producto("QA-"+sufijo,"MERCADERIA","10","10"),servicio=producto("QS-"+sufijo,"SERVICIO","0","5"),sinStock=producto("Q0-"+sufijo,"INSUMO","0","2");
        Solped s=pedido(dni,item(ropa,"2"),item(servicio,"1"));
        rechaza(() -> ventas.crearDesdeSolped(s.getIdSolped(),usuario));
        solicitudes.cambiarEstado(s.getIdSolped(),"APROBADA");
        ExecutorService hilos=Executors.newFixedThreadPool(2);
        int idVenta=0,aciertos=0;
        try {
            Callable<Integer> convertir=() -> { try { return ventas.crearDesdeSolped(s.getIdSolped(),usuario); } catch(IllegalArgumentException | SQLException e) { return 0; } };
            List<Future<Integer>> resultados=hilos.invokeAll(List.of(convertir,convertir));
            for(Future<Integer> f:resultados) { int id=f.get(); if(id>0) { idVenta=id; aciertos++; } }
        } finally { hilos.shutdown(); }
        exige(aciertos==1,"La SOLPED se convirtió más de una vez");
        final int id=idVenta;
        igual(productos.buscarPorId(ropa.getIdProducto()).getStock(),"8");
        igual(productos.buscarPorId(servicio.getIdProducto()).getStock(),"0");
        exige("ATENDIDA".equals(solicitudes.buscarPorId(s.getIdSolped()).getEstado()),"SOLPED no atendida");
        Venta v=ventas.buscarPorId(id); igual(v.getSubtotal(),"25"); igual(v.getIgv(),"4.50"); igual(v.getTotal(),"29.50");
        v.setDetalles(new ArrayList<>(List.of(item(ropa,"3"),item(servicio,"2")))); ventas.actualizar(v);
        igual(productos.buscarPorId(ropa.getIdProducto()).getStock(),"7"); igual(ventas.buscarPorId(id).getTotal(),"47.20");
        v.setDetalles(new ArrayList<>(List.of(item(ropa,"999")))); rechaza(() -> ventas.actualizar(v));
        igual(productos.buscarPorId(ropa.getIdProducto()).getStock(),"7"); igual(ventas.buscarPorId(id).getTotal(),"47.20");
        rechaza(() -> guias.guardar(guia(id)));
        rechaza(() -> comprobantes.guardar(comprobante(id,"FACTURA")));
        Comprobante b=comprobante(id,"BOLETA"); comprobantes.guardar(b);
        rechaza(() -> comprobantes.guardar(comprobante(id,"BOLETA")));
        rechaza(() -> ventas.actualizar(v));
        b.setTipo("FACTURA"); rechaza(() -> comprobantes.actualizar(b)); b.setTipo("BOLETA");
        GuiaRemision g=guia(id); guias.guardar(g); rechaza(() -> guias.guardar(guia(id)));
        g.setEstado("ENTREGADA"); g.setPuntoLlegada("Nueva dirección QA"); guias.actualizar(g);
        exige("ENTREGADA".equals(guias.buscarPorId(g.getIdGuia()).getEstado()),"Guía no actualizada");
        rechaza(() -> comprobantes.eliminar(b.getIdComprobante()));
        guias.eliminar(g.getIdGuia()); comprobantes.actualizar(b); comprobantes.eliminar(b.getIdComprobante());
        ventas.anular(id); igual(productos.buscarPorId(ropa.getIdProducto()).getStock(),"10");
        rechaza(() -> ventas.anular(id)); igual(productos.buscarPorId(ropa.getIdProducto()).getStock(),"10");
        ventas.eliminar(id); borrarPedido(s);

        Solped empresa=pedido(ruc,item(ropa,"1")); solicitudes.cambiarEstado(empresa.getIdSolped(),"APROBADA");
        int idEmpresa=ventas.crearDesdeSolped(empresa.getIdSolped(),usuario);
        Comprobante f=comprobante(idEmpresa,"FACTURA"); comprobantes.guardar(f);
        exige("F001".equals(comprobantes.buscarPorId(f.getIdComprobante()).getSerie()),"Serie de factura inválida");
        f.setTipo("BOLETA"); comprobantes.actualizar(f);
        exige("B001".equals(comprobantes.buscarPorId(f.getIdComprobante()).getSerie()),"Cambio de tipo inválido");
        comprobantes.eliminar(f.getIdComprobante()); ventas.anular(idEmpresa); ventas.eliminar(idEmpresa); borrarPedido(empresa);

        Solped insuficiente=pedido(dni,item(ropa,"2"),item(sinStock,"1")); solicitudes.cambiarEstado(insuficiente.getIdSolped(),"APROBADA");
        int ventasAntes=ventas.listar().size(); rechaza(() -> ventas.crearDesdeSolped(insuficiente.getIdSolped(),usuario));
        igual(productos.buscarPorId(ropa.getIdProducto()).getStock(),"10"); exige(ventas.listar().size()==ventasAntes,"Cabecera guardada tras rollback");
        exige("APROBADA".equals(solicitudes.buscarPorId(insuficiente.getIdSolped()).getEstado()),"Estado cambiado tras rollback");
        borrarPedido(insuficiente);
        int pedidosAntes=solicitudes.listar().size(); rechaza(() -> pedido(dni,item(ropa,"1"),item(ropa,"2")));
        exige(solicitudes.listar().size()==pedidosAntes,"Pedido parcial guardado");

        Solped a=pedido(dni,item(ropa,"7")),z=pedido(dni,item(ropa,"7"));
        solicitudes.cambiarEstado(a.getIdSolped(),"APROBADA"); solicitudes.cambiarEstado(z.getIdSolped(),"APROBADA");
        ExecutorService dos=Executors.newFixedThreadPool(2); List<Integer> ids=new ArrayList<>();
        try {
            List<Callable<Integer>> tareas=new ArrayList<>();
            for(Solped p:List.of(a,z)) tareas.add(() -> { try { return ventas.crearDesdeSolped(p.getIdSolped(),usuario); } catch(IllegalArgumentException | SQLException e) { return 0; } });
            for(Future<Integer> resultado:dos.invokeAll(tareas)) if(resultado.get()>0) ids.add(resultado.get());
        } finally { dos.shutdown(); }
        exige(ids.size()==1,"Sobreventa concurrente"); igual(productos.buscarPorId(ropa.getIdProducto()).getStock(),"3");
        for(int creado:ids) { ventas.anular(creado); ventas.eliminar(creado); }
        borrarPedido(a); borrarPedido(z);
        igual(productos.buscarPorId(ropa.getIdProducto()).getStock(),"10");
        productos.eliminar(ropa.getIdProducto()); productos.eliminar(servicio.getIdProducto()); productos.eliminar(sinStock.getIdProducto());
        clientes.eliminar(dni.getIdCliente()); clientes.eliminar(ruc.getIdCliente());
        System.out.println("Ventas: CRUD, IGV, servicios, RUC, unicidad, stock, rollback y concurrencia correctos; registros de prueba eliminados.");
    }
}
