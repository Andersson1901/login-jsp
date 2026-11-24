<!-- Autor: Rodriguez Alvarado Andersson Eduardo -->
# Análisis del patrón Observer en el cambio de estado de una venta

Cuando una venta se emite o se anula, cada dueño de tienda activo recibe un aviso en su panel. Recorrido: `VentaRepositoryJDBC` confirma la transacción → `SujetoVenta.notificar` → `NotificadorDueno` → `NotificacionRepository.notificarDuenos` → tabla `notificacion` → pantalla `/notificaciones`.

Decisiones: los observadores se avisan DESPUÉS del commit, de modo que una venta rechazada o revertida no genera avisos. El fallo de un observador se registra y no revierte la venta. El proyecto no separa las tiendas, así que todos los usuarios DUENO activos reciben cada aviso. La suscripción se hace una sola vez al iniciar la aplicación.

Nombre de archivo: ObservadorVenta.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package observador;
import java.sql.SQLException;

// Observador: recibe cada cambio de estado de una venta, ya confirmado en la base.
public interface ObservadorVenta {
    void actualizar(EventoVenta evento) throws SQLException;
}
```
Explicación: Contrato del observador: recibe un EventoVenta con la venta y sus estados. Puede lanzar SQLException porque los observadores escriben en la base.

Nombre de archivo: EventoVenta.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package observador;
import modelo.Venta;

// Cambio de estado de una venta. Al emitirla, el estado anterior es null.
public final class EventoVenta {
    private final Venta venta;
    private final String estadoAnterior;
    private final String estadoNuevo;
    public EventoVenta(Venta venta,String estadoAnterior,String estadoNuevo) {
        this.venta=venta; this.estadoAnterior=estadoAnterior; this.estadoNuevo=estadoNuevo;
    }
    public Venta getVenta() { return venta; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public String getEstadoNuevo() { return estadoNuevo; }
}
```
Explicación: Objeto inmutable que viaja del sujeto a los observadores. Al emitir una venta el estado anterior es null.

Nombre de archivo: SujetoVenta.java

Bloque 1:
```java
public class SujetoVenta {
    private static final Logger REGISTRO=Logger.getLogger(SujetoVenta.class.getName());
    private final CopyOnWriteArrayList<ObservadorVenta> observadores=new CopyOnWriteArrayList<>();

    private SujetoVenta() { }

    private static class Contenedor {
        private static final SujetoVenta INSTANCIA=new SujetoVenta();
    }

    public static SujetoVenta getInstancia() { return Contenedor.INSTANCIA; }
    public void suscribir(ObservadorVenta observador) { observadores.addIfAbsent(observador); }
    public void cancelar(ObservadorVenta observador) { observadores.remove(observador); }
```
Explicación: El sujeto es único por classloader, con el mismo contenedor estático de ConexionBD. La lista es segura para hilos y suscribir ignora duplicados.

Bloque 2:
```java
// La venta ya está confirmada: el fallo de un observador se registra y no afecta a la venta ni a los demás.
    public void notificar(EventoVenta evento) {
        for(ObservadorVenta observador:observadores) {
            try { observador.actualizar(evento); }
            catch(SQLException | RuntimeException e) { REGISTRO.log(Level.WARNING,"Un observador de ventas falló",e); }
        }
    }
```
Explicación: Recorre los observadores en orden. Si uno falla se registra y se continúa con los demás; la venta ya está confirmada y no se afecta.

Nombre de archivo: NotificadorDueno.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package observador;
import repositorio.NotificacionRepository;
import java.sql.SQLException;

// Observador concreto: deja un aviso a cada dueño de tienda activo cuando cambia el estado de una venta.
public class NotificadorDueno implements ObservadorVenta {
    private final NotificacionRepository notificaciones;
    public NotificadorDueno(NotificacionRepository notificaciones) { this.notificaciones=notificaciones; }
    @Override public void actualizar(EventoVenta evento) throws SQLException {
        notificaciones.notificarDuenos(evento.getVenta(),evento.getEstadoAnterior(),evento.getEstadoNuevo());
    }
}
```
Explicación: Observador concreto. Depende de la interfaz NotificacionRepository, no del JDBC, igual que los servlets.

Nombre de archivo: SuscripcionVentas.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package observador;
import repositorio.Repositorios;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

// Suscribe los observadores una sola vez, al iniciar la aplicación.
@WebListener
public class SuscripcionVentas implements ServletContextListener {
    @Override public void contextInitialized(ServletContextEvent evento) {
        SujetoVenta.getInstancia().suscribir(new NotificadorDueno(Repositorios.notificaciones()));
    }
}
```
Explicación: Es la suscripción: al iniciar la aplicación registra el NotificadorDueno en el sujeto. Agregar otro observador solo requiere otra línea aquí.

Nombre de archivo: VentaRepositoryJDBC.java

Bloque 1:
```java
// Se avisa a los observadores cuando la transacción ya fue confirmada.
    private static void avisar(Venta venta,String estadoAnterior,String estadoNuevo) {
        SujetoVenta.getInstancia().notificar(new EventoVenta(venta,estadoAnterior,estadoNuevo));
    }
```
Explicación: Punto único de publicación de eventos desde el repositorio.

