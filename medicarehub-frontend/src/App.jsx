// src/App.jsx
import React, { useEffect } from 'react';
import { Routes, Route, Navigate, useNavigate, useLocation, Link } from 'react-router-dom'; // Added Link
import MainLayout from './layouts/MainLayout.jsx';
import HomePage from './pages/HomePage.jsx';
import LoginPage from './pages/Auth/LoginPage.jsx';
import SignupPage from './pages/Auth/SignupPage.jsx';
import PatientDashboardPage from './pages/Dashboards/PatientDashboardPage.jsx';
import DoctorDashboardPage from './pages/Dashboards/DoctorDashboardPage.jsx';
import AdminDashboardPage from './pages/Dashboards/AdminDashboardPage.jsx';
import { useAuth } from './contexts/AuthContext.jsx';

import PatientListPage from './pages/Admin/PatientListPage.jsx';
import AddPatientPage from './pages/Admin/AddPatientPage.jsx';
import EditPatientPage from './pages/Admin/EditPatientPage.jsx';

import DoctorListPage from './pages/Admin/DoctorListPage.jsx';
import AddDoctorPage from './pages/Admin/AddDoctorPage.jsx';
import EditDoctorPage from './pages/Admin/EditDoctorPage.jsx';

import DepartmentListPage from './pages/Admin/DepartmentListPage.jsx';
import AddDepartmentPage from './pages/Admin/AddDepartmentPage.jsx';
import EditDepartmentPage from './pages/Admin/EditDepartmentPage.jsx';


// --- ProtectedRoute defined at the TOP LEVEL of the module ---
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { isAuthenticated, currentUser, isLoading: authLoading } = useAuth();
  const location = useLocation();

  if (authLoading) {
    return <div className="text-center p-10">Loading authentication state...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  const userRoles = currentUser?.roles || [];
  const isAuthorized = !allowedRoles || (allowedRoles && allowedRoles.some(role => userRoles.includes(role)));

  if (!isAuthorized) {
    console.warn(`User ${currentUser?.username} with roles ${userRoles.join(', ')} tried to access restricted route ${location.pathname}. Required: ${allowedRoles?.join(' or ')}`);
    return <Navigate to="/" replace />;
  }

  return children;
};


function App() {
  const { isAuthenticated, currentUser, isLoading: authIsLoading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (authIsLoading) return;

    const currentPath = location.pathname;
    const roles = currentUser?.roles || [];

    if (isAuthenticated) {
      if (currentPath === '/login' || currentPath === '/signup') {
        if (roles.includes("ROLE_ADMIN")) navigate("/admin/dashboard", { replace: true });
        else if (roles.includes("ROLE_DOCTOR")) navigate("/doctor/dashboard", { replace: true });
        else if (roles.includes("ROLE_PATIENT")) navigate("/patient/dashboard", { replace: true });
        else navigate("/", { replace: true });
      }
    } else {
      const broadlyProtectedPaths = [
        "/patient/dashboard", "/doctor/dashboard", "/admin/dashboard",
        "/admin/patients", "/admin/doctors", "/admin/departments" // Added departments
      ];
      if (broadlyProtectedPaths.some(p => currentPath.startsWith(p))) {
         if (currentPath !== '/login' && currentPath !== '/signup') {
            navigate("/login", { replace: true, state: { from: location } });
         }
      }
    }
  }, [isAuthenticated, currentUser, authIsLoading, location.pathname, navigate]);


  if (authIsLoading) {
    return <div className="flex justify-center items-center h-screen">Loading application state...</div>;
  }

  return (
    <MainLayout>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route
          path="/login"
          element={isAuthenticated ? <Navigate to="/" replace /> : <LoginPage /> }
        />
        <Route
          path="/signup"
          element={isAuthenticated ? <Navigate to="/" replace /> : <SignupPage /> }
        />

        {/* === DASHBOARDS === */}
        <Route path="/patient/dashboard" element={<ProtectedRoute allowedRoles={["ROLE_PATIENT"]}><PatientDashboardPage /></ProtectedRoute>} />
        <Route path="/doctor/dashboard" element={<ProtectedRoute allowedRoles={["ROLE_DOCTOR", "ROLE_ADMIN"]}><DoctorDashboardPage /></ProtectedRoute>} />
        <Route path="/admin/dashboard" element={<ProtectedRoute allowedRoles={["ROLE_ADMIN"]}><AdminDashboardPage /></ProtectedRoute>} />

        {/* === ADMIN: PATIENT MANAGEMENT === */}
        <Route path="/admin/patients" element={<ProtectedRoute allowedRoles={["ROLE_ADMIN", "ROLE_RECEPTIONIST"]}><PatientListPage /></ProtectedRoute>} />
        <Route path="/admin/patients/add" element={<ProtectedRoute allowedRoles={["ROLE_ADMIN", "ROLE_RECEPTIONIST"]}><AddPatientPage /></ProtectedRoute>} />
        <Route path="/admin/patients/edit/:patientId" element={<ProtectedRoute allowedRoles={["ROLE_ADMIN", "ROLE_RECEPTIONIST"]}><EditPatientPage /></ProtectedRoute>} />

        {/* === ADMIN: DOCTOR MANAGEMENT === */}
        <Route path="/admin/doctors" element={<ProtectedRoute allowedRoles={["ROLE_ADMIN"]}><DoctorListPage /></ProtectedRoute>} />
        <Route path="/admin/doctors/add" element={<ProtectedRoute allowedRoles={["ROLE_ADMIN"]}><AddDoctorPage /></ProtectedRoute>} />
        <Route path="/admin/doctors/edit/:doctorId" element={<ProtectedRoute allowedRoles={["ROLE_ADMIN"]}><EditDoctorPage /></ProtectedRoute>} />

        {/* === ADMIN: DEPARTMENT MANAGEMENT === */}
        <Route path="/admin/departments" element={<ProtectedRoute allowedRoles={["ROLE_ADMIN"]}><DepartmentListPage /></ProtectedRoute>} />
        <Route path="/admin/departments/add" element={<ProtectedRoute allowedRoles={["ROLE_ADMIN"]}><AddDepartmentPage /></ProtectedRoute>} />
        <Route path="/admin/departments/edit/:departmentId" element={<ProtectedRoute allowedRoles={["ROLE_ADMIN"]}><EditDepartmentPage /></ProtectedRoute>} />

        {/* Fallback 404 Page - Corrected JSX comment or structure */}
        <Route
          path="*"
          element={
            // Method 1: Comment outside the JSX expression if it's just for a note
            // /* This is the 404 page */
            <div className="text-center p-10">
              <h1 className="text-4xl font-bold mb-4">404 - Page Not Found</h1>
              <p className="mb-6">Sorry, the page you are looking for does not exist.</p>
              <Link to="/" className="btn btn-primary">Go to Homepage</Link>
            </div>
            // Method 2: If you need a comment *inside* the JSX expression (less common here)
            // {
            //   /* This is a comment inside JSX expression */
            //   (
            //     <div className="text-center p-10">
            //       {/* ... content ... */}
            //     </div>
            //   )
            // }
          }
        /> {/* <--- Ensure this closing tag for Route is present */}
      </Routes>
    </MainLayout>
  );
} // <--- Ensure this is the correct closing brace for the App function

export default App; // <--- Ensure this is at the top level