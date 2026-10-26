// Autor: Rodriguez Alvarado Andersson Eduardo
document.addEventListener("DOMContentLoaded", function () {
    const boton = document.getElementById("sidebarToggle");
    if (boton) boton.addEventListener("click", function () { document.body.classList.toggle("sb-sidenav-toggled"); });
    document.querySelectorAll("form[data-confirmar]").forEach(function (formulario) {
        formulario.addEventListener("submit", function (evento) {
            if (!window.confirm(formulario.dataset.confirmar)) evento.preventDefault();
        });
    });
});