Bloque 2:
```java
return buscar(c,id,false);
        });
        avisar(emitida,null,"EMITIDA");
        return emitida.getIdVenta();
```
Explicación: La transacción devuelve la venta emitida y el aviso se envía recién después del commit, con la misma interfaz pública de antes (devuelve el id).

Bloque 3:
```java
@Override public void anular(Integer id) throws SQLException {
        Venta anulada=OperacionVentaJDBC.transaccion(c -> {
            Venta actual=exigirEmitida(c,id);
            OperacionVentaJDBC.moverStock(c,actual.getDetalles(),false);
            try(PreparedStatement p=c.prepareStatement("UPDATE venta SET estado='ANULADA' WHERE id_venta=?")) { p.setInt(1,id); p.executeUpdate(); }
            try(PreparedStatement p=c.prepareStatement("UPDATE guia_remision SET estado='ANULADA' WHERE id_venta=?")) { p.setInt(1,id); p.executeUpdate(); }
            actual.setEstado("ANULADA");
            return actual;
        });
        avisar(anulada,"EMITIDA","ANULADA");
    }
```
Explicación: La anulación devuelve la venta dentro de la transacción y avisa al confirmarse. El estado anterior siempre es EMITIDA porque exigirEmitida lo garantiza.

Nombre de archivo: NotificacionRepositoryJDBC.java

Bloque 1:
```java
// Una sola sentencia crea el aviso de cada dueño activo; el proyecto no separa las tiendas.
    @Override public void notificarDuenos(Venta venta,String estadoAnterior,String estadoNuevo) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("INSERT INTO notificacion(id_usuario,id_venta,nro_venta,cliente,estado_anterior,estado_nuevo,total) SELECT id_usuario,?,?,?,?,?,? FROM usuarios WHERE rol='DUENO' AND estado='ACTIVO'")) {
            p.setInt(1,venta.getIdVenta()); p.setString(2,venta.getNroVenta()); p.setString(3,venta.getCliente());
            if(estadoAnterior==null) p.setNull(4,Types.VARCHAR); else p.setString(4,estadoAnterior);
            p.setString(5,estadoNuevo); p.setBigDecimal(6,venta.getTotal()); p.executeUpdate();
        }
    }
```
Explicación: Una sentencia INSERT ... SELECT crea el aviso de cada dueño activo. Usa PreparedStatement y una conexión nueva, como el resto de repositorios.

Nombre de archivo: 06_observer_ventas.sql

Bloque 1:
```sql
CREATE TABLE notificacion (
 id_notificacion INT AUTO_INCREMENT PRIMARY KEY, id_usuario INT NOT NULL, id_venta INT NULL,
 nro_venta VARCHAR(30) NOT NULL, cliente VARCHAR(150) NOT NULL,
 estado_anterior ENUM('EMITIDA','ANULADA') NULL, estado_nuevo ENUM('EMITIDA','ANULADA') NOT NULL,
 total DECIMAL(14,2) NOT NULL, fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 leida TINYINT(1) NOT NULL DEFAULT 0,
 FOREIGN KEY(id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
 FOREIGN KEY(id_venta) REFERENCES venta(id_venta) ON DELETE SET NULL,
 INDEX ix_notificacion_usuario(id_usuario,leida,fecha)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
Explicación: Si se elimina un usuario se eliminan sus avisos (CASCADE). Si se elimina una venta, el aviso se conserva con id_venta nulo (SET NULL) y guarda el número y el cliente.

Nombre de archivo: SesionFilter.java

Bloque 1:
```java
if (ruta.equals("/notificaciones") && !"DUENO".equals(sesion.getAttribute("rol"))) {
            response.setStatus(403);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Las notificaciones de ventas son para el dueño de la tienda.");
            return;
        }
```
Explicación: La ruta se limita al rol DUENO en el filtro central, como /usuarios para ADMIN. El filtro ya exige el token CSRF en todo POST.

Nombre de archivo: NotificacionServlet.java

Bloque 1:
```java
@Override protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        try {
            if(!"leer".equals(Web.requerido(request,"accion",20))) throw new IllegalArgumentException("Acción no válida.");
            repositorio.marcarLeidas(usuario(request));
            Web.mensaje(request,"Notificaciones marcadas como leídas.");
        } catch(IllegalArgumentException e) { Web.mensaje(request,e.getMessage()); }
        catch(SQLException e) { log("Error al actualizar notificaciones",e); Web.mensaje(request,"No se pudo completar la operación. Intenta nuevamente."); }
        response.sendRedirect(request.getContextPath()+"/notificaciones");
    }
```
Explicación: Marcar como leídas es un POST con acción validada. El destinatario sale siempre de la sesión, nunca de un parámetro del navegador.

Análisis crítico del código fuente: El patrón desacopla la venta de lo que ocurre después: VentaRepositoryJDBC no conoce al dueño ni a la tabla de avisos, y un segundo observador (bitácora, alerta de stock) se agrega con una línea de suscripción. A cambio, la notificación es síncrona, así que su costo se suma a la petición, y es de mejor esfuerzo: si el observador falla, el aviso se pierde y solo queda el registro en el log; garantizarlo exigiría escribir el aviso dentro de la misma transacción o usar una cola. El sujeto es estado estático global, por lo que las pruebas deben cancelar sus suscripciones. Existe una dependencia circular entre los paquetes repositorio y observador, aceptable en este tamaño. Solo se notifican la emisión y la anulación; no la edición ni la eliminación de una venta.
