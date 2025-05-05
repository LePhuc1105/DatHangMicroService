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

// API Endpoints using config.js
const API_BASE = {
  products: window.API_CONFIG.getUrl('products'),
  cart: window.API_CONFIG.getUrl('cart'),
  orders: window.API_CONFIG.getUrl('orders'),
  users: window.API_CONFIG.getUrl('users')
};

// Function to fetch and display products
function fetchProducts() {
  fetch(`${API_BASE.products}/`, {
    ...API_CONFIG.fetchOptions,
    method: 'GET'
  })
    .then(response => {
      if (!response.ok) throw new Error('Không thể tải sản phẩm.');
      return response.json();
    })
    .then(data => {
      products = data;
      displayProducts();
    })
    .catch(error => {
      console.error('Error fetching products:', error);
      productErrorMessage.textContent = 'Không thể tải sản phẩm.';
    });
}

// Display products in dropdown
function displayProducts() {
  productList.innerHTML = '';
  products.forEach(product => {
    const productDiv = document.createElement('div');
    productDiv.innerHTML = `
      <span>${product.name} - Giá: ${product.price} VND</span>
      <select onchange="selectProduct(${product.id}, ${product.quantity})">
        <option value="">Chọn số lượng</option>
        ${Array.from({ length: product.quantity }).map((_, index) => `<option value="${index + 1}">${index + 1}</option>`).join('')}
      </select>
    `;
    productList.appendChild(productDiv);
  });
}

// Handle product selection
function selectProduct(productId, maxQuantity) {
  const quantity = parseInt(event.target.value);
  if (!quantity) {
    selectedProduct = null;
    selectedQuantity = 0;
    orderSummary.innerHTML = '';
    submitOrderButton.disabled = true;
    return;
  }

  // Find selected product from products array
  const product = products.find(p => p.id === productId);
  selectedProduct = product;  // Gán sản phẩm đã chọn
  selectedQuantity = quantity;

  // Hiển thị thông tin sản phẩm và số lượng đã chọn
  orderSummary.innerHTML = `
    <strong>Sản phẩm:</strong> ${selectedProduct.name} <br>
    <strong>Số lượng:</strong> ${selectedQuantity} <br>
    <strong>Tổng:</strong> ${selectedProduct.price * selectedQuantity} VND
  `;

  // Chỉ kích hoạt nút đặt hàng khi người dùng hợp lệ
  submitOrderButton.disabled = !isUserValid;
}

// Handle user form submission (check user)
userForm.addEventListener('submit', (e) => {
  e.preventDefault();
  const username = usernameField.value;
  const email = emailField.value;
  const address = addressField.value;
  const phone = phoneField.value;

  fetch(`${API_BASE.users}/${username}`, {
    ...API_CONFIG.fetchOptions,
    method: 'GET'
  })
    .then(response => {
      if (response.status === 404) {
        throw new Error('Tài khoản không tồn tại. Vui lòng sử dụng tài khoản hợp lệ.');
      }
      if (!response.ok) {
        throw new Error('Không thể kiểm tra tài khoản.');
      }
      return response.json();
    })
    .then(user => {
      if (!user) {
        throw new Error('Tài khoản không tồn tại. Vui lòng sử dụng tài khoản hợp lệ.');
      }
      userErrorMessage.textContent = '';
      isUserValid = true; // Mark user as valid
      productList.innerHTML = ''; // Clear previous products
      orderSummary.innerHTML = ''; // Clear order summary
      submitOrderButton.disabled = true; // Disable order button until product is selected
      fetchProducts(); // Fetch products only after validating the user
    })
    .catch(error => {
      console.error('Error checking user:', error);
      userErrorMessage.textContent = error.message;
      isUserValid = false; // Mark user as invalid
      productList.innerHTML = ''; // Clear product list
      orderSummary.innerHTML = ''; // Clear order summary
      submitOrderButton.disabled = true; // Disable order button
    });
});

// Handle order submission
submitOrderButton.addEventListener('click', () => {
  if (!isUserValid) {
    userErrorMessage.textContent = 'Vui lòng sử dụng tài khoản hợp lệ trước khi đặt hàng.';
    return;
  }

  if (!selectedProduct || selectedQuantity <= 0) {
    productErrorMessage.textContent = 'Sản phẩm hoặc số lượng không hợp lệ.';
    return;
  }

  const order = {
    productId: selectedProduct.id, // Gán đúng ID sản phẩm
    quantity: selectedQuantity,
    customerName: usernameField.value,
    customerAddress: addressField.value,
  };

  console.log('Order:', order);

  fetch(API_BASE.orders, {
    ...API_CONFIG.fetchOptions,
    method: 'POST',
    body: JSON.stringify(order),
  })
    .then(response => {
      if (!response.ok) {
        throw new Error('Không thể đặt hàng. Vui lòng thử lại.');
      }
      return response.json();
    })
    .then(orderData => {
      successMessage.classList.remove('hidden');
      setTimeout(() => successMessage.classList.add('hidden'), 5000);
      // Reset form after successful order
      userForm.reset();
      productList.innerHTML = '';
      orderSummary.innerHTML = '';
      submitOrderButton.disabled = true;
      isUserValid = false;
      selectedProduct = null;
      selectedQuantity = 0;
    })
    .catch(error => {
      console.error('Error placing order:', error);
      productErrorMessage.textContent = error.message;
    });
});
