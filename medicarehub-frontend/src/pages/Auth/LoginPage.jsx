// src/pages/Auth/LoginPage.jsx
import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext'; // Import useAuth

const LoginPage = () => { // Removed onLoginSuccess prop
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const { login } = useAuth(); // Get login function from context
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || "/"; // Get redirect path from location state or default to home

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);
    try {
      await login(usernameOrEmail, password); // Use context login
      // Navigation is now handled by App.jsx's useEffect after auth state update
      navigate(from, { replace: true }); // Navigate to where the user was trying to go or home
    } catch (err) {
      let errorMessage = 'Login failed. Please check your credentials.';
      if (err.response) {
        if (typeof err.response.data === 'string') errorMessage = err.response.data;
        else if (err.response.data && err.response.data.message) errorMessage = err.response.data.message;
        else if (err.response.data && err.response.data.error) errorMessage = err.response.data.error;
      } else if (err.request) errorMessage = 'Network error. Please check your connection.';
      else errorMessage = err.message || errorMessage;
      setError(errorMessage);
      console.error('Login error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="form-container">
      <h2 className="form-title">Login</h2>
      {error && <p className="error-message">{error}</p>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="usernameOrEmail">Username or Email</label>
          <input
            type="text"
            id="usernameOrEmail"
            className="form-input"
            value={usernameOrEmail}
            onChange={(e) => setUsernameOrEmail(e.target.value)}
            required
            disabled={isLoading}
            autoComplete="username"
          />
        </div>
        <div className="form-group">
          <label htmlFor="password">Password</label>
          <input
            type="password"
            id="password"
            className="form-input"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={isLoading}
            autoComplete="current-password"
          />
        </div>
        <button type="submit" className="btn btn-primary" disabled={isLoading}>
          {isLoading ? 'Logging in...' : 'Login'}
        </button>
      </form>
      <div className="form-link-container">
        <p>Don't have an account? <Link to="/signup" className="form-link">Sign up</Link></p>
      </div>
    </div>
  );
};

export default LoginPage;