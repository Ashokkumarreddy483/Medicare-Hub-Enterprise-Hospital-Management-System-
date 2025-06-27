// src/contexts/AuthContext.jsx
import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import authService from '../services/authService.js'; // Make sure .js if it's plain JS
import { useNavigate } from 'react-router-dom';

const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

const parseJwt = (token) => { /* ... (implementation from before) ... */
  if (!token) return null;
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (e) { console.error("Invalid token:", e); return null; }
};

export const AuthProvider = ({ children }) => {
  const [currentUser, setCurrentUser] = useState(null);
  const [authToken, setAuthToken] = useState(localStorage.getItem('authToken'));
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('authToken');
    const userDataString = localStorage.getItem('userData');
    if (token && userDataString) {
      try {
        const parsedUserData = JSON.parse(userDataString);
        const jwtPayload = parseJwt(token);
        if (jwtPayload && jwtPayload.exp * 1000 > Date.now()) {
          setCurrentUser(parsedUserData);
          setAuthToken(token);
        } else {
          localStorage.removeItem('authToken');
          localStorage.removeItem('userData');
        }
      } catch (error) {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
      }
    }
    setIsLoading(false);
  }, []);

  const login = useCallback(async (usernameOrEmail, password) => {
    const response = await authService.login(usernameOrEmail, password);
    localStorage.setItem('authToken', response.token);
    const userData = { id: response.id, username: response.username, email: response.email, roles: response.roles, firstName: response.firstName, lastName: response.lastName }; // Assuming firstName/lastName in JWT/login response
    localStorage.setItem('userData', JSON.stringify(userData));
    setAuthToken(response.token);
    setCurrentUser(userData);
    return response;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userData');
    setAuthToken(null);
    setCurrentUser(null);
    navigate('/login');
  }, [navigate]);

  const signup = useCallback(async (signupData) => {
    return await authService.signup(signupData);
  }, []);

  const value = { currentUser, authToken, isAuthenticated: !!currentUser && !!authToken, isLoading, login, logout, signup };

  return (
    <AuthContext.Provider value={value}>
      {!isLoading && children}
    </AuthContext.Provider>
  );
};