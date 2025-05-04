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
let selectedQuantity = 1; // Default quantity to 1
let isUserValid = false; // Track if user is valid
let userData = null;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    fetchProducts();
    setupEventListeners();
    
    loadUserData();
});

// Load user data from auth
function loadUserData() {
    // Get user data from auth.js
    const authUser = getUserData();
    
    if (authUser) {
        userData = authUser;
        
        // Update display name in navbar
        const userDisplayName = document.getElementById('userDisplayName');
        if (userDisplayName) {
            userDisplayName.textContent = userData.fullName || userData.username;
        }
        
        // Pre-fill user form if it exists
        if (userForm) {
            usernameField.value = userData.fullName || userData.username;
            emailField.value = userData.email;
            addressField.value = userData.address || '';
            phoneField.value = userData.phone || '';
            
            // Auto-submit the form to validate user info
            handleUserSubmit(new Event('submit'));
        }
    }
}

// Event Listeners
function setupEventListeners() {
    if (userForm) {
    userForm.addEventListener('submit', handleUserSubmit);
    }
    
    if (submitOrderButton) {
    submitOrderButton.addEventListener('click', handleOrderSubmit);
    }
    
    // Add retry functionality
    const retryButton = document.getElementById('retry-fetch');
    if (retryButton) {
        retryButton.addEventListener('click', fetchProducts);
    }
    
    // Setup profile button actions
    const btnMyProfile = document.getElementById('btnMyProfile');
    if (btnMyProfile) {
        btnMyProfile.addEventListener('click', (e) => {
            e.preventDefault();
            // Show profile info in a modal or alert
            alert('Tính năng đang phát triển');
        });
    }
    
    const btnMyOrders = document.getElementById('btnMyOrders');
    if (btnMyOrders) {
        btnMyOrders.addEventListener('click', (e) => {
            e.preventDefault();
            // Show orders in a modal or navigate to orders page
            alert('Tính năng đang phát triển');
        });
    }
}

// Function to fetch and display products
async function fetchProducts() {
    try {
        showLoading();
        let products = [];
        try {
            // Use fetchWithAuth instead of fetchWithCORS
            const response = await window.fetchWithAuth('http://localhost:8082/api/products/', {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                mode: 'cors'
            });
            
            if (response.ok) {
                products = await response.json();
                console.log('Successfully fetched products from API:', products);
            } else {
                console.error('Server responded with status:', response.status);
            }
        } catch (error) {
            console.error('Direct API call failed:', error);
        }
        
        if (Array.isArray(products) && products.length > 0) {
        displayProducts(products);
            // Update product count
            const productCount = document.getElementById('product-count');
            if (productCount) {
                productCount.textContent = products.length;
            }
        } else {
            productList.innerHTML = '<div class="alert alert-warning">Không có sản phẩm nào để hiển thị</div>';
            const productCount = document.getElementById('product-count');
            if (productCount) {
                productCount.textContent = '0';
            }
        }
    } catch (error) {
    } finally {
        hideLoading();
    }
}

// Display loading indicator
function showLoading() {
    productList.innerHTML = '<div class="text-center p-4"><div class="spinner-border text-primary" role="status"></div><p class="mt-2">Đang tải sản phẩm...</p></div>';
}

function hideLoading() {
    // Loading indicator will be replaced when products are displayed
}

// Display products in dropdown
function displayProducts(products) {
    if (!Array.isArray(products) || products.length === 0) {
        productList.innerHTML = '<div class="alert alert-warning">Không có sản phẩm nào để hiển thị</div>';
        return;
    }
    
    productList.innerHTML = products.map(product => `
        <div class="product-item" data-id="${product.id}" onclick="selectProduct(${product.id}, '${product.name}', ${product.price})">
            <div class="product-details">
            <h6>${product.name}</h6>
                <p class="price">${formatPrice(product.price)} VND</p>
                <p class="description">${product.description || 'Không có mô tả'}</p>
                <p class="stock ${product.quantity > 10 ? 'text-success' : 'text-warning'}">
                    <i class="fas ${product.quantity > 10 ? 'fa-check-circle' : 'fa-exclamation-circle'}"></i>
                    Còn ${product.quantity || 'N/A'} sản phẩm
                </p>
            </div>
        </div>
    `).join('');
}

// Handle product selection - Make this globally accessible
window.selectProduct = function(id, name, price) {
    // Remove selected class from all products
    document.querySelectorAll('.product-item').forEach(item => {
        item.classList.remove('selected');
    });
    
    // Add selected class to clicked product
    document.querySelector(`.product-item[data-id="${id}"]`).classList.add('selected');
    
    // Update selected product
    selectedProduct = { id, name, price };
    selectedQuantity = 1; // Reset quantity to 1 when selecting new product
    
    // Update quantity display
    document.getElementById('selectedQuantity').textContent = selectedQuantity;
    
    // Enable quantity buttons
    document.getElementById('decreaseQuantity').disabled = false;
    document.getElementById('increaseQuantity').disabled = false;
    
    // Update order summary
    updateOrderSummary();
    
    // Enable submit button if user data exists
    if (userData) {
        submitOrderButton.disabled = false;
    }
};

