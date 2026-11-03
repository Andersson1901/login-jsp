// Autor: Rodriguez Alvarado Andersson Eduardo
package util;

import javax.servlet.http.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public final class FormularioPublico {
    private FormularioPublico() { }
    public static String token(HttpServletRequest request) {
        HttpSession sesion=request.getSession(true);
        synchronized(sesion) {
            String token=(String)sesion.getAttribute("csrfPublico");
            if(token==null) {
                token=java.util.UUID.randomUUID().toString();
                sesion.setAttribute("csrfPublico",token);
            }
            return token;
        }
    }
    public static boolean valido(HttpServletRequest request) {
        HttpSession sesion=request.getSession(false);
        String recibido=request.getParameter("csrfPublico");
        if(sesion==null || recibido==null || recibido.length()>100) return false;
        Object esperado=sesion.getAttribute("csrfPublico");
        return esperado instanceof String && MessageDigest.isEqual(
                ((String)esperado).getBytes(StandardCharsets.UTF_8),recibido.getBytes(StandardCharsets.UTF_8));
    }
}
