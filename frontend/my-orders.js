// DOM Elements
const profileUsernameElement = document.getElementById('profileUsername');
const profileEmailElement = document.getElementById('profileEmail');
const userDisplayNameElement = document.getElementById('userDisplayName');
const ordersListElement = document.getElementById('ordersList');
const noOrdersMessageElement = document.getElementById('noOrdersMessage');
const refreshOrdersButton = document.getElementById('refreshOrdersBtn');
const searchOrdersInput = document.getElementById('searchOrders');
const filterStatusLinks = document.querySelectorAll('.filter-status');
const orderDetailModalElement = document.getElementById('orderDetailModal');
const orderDetailContentElement = document.getElementById('orderDetailContent');

// Bootstrap modal instance
let orderDetailModal;

// User data from local storage
let userData = null;

// Orders data
let allOrders = [];
let filteredOrders = [];
let currentFilter = 'all';

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Initialize the modal
    orderDetailModal = new bootstrap.Modal(orderDetailModalElement);
    
    // Check if user is logged in
    checkAuthentication();
    
    // Load user profile data
    loadUserProfile();
    
    // Fetch orders
    loadUserOrders();
    
    // Setup event listeners
    setupEventListeners();
});

// Check authentication
function checkAuthentication() {
    const savedUserData = localStorage.getItem('dathang_user');
    
    if (!savedUserData) {
        // Redirect to login page if not logged in
        window.location.href = 'login.html?redirect=my-orders.html';
        return;
    }
    
    try {
        userData = JSON.parse(savedUserData);
    } catch (e) {
        console.error('Error parsing user data:', e);
    }
}

// Load profile data
function loadUserProfile() {
    if (!userData) return;
    
    // Always use fullName for all displays, with fallback to username only if necessary
    const displayName = userData.fullName || userData.username || 'Tài khoản';
    
    // Update header display name
    userDisplayNameElement.textContent = displayName;
    
    // Update profile header
    profileUsernameElement.textContent = displayName;
    profileEmailElement.textContent = userData.email || 'Chưa cập nhật';
}

// Setup event listeners
function setupEventListeners() {
    // Refresh orders button
    if (refreshOrdersButton) {
        refreshOrdersButton.addEventListener('click', loadUserOrders);
    }
    
    // Search orders
    if (searchOrdersInput) {
        searchOrdersInput.addEventListener('input', handleSearch);
    }
    
    // Filter order status
    if (filterStatusLinks) {
        filterStatusLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                filterOrders(link.dataset.status);
            });
        });
    }
}

// Handle search
function handleSearch() {
    const searchTerm = searchOrdersInput.value.trim().toLowerCase();
    
    if (allOrders.length === 0) return;
    
    // Apply both search and filter
    applySearchAndFilter(searchTerm, currentFilter);
}

// Filter orders by status
function filterOrders(status) {
    if (!status) return;
    
    currentFilter = status;
    const searchTerm = searchOrdersInput.value.trim().toLowerCase();
    
    // Apply both search and filter
    applySearchAndFilter(searchTerm, status);
}

// Apply search and filter
function applySearchAndFilter(searchTerm, status) {
    // First filter by status
    if (status === 'all') {
        filteredOrders = [...allOrders];
    } else {
        filteredOrders = allOrders.filter(order => order.status === status);
    }
    
    // Then apply search if needed
    if (searchTerm) {
        filteredOrders = filteredOrders.filter(order => 
            order.id.toString().includes(searchTerm) ||
            (order.productId && order.productId.toString().includes(searchTerm))
        );
    }
    
    // Display filtered orders
    displayOrders(filteredOrders);
}

