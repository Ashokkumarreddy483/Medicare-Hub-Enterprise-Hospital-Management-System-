// src/pages/HomePage.jsx
import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext.jsx'; // Ensure .jsx if AuthContext is .jsx

// Example: Placeholder icons (you'd use actual SVGs or an icon library)
// For real icons, you might import them or use an icon font class
const FeatureIcon = ({ iconContent, colorClass = "icon-blue" }) => (
  <div className={`feature-icon ${colorClass}`}>{iconContent}</div>
);

const HomePage = () => {
  const { isAuthenticated, currentUser } = useAuth();
  const navigate = useNavigate();

  const getDashboardPathForCurrentUser = () => {
    if (!currentUser || !currentUser.roles || currentUser.roles.length === 0) {
      return "/";
    }
    if (currentUser.roles.includes("ROLE_ADMIN")) return "/admin/dashboard";
    if (currentUser.roles.includes("ROLE_DOCTOR")) return "/doctor/dashboard";
    if (currentUser.roles.includes("ROLE_PATIENT")) return "/patient/dashboard";
    // Add more role-based dashboard paths for Nurse, Receptionist etc. if they have dedicated dashboards
    return "/"; // Fallback
  };

  const handleGoToDashboard = () => {
    navigate(getDashboardPathForCurrentUser());
  };

  // Define your modules here
  const modules = [
    {
      name: "Patient Management",
      description: "Register, search, and manage all patient records, demographics, and medical history with ease.",
      icon: "👤", // Placeholder icon
      linkPath: "/admin/patients", // Example direct link
      rolesWithLink: ["ROLE_ADMIN", "ROLE_RECEPTIONIST"], // Roles that see the direct link
      publicInfoLink: null, // No public "learn more" link for this example
      requiresAuthForLink: true
    },
    {
      name: "Doctor Management",
      description: "Maintain comprehensive doctor profiles, specializations, schedules, and department assignments.",
      icon: "⚕️",
      linkPath: "/admin/doctors",
      rolesWithLink: ["ROLE_ADMIN"],
      publicInfoLink: null,
      requiresAuthForLink: true
    },
    {
      name: "Appointment Scheduling",
      description: "Book, view, update, and cancel appointments seamlessly. Search for doctor availability by date or department.",
      icon: "📅",
      // Dynamic link based on role
      linkPath: () => {
          if (currentUser?.roles?.includes("ROLE_PATIENT")) return "/patient/appointments/book"; // Example path
          if (currentUser?.roles?.includes("ROLE_RECEPTIONIST") || currentUser?.roles?.includes("ROLE_ADMIN")) return "/admin/appointments/manage"; // Example path
          if (currentUser?.roles?.includes("ROLE_DOCTOR")) return "/doctor/appointments/view"; // Example path
          return "/find-a-doctor"; // Fallback or public view
      },
      rolesWithLink: ["ROLE_PATIENT", "ROLE_RECEPTIONIST", "ROLE_ADMIN", "ROLE_DOCTOR"],
      publicInfoLink: "/find-a-doctor", // Public link to view doctors
      requiresAuthForLink: true // The primary action (booking/managing) requires auth
    },
    {
      name: "Staff Management",
      description: "Administrators can add, view, and manage profiles for nurses, receptionists, and other hospital staff.",
      icon: "👥",
      linkPath: "/admin/staff", // Future link
      rolesWithLink: ["ROLE_ADMIN"],
      publicInfoLink: null,
      requiresAuthForLink: true
    },
    {
      name: "Billing & Invoicing",
      description: "Generate accurate invoices, track payment statuses, and manage hospital financials efficiently.",
      icon: "💳",
      linkPath: null, // Placeholder
      rolesWithLink: ["ROLE_ADMIN", "ROLE_RECEPTIONIST"],
      publicInfoLink: null,
      requiresAuthForLink: true
    },
    {
      name: "Insurance Module",
      description: "Manage insurance providers and link patient insurance details for streamlined billing and claims.",
      icon: "🛡️",
      linkPath: null, // Placeholder
      rolesWithLink: ["ROLE_ADMIN", "ROLE_RECEPTIONIST", "ROLE_PATIENT"],
      publicInfoLink: null,
      requiresAuthForLink: true
    },
    {
      name: "Role-Specific Dashboards",
      description: "Customized views with key metrics, quick links, and task summaries tailored to each user role.",
      icon: "📊",
      linkPath: getDashboardPathForCurrentUser, // Use the function here
      rolesWithLink: [], // All authenticated users have a dashboard path
      publicInfoLink: null,
      requiresAuthForLink: true
    }
  ];

  return (
    <div className="home-page-container">

      <section className="hero-section">
        <div className="container hero-content">
          <h1 className="hero-title">MediCareHub</h1>
          <p className="hero-subtitle">
            Empowering Healthcare with Smart, Efficient, and Integrated Management Solutions.
          </p>
          {!isAuthenticated ? (
            <div className="hero-cta-buttons">
              <Link to="/login" className="btn btn-primary btn-lg">Access Your Portal</Link>
              <Link to="/signup" className="btn btn-secondary btn-lg">Register Now</Link>
            </div>
          ) : (
            <div className="authenticated-welcome-home">
              <p className="success-message large-success-message">
                Welcome back, {currentUser?.firstName || currentUser?.username}!
              </p>
              <button onClick={handleGoToDashboard} className="btn btn-primary btn-lg" style={{ marginTop: '1rem' }}>
                Go to Your Dashboard
              </button>
            </div>
          )}
        </div>
      </section>

      <section className="features-section container">
        <h2 className="section-title">Comprehensive Hospital Management</h2>
        <p className="section-tagline">
          MediCareHub offers a complete suite of modules designed to streamline every aspect of hospital operations,
          enhancing patient care and operational efficiency.
        </p>
        <div className="features-grid">
          {modules.map((module, index) => {
            let moduleLink = null;
            if (typeof module.linkPath === 'function') {
              moduleLink = isAuthenticated ? module.linkPath() : module.publicInfoLink;
            } else {
              moduleLink = isAuthenticated && module.requiresAuthForLink ? module.linkPath : module.publicInfoLink;
            }

            // Determine if the "Go to Module" link should be shown based on auth and roles
            let showGoToModuleLink = false;
            if (isAuthenticated && module.requiresAuthForLink && module.linkPath) {
                if (!module.rolesWithLink || module.rolesWithLink.length === 0) { // If no specific roles, any authenticated user can see
                    showGoToModuleLink = true;
                } else {
                    showGoToModuleLink = module.rolesWithLink.some(role => currentUser?.roles?.includes(role));
                }
                 // If linkPath is a function, it already considers the current user for dynamic link generation
                if (typeof module.linkPath === 'function') showGoToModuleLink = true;
            }


            return (
              <div key={index} className="feature-card">
                <FeatureIcon iconContent={module.icon} colorClass={index % 3 === 0 ? 'icon-blue' : (index % 3 === 1 ? 'icon-green' : 'icon-orange')} />
                <h3>{module.name}</h3>
                <p>{module.description}</p>
                {showGoToModuleLink && typeof module.linkPath === 'function' && module.linkPath() && (
                     <Link to={module.linkPath()} className="feature-link btn btn-primary-outline">
                        Go to Module →
                     </Link>
                )}
                {showGoToModuleLink && typeof module.linkPath === 'string' && (
                    <Link to={module.linkPath} className="feature-link btn btn-primary-outline">
                        Go to Module →
                    </Link>
                )}
                {!isAuthenticated && module.publicInfoLink && (
                  <Link to={module.publicInfoLink} className="feature-link btn btn-primary-outline">
                    Learn More →
                  </Link>
                )}
              </div>
            );
          })}
        </div>
      </section>

      {!isAuthenticated && (
        <section className="cta-section container">
          <h2>Transform Your Healthcare Services Today</h2>
          <p>Discover the power of integrated management. Sign up for MediCareHub and elevate your patient care experience.</p>
          <Link to="/signup" className="btn btn-success btn-lg">Get Started with MediCareHub</Link>
        </section>
      )}
    </div>
  );
};

export default HomePage;