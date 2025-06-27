// src/components/common/Navbar.jsx
import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext'; // Import useAuth

const Navbar = () => { // Removed onAuthChange prop
  const { currentUser, isAuthenticated, logout } = useAuth(); // Get from context

  const handleLogout = () => {
    logout(); // Use context logout
  };

  return (
    <header className="navbar">
      <div className="container">
        <Link to="/" className="navbar-brand">MediCareHub</Link>
        <nav className="nav-links">
          <Link to="/">Home</Link>
          {!isAuthenticated ? (
            <>
              <Link to="/login">Login</Link>
              <Link to="/signup">Sign Up</Link>
            </>
          ) : (
            <>
              {currentUser?.roles?.includes("ROLE_ADMIN") && <Link to="/admin/dashboard">Admin</Link>}
              {currentUser?.roles?.includes("ROLE_DOCTOR") && <Link to="/doctor/dashboard">Doctor</Link>}
              {currentUser?.roles?.includes("ROLE_PATIENT") && <Link to="/patient/dashboard">Dashboard</Link>}
              <button onClick={handleLogout}>Logout ({currentUser?.username})</button>
            </>
          )}
        </nav>
      </div>
    </header>
  );
};

export default Navbar;