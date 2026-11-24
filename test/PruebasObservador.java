// Autor: Rodriguez Alvarado Andersson Eduardo
import modelo.*;
import observador.*;
import repositorio.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

// La primera parte no usa la base; con --sin-bd solo se ejecuta esa parte.
// La segunda parte, solo sobre la base de desarrollo con el script 06 aplicado, crea y elimina sus propios registros.
public class PruebasObservador {
    interface Accion { void ejecutar() throws Exception; }
    static void rechaza(Accion accion) throws Exception {
        try { accion.ejecutar(); } catch(IllegalArgumentException | SQLException e) { return; }
        throw new AssertionError("La operación inválida fue aceptada");
    }
    static void exige(boolean valor,String mensaje) { if(!valor) throw new AssertionError(mensaje); }
    static void igual(BigDecimal actual,String esperado) {
        if(actual.compareTo(new BigDecimal(esperado))!=0) throw new AssertionError(actual+" != "+esperado);
    }
    static Venta venta(String numero) {
        Venta v=new Venta(); v.setIdVenta(1); v.setNroVenta(numero); v.setEstado("EMITIDA"); return v;
    }
    static void sujeto() {
        SujetoVenta sujeto=SujetoVenta.getInstancia(); List<String> recibidos=new ArrayList<>();
        ObservadorVenta registro=e -> recibidos.add(e.getEstadoAnterior()+">"+e.getEstadoNuevo()+":"+e.getVenta().getNroVenta());
        ObservadorVenta fallaSql=e -> { throw new SQLException("Fallo simulado"); };
        ObservadorVenta fallaEjecucion=e -> { throw new IllegalStateException("Fallo simulado"); };
        sujeto.suscribir(fallaSql); sujeto.suscribir(fallaEjecucion); sujeto.suscribir(registro); sujeto.suscribir(registro);
        sujeto.notificar(new EventoVenta(venta("V-1"),null,"EMITIDA"));
        sujeto.notificar(new EventoVenta(venta("V-1"),"EMITIDA","ANULADA"));
        exige(recibidos.equals(List.of("null>EMITIDA:V-1","EMITIDA>ANULADA:V-1")),"Eventos incorrectos: "+recibidos);
        sujeto.cancelar(registro); sujeto.notificar(new EventoVenta(venta("V-2"),null,"EMITIDA"));
        exige(recibidos.size()==2,"Un observador cancelado recibió eventos");
        sujeto.cancelar(fallaSql); sujeto.cancelar(fallaEjecucion);
    }
    static Usuario usuario(String rol,String estado,String indice,String sufijo) throws Exception {
        Usuario u=new Usuario(); u.setNombres("Observador"); u.setApellidos("QA "+indice); u.setDni(indice+sufijo.substring(1));
        u.setCorreo("obsqa"+indice+sufijo+"@ejemplo.test"); u.setUsuario("obsqa"+indice+sufijo); u.setRol(rol); u.setEstado(estado);
        u.setClaveHash(util.Seguridad.generarHash("clave-prueba")); if("DUENO".equals(rol)) u.setNombreNegocio("Tienda QA");
        Repositorios.usuarios().guardar(u); return u;
    }
    static Solped pedido(Cliente c,Producto p,String cantidad,int usuario) throws Exception {
        Detalle d=new Detalle(); d.setIdProducto(p.getIdProducto()); d.setCantidad(new BigDecimal(cantidad));
        Solped s=new Solped(); s.setIdCliente(c.getIdCliente()); s.setIdUsuario(usuario); s.setObservacion("Prueba automática del Observer");
        s.setDetalles(new ArrayList<>(List.of(d))); Repositorios.solped().guardar(s);
        Repositorios.solped().cambiarEstado(s.getIdSolped(),"APROBADA"); return s;
    }
    static void borrarPedido(Solped s) throws Exception {
        Repositorios.solped().cambiarEstado(s.getIdSolped(),"ANULADA"); Repositorios.solped().eliminar(s.getIdSolped());
    }
    public static void main(String[] args) throws Exception {
        sujeto();
        System.out.println("Observer: suscripción, cancelación, duplicados y aislamiento de fallos correctos.");
        if(args.length>0 && "--sin-bd".equals(args[0])) return;
        UsuarioRepository usuarios=Repositorios.usuarios(); NotificacionRepository avisos=Repositorios.notificaciones();
        VentaRepository ventas=Repositorios.ventas(); ClienteRepository clientes=Repositorios.clientes(); ProductoRepository productos=Repositorios.productos();
        int admin=usuarios.buscarPorUsuario("admin").getIdUsuario();
        String sufijo=String.format("%08d",Math.floorMod(System.nanoTime(),100000000));
        Usuario dueno=usuario("DUENO","ACTIVO","1",sufijo),inactivo=usuario("DUENO","INACTIVO","2",sufijo),vendedor=usuario("VENDEDOR","ACTIVO","3",sufijo);
        Cliente cliente=new Cliente(); cliente.setTipoDocumento("DNI"); cliente.setNroDocumento(sufijo); cliente.setNombresRazonSocial("Cliente Observer "+sufijo);
        cliente.setDireccion("Dirección de prueba"); cliente.setTelefono("900000000"); cliente.setCorreo("obsqa@ejemplo.test"); cliente.setEstado("ACTIVO"); clientes.guardar(cliente);
        Producto producto=new Producto(); producto.setCodigo("OB-"+sufijo); producto.setNombre("OB-"+sufijo); producto.setDescripcion("Artículo creado por la prueba del Observer");
        producto.setTipo("MERCADERIA"); producto.setCategoria("Pruebas"); producto.setUnidadMedida("UND"); producto.setPrecioCompra(BigDecimal.ZERO);
        producto.setPrecioVenta(new BigDecimal("10")); producto.setStock(new BigDecimal("10")); producto.setStockMinimo(BigDecimal.ZERO); producto.setEstado("ACTIVO"); productos.guardar(producto);
        // En la aplicación suscribe SuscripcionVentas al iniciar; la prueba lo hace de forma explícita.
        NotificadorDueno notificador=new NotificadorDueno(avisos);
        SujetoVenta.getInstancia().suscribir(notificador);
        try {
            Solped s=pedido(cliente,producto,"2",admin); int id=ventas.crearDesdeSolped(s.getIdSolped(),admin);
            List<Notificacion> lista=avisos.listarPorUsuario(dueno.getIdUsuario());
            exige(lista.size()==1,"El dueño debe recibir un aviso al emitir la venta");
            Notificacion emitida=lista.get(0);
            exige(emitida.getEstadoAnterior()==null && "EMITIDA".equals(emitida.getEstadoNuevo()),"Estados del aviso de emisión incorrectos");
            exige(emitida.getIdVenta()==id && ventas.buscarPorId(id).getNroVenta().equals(emitida.getNroVenta()),"Venta del aviso incorrecta");
            exige(("Cliente Observer "+sufijo).equals(emitida.getCliente()) && !emitida.isLeida(),"Datos del aviso incorrectos");
            igual(emitida.getTotal(),"23.60");
            exige(avisos.listarPorUsuario(inactivo.getIdUsuario()).isEmpty() && avisos.listarPorUsuario(vendedor.getIdUsuario()).isEmpty(),"Solo los dueños activos reciben avisos");
            ventas.anular(id); lista=avisos.listarPorUsuario(dueno.getIdUsuario());
            exige(lista.size()==2,"El dueño debe recibir un aviso al anular la venta");
            exige("EMITIDA".equals(lista.get(0).getEstadoAnterior()) && "ANULADA".equals(lista.get(0).getEstadoNuevo()),"Estados del aviso de anulación incorrectos");
            exige(avisos.contarNoLeidas(dueno.getIdUsuario())==2,"Conteo de avisos sin leer incorrecto");
            avisos.marcarLeidas(dueno.getIdUsuario()); exige(avisos.contarNoLeidas(dueno.getIdUsuario())==0,"Los avisos no se marcaron como leídos");
            Solped excesiva=pedido(cliente,producto,"999",admin);
            rechaza(() -> ventas.crearDesdeSolped(excesiva.getIdSolped(),admin));
            exige(avisos.listarPorUsuario(dueno.getIdUsuario()).size()==2,"Una venta rechazada no debe notificar");
            borrarPedido(excesiva);
            ventas.eliminar(id); lista=avisos.listarPorUsuario(dueno.getIdUsuario());
            exige(lista.size()==2 && lista.get(0).getIdVenta()==0 && lista.get(1).getIdVenta()==0,"Los avisos deben conservarse sin enlace tras eliminar la venta");
            borrarPedido(s);
        } finally { SujetoVenta.getInstancia().cancelar(notificador); }
        productos.eliminar(producto.getIdProducto()); clientes.eliminar(cliente.getIdCliente());
        usuarios.eliminar(dueno.getIdUsuario()); usuarios.eliminar(inactivo.getIdUsuario()); usuarios.eliminar(vendedor.getIdUsuario());
        System.out.println("Observer de ventas: avisos al dueño activo, estados, sin leer, rollback sin aviso y eliminación de venta correctos; registros de prueba eliminados.");
    }
}
