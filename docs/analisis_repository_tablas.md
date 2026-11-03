<!-- Autor: Rodriguez Alvarado Andersson Eduardo -->
# Repository de clientes, proveedores y productos

Nombre de archivo: ClienteRepository.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import modelo.Cliente;
import java.sql.SQLException;
import java.util.List;

public interface ClienteRepository extends Repository<Cliente, Integer> {
    List<Cliente> listar(String busqueda) throws SQLException;
}
```
Explicación: Especializa Repository con búsqueda textual del catálogo.

Análisis crítico del código fuente: El contrato evita que las vistas conozcan SQL. Listar todo resulta simple para los ocho ejemplos, pero necesita paginación si aumenta el catálogo.

Nombre de archivo: ClienteRepositoryJDBC.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import conexion.ConexionBD;
import modelo.Cliente;
import java.sql.*;
import java.util.*;

public class ClienteRepositoryJDBC implements ClienteRepository {
    private Cliente mapear(ResultSet r) throws SQLException {
        Cliente entidad = new Cliente();
        entidad.setIdCliente(r.getInt("id_cliente"));
        entidad.setTipoDocumento(r.getString("tipo_documento"));
```
Explicación: La implementación convierte columnas de la tabla en una entidad.

Bloque 2:
```java
@Override public void guardar(Cliente entidad) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("INSERT INTO clientes(tipo_documento,nro_documento,nombres_razon_social,direccion,telefono,correo,estado) VALUES(?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
            parametros(p,entidad); p.executeUpdate();
            try(ResultSet r=p.getGeneratedKeys()) { if(r.next()) entidad.setIdCliente(r.getInt(1)); }
        }
    }
```
Explicación: El alta usa PreparedStatement, recupera el ID generado y cierra todos los recursos.

Análisis crítico del código fuente: Repository concentra el almacenamiento de documentos de clientes. Las restricciones únicas en BD cubren solicitudes concurrentes; el borrado de registros usados se rechaza por claves foráneas y se ofrece inactivarlos.

