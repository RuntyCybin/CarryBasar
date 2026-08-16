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

  // cargamos los valores de la session
  document.getElementById('username').textContent = username;
  document.getElementById("userNameMod").value = username
  document.getElementById("userEmail").value = email;

  // comprobamos el rol de usuario
  if (userRoles.includes("USER") && userRoles.includes("TRANSPORTER")) {
    // indicador del rol en la barra de navegacion
    document.getElementById("roleUser").textContent = "[TRANSPORTISTA]";
  } else if (userRoles.includes("ADMIN")) {
    window.location.href = '/public/dashboardAdmin.html';
    return;
  }

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

  // ............................................................................
  // Event listener para el SUBMIT del formulario de UPDATE los datos del USUARIO
  document.getElementById("profileForm")
    .addEventListener('submit', function (event) {
      event.preventDefault(); // evita que el formulario se envíe por defecto
      console.log("ACTUALIZAMOS EL PERFIL");

      const selectedRoles = Array.from(rolesSelect.selectedOptions)
        .map(opt => opt.value);
      const formData = {
        username: this.nombre.value,
        email: this.email.value,
        newPassword: this.password.value,
        role: selectedRoles
      };
      console.log("Datos enviados:", formData);
      // PUT al backend
      fetch('/v1/api/user/update', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify(formData)
      })
        .then(async res => {
          // Gestión del 401/403 (no autorizado)
          if (!res) throw new Error('No autorizado');

          // Todo correcto → devolvemos JSON
          return res.json();
        })
        .then(data => {
          showAlert('The user was updated successfully');
          setTimeout(() => {
            sessionStorage.clear();
            window.location.href = '/public/login.html';
          }, 500);
        })
        .catch(err => console.error(err));
    });
  // ............................................................................

  // Muestra un alert con fade in/out durante `duration` ms
  function showAlert(message, duration = 5000) {
    const alertContainer = document.getElementById('alertContainer');
    if (!alertContainer) return;

    const alertDiv = document.createElement('div');
    alertDiv.className = 'bg-green-50 border border-green-400 text-green-800 px-4 py-3 rounded mb-3 opacity-0 transition-opacity duration-300';
    alertDiv.setAttribute('role', 'alert');
    alertDiv.textContent = message;

    alertContainer.appendChild(alertDiv);

    // se quita "opacity-0" en el siguiente frame para que la transicion de fade-in se aplique
    requestAnimationFrame(() => alertDiv.classList.remove('opacity-0'));

    setTimeout(() => {
      alertDiv.classList.add('opacity-0'); // dispara el fade-out
      alertDiv.addEventListener('transitionend', () => alertDiv.remove(), { once: true });
    }, duration);
  }


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