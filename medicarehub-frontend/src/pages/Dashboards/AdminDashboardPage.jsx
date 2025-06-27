// src/pages/Dashboards/AdminDashboardPage.jsx
import React from 'react';

const AdminDashboardPage = () => { // Removed ": React.FC"
  const getUserData = () => {
    const data = localStorage.getItem('userData');
    try {
      return data ? JSON.parse(data) : null;
    } catch (e) {
      console.error("Error parsing user data from localStorage", e);
      return null;
    }
  }
  const userData = getUserData();

  return (
    <div className="p-6 bg-white rounded-lg shadow-md">
      <h1 className="text-3xl font-bold text-gray-800 mb-4">Admin Dashboard</h1>
       {userData && userData.username && (
        <p className="text-lg text-gray-600 mb-6">
          Welcome, Administrator {userData.username}!
        </p>
      )}
      <p className="text-gray-600 mb-6">Manage users, system settings, and view overall analytics.</p>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-red-100 p-4 rounded-lg shadow hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold text-red-700 mb-2">User Management</h2>
          <p className="text-red-600">Add, edit, or remove user accounts.</p>
          {/* Link to user management page */}
        </div>

        <div className="bg-purple-100 p-4 rounded-lg shadow hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold text-purple-700 mb-2">System Analytics</h2>
          <p className="text-purple-600">View patient registration trends, revenue reports.</p>
          {/* Link to analytics page */}
        </div>

        <div className="bg-gray-200 p-4 rounded-lg shadow hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold text-gray-700 mb-2">System Configuration</h2>
          <p className="text-gray-600">Manage departments, services, roles & permissions.</p>
          {/* Link to settings page */}
        </div>
      </div>
    </div>
  );
};

export default AdminDashboardPage;