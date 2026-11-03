# Autor: Rodriguez Alvarado Andersson Eduardo
"""Prueba HTTP/BD de Etapa 7 en desarrollo. Requiere Tomcat iniciado y el cliente mariadb.
Uso: python3 test/PruebasPublicas.py http://localhost:8080/LoginJSP/
Crea y elimina únicamente registros con un sufijo aleatorio propio.
"""
from pathlib import Path
import http.cookiejar
import re
import secrets
import subprocess
import sys
import tempfile
import urllib.error
import urllib.parse
import urllib.request

RAIZ=Path(__file__).resolve().parents[1]
BASE=sys.argv[1] if len(sys.argv)>1 else 'http://localhost:8080/LoginJSP/'
BASE=BASE.rstrip('/')+'/'
config=RAIZ/'src/conexion.properties'
if not config.exists(): config=RAIZ/'src/conexion.properties.example'
valores={}
for linea in config.read_text().splitlines():
    if '=' in linea and not linea.lstrip().startswith('#'):
        k,v=linea.split('=',1); valores[k.strip()]=v.strip()
url=urllib.parse.urlparse(valores['url'].removeprefix('jdbc:'))

def sql(consulta):
    def escapar(v): return str(v).replace('\\','\\\\').replace('"','\\"')
    with tempfile.NamedTemporaryFile(mode='w',suffix='.cnf') as f:
        f.write('[client]\n'+''.join(k+'="'+escapar(v)+'"\n' for k,v in {
            'host':url.hostname,'port':url.port or 3306,'user':valores['usuario'],
            'password':valores['clave'],'database':url.path.lstrip('/'),
            'default-character-set':'utf8mb4'}.items()))
        f.flush()
        return subprocess.check_output(['mariadb','--defaults-extra-file='+f.name,'-N'],input=consulta,text=True).strip()

class Navegador:
    def __init__(self):
        self.opener=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
    def pedir(self,ruta,datos=None):
        cuerpo=None if datos is None else urllib.parse.urlencode(datos,doseq=True).encode()
        try: respuesta=self.opener.open(BASE+ruta,cuerpo,timeout=20)
        except urllib.error.HTTPError as e: respuesta=e
        with respuesta:
            assert 'charset=utf-8' in respuesta.headers.get('Content-Type','').lower(), ruta
            return respuesta.status,respuesta.url,respuesta.read().decode('utf-8')

def token(html,nombre='csrf'):
    encontrado=re.search(r'name="'+nombre+r'" value="([^"]+)"',html)
    assert encontrado, 'Falta token del formulario'
    return encontrado.group(1)

marca=str(secrets.randbelow(70000000)+10000000)
nombres=['qa7_'+marca+'_'+tipo for tipo in ('comprador','tienda','vendedor','forjado')]
codigo='QA7-'+marca
admin=Navegador()
estado,_,html=admin.pedir('validar.jsp',dict(usuario='admin',clave='1234'))
assert estado==200 and 'Panel principal' in html,'Necesita admin/1234 activo para la demostración'
csrf=token(html)
def cambiar(datos): return admin.pedir('usuarios',dict(datos,csrf=csrf))
def registro(tipo,indice):
    return dict(tipo=tipo,nombres='María <script>prueba</script>',apellidos='Muñoz',dni=str(int(marca)+indice),
                correo=nombres[indice]+'@ejemplo.test',usuario=nombres[indice],clave='Prueba1234',nombreNegocio='Ropa Ñandú')

