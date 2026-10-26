// Autor: Rodriguez Alvarado Andersson Eduardo
import util.Seguridad;
import conexion.ConexionBD;
import java.sql.Connection;
import java.util.concurrent.*;

public class PruebasSeguridad {
    public static void main(String[] args) throws Exception {
        String primero = Seguridad.generarHash("1234");
        String segundo = Seguridad.generarHash("1234");
        if (primero.equals(segundo)) throw new AssertionError("La sal debe cambiar");
        if (!Seguridad.verificar("1234", primero)) throw new AssertionError("Clave correcta rechazada");
        if (Seguridad.verificar("otro", primero)) throw new AssertionError("Clave incorrecta aceptada");
        if (Seguridad.verificar("1234", "malformado")) throw new AssertionError("Hash inválido aceptado");
        ExecutorService hilos = Executors.newFixedThreadPool(8);
        try {
            for (int i = 0; i < 24; i++) {
                Future<ConexionBD> resultado = hilos.submit(ConexionBD::getInstancia);
                if (resultado.get() != ConexionBD.getInstancia()) throw new AssertionError("Dos instancias");
            }
        } finally { hilos.shutdown(); }
        try (Connection a = ConexionBD.getInstancia().getConexion();
             Connection b = ConexionBD.getInstancia().getConexion()) {
            if (a == b) throw new AssertionError("Conexión compartida");
        }
        System.out.println("PBKDF2 y Singleton: pruebas correctas");
    }
}
