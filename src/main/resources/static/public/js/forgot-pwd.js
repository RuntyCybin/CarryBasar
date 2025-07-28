(function () {
    const formrecoverpwd = document.getElementById('recoverPwdForm');
    formrecoverpwd.addEventListener('submit', async (e) => {
        e.preventDefault();

        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        const passwordRepeat = document.getElementById('passwordRepeat').value;
        const errorDiv = document.getElementById('error');
        errorDiv.textContent = '';

        try {
            if (password !== passwordRepeat) {
                throw new Error('Las contraseñas no coinciden');
                alert('Las contraseñas no coinciden');
                errorDiv.textContent = 'Las contraseñas no coinciden';
            }
            const payload = { username: username, password: password };
            const response = await fetch('/public/usr/recoverpwd', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            }).then(res => {
                if (res.status === 500) {
                    throw new Error('Error interno del servidor');
                } else if (res.status === 200) {
                    console.log("Contraseña cambiada correctamente");
                    return res;
                }

                return res;
            }).then(data => {
                console.log("RESPONSE: " + data.json());
                if (data.status === 200) {
                    alert('Contraseña cambiada correctamente');
                    window.location.href = '/public/login.html';
                } else {
                    throw new Error('Error al cambiar la contraseña');
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

            // Redirigir al dashboard
            // window.location.href = '/public/dashboard.html';
        } catch (error) {
            console.error(error);
            errorDiv.textContent = error.message;
        }
    });
})();


/*
window.addEventListener("DOMContentLoaded", async () => {
    const token = sessionStorage.getItem("token");

}); 
*/