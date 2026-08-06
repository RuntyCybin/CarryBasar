(function () {
    document.addEventListener('DOMContentLoaded', function () {

        // ............................................................................
        // recogemos los roles del endpoint publico para el dropdown
        const rolesList = document.getElementById("rolesList");
        rolesList.innerHTML = ""; // Limpiar contenido previo
        fetch('/public/usr/getPublicRoles').then(async res => {
            // Gestión del 401/403 (no autorizado)
            if (!res.ok) throw new Error('No autorizado');

            // Todo correcto → devolvemos JSON
            return res.json();
        }).then(data => {
            console.log("RESPONSE JSON STRINGFY: " + JSON.stringify(data, null, 2));

            // iteramos el data
            const allRoles = generateAllRoles(data);

            console.log("ALL ROLES: " + allRoles);

            allRoles.forEach(role => {
                const option = document.createElement('option');
                option.value = role;
                option.textContent = role;
                rolesList.appendChild(option);
            });

        });
        // ............................................................................


        // ............................................................................
        // SUBMIT - Evento para el registro de usuario
        const form = document.getElementById('signUpForm');
        form.addEventListener('submit', async function (e) {
            e.preventDefault();

            const username = document.getElementById('userName').value;
            const email = document.getElementById('userEmail').value;
            const password = document.getElementById('userPassword').value;
            const confirmPassword = document.getElementById('userPassword2').value;
            if (!username || !email || !password || !confirmPassword) {
                alert("Por favor, completa todos los campos.");
                return;
            }
            if (password != confirmPassword) {
                alert("Las contraseñas no coinciden.");
                return;
            }
            const formData = {
                username: username,
                email: email,
                password: password,
                roles: Array.from(rolesList.selectedOptions).map(opt => opt.value)
            };

            if (password !== confirmPassword) {
                alert("Las contraseñas no coinciden.");
                return;
            }

            try {
                console.log("Form Data:", formData);
                const response = await fetch('/public/usr/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(formData)
                });

                if (!response.ok) {
                    throw new Error('Error al registrar el usuario');
                }

                alert("Usuario registrado con exito!");
                window.location.href = '/public/login.html';
            } catch (error) {
                console.error("Error en el registro:", error);
                alert("Error al registrar el usuario: " + error.message);
            }
        });

    });
    // ............................................................................


    // ............................................................................
    // LOGOUT
    document.getElementById('logoutBtn').addEventListener('click', () => {
        sessionStorage.clear();
        window.location.href = '/public/login.html';
    });
    // ............................................................................


    function generateAllRoles(data) {
        var aux = [];
        for (let i = 0; i < data.length; i++) {
            const role = data[i];
            aux.push(role.name);
        }
        return aux;
    }

})();