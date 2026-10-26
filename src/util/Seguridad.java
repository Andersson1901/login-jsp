// Autor: Rodriguez Alvarado Andersson Eduardo
package util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Seguridad {
    private static final int ITERACIONES = 120000;
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private Seguridad() { }

    public static String generarHash(String clave) {
        if (clave == null || clave.isEmpty() || clave.length() > 128)
            throw new IllegalArgumentException("La clave debe tener entre 1 y 128 caracteres.");
        byte[] sal = new byte[16];
        ALEATORIO.nextBytes(sal);
        byte[] hash = derivar(clave, sal, ITERACIONES);
        return ITERACIONES + ":" + Base64.getEncoder().encodeToString(sal)
                + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verificar(String clave, String almacenado) {
        if (clave == null || clave.length() > 128 || almacenado == null) return false;
        try {
            String[] partes = almacenado.split(":", -1);
            if (partes.length != 3) return false;
            int iteraciones = Integer.parseInt(partes[0]);
            if (iteraciones < ITERACIONES || iteraciones > 1000000) return false;
            byte[] sal = Base64.getDecoder().decode(partes[1]);
            byte[] esperado = Base64.getDecoder().decode(partes[2]);
            if (sal.length != 16 || esperado.length != 32) return false;
            return MessageDigest.isEqual(esperado, derivar(clave, sal, iteraciones));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(String clave, byte[] sal, int iteraciones) {
        char[] caracteres = clave.toCharArray();
        PBEKeySpec especificacion = new PBEKeySpec(caracteres, sal, iteraciones, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No está disponible PBKDF2WithHmacSHA256", e);
        } finally {
            especificacion.clearPassword();
            Arrays.fill(caracteres, '\0');
        }
    }

    public static void main(String[] args) {
        if (System.console() == null) {
            System.err.println("Ejecuta esta clase desde una terminal para introducir la clave sin mostrarla.");
            return;
        }
        char[] clave = System.console().readPassword("Clave: ");
        if (clave == null) return;
        try { System.out.println(generarHash(new String(clave))); }
        finally { Arrays.fill(clave, '\0'); }
    }
}
