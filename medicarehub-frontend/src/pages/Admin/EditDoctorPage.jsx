// src/pages/Admin/EditDoctorPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import doctorService from '../../services/doctorService';
import { toast } from 'react-toastify';

const EditDoctorPage = () => {
  const navigate = useNavigate();
  const { doctorId } = useParams();
  const [formData, setFormData] = useState({
    username: '', email: '', // Read-only for display
    firstName: '', lastName: '', phoneNumber: '',
    departmentId: '', specialization: '', licenseNumber: '',
    yearsOfExperience: '', consultationFee: '', qualifications: '',
  });
  const [departments, setDepartments] = useState([]);
  const [formErrors, setFormErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false); // For submit
  const [isFetching, setIsFetching] = useState(true); // For initial data load

  const fetchDoctorAndDepartments = useCallback(async () => {
    if (!doctorId) {
      toast.error("Doctor ID is missing.");
      setIsFetching(false);
      return;
    }
    setIsFetching(true);
    setFormErrors({});
    try {
      // Fetch both simultaneously
      const [doctorData, deptsData] = await Promise.all([
        doctorService.getDoctorById(Number(doctorId)),
        doctorService.getAllDepartments()
      ]);

      setDepartments(deptsData || []);
      setFormData({
        username: doctorData.username || '',
        email: doctorData.email || '',
        firstName: doctorData.firstName || '',
        lastName: doctorData.lastName || '',
        phoneNumber: doctorData.phoneNumber || '',
        departmentId: doctorData.departmentId || '',
        specialization: doctorData.specialization || '',
        licenseNumber: doctorData.licenseNumber || '',
        yearsOfExperience: doctorData.yearsOfExperience !== null && doctorData.yearsOfExperience !== undefined ? doctorData.yearsOfExperience : '',
        consultationFee: doctorData.consultationFee !== null && doctorData.consultationFee !== undefined ? doctorData.consultationFee : '',
        qualifications: doctorData.qualifications || '',
      });

    } catch (err) {
      console.error("Failed to fetch doctor data or departments:", err);
      toast.error(err.response?.data?.message || err.message || "Could not load doctor data.");
    } finally {
      setIsFetching(false);
    }
  }, [doctorId]);

  useEffect(() => {
    fetchDoctorAndDepartments();
  }, [fetchDoctorAndDepartments]);

  const handleChange = (e) => { /* Same as AddDoctorPage */
    const { name, value, type } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'number' ? (value === '' ? '' : Number(value)) : value
    });
    if (formErrors[name]) setFormErrors({ ...formErrors, [name]: null });
  };

  const validateForm = () => { /* Same editable field validations as AddDoctorPage */
    const errors = {};
    if (!formData.firstName.trim()) errors.firstName = "First name is required.";
    if (!formData.lastName.trim()) errors.lastName = "Last name is required.";
    if (!formData.departmentId) errors.departmentId = "Department is required.";
    if (!formData.specialization.trim()) errors.specialization = "Specialization is required.";
    if (!formData.licenseNumber.trim()) errors.licenseNumber = "License number is required.";
    if (formData.yearsOfExperience !== '' && Number(formData.yearsOfExperience) < 0) errors.yearsOfExperience = "Experience can't be negative.";
    if (formData.consultationFee !== '' && parseFloat(formData.consultationFee) < 0) errors.consultationFee = "Fee can't be negative.";
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) {
      toast.error("Please correct the form errors.");
      return;
    }
    setIsLoading(true);
    // Exclude username, email, password from update payload sent to backend
    // The backend DTO for update should ideally not require them or service layer should ignore them.
    const { username, email, password, ...updateData } = formData;
    const payload = {
        ...updateData,
        yearsOfExperience: updateData.yearsOfExperience === '' ? null : Number(updateData.yearsOfExperience),
        consultationFee: updateData.consultationFee === '' ? null : parseFloat(updateData.consultationFee),
        departmentId: Number(updateData.departmentId),
        // If your backend update DTO still requires username/email for @Valid (even if not updated)
        // send the original ones. Password should NOT be sent or should be a dummy ignored value.
        username: formData.username, // Send original username
        email: formData.email,       // Send original email
        password: "DUMMY_PASSWORD_UPDATE_IGNORED", // Service should ignore this
    };

    try {
      await doctorService.updateDoctor(Number(doctorId), payload);
      toast.success("Doctor updated successfully! Redirecting...");
      setTimeout(() => navigate('/admin/doctors'), 2000);
    } catch (err) {
      console.error("Failed to update doctor:", err);
      const apiError = err.response?.data;
      let errorMessage = "Could not update doctor.";
      if (apiError) {
        if (typeof apiError === 'string') errorMessage = apiError;
        else if (apiError.message) errorMessage = apiError.message;
        else if (typeof apiError === 'object') {
             const fieldErrors = Object.entries(apiError).map(([key, value]) => `${key}: ${value}`).join('; ');
             errorMessage = fieldErrors || errorMessage;
        }
      } else if (err.request) errorMessage = 'Network error.';
      else errorMessage = err.message || errorMessage;
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  if (isFetching) return <div className="text-center p-10">Loading doctor details...</div>;
  // Error during fetch:
  if (!isFetching && !formData.username && doctorId) { // Check if formData is still empty after fetch attempt
     return <div className="error-message m-10">Could not load doctor data. <Link to="/admin/doctors" className="form-link">Go back</Link></div>;
  }


  return (
    <div className="form-container max-w-3xl">
      <h2 className="form-title">Edit Doctor: {formData.firstName} {formData.lastName} <span className="text-base font-normal text-gray-500">({formData.username})</span></h2>
      <form onSubmit={handleSubmit} noValidate>
        {/* User Account Details (Read-Only) */}
        <section className="form-section">
          <h3 className="form-section-title">User Account (Read-only)</h3>
          <div className="grid md:grid-cols-2 gap-x-6 gap-y-4">
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

        {/* Personal Information */}
        <section className="form-section">
          <h3 className="form-section-title">Personal Information</h3>
          <div className="grid md:grid-cols-2 gap-x-6 gap-y-4">
            <div className="form-group">
              <label htmlFor="firstName">First Name*</label>
              <input type="text" name="firstName" id="firstName" className={`form-input ${formErrors.firstName ? 'input-error' : ''}`} value={formData.firstName} onChange={handleChange} required />
              {formErrors.firstName && <p className="error-text">{formErrors.firstName}</p>}
            </div>
            {/* ... other personal info fields: lastName, phoneNumber ... */}
            <div className="form-group">
              <label htmlFor="lastName">Last Name*</label>
              <input type="text" name="lastName" id="lastName" className={`form-input ${formErrors.lastName ? 'input-error' : ''}`} value={formData.lastName} onChange={handleChange} required />
              {formErrors.lastName && <p className="error-text">{formErrors.lastName}</p>}
            </div>
            <div className="form-group">
              <label htmlFor="phoneNumber">Phone Number</label>
              <input type="tel" name="phoneNumber" id="phoneNumber" className="form-input" value={formData.phoneNumber} onChange={handleChange} />
            </div>
          </div>
        </section>

        {/* Professional Details */}
        <section className="form-section">
          <h3 className="form-section-title">Professional Details</h3>
          <div className="grid md:grid-cols-2 gap-x-6 gap-y-4">
            <div className="form-group">
              <label htmlFor="departmentId">Department*</label>
              <select name="departmentId" id="departmentId" className={`form-input ${formErrors.departmentId ? 'input-error' : ''}`} value={formData.departmentId} onChange={handleChange} required disabled={isFetching}>
                <option value="">{isFetching ? "Loading depts..." : "Select Department"}</option>
                {departments.map(dept => (
                  <option key={dept.id} value={dept.id}>{dept.name}</option>
                ))}
              </select>
              {formErrors.departmentId && <p className="error-text">{formErrors.departmentId}</p>}
            </div>
             {/* ... other professional fields: specialization, licenseNumber, yearsOfExperience ... */}
             <div className="form-group">
              <label htmlFor="specialization">Specialization*</label>
              <input type="text" name="specialization" id="specialization" className={`form-input ${formErrors.specialization ? 'input-error' : ''}`} value={formData.specialization} onChange={handleChange} required />
              {formErrors.specialization && <p className="error-text">{formErrors.specialization}</p>}
            </div>
            <div className="form-group">
              <label htmlFor="licenseNumber">License Number*</label> {/* Usually not editable or with strict rules */}
              <input type="text" name="licenseNumber" id="licenseNumber" className={`form-input ${formErrors.licenseNumber ? 'input-error' : ''} ${!doctorId ? '' : 'bg-gray-200 cursor-not-allowed'}`} value={formData.licenseNumber} onChange={handleChange} required readOnly={!!doctorId} /> {/* Read-only on edit */}
              {formErrors.licenseNumber && <p className="error-text">{formErrors.licenseNumber}</p>}
            </div>
            <div className="form-group">
              <label htmlFor="yearsOfExperience">Years of Experience</label>
              <input type="number" name="yearsOfExperience" id="yearsOfExperience" className={`form-input ${formErrors.yearsOfExperience ? 'input-error' : ''}`} value={formData.yearsOfExperience} onChange={handleChange} min="0" />
              {formErrors.yearsOfExperience && <p className="error-text">{formErrors.yearsOfExperience}</p>}
            </div>
          </div>
          <div className="form-group mt-4">
            <label htmlFor="consultationFee">Consultation Fee (USD)</label>
            <input type="number" name="consultationFee" id="consultationFee" className={`form-input ${formErrors.consultationFee ? 'input-error' : ''}`} value={formData.consultationFee} onChange={handleChange} min="0" step="0.01" />
            {formErrors.consultationFee && <p className="error-text">{formErrors.consultationFee}</p>}
          </div>
          <div className="form-group mt-4">
            <label htmlFor="qualifications">Qualifications</label>
            <textarea name="qualifications" id="qualifications" rows="2" className="form-input" value={formData.qualifications} onChange={handleChange}></textarea>
          </div>
        </section>

        <div className="mt-8 flex space-x-4">
          <button type="submit" className="btn btn-primary flex-grow py-3" disabled={isLoading || isFetching}>
            {isLoading ? 'Updating Doctor...' : 'Update Doctor'}
          </button>
          <Link to="/admin/doctors" className="btn bg-gray-300 hover:bg-gray-400 text-gray-800 flex-grow text-center py-3">
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
};
export default EditDoctorPage;