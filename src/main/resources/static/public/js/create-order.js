// JS encargado de enviar el formulario para crear un pedido
// Asegúrate de tener el token guardado en sessionStorage (sessionStorage.setItem('token', '...'))

document.addEventListener('DOMContentLoaded', () => {
    const token = sessionStorage.getItem('token');
    if (!token) {
        showAlert('Sesión inválida. Inicie sesión nuevamente.', 'error');
        setTimeout(() => {
            window.location.href = '/public/login.html';
        }, 500);
    } else {
        const username = sessionStorage.getItem('username');
        const roles = sessionStorage.getItem('roles');

        console.log("ROLES: " + roles);

        // comprobamos el rol de usuario
        if (roles.includes("USER") && roles.includes("CARRY")) {
            console.log("---ROL USER and CARRY---");
            // indicador del rol en la barra de navegacion
            const role_nav_span = document.getElementById("roleUser");
            role_nav_span.textContent = "[CLIENTE]";
        }

        const form = document.getElementById('createOrderForm');
        if (!form) return;

        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            const description = document.getElementById('description').value.trim();
            const volume = parseInt(document.getElementById('volume').value, 10);
            const price = parseFloat(document.getElementById('price').value);
            const createdAt = new Date().toISOString().slice(0,19);
            const dueDate = document.getElementById('dueDate').value + 'T00:00:00';
            const fromLocation = document.getElementById('fromLocation').value.trim();
            const toLocation = document.getElementById('toLocation').value.trim();
            const token = sessionStorage.getItem('token');
            try {
                const response = await fetch('/v1/api/order', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer ' + token
                    },
                    body: JSON.stringify({ description, volume, price, createdAt, dueDate, toLocation, fromLocation })
                });

                if (!response.ok) {
                    const errorBody = await response.json().catch(() => ({}));
                    const message = errorBody.error || 'Error al crear pedido';
                    throw new Error(message);
                }

                sessionStorage.setItem('pendingAlert', 'Pedido creado correctamente!');
                form.reset();
                // Redirige o actualiza lista si es necesario
                window.location.href = 'dashboard.html';
            } catch (err) {
                console.error(err.message);
                showAlert('No se pudo crear el pedido: ' + err.message, 'error');
            }
        });
    }

});

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

    requestAnimationFrame(() => alertDiv.classList.remove('opacity-0'));

    setTimeout(() => {
        alertDiv.classList.add('opacity-0');
        alertDiv.addEventListener('transitionend', () => alertDiv.remove(), { once: true });
    }, duration);
}