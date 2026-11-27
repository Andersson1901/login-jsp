// Autor: Rodriguez Alvarado Andersson Eduardo
// Dibuja cada div.grafico con los datos de la tabla indicada en data-tabla; así el gráfico y la tabla nunca difieren.
// Los valores salen de data-valor (número sin formato local) y las etiquetas de la primera celda de cada fila.
document.addEventListener("DOMContentLoaded", function () {
    if (!window.echarts) return;
    const colores = ["#0d1f4d", "#e0b83c", "#3f6fb5", "#9aa7c2", "#b5883a", "#6c7a96"];
    function leer(tabla, columna) {
        const etiquetas = [], valores = [];
        tabla.querySelectorAll("tbody tr[data-dato]").forEach(function (fila) {
            const celda = fila.querySelectorAll("td")[columna];
            etiquetas.push(fila.cells[0].textContent.trim());
            valores.push(Number(celda.dataset.valor) || 0);
        });
        return { etiquetas: etiquetas, valores: valores };
    }
    document.querySelectorAll("div.grafico").forEach(function (contenedor) {
        const tabla = document.getElementById(contenedor.dataset.tabla);
        if (!tabla) return;
        const datos = leer(tabla, Number(contenedor.dataset.columna) || 1);
        if (!datos.etiquetas.length) { contenedor.textContent = "Sin datos en el rango."; contenedor.classList.add("tabla-vacia"); return; }
        const tipo = contenedor.dataset.tipo || "bar";
        const grafico = echarts.init(contenedor, null, { renderer: "svg" });
        const opcion = { color: colores, tooltip: { trigger: tipo === "pie" ? "item" : "axis" }, grid: { left: 50, right: 20, top: 20, bottom: 50, containLabel: true } };
        if (tipo === "pie") {
            opcion.legend = { bottom: 0 };
            opcion.series = [{ type: "pie", radius: ["40%", "70%"], data: datos.etiquetas.map(function (n, i) { return { name: n, value: datos.valores[i] }; }) }];
            delete opcion.grid;
        } else {
            opcion.xAxis = { type: "category", data: datos.etiquetas, axisLabel: { interval: 0, rotate: datos.etiquetas.length > 6 ? 30 : 0 } };
            opcion.yAxis = { type: "value" };
            opcion.series = [{ type: tipo, data: datos.valores, name: contenedor.dataset.nombre || "" }];
        }
        grafico.setOption(opcion);
        window.addEventListener("resize", function () { grafico.resize(); });
    });
});
