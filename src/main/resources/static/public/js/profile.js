(function () {
    const token = sessionStorage.getItem('token');
    const username = sessionStorage.getItem('username');
    const userRoles = sessionStorage.getItem('roles');
    const email = sessionStorage.getItem('email');

    // comprobamos si el token existe
    if (!token) {
        window.location.href = '/public/login.html';
        return;
    }

    document.getElementById('username').textContent = username;
    document.getElementById("userNameMod").value = username
    document.getElementById("userEmail").value = email;
    const rolesSelect = document.getElementById("rolesList");
    rolesSelect.innerHTML = ""; // Limpiar contenido previo

    fetch('/v1/api/role/listRoles', {
        headers: {
            'Authorization': 'Bearer ' + token
        }
    }).then(async res => {
        // Gestión del 401/403 (no autorizado)
        if (!res.ok) throw new Error('No autorizado');

        // Todo correcto → devolvemos JSON
        return res.json();
    }).then(data => {
        console.log("RESPONSE JSON STRINGFY: " + JSON.stringify(data, null, 2));

        // iteramos el data
        const allRoles = generateAllRoles(data);

        console.log("ALL ROLES: " + allRoles);
        console.log("ROLES FROM SESSION: " + userRoles);

        allRoles.forEach(role => {
            const option = document.createElement('option');
            option.value = role;
            option.textContent = role;
            if (userRoles.includes(role)) {
                option.selected = true;
            }
            rolesSelect.appendChild(option);
        });

    });

    document.getElementById("profileForm").addEventListener('submit', function (event) {
        event.preventDefault(); // evita que el formulario se envíe por defecto
        console.log("ACTUALIZAMOS EL PERFIL");

        const selectedRoles = Array.from(rolesSelect.selectedOptions).map(opt => opt.value);
        const formData = {
            nombre: this.nombre.value,
            email: this.email.value,
            password: this.password.value,
            roles: selectedRoles
        };
        console.log("Datos enviados:", formData);
        // Aquí puedes hacer fetch PUT/POST al backend

    });

    // Logout
    document.getElementById('logoutBtn').addEventListener('click', () => {
        sessionStorage.clear();
        window.location.href = '/public/login.html';
    });



    function generateAllRoles(data) {
        var aux = [];
        for (let i = 0; i < data.length; i++) {
            const role = data[i];
            aux.push(role.name);
        }
        return aux;
    }

})();