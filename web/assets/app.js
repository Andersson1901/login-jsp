// Autor: Rodriguez Alvarado Andersson Eduardo
document.addEventListener("DOMContentLoaded", function () {
    const boton = document.getElementById("sidebarToggle");
    if (boton) boton.addEventListener("click", function () { document.body.classList.toggle("sb-sidenav-toggled"); });
    document.querySelectorAll("form[data-confirmar]").forEach(function (formulario) {
        formulario.addEventListener("submit", function (evento) {
            if (!window.confirm(formulario.dataset.confirmar)) evento.preventDefault();
        });
    });
    document.querySelectorAll("select[data-autoenviar]").forEach(function (lista) {
        lista.addEventListener("change", function () { lista.form.submit(); });
    });
    // Búsqueda automática: cada 470 ms, y solo si hay texto distinto del último buscado, se pide la tabla y se cambia sin recargar.
    document.querySelectorAll("input[data-buscar]").forEach(function (campo) {
        var ultimo = campo.value.trim(), pedido = null;
        setInterval(function () {
            var texto = campo.value.trim(), destino = document.getElementById("resultados");
            if (texto === "" || texto === ultimo || !destino) return;
            var anterior = ultimo; ultimo = texto;
            if (pedido) pedido.abort();
            pedido = new AbortController();
            var datos = new URLSearchParams(new FormData(campo.form));
            datos.set("q", texto);
            var direccion = campo.form.getAttribute("action") + "?" + datos.toString();
            fetch(direccion, { credentials: "same-origin", signal: pedido.signal })
                .then(function (respuesta) { return respuesta.text(); })
                .then(function (html) {
                    var nuevo = new DOMParser().parseFromString(html, "text/html").getElementById("resultados");
                    if (!nuevo) { window.location.href = direccion; return; }
                    destino.innerHTML = nuevo.innerHTML;
                    window.history.replaceState(null, "", direccion);
                })
                .catch(function (error) { if (error.name !== "AbortError") ultimo = anterior; });
        }, 470);
    });
});
