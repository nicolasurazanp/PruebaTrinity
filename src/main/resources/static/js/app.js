"use strict";

const estado = {
    clientes: []
};

const nombresTipoCuenta = { AHORROS: "Ahorros", CORRIENTE: "Corriente" };
const nombresTransaccion = { CONSIGNACION: "Consignación", RETIRO: "Retiro", TRANSFERENCIA: "Transferencia" };
const nombresMovimiento = { CREDITO: "Crédito", DEBITO: "Débito" };
const nombresEstado = { ACTIVA: "Activa", INACTIVA: "Inactiva", CANCELADA: "Cancelada" };

const nombresCampo = {
    tipoIdentificacion: "Tipo de identificación",
    numeroIdentificacion: "Número de identificación",
    nombres: "Nombres",
    apellido: "Apellido",
    correoElectronico: "Correo electrónico",
    fechaNacimiento: "Fecha de nacimiento",
    clienteId: "Cliente",
    tipoCuenta: "Tipo de cuenta",
    exentaGmf: "Exenta del GMF",
    cuentaId: "Cuenta",
    cuentaOrigenId: "Cuenta de origen",
    cuentaDestinoId: "Cuenta de destino",
    monto: "Monto"
};

const formatoDinero = new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP", minimumFractionDigits: 2 });
const formatoFecha = new Intl.DateTimeFormat("es-CO", { dateStyle: "medium", timeStyle: "short" });

/* Utilidades */

function escapar(texto) {
    return String(texto ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;");
}

function dinero(valor) {
    return formatoDinero.format(Number(valor));
}

function fecha(valor) {
    return formatoFecha.format(new Date(valor));
}

function datosDe(formulario) {
    return Object.fromEntries(new FormData(formulario).entries());
}

class ErrorApi extends Error {
    constructor(mensaje, detalles) {
        super(mensaje);
        this.detalles = detalles;
    }
}

async function api(metodo, url, cuerpo) {
    const opciones = { method: metodo, headers: {} };
    if (cuerpo !== undefined) {
        opciones.headers["Content-Type"] = "application/json";
        opciones.body = JSON.stringify(cuerpo);
    }
    const respuesta = await fetch(url, opciones);
    if (respuesta.status === 204) {
        return null;
    }
    const contenido = await respuesta.json().catch(() => null);
    if (!respuesta.ok) {
        const mensaje = contenido?.detail ?? `Error ${respuesta.status}`;
        const detalles = contenido?.errores
            ? Object.entries(contenido.errores).map(([campo, error]) => `${nombresCampo[campo] ?? campo}: ${error}`)
            : [];
        throw new ErrorApi(mensaje, detalles);
    }
    return contenido;
}

/* Avisos y confirmaciones */

function avisar(titulo, detalle = "", tipo = "exito", lista = []) {
    const aviso = document.createElement("div");
    aviso.className = `aviso ${tipo}`;
    aviso.innerHTML = `<strong>${escapar(titulo)}</strong>${escapar(detalle)}`
        + (lista.length ? `<ul>${lista.map(item => `<li>${escapar(item)}</li>`).join("")}</ul>` : "");
    document.getElementById("avisos").append(aviso);
    setTimeout(() => aviso.remove(), tipo === "error" ? 7000 : 4000);
}

function avisarError(error) {
    avisar("No se pudo completar la operación", error.message, "error", error.detalles ?? []);
}

function confirmar(titulo, mensaje) {
    const dialogo = document.getElementById("dialogo-confirmar");
    document.getElementById("dialogo-titulo").textContent = titulo;
    document.getElementById("dialogo-mensaje").textContent = mensaje;
    dialogo.returnValue = "";
    dialogo.showModal();
    return new Promise(resolver => {
        dialogo.addEventListener("close", () => resolver(dialogo.returnValue === "confirmar"), { once: true });
    });
}

/* Navegación */

function mostrarVista(nombre) {
    document.querySelectorAll(".pestana").forEach(pestana =>
        pestana.classList.toggle("activa", pestana.dataset.vista === nombre));
    document.querySelectorAll(".vista").forEach(vista =>
        vista.classList.toggle("activa", vista.id === `vista-${nombre}`));
    history.replaceState(null, "", `#${nombre}`);
}

document.querySelectorAll(".pestana").forEach(pestana =>
    pestana.addEventListener("click", () => mostrarVista(pestana.dataset.vista)));

/* Clientes */

async function cargarClientes() {
    estado.clientes = await api("GET", "/api/clientes");
    pintarClientes();
    await poblarSelectoresCliente();
}

function pintarClientes() {
    const cuerpo = document.getElementById("tabla-clientes");
    if (estado.clientes.length === 0) {
        cuerpo.innerHTML = `<tr><td colspan="4" class="vacio">Todavía no hay clientes registrados.</td></tr>`;
        return;
    }
    cuerpo.innerHTML = estado.clientes.map(cliente => `
        <tr>
            <td>
                <span class="monospace">${escapar(cliente.numeroIdentificacion)}</span>
                <span class="secundario-texto">${escapar(cliente.tipoIdentificacion)}</span>
            </td>
            <td>${escapar(cliente.nombres)} ${escapar(cliente.apellido)}</td>
            <td>${escapar(cliente.correoElectronico)}</td>
            <td class="derecha">
                <button class="enlace" data-editar="${cliente.id}">Editar</button>
                <button class="enlace peligro" data-eliminar="${cliente.id}">Eliminar</button>
            </td>
        </tr>`).join("");
}

const formCliente = document.getElementById("form-cliente");

function reiniciarFormCliente() {
    formCliente.reset();
    formCliente.elements.id.value = "";
    document.getElementById("titulo-form-cliente").textContent = "Nuevo cliente";
    document.getElementById("boton-guardar-cliente").textContent = "Registrar cliente";
    document.getElementById("boton-cancelar-edicion").classList.add("oculto");
}

formCliente.addEventListener("submit", async evento => {
    evento.preventDefault();
    const { id, ...datos } = datosDe(formCliente);
    try {
        if (id) {
            await api("PUT", `/api/clientes/${id}`, datos);
            avisar("Cliente actualizado", `${datos.nombres} ${datos.apellido}`);
        } else {
            await api("POST", "/api/clientes", datos);
            avisar("Cliente registrado", `${datos.nombres} ${datos.apellido}`);
        }
        reiniciarFormCliente();
        await cargarClientes();
    } catch (error) {
        avisarError(error);
    }
});

document.getElementById("boton-cancelar-edicion").addEventListener("click", reiniciarFormCliente);

document.getElementById("tabla-clientes").addEventListener("click", async evento => {
    const idEditar = evento.target.dataset.editar;
    const idEliminar = evento.target.dataset.eliminar;

    if (idEditar) {
        const cliente = estado.clientes.find(c => String(c.id) === idEditar);
        for (const campo of ["id", "tipoIdentificacion", "numeroIdentificacion", "nombres",
            "apellido", "correoElectronico", "fechaNacimiento"]) {
            formCliente.elements[campo].value = cliente[campo];
        }
        document.getElementById("titulo-form-cliente").textContent = "Editar cliente";
        document.getElementById("boton-guardar-cliente").textContent = "Guardar cambios";
        document.getElementById("boton-cancelar-edicion").classList.remove("oculto");
        formCliente.scrollIntoView({ behavior: "smooth", block: "start" });
    }

    if (idEliminar) {
        const cliente = estado.clientes.find(c => String(c.id) === idEliminar);
        const aceptado = await confirmar("Eliminar cliente",
            `Se eliminará a ${cliente.nombres} ${cliente.apellido}. Esta acción no se puede deshacer.`);
        if (!aceptado) {
            return;
        }
        try {
            await api("DELETE", `/api/clientes/${idEliminar}`);
            avisar("Cliente eliminado", `${cliente.nombres} ${cliente.apellido}`);
            await cargarClientes();
        } catch (error) {
            avisarError(error);
        }
    }
});

/* Selectores de cliente y de cuenta */

async function poblarSelectoresCliente() {
    const opciones = estado.clientes.map(cliente =>
        `<option value="${cliente.id}">${escapar(cliente.nombres)} ${escapar(cliente.apellido)} - ${escapar(cliente.numeroIdentificacion)}</option>`
    ).join("");

    for (const selector of document.querySelectorAll(".selector-cliente")) {
        const anterior = selector.value;
        selector.innerHTML = opciones || `<option value="">Sin clientes</option>`;
        if (estado.clientes.some(c => String(c.id) === anterior)) {
            selector.value = anterior;
        }
    }

    await Promise.all([...document.querySelectorAll(".selector-cliente[data-cuentas]")].map(poblarCuentasDe));
    await pintarCuentas();
}

async function poblarCuentasDe(selectorCliente) {
    const destino = document.getElementById(selectorCliente.dataset.cuentas);
    const anterior = destino.value;
    if (!selectorCliente.value) {
        destino.innerHTML = `<option value="">Sin cuentas</option>`;
        destino.dispatchEvent(new Event("change"));
        return;
    }
    const cuentas = await api("GET", `/api/cuentas?clienteId=${selectorCliente.value}`);
    destino.innerHTML = cuentas.length
        ? cuentas.map(cuenta =>
            `<option value="${cuenta.id}">${cuenta.numeroCuenta} - ${nombresTipoCuenta[cuenta.tipoCuenta]} - ${dinero(cuenta.saldo)}</option>`
        ).join("")
        : `<option value="">Sin cuentas</option>`;
    if (cuentas.some(c => String(c.id) === anterior)) {
        destino.value = anterior;
    }
    destino.dispatchEvent(new Event("change"));
}

document.querySelectorAll(".selector-cliente[data-cuentas]").forEach(selector =>
    selector.addEventListener("change", () => poblarCuentasDe(selector).catch(avisarError)));

async function refrescarCuentas() {
    await Promise.all([...document.querySelectorAll(".selector-cliente[data-cuentas]")].map(poblarCuentasDe));
    await pintarCuentas();
}

/* Cuentas */

const selectorCuentasCliente = document.getElementById("cuentas-cliente");

async function pintarCuentas() {
    const cuerpo = document.getElementById("tabla-cuentas");
    if (!selectorCuentasCliente.value) {
        cuerpo.innerHTML = `<tr><td colspan="5" class="vacio">Registre un cliente para abrir cuentas.</td></tr>`;
        return;
    }
    const cuentas = await api("GET", `/api/cuentas?clienteId=${selectorCuentasCliente.value}`);
    if (cuentas.length === 0) {
        cuerpo.innerHTML = `<tr><td colspan="5" class="vacio">Este cliente no tiene cuentas.</td></tr>`;
        return;
    }
    cuerpo.innerHTML = cuentas.map(cuenta => `
        <tr>
            <td>
                <span class="monospace">${cuenta.numeroCuenta}</span>
                <span class="secundario-texto">${cuenta.exentaGmf ? "Exenta del GMF" : "Sujeta al GMF"}</span>
            </td>
            <td>${nombresTipoCuenta[cuenta.tipoCuenta]}</td>
            <td><span class="etiqueta ${cuenta.estado}">${nombresEstado[cuenta.estado]}</span></td>
            <td class="derecha">${dinero(cuenta.saldo)}</td>
            <td class="derecha">
                <button class="enlace" data-cuenta="${cuenta.id}" data-accion="activar"
                    ${cuenta.estado !== "INACTIVA" ? "disabled" : ""}>Activar</button>
                <button class="enlace" data-cuenta="${cuenta.id}" data-accion="inactivar"
                    ${cuenta.estado !== "ACTIVA" ? "disabled" : ""}>Inactivar</button>
                <button class="enlace peligro" data-cuenta="${cuenta.id}" data-accion="cancelar" data-numero="${cuenta.numeroCuenta}"
                    ${cuenta.estado === "CANCELADA" ? "disabled" : ""}>Cancelar</button>
            </td>
        </tr>`).join("");
}

selectorCuentasCliente.addEventListener("change", () => pintarCuentas().catch(avisarError));

document.getElementById("tabla-cuentas").addEventListener("click", async evento => {
    const { cuenta, accion, numero } = evento.target.dataset;
    if (!cuenta || evento.target.disabled) {
        return;
    }
    if (accion === "cancelar") {
        const aceptado = await confirmar("Cancelar cuenta",
            `La cuenta ${numero} quedará cancelada de forma definitiva. Solo es posible si su saldo es $0.`);
        if (!aceptado) {
            return;
        }
    }
    try {
        const actualizada = await api("PATCH", `/api/cuentas/${cuenta}/${accion}`);
        avisar("Estado actualizado", `La cuenta ${actualizada.numeroCuenta} está ${nombresEstado[actualizada.estado].toLowerCase()}.`);
        await refrescarCuentas();
    } catch (error) {
        avisarError(error);
    }
});

document.getElementById("form-cuenta").addEventListener("submit", async evento => {
    evento.preventDefault();
    const formulario = evento.target;
    try {
        const cuenta = await api("POST", "/api/cuentas", {
            clienteId: Number(formulario.elements.clienteId.value),
            tipoCuenta: formulario.elements.tipoCuenta.value,
            exentaGmf: formulario.elements.exentaGmf.checked
        });
        avisar("Cuenta abierta", `${nombresTipoCuenta[cuenta.tipoCuenta]} número ${cuenta.numeroCuenta}`);
        formulario.elements.exentaGmf.checked = false;
        selectorCuentasCliente.value = String(cuenta.clienteId);
        await refrescarCuentas();
    } catch (error) {
        avisarError(error);
    }
});

/* Transacciones */

document.getElementById("form-operacion").addEventListener("submit", async evento => {
    evento.preventDefault();
    const formulario = evento.target;
    const { operacion, cuentaId, monto } = datosDe(formulario);
    try {
        const movimiento = await api("POST", `/api/transacciones/${operacion}`, {
            cuentaId: Number(cuentaId),
            monto: Number(monto)
        });
        avisar(operacion === "consignaciones" ? "Consignación registrada" : "Retiro registrado",
            `Nuevo saldo: ${dinero(movimiento.saldoResultante)}`);
        formulario.elements.monto.value = "";
        await refrescarCuentas();
    } catch (error) {
        avisarError(error);
    }
});

document.getElementById("form-transferencia").addEventListener("submit", async evento => {
    evento.preventDefault();
    const formulario = evento.target;
    const { cuentaOrigenId, cuentaDestinoId, monto } = datosDe(formulario);
    try {
        const [debito, credito] = await api("POST", "/api/transacciones/transferencias", {
            cuentaOrigenId: Number(cuentaOrigenId),
            cuentaDestinoId: Number(cuentaDestinoId),
            monto: Number(monto)
        });
        avisar("Transferencia realizada",
            `Origen: ${dinero(debito.saldoResultante)}. Destino: ${dinero(credito.saldoResultante)}.`);
        formulario.elements.monto.value = "";
        await refrescarCuentas();
    } catch (error) {
        avisarError(error);
    }
});

/* Estado de cuenta */

document.getElementById("estado-cuenta").addEventListener("change", evento =>
    pintarEstadoCuenta(evento.target.value).catch(avisarError));

async function pintarEstadoCuenta(cuentaId) {
    const resumen = document.getElementById("estado-resumen");
    const cuerpo = document.getElementById("tabla-movimientos");
    if (!cuentaId) {
        resumen.classList.add("oculto");
        cuerpo.innerHTML = `<tr><td colspan="5" class="vacio">Seleccione una cuenta para ver sus movimientos.</td></tr>`;
        return;
    }
    const estadoCuenta = await api("GET", `/api/cuentas/${cuentaId}/estado-cuenta`);
    document.getElementById("estado-saldo").textContent = dinero(estadoCuenta.saldo);
    document.getElementById("estado-disponible").textContent = dinero(estadoCuenta.saldoDisponible);
    document.getElementById("estado-numero").textContent =
        `${estadoCuenta.numeroCuenta} - ${nombresTipoCuenta[estadoCuenta.tipoCuenta]}`;
    document.getElementById("estado-estado").innerHTML =
        `<span class="etiqueta ${estadoCuenta.estado}">${nombresEstado[estadoCuenta.estado]}</span>`;
    resumen.classList.remove("oculto");

    if (estadoCuenta.movimientos.length === 0) {
        cuerpo.innerHTML = `<tr><td colspan="5" class="vacio">La cuenta todavía no tiene movimientos.</td></tr>`;
        return;
    }
    cuerpo.innerHTML = estadoCuenta.movimientos.map(movimiento => {
        const esCredito = movimiento.tipoMovimiento === "CREDITO";
        return `
        <tr>
            <td>${fecha(movimiento.fecha)}</td>
            <td>
                ${nombresTransaccion[movimiento.tipoTransaccion]}
                <span class="secundario-texto monospace">${escapar(movimiento.codigoTransaccion.slice(0, 8))}</span>
            </td>
            <td>${nombresMovimiento[movimiento.tipoMovimiento]}</td>
            <td class="derecha ${esCredito ? "credito" : "debito"}">${esCredito ? "+" : "-"} ${dinero(movimiento.monto)}</td>
            <td class="derecha">${dinero(movimiento.saldoResultante)}</td>
        </tr>`;
    }).join("");
}

/* Inicio */

const vistaInicial = location.hash.slice(1);
if (document.getElementById(`vista-${vistaInicial}`)) {
    mostrarVista(vistaInicial);
}

cargarClientes().catch(avisarError);
