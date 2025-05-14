window.addEventListener("DOMContentLoaded", async () => {
    const token = sessionStorage.getItem("token");
    console.log("0.token: " + token);

    // Check token
    if (token) {
        console.log("1.token: " + token);
        try {
            const response = await fetch("/public/usr/check", {
                headers: {
                    "Authorization": "Bearer " + token
                }
            });

            if (response.ok) {
                // Usuario autenticado, redirigir
                window.location.href = "/dashboard.html";
                return;
            } else {
                console.log("Token inválido o expirado.");
            }
        } catch (error) {
            console.error("Error comprobando token:", error);
        }
    }

    // Manejo de login si el usuario no está autenticado
    const form = document.getElementById('loginForm');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        const errorDiv = document.getElementById('error');
        errorDiv.textContent = '';

        try {
            const response = await fetch('/public/usr/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                throw new Error('Usuario o contraseña incorrectos');
            }

            const data = await response.json();

            // Guardar en sessionStorage
            sessionStorage.setItem('token', data.jwt);
            sessionStorage.setItem('username', data.username);
            sessionStorage.setItem('email', data.email);
            sessionStorage.setItem('roles', JSON.stringify(data.roles));

            // Redirigir al dashboard
            window.location.href = '/public/dashboard.html';
        } catch (error) {
            console.error(error);
            errorDiv.textContent = error.message;
        }
    });
});