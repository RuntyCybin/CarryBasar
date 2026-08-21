(function () {

  // ............................................................................
  // metadatos de presentación para el listbox de roles (insignia + texto de apoyo)
  const ROLE_META = {
    USER: {initial: 'U', color: 'bg-gray-500', description: 'Cuenta base para comprar productos y solicitar encargos.'},
    CARRY: {initial: 'C', color: 'bg-blue-600', description: 'Crea encargos para que un transportista los traiga.'},
    TRANSPORTER: {
      initial: 'T',
      color: 'bg-emerald-600',
      description: 'Acepta y entrega los encargos de otros usuarios.'
    }
  };

  function roleMeta(roleName) {
    return ROLE_META[roleName] || {initial: roleName.charAt(0).toUpperCase(), color: 'bg-indigo-500', description: ''};
  }

  // ............................................................................

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
      alertDiv.addEventListener('transitionend', () => alertDiv.remove(), {once: true});
    }, duration);
  }

  document.addEventListener('DOMContentLoaded', function () {

    // ............................................................................
    // recogemos los roles del endpoint publico y montamos el listbox personalizado
    const rolesList = document.getElementById("rolesList");
    const rolesButton = document.getElementById("rolesButton");
    const rolesButtonText = document.getElementById("rolesButtonText");
    const rolesOptions = document.getElementById("rolesOptions");
    rolesList.innerHTML = ""; // Limpiar contenido previo
    rolesOptions.innerHTML = "";

    function updateRolesButtonText() {
      const selected = Array.from(rolesList.selectedOptions).map(opt => opt.value);
      rolesButtonText.textContent = selected.length ? selected.join(', ') : 'Selecciona el rol';
      rolesButtonText.classList.toggle('text-gray-500', selected.length === 0);
      rolesButtonText.classList.toggle('text-gray-900', selected.length > 0);
    }

    function toggleRolesPanel(show) {
      const expand = show === undefined ? rolesOptions.classList.contains('hidden') : show;
      rolesOptions.classList.toggle('hidden', !expand);
      rolesButton.setAttribute('aria-expanded', String(expand));
    }

    rolesButton.addEventListener('click', () => toggleRolesPanel());
    document.addEventListener('click', (e) => {
      if (!e.target.closest('#rolesButton') && !e.target.closest('#rolesOptions')) {
        toggleRolesPanel(false);
      }
    });

    fetch('/public/usr/listRolesForSignUpForm').then(async res => {
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

        const meta = roleMeta(role);
        const item = document.createElement('li');
        item.setAttribute('role', 'option');
        item.setAttribute('aria-selected', 'false');
        item.dataset.value = role;
        item.className = 'roleOption group relative cursor-pointer select-none py-2 pl-3 pr-9 text-gray-900 hover:bg-blue-600 hover:text-white';
        item.innerHTML = `
                    <div class="flex items-center">
                        <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full ${meta.color} text-xs font-semibold text-white">${meta.initial}</span>
                        <span class="ml-3 block">
                            <span class="block truncate font-medium">${role}</span>
                            <span class="block truncate text-xs text-gray-500 group-hover:text-blue-200">${meta.description}</span>
                        </span>
                    </div>
                    <span class="roleCheck hidden absolute inset-y-0 right-0 flex items-center pr-4 text-blue-600 group-hover:text-white">
                        <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                            <path fill-rule="evenodd" d="M16.704 4.153a.75.75 0 01.143 1.052l-8 10.5a.75.75 0 01-1.127.075l-4.5-4.5a.75.75 0 011.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 011.05-.143z" clip-rule="evenodd" />
                        </svg>
                    </span>`;

        item.addEventListener('click', () => {
          option.selected = !option.selected;
          item.setAttribute('aria-selected', String(option.selected));
          item.querySelector('.roleCheck').classList.toggle('hidden', !option.selected);
          updateRolesButtonText();
        });

        rolesOptions.appendChild(item);
      });

      updateRolesButtonText();
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
        showAlert("Por favor, completa todos los campos.", 'error');
        return;
      }
      if (password !== confirmPassword) {
        showAlert("Las contraseñas no coinciden.", 'error');
        return;
      }
      const formData = {
        username: username,
        email: email,
        password: password,
        roles: Array.from(rolesList.selectedOptions).map(opt => opt.value)
      };

      if (password !== confirmPassword) {
        showAlert("Las contraseñas no coinciden.", 'error');
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

        showAlert("Usuario registrado con exito!");
        setTimeout(() => {
          window.location.href = '/public/login.html';
        }, 500);
      } catch (error) {
        console.error("Error en el registro:", error);
        showAlert("Error al registrar el usuario: " + error.message, 'error');
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