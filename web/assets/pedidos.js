// Autor: Rodriguez Alvarado Andersson Eduardo
document.addEventListener("DOMContentLoaded", function () {
    const filas = document.getElementById("itemsPedido");
    const plantilla = document.getElementById("plantillaItem");
    function calcular() {
        let subtotal = 0;
        filas.querySelectorAll("tr").forEach(function (fila) {
            const opcion = fila.querySelector(".producto").selectedOptions[0];
            const precio = Number(opcion ? opcion.dataset.precio || 0 : 0);
            const cantidad = Number(fila.querySelector(".cantidad").value) || 0;
            const importe = Math.round(precio * cantidad * 100) / 100;
            fila.querySelector(".precio").textContent = precio.toFixed(2);
            fila.querySelector(".importe").textContent = importe.toFixed(2);
            subtotal += importe;
        });
        const igv = Math.round(subtotal * 0.18 * 100) / 100;
        document.getElementById("subtotalPedido").textContent = subtotal.toFixed(2);
        document.getElementById("igvPedido").textContent = igv.toFixed(2);
        document.getElementById("totalPedido").textContent = (subtotal + igv).toFixed(2);
    }
    function agregar() { filas.appendChild(plantilla.content.cloneNode(true)); calcular(); }
    document.getElementById("agregarItem").addEventListener("click", agregar);
    filas.addEventListener("input", calcular);
    filas.addEventListener("change", calcular);
    filas.addEventListener("click", function (evento) {
        if (evento.target.classList.contains("quitar")) { evento.target.closest("tr").remove(); calcular(); }
    });
    document.getElementById("pedidoFormulario").addEventListener("submit", function (evento) {
        if (!filas.children.length) { evento.preventDefault(); window.alert("Agrega al menos un ítem."); }
    });
    if (!filas.children.length) agregar();
    calcular();
});