try:
    anon=Navegador()
    for ruta in ('registro_comprador.jsp','registro_tienda.jsp','solicitud_vendedor.jsp','recuperar.jsp','catalogo','catalogo.jsp'):
        estado,_,html=anon.pedir(ruta); assert estado==200 and 'ModaExpress' in html,ruta
    for ruta in ('usuarios','clientes','proveedores','productos','ventas','solped','guias','comprobantes','resumen'):
        assert anon.pedir(ruta)[1].endswith('login.jsp'),ruta
    assert anon.pedir('catalogo',{'accion':'eliminar','id':'1'})[0]==405
    assert anon.pedir('registro',registro('COMPRADOR',0))[0]==403
    assert anon.pedir('recuperar',{'correo':'admin@modaexpress.test'})[0]==403
    html=anon.pedir('registro_comprador.jsp')[2]
    publico=token(html,'csrfPublico')
    base=registro('COMPRADOR',0)
    for campo,valor in [('dni','123'),('correo','sin-arroba'),('clave','x'),('nombres',''),('tipo','ADMIN'),('usuario','x'*51)]:
        datos=dict(base,csrfPublico=publico); datos[campo]=valor
        assert anon.pedir('registro',datos)[0]==400,campo
    assert anon.pedir('registro',dict(registro('TIENDA',1),nombreNegocio='',csrfPublico=publico))[0]==400
    clientes=[]
    for i,(tipo,rol) in enumerate([('COMPRADOR','CLIENTE'),('TIENDA','DUENO'),('VENDEDOR','VENDEDOR')]):
        datos=dict(registro(tipo,i),csrfPublico=publico)
        estado,destino,html=anon.pedir('registro',datos)
        assert estado==200 and destino.endswith('login.jsp?registro=1') and 'Tu cuenta está activa' in html
        registro_bd=sql("SELECT CONCAT(rol,':',estado,':',intentos_fallidos,':',LEFT(clave_hash,7)) FROM usuarios WHERE usuario='"+nombres[i]+"'")
        assert registro_bd==rol+':ACTIVO:0:120000:',registro_bd
        assert anon.pedir('registro',datos)[0]==409,'No rechazó duplicados'
        cliente=Navegador(); estado,destino,html=cliente.pedir('validar.jsp',dict(usuario=nombres[i],clave='Prueba1234'))
        assert estado==200 and destino.endswith('catalogo' if rol=='CLIENTE' else 'bienvenido.jsp')
        assert cliente.pedir('usuarios')[0]==403
        clientes.append(cliente)
    # Aunque se envíen rol/estado manipulados, el tipo público fija CLIENTE y ACTIVO.
    assert anon.pedir('registro',dict(registro('COMPRADOR',3),rol='ADMIN',estado='INACTIVO',csrfPublico=publico))[0]==200
    assert sql("SELECT CONCAT(rol,':',estado) FROM usuarios WHERE usuario='"+nombres[3]+"'")=='CLIENTE:ACTIVO'
    cliente=clientes[0]
    assert cliente.pedir('bienvenido.jsp')[1].endswith('catalogo')
    for ruta in ('clientes','proveedores','productos','ventas','solped','guias','comprobantes','resumen'):
        assert cliente.pedir(ruta)[0]==403,ruta
    html=cliente.pedir('catalogo')[2]
    assert cliente.pedir('productos',dict(accion='eliminar',id=1,csrf=token(html)))[0]==403
    assert cliente.pedir('logout.jsp',dict(csrf=token(html)))[1].endswith('login.jsp')
    print('Registro de tres perfiles, validaciones, duplicados, hash, CSRF y separación de accesos: OK')

    id_tienda=sql("SELECT id_usuario FROM usuarios WHERE usuario='"+nombres[1]+"'")
    html=admin.pedir('usuarios?accion=editar&id='+id_tienda)[2]
    assert 'Ropa Ñandú' in html and 'value="DUENO" selected' in html
    assert '&lt;script&gt;prueba&lt;/script&gt;' in html and '<script>prueba</script>' not in html
    datos=registro('TIENDA',1)
    datos.update(accion='guardar',id=id_tienda,rol='DUENO',estado='ACTIVO',nombreNegocio='Tienda actualizada')
    assert 'Operación realizada' in cambiar(datos)[2]
    assert sql('SELECT nombre_negocio FROM usuarios WHERE id_usuario='+id_tienda)=='Tienda actualizada'
    datos['estado']='INACTIVO'; assert 'Operación realizada' in cambiar(datos)[2]
    assert clientes[1].pedir('productos')[1].endswith('bloqueado.jsp')
    assert Navegador().pedir('validar.jsp',dict(usuario=nombres[1],clave='Prueba1234'))[1].endswith('bloqueado.jsp')
    datos['estado']='ACTIVO'; assert 'Operación realizada' in cambiar(datos)[2]
    print('ADMIN edita el nombre del negocio y desactiva cuentas; sesiones revocadas: OK')

    recuperar=Navegador(); publico=token(recuperar.pedir('recuperar.jsp')[2],'csrfPublico')
    cuenta=nombres[0]; correo=cuenta+'@ejemplo.test'
    antes=sql("SELECT clave_hash FROM usuarios WHERE usuario='"+cuenta+"'")
    estado,destino,html=recuperar.pedir('recuperar',dict(correo=correo,csrfPublico=publico))
    assert estado==200 and destino.endswith('recuperar.jsp?enviado=1')
    mensaje=re.search(r'<div class="alert alert-success"[^>]*>(.*?)</div>',html,re.S).group(1)
    token_bd=sql("SELECT token_recuperacion FROM usuarios WHERE usuario='"+cuenta+"'")
    assert re.fullmatch('[0-9a-f]{64}',token_bd) and token_bd not in html
    assert sql("SELECT clave_hash FROM usuarios WHERE usuario='"+cuenta+"'")==antes
    assert 800<int(sql("SELECT TIMESTAMPDIFF(SECOND,NOW(),token_expira) FROM usuarios WHERE usuario='"+cuenta+"'"))<=900
    html=recuperar.pedir('recuperar',dict(correo='no-existe-'+marca+'@ejemplo.test',csrfPublico=publico))[2]
    assert mensaje==re.search(r'<div class="alert alert-success"[^>]*>(.*?)</div>',html,re.S).group(1)
    assert recuperar.pedir('recuperar',dict(correo='invalido',csrfPublico=publico))[0]==400
    recuperar.pedir('recuperar',dict(correo=correo,csrfPublico=publico))
    assert sql("SELECT token_recuperacion FROM usuarios WHERE usuario='"+cuenta+"'")!=token_bd
    sql("UPDATE usuarios SET estado='INACTIVO',token_recuperacion=NULL,token_expira=NULL WHERE usuario='"+cuenta+"'")
    recuperar.pedir('recuperar',dict(correo=correo,csrfPublico=publico))
    assert sql("SELECT token_recuperacion IS NULL FROM usuarios WHERE usuario='"+cuenta+"'")=='1'
    print('Recuperación simulada: mensaje genérico, caducidad, renovación y clave intacta: OK')

    sql("INSERT INTO productos(codigo,nombre,descripcion,tipo,categoria,unidad_medida,precio_compra,precio_venta,stock,stock_minimo,estado) VALUES('"+codigo+"','Producto QA <script>ropa</script>','Algodón peruano','PRODUCTO','Pruebas','UND',999.99,45,1,0,'INACTIVO')")
    assert 'Producto QA &lt;script&gt;ropa&lt;/script&gt;' not in anon.pedir('catalogo?q='+codigo)[2]
    sql("UPDATE productos SET estado='ACTIVO' WHERE codigo='"+codigo+"'")
    html=anon.pedir('catalogo?q='+codigo)[2]
    assert codigo in html and '&lt;script&gt;ropa&lt;/script&gt;' in html and '<script>ropa</script>' not in html
    assert '999.99' not in html and 'precio_compra' not in html and 'Eliminar' not in html
    assert anon.pedir('catalogo?q='+urllib.parse.quote("' OR '1'='1"))[0]==200
    assert 'No hay productos activos' in anon.pedir('catalogo?q=no-existe-'+marca)[2]
    print('Catálogo público: activos, búsqueda, escape HTML, precios y solo lectura: OK')
finally:
    sql("DELETE FROM usuarios WHERE usuario IN ("+','.join("'"+x+"'" for x in nombres)+"); DELETE FROM productos WHERE codigo='"+codigo+"';")
    print('Registros temporales retirados.')
