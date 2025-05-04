// Variables
let selectedProduct = null;
let selectedQuantity = 1;
let userData = null;

// DOM elements
const productDetailsElement = document.getElementById('productDetails');
const productControlsElement = document.getElementById('productControls');
const accountInfoSection = document.getElementById('accountInfoSection');
const accountSelect = document.getElementById('accountSelect');
const userForm = document.getElementById('userForm');
const existingUserInfo = document.getElementById('existingUserInfo');
const proceedToCheckoutButton = document.getElementById('proceedToCheckout');
const submitOrderButton = document.getElementById('submitOrder');
const productErrorMessage = document.getElementById('productErrorMessage');
const userErrorMessage = document.getElementById('userErrorMessage');
const successMessage = document.getElementById('successMessage');

// Form fields
const usernameField = document.getElementById('username');
const emailField = document.getElementById('email');
const addressField = document.getElementById('address');
const phoneField = document.getElementById('phone');

// Order summary elements
const selectedProductNameElement = document.getElementById('selectedProductName');
const summaryQuantityElement = document.getElementById('summaryQuantity');
const productPriceElement = document.getElementById('productPrice');
const totalPriceElement = document.getElementById('totalPrice');

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Check if product ID is in URL
    const urlParams = new URLSearchParams(window.location.search);
    const productId = urlParams.get('id');
    
    if (!productId) {
        showError(productErrorMessage, 'Không tìm thấy sản phẩm. Vui lòng quay lại trang chủ.');
        return;
    }
    
    // Load product details
    loadProductDetails(productId);
    
    // Load user data
    loadUserData();
    
    // Setup event listeners
    setupEventListeners();
});

// Load product details
async function loadProductDetails(productId) {
    try {
        const response = await window.fetchWithAuth(`http://localhost:8082/api/products/${productId}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            },
            mode: 'cors'
        });
        
        if (!response.ok) {
            console.error('Error fetching product details:', response.status);
            throw new Error(`Không thể tải thông tin sản phẩm: ${response.status}`);
        }
        
        const product = await response.json();
        selectedProduct = product;
        
        // Display product details
        displayProductDetails(product);
        
    } catch (error) {
    }
}

// Display product details
function displayProductDetails(product) {
    productDetailsElement.innerHTML = `
        <div class="product-detail">
            <h4 class="mb-3">${product.name}</h4>
            <div class="mb-3">
                <span class="badge bg-${product.quantity > 0 ? 'success' : 'danger'} mb-2">
                    ${product.quantity > 0 ? 'Còn hàng' : 'Hết hàng'}
                </span>
                <p class="text-muted">${product.description || 'Không có mô tả'}</p>
            </div>
            <div class="d-flex justify-content-between align-items-center mb-3">
                <span class="text-muted">Đơn giá:</span>
                <span class="price-tag h5 mb-0">${formatPrice(product.price)} VND</span>
            </div>
            <div class="d-flex justify-content-between align-items-center mb-3">
                <span class="text-muted">Còn lại:</span>
                <span>${product.quantity} sản phẩm</span>
            </div>
        </div>
    `;
    
    // Show quantity controls if product is available
    if (product.quantity > 0) {
        productControlsElement.classList.remove('d-none');
        
        // Update the order summary preview
        updateOrderSummary();
    } else {
        productControlsElement.innerHTML = `
            <button class="btn btn-secondary w-100" disabled>
                <i class="fas fa-ban me-2"></i>
                Hết hàng
            </button>
        `;
    }
}

// Load user data
function loadUserData() {
    const savedUserData = localStorage.getItem('dathang_user');
    
    if (savedUserData) {
        try {
            userData = JSON.parse(savedUserData);
            
            // Update user display name - use fullName with fallback to username
            const userDisplayName = document.getElementById('userDisplayName');
            if (userDisplayName) {
                userDisplayName.textContent = userData.fullName || userData.username || 'Tài khoản';
            }
            
            // Update existing user info display
            updateExistingUserInfo();
        } catch (e) {
            console.error('Error parsing user data:', e);
        }
    }
}

// Update existing user info display
function updateExistingUserInfo() {
    if (!userData) return;
    
    // Display fullName in user info block instead of username
    document.getElementById('currentUsername').textContent = userData.fullName || userData.username || '';
    document.getElementById('currentEmail').textContent = userData.email || '';
    document.getElementById('currentAddress').textContent = userData.address || '';
    document.getElementById('currentPhone').textContent = userData.phone || '';
}

// Setup event listeners
function setupEventListeners() {
    // Account select change
    if (accountSelect) {
        accountSelect.addEventListener('change', handleAccountSelectChange);
    }
    
    // Proceed to checkout button
    if (proceedToCheckoutButton) {
        proceedToCheckoutButton.addEventListener('click', showAccountInfo);
    }
    
    // Submit order button
    if (submitOrderButton) {
        submitOrderButton.addEventListener('click', handleOrderSubmit);
    }
    
    // User form
    if (userForm) {
        userForm.addEventListener('submit', handleUserSubmit);
    }
}

// Handle account select change
function handleAccountSelectChange() {
    const selectedOption = accountSelect.value;
    
    if (selectedOption === 'existing') {
        userForm.classList.add('d-none');
        existingUserInfo.classList.remove('d-none');
    } else if (selectedOption === 'new') {
        userForm.classList.remove('d-none');
        existingUserInfo.classList.add('d-none');
    } else {
        userForm.classList.add('d-none');
        existingUserInfo.classList.add('d-none');
    }
}

// Show account info section
function showAccountInfo() {
    accountInfoSection.classList.remove('d-none');
    
    // Scroll to account info section
    accountInfoSection.scrollIntoView({ behavior: 'smooth' });
}

// Update quantity
function updateQuantity(change) {
    if (!selectedProduct) return;
    
    const newQuantity = selectedQuantity + change;
    
    // Ensure quantity is between 1 and available stock
    if (newQuantity >= 1 && newQuantity <= selectedProduct.quantity) {
        selectedQuantity = newQuantity;
        document.getElementById('selectedQuantity').textContent = selectedQuantity;
        
        // Update the order summary
        updateOrderSummary();
    }
    
    // Disable decrease button if quantity is 1
    document.getElementById('decreaseQuantity').disabled = (selectedQuantity <= 1);
    
    // Disable increase button if quantity is max
    document.getElementById('increaseQuantity').disabled = (selectedQuantity >= selectedProduct.quantity);
}

// Update Order Summary
function updateOrderSummary() {
    if (selectedProduct) {
        selectedProductNameElement.textContent = selectedProduct.name;
        summaryQuantityElement.textContent = selectedQuantity;
        productPriceElement.textContent = formatPrice(selectedProduct.price) + ' VND';
        
        const total = selectedProduct.price * selectedQuantity;
        totalPriceElement.textContent = formatPrice(total) + ' VND';
    }
}

// Handle user form submission
function handleUserSubmit(event) {
    if (event) event.preventDefault();
    
    const formData = {
        username: usernameField.value,
        email: emailField.value,
        address: addressField.value,
        phone: phoneField.value
    };
    
    // Update user data
    userData = {
        username: formData.username,
        email: formData.email,
        address: formData.address,
        phone: formData.phone
    };
    
    // Store in local storage for future use
    localStorage.setItem('dathang_user', JSON.stringify(userData));
    
    hideError(userErrorMessage);
    showSuccess('Thông tin người dùng đã được xác nhận');
    
    return true;
}

// Handle order submission
async function handleOrderSubmit() {
    try {
        console.log("=== Starting Order Submission ===");
        
        // Check if product is selected
        if (!selectedProduct) {
            showError(userErrorMessage, 'Vui lòng chọn sản phẩm trước khi đặt hàng!');
            return;
        }
        
        // Check if account is selected
        const selectedOption = accountSelect.value;
        if (!selectedOption) {
            showError(userErrorMessage, 'Vui lòng chọn thông tin tài khoản!');
            return;
        }
        
        // If new account info is selected, validate and save form
        if (selectedOption === 'new') {
            if (!handleUserSubmit()) {
                return;
            }
        } else if (selectedOption === 'existing') {
            // Use existing user data
            if (!userData) {
                showError(userErrorMessage, 'Không có thông tin tài khoản. Vui lòng đăng nhập hoặc nhập thông tin mới!');
                return;
            }
        }
        
        // Disable submit button and show loading
        submitOrderButton.disabled = true;
        submitOrderButton.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang xử lý...';
        
        // Tính tổng tiền đơn hàng
        const totalAmount = selectedProduct.price * selectedQuantity;
        console.log('user dataaa', userData);
        console.log('Selected product:', selectedProduct);
        console.log('Quantity:', selectedQuantity);
        console.log('Total amount:', totalAmount);
        
        // Match the order data structure with the database schema
        const orderData = {
            userId: userData.id ? Number(userData.id) : 0,
            productId: Number(selectedProduct.id),
            quantity: Number(selectedQuantity),
            status: "PENDING"
        };
        
        console.log('Order data being sent:', JSON.stringify(orderData));
        
        try {
            // First, try debug endpoint to verify JSON structure
            console.log('Testing debug endpoint to verify JSON structure...');
            const debugResponse = await window.fetchWithCORS('http://localhost:8081/orders/debug', {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                mode: 'cors',
                body: JSON.stringify(orderData)
            });
            
            const debugResult = await debugResponse.json();
            console.log('Debug response:', debugResult);
            
            // Main order API call
            console.log('Making API call to create order...');
            const response = await window.fetchWithCORS('http://localhost:8081/orders', {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                mode: 'cors',
                body: JSON.stringify(orderData)
            });
            
            console.log('API response status:', response.status);
            console.log('API response statusText:', response.statusText);
            
            let responseData;
            try {
                responseData = await response.json();
                console.log('API response data:', responseData);
            } catch (jsonError) {
                console.error('Error parsing response JSON:', jsonError);
                responseData = { message: 'Could not parse response' };
            }
            
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
                    <p>Mã đơn hàng: #${responseData.id || 'N/A'}</p>
                    <p>Sản phẩm: ${selectedProduct.name}</p>
                    <p>Số lượng: ${selectedQuantity}</p>
                    <p>Tổng tiền: ${formatPrice(totalAmount)} VND</p>
                    <p>Chúng tôi sẽ liên hệ với bạn qua email để xác nhận đơn hàng.</p>
                    <div class="mt-3">
                        <a href="index.html" class="btn btn-primary">Quay lại trang chủ</a>
                    </div>
                </div>
            `;
            
            showSuccess(successMsg);
            hideError(userErrorMessage);
            
            // Disable all controls
            accountSelect.disabled = true;
            submitOrderButton.disabled = true;
            if (userForm) {
                const formInputs = userForm.querySelectorAll('input');
                formInputs.forEach(input => input.disabled = true);
            }
        } catch (apiError) {
            console.error('API call error:', apiError);
            throw apiError;
        }
    } catch (error) {
        console.error('Order submission error:', error);
        showError(userErrorMessage, 'Lỗi đặt hàng: ' + error.message);
    } finally {
        // Khôi phục nút đặt hàng
        submitOrderButton.disabled = false;
        submitOrderButton.innerHTML = '<i class="fas fa-check me-2"></i>Xác Nhận Đặt Hàng';
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
}