Nombre de archivo: ClienteServlet.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;
import modelo.Cliente;
import repositorio.ClienteRepository;
import repositorio.Repositorios;
import util.Web;
import java.io.IOException;
import java.sql.SQLException;
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/clientes")
public class ClienteServlet extends HttpServlet {
    private final ClienteRepository repositorio = Repositorios.clientes();
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        try {
```
Explicación: El controlador recibe HTTP y trabaja con la interfaz del catálogo.

Análisis crítico del código fuente: El servlet valida formatos antes de delegar; la BD conserva restricciones de integridad. Las validaciones se concentran aquí y en Web, por lo que otro consumidor del Repository debe respetar el mismo contrato.

Nombre de archivo: ProveedorRepository.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import modelo.Proveedor;
import java.sql.SQLException;
import java.util.List;

public interface ProveedorRepository extends Repository<Proveedor, Integer> {
    List<Proveedor> listar(String busqueda) throws SQLException;
}
```
Explicación: Especializa Repository con búsqueda textual del catálogo.

Análisis crítico del código fuente: El contrato evita que las vistas conozcan SQL. Listar todo resulta simple para los ocho ejemplos, pero necesita paginación si aumenta el catálogo.

Nombre de archivo: ProveedorRepositoryJDBC.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import conexion.ConexionBD;
import modelo.Proveedor;
import java.sql.*;
import java.util.*;

public class ProveedorRepositoryJDBC implements ProveedorRepository {
    private Proveedor mapear(ResultSet r) throws SQLException {
        Proveedor entidad = new Proveedor();
        entidad.setIdProveedor(r.getInt("id_proveedor"));
        entidad.setRuc(r.getString("ruc"));
```
Explicación: La implementación convierte columnas de la tabla en una entidad.

Bloque 2:
```java
@Override public void guardar(Proveedor entidad) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("INSERT INTO proveedores(ruc,razon_social,contacto,telefono,correo,direccion,estado) VALUES(?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
            parametros(p,entidad); p.executeUpdate();
            try(ResultSet r=p.getGeneratedKeys()) { if(r.next()) entidad.setIdProveedor(r.getInt(1)); }
        }
    }
```
Explicación: El alta usa PreparedStatement, recupera el ID generado y cierra todos los recursos.

Análisis crítico del código fuente: Repository concentra el almacenamiento de RUC de proveedores. Las restricciones únicas en BD cubren solicitudes concurrentes; el borrado de registros usados se rechaza por claves foráneas y se ofrece inactivarlos.

Nombre de archivo: ProveedorServlet.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;
import modelo.Proveedor;
import repositorio.ProveedorRepository;
import repositorio.Repositorios;
import util.Web;
import java.io.IOException;
import java.sql.SQLException;
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/proveedores")
public class ProveedorServlet extends HttpServlet {
    private final ProveedorRepository repositorio = Repositorios.proveedores();
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        try {
```
Explicación: El controlador recibe HTTP y trabaja con la interfaz del catálogo.

Análisis crítico del código fuente: El servlet valida formatos antes de delegar; la BD conserva restricciones de integridad. Las validaciones se concentran aquí y en Web, por lo que otro consumidor del Repository debe respetar el mismo contrato.

Nombre de archivo: ProductoRepository.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import modelo.Producto;
import java.sql.SQLException;
import java.util.List;

public interface ProductoRepository extends Repository<Producto, Integer> {
    List<Producto> listarActivos(String busqueda) throws SQLException;
    List<Producto> listar(String busqueda) throws SQLException;
}
```
Explicación: Especializa Repository con búsqueda textual del catálogo.

Análisis crítico del código fuente: El contrato evita que las vistas conozcan SQL. Listar todo resulta simple para los ocho ejemplos, pero necesita paginación si aumenta el catálogo.

Nombre de archivo: ProductoRepositoryJDBC.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import conexion.ConexionBD;
import modelo.Producto;
import java.sql.*;
import java.util.*;

public class ProductoRepositoryJDBC implements ProductoRepository {
    private Producto mapear(ResultSet r) throws SQLException {
        Producto entidad = new Producto();
        entidad.setIdProducto(r.getInt("id_producto"));
        entidad.setCodigo(r.getString("codigo"));
```
Explicación: La implementación convierte columnas de la tabla en una entidad.

Bloque 2:
```java
@Override public void guardar(Producto entidad) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("INSERT INTO productos(codigo,nombre,descripcion,tipo,categoria,unidad_medida,precio_compra,precio_venta,stock,stock_minimo,estado) VALUES(?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
            parametros(p,entidad); p.executeUpdate();
            try(ResultSet r=p.getGeneratedKeys()) { if(r.next()) entidad.setIdProducto(r.getInt(1)); }
        }
    }
```
Explicación: El alta usa PreparedStatement, recupera el ID generado y cierra todos los recursos.

Análisis crítico del código fuente: Repository concentra el almacenamiento de códigos, precios y existencias. Las restricciones únicas en BD cubren solicitudes concurrentes; el borrado de registros usados se rechaza por claves foráneas y se ofrece inactivarlos.

Nombre de archivo: ProductoServlet.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;
import modelo.Producto;
import repositorio.ProductoRepository;
import repositorio.Repositorios;
import util.Web;
import java.io.IOException;
import java.sql.SQLException;
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/productos")
public class ProductoServlet extends HttpServlet {
    private final ProductoRepository repositorio = Repositorios.productos();
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        try {
```
Explicación: El controlador recibe HTTP y trabaja con la interfaz del catálogo.

Análisis crítico del código fuente: El servlet valida formatos antes de delegar; la BD conserva restricciones de integridad. Las validaciones se concentran aquí y en Web, por lo que otro consumidor del Repository debe respetar el mismo contrato.
