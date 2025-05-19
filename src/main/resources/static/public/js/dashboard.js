(function () {
    const token = sessionStorage.getItem('token');
    const username = sessionStorage.getItem('username');
    const roles = sessionStorage.getItem('roles');

    // comprobamos si el token existe
    if (!token) {
        window.location.href = '/public/login.html';
        return;
    }

    document.getElementById('username').textContent = username;

    // comprobamos el rol de usuario
    if (roles.includes("USER") && roles.includes("TRANSPORTER")) {
        console.log("ROL USER and TRANSPORTER");

        const descripcionDashboard = document.getElementById("descDashboard");
        descripcionDashboard.innerHTML = `<p id="descDashboard">Hola <span>${username}</span> selecciona algun order para llevar.</p>`;

        // mostramos orders que se pueden llevar
        fetch('/v1/api/order/all', {
            headers: {
                'Authorization': 'Bearer ' + token
            }
        })
            .then(async res => {

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

            })
            .then(data => {
                console.log("RESPONSE: " + JSON.stringify(data, null, 2));
                const listContainer = document.getElementById("ordersList");
                listContainer.innerHTML = ""; // Limpiar contenido previo
                // iteramos el data
                for (let i = 0; i < data.length; i++) {
                    const order = data[i];
                    console.log(`Pedido ${i + 1}: ${order.description}, volumen ${order.volume}, creado el ${order.createdAt}`);

                    const item = document.createElement("a");
                    item.href = "#";
                    item.className = "list-group-item list-group-item-action d-flex gap-3 py-3";

                    item.innerHTML = `
                    <img src="https://github.com/twbs.png" alt="" width="32" height="32"
                        class="rounded-circle flex-shrink-0">
                    <div class="d-flex gap-2 w-100 justify-content-between">
                    <div>
                        <h6 class="mb-0">Pedido ${i + 1}</h6>
                        <p class="mb-0 opacity-75">${order.description}</p>
                        <p class="mb-0 opacity-75">Volumen: ${order.volume}</p>
                    </div>
                    <small class="opacity-50 text-nowrap">${new Date(order.createdAt).toLocaleString()}</small>
                    </div>
                `;

                    listContainer.appendChild(item);
                }
            })
            .catch(err => {
                console.error(err);

                // Si fue un 500 mostramos el mensaje del backend, si no, mensaje genérico
                alert(err.errorMsg.includes("Internal server")
                    ? 'No se pudieron obtener tus pedidos: ' + err.message
                    : 'Sesión inválida. Inicie sesión nuevamente.');

                sessionStorage.clear();
                window.location.href = '/public/login.html';
            });

    } else if (roles.includes("USER") && roles.includes("CARRY")) {
        console.log("ROL USER and CARRY");

        const descripcionDashboard = document.getElementById("descDashboard");
        descripcionDashboard.innerHTML = `<p id="descDashboard">Listado de orders creados por <span>${username}</span>.</p>`;

        // mostramos orders que tiene el usuario creados
        fetch('/v1/api/order/my-orders', {
            headers: {
                'Authorization': 'Bearer ' + token
            }
        })
            .then(async res => {

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

            })
            .then(data => {
                console.log("RESPONSE: " + JSON.stringify(data, null, 2));
                const listContainer = document.getElementById("ordersList");
                listContainer.innerHTML = ""; // Limpiar contenido previo
                // iteramos el data
                for (let i = 0; i < data.length; i++) {
                    const order = data[i];
                    console.log(`Pedido ${i + 1}: ${order.description}, volumen ${order.volume}, creado el ${order.createdAt}`);

                    const item = document.createElement("a");
                    item.href = "#";
                    item.className = "list-group-item list-group-item-action d-flex gap-3 py-3";

                    item.innerHTML = `
                    <img src="https://github.com/twbs.png" alt="" width="32" height="32"
                        class="rounded-circle flex-shrink-0">
                    <div class="d-flex gap-2 w-100 justify-content-between">
                    <div>
                        <h6 class="mb-0">Pedido ${i + 1}</h6>
                        <p class="mb-0 opacity-75">${order.description}</p>
                        <p class="mb-0 opacity-75">Volumen: ${order.volume}</p>
                    </div>
                    <small class="opacity-50 text-nowrap">${new Date(order.createdAt).toLocaleString()}</small>
                    </div>
                `;

                    listContainer.appendChild(item);
                }
            })
            .catch(err => {
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





    // Logout
    /*document.getElementById('logoutBtn').addEventListener('click', () => {
        sessionStorage.clear();
        window.location.href = '/public/login.html';
    });*/
})();