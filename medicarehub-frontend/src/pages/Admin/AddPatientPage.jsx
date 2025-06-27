// src/pages/Admin/AddPatientPage.jsx
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import patientService from '../../services/patientService';
import { toast } from 'react-toastify'; // Import toast

const AddPatientPage = () => {
  // ... (formData, formErrors, isLoading, handleChange, validateForm states and functions) ...
  // REMOVE submitError and successMessage states if using toast for everything

  const handleSubmit = async (e) => {
    e.preventDefault();
    // setSubmitError(null); // Not needed if using toast
    // setSuccessMessage(null); // Not needed if using toast

    if (!validateForm()) {
      toast.error("Please correct the form errors."); // Toast for validation summary
      return;
    }

    setIsLoading(true);
    try {
      await patientService.createPatient(formData);
      toast.success("Patient created successfully! Redirecting..."); // Success toast
      setTimeout(() => {
        navigate('/admin/patients');
      }, 2000);
    } catch (err) {
      console.error("Failed to create patient:", err);
      let errorMessage = "Could not create patient.";
       if (err.response?.data) {
        if (typeof err.response.data === 'string') errorMessage = err.response.data;
        else if (err.response.data.message) errorMessage = err.response.data.message;
        else if (typeof err.response.data === 'object') {
            const fieldErrors = Object.entries(err.response.data).map(([key, value]) => `${key}: ${value}`).join('\n'); // Join with newline for toast
            errorMessage = fieldErrors || errorMessage;
        }
      } else if (err.request) errorMessage = 'Network error.';
      else errorMessage = err.message || errorMessage;
      toast.error(errorMessage); // Error toast for API errors
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="form-container max-w-3xl">
      <h2 className="form-title">Add New Patient</h2>
      {/* Removed <p className="error-message">{submitError}</p> and successMessage */}
      {/* Form JSX as before, with field-specific formErrors.fieldName still displayed */}
      {/* ... form fields ... */}
       <form onSubmit={handleSubmit} noValidate>
        {/* ... sections and form groups ... */}
         {/* Example field with validation error display */}
        <section className="mb-6 p-4 border rounded-md shadow-sm bg-white">
            <h3 className="form-section-title">User Account Details</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-4">
                <div className="form-group">
                <label htmlFor="username">Username*</label>
                <input type="text" name="username" id="username" className={`form-input ${formErrors.username ? 'input-error' : ''}`} value={formData.username} onChange={handleChange} required disabled={isLoading} />
                {formErrors.username && <p className="error-text">{formErrors.username}</p>}
                </div>
                {/* ... other fields ... */}
            </div>
        </section>
        {/* ... other sections ... */}
        <div className="mt-8 flex space-x-4">
          <button type="submit" className="btn btn-primary flex-grow py-3" disabled={isLoading}>
            {isLoading ? 'Saving Patient...' : 'Create Patient'}
          </button>
          <Link to="/admin/patients" className="btn bg-gray-300 hover:bg-gray-400 text-gray-800 flex-grow text-center py-3">
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
};
export default AddPatientPage;