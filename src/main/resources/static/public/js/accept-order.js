(function () {
    const token = sessionStorage.getItem('token');

    const BTN_PRIMARY = 'border border-blue-600 text-blue-600 hover:bg-blue-600 hover:text-white px-4 py-2 rounded transition-colors font-medium';
    const BTN_DANGER = 'border border-red-600 text-red-600 hover:bg-red-600 hover:text-white px-4 py-2 rounded transition-colors font-medium';
    const LIST_ITEM = 'flex items-center gap-3 p-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors';

    // Escapa texto antes de insertarlo en innerHTML para evitar XSS
    function escapeHtml(value) {
        const div = document.createElement('div');
        div.textContent = value ?? '';
        return div.innerHTML;
    }

    // Modal de confirmación (sustituye a confirm())
    const confirmModal = document.getElementById('confirmModal');

    function showConfirmModal(message) {
        return new Promise((resolve) => {
            if (!confirmModal) {
                resolve(window.confirm(message));
                return;
            }

            document.getElementById('confirmModalMessage').textContent = message;
            confirmModal.classList.remove('hidden');
            confirmModal.classList.add('flex');

            const yesBtn = document.getElementById('confirmModalYesBtn');
            const noBtn = document.getElementById('confirmModalNoBtn');

            function cleanup(result) {
                confirmModal.classList.add('hidden');
                confirmModal.classList.remove('flex');
                yesBtn.removeEventListener('click', onYes);
                noBtn.removeEventListener('click', onNo);
                confirmModal.removeEventListener('click', onBackdrop);
                document.removeEventListener('keydown', onKeydown);
                resolve(result);
            }

            function onYes() { cleanup(true); }
            function onNo() { cleanup(false); }
            function onBackdrop(e) { if (e.target === confirmModal) cleanup(false); }
            function onKeydown(e) { if (e.key === 'Escape') cleanup(false); }

            yesBtn.addEventListener('click', onYes);
            noBtn.addEventListener('click', onNo);
            confirmModal.addEventListener('click', onBackdrop);
            document.addEventListener('keydown', onKeydown);
        });
    }

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
            <div class="flex gap-2" id="salirCrearBtns">
                <button type="button" id="volverBtn" class="${BTN_PRIMARY}">Volver</button>
                <button type="button" id="logoutBtn" class="${BTN_PRIMARY}">Salir</button>
            </div>`;

            document.getElementById('logoutBtn').addEventListener('click', () => {
                sessionStorage.clear();
                window.location.href = '/public/login.html';
            });

            // mostramos orders que se pueden llevar
            fetch('/v1/api/acceptOrder/getAcceptedOrders/'+userId, {
                headers: {
                    'Authorization': 'Bearer ' + token
                },
                method: 'GET'
            }).then(async data => {
                // Gestión del 500
                if (data.status === 500) {
                    // Leemos el cuerpo para obtener el mensaje que envía el backend
                    const errorBody = await data.json().catch(() => ({}));
                    const errorMsg = errorBody.error || "Internal server error";
                    throw new Error(errorMsg);
                } else if (data.status === 404) {
                    throw new Error('Order not found');
                } else if (data.status === 200) {
                    if (!data.ok) throw new Error('No autorizado');

                    const parsedData = await data.json();
                    console.log("STATUS: " + data.status);
                    console.log("RESPONSE: " + JSON.stringify(parsedData, null, 2));

                    const container = document.getElementById("acceptedOrderList");
                    container.innerHTML = ""; // Limpiar contenido previo

                    parsedData.forEach(order => {
                        console.log("Order NAME: " + order.orderDesc);

                        const item = document.createElement("div");
                        item.className = LIST_ITEM;
                        item.innerHTML = `
                        <img src="https://github.com/twbs.png" alt="" width="32" height="32" class="rounded-full flex-shrink-0">
                        <div class="flex gap-2 w-full justify-between items-center">
                            <div class="min-w-0">
                                <h6 class="font-semibold text-sm">${escapeHtml(order.orderDesc)}</h6>
                                <p class="text-gray-500 text-sm">Identificador de la orden: ${escapeHtml(order.orderId)}</p>
                                <p class="text-gray-500 text-sm">Volumen: ${escapeHtml(order.vol)}</p>
                                <p class="text-gray-500 text-sm">Usuario: ${escapeHtml(order.userId)}</p>
                            </div>
                            <small class="text-gray-400 text-xs whitespace-nowrap">${new Date(order.createdAt).toLocaleString()}</small>
                            <button type="button" class="eliminarAcceptedOrderBtn ${BTN_DANGER}">Eliminar</button>
                        </div>`;

                        item.querySelector('.eliminarAcceptedOrderBtn').addEventListener('click', () => eliminarAcceptedOrder(order.orderId, order.userId));

                        container.appendChild(item);
                    });
                }

            }).catch(err => {
                console.error(err);

                // Si fue un 500 mostramos el mensaje del backend, si no, mensaje genérico
                showAlert(err.message.includes("Internal server")
                    ? 'No se pudieron obtener tus pedidos: ' + err.message
                    : 'Sesión inválida. Inicie sesión nuevamente.', 'error');

                sessionStorage.clear();
                setTimeout(() => {
                    window.location.href = '/public/login.html';
                }, 500);
            });

            document.getElementById('volverBtn').addEventListener('click', () => {
                window.location.href = '/public/dashboard.html';
            });
        }
    }

    // Muestra un alert con fade in/out durante `duration` ms
    function showAlert(message, type = 'success', duration = 5000) {
        const alertContainer = document.getElementById('alertContainer');
        if (!alertContainer) return;

        const styles = type === 'error'
            ? 'bg-red-50 border border-red-400 text-red-800'
            : 'bg-green-50 border border-green-400 text-green-800';

        const alertDiv = document.createElement('div');
        alertDiv.className = `${styles} px-4 py-3 rounded mb-3 opacity-0 transition-opacity duration-300`;
        alertDiv.setAttribute('role', 'alert');
        alertDiv.textContent = message;

        alertContainer.appendChild(alertDiv);

        // se quita "opacity-0" en el siguiente frame para que la transicion de fade-in se aplique
        requestAnimationFrame(() => alertDiv.classList.remove('opacity-0'));

        setTimeout(() => {
            alertDiv.classList.add('opacity-0'); // dispara el fade-out
            alertDiv.addEventListener('transitionend', () => alertDiv.remove(), { once: true });
        }, duration);
    }

    // Si venimos de una recarga tras una accion (p.ej. eliminar una orden aceptada), mostramos el alert pendiente
    const pendingAlert = sessionStorage.getItem('pendingAlert');
    if (pendingAlert) {
        sessionStorage.removeItem('pendingAlert');
        showAlert(pendingAlert);
    }

    // Funcion para eliminar una orden aceptada
    window.eliminarAcceptedOrder = async function eliminarAcceptedOrder(orderId, userId) {
        const token = sessionStorage.getItem('token');
        if (!token) {
            showAlert('Token no válido', 'error');
            return;
        }
        const confirmed = await showConfirmModal('¿Estás seguro de eliminar esta orden aceptada?');
        if (confirmed) {
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
                showAlert('Error: ' + err.message, 'error');
            });
        }
    }
})();