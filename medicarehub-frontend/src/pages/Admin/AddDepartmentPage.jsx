// src/pages/Admin/AddDepartmentPage.jsx
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import departmentService from '../../services/departmentService.js';
import { toast } from 'react-toastify';
import Spinner from '../../components/common/Spinner.jsx';

const AddDepartmentPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    description: '',
  });
  const [formErrors, setFormErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (formErrors[name]) {
      setFormErrors(prev => ({ ...prev, [name]: null }));
    }
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.name.trim()) errors.name = "Department name is required.";
    else if (formData.name.length < 2 || formData.name.length > 100) errors.name = "Name must be 2-100 characters.";
    if (formData.description.length > 500) errors.description = "Description max 500 characters.";

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
      await departmentService.createDepartment(formData);
      toast.success("Department created successfully! Redirecting...");
      setTimeout(() => navigate('/admin/departments'), 1500);
    } catch (err) {
      console.error("Failed to create department:", err);
      const errMsg = err.response?.data?.message || err.message || "Could not create department.";
      toast.error(errMsg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="form-container max-w-lg"> {/* Adjusted max-width */}
      <h2 className="form-title">Add New Department</h2>
      <form onSubmit={handleSubmit} noValidate>
        <div className="form-group">
          <label htmlFor="name">Department Name*</label>
          <input
            type="text"
            name="name"
            id="name"
            className={`form-input ${formErrors.name ? 'input-error' : ''}`}
            value={formData.name}
            onChange={handleChange}
            required
            disabled={isLoading}
          />
          {formErrors.name && <p className="error-text">{formErrors.name}</p>}
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            name="description"
            id="description"
            rows="4"
            className={`form-input ${formErrors.description ? 'input-error' : ''}`}
            value={formData.description}
            onChange={handleChange}
            disabled={isLoading}
          ></textarea>
          {formErrors.description && <p className="error-text">{formErrors.description}</p>}
        </div>

        <div className="mt-6 flex space-x-4">
          <button type="submit" className="btn btn-primary flex-grow py-2.5" disabled={isLoading}>
            {isLoading ? <Spinner size="sm" /> : 'Create Department'}
          </button>
          <Link to="/admin/departments" className="btn btn-cancel flex-grow text-center py-2.5">
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
};

export default AddDepartmentPage;