// Fetch orders
async function loadUserOrders() {
    showLoading();
    
    try {
        // Gọi API lấy đơn hàng
        const response = await fetch(`http://localhost:8083/orders/user/${userData.id}`, {
            headers: {
                'Authorization': `Bearer ${userData.token || ''}`
            }
        });

        console.log({response})
        
        if (!response.ok) {
            throw new Error('Không thể tải đơn hàng');
        }
        
        allOrders = await response.json();
        filteredOrders = [...allOrders];
        currentFilter = 'all';
        
        displayOrders(allOrders);
    } catch (error) {
        console.error('Error loading orders:', error);
        ordersListElement.innerHTML = `
            <div class="alert alert-danger">
                <i class="fas fa-exclamation-circle me-2"></i>
                Lỗi khi tải đơn hàng: ${error.message}
            </div>
        `;
    }
}

// Display orders
function displayOrders(orders) {
    if (!orders || orders.length === 0) {
        ordersListElement.classList.add('d-none');
        noOrdersMessageElement.classList.remove('d-none');
        return;
    }
    
    noOrdersMessageElement.classList.add('d-none');
    ordersListElement.classList.remove('d-none');
    
    ordersListElement.innerHTML = orders.map(order => `
        <div class="card mb-3 order-card">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <h6 class="card-subtitle">
                        <i class="fas fa-shopping-bag me-2"></i>
                        Đơn hàng #${order.id}
                    </h6>
                    <span class="badge ${getStatusBadgeClass(order.status)}">${getStatusText(order.status)}</span>
                </div>
                <div class="row mb-2">
                    <div class="col-md-6">
                        <p class="mb-1"><strong>Ngày đặt:</strong> ${formatDate(order.createdAt)}</p>
                        <p class="mb-1"><strong>Sản phẩm ID:</strong> ${order.productId}</p>
                        <p class="mb-0"><strong>Số lượng:</strong> ${order.quantity}</p>
                    </div>
                    <div class="col-md-6">
                        <p class="mb-1"><strong>Người dùng ID:</strong> ${order.userId}</p>
                        <p class="mb-0"><strong>Cập nhật:</strong> ${formatDate(order.updatedAt)}</p>
                    </div>
                </div>
                <button class="btn btn-sm btn-outline-primary mt-2 view-details-btn" data-order-id="${order.id}">
                    <i class="fas fa-info-circle me-1"></i>
                    Xem chi tiết
                </button>
            </div>
        </div>
    `).join('');
    
    // Add event listeners to view details buttons
    document.querySelectorAll('.view-details-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const orderId = btn.dataset.orderId;
            showOrderDetails(orderId);
        });
    });
}

// Show order details in modal
function showOrderDetails(orderId) {
    const order = allOrders.find(o => o.id.toString() === orderId.toString());
    
    if (!order) {
        orderDetailContentElement.innerHTML = `
            <div class="alert alert-danger">
                <i class="fas fa-exclamation-circle me-2"></i>
                Không tìm thấy thông tin đơn hàng
            </div>
        `;
        orderDetailModal.show();
        return;
    }
    
    orderDetailContentElement.innerHTML = `
        <div class="order-detail">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="mb-0">Đơn hàng #${order.id}</h5>
                <span class="badge ${getStatusBadgeClass(order.status)}">${getStatusText(order.status)}</span>
            </div>
            
            <div class="card mb-3">
                <div class="card-header">
                    <h6 class="mb-0">Thông tin đơn hàng</h6>
                </div>
                <div class="card-body">
                    <ul class="list-unstyled">
                        <li class="mb-2"><i class="fas fa-calendar me-2"></i> <strong>Ngày đặt:</strong> ${formatDate(order.createdAt)}</li>
                        <li class="mb-2"><i class="fas fa-clock me-2"></i> <strong>Cập nhật:</strong> ${formatDate(order.updatedAt)}</li>
                        <li class="mb-2"><i class="fas fa-tag me-2"></i> <strong>Trạng thái:</strong> ${getStatusText(order.status)}</li>
                        <li class="mb-2"><i class="fas fa-user me-2"></i> <strong>ID Người dùng:</strong> ${order.userId}</li>
                    </ul>
                </div>
            </div>
            
            <div class="card mb-3">
                <div class="card-header">
                    <h6 class="mb-0">Chi tiết sản phẩm</h6>
                </div>
                <div class="card-body">
                    <div class="d-flex align-items-center">
                        <div class="me-3">
                            <i class="fas fa-box fa-2x text-primary"></i>
                        </div>
                        <div class="flex-grow-1">
                            <h6 class="mb-1">ID Sản phẩm: ${order.productId}</h6>
                            <div>
                                <span class="badge bg-secondary">${order.quantity} sản phẩm</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    orderDetailModal.show();
}

// Show loading state
function showLoading() {
    ordersListElement.innerHTML = `
        <div class="text-center p-4">
            <div class="spinner-border text-primary" role="status"></div>
            <p class="mt-2">Đang tải đơn hàng...</p>
        </div>
    `;
    ordersListElement.classList.remove('d-none');
    noOrdersMessageElement.classList.add('d-none');
}

// Utility function to get order status text
function getStatusText(status) {
    const statusMap = {
        'PENDING': 'Đang chờ xử lý',
        'CONFIRMED': 'Đã xác nhận',
        'SHIPPED': 'Đang giao hàng',
        'DELIVERED': 'Đã giao hàng',
        'CANCELED': 'Đã hủy'
    };
    
    return statusMap[status] || status;
}

// Utility function to get badge class for status
function getStatusBadgeClass(status) {
    const classMap = {
        'PENDING': 'bg-warning',
        'CONFIRMED': 'bg-info',
        'SHIPPED': 'bg-primary',
        'DELIVERED': 'bg-success',
        'CANCELED': 'bg-danger'
    };
    
    return classMap[status] || 'bg-secondary';
}

// Utility function to format price
function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN').format(price);
}

// Utility function to format date
function formatDate(dateString) {
    if (!dateString) return 'Không xác định';
    
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

// Utility function to generate order timeline
function getOrderTimeline(order) {
    const statusOrder = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED'];
    let timeline = '';
    
    if (order.status === 'CANCELED') {
        timeline += `
            <div class="timeline-item">
                <div class="timeline-marker bg-danger"></div>
                <div class="timeline-content">
                    <h6 class="mb-0">Đơn hàng đã bị hủy</h6>
                    <p class="mb-0 text-muted small">${formatDate(order.updatedAt)}</p>
                </div>
            </div>
        `;
        return timeline;
    }
    
    const currentStatusIndex = statusOrder.indexOf(order.status);
    
    for (let i = 1; i < statusOrder.length; i++) {
        const status = statusOrder[i];
        const isPast = i <= currentStatusIndex;
        const isCurrent = i === currentStatusIndex;
        
        timeline += `
            <div class="timeline-item">
                <div class="timeline-marker ${isPast ? 'bg-success' : 'bg-light'}"></div>
                <div class="timeline-content">
                    <h6 class="mb-0">${getStatusText(status)} ${isCurrent ? '(Hiện tại)' : ''}</h6>
                    <p class="mb-0 text-muted small">${isPast ? formatDate(order.updatedAt) : '-'}</p>
                </div>
            </div>
        `;
    }
    
    return timeline;
}

// Add some CSS for timeline
document.addEventListener('DOMContentLoaded', () => {
    // Add timeline CSS if not already added
    if (!document.getElementById('timeline-css')) {
        const style = document.createElement('style');
        style.id = 'timeline-css';
        style.textContent = `
            .timeline {
                position: relative;
                padding-left: 30px;
            }
            
            .timeline::before {
                content: '';
                position: absolute;
                top: 0;
                bottom: 0;
                left: 8px;
                width: 2px;
                background: #e9ecef;
            }
            
            .timeline-item {
                position: relative;
                margin-bottom: 20px;
            }
            
            .timeline-marker {
                position: absolute;
                left: -30px;
                width: 16px;
                height: 16px;
                border-radius: 50%;
                border: 2px solid #fff;
            }
            
            .timeline-content {
                padding-left: 10px;
            }
            
            .order-card:hover {
                box-shadow: 0 5px 15px rgba(0, 123, 255, 0.1);
            }
        `;
        document.head.appendChild(style);
    }
}); 