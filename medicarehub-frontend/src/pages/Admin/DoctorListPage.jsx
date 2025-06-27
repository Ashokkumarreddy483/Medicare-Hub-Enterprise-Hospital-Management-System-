// src/pages/Admin/DoctorListPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import doctorService from '../../services/doctorService';
import DoctorTable from '../../components/admin/DoctorTable.jsx'; // We will create this
import Pagination from '../../components/common/Pagination.jsx'; // Reusable
// import Alert from '../../components/common/Alert';

const DoctorListPage = () => {
  const [doctors, setDoctors] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 10;

  // Search state
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');

  // Debounce search term
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
    }, 500); // 500ms delay

    return () => {
      clearTimeout(handler);
    };
  }, [searchTerm]);


  const fetchDoctors = useCallback(async (page, search) => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await doctorService.getAllDoctors(page, pageSize, 'user.lastName,asc', search);
      setDoctors(data.content);
      setTotalPages(data.totalPages);
      setCurrentPage(data.number);
      setTotalElements(data.totalElements);
    } catch (err) {
      console.error("Failed to fetch doctors:", err);
      setError(err.response?.data?.message || err.message || "Could not fetch doctor data.");
      setDoctors([]);
    } finally {
      setIsLoading(false);
    }
  }, [pageSize]);

  useEffect(() => {
    // Fetch doctors when page or debouncedSearchTerm changes
    // Reset to page 0 when search term changes
    if (debouncedSearchTerm !== searchTerm && currentPage !== 0) {
        setCurrentPage(0); // Trigger fetch via this state change for new search
    } else {
        fetchDoctors(currentPage, debouncedSearchTerm);
    }
  }, [fetchDoctors, currentPage, debouncedSearchTerm]);


  const handleDeleteDoctor = async (doctorId) => {
    if (window.confirm("Are you sure you want to delete this doctor? This may also affect their user account.")) {
      setError(null);
      setSuccessMessage(null);
      try {
        await doctorService.deleteDoctor(doctorId);
        setSuccessMessage(`Doctor (ID: ${doctorId}) deleted successfully.`);
        let newPage = currentPage;
        if (doctors.length === 1 && currentPage > 0) {
            newPage = currentPage - 1;
        }
        // If current page was changed, useEffect will refetch. Otherwise, fetch manually.
        if (newPage !== currentPage) {
            setCurrentPage(newPage);
        } else {
            fetchDoctors(newPage, debouncedSearchTerm);
        }
      } catch (err) {
        console.error("Failed to delete doctor:", err);
        setError("Failed to delete doctor: " + (err.response?.data?.message || err.message));
      }
    }
  };

  const handlePageChange = (pageNumber) => {
    if (pageNumber >= 0 && pageNumber < totalPages) {
      setCurrentPage(pageNumber);
    }
  };

  const handleSearchChange = (event) => {
    setSearchTerm(event.target.value);
  };


  return (
    <div className="doctor-list-page p-4 md:p-6">
      <div className="flex flex-col sm:flex-row justify-between sm:items-center mb-6 gap-4">
        <h1 className="text-2xl md:text-3xl font-bold text-gray-700">Doctor Management</h1>
        <Link to="/admin/doctors/add" className="btn btn-primary w-full sm:w-auto">
          Add New Doctor
        </Link>
      </div>

      {error && <div className="error-message mb-4">{error}</div>}
      {successMessage && <div className="success-message mb-4">{successMessage}</div>}

      <div className="mb-4 p-4 bg-gray-50 rounded-lg shadow-sm">
        <input
          type="text"
          placeholder="Search by name, specialization, department..."
          className="form-input w-full md:w-1/2"
          value={searchTerm}
          onChange={handleSearchChange}
        />
      </div>

      <DoctorTable
        doctors={doctors}
        onDeleteDoctor={handleDeleteDoctor}
        isLoading={isLoading && doctors.length === 0} // Show table loading only if list is empty
      />
      {isLoading && doctors.length > 0 && <p className="text-center text-blue-500 my-2">Updating list...</p>}


      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={handlePageChange}
        totalElements={totalElements}
        pageSize={pageSize}
      />
    </div>
  );
};

export default DoctorListPage;