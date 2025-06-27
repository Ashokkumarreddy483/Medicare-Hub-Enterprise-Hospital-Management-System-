// src/services/authService.js
import apiClient from '../config/axiosConfig'; // Uses the configured Axios instance

const login = async (usernameOrEmail, password_param) => {
  try {
    const response = await apiClient.post('/auth/signin', {
      usernameOrEmail: usernameOrEmail,
      password: password_param
    });
    return response.data; // Assuming backend returns data like { token, userDetails }
  } catch (error) {
    console.error("Login service error:", error.response || error.message || error);
    throw error; // Re-throw to be caught by the component
  }
};

const signup = async (userData) => {
  try {
    const response = await apiClient.post('/auth/signup', userData);
    return response.data; // Assuming backend returns a success message
  } catch (error) {
    console.error("Signup service error:", error.response || error.message || error);
    throw error; // Re-throw
  }
};

const authService = {
  login,
  signup,
};

export default authService;