// src/services/doctorService.js
import apiClient from '../config/axiosConfig';

// Department DTO (simple version for dropdown)
/*
interface DepartmentMinDto {
  id: number;
  name: string;
}
*/

// Doctor DTOs (for clarity)
/*
interface DoctorRequestDto {
  username: string;
  email: string;
  password?: string; // Required for creation
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  departmentId: number;
  specialization: string;
  licenseNumber: string;
  yearsOfExperience?: number;
  consultationFee?: number; // string or number, backend handles BigDecimal
  qualifications?: string;
}

interface DoctorResponseDto {
  id: number;
  userId: number;
  // ... all fields from backend DoctorResponseDto
  departmentName: string;
}

interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number; // current page
  size: number;
}
*/

// --- Department Functions (often needed when managing doctors) ---
const getAllDepartments = async () => {
  const response = await apiClient.get('/departments');
  return response.data; // Expected: List<DepartmentDto>
};


// --- Doctor Functions ---
const createDoctor = async (doctorData) => {
  const response = await apiClient.post('/doctors', doctorData);
  return response.data; // Expected: DoctorResponseDto
};

const getAllDoctors = async (page = 0, size = 10, sort = 'user.lastName,asc', searchTerm = '') => {
  const params = { page, size, sort };
  if (searchTerm) {
    params.searchTerm = searchTerm;
  }
  const response = await apiClient.get('/doctors', { params });
  return response.data; // Expected: Page<DoctorResponseDto>
};

const getDoctorById = async (doctorId) => {
  const response = await apiClient.get(`/doctors/${doctorId}`);
  return response.data; // Expected: DoctorResponseDto
};

const updateDoctor = async (doctorId, doctorData) => {
  // Backend DoctorRequestDto might require all fields for validation, even if some are not truly updatable.
  // Service layer on backend should handle which fields actually get updated.
  const response = await apiClient.put(`/doctors/${doctorId}`, doctorData);
  return response.data; // Expected: DoctorResponseDto
};

const deleteDoctor = async (doctorId) => {
  const response = await apiClient.delete(`/doctors/${doctorId}`);
  return response.data; // Usually 204 No Content, so data might be undefined
};

const doctorService = {
  getAllDepartments, // Include this for convenience
  createDoctor,
  getAllDoctors,
  getDoctorById,
  updateDoctor,
  deleteDoctor,
};

export default doctorService;