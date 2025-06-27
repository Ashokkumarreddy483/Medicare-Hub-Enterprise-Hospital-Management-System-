// src/services/departmentService.js
import apiClient from '../config/axiosConfig.js'; // Ensure .js if axiosConfig is .js

const getAllDepartments = async () => {
  const response = await apiClient.get('/departments');
  return response.data;
};

const getDepartmentById = async (departmentId) => {
  const response = await apiClient.get(`/departments/${departmentId}`);
  return response.data;
};

const createDepartment = async (departmentData) => {
  const response = await apiClient.post('/departments', departmentData);
  return response.data;
};

const updateDepartment = async (departmentId, departmentData) => {
  const response = await apiClient.put(`/departments/${departmentId}`, departmentData);
  return response.data;
};

const deleteDepartment = async (departmentId) => {
  await apiClient.delete(`/departments/${departmentId}`);
  // Delete often returns 204 No Content, so no specific data to return
};

const departmentService = {
  getAllDepartments,
  getDepartmentById,
  createDepartment,
  updateDepartment,
  deleteDepartment,
};

export default departmentService;