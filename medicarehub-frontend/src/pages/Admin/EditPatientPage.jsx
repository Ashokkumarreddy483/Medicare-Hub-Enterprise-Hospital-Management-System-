// src/pages/Admin/EditPatientPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import patientService from '../../services/patientService';

const EditPatientPage = () => {
  const navigate = useNavigate();
  const { patientId } = useParams();

  const [formData, setFormData] = useState({
    username: '', email: '', // Read-only display
    firstName: '', lastName: '', phoneNumber: '', address: '', dateOfBirth: '',
    bloodGroup: '', medicalHistorySummary: '', emergencyContactName: '',
    emergencyContactPhone: '', emergencyContactRelationship: '',
    registrationDate: '', // Read-only display
  });

  const [formErrors, setFormErrors] = useState({}); // For client-side validation errors
  const [submitError, setSubmitError] = useState(null); // For API submission errors
  const [successMessage, setSuccessMessage] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isFetching, setIsFetching] = useState(true);

  const fetchPatientData = useCallback(async () => {
    if (!patientId) {
      setSubmitError("Patient ID is missing.");
      setIsFetching(false);
      return;
    }
    setIsFetching(true);
    setSubmitError(null); // Clear previous submission errors
    setFormErrors({});    // Clear previous form errors
    try {
      const data = await patientService.getPatientById(Number(patientId));
      setFormData({
        username: data.username || '',
        email: data.email || '',
        firstName: data.firstName || '',
        lastName: data.lastName || '',
        phoneNumber: data.phoneNumber || '',
        address: data.address || '',
        dateOfBirth: data.dateOfBirth ? data.dateOfBirth.split('T')[0] : '',
        bloodGroup: data.bloodGroup || '',
        medicalHistorySummary: data.medicalHistorySummary || '',
        emergencyContactName: data.emergencyContactName || '',
        emergencyContactPhone: data.emergencyContactPhone || '',
        emergencyContactRelationship: data.emergencyContactRelationship || '',
        registrationDate: data.registrationDate ? data.registrationDate.split('T')[0] : '',
      });
    } catch (err) {
      console.error("Failed to fetch patient data for edit:", err);
      setSubmitError(err.response?.data?.message || err.message || "Could not load patient data.");
    } finally {
      setIsFetching(false);
    }
  }, [patientId]);

  useEffect(() => {
    fetchPatientData();
  }, [fetchPatientData]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
    if (formErrors[name]) { // Clear error for this field when user types
      setFormErrors({ ...formErrors, [name]: null });
    }
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.firstName.trim()) errors.firstName = "First name is required.";
    if (!formData.lastName.trim()) errors.lastName = "Last name is required.";
    if (!formData.dateOfBirth) errors.dateOfBirth = "Date of birth is required.";
    else if (new Date(formData.dateOfBirth) >= new Date(new Date().toDateString())) errors.dateOfBirth = "Date of birth must be in the past.";
    // Add more validations for editable fields as needed
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitError(null);
    setSuccessMessage(null);

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);

    const updatePayload = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        phoneNumber: formData.phoneNumber,
        address: formData.address,
        dateOfBirth: formData.dateOfBirth,
        bloodGroup: formData.bloodGroup,
        medicalHistorySummary: formData.medicalHistorySummary,
        emergencyContactName: formData.emergencyContactName,
        emergencyContactPhone: formData.emergencyContactPhone,
        emergencyContactRelationship: formData.emergencyContactRelationship,
        // Send original username, email, etc., if backend DTO requires them for validation
        // but service layer should ideally ignore them for update logic
        username: formData.username,
        email: formData.email,
        password: "DUMMY_PASSWORD_UPDATE_IGNORED", // Example: service ignores this
        registrationDate: formData.registrationDate,
    };

    try {
      await patientService.updatePatient(Number(patientId), updatePayload);
      setSuccessMessage("Patient details updated successfully! Redirecting...");
      setTimeout(() => {
        navigate('/admin/patients');
      }, 2000);
    } catch (err) {
      console.error("Failed to update patient:", err);
      let errorMessage = 'Could not update patient.';
      if (err.response?.data) {
        if (typeof err.response.data === 'string') errorMessage = err.response.data;
        else if (err.response.data.message) errorMessage = err.response.data.message;
        else if (typeof err.response.data === 'object') {
             const fieldErrors = Object.entries(err.response.data).map(([key, value]) => `${key}: ${value}`).join('; ');
             errorMessage = fieldErrors || errorMessage;
        }
      } else if (err.request) errorMessage = 'Network error.';
      else errorMessage = err.message || errorMessage;
      setSubmitError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  if (isFetching) {
    return <div className="text-center p-10">Loading patient details...</div>;
  }

  if (submitError && !isFetching && !formData.firstName) { // If fetch error and no data loaded
    return <div className="error-message m-10">{submitError} <Link to="/admin/patients" className="form-link ml-2">Go back</Link></div>;
  }

  return (
    <div className="form-container max-w-3xl">
      <h2 className="form-title">Edit Patient: {formData.firstName} {formData.lastName} <span className="text-base font-normal text-gray-500">({formData.username})</span></h2>

      {submitError && <div className="error-message mb-4">{submitError}</div>}
      {successMessage && <div className="success-message mb-4">{successMessage}</div>}

      <form onSubmit={handleSubmit} noValidate>
        <section className="mb-6 p-4 border rounded-md shadow-sm bg-white">
          <h3 className="form-section-title">Account Details (Read-only)</h3>
           <div className="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-4">
              <div className="form-group">
                  <label htmlFor="usernameDisplay">Username</label>
                  <input type="text" id="usernameDisplay" className="form-input bg-gray-200 cursor-not-allowed" value={formData.username} readOnly />
              </div>
              <div className="form-group">
                  <label htmlFor="emailDisplay">Email</label>
                  <input type="email" id="emailDisplay" className="form-input bg-gray-200 cursor-not-allowed" value={formData.email} readOnly />
              </div>
          </div>
        </section>

        <section className="mb-6 p-4 border rounded-md shadow-sm bg-white">
          <h3 className="form-section-title">Personal Information</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-4">
            <div className="form-group">
              <label htmlFor="firstName">First Name*</label>
              <input type="text" name="firstName" id="firstName" className={`form-input ${formErrors.firstName ? 'input-error' : ''}`} value={formData.firstName} onChange={handleChange} required disabled={isLoading || isFetching} />
              {formErrors.firstName && <p className="error-text">{formErrors.firstName}</p>}
            </div>
            <div className="form-group">
              <label htmlFor="lastName">Last Name*</label>
              <input type="text" name="lastName" id="lastName" className={`form-input ${formErrors.lastName ? 'input-error' : ''}`} value={formData.lastName} onChange={handleChange} required disabled={isLoading || isFetching} />
              {formErrors.lastName && <p className="error-text">{formErrors.lastName}</p>}
            </div>
            <div className="form-group">
              <label htmlFor="dateOfBirth">Date of Birth*</label>
              <input type="date" name="dateOfBirth" id="dateOfBirth" className={`form-input ${formErrors.dateOfBirth ? 'input-error' : ''}`} value={formData.dateOfBirth} onChange={handleChange} required disabled={isLoading || isFetching} max={new Date().toISOString().split("T")[0]}/>
              {formErrors.dateOfBirth && <p className="error-text">{formErrors.dateOfBirth}</p>}
            </div>
            <div className="form-group">
              <label htmlFor="phoneNumber">Phone Number</label>
              <input type="tel" name="phoneNumber" id="phoneNumber" className="form-input" value={formData.phoneNumber} onChange={handleChange} disabled={isLoading || isFetching} />
            </div>
          </div>
          <div className="form-group mt-4">
            <label htmlFor="address">Address</label>
            <textarea name="address" id="address" rows="3" className="form-input" value={formData.address} onChange={handleChange} disabled={isLoading || isFetching}></textarea>
          </div>
        </section>

        <section className="mb-6 p-4 border rounded-md shadow-sm bg-white">
          <h3 className="form-section-title">Medical Information</h3>
           <div className="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-4">
              <div className="form-group">
                  <label htmlFor="bloodGroup">Blood Group</label>
                  <input type="text" name="bloodGroup" id="bloodGroup" className="form-input" value={formData.bloodGroup} onChange={handleChange} disabled={isLoading || isFetching} />
              </div>
              <div className="form-group">
                  <label htmlFor="registrationDateDisplay">Registration Date</label>
                  <input type="date" id="registrationDateDisplay" className="form-input bg-gray-200 cursor-not-allowed" value={formData.registrationDate} readOnly />
              </div>
          </div>
          <div className="form-group mt-4">
            <label htmlFor="medicalHistorySummary">Medical History Summary</label>
            <textarea name="medicalHistorySummary" id="medicalHistorySummary" rows="3" className="form-input" value={formData.medicalHistorySummary} onChange={handleChange} disabled={isLoading || isFetching}></textarea>
          </div>
        </section>

        <section className="mb-6 p-4 border rounded-md shadow-sm bg-white">
          <h3 className="form-section-title">Emergency Contact</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-4">
              <div className="form-group">
                  <label htmlFor="emergencyContactName">Contact Name</label>
                  <input type="text" name="emergencyContactName" id="emergencyContactName" className="form-input" value={formData.emergencyContactName} onChange={handleChange} disabled={isLoading || isFetching} />
              </div>
              <div className="form-group">
                  <label htmlFor="emergencyContactPhone">Contact Phone</label>
                  <input type="tel" name="emergencyContactPhone" id="emergencyContactPhone" className="form-input" value={formData.emergencyContactPhone} onChange={handleChange} disabled={isLoading || isFetching} />
              </div>
          </div>
          <div className="form-group mt-4">
              <label htmlFor="emergencyContactRelationship">Relationship</label>
              <input type="text" name="emergencyContactRelationship" id="emergencyContactRelationship" className="form-input" value={formData.emergencyContactRelationship} onChange={handleChange} disabled={isLoading || isFetching} />
          </div>
        </section>

        <div className="mt-8 flex space-x-4">
          <button type="submit" className="btn btn-primary flex-grow py-3" disabled={isLoading || isFetching}>
            {isLoading ? 'Updating Patient...' : 'Update Patient'}
          </button>
          <Link to="/admin/patients" className="btn bg-gray-300 hover:bg-gray-400 text-gray-800 flex-grow text-center py-3">
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
};

export default EditPatientPage;