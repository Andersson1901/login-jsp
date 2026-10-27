<!-- Autor: Rodriguez Alvarado Andersson Eduardo -->
# Análisis del módulo de ventas

Flujo académico: SOLPED → VENTA → FACTURA/BOLETA → GUÍA. Los precios de venta se guardan sin IGV; se calcula 18% con BigDecimal y redondeo HALF_UP a dos decimales.

Nombre de archivo: OperacionVentaJDBC.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import conexion.ConexionBD;
import modelo.Detalle;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.*;

// Operaciones compartidas que reciben la MISMA conexión de la transacción.
final class OperacionVentaJDBC {
    private OperacionVentaJDBC() { }
```
Explicación: Agrupa operaciones JDBC internas; todas reciben la conexión de la misma transacción.

Bloque 2:
```java
static <T> T transaccion(Trabajo<T> trabajo) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion()) {
            // Tras esperar un bloqueo, las comprobaciones ven los documentos recién confirmados.
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            c.setAutoCommit(false);
            try {
                T resultado=trabajo.ejecutar(c);
                c.commit();
                return resultado;
            } catch(SQLException | RuntimeException e) {
                try { c.rollback(); } catch(SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
    }
```
Explicación: Confirma el conjunto completo o revierte los cambios. READ_COMMITTED evita leer documentos anteriores a una espera por bloqueo.

Bloque 3:
```java
static int siguiente(Connection c,String serie) throws SQLException {
        int numero;
        try(PreparedStatement p=c.prepareStatement("SELECT ultimo FROM correlativos WHERE serie=? FOR UPDATE")) {
            p.setString(1,serie); try(ResultSet r=p.executeQuery()) {
                if(!r.next()) throw new SQLException("Serie no configurada");
                numero=Math.addExact(r.getInt(1),1);
            }
        }
        try(PreparedStatement p=c.prepareStatement("UPDATE correlativos SET ultimo=? WHERE serie=?")) {
            p.setInt(1,numero); p.setString(2,serie); p.executeUpdate();
        }
        return numero;
    }
```
Explicación: El bloqueo del numerador evita repetir una serie y número entre peticiones simultáneas.

Análisis crítico del código fuente: La transacción mantiene cabecera, detalle, stock y estados consistentes. No sustituye un pool de conexiones ni un sistema de auditoría; los bloqueos pueden producir esperas o interbloqueos, que se revierten y permiten reintentar.

Nombre de archivo: SolpedRepositoryJDBC.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import conexion.ConexionBD;
import modelo.Solped;
import java.sql.*;
import java.util.*;

public class SolpedRepositoryJDBC implements SolpedRepository {
    private static final String CONSULTA="SELECT s.*,c.nombres_razon_social cliente FROM solped s JOIN clientes c ON c.id_cliente=s.id_cliente";
    private Solped mapear(ResultSet r) throws SQLException {
```
Explicación: La implementación separa la consulta de solicitudes de la capa HTTP.

Bloque 2:
```java
@Override public void guardar(Solped s) throws SQLException {
        OperacionVentaJDBC.transaccion(c -> {
            OperacionVentaJDBC.clienteActivo(c,s.getIdCliente());
            OperacionVentaJDBC.preparar(c,s.getDetalles());
            s.setNroSolped(String.format("SP-%06d",OperacionVentaJDBC.siguiente(c,"SP")));
            try(PreparedStatement p=c.prepareStatement("INSERT INTO solped(nro_solped,id_cliente,observacion,id_usuario) VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                p.setString(1,s.getNroSolped()); p.setInt(2,s.getIdCliente()); p.setString(3,s.getObservacion()); p.setInt(4,s.getIdUsuario());
                p.executeUpdate(); s.setIdSolped(OperacionVentaJDBC.generado(p));
            }
            OperacionVentaJDBC.guardarDetalles(c,false,s.getIdSolped(),s.getDetalles()); return null;
        });
    }
```
Explicación: Guarda la cabecera y los ítems en una transacción y obtiene precios del catálogo, no del navegador.

Análisis crítico del código fuente: Repository persiste el agregado cabecera-detalle. Una solicitud no reserva stock; la disponibilidad se verifica al convertirla. Solo se edita cuando está pendiente y las solicitudes atendidas conservan su referencia a la venta.

Nombre de archivo: VentaRepositoryJDBC.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import conexion.ConexionBD;
import modelo.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class VentaRepositoryJDBC implements VentaRepository {
    private static final String CONSULTA="SELECT v.*,s.nro_solped,c.nombres_razon_social cliente,c.tipo_documento,c.nro_documento FROM venta v JOIN solped s ON s.id_solped=v.id_solped JOIN clientes c ON c.id_cliente=v.id_cliente";
```
Explicación: El repositorio reúne operaciones de venta y su detalle.

Bloque 2:
```java
@Override public int crearDesdeSolped(Integer idSolped,Integer idUsuario) throws SQLException {
        return OperacionVentaJDBC.transaccion(c -> {
            Solped s=new SolpedRepositoryJDBC().buscar(c,idSolped,true);
            if(s==null || !"APROBADA".equals(s.getEstado())) throw new IllegalArgumentException("Solo una SOLPED APROBADA puede convertirse en venta.");
            OperacionVentaJDBC.clienteActivo(c,s.getIdCliente());
            if(s.getDetalles().isEmpty()) throw new IllegalArgumentException("La SOLPED no tiene productos.");
            OperacionVentaJDBC.moverStock(c,s.getDetalles(),true);
            BigDecimal subtotal=OperacionVentaJDBC.subtotal(s.getDetalles()), igv=OperacionVentaJDBC.igv(subtotal);
```
Explicación: Bloquea la SOLPED aprobada, valida cliente y descuenta existencias antes de calcular los importes.

Bloque 3:
```java
OperacionVentaJDBC.guardarDetalles(c,true,id,s.getDetalles());
            try(PreparedStatement p=c.prepareStatement("UPDATE solped SET estado='ATENDIDA' WHERE id_solped=?")) { p.setInt(1,idSolped); p.executeUpdate(); }
            return id;
        });
    }
```
Explicación: El detalle y el estado ATENDIDA se confirman junto con la cabecera, sin estados intermedios visibles.

Análisis crítico del código fuente: Los bloqueos y la unicidad por SOLPED impiden ventas duplicadas y stock negativo por concurrencia. Editar antes de emitir documentos reemplaza el detalle con precios actuales; el pedido original se conserva como antecedente. La anulación restituye stock una sola vez.

Nombre de archivo: ComprobanteRepositoryJDBC.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import conexion.ConexionBD;
import modelo.*;
import java.sql.*;
import java.util.*;

public class ComprobanteRepositoryJDBC implements ComprobanteRepository {
    private static final String CONSULTA="SELECT d.*,v.nro_venta FROM comprobante d JOIN venta v ON v.id_venta=d.id_venta";
```
Explicación: Implementa altas, consultas, cambios de tipo y bajas del documento académico.

Bloque 2:
```java
private String serie(Comprobante d,Venta venta) {
        if(!Set.of("FACTURA","BOLETA").contains(d.getTipo())) throw new IllegalArgumentException("Elige FACTURA o BOLETA.");
        if("FACTURA".equals(d.getTipo()) && (!"RUC".equals(venta.getTipoDocumento()) || !venta.getNroDocumento().matches("[0-9]{11}")))
            throw new IllegalArgumentException("La FACTURA exige un cliente con RUC de 11 dígitos.");
        return "FACTURA".equals(d.getTipo())?"F001":"B001";
    }
```
Explicación: Factura exige RUC; boleta admite cualquier cliente. La serie se decide en el servidor.

Análisis crítico del código fuente: El total proviene de la venta bloqueada y una restricción única permite un solo comprobante. Cambiar de tipo asigna un nuevo número de la nueva serie; eliminar no reutiliza números. No implementa emisión electrónica, anulaciones tributarias ni archivos fiscales.

Nombre de archivo: GuiaRepositoryJDBC.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import conexion.ConexionBD;
import modelo.*;
import java.sql.*;
import java.util.*;

public class GuiaRepositoryJDBC implements GuiaRepository {
    private static final String CONSULTA="SELECT g.*,v.nro_venta FROM guia_remision g JOIN venta v ON v.id_venta=g.id_venta";
```
Explicación: El repositorio administra los datos de traslado.

Bloque 2:
```java
@Override public void guardar(GuiaRemision g) throws SQLException {
        validar(g);
        OperacionVentaJDBC.transaccion(c -> {
            Venta v=VentaRepositoryJDBC.exigirEmitida(c,g.getIdVenta());
            try(PreparedStatement p=c.prepareStatement("SELECT id_comprobante FROM comprobante WHERE id_venta=?")) {
                p.setInt(1,v.getIdVenta()); try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new IllegalArgumentException("Emite el comprobante antes de crear la guía."); }
            }
```
Explicación: Valida los campos y exige venta emitida con comprobante antes de asignar el número T001.

Análisis crítico del código fuente: El orden de documentos se valida en la transacción y la unicidad evita dos guías por venta. Las direcciones se guardan como texto y no se integra transporte externo; el catálogo de clientes se comparte con ventas y no hay una copia histórica completa de sus datos.

Nombre de archivo: VentaServlet.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;

import modelo.*;
import repositorio.*;
import util.Web;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet({"/solped","/ventas","/comprobantes","/guias"})
public class VentaServlet extends HttpServlet {
    private final SolpedRepository solicitudes=Repositorios.solped();
    private final VentaRepository ventas=Repositorios.ventas();
    private final ComprobanteRepository comprobantes=Repositorios.comprobantes();
    private final GuiaRepository guias=Repositorios.guias();
    private final ClienteRepository clientes=Repositorios.clientes();
    private final ProductoRepository productos=Repositorios.productos();

    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
```
Explicación: El controlador enlaza las cuatro pantallas con interfaces Repository.

Bloque 2:
```java
private List<Detalle> detalles(HttpServletRequest request) {
        String[] ids=request.getParameterValues("idProducto"), cantidades=request.getParameterValues("cantidad");
        if(ids==null || cantidades==null || ids.length!=cantidades.length || ids.length==0 || ids.length>100) throw new IllegalArgumentException("Agrega entre 1 y 100 ítems completos.");
        List<Detalle> lista=new ArrayList<>();
        try {
            for(int i=0;i<ids.length;i++) {
                if(ids[i].length()>10 || cantidades[i].length()>20) throw new NumberFormatException();
                Detalle d=new Detalle(); d.setIdProducto(Integer.parseInt(ids[i])); d.setCantidad(new BigDecimal(cantidades[i])); lista.add(d);
            }
        } catch(NumberFormatException e) { throw new IllegalArgumentException("Revisa los productos y cantidades."); }
        return lista;
    }
```
Explicación: Recibe productos y cantidades, limita los ítems y rechaza datos mal formados; los precios se consultan en el servidor.

Análisis crítico del código fuente: Mantiene HTTP y navegación fuera del SQL. Un solo servlet reduce archivos en esta tarea, aunque crece con cada módulo; en una aplicación mayor conviene dividir controladores y reglas en servicios.
