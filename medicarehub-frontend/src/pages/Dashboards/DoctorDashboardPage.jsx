// src/pages/Dashboards/DoctorDashboardPage.jsx
import React from 'react';

const DoctorDashboardPage = () => { // Ensure no ": React.FC"
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
      <h1 className="text-3xl font-bold text-gray-800 mb-4">
        Doctor Dashboard
      </h1>
      {userData && userData.username && (
        <p className="text-lg text-gray-600 mb-6">
          Welcome, Dr. {userData.lastName || userData.username}!
        </p>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-blue-100 p-4 rounded-lg shadow hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold text-blue-700 mb-2">Upcoming Appointments</h2>
          <p className="text-blue-600">You have 5 upcoming appointments today.</p>
        </div>
        <div className="bg-green-100 p-4 rounded-lg shadow hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold text-green-700 mb-2">My Patients</h2>
          <p className="text-green-600">View and manage your patient records.</p>
        </div>
        <div className="bg-yellow-100 p-4 rounded-lg shadow hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold text-yellow-700 mb-2">Lab Results Pending</h2>
          <p className="text-yellow-600">3 lab results require your review.</p>
        </div>
        <div className="bg-indigo-100 p-4 rounded-lg shadow hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold text-indigo-700 mb-2">Create Prescription</h2>
          <p className="text-indigo-600">Quick link to issue a new prescription.</p>
        </div>
      </div>
      <div className="mt-8">
        <h2 className="text-2xl font-semibold text-gray-700 mb-3">Activity Feed</h2>
        <ul className="list-disc list-inside bg-gray-50 p-4 rounded-md">
          <li className="text-gray-600">Patient John Doe's lab report uploaded.</li>
          <li className="text-gray-600">New appointment scheduled for Jane Smith.</li>
          <li className="text-gray-600">Prescription dispensed for Michael Brown.</li>
        </ul>
      </div>
    </div>
  );
};

export default DoctorDashboardPage;