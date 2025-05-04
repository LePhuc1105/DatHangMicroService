// DOM Elements
const profileUsernameElement = document.getElementById('profileUsername');
const profileEmailElement = document.getElementById('profileEmail');
const userDisplayNameElement = document.getElementById('userDisplayName');

// Profile view elements
const viewNameElement = document.getElementById('viewName');
const viewEmailElement = document.getElementById('viewEmail');
const viewPhoneElement = document.getElementById('viewPhone');
const viewAddressElement = document.getElementById('viewAddress');

// Profile edit elements
const editNameElement = document.getElementById('editName');
const editEmailElement = document.getElementById('editEmail');
const editPhoneElement = document.getElementById('editPhone');
const editAddressElement = document.getElementById('editAddress');

// Action buttons
const editProfileButton = document.getElementById('editProfileBtn');
const cancelEditButton = document.getElementById('cancelEditBtn');
const profileEditForm = document.getElementById('profileEditForm');
const profileInfoView = document.getElementById('profileInfo');

// Message elements
const successMessageElement = document.getElementById('successMessage');
const errorMessageElement = document.getElementById('errorMessage');

// User data from local storage
let userData = null;

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Check if user is logged in
    checkAuth();
    
    // Load profile data
    fetchUserInfo();
    
    // Setup event listeners
    setupEventListeners();
});

// Check authentication
function checkAuth() {
    const savedUserData = localStorage.getItem('dathang_user');
    
    if (!savedUserData) {
        // Redirect to login page if not logged in
        window.location.href = 'login.html?redirect=profile.html';
        return;
    }
    
    try {
        userData = JSON.parse(savedUserData);
    } catch (e) {
        console.error('Error parsing user data:', e);
        showError('Có lỗi xảy ra khi tải thông tin người dùng.');
    }
}

// Fetch user info from API
async function fetchUserInfo() {
    try {
        // Show loading state
        showLoadingState();
        
        // Get username from userData
        if (!userData || !userData.username) {
            throw new Error('Username not found in stored user data');
        }
        
        const username = userData.username;
        console.log(`Fetching user info for: ${username}`);
        console.log("Initial userData:", userData);
        
        // Try multiple endpoints in sequence
        let userInfo = null;
        let success = false;
        
        // First try the getInfo endpoint
        try {
            console.log(`Trying GET /api/users/getInfo/${username}`);
            const response = await window.fetchWithCORS(`http://localhost:8083/api/users/getInfo/${username}`, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            });
            
            if (response.ok) {
                userInfo = await response.json();
                success = true;
                console.log("Successfully got user info from getInfo endpoint:", userInfo);
            } else {
                console.warn(`getInfo endpoint failed with status: ${response.status}`);
            }
        } catch (error) {
            console.warn("Error using getInfo endpoint:", error);
        }
        
        // If that fails, try the regular getUserByUsername endpoint
        if (!success) {
            try {
                console.log(`Trying GET /api/users/${username}`);
                const response = await window.fetchWithCORS(`http://localhost:8083/api/users/${username}`, {
                    method: 'GET',
                    headers: {
                        'Accept': 'application/json'
                    }
                });
                
                if (response.ok) {
                    const user = await response.json();
                    console.log("Got user from direct endpoint:", user);
                    
                    // Convert User entity to UserResponse format
                    userInfo = {
                        id: user.id,
                        username: user.username,
                        email: user.email,
                        fullName: user.fullName || '',
                        address: user.address,
                        phone: user.phone
                    };
                    success = true;
                } else {
                    console.warn(`Direct user endpoint failed with status: ${response.status}`);
                }
            } catch (error) {
                console.warn("Error using direct user endpoint:", error);
            }
        }
        
        // If all endpoints fail, throw error
        if (!success) {
            throw new Error('All API endpoints failed to get user info');
        }
        
        // Update userData with API data
        userData = {
            ...userData,
            username: userInfo.username, // Keep original username for identification
            fullName: userInfo.fullName || '', // Store fullName separately
            email: userInfo.email || '',
            phone: userInfo.phone || '',
            address: userInfo.address || ''
        };
        
        console.log("Updated userData after API call:", userData);
        
        // Update localStorage with new data
        localStorage.setItem('dathang_user', JSON.stringify(userData));
        
        // Load updated profile data
        loadProfileData();
    } catch (error) {
        console.error('Error fetching user info:', error);
        showError('Không thể tải thông tin từ máy chủ. Đang sử dụng dữ liệu đã lưu.');
        
        // Fallback to localStorage data
        loadProfileData();
    }
}

// Show loading state
function showLoadingState() {
    viewNameElement.textContent = 'Đang tải...';
    viewEmailElement.textContent = 'Đang tải...';
    viewPhoneElement.textContent = 'Đang tải...';
    viewAddressElement.textContent = 'Đang tải...';
}

// Load profile data
function loadProfileData() {
    if (!userData) return;
    
    console.log("Loading profile data with userData:", userData);
    
    // Always use fullName for all displays, with fallback to username only if necessary
    const displayName = userData.fullName || userData.username || 'Tài khoản';
    
    // Update header display name - always use fullName
    userDisplayNameElement.textContent = displayName;
    
    // Update profile header - always use fullName
    profileUsernameElement.textContent = displayName;
    profileEmailElement.textContent = userData.email || 'Chưa cập nhật';
    
    // Update profile view - always use fullName
    viewNameElement.textContent = userData.fullName || 'Chưa cập nhật';
    viewEmailElement.textContent = userData.email || 'Chưa cập nhật';
    viewPhoneElement.textContent = userData.phone || 'Chưa cập nhật';
    viewAddressElement.textContent = userData.address || 'Chưa cập nhật';
    
    // Populate edit form - always use fullName
    editNameElement.value = userData.fullName || '';
    editEmailElement.value = userData.email || '';
    editPhoneElement.value = userData.phone || '';
    editAddressElement.value = userData.address || '';
    
    console.log("Profile data loaded. Form values:", {
        fullName: editNameElement.value,
        email: editEmailElement.value,
        phone: editPhoneElement.value,
        address: editAddressElement.value
    });
}

// Setup event listeners
function setupEventListeners() {
    // Edit profile button
    if (editProfileButton) {
        editProfileButton.addEventListener('click', showEditForm);
    }
    
    // Cancel edit button
    if (cancelEditButton) {
        cancelEditButton.addEventListener('click', hideEditForm);
    }
    
    // Profile edit form submission
    if (profileEditForm) {
        profileEditForm.addEventListener('submit', handleProfileUpdate);
    }
}

// Show edit form
function showEditForm() {
    profileInfoView.classList.add('d-none');
    profileEditForm.classList.remove('d-none');
    editProfileButton.classList.add('d-none');
}

// Hide edit form
function hideEditForm() {
    profileInfoView.classList.remove('d-none');
    profileEditForm.classList.add('d-none');
    editProfileButton.classList.remove('d-none');
}

// Handle profile update
function handleProfileUpdate(e) {
    e.preventDefault();
    
    // Get form data
    const updatedUserData = {
        ...userData,
        fullName: editNameElement.value.trim(),
        email: editEmailElement.value.trim(),
        phone: editPhoneElement.value.trim(),
        address: editAddressElement.value.trim()
        // Note: username is preserved from the existing userData
    };
    
    console.log("Profile update - original data:", userData);
    console.log("Profile update - new data:", updatedUserData);
    
    // Validate data - only email is required
    if (!updatedUserData.email) {
        showError('Email không được để trống');
        return;
    }
    
    // Update local storage
    localStorage.setItem('dathang_user', JSON.stringify(updatedUserData));
    
    // Update user data variable
    userData = updatedUserData;
    
    // Reload profile data
    loadProfileData();
    
    // Hide edit form
    hideEditForm();
    
    // Show success message
    showSuccess('Thông tin tài khoản đã được cập nhật thành công!');
    
    // Make an API call to update user information on server
    updateUserInfo(updatedUserData);
}

// Update user info on server
async function updateUserInfo(userData) {
    try {
        // Check if username exists
        if (!userData || !userData.username) {
            throw new Error('Username not found in user data');
        }
        
        console.log('Updating user info on server:', {
            username: userData.username, // For identification only
            fullName: userData.fullName,
            email: userData.email,
            phone: userData.phone,
            address: userData.address
        });
        
        // First try PUT method
        try {
            console.log('Trying PUT /api/users/updateInfo');
            const response = await window.fetchWithCORS('http://localhost:8083/api/users/updateInfo', {
                method: 'PUT',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username: userData.username, // Required for identification
                    fullName: userData.fullName || '',
                    email: userData.email || '',
                    phone: userData.phone || '',
                    address: userData.address || ''
                })
            });
            
            if (response.ok) {
                const data = await response.json();
                console.log('User updated successfully (PUT):', data);
                return;
            } else {
                console.warn(`PUT update failed with status: ${response.status}`);
            }
        } catch (error) {
            console.warn('Error with PUT update:', error);
        }
        
        // If PUT fails, try POST method
        try {
            console.log('Trying POST /api/users/updateInfo');
            const response = await window.fetchWithCORS('http://localhost:8083/api/users/updateInfo', {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username: userData.username, // Required for identification
                    fullName: userData.fullName || '',
                    email: userData.email || '',
                    phone: userData.phone || '',
                    address: userData.address || ''
                })
            });
            
            if (response.ok) {
                const data = await response.json();
                console.log('User updated successfully (POST):', data);
                return;
            } else {
                console.warn(`POST update failed with status: ${response.status}`);
                throw new Error(`Error updating user: ${response.status}`);
            }
        } catch (error) {
            console.error('Error updating user info:', error);
            throw error;
        }
    } catch (error) {
        console.error('Error in updateUserInfo:', error);
        showError('Không thể cập nhật thông tin người dùng trên máy chủ.');
    }
}

// Show success message
function showSuccess(message) {
    successMessageElement.innerHTML = `<i class="fas fa-check-circle me-2"></i>${message}`;
    successMessageElement.classList.remove('d-none');
    
    setTimeout(() => {
        successMessageElement.classList.add('d-none');
    }, 5000);
}

// Show error message
function showError(message) {
    errorMessageElement.innerHTML = `<i class="fas fa-exclamation-circle me-2"></i>${message}`;
    errorMessageElement.classList.remove('d-none');
    
    setTimeout(() => {
        errorMessageElement.classList.add('d-none');
    }, 5000);
}

// Log out function (implemented in auth.js, but we'll define a backup here)
if (typeof logout !== 'function') {
    window.logout = function() {
        localStorage.removeItem('dathang_user');
        window.location.href = 'login.html';
    };
} 