// Update quantity function - Make this globally accessible
window.updateQuantity = function(change) {
    if (!selectedProduct) return;
    
    const newQuantity = selectedQuantity + change;
    
    // Ensure quantity is between 1 and 99
    if (newQuantity >= 1 && newQuantity <= 99) {
        selectedQuantity = newQuantity;
        document.getElementById('selectedQuantity').textContent = selectedQuantity;
        updateOrderSummary();
    }
    
    // Disable decrease button if quantity is 1
    document.getElementById('decreaseQuantity').disabled = (selectedQuantity <= 1);
    
    // Disable increase button if quantity is 99
    document.getElementById('increaseQuantity').disabled = (selectedQuantity >= 99);
};

// Update Order Summary
function updateOrderSummary() {
    if (selectedProduct) {
        document.getElementById('selectedProductName').textContent = selectedProduct.name;
        document.getElementById('productPrice').textContent = formatPrice(selectedProduct.price) + ' VND';
        const total = selectedProduct.price * selectedQuantity;
        document.getElementById('totalPrice').textContent = formatPrice(total) + ' VND';
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
        // Use authenticated user data if available
        if (!userData) {
            userData = {
                id: 1, // Will be replaced with real ID from auth
                username: formData.username,
                email: formData.email,
                address: formData.address,
                phone: formData.phone
            };
        } else {
            // Update user data with form values
            userData.address = formData.address;
            userData.phone = formData.phone;
        }
        
        hideError(userErrorMessage);
        showSuccess('Thông tin người dùng đã được xác nhận');
        
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
    if (!userData || !selectedProduct) {
        showError(userErrorMessage, 'Vui lòng nhập thông tin người dùng và chọn sản phẩm trước khi đặt hàng!');
        return;
    }
    
    // Hiển thị loading
    submitOrderButton.disabled = true;
    submitOrderButton.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang xử lý...';
    
    // Tính tổng tiền đơn hàng
    const totalAmount = selectedProduct.price * selectedQuantity;
    
    const orderData = {
        customerId: userData.id,
        customerName: userData.username,
        customerAddress: userData.address,
        customerEmail: userData.email,
        customerPhone: userData.phone,
        productId: selectedProduct.id,
        quantity: selectedQuantity,
        unitPrice: selectedProduct.price,
        totalAmount: totalAmount,
        notes: 'Đơn hàng đặt từ hệ thống web'
    };
    
    try {
        // Gửi yêu cầu đặt hàng sử dụng fetchWithAuth
        const response = await window.fetchWithAuth('http://localhost:8081/orders', {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            mode: 'cors',
            body: JSON.stringify(orderData)
        });
        
        const responseData = await response.json();
        
        if (!response.ok) {
            console.error('Order API responded with status:', response.status);
            let errorMessage = responseData.message || `Không thể tạo đơn hàng: ${response.status}`;
            throw new Error(errorMessage);
        }
        
        console.log('Order created successfully:', responseData);
        
        // Hiển thị thông báo thành công với thông tin đơn hàng
        const successMsg = `
            <div>
                <strong>Đặt hàng thành công!</strong>
                <p>Mã đơn hàng: #${responseData.orderId}</p>
                <p>Sản phẩm: ${selectedProduct.name}</p>
                <p>Số lượng: ${selectedQuantity}</p>
                <p>Tổng tiền: ${formatPrice(totalAmount)} VND</p>
                <p>Chúng tôi sẽ liên hệ với bạn qua email để xác nhận đơn hàng.</p>
            </div>
        `;
        
        showSuccess(successMsg);
        resetForm();
    } catch (error) {
        console.error('Order submission error:', error);
        showError(userErrorMessage, 'Lỗi đặt hàng: ' + error.message);
    } finally {
        // Khôi phục nút đặt hàng
        submitOrderButton.disabled = false;
        submitOrderButton.innerHTML = '<i class="fas fa-check me-2"></i>Đặt Hàng';
    }
}

// Utility Functions
function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN').format(price);
}

function showError(element, message) {
    element.innerHTML = `<i class="fas fa-exclamation-circle me-2"></i>${message}`;
    element.classList.remove('d-none');
    setTimeout(() => {
        hideError(element);
    }, 5000);
}

function hideError(element) {
    element.classList.add('d-none');
}

function showSuccess(message) {
    successMessage.innerHTML = `<i class="fas fa-check-circle me-2"></i>${message}`;
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
    
    // Reset order summary
    document.getElementById('selectedProductName').textContent = 'Chưa chọn';
    document.getElementById('selectedQuantity').textContent = '0';
    document.getElementById('productPrice').textContent = '0 VND';
    document.getElementById('totalPrice').textContent = '0 VND';
}
