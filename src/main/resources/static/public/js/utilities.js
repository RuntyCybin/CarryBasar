// Configuration
const API_URL = 'http://localhost:8080'; // Reemplaza con tu URL real

console.log("LLLLEGO");

async function fetchData() {
    try {
        const response = await fetch('/api/completedusers', {
            method: "GET"
        });

        // Asegúrate de que la respuesta es correcta
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();  // Lee el contenido de la respuesta
        console.log('Datos:', data);

        return data;
    } catch (error) {
        console.error('Error en la solicitud:', error);
    }
}

fetchData();


//:::::::::::::::::::::::::::::::::::::::


// API Service
const api = {
    async getData() {
        fetchCompleto();
    },

    async postData(data) {
        try {
            const response = await fetch(`${API_URL}/data`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            });
            if (!response.ok) throw new Error('Error al enviar los datos');
            return await response.json();
        } catch (error) {
            throw new Error('Error al enviar los datos');
        }
    }
};

async function fetchCompleto() {
    try {
        console.log("LLLLEGO fetchCompleto");
        const response = await fetch(`${API_URL}/api/completedusers`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });

        console.log("response: " + response);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Error:', error);
        throw error;
    }
}

// Versión 1: Manejo detallado de errores
async function fetchUsers() {
    try {

        const response = await fetch(`${API_URL}/api/users`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
                // Si necesitas autorización:
                // 'Authorization': 'Bearer your-token-here'
            }
        });

        // Logging para debug
        console.log('Status:', response.status);
        console.log('Headers:', response.headers);

        if (!response.ok) {
            // Capturamos el mensaje de error del servidor si existe
            const errorData = await response.text();
            console.error('Error response:', errorData);
            throw new Error(`HTTP error! status: ${response.status} - ${errorData}`);
        }

        // Intentamos parsear la respuesta
        const data = await response.json();
        console.log('Data received:', data);
        return data;

    } catch (error) {
        // Discriminamos tipos de error
        if (error instanceof TypeError) {
            // Error de red o CORS
            console.error('Network or CORS error:', error);
            throw new Error('Error de conexión con el servidor');
        } else if (error.name === 'SyntaxError') {
            // Error al parsear JSON
            console.error('JSON parsing error:', error);
            throw new Error('Error en el formato de la respuesta');
        } else {
            // Otros errores
            console.error('Other error:', error);
            throw error;
        }
    }
}

// Versión 2: Implementación con validación de datos
async function fetchUsersWithValidation() {
    try {
        const response = await fetch(`${API_URL}/api/completedusers`);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();

        // Validamos que la respuesta tenga la estructura esperada
        if (!Array.isArray(data)) {
            throw new Error('La respuesta no es un array');
        }

        // Validamos cada objeto del array
        const validUsers = data.every(user =>
            user.id &&
            typeof user.id === 'string' &&
            user.name &&
            typeof user.name === 'string' &&
            user.role &&
            typeof user.role === 'string'
        );

        if (!validUsers) {
            throw new Error('Formato de datos inválido');
        }

        return data;

    } catch (error) {
        console.error('Error fetching users:', error);
        throw new Error('Error al obtener los datos de usuarios');
    }
}

// Ejemplo de uso:
async function init() {

    try {
        const users = await fetchUsers();
        // o const users = await fetchUsersWithValidation();
        console.log('Users:', users);
    } catch (error) {
        console.error('Error en init:', error.message);
    }
}

// UI Functions
function showLoading() {
    loadingElement.classList.add('active');
}

function hideLoading() {
    loadingElement.classList.remove('active');
}

function showError(message) {
    errorElement.textContent = message;
    errorElement.classList.add('active');
}

function hideError() {
    errorElement.classList.remove('active');
}

function renderCards(data) {
    cardsGrid.innerHTML = data.map(item => `
                <div class="card">
                    <h3>${item.title}</h3>
                    <p>${item.description}</p>
                </div>
            `).join('');
}

// Navigation
function showSection(sectionId) {
    sections.forEach(section => {
        section.style.display = section.id === sectionId ? 'block' : 'none';
    });

    if (sectionId === 'data') {
        loadData();
    }
}

// Data Loading
async function loadData() {
    try {
        showLoading();
        hideError();
        const data = await api.getData();
        renderCards(data);
    } catch (error) {
        showError(error.message);
    } finally {
        hideLoading();
    }
}

// Event Listeners
navLinks.forEach(link => {
    link.addEventListener('click', (e) => {
        e.preventDefault();
        const section = e.target.dataset.section;
        showSection(section);
    });
});

dataForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = {
        title: e.target.title.value,
        description: e.target.description.value
    };

    try {
        showLoading();
        hideError();
        await api.postData(formData);
        e.target.reset();
        showSection('data');
    } catch (error) {
        showError(error.message);
    } finally {
        hideLoading();
    }
});

// Initial Load
showSection('home');