// src/components/admin/DoctorTable.jsx
import React from 'react';
import { Link } from 'react-router-dom';

const DoctorTable = ({ doctors, onDeleteDoctor, isLoading }) => {
  if (isLoading && (!doctors || doctors.length === 0)) { // Show loading if explicit and no data
    return <p className="text-center p-4 my-6 text-gray-500">Loading doctor data...</p>;
  }

  if (!isLoading && (!doctors || doctors.length === 0)) {
    return <p className="text-center text-gray-500 py-10">No doctors found.</p>;
  }

  return (
    <div className="overflow-x-auto bg-white shadow-md rounded-lg">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-100">
          <tr>
            <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Name</th>
            <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Email</th>
            <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Department</th>
            <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Specialization</th>
            <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">License No.</th>
            <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Actions</th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {doctors.map((doctor) => (
            <tr key={doctor.id} className="hover:bg-gray-50 transition-colors duration-150 ease-in-out">
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{doctor.firstName} {doctor.lastName}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{doctor.email}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{doctor.departmentName}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{doctor.specialization}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{doctor.licenseNumber}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <Link
                  to={`/admin/doctors/edit/${doctor.id}`}
                  className="text-indigo-600 hover:text-indigo-800 transition-colors mr-4"
                  title="Edit Doctor"
                >
                  Edit
                </Link>
                <button
                  onClick={() => onDeleteDoctor(doctor.id)}
                  className="text-red-600 hover:text-red-800 transition-colors"
                  title="Delete Doctor"
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default DoctorTable;