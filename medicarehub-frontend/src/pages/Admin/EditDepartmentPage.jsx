// src/pages/Admin/EditDepartmentPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import departmentService from '../../services/departmentService.js';
import { toast } from 'react-toastify';
import Spinner from '../../components/common/Spinner.jsx';

const EditDepartmentPage = () => {
  const navigate = useNavigate();
  const { departmentId } = useParams();
  const [formData, setFormData] = useState({ name: '', description: '' });
  const [formErrors, setFormErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false); // For submit
  const [isFetching, setIsFetching] = useState(true); // For initial load
  const [fetchError, setFetchError] = useState(null);

  const fetchDepartment = useCallback(async () => {
    if (!departmentId) {
      toast.error("Department ID missing.");
      setIsFetching(false);
      return;
    }
    setIsFetching(true);
    setFetchError(null);
    try {
      const data = await departmentService.getDepartmentById(Number(departmentId));
      setFormData({ name: data.name || '', description: data.description || '' });
    } catch (err) {
      console.error("Failed to fetch department:", err);
      const errMsg = err.response?.data?.message || err.message || "Could not load department data.";
      setFetchError(errMsg);
      toast.error(errMsg);
    } finally {
      setIsFetching(false);
    }
  }, [departmentId]);

  useEffect(() => {
    fetchDepartment();
  }, [fetchDepartment]);

  const handleChange = (e) => { /* Same as AddDepartmentPage */
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (formErrors[name]) {
      setFormErrors(prev => ({ ...prev, [name]: null }));
    }
  };
  const validateForm = () => { /* Same as AddDepartmentPage */
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
      await departmentService.updateDepartment(Number(departmentId), formData);
      toast.success("Department updated successfully! Redirecting...");
      setTimeout(() => navigate('/admin/departments'), 1500);
    } catch (err) {
      console.error("Failed to update department:", err);
      const errMsg = err.response?.data?.message || err.message || "Could not update department.";
      toast.error(errMsg);
    } finally {
      setIsLoading(false);
    }
  };

  if (isFetching) {
    return <div className="text-center p-10"><Spinner size="lg" /><p className="ml-2">Loading department...</p></div>;
  }
  if (fetchError && !isFetching) {
    return <div className="error-message m-10">{fetchError} <Link to="/admin/departments" className="form-link ml-2">Go Back</Link></div>;
  }

  return (
    <div className="form-container max-w-lg">
      <h2 className="form-title">Edit Department: {formData.name || `ID: ${departmentId}`}</h2>
      <form onSubmit={handleSubmit} noValidate>
        <div className="form-group">
          <label htmlFor="name">Department Name*</label>
          <input type="text" name="name" id="name" className={`form-input ${formErrors.name ? 'input-error' : ''}`} value={formData.name} onChange={handleChange} required disabled={isLoading} />
          {formErrors.name && <p className="error-text">{formErrors.name}</p>}
        </div>
        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea name="description" id="description" rows="4" className={`form-input ${formErrors.description ? 'input-error' : ''}`} value={formData.description} onChange={handleChange} disabled={isLoading}></textarea>
          {formErrors.description && <p className="error-text">{formErrors.description}</p>}
        </div>
        <div className="mt-6 flex space-x-4">
          <button type="submit" className="btn btn-primary flex-grow py-2.5" disabled={isLoading || isFetching}>
            {isLoading ? <Spinner size="sm" /> : 'Update Department'}
          </button>
          <Link to="/admin/departments" className="btn btn-cancel flex-grow text-center py-2.5">
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
};

export default EditDepartmentPage;