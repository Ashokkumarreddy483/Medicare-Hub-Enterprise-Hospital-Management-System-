// src/services/patientService.js
import apiClient from '../config/axiosConfig'; // Your configured Axios instance

// DTO interfaces (for clarity, even in JS, good to think about shapes)
/*
interface PatientRequestDto {
  username: string;
  email: string;
  password?: string; // Only for creation
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  address?: string;
  dateOfBirth: string; // "YYYY-MM-DD"
  bloodGroup?: string;
  medicalHistorySummary?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  emergencyContactRelationship?: string;
  registrationDate: string; // "YYYY-MM-DD"
}

interface PatientResponseDto {
  id: number;
  patientUniqueId: string;
  userId: number;
  username: string;
  email: string;
  // ... other fields as defined in backend DTO
}

interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number; // current page number
  size: number;
}
*/

const createPatient = async (patientData) => {
  const response = await apiClient.post('/patients', patientData);
  return response.data; // Expected: PatientResponseDto
};

const getAllPatients = async (page = 0, size = 10, sort = 'user.lastName,asc') => {
  const response = await apiClient.get('/patients', {
    params: { page, size, sort }
  });
  return response.data; // Expected: Page<PatientResponseDto>
};

const getPatientById = async (patientId) => {
  const response = await apiClient.get(`/patients/${patientId}`);
  return response.data; // Expected: PatientResponseDto
};

const getPatientByUniqueId = async (patientUniqueId) => {
    const response = await apiClient.get(`/patients/unique/${patientUniqueId}`);
    return response.data; // Expected: PatientResponseDto
}

const updatePatient = async (patientId, patientData) => {
  // For update, typically password is not sent, or handled differently
  // Ensure your backend DTO and service handle this appropriately
  const { password, username, email, ...updateData } = patientData; // Example: exclude password, username, email
  const response = await apiClient.put(`/patients/${patientId}`, updateData);
  return response.data; // Expected: PatientResponseDto
};

const deletePatient = async (patientId) => {
  const response = await apiClient.delete(`/patients/${patientId}`);
  return response.data; // Expected: 204 No Content, so data might be undefined or an empty object
};

const patientService = {
  createPatient,
  getAllPatients,
  getPatientById,
  getPatientByUniqueId,
  updatePatient,
  deletePatient,
};

export default patientService;