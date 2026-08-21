(function () {
  const token = sessionStorage.getItem('token');

  const BTN_PRIMARY = 'border border-blue-600 text-blue-600 hover:bg-blue-600 hover:text-white px-4 py-2 rounded transition-colors font-medium';
  const BTN_SUCCESS = 'border border-green-600 text-green-600 hover:bg-green-600 hover:text-white px-4 py-2 rounded transition-colors font-medium';
  const BTN_DANGER = 'border border-red-600 text-red-600 hover:bg-red-600 hover:text-white px-4 py-2 rounded transition-colors font-medium';
  const LIST_ITEM = 'flex items-center gap-3 p-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors';

  // Escapa texto antes de insertarlo en innerHTML para evitar XSS
  function escapeHtml(value) {
    const div = document.createElement('div');
    div.textContent = value ?? '';
    return div.innerHTML;
  }

  // Modal de detalle de pedido
  const orderModal = document.getElementById('orderModal');

  function openOrderModal() {
    orderModal.classList.remove('hidden');
    orderModal.classList.add('flex');
  }

  function closeOrderModal() {
    orderModal.classList.add('hidden');
    orderModal.classList.remove('flex');
  }

  function showOrderModal(order) {
    document.getElementById('orderModalDescription').textContent = order.description ?? '';
    document.getElementById('orderModalId').textContent = order.orderId ?? '';
    document.getElementById('orderModalVolume').textContent = order.volume ?? '';
    document.getElementById('orderModalPrice').textContent = order.price != null
      ? order.price.toLocaleString('es-ES', {style: 'currency', currency: 'EUR'})
      : 'Sin especificar';
    document.getElementById('orderModalFrom').textContent = order.fromLocation ?? '';
    document.getElementById('orderModalTo').textContent = order.toLocation ?? '';
    document.getElementById('orderModalCreated').textContent = order.createdAt ? new Date(order.createdAt).toLocaleString() : '';
    document.getElementById('orderModalDue').textContent = order.dueDate ? new Date(order.dueDate).toLocaleString() : '';
    openOrderModal();
  }

  if (orderModal) {
    document.getElementById('orderModalCloseBtn').addEventListener('click', closeOrderModal);
    document.getElementById('orderModalCloseBtnBottom').addEventListener('click', closeOrderModal);
    orderModal.addEventListener('click', (e) => {
      if (e.target === orderModal) closeOrderModal();
    });
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') closeOrderModal();
    });
  }

  // Modal de confirmación (sustituye a confirm())
  const confirmModal = document.getElementById('confirmModal');

  function showConfirmModal(message) {
    return new Promise((resolve) => {
      if (!confirmModal) {
        resolve(window.confirm(message));
        return;
      }

      document.getElementById('confirmModalMessage').textContent = message;
      confirmModal.classList.remove('hidden');
      confirmModal.classList.add('flex');

      const yesBtn = document.getElementById('confirmModalYesBtn');
      const noBtn = document.getElementById('confirmModalNoBtn');

      function cleanup(result) {
        confirmModal.classList.add('hidden');
        confirmModal.classList.remove('flex');
        yesBtn.removeEventListener('click', onYes);
        noBtn.removeEventListener('click', onNo);
        confirmModal.removeEventListener('click', onBackdrop);
        document.removeEventListener('keydown', onKeydown);
        resolve(result);
      }

      function onYes() { cleanup(true); }
      function onNo() { cleanup(false); }
      function onBackdrop(e) { if (e.target === confirmModal) cleanup(false); }
      function onKeydown(e) { if (e.key === 'Escape') cleanup(false); }

      yesBtn.addEventListener('click', onYes);
      noBtn.addEventListener('click', onNo);
      confirmModal.addEventListener('click', onBackdrop);
      document.addEventListener('keydown', onKeydown);
    });
  }

  // Modal de confirmación de precio (aceptar order como TRANSPORTER)
  const priceConfirmModal = document.getElementById('priceConfirmModal');
  const priceConfirmStep = document.getElementById('priceConfirmStep');
  const priceInputStep = document.getElementById('priceInputStep');
  let priceConfirmOrder = null;

  function openPriceConfirmModal() {
    priceConfirmModal.classList.remove('hidden');
    priceConfirmModal.classList.add('flex');
  }

  function closePriceConfirmModal() {
    priceConfirmModal.classList.add('hidden');
    priceConfirmModal.classList.remove('flex');
    priceConfirmStep.classList.remove('hidden');
    priceInputStep.classList.add('hidden');
    document.getElementById('priceInputField').value = '';
    priceConfirmOrder = null;
  }

  function showPriceConfirmModal(order) {
    priceConfirmOrder = order;
    document.getElementById('priceConfirmAmount').textContent = order.price != null
      ? order.price.toLocaleString('es-ES', {style: 'currency', currency: 'EUR'})
      : 'sin especificar';
    priceConfirmStep.classList.remove('hidden');
    priceInputStep.classList.add('hidden');
    openPriceConfirmModal();
  }

  if (priceConfirmModal) {
    document.getElementById('priceConfirmYesBtn').addEventListener('click', () => {
      const order = priceConfirmOrder;
      closePriceConfirmModal();
      if (order) aceptarOrder(order);
    });

    document.getElementById('priceConfirmNoBtn').addEventListener('click', () => {
      priceConfirmStep.classList.add('hidden');
      priceInputStep.classList.remove('hidden');
      document.getElementById('priceInputField').focus();
    });

    document.getElementById('priceInputCancelBtn').addEventListener('click', closePriceConfirmModal);

    document.getElementById('priceInputSubmitBtn').addEventListener('click', () => {
      const order = priceConfirmOrder;
      const newPrice = parseFloat(document.getElementById('priceInputField').value);

      if (!order || Number.isNaN(newPrice) || newPrice < 0) {
        alert('Introduce un precio válido.');
        return;
      }

      const token = sessionStorage.getItem('token');
      fetch(`/v1/api/order/suggestedPrice?orderId=${order.orderId}&price=${newPrice}`, {
        method: 'POST',
        headers: {
          'Authorization': 'Bearer ' + token
        }
      }).then(async res => {
        if (!res.ok) {
          const errorBody = await res.json().catch(() => ({}));
          const errorMsg = errorBody.error || 'Error al enviar el precio propuesto';
          throw new Error(errorMsg);
        }
        return res.text();
      }).then(msg => {
        console.log('Precio propuesto enviado:', msg);
        showAlert('Precio propuesto enviado correctamente.');
      }).catch(err => {
        console.error(err);
        alert('Error: ' + err.message);
      });

      closePriceConfirmModal();
    });

    priceConfirmModal.addEventListener('click', (e) => {
      if (e.target === priceConfirmModal) closePriceConfirmModal();
    });
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && !priceConfirmModal.classList.contains('hidden')) closePriceConfirmModal();
    });
  }

  // comprobamos si el token existe
  if (!token) {
    window.location.href = '/public/login.html';
    return;
  } else {
    const username = sessionStorage.getItem('username');
    const roles = sessionStorage.getItem('roles');
    document.getElementById('username').textContent = username;

    // comprobamos el rol de usuario
    if (roles.includes("USER") && roles.includes("TRANSPORTER")) {
      console.log("---DASHBOARD - ROL USER and TRANSPORTER---");

      // indicador del rol en la barra de navegacion
      const role_nav_span = document.getElementById("roleUser");
      role_nav_span.textContent = "[TRANSPORTISTA]";

      // texto del usuario tipo TRANSPORTER
      const descripcionDashboard = document.getElementById("descDashboard");
      descripcionDashboard.innerHTML = `<p id="descDashboard">Hola <span class="font-semibold">${escapeHtml(username)}</span> selecciona algun order para llevar.</p>`;

      // menu de botones superior derecha
      const btnsCerrarCrear = document.getElementById("salirCrearBtns");
      btnsCerrarCrear.innerHTML = `
            <div class="flex gap-2" id="salirCrearBtns">
                <button type="button" id="acceptedOrderstBtn" class="${BTN_PRIMARY}">Ordenes aceptadas</button>
                <button type="button" id="logoutBtn" class="${BTN_PRIMARY}">Salir</button>
            </div>`;

      // mostramos orders que se pueden llevar
      fetch('/v1/api/order/all', {
        headers: {
          'Authorization': 'Bearer ' + token
        }
      }).then(async res => {

        // Gestión del 500
        if (res.status === 500) {
          // Leemos el cuerpo para obtener el mensaje que envía el backend
          const errorBody = await res.json().catch(() => ({}));
          const errorMsg = errorBody.error || "Internal server error";
          throw new Error(errorMsg);
        }

        // Gestión del 401/403 (no autorizado)
        if (!res.ok) throw new Error('No autorizado');

        // Todo correcto → devolvemos JSON
        return res.json();

      }).then(data => {
        console.log("RESPONSE: " + JSON.stringify(data, null, 2));
        const listContainer = document.getElementById("ordersList");
        listContainer.innerHTML = ""; // Limpiar contenido previo

        // iteramos el data
        for (let i = 0; i < data.length; i++) {
          const order = data[i];
          console.log(`Pedido ${order.orderId}: ${order.description}, volumen ${order.volume}, creado el ${order.createdAt}`);

          const item = document.createElement("div");
          item.innerHTML = `
            <img src="https://github.com/twbs.png" alt="" width="32" height="32"
                class="rounded-full flex-shrink-0">
            <div class="flex gap-2 w-full">
                <div class="basis-[30%] min-w-0">
                    <h6 class="font-semibold text-sm">${escapeHtml(order.description)}</h6>
                    <p class="text-gray-500 text-sm">Volumen: ${escapeHtml(order.volume)}</p>
                </div>
                <div class="basis-[20%] flex items-center justify-center">
                  <button type="button" class="aceptarBtn ${BTN_SUCCESS}">Aceptar</button>
                </div>
                <div class="basis-[50%] flex gap-3">
                    <div class="flex flex-col items-start basis-1/2">
                        <small class="text-gray-400 text-xs whitespace-nowrap"><span class="block">Fecha límite: </span>${new Date(order.dueDate).toLocaleString()}</small>
                    </div>
                    <div class="flex flex-col items-start basis-1/2">
                        <small class="text-gray-400 text-xs whitespace-nowrap"><span class="block">Origen: </span>${escapeHtml(order.fromLocation)}</small>
                        <small class="text-gray-400 text-xs whitespace-nowrap"><span class="block">Destino: </span>${escapeHtml(order.toLocation)}</small>
                    </div>
                </div>
            </div>`;

          //item.href = "accept-order.html";

          item.className = LIST_ITEM + ' cursor-pointer';
          item.addEventListener('click', () => showOrderModal(order));
          item.querySelector('.aceptarBtn').addEventListener('click', (e) => {
            e.stopPropagation();
            showPriceConfirmModal(order);
          });

          listContainer.appendChild(item);
        }
      }).catch(err => {
        console.error(err);

        // Si fue un 500 mostramos el mensaje del backend, si no, mensaje genérico
        alert(err.message.includes("Internal server")
          ? 'No se pudieron obtener tus pedidos: ' + err.message
          : 'Sesión inválida. Inicie sesión nuevamente.');

        sessionStorage.clear();
        window.location.href = '/public/login.html';
      });

    } else if (roles.includes("USER") && roles.includes("CARRY")) {
      console.log("---ROL USER and CARRY---");

      // indicador del rol en la barra de navegacion
      const role_nav_span = document.getElementById("roleUser");
      role_nav_span.textContent = "[CLIENTE]";

      // texto del usuario tipo CARRY
      const descripcionDashboard = document.getElementById("descDashboard");
      descripcionDashboard.innerHTML = `<p id="descDashboard">Listado de orders creados por <span class="font-semibold">${escapeHtml(username)}</span>.</p>`;

      const btnsCerrarCrear = document.getElementById("salirCrearBtns");
      btnsCerrarCrear.innerHTML = `
            <div class="flex gap-2" id="salirCrearBtns">
                <button type="button" id="logoutBtn" class="${BTN_PRIMARY}">Salir</button>
                <a href="create-order.html" class="${BTN_PRIMARY}">Crear pedido</a>
            </div>`;

      // mostramos orders que tiene el usuario creados
      fetch('/v1/api/order/my-orders', {
        headers: {
          'Authorization': 'Bearer ' + token
        }
      }).then(async res => {
        console.log("STATUS: " + res.status);

        // Gestión del 500
        if (res.status === 500) {
          // Leemos el cuerpo para obtener el mensaje que envía el backend
          const errorBody = await res.json().catch(() => ({}));
          const errorMsg = errorBody.error || "Internal server error";
          throw new Error(errorMsg);
        }

        // Gestión del 400 (no se han recogido orders)
        if (res.status === 400) {
          console.log("No hay orders creados por el usuario");

          const listContainer = document.getElementById("ordersList");
          listContainer.innerHTML = ""; // Limpiar contenido previo
          const item = document.createElement("span");
          item.className = "block text-center text-gray-500 py-6";
          item.innerHTML = `
            <div>
                <h6 class="font-semibold">NO TIENE ORDENES CREADAS</h6>
            </div>`;

          listContainer.appendChild(item);
        }

        // No autorizado
        if (res.status === 401 || res.status === 403) {
          throw new Error('No autorizado');
        }

        // OK
        if (res.status === 200) {
          console.log("Orders obtenidos correctamente");
          res.json().then(data => {
            console.log("RESPONSE: " + JSON.stringify(data, null, 2));
            const listContainer = document.getElementById("ordersList");
            listContainer.innerHTML = ""; // Limpiar contenido previo
            // iteramos el res
            for (let i = 0; i < data.length; i++) {
              const order = data[i];
              console.log(`Pedido ${i + 1}: ${order.fromLocation}, con id ${order.orderId}`);

              const item = document.createElement("a");
              item.href = "#";
              item.innerHTML = `
                <img src="https://github.com/twbs.png" alt="" width="32" height="32"
                    class="rounded-full flex-shrink-0">
                <div class="flex flex-wrap items-center gap-4 w-full">
                  <div class="min-w-0">
                      <h6 class="font-semibold text-sm">${escapeHtml(order.description)}</h6>
                      <p class="text-gray-500 text-sm">Volumen: ${escapeHtml(order.volume)}</p>
                  </div>
                  <div class="flex gap-6">
                    <div>
                      <small class="block text-gray-400 text-xs whitespace-nowrap">Fecha límite: ${new Date(order.dueDate).toLocaleString()}</small>
                    </div>
                    <div>
                      <small class="block text-gray-400 text-xs whitespace-nowrap">Desde: ${escapeHtml(order.fromLocation)}</small>
                      <small class="block text-gray-400 text-xs whitespace-nowrap">Donde: ${escapeHtml(order.toLocation)}</small>
                    </div>
                  </div>
                  <div class="ml-auto">
                    <button type="button" class="eliminarBtn ${BTN_DANGER}">Eliminar</button>
                  </div>
                </div>`;

              item.className = LIST_ITEM;
              item.addEventListener('click', (e) => {
                e.preventDefault();
                showOrderModal(order);
              });
              item.querySelector('.eliminarBtn').addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                eliminarOrder(order.orderId);
              });

              listContainer.appendChild(item);
            }
          }).catch(err => {
            console.error("Error parsing JSON:", err);
          });
          // Todo correcto → devolvemos JSON
          // return res.json();
        }
      }).catch(err => {
        console.error(err);

        // Si fue un 500 mostramos el mensaje del backend, si no, mensaje genérico
        alert(err.message.includes("Internal server")
          ? 'No se pudieron obtener tus pedidos: ' + err.message
          : 'Sesión inválida. Inicie sesión nuevamente.');

        sessionStorage.clear();
        window.location.href = '/public/login.html';
      });
    } else if (roles.includes("ADMIN")) {
      window.location.href = '/public/dashboardAdmin.html';
      return;
    }
  }

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
      alertDiv.addEventListener('transitionend', () => alertDiv.remove(), {once: true});
    }, duration);
  }

  // Si venimos de una recarga tras una accion (p.ej. eliminar un order), mostramos el alert pendiente
  const pendingAlert = sessionStorage.getItem('pendingAlert');
  if (pendingAlert) {
    sessionStorage.removeItem('pendingAlert');
    showAlert(pendingAlert);
  }

  // Funcion para eliminar orders de clientes
  window.eliminarOrder = async function eliminarOrder(idOrder) {
    const token = sessionStorage.getItem('token');
    if (!token) {
      alert('Token no válido');
      return;
    }
    const confirmed = await showConfirmModal('¿Estás seguro de eliminar este order?');
    if (confirmed) {
      fetch(`/v1/api/order/${idOrder}`, {
        method: 'DELETE',
        headers: {
          'Authorization': 'Bearer ' + token,
          'Content-Type': 'application/json'
        }
      }).then(async res => {
        if (!res.ok) {
          const errorBody = await res.json().catch(() => ({}));
          const errorMsg = errorBody.error || 'Error eliminando el order';
          throw new Error(errorMsg);
        }
        sessionStorage.setItem('pendingAlert', 'Order removed successfully!');
        window.location.reload();
      }).catch(err => {
        console.error(err);
        alert('Error: ' + err.message);
      });
    }
  }


  // Funcion para aceptar orders de transportistas
  window.aceptarOrder = async function aceptarOrder(order) {
    const {orderId, description, volume} = order;
    const token = sessionStorage.getItem('token');
    const userId = sessionStorage.getItem('userId');
    sessionStorage.setItem('accepted_order_id', orderId);

    if (!token || !userId) {
      alert('Sesión inválida. Inicie sesión nuevamente.');
      window.location.href = '/public/login.html';
      return;
    }

    fetch(`/v1/api/acceptOrder`, {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        orderId: orderId,
        userId: userId,
        shipAt: new Date().toISOString(),
        shipTo: new Date(Date.now() + 2 * 24 * 60 * 60 * 1000).toISOString(),
        description: description,
        volumen: volume
      })
    }).then(async res => {
      if (res.status === 200) {
        sessionStorage.setItem('pendingAlert', 'Order aceptado correctamente.');
        //window.location.href = '/public/transport-orders.html';
        window.location.reload();
      } else if (res.status === 404) {
        throw new Error('Order no encontrado');
      } else {
        const errorBody = await res.json().catch(() => ({}));
        const errorMsg = errorBody.error || 'Error al aceptar el order';
        throw new Error(errorMsg);
      }
    }).catch(err => {
      console.error(err);
      alert('Error: ' + err.message);
    });
  }

  const acceptedOrderstBtn = document.getElementById('acceptedOrderstBtn');
  if (acceptedOrderstBtn) {
    acceptedOrderstBtn.addEventListener('click', () => {
      window.location.href = '/public/accept-order.html';
    });
  }


  // Logout
  document.getElementById('logoutBtn').addEventListener('click', () => {
    console.log("Logout button clicked");
    sessionStorage.clear();
    window.location.href = '/public/login.html';
  });
})();
