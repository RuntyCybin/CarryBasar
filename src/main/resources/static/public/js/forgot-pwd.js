(function () {
  const formrecoverpwd = document.getElementById('recoverPwdForm');
  formrecoverpwd.addEventListener('submit', async (e) => {
    e.preventDefault();

    const userEmail = document.getElementById('useremail').value;
    const password = document.getElementById('password').value;
    const passwordRepeat = document.getElementById('passwordRepeat').value;
    const errorDiv = document.getElementById('error');
    errorDiv.textContent = '';

    try {
      if (password !== passwordRepeat) {
        alert('Las contraseñas no coinciden');
        errorDiv.textContent = 'Las contraseñas no coinciden';
        throw new Error('Las contraseñas no coinciden');
      }
      const payload = {userEmail: userEmail, newPassword: password};
      const response = await fetch('/public/usr/changePassword', {
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
          showAlert('Contraseña cambiada correctamente');
          setTimeout(() => {
            window.location.href = '/public/login.html';
          }, 500);
        } else {
          throw new Error('Error al cambiar la contraseña');
        }
      }).catch(err => {
        console.error(err);
        alert('No se pudo cambiar la contraseña: ' + err.message);

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

  // Muestra un alert con fade in/out durante `duration` ms
  function showAlert(message, duration = 7000) {
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
})();


/*
window.addEventListener("DOMContentLoaded", async () => {
    const token = sessionStorage.getItem("token");

}); 
*/