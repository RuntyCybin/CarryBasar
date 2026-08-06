(function () {
    const token = sessionStorage.getItem('token');

    // comprobamos si el token existe
    if (!token) {
        window.location.href = '/public/login.html';
        return;
    } else {
        const username = sessionStorage.getItem('username');
        const roles = sessionStorage.getItem('roles');
        document.getElementById('username').textContent = username;

        // comprobamos el rol de usuario
        if (roles.includes("USER") && roles.includes("TRANSPORTER")) {
            console.log("---DASHBOARD - ROL USER and TRANSPORTER---");

            // indicador del rol en la barra de navegacion
            const role_nav_span = document.getElementById("roleUser");
            role_nav_span.textContent = "[TRANSPORTISTA]";

            // texto del usuario tipo TRANSPORTER
            const descripcionDashboard = document.getElementById("descDashboard");
            descripcionDashboard.innerHTML = `<p id="descDashboard">Hola <span style='font-weight: 700;'>${username}</span> selecciona algun order para llevar.</p>`;

            // menu de botones superior derecha
            const btnsCerrarCrear = document.getElementById("salirCrearBtns");
            btnsCerrarCrear.innerHTML = `
            <div class="btn-group btn-group-lg" role="group" aria-label="Large button group" id="salirCrearBtns">
                <button type="button" id="acceptedOrderstBtn" class="btn btn-outline-primary">Ordenes aceptadas</button>
                <button type="button" id="logoutBtn" class="btn btn-outline-primary">Salir</button>
            </div>`;

            // mostramos orders que se pueden llevar
            fetch('/v1/api/order/all', {
                headers: {
                    'Authorization': 'Bearer ' + token
                }
            }).then(async res => {

                // Gestión del 500
                if (res.status == 500) {
                    // Leemos el cuerpo para obtener el mensaje que envía el backend
                    const errorBody = await res.json().catch(() => ({}));
                    const errorMsg = errorBody.error || "Internal server error";
                    throw new Error(errorMsg);
                }

                // Gestión del 401/403 (no autorizado)
                if (!res.ok) throw new Error('No autorizado');

                // Todo correcto → devolvemos JSON
                return res.json();

            }).then(data => {
                console.log("RESPONSE: " + JSON.stringify(data, null, 2));
                const listContainer = document.getElementById("ordersList");
                listContainer.innerHTML = ""; // Limpiar contenido previo

                // iteramos el data
                for (let i = 0; i < data.length; i++) {
                    const order = data[i];
                    console.log(`Pedido ${order.id}: ${order.description}, volumen ${order.vol}, creado el ${order.orderDate}`);

                    const item = document.createElement("div");
                    //item.href = "accept-order.html";

                    item.className = "list-group-item list-group-item-action d-flex gap-3 py-3";

                    item.innerHTML = `
                    <img src="https://github.com/twbs.png" alt="" width="32" height="32"
                        class="rounded-circle flex-shrink-0">
                    <div class="d-flex gap-2 w-100 justify-content-between">
                    <div>
                        <h6 class="mb-0" id="desc">${order.description}</h6>
                        <p class="mb-0 opacity-75">Identificador pedido: ${order.id}</p>
                        <p class="mb-0 opacity-75" id="volume">Volumen: ${order.vol}</p>
                    </div>
                    <input type="hidden" id="orderId" value="${order.id}">
                    <button type="button" id="aceptarBtn" onclick='aceptarOrder(${JSON.stringify(order)})' class="btn btn-outline-success">Aceptar</button>
                    <small class="opacity-50 text-nowrap"><p>Creado: </p>${new Date(order.orderDate).toLocaleString()}</small>
                    <small class="opacity-50 text-nowrap"><p>Fecha límite: </p>${new Date(order.dueDate).toLocaleString()}</small>`;

                    listContainer.appendChild(item);
                }
            }).catch(err => {
                console.error(err);

                // Si fue un 500 mostramos el mensaje del backend, si no, mensaje genérico
                alert(err.errorMsg.includes("Internal server")
                    ? 'No se pudieron obtener tus pedidos: ' + err.message
                    : 'Sesión inválida. Inicie sesión nuevamente.');

                sessionStorage.clear();
                window.location.href = '/public/login.html';
            });

        } else if (roles.includes("USER") && roles.includes("CARRY")) {
            console.log("---ROL USER and CARRY---");

            // indicador del rol en la barra de navegacion
            const role_nav_span = document.getElementById("roleUser");
            role_nav_span.textContent = "[CLIENTE]";

            // texto del usuario tipo CARRY
            const descripcionDashboard = document.getElementById("descDashboard");
            descripcionDashboard.innerHTML = `<p id="descDashboard">Listado de orders creados por <span>${username}</span>.</p>`;

            const btnsCerrarCrear = document.getElementById("salirCrearBtns");
            btnsCerrarCrear.innerHTML = `
            <div class="btn-group btn-group-lg" role="group" aria-label="Large button group" id="salirCrearBtns">
                <button type="button" id="logoutBtn" class="btn btn-outline-primary">Salir</button>
                <a href="create-order.html" type="button" class="btn btn-outline-primary">Crear pedido</a>
            </div>`;

            // mostramos orders que tiene el usuario creados
            fetch('/v1/api/order/my-orders', {
                headers: {
                    'Authorization': 'Bearer ' + token
                }
            }).then(async res => {
                console.log("STATUS: " + res.status);

                // Gestión del 500
                if (res.status == 500) {
                    // Leemos el cuerpo para obtener el mensaje que envía el backend
                    const errorBody = await res.json().catch(() => ({}));
                    const errorMsg = errorBody.error || "Internal server error";
                    throw new Error(errorMsg);
                }

                // Gestión del 400 (no se han recogido orders)
                if (res.status === 400) {
                    console.log("No hay orders creados por el usuario");

                    const listContainer = document.getElementById("ordersList");
                    listContainer.innerHTML = ""; // Limpiar contenido previo
                    const item = document.createElement("span");
                    item.className = "no-items-message";
                    item.innerHTML = `
                        <div>
                            <h6 class="mb-0">NO TIENE ORDENES CREADAS</h6>
                        </div>`;

                    listContainer.appendChild(item);
                }

                // No autorizado
                if (res.status === 401 || res.status === 403) {
                    throw new Error('No autorizado');
                }

                // Todo correcto
                if (res.status === 200) {
                    console.log("Orders obtenidos correctamente");
                    res.json().then(data => {
                        console.log("RESPONSE: " + JSON.stringify(data, null, 2));
                        const listContainer = document.getElementById("ordersList");
                        listContainer.innerHTML = ""; // Limpiar contenido previo
                        // iteramos el res
                        for (let i = 0; i < data.length; i++) {
                            const order = data[i];
                            console.log(`Pedido ${i + 1}: ${order.description}, volumen ${order.volume}, creado el ${order.createdAt}, con id ${order.orderId}`);

                            const item = document.createElement("a");
                            item.href = "#";
                            item.className = "list-group-item list-group-item-action d-flex gap-3 py-3";

                            item.innerHTML = `
                            <img src="https://github.com/twbs.png" alt="" width="32" height="32"
                                class="rounded-circle flex-shrink-0">
                            <div class="d-flex gap-2 w-100 justify-content-between">
                            <div>
                                <h6 class="mb-0" id="desc">${order.description}</h6>
                                <p class="mb-0 opacity-75">Identificador pedido: ${order.orderId}</p>
                                <p class="mb-0 opacity-75">Volumen: ${order.volume}</p>
                            </div>
                            <input type="hidden" id="orderId" value="${order.orderId}">
                            <small class="opacity-50 text-nowrap"><p>Creado: </p>${new Date(order.createdAt).toLocaleString()}</small>
                            <small class="opacity-50 text-nowrap"><p>Fecha límite: </p>${new Date(order.dueDate).toLocaleString()}</small>
                            <button type="button" id="eliminarBtn" onclick="eliminarOrder('${order.orderId}')" class="btn btn-outline-danger">Eliminar</button>
                            </div>`;

                            listContainer.appendChild(item);
                        }
                    }).catch(err => {
                        console.error("Error parsing JSON:", err);
                    });
                    // Todo correcto → devolvemos JSON
                    // return res.json();
                }
            }).catch(err => {
                console.error(err);

                // Si fue un 500 mostramos el mensaje del backend, si no, mensaje genérico
                alert(err.errorMsg.includes("Internal server")
                    ? 'No se pudieron obtener tus pedidos: ' + err.message
                    : 'Sesión inválida. Inicie sesión nuevamente.');

                sessionStorage.clear();
                window.location.href = '/public/login.html';
            });
        } else if (roles.includes("ADMIN")) {
            window.location.href = '/public/dashboardAdmin.html';
            return;
        }
    }


    // Muestra un alert de Bootstrap con fade in/out durante `duration` ms
    function showBootstrapAlert(message, duration = 5000) {
        const alertContainer = document.getElementById('alertContainer');
        if (!alertContainer) return;

        const alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-success fade';
        alertDiv.setAttribute('role', 'alert');
        alertDiv.textContent = message;

        alertContainer.appendChild(alertDiv);

        // se añade "show" en el siguiente frame para que la transicion de fade-in se aplique
        requestAnimationFrame(() => alertDiv.classList.add('show'));

        setTimeout(() => {
            alertDiv.classList.remove('show'); // dispara el fade-out
            alertDiv.addEventListener('transitionend', () => alertDiv.remove(), { once: true });
        }, duration);
    }

    // Si venimos de una recarga tras una accion (p.ej. eliminar un order), mostramos el alert pendiente
    const pendingAlert = sessionStorage.getItem('pendingAlert');
    if (pendingAlert) {
        sessionStorage.removeItem('pendingAlert');
        showBootstrapAlert(pendingAlert);
    }

    // Funcion para eliminar orders de clientes
    window.eliminarOrder = function eliminarOrder(idOrder) {
        const token = sessionStorage.getItem('token');
        if (!token) {
            alert('Token no válido');
            return;
        }
        if (confirm('¿Estás seguro de eliminar este order?')) {
            fetch(`/v1/api/order/${idOrder}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type': 'application/json'
                }
            }).then(async res => {
                if (!res.ok) {
                    const errorBody = await res.json().catch(() => ({}));
                    const errorMsg = errorBody.error || 'Error eliminando el order';
                    throw new Error(errorMsg);
                }
                sessionStorage.setItem('pendingAlert', 'Order removed successfully!');
                window.location.reload();
            }).catch(err => {
                console.error(err);
                alert('Error: ' + err.message);
            });
        }
    }


    // Funcion para aceptar orders de transportistas
    window.aceptarOrder = function aceptarOrder(order) {
        const { id, description, vol, orderDate } = order;
        const token = sessionStorage.getItem('token');
        const userId = sessionStorage.getItem('userId');
        sessionStorage.setItem('accepted_order_id', id);

        if (!token || !userId) {
            alert('Sesión inválida. Inicie sesión nuevamente.');
            window.location.href = '/public/login.html';
            return;
        }

        if (confirm('¿Estás seguro de aceptar este order?')) {
            fetch(`/v1/api/acceptOrder/create`, {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type': 'application/json'
                },
                // TODO: cambiar fecha por las recogidas de los date time pickers
                body: JSON.stringify({ 
                    orderId: id, 
                    userId: userId, 
                    shipAt: new Date().toISOString(), 
                    shipTo: new Date(Date.now() + 2 * 24 * 60 * 60 * 1000).toISOString(),
                    description: description,
                    volumen: vol
                })
            }).then(async res => {
                if (res.status === 200) {
                    alert('Order aceptado correctamente.');
                    //window.location.href = '/public/transport-orders.html';
                    window.location.reload();
                } else if (res.status === 404) {
                    throw new Error('Order no encontrado');
                } else {
                    const errorBody = await res.json().catch(() => ({}));
                    const errorMsg = errorBody.error || 'Error al aceptar el order';
                    throw new Error(errorMsg);
                }
            }).catch(err => {
                console.error(err);
                alert('Error: ' + err.message);
            });
        }
    }

    const acceptedOrderstBtn = document.getElementById('acceptedOrderstBtn');
    if (acceptedOrderstBtn) {
        acceptedOrderstBtn.addEventListener('click', () => {
            window.location.href = '/public/accept-order.html';
        });
    }


    // Logout
    document.getElementById('logoutBtn').addEventListener('click', () => {
        console.log("Logout button clicked");
        sessionStorage.clear();
        window.location.href = '/public/login.html';
    });
})();