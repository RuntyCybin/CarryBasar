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
            console.log("LLEGO2");
            console.log("RESPONSE: " + JSON.stringify(data, null, 2));
            //document.getElementById('datos').textContent = JSON.stringify(data, null, 2);
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