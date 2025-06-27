// src/pages/Admin/PatientListPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import patientService from '../../services/patientService.js'; // Make sure this path and extension are correct
import PatientTable from '../../components/admin/PatientTable.jsx'; // CORRECTED IMPORT PATH
import Pagination from '../../components/common/Pagination.jsx';   // Make sure this path and extension are correct
// import Alert from '../../components/common/Alert'; // If you implement an Alert component

const PatientListPage = () => {
  const [patients, setPatients] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 10;

  const [searchTerm, setSearchTerm] = useState(''); // For search functionality
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState(''); // For debounced search

  // Debounce search term effect
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
    }, 500); // 500ms delay before triggering search
    return () => {
      clearTimeout(handler);
    };
  }, [searchTerm]);

  const fetchPatients = useCallback(async (page, search) => {
    setIsLoading(true);
    setError(null);
    // Do not clear successMessage here, let it persist until next action or manual clear
    try {
      // Using user.lastName for sorting as an example, adjust if your User entity is different
      const data = await patientService.getAllPatients(page, pageSize, 'user.lastName,asc', search);
      setPatients(data.content || []); // Fallback to empty array if content is undefined
      setTotalPages(data.totalPages || 0);
      setCurrentPage(data.number || 0);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      console.error("Failed to fetch patients:", err);
      setError(err.response?.data?.message || err.message || "Could not fetch patient data.");
      setPatients([]); // Clear patients on error
      setTotalPages(0); // Reset pagination on error
      setCurrentPage(0);
      setTotalElements(0);
    } finally {
      setIsLoading(false);
    }
  }, [pageSize]); // pageSize is a stable dependency

  // Effect to fetch patients when currentPage or debouncedSearchTerm changes
  useEffect(() => {
    // If search term changes, reset to page 0 to avoid being on a non-existent page for new results
    if (debouncedSearchTerm !== searchTerm && currentPage !== 0) {
        setCurrentPage(0); // This state change will trigger another run of this useEffect
    } else {
        fetchPatients(currentPage, debouncedSearchTerm);
    }
  }, [fetchPatients, currentPage, debouncedSearchTerm, searchTerm]); // Added searchTerm to dependency array

  const handleDeletePatient = async (patientId) => {
    if (window.confirm("Are you sure you want to delete this patient? This action might be irreversible.")) {
      setError(null);
      setSuccessMessage(null);
      try {
        await patientService.deletePatient(patientId);
        setSuccessMessage(`Patient (ID: ${patientId}) deleted successfully.`);

        // Optimistic UI update or refetch:
        // If it's the last item on the current page (and not the only page), go to previous page
        let newPage = currentPage;
        if (patients.length === 1 && currentPage > 0) {
            newPage = currentPage - 1;
        }

        // If page changed, setCurrentPage will trigger refetch via useEffect.
        // Otherwise, call fetchPatients directly for the current page.
        if (newPage !== currentPage) {
            setCurrentPage(newPage);
        } else {
            fetchPatients(newPage, debouncedSearchTerm); // Refetch current (or new current) page
        }

      } catch (err) {
        console.error("Failed to delete patient:", err);
        setError("Failed to delete patient: " + (err.response?.data?.message || err.message));
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
    // No need to set currentPage to 0 here, useEffect will handle it when debouncedSearchTerm changes
  };

  return (
    <div className="patient-list-page p-4 md:p-6"> {/* Uses .patient-list-page class from index.css */}
      <div className="flex flex-col sm:flex-row justify-between sm:items-center mb-6 gap-4">
        <h1 className="text-2xl md:text-3xl font-bold text-gray-700">Patient Management</h1>
        <Link to="/admin/patients/add" className="btn btn-primary w-full sm:w-auto">
          Add New Patient
        </Link>
      </div>

      {error && <div className="error-message mb-4">{error}</div>} {/* Uses .error-message from index.css */}
      {successMessage && <div className="success-message mb-4">{successMessage}</div>} {/* Uses .success-message from index.css */}

      <div className="mb-6 p-4 bg-white rounded-lg shadow"> {/* Enhanced search bar container */}
        <label htmlFor="patientSearch" className="sr-only">Search Patients</label> {/* For accessibility */}
        <input
          type="text"
          id="patientSearch"
          placeholder="Search by name, email, or patient ID..."
          className="form-input w-full md:w-2/3 lg:w-1/2" /* Uses .form-input */
          value={searchTerm}
          onChange={handleSearchChange}
        />
      </div>

      <PatientTable
        patients={patients}
        onDeletePatient={handleDeletePatient}
        isLoading={isLoading && patients.length === 0} // Show table's internal loading only if no data yet
      />
      {/* Show a general loading indicator if loading and data already exists (meaning it's a refresh/page change) */}
      {isLoading && patients.length > 0 && <p className="text-center text-blue-500 my-4">Updating patient list...</p>}


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

export default PatientListPage;