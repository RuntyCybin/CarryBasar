(function () {
    const token = sessionStorage.getItem('token');

    // comprobamos si el token existe
    if (!token) {
        window.location.href = '/public/login.html';
        return;
    } else {
        const username = sessionStorage.getItem('username');
        const roles = sessionStorage.getItem('roles');
    }

    document.getElementById('username').textContent = username;

    // comprobamos el rol de usuario
    if (roles.includes("USER") && roles.includes("TRANSPORTER")) {


    } else if (roles.includes("USER") && roles.includes("CARRY")) {

    } else if (roles.includes("ADMIN")) {
        window.location.href = '/public/dashboardAdmin.html';
        return;
    }

})();