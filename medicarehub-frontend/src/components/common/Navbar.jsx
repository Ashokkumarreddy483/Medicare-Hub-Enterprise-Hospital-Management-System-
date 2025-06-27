// src/components/common/Navbar.jsx
import React from 'react';
import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext.jsx';

const Navbar = () => {
  const { currentUser, isAuthenticated, logout } = useAuth();

  const canManagePatients = isAuthenticated && currentUser?.roles && (currentUser.roles.includes("ROLE_ADMIN") || currentUser.roles.includes("ROLE_RECEPTIONIST"));
  const canManageDoctors = isAuthenticated && currentUser?.roles?.includes("ROLE_ADMIN");
  const isAdmin = isAuthenticated && currentUser?.roles?.includes("ROLE_ADMIN");
  const isDoctor = isAuthenticated && currentUser?.roles?.includes("ROLE_DOCTOR");
  const isPatient = isAuthenticated && currentUser?.roles?.includes("ROLE_PATIENT");

  return (
    <header className="navbar">
      <div className="container">
        <Link to="/" className="navbar-brand">MediCareHub</Link>
        <nav className="nav-links">
          <NavLink to="/" className={({ isActive }) => isActive ? "active-link" : ""} end>Home</NavLink>
          {isAuthenticated ? (
            <>
              {isPatient && <NavLink to="/patient/dashboard" className={({ isActive }) => isActive ? "active-link" : ""}>My Dashboard</NavLink>}
              {isDoctor && <NavLink to="/doctor/dashboard" className={({ isActive }) => isActive ? "active-link" : ""}>Doctor Dashboard</NavLink>}
              {isAdmin && <NavLink to="/admin/dashboard" className={({ isActive }) => isActive ? "active-link" : ""}>Admin Dashboard</NavLink>}
              {canManagePatients && <NavLink to="/admin/patients" className={({ isActive }) => isActive ? "active-link" : ""}>Manage Patients</NavLink>}
              {canManageDoctors && <NavLink to="/admin/doctors" className={({ isActive }) => isActive ? "active-link" : ""}>Manage Doctors</NavLink>}
              <span className="navbar-username">Hi, {currentUser?.firstName || currentUser?.username}!</span>
              <button onClick={logout} className="btn-logout">Logout</button>
            </>
          ) : (
            <>
              <NavLink to="/login" className={({ isActive }) => isActive ? "active-link" : ""}>Login</NavLink>
              <NavLink to="/signup" className={({ isActive }) => isActive ? "active-link" : ""}>Sign Up</NavLink>
            </>
          )}
        </nav>
      </div>
    </header>
  );
};
export default Navbar;