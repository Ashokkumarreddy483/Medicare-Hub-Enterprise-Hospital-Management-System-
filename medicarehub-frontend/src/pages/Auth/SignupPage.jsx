// src/pages/Auth/SignupPage.jsx
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext'; // Import useAuth

const SignupPage = () => {
  const [formData, setFormData] = useState({ /* ... as before ... */
    username: '', email: '', password: '', firstName: '', lastName: '', phoneNumber: '',
  });
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const { signup } = useAuth(); // Get signup function from context
  const navigate = useNavigate();

  const handleChange = (e) => { /* ... as before ... */
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => { /* ... as before, but use context signup ... */
    e.preventDefault();
    setError(null);
    setSuccessMessage(null);
    setIsLoading(true);

    if (formData.password.length < 6) {
      setError("Password must be at least 6 characters long.");
      setIsLoading(false);
      return;
    }

    try {
      const signupData = { ...formData, roles: ['ROLE_PATIENT'] };
      const response = await signup(signupData); // Use context signup
      setSuccessMessage(response.message + ' Redirecting to login...');
      setTimeout(() => {
        navigate('/login');
      }, 2500);
    } catch (err) {
      let errorMessage = 'Signup failed. Please try again.';
      if (err.response?.data) {
        if (typeof err.response.data === 'string') errorMessage = err.response.data;
        else if (err.response.data.message) errorMessage = err.response.data.message;
        else if (typeof err.response.data === 'object') {
          const fieldErrors = Object.entries(err.response.data).map(([key, value]) => `${key}: ${value}`).join('; ');
          errorMessage = fieldErrors || errorMessage;
        }
      } else if (err.request) errorMessage = 'Network error.';
      else errorMessage = err.message || errorMessage;
      setError(errorMessage);
      console.error('Signup error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    // ... JSX structure as before ...
    <div className="form-container">
      <h2 className="form-title">Create Account</h2>
      {error && <p className="error-message">{error}</p>}
      {successMessage && <p className="success-message">{successMessage}</p>}
      <form onSubmit={handleSubmit}>
        {/* Form groups as before */}
        <div className="form-group">
          <label htmlFor="firstName">First Name</label>
          <input type="text" id="firstName" name="firstName" className="form-input" value={formData.firstName} onChange={handleChange} required disabled={isLoading} />
        </div>
        <div className="form-group">
          <label htmlFor="lastName">Last Name</label>
          <input type="text" id="lastName" name="lastName" className="form-input" value={formData.lastName} onChange={handleChange} required disabled={isLoading} />
        </div>
        <div className="form-group">
          <label htmlFor="username">Username</label>
          <input type="text" id="username" name="username" className="form-input" value={formData.username} onChange={handleChange} required disabled={isLoading} />
        </div>
        <div className="form-group">
          <label htmlFor="email">Email</label>
          <input type="email" id="email" name="email" className="form-input" value={formData.email} onChange={handleChange} required disabled={isLoading} />
        </div>
        <div className="form-group">
          <label htmlFor="phoneNumber">Phone Number (Optional)</label>
          <input type="tel" id="phoneNumber" name="phoneNumber" className="form-input" value={formData.phoneNumber} onChange={handleChange} disabled={isLoading} />
        </div>
        <div className="form-group">
          <label htmlFor="password">Password</label>
          <input type="password" id="password" name="password" className="form-input" value={formData.password} onChange={handleChange} required minLength={6} disabled={isLoading} />
        </div>
        <button type="submit" className="btn btn-primary" disabled={isLoading}>
          {isLoading ? 'Creating Account...' : 'Sign Up'}
        </button>
      </form>
      <div className="form-link-container">
        <p>Already have an account? <Link to="/login" className="form-link">Log in</Link></p>
      </div>
    </div>
  );
};

export default SignupPage;