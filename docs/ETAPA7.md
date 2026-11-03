<!-- Autor: Rodriguez Alvarado Andersson Eduardo -->
# Etapa 7 — resultado

| Parte del plan | Antigravity había dejado | Resultado final |
|---|---|---|
| BD y script 05 | Creados y ya aplicados | Verificados; sin repetir migración |
| Usuario.java | Negocio y campos de token | Conservados |
| UsuarioRepositoryJDBC | Solo lectura del negocio | Alta, edición y recuperación completadas |
| Registro comprador, tienda, vendedor | Pendientes | Tres formularios y RegistroServlet |
| Recuperación simulada | Pendiente | recuperar.jsp y RecuperarServlet |
| Catálogo público | Pendiente | catalogo.jsp y CatalogoServlet con búsqueda de activos |
| Enlaces del login y filtro | Pendientes | Integrados con control de roles y CSRF |
| CRUD ADMIN | Sin roles nuevos | Edita cuatro roles, negocio y estado |

Todas las altas públicas quedan ACTIVO. CLIENTE accede al catálogo; DUENO y VENDEDOR al panel operativo; ADMIN además administra usuarios. La tienda se identifica por nombre_negocio, sin inventarios independientes por dueño.

La recuperación es una simulación explícita: no envía correo ni cambia la contraseña. El mismo mensaje se muestra con correo existente o desconocido. Para cuentas activas guarda un hash de token con 15 minutos de vigencia.

Para probar en NetBeans: Limpiar y construir, Ejecutar y abrir http://localhost:8080/LoginJSP/login.jsp. El login contiene los cinco enlaces. Admin sigue siendo admin/1234. El script 05 ya está aplicado en esta PC; para otra base se ejecuta después de 01–04.

Prueba adicional: `python3 test/PruebasPublicas.py http://localhost:8080/LoginJSP/`. Requiere el cliente mariadb y usa la conexión local privada. Los datos temporales se eliminan automáticamente.

Las etapas previas siguen conservadas en el historial. La normalización a tablas separadas de roles, permisos y datos personales se preparó anteriormente como borrador, pero no llegó a aplicarse. Esta entrega corresponde al plan de Etapa 7 con ENUM rol.
