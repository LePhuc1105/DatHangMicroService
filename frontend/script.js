const userForm = document.getElementById('userForm');
const usernameField = document.getElementById('username');
const emailField = document.getElementById('email');
const addressField = document.getElementById('address');
const phoneField = document.getElementById('phone');
const productList = document.getElementById('productList');
const orderSummary = document.getElementById('orderSummary');
const submitOrderButton = document.getElementById('submitOrder');
const userErrorMessage = document.getElementById('userErrorMessage');
const productErrorMessage = document.getElementById('productErrorMessage');
const successMessage = document.getElementById('successMessage');

let products = []; // Store products fetched from Product Service
let selectedProduct = null;
let selectedQuantity = 0;
let isUserValid = false; // Track if user is valid
let userData = null;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    fetchProducts();
    setupEventListeners();
});

// Event Listeners
function setupEventListeners() {
    userForm.addEventListener('submit', handleUserSubmit);
    submitOrderButton.addEventListener('click', handleOrderSubmit);
}

// Function to fetch and display products
async function fetchProducts() {
    try {
        const response = await fetch('http://localhost:8082/api/products/');
        if (!response.ok) throw new Error('Không thể tải danh sách sản phẩm');
        
        const products = await response.json();
        displayProducts(products);
    } catch (error) {
        showError(productErrorMessage, error.message);
    }
}

// Display products in dropdown
function displayProducts(products) {
    productList.innerHTML = products.map(product => `
        <div class="product-item" data-id="${product.id}" onclick="selectProduct(this, ${product.id}, '${product.name}', ${product.price})">
            <h6>${product.name}</h6>
            <p>${formatPrice(product.price)} VND</p>
        </div>
    `).join('');
}

// Handle product selection
function selectProduct(element, id, name, price) {
    // Remove selected class from all products
    document.querySelectorAll('.product-item').forEach(item => {
        item.classList.remove('selected');
    });
    
    // Add selected class to clicked product
    element.classList.add('selected');
    
    // Update selected product
    selectedProduct = { id, name, price };
    
    // Update order summary
    updateOrderSummary();
    
    // Enable submit button if user data exists
    if (userData) {
        submitOrderButton.disabled = false;
    }
}

// Update Order Summary
function updateOrderSummary() {
    if (selectedProduct) {
        document.getElementById('selectedProductName').textContent = selectedProduct.name;
        document.getElementById('productPrice').textContent = formatPrice(selectedProduct.price) + ' VND';
        document.getElementById('totalPrice').textContent = formatPrice(selectedProduct.price) + ' VND';
    }
}

// Handle user form submission (check user)
async function handleUserSubmit(event) {
    event.preventDefault();
    
    const formData = {
        username: usernameField.value,
        email: emailField.value,
        address: addressField.value,
        phone: phoneField.value
    };
    
    try {
        const response = await fetch('http://localhost:8081/api/users/', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });
        
        if (!response.ok) throw new Error('Không thể tạo người dùng');
        
        const data = await response.json();
        userData = data;
        hideError(userErrorMessage);
        
        // Enable submit button if product is selected
        if (selectedProduct) {
            submitOrderButton.disabled = false;
        }
    } catch (error) {
        showError(userErrorMessage, error.message);
    }
}

// Handle order submission
async function handleOrderSubmit() {
    if (!userData || !selectedProduct) return;
    
    const orderData = {
        userId: userData.id,
        productId: selectedProduct.id,
        quantity: 1,
        totalPrice: selectedProduct.price
    };
    
    try {
        const response = await fetch('http://localhost:8083/api/orders/', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(orderData)
        });
        
        if (!response.ok) throw new Error('Không thể tạo đơn hàng');
        
        const data = await response.json();
        showSuccess('Đơn hàng đã được tạo thành công!');
        resetForm();
    } catch (error) {
        showError(userErrorMessage, error.message);
    }
}

// Utility Functions
function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN').format(price);
}

function showError(element, message) {
    element.textContent = message;
    element.classList.remove('d-none');
    setTimeout(() => {
        hideError(element);
    }, 5000);
}

function hideError(element) {
    element.classList.add('d-none');
}

function showSuccess(message) {
    successMessage.textContent = message;
    successMessage.classList.remove('d-none');
    setTimeout(() => {
        successMessage.classList.add('d-none');
    }, 5000);
}

function resetForm() {
    userForm.reset();
    document.querySelectorAll('.product-item').forEach(item => {
        item.classList.remove('selected');
    });
    selectedProduct = null;
    userData = null;
    submitOrderButton.disabled = true;
    updateOrderSummary();
}
