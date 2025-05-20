// JS encargado de enviar el formulario para crear un pedido
// Asegúrate de tener el token guardado en sessionStorage (sessionStorage.setItem('token', '...'))

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('createOrderForm');

    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const description = document.getElementById('description').value.trim();
        const volume = parseInt(document.getElementById('volume').value, 10);
        const createdAt = new Date().toISOString().slice(0,19);
        const token = sessionStorage.getItem('token');

        if (!token) {
            alert('Sesión inválida. Inicie sesión nuevamente.');
            window.location.href = '/public/login.html';
            return;
        }

        try {
            const response = await fetch('/v1/api/order/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify({ description, volume, createdAt })
            });

            if (!response.ok) {
                const errorBody = await response.json().catch(() => ({}));
                const message = errorBody.error || 'Error al crear pedido';
                throw new Error(message);
            }

            alert('Pedido creado correctamente!');
            form.reset();
            // Redirige o actualiza lista si es necesario
            window.location.href = 'dashboard.html';
        } catch (err) {
            console.error(err.message);
            alert('No se pudo crear el pedido: ' + err.message);
        }
    });
});