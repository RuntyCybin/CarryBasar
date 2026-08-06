(function () {
    const token = sessionStorage.getItem('token');

    // comprobamos si el token existe
    if (!token) {
        window.location.href = '/public/login.html';
        return;
    } else {
        const username = sessionStorage.getItem('username');
        const roles = sessionStorage.getItem('roles');
        const orderId = sessionStorage.getItem('accepted_order_id');
        const userId = sessionStorage.getItem('userId');
        document.getElementById('username').textContent = username;
        //document.getElementById('orderId').textContent = orderId;

        // comprobamos el rol de usuario
        if (roles.includes("USER") && roles.includes("TRANSPORTER")) {
            console.log("---ACCEPTED ORDERS - TRANSPORTER---");

            // indicador del rol en la barra de navegacion
            const role_nav_span = document.getElementById("roleUser");
            role_nav_span.textContent = "[TRANSPORTISTA]";

            // texto del usuario tipo TRANSPORTER
            const descripcionAcceptOrder = document.getElementById("descAcceptOrder");
            descripcionAcceptOrder.innerHTML = `<p id="descAcceptOrder">Tus ordenes aceptadas.</p>`;

            // menu de botones superior derecha
            const btnsCerrarCrear = document.getElementById("salirCrearBtns");
            btnsCerrarCrear.innerHTML = `
            <div class="btn-group btn-group-lg" role="group" aria-label="Large button group" id="salirCrearBtns">
                <button type="button" id="volverBtn" class="btn btn-outline-primary">Volver</button>
                <button type="button" class="btn btn-outline-primary">Salir</button>
            </div>`;

            // mostramos orders que se pueden llevar
            fetch('/v1/api/acceptOrder/getAcceptedOrders/'+userId, {
                headers: {
                    'Authorization': 'Bearer ' + token
                },
                method: 'GET'
            }).then(data => {
                // Gestión del 500
                if (data.status == 500) {
                    // Leemos el cuerpo para obtener el mensaje que envía el backend
                    const errorBody = data.json().catch(() => ({}));
                    const errorMsg = errorBody.error || "Internal server error";
                    throw new Error(errorMsg);
                } else if (data.status == 404) {
                    throw new Error('Order not found');
                } else if (data.status == 200) {
                    if (!data.ok) throw new Error('No autorizado');

                    return data.json().then(parsedData => {
                        console.log("STATUS: " + data.status);
                        console.log("RESPONSE: " + JSON.stringify(parsedData, null, 2));

                        const container = document.getElementById("acceptedOrderList");
                        container.innerHTML = ""; // Limpiar contenido previo

                        parsedData.forEach(order => {
                            console.log("Order NAME: " + order.orderDesc);
                            container.innerHTML += `
                            <h6 class="display-5" id="idOrder">Orden: ${order.orderDesc}</h6>
                            <p class="col-md-8 fs-4" id="descOrder">Identificador de la orden: ${order.orderId}</p>
                            <p class="col-md-8 fs-4" id="volumeOrder">Volumen: ${order.vol}</p>
                            <p class="col-md-8 fs-4" id="userOrder">Usuario: ${order.userId}</p>
                            <small class="opacity-50 text-nowrap">${new Date(order.createdAt).toLocaleString()}</small>
                            <button type="button" id="eliminarAcceptedOrderBtn" onclick="eliminarAcceptedOrder(${order.orderId}, ${order.userId})" class="btn btn-outline-danger">Eliminar</button>
                            <hr>`;
                        });
                    });
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

            document.getElementById('volverBtn').addEventListener('click', () => {
                window.location.href = '/public/dashboard.html';
            });
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

    // Si venimos de una recarga tras una accion (p.ej. eliminar una orden aceptada), mostramos el alert pendiente
    const pendingAlert = sessionStorage.getItem('pendingAlert');
    if (pendingAlert) {
        sessionStorage.removeItem('pendingAlert');
        showBootstrapAlert(pendingAlert);
    }

    // Funcion para eliminar una orden aceptada
    window.eliminarAcceptedOrder = function eliminarAcceptedOrder(orderId, userId) {
        const token = sessionStorage.getItem('token');
        if (!token) {
            alert('Token no válido');
            return;
        }
        if (confirm('¿Estás seguro de eliminar esta orden aceptada?')) {
            fetch(`/v1/api/acceptOrder?orderId=${orderId}&userId=${userId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type': 'application/json'
                }
            }).then(async res => {
                if (!res.ok) {
                    const errorBody = await res.json().catch(() => ({}));
                    const errorMsg = errorBody.error || 'Error eliminando la orden aceptada';
                    throw new Error(errorMsg);
                }
                sessionStorage.setItem('pendingAlert', 'Orden aceptada eliminada correctamente!');
                window.location.reload();
            }).catch(err => {
                console.error(err);
                alert('Error: ' + err.message);
            });
        }
    }
})();