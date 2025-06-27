// src/pages/Admin/AddDoctorPage.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import doctorService from '../../services/doctorService';
import { toast } from 'react-toastify';

const AddDoctorPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '', email: '', password: '', firstName: '', lastName: '', phoneNumber: '',
    departmentId: '', specialization: '', licenseNumber: '',
    yearsOfExperience: 0, consultationFee: '', qualifications: '',
  });
  const [departments, setDepartments] = useState([]);
  const [formErrors, setFormErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [isDeptLoading, setIsDeptLoading] = useState(true);

  useEffect(() => {
    const fetchDepartments = async () => {
      setIsDeptLoading(true);
      try {
        const depts = await doctorService.getAllDepartments(); // Using the one in doctorService
        setDepartments(depts || []); // Ensure depts is an array
      } catch (error) {
        console.error("Failed to fetch departments:", error);
        toast.error("Could not load departments for selection.");
        setDepartments([]); // Set to empty array on error
      } finally {
        setIsDeptLoading(false);
      }
    };
    fetchDepartments();
  }, []);

  const handleChange = (e) => {
    const { name, value, type } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'number' ? (value === '' ? '' : Number(value)) : value
    });
    if (formErrors[name]) setFormErrors({ ...formErrors, [name]: null });
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.username.trim()) errors.username = "Username is required.";
    else if (formData.username.length < 3) errors.username = "Username > 3 chars.";
    if (!formData.email.trim()) errors.email = "Email is required.";
    else if (!/\S+@\S+\.\S+/.test(formData.email)) errors.email = "Email is invalid.";
    if (!formData.password) errors.password = "Password is required.";
    else if (formData.password.length < 6) errors.password = "Password > 6 chars.";
    if (!formData.firstName.trim()) errors.firstName = "First name is required.";
    if (!formData.lastName.trim()) errors.lastName = "Last name is required.";
    if (!formData.departmentId) errors.departmentId = "Department is required.";
    if (!formData.specialization.trim()) errors.specialization = "Specialization is required.";
    if (!formData.licenseNumber.trim()) errors.licenseNumber = "License number is required.";
    // Add more specific validations for numbers, etc.
    if (formData.yearsOfExperience < 0) errors.yearsOfExperience = "Experience can't be negative.";
    if (formData.consultationFee && parseFloat(formData.consultationFee) < 0) errors.consultationFee = "Fee can't be negative.";

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
    try {
      // Ensure numbers are numbers if empty strings were allowed in state
      const payload = {
        ...formData,
        yearsOfExperience: formData.yearsOfExperience === '' ? null : Number(formData.yearsOfExperience),
        consultationFee: formData.consultationFee === '' ? null : parseFloat(formData.consultationFee),
        departmentId: Number(formData.departmentId)
      };
      await doctorService.createDoctor(payload);
      toast.success("Doctor created successfully! Redirecting...");
      setTimeout(() => navigate('/admin/doctors'), 2000);
    } catch (err) {
      console.error("Failed to create doctor:", err);
      const apiError = err.response?.data;
      let errorMessage = "Could not create doctor.";
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

  return (
    <div className="form-container max-w-3xl">
      <h2 className="form-title">Add New Doctor</h2>
      <form onSubmit={handleSubmit} noValidate>
        {/* User Account Details */}
        <section className="form-section">
          <h3 className="form-section-title">User Account</h3>
          <div className="grid md:grid-cols-2 gap-x-6 gap-y-4">
            <div className="form-group">
              <label htmlFor="username">Username*</label>
              <input type="text" name="username" id="username" className={`form-input ${formErrors.username ? 'input-error' : ''}`} value={formData.username} onChange={handleChange} required />
              {formErrors.username && <p className="error-text">{formErrors.username}</p>}
            </div>
            <div className="form-group">
              <label htmlFor="email">Email*</label>
              <input type="email" name="email" id="email" className={`form-input ${formErrors.email ? 'input-error' : ''}`} value={formData.email} onChange={handleChange} required />
              {formErrors.email && <p className="error-text">{formErrors.email}</p>}
            </div>
            <div className="form-group md:col-span-2"> {/* Password full width on medium screens */}
              <label htmlFor="password">Password*</label>
              <input type="password" name="password" id="password" className={`form-input ${formErrors.password ? 'input-error' : ''}`} value={formData.password} onChange={handleChange} required />
              {formErrors.password && <p className="error-text">{formErrors.password}</p>}
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
              <select name="departmentId" id="departmentId" className={`form-input ${formErrors.departmentId ? 'input-error' : ''}`} value={formData.departmentId} onChange={handleChange} required disabled={isDeptLoading}>
                <option value="">{isDeptLoading ? "Loading..." : "Select Department"}</option>
                {departments.map(dept => (
                  <option key={dept.id} value={dept.id}>{dept.name}</option>
                ))}
              </select>
              {formErrors.departmentId && <p className="error-text">{formErrors.departmentId}</p>}
            </div>
            <div className="form-group">
              <label htmlFor="specialization">Specialization*</label>
              <input type="text" name="specialization" id="specialization" className={`form-input ${formErrors.specialization ? 'input-error' : ''}`} value={formData.specialization} onChange={handleChange} required />
              {formErrors.specialization && <p className="error-text">{formErrors.specialization}</p>}
            </div>
            <div className="form-group">
              <label htmlFor="licenseNumber">License Number*</label>
              <input type="text" name="licenseNumber" id="licenseNumber" className={`form-input ${formErrors.licenseNumber ? 'input-error' : ''}`} value={formData.licenseNumber} onChange={handleChange} required />
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
            <label htmlFor="qualifications">Qualifications (e.g., MBBS, MD)</label>
            <textarea name="qualifications" id="qualifications" rows="2" className="form-input" value={formData.qualifications} onChange={handleChange}></textarea>
          </div>
        </section>

        <div className="mt-8 flex space-x-4">
          <button type="submit" className="btn btn-primary flex-grow py-3" disabled={isLoading || isDeptLoading}>
            {isLoading ? 'Saving Doctor...' : 'Create Doctor'}
          </button>
          <Link to="/admin/doctors" className="btn bg-gray-300 hover:bg-gray-400 text-gray-800 flex-grow text-center py-3">
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
};
export default AddDoctorPage;