<%-- 
    Document   : login
    Created on : 29 set. 2026, 23:14:18
    Author     : ander
--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>ModaExpress Perú | Iniciar sesión</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: "Segoe UI", Roboto, Arial, sans-serif;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
            background: linear-gradient(135deg, #0d1f4d, #1b3a8a);
        }
        .card {
            display: flex;
            width: 100%;
            max-width: 920px;
            background: #fff;
            border-radius: 20px;
            overflow: hidden;
            box-shadow: 0 25px 60px rgba(0, 0, 0, 0.35);
        }
        .brand {
            flex: 1;
            padding: 44px 32px;
            color: #fff;
            text-align: center;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background: linear-gradient(160deg, #0d1f4d, #16327a);
        }
        .brand svg { width: 130px; height: 130px; margin-bottom: 14px; }
        .brand h1 { font-size: 28px; letter-spacing: 1px; }
        .brand h1 span { color: #e0b83c; }
        .brand p { margin-top: 6px; font-size: 14px; color: #c9d6f2; }
        .badges { margin-top: 20px; display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
        .badges span {
            font-size: 12px;
            padding: 5px 12px;
            border-radius: 20px;
            border: 1px solid #e0b83c;
            color: #e0b83c;
        }
        .flow { margin-top: 26px; display: flex; align-items: center; gap: 6px; font-size: 11px; color: #c9d6f2; }
        .flow div {
            background: rgba(255, 255, 255, 0.1);
            padding: 6px 8px;
            border-radius: 8px;
            line-height: 1.2;
        }
        .flow b { color: #e0b83c; }
        .form-side { flex: 1; padding: 48px 40px; display: flex; flex-direction: column; justify-content: center; }
        .form-side h2 { color: #0d1f4d; font-size: 26px; }
        .form-side .sub { color: #6b7691; font-size: 14px; margin: 6px 0 26px; }
        label { display: block; font-size: 13px; font-weight: 600; color: #0d1f4d; margin-bottom: 6px; }
        .field { position: relative; margin-bottom: 18px; }
        input {
            width: 100%;
            padding: 13px 14px;
            border: 1.5px solid #d5dcec;
            border-radius: 10px;
            font-size: 15px;
            outline: none;
            transition: border-color 0.2s, box-shadow 0.2s;
        }
        input:focus { border-color: #1b3a8a; box-shadow: 0 0 0 3px rgba(27, 58, 138, 0.15); }
        .ver {
            position: absolute;
            right: 12px;
            bottom: 12px;
            background: none;
            border: none;
            color: #1b3a8a;
            font-size: 12px;
            font-weight: 600;
            cursor: pointer;
        }
        .btn {
            width: 100%;
            padding: 14px;
            border: none;
            border-radius: 10px;
            font-size: 16px;
            font-weight: 700;
            color: #0d1f4d;
            cursor: pointer;
            background: #e0b83c;
            transition: transform 0.15s, background 0.2s;
        }
        .btn:hover { background: #ebc94f; transform: translateY(-1px); }
        .error {
            background: #fdecec;
            color: #b3261e;
            border-left: 4px solid #b3261e;
            padding: 10px 12px;
            border-radius: 8px;
            font-size: 14px;
            margin-bottom: 16px;
        }
        .accesos { margin-top: 22px; padding-top: 14px; border-top: 1px solid #d5dcec; font-size: 13px; line-height: 1.7; }
        .accesos p { margin: 7px 0; }
        .accesos a { color: #0d1f4d; font-weight: 600; }
        .exito { padding: 12px; background: #e8f5ee; color: #165c36; border-radius: 8px; margin-bottom: 16px; }
        .foot { margin-top: 22px; text-align: center; font-size: 12px; color: #8a93ab; }
        @media (max-width: 720px) {
            .card { flex-direction: column; }
            .brand { padding: 28px 20px; }
            .brand svg { width: 80px; height: 80px; }
            .flow { display: none; }
            .form-side { padding: 30px 24px; }
        }
    </style>
</head>
<body>
    <div class="card">
        <div class="brand">
            <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
                <path d="M60 30 L20 55 L40 85 L60 72 L60 165 Q60 172 67 172 L133 172 Q140 172 140 165 L140 72 L160 85 L180 55 L140 30 Q130 48 100 48 Q70 48 60 30 Z" fill="#e0b83c"/>
                <path d="M78 34 Q100 60 122 34" fill="none" stroke="#0d1f4d" stroke-width="4" stroke-linecap="round"/>
                <rect x="82" y="105" width="36" height="26" rx="4" fill="#0d1f4d"/>
                <path d="M88 118 L96 125 L112 110" fill="none" stroke="#e0b83c" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <h1>Moda<span>Express</span> Perú</h1>
            <p>Módulo de gestión de pedidos</p>
     
            <div class="flow">
                <div><b>1</b><br>Pedido</div>
                <div><b>2</b><br>Stock</div>
                <div><b>3</b><br>Orden</div>
                <div><b>4</b><br>Despacho</div>
                <div><b>5</b><br>Entrega</div>
            </div>
        </div>

        <div class="form-side">
            <h2>Bienvenido</h2>
            <p class="sub">Ingresa para gestionar los pedidos de la tienda</p>

            <% if (request.getParameter("error") != null) { %>
                <div class="error">Usuario o contraseña incorrectos</div>
            <% } %>

            <% if ("1".equals(request.getParameter("registro"))) { %>
                <div class="exito" role="status">Tu cuenta está activa. Ya puedes iniciar sesión.</div>
            <% } %>
            <form action="validar.jsp" method="post">
                <div class="field">
                    <label for="usuario">Usuario</label>
                    <input type="text" id="usuario" name="usuario" placeholder="Ingresa tu usuario" required>
                </div>
                <div class="field">
                    <label for="clave">Contraseña</label>
                    <input type="password" id="clave" name="clave" placeholder="Ingresa tu contraseña" required>
                    <button type="button" class="ver" onclick="verClave()">Mostrar</button>
                </div>
                <button type="submit" class="btn">Ingresar</button>
            </form>
            <div class="accesos">
                <p><a href="recuperar.jsp">¿Olvidaste tu contraseña?</a></p>
                <p>¿Eres dueño de tienda? <a href="registro_tienda.jsp">Registra tu tienda</a></p>
                <p>¿Eres vendedor? <a href="solicitud_vendedor.jsp">Solicita ser vendedor</a></p>
                <p>¿Quieres comprar? <a href="registro_comprador.jsp">Crea tu cuenta de comprador</a> o <a href="catalogo">mira la tienda</a></p>
            </div>

        
        </div>
    </div>

    <script>
        function verClave() {
            var campo = document.getElementById("clave");
            var boton = document.querySelector(".ver");
            if (campo.type === "password") {
                campo.type = "text";
                boton.textContent = "Ocultar";
            } else {
                campo.type = "password";
                boton.textContent = "Mostrar";
            }
        }
    </script>
</body>
</html>