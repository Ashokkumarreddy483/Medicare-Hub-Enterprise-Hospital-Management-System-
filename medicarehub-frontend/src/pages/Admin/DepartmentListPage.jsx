// src/pages/Admin/DepartmentListPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import departmentService from '../../services/departmentService.js';
import { toast } from 'react-toastify';
// import Spinner from '../../components/common/Spinner'; // If needed

const DepartmentListPage = () => {
  const [departments, setDepartments] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchDepartments = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await departmentService.getAllDepartments();
      setDepartments(data || []);
    } catch (err) {
      console.error("Failed to fetch departments:", err);
      setError(err.response?.data?.message || err.message || "Could not fetch departments.");
      setDepartments([]);
      toast.error(error || "Failed to load departments.");
    } finally {
      setIsLoading(false);
    }
  }, [error]); // Added error to dependency array to potentially clear it

  useEffect(() => {
    fetchDepartments();
  }, [fetchDepartments]);

  const handleDeleteDepartment = async (id) => {
    if (window.confirm("Are you sure you want to delete this department? This might affect associated doctors.")) {
      try {
        await departmentService.deleteDepartment(id);
        toast.success(`Department (ID: ${id}) deleted successfully.`);
        fetchDepartments(); // Refresh the list
      } catch (err) {
        console.error("Failed to delete department:", err);
        toast.error(err.response?.data?.message || err.message || "Could not delete department.");
      }
    }
  };

  if (isLoading && departments.length === 0) {
    return <div className="text-center p-10">Loading departments...</div>;
  }

  // If there was an error during fetch and departments list is empty
  // We show the error, but allow retrying by keeping the component rendered
  // if (error && departments.length === 0) {
  //   return <div className="error-message m-10">{error}</div>;
  // }


  return (
    <div className="p-4 md:p-6">
      <div className="flex flex-col sm:flex-row justify-between sm:items-center mb-6 gap-4">
        <h1 className="text-2xl md:text-3xl font-bold text-gray-700">Department Management</h1>
        <Link to="/admin/departments/add" className="btn btn-primary w-full sm:w-auto">
          Add New Department
        </Link>
      </div>
      {error && <div className="error-message mb-4">{error}</div>} {/* Display error if any */}

      {isLoading && departments.length > 0 && <p className="text-center text-blue-500 my-2">Updating list...</p>}


      {!isLoading && departments.length === 0 && !error && (
         <p className="text-center text-gray-500 py-10">No departments found. Add one to get started!</p>
      )}

      {departments.length > 0 && (
        <div className="overflow-x-auto bg-white shadow-md rounded-lg">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-100">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">ID</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Name</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Description</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {departments.map(dept => (
                <tr key={dept.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">{dept.id}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{dept.name}</td>
                  <td className="px-6 py-4 text-sm text-gray-500 max-w-xs truncate" title={dept.description}>{dept.description || 'N/A'}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <Link to={`/admin/departments/edit/${dept.id}`} className="text-indigo-600 hover:text-indigo-800 mr-4">Edit</Link>
                    <button onClick={() => handleDeleteDepartment(dept.id)} className="text-red-600 hover:text-red-800">Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {/* No pagination for departments for now, assuming a smaller list */}
    </div>
  );
};

export default DepartmentListPage;