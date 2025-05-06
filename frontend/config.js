/**
 * Configuration for frontend to use different API endpoints in different environments
 */

// Check if we're running in Docker container
const isDocker = window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1';

// Base API URL
const API_HOST = isDocker ? window.location.hostname : 'localhost';

// API Endpoints
const API_CONFIG = {
  host: API_HOST,
  port: 8080,
  get baseUrl() {
    return `http://${this.host}:${this.port}`;
  },
  endpoints: {
    products: '/api/products',
    cart: '/api/cart',
    orders: '/api/orders',
    users: '/api/users'
  },
  getUrl(endpoint) {
    return this.baseUrl + this.endpoints[endpoint];
  },
  // Standard fetch options with CORS settings
  fetchOptions: {
    mode: 'cors',
    credentials: 'omit',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  }
};

// Export for use in other scripts
window.API_CONFIG = API_CONFIG; 