(function () {
    const token = sessionStorage.getItem('token');

    // comprobamos si el token existe
    if (!token) {
        window.location.href = '/public/login.html';
        return;
    }

    const username = sessionStorage.getItem('username');
    const roles = sessionStorage.getItem('roles');

    document.getElementById('username').textContent = username;

    // comprobamos el rol de usuario
    if (roles.includes("USER") && roles.includes("TRANSPORTER")) {
        // indicador del rol en la barra de navegacion
        const role_nav_span = document.getElementById("roleUser");
        role_nav_span.textContent = "[TRANSPORTISTA]";

    } else if (roles.includes("USER") && roles.includes("CARRY")) {

    } else if (roles.includes("ADMIN")) {
        window.location.href = '/public/dashboardAdmin.html';
        return;
    }

    document.getElementById('contact-form').addEventListener('submit', function (event) {
        event.preventDefault();
        const name = document.getElementById('name').value;
        const email = document.getElementById('email').value;
        const message = document.getElementById('message').value;

        console.log('Formulario enviado:', { name, email, message });
        showAlert('¡Gracias por contactarnos!');
        this.reset();
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
})();