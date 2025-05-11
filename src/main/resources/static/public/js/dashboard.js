(function () {
    const token = sessionStorage.getItem('token');
    const username = sessionStorage.getItem('username');
    if (!token) {
        window.location.href = '/public/login.html';
        return;
    }

    document.getElementById('username').textContent = username;

    // Ejemplo de fetch con autenticación
    fetch('/v1/api/order/my-orders', {
        headers: {
            'Authorization': 'Bearer ' + token
        }
    })
        .then(res => {
            if (!res.ok) throw new Error('No autorizado');
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
            alert('Sesión inválida. Inicie sesión nuevamente.');
            sessionStorage.clear();
            window.location.href = '/public/login.html';
        });

    // Logout
    /*document.getElementById('logoutBtn').addEventListener('click', () => {
        sessionStorage.clear();
        window.location.href = '/public/login.html';
    });*/
})();