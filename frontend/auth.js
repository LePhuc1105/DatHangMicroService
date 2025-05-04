// Authentication related functions for login and registration

// Configuration
const USER_SERVICE_URL = 'http://localhost:8083/api/users';
const LOCAL_STORAGE_KEY = 'dathang_user';

// DOM Elements for Login Page
document.addEventListener('DOMContentLoaded', function() {
    // Setup Login Form
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
    
    // Setup Register Form
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
        
        // Add password confirmation validation
        const passwordField = document.getElementById('password');
        const confirmPasswordField = document.getElementById('confirmPassword');
        if (passwordField && confirmPasswordField) {
            confirmPasswordField.addEventListener('input', () => {
                if (passwordField.value !== confirmPasswordField.value) {
                    confirmPasswordField.setCustomValidity('Mật khẩu không khớp');
                } else {
                    confirmPasswordField.setCustomValidity('');
                }
            });
        }
    }
    
    // Check if user is already logged in
    checkAuthStatus();
});

// Function to handle login form submission
async function handleLogin(event) {
    event.preventDefault();
    
    const loginError = document.getElementById('loginError');
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    // Hide previous error messages
    loginError.classList.add('d-none');
    
    // Validate input
    if (!username || !password) {
        showError(loginError, 'Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu');
        return;
    }
    
    // Create login data
    const loginData = { username, password };
    
    try {
        // Submit login request
        const response = await fetch(`${USER_SERVICE_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(loginData)
        });
        
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'Đăng nhập thất bại');
        }
        
        console.log("Login response data:", data);
        
        // Store user data in localStorage
        localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify({
            id: data.id,
            username: data.username,
            email: data.email,
            token: data.token,
            address: data.address,
            phone: data.phone,
            fullName: data.fullName || '' // Store fullName from login response
        }));
        
        // Redirect to main page
        window.location.href = 'index.html';
    } catch (error) {
        console.error('Login error:', error);
        showError(loginError, error.message || 'Đăng nhập thất bại. Vui lòng kiểm tra tên đăng nhập và mật khẩu.');
    }
}

// Function to handle registration form submission
async function handleRegister(event) {
    event.preventDefault();
    
    const registerError = document.getElementById('registerError');
    const registerSuccess = document.getElementById('registerSuccess');
    
    // Hide previous messages
    registerError.classList.add('d-none');
    registerSuccess.classList.add('d-none');
    
    // Get form data
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const email = document.getElementById('email').value || '';
    const phone = document.getElementById('phone').value || '';
    const address = document.getElementById('address').value || '';
    // Get fullName from form if available
    const fullName = document.getElementById('fullName') ? 
                     document.getElementById('fullName').value : 
                     username; // Default to username if field doesn't exist
    
    // Validate input
    if (!username || !password) {
        showError(registerError, 'Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu');
        return;
    }
    
    // Validate username length
    if (username.length < 4) {
        showError(registerError, 'Tên đăng nhập phải có ít nhất 4 ký tự');
        return;
    }
    
    // Validate password length
    if (password.length < 6) {
        showError(registerError, 'Mật khẩu phải có ít nhất 6 ký tự');
        return;
    }
    
    // Validate passwords match
    if (password !== confirmPassword) {
        showError(registerError, 'Mật khẩu xác nhận không khớp');
        return;
    }
    
    // Create user data
    const userData = {
        username,
        password,
        email,
        phone,
        address,
        fullName // Include fullName in registration data
    };
    
    try {
        // Submit registration request
        const response = await fetch(`${USER_SERVICE_URL}/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(userData)
        });
        
        const data = await response.json();
        console.log('Registration response data:', data);
        
        if (!response.ok) {
            throw new Error(data.message || 'Đăng ký thất bại');
        }
        
        // Show success message
        registerSuccess.innerHTML = `
            <i class="fas fa-check-circle me-2"></i>
            Đăng ký thành công! Bạn có thể <a href="login.html">đăng nhập</a> ngay bây giờ.
        `;
        registerSuccess.classList.remove('d-none');
        
        // Reset form
        document.getElementById('registerForm').reset();
    } catch (error) {
        console.error('Registration error:', error);
        showError(registerError, error.message || 'Đăng ký thất bại. Vui lòng thử lại sau.');
    }
}

// Check if user is already logged in
function checkAuthStatus() {
    const userData = getUserData();
    
    // If on login page and user is already logged in, redirect to main page
    if (userData && window.location.pathname.includes('login.html')) {
        window.location.href = 'index.html';
    }
    
    // If on main page and not logged in, redirect to login page
    if (!userData && 
        !window.location.pathname.includes('login.html') && 
        !window.location.pathname.includes('register.html')) {
        window.location.href = 'login.html';
    }
}

// Get logged in user data from localStorage
function getUserData() {
    const userDataString = localStorage.getItem(LOCAL_STORAGE_KEY);
    if (!userDataString) return null;
    
    try {
        return JSON.parse(userDataString);
    } catch (error) {
        console.error('Error parsing user data:', error);
        return null;
    }
}

// Logout function
function logout() {
    localStorage.removeItem(LOCAL_STORAGE_KEY);
    window.location.href = 'login.html';
}

// Expose logout function to window
window.logout = logout;

// Helper function to display error message
function showError(element, message) {
    element.innerHTML = `<i class="fas fa-exclamation-circle me-2"></i>${message}`;
    element.classList.remove('d-none');
}

// Helper function to include auth token in all API requests
function getAuthorizationHeader() {
    const userData = getUserData();
    return userData ? `Bearer ${userData.token}` : '';
}

// Add auth header to fetch (can be used in script.js)
window.fetchWithAuth = async function(url, options = {}) {
    const token = getAuthorizationHeader();
    if (token) {
        options.headers = options.headers || {};
        options.headers['Authorization'] = token;
    }
    return fetch(url, options);
}; 