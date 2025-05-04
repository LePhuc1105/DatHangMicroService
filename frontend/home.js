// Variables
let productList = [];

// DOM elements
const productListElement = document.getElementById('productList');
const searchInput = document.getElementById('searchProduct');
const retryFetchButton = document.getElementById('retry-fetch');
const productErrorMessage = document.getElementById('productErrorMessage');

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Check user authentication
    checkUserAuth();
    
    // Load products
    fetchProducts();
    
    // Setup event listeners
    setupEventListeners();
});

// Load user data
function checkUserAuth() {
    const userData = localStorage.getItem('dathang_user');
    const userDisplayName = document.getElementById('userDisplayName');
    
    if (userData) {
        try {
            const user = JSON.parse(userData);
            // Use fullName for display, with fallback to username
            userDisplayName.textContent = user.fullName || user.username || 'Tài khoản';
        } catch (e) {
            console.error('Error parsing user data:', e);
        }
    }
}

// Setup event listeners
function setupEventListeners() {
    // Retry fetching products
    if (retryFetchButton) {
        retryFetchButton.addEventListener('click', fetchProducts);
    }
    
    // Search products
    if (searchInput) {
        searchInput.addEventListener('input', handleSearch);
    }
    
    // Logout button - Already defined in auth.js
}

// Handle search
function handleSearch() {
    const searchTerm = searchInput.value.trim().toLowerCase();
    
    if (productList.length === 0) return;
    
    if (searchTerm === '') {
        // Show all products
        displayProducts(productList);
    } else {
        // Filter products by name
        const filteredProducts = productList.filter(product => 
            product.name.toLowerCase().includes(searchTerm)
        );
        
        displayProducts(filteredProducts);
    }
}

// Fetch products
async function fetchProducts() {
    showLoading();
    
    try {
        const response = await window.fetchWithAuth('http://localhost:8082/api/products/', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            },
            mode: 'cors'
        });
        
        if (!response.ok) {
            console.error('Error fetching products:', response.status);
            throw new Error(`Không thể tải sản phẩm: ${response.status}`);
        }
        
        const data = await response.json();
        productList = data;
        
        // Display products
        displayProducts(productList);
        console.log('Products loaded:', productList.length);
        
        // Hide loading, error messages
        hideLoading();
        hideError();
        
    } catch (error) {
        console.error('Error fetching products:', error);
        
        // Show placeholder products for demo/development
        productList = getPlaceholderProducts();
        displayProducts(productList);
        
        // Show error message
        showError(`Không thể tải sản phẩm: ${error.message}. Hiển thị dữ liệu mẫu.`);
        
        // Hide loading
        hideLoading();
    }
}

// Show loading
function showLoading() {
    productListElement.innerHTML = `
        <div class="text-center p-4 col-12">
            <div class="spinner-border text-primary" role="status"></div>
            <p class="mt-2">Đang tải sản phẩm...</p>
        </div>
    `;
}

// Hide loading
function hideLoading() {
    // Will be cleared when products are displayed
}

// Show error
function showError(message) {
    productErrorMessage.innerHTML = `<i class="fas fa-exclamation-circle me-2"></i>${message}`;
    productErrorMessage.classList.remove('d-none');
    setTimeout(() => {
        hideError();
    }, 5000);
}

// Hide error
function hideError() {
    productErrorMessage.classList.add('d-none');
}

// Get placeholder products for demo
function getPlaceholderProducts() {
    return [
        { id: 1, name: "Laptop Dell XPS 13", price: 25000000, quantity: 10, description: "Laptop cao cấp với màn hình 13 inch, Core i7" },
        { id: 2, name: "Samsung Galaxy S21", price: 15000000, quantity: 15, description: "Điện thoại Samsung mới nhất với camera 108MP" },
        { id: 3, name: "Tai nghe Sony WH-1000XM4", price: 8500000, quantity: 8, description: "Tai nghe chống ồn cao cấp" },
        { id: 4, name: "Màn hình LG UltraGear", price: 12000000, quantity: 5, description: "Màn hình gaming 27 inch, 144Hz" },
        { id: 5, name: "Apple iPad Pro 2021", price: 20000000, quantity: 7, description: "iPad Pro với chip M1 mạnh mẽ" },
        { id: 6, name: "Bàn phím cơ Logitech G Pro", price: 3000000, quantity: 20, description: "Bàn phím cơ chuyên game" }
    ];
}

// Display products
function displayProducts(products) {
    if (!products || products.length === 0) {
        productListElement.innerHTML = `
            <div class="col-12 text-center p-5">
                <i class="fas fa-box-open fa-3x mb-3 text-muted"></i>
                <p class="text-muted">Không có sản phẩm nào được tìm thấy</p>
            </div>
        `;
        return;
    }
    
    productListElement.innerHTML = products.map(product => `
        <div class="col">
            <div class="card product-card h-100">
                <div class="card-body">
                    <h5 class="card-title">${product.name}</h5>
                    <p class="card-text text-muted small">${product.description || 'Không có mô tả'}</p>
                    <div class="d-flex justify-content-between align-items-center mt-3">
                        <span class="price-tag">${formatPrice(product.price)} VND</span>
                        <span class="badge bg-${product.quantity > 0 ? 'success' : 'danger'}">
                            ${product.quantity > 0 ? 'Còn hàng' : 'Hết hàng'}
                        </span>
                    </div>
                </div>
                <div class="card-footer bg-white border-top-0">
                    <a href="order.html?id=${product.id}" class="btn btn-primary w-100">
                        <i class="fas fa-shopping-cart me-2"></i>Đặt hàng
                    </a>
                </div>
            </div>
        </div>
    `).join('');
}

// Format price
function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN').format(price);
}
