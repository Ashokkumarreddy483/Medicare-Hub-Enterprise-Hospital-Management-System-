// src/components/admin/PatientTable.jsx
import React from 'react';
import { Link } from 'react-router-dom';

const PatientTable = ({ patients, onDeletePatient, isLoading }) => {
  if (isLoading && (!patients || patients.length === 0)) {
    return <p className="text-center p-4 my-6 text-gray-500">Loading patient data...</p>;
  }

  if (!isLoading && (!patients || patients.length === 0)) {
    return <p className="text-center text-gray-500 py-10">No patients found.</p>;
  }

  return (
    <div className="overflow-x-auto bg-white shadow-md rounded-lg">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-100"> {/* Slightly different bg for thead */}
          <tr>
            <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
              Unique ID
            </th>
            <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
              Name
            </th>
            <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
              Email
            </th>
            <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
              Phone
            </th>
            <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
              Reg. Date
            </th>
            <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
              Actions
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {patients.map((patient) => (
            <tr key={patient.id} className="hover:bg-gray-50 transition-colors duration-150 ease-in-out">
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">{patient.patientUniqueId}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{patient.firstName} {patient.lastName}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{patient.email}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{patient.phoneNumber || 'N/A'}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {patient.registrationDate ? new Date(patient.registrationDate).toLocaleDateString() : 'N/A'}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <Link
                  to={`/admin/patients/edit/${patient.id}`}
                  className="text-indigo-600 hover:text-indigo-800 transition-colors mr-4" // Increased margin
                  title="Edit Patient"
                >
                  Edit
                </Link>
                <button
                  onClick={() => onDeletePatient(patient.id)}
                  className="text-red-600 hover:text-red-800 transition-colors"
                  title="Delete Patient"
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

export default PatientTable;