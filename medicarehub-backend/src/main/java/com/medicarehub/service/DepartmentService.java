package com.medicarehub.service;

import com.medicarehub.dto.DepartmentDto;
import com.medicarehub.entity.Department;
import com.medicarehub.exception.ResourceAlreadyExistsException; // New Exception
import com.medicarehub.exception.ResourceNotFoundException;
import com.medicarehub.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    // --- Mapper Methods ---
    private DepartmentDto mapToDto(Department department) {
        if (department == null) {
            return null;
        }
        return new DepartmentDto(
                department.getId(),
                department.getName(),
                department.getDescription()
        );
    }

    private Department mapToEntity(DepartmentDto departmentDto) {
        if (departmentDto == null) {
            return null;
        }
        Department department = new Department();
        // ID is not set from DTO for creation, and typically not changed for update
        // department.setId(departmentDto.getId()); // Usually set by JPA or not changed
        department.setName(departmentDto.getName());
        department.setDescription(departmentDto.getDescription());
        return department;
    }

    // --- CRUD Methods ---

    @Transactional
    public DepartmentDto createDepartment(DepartmentDto departmentDto) {
        // Check if department with the same name already exists
        if (departmentRepository.existsByName(departmentDto.getName())) {
            throw new ResourceAlreadyExistsException("Department with name '" + departmentDto.getName() + "' already exists.");
        }

        Department department = mapToEntity(departmentDto);
        Department savedDepartment = departmentRepository.save(department);
        return mapToDto(savedDepartment);
    }

    public DepartmentDto getDepartmentById(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));
        return mapToDto(department);
    }

    public List<DepartmentDto> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        return departments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentDto updateDepartment(Long departmentId, DepartmentDto departmentDto) {
        Department existingDepartment = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));

        // Check if the new name is being changed to an already existing department name (excluding itself)
        if (departmentDto.getName() != null &&
                !existingDepartment.getName().equalsIgnoreCase(departmentDto.getName()) &&
                departmentRepository.existsByName(departmentDto.getName())) {
            throw new ResourceAlreadyExistsException("Another department with name '" + departmentDto.getName() + "' already exists.");
        }

        // Update fields from DTO
        if (departmentDto.getName() != null) {
            existingDepartment.setName(departmentDto.getName());
        }
        if (departmentDto.getDescription() != null) {
            existingDepartment.setDescription(departmentDto.getDescription());
        }
        // Add any other updatable fields

        Department updatedDepartment = departmentRepository.save(existingDepartment);
        return mapToDto(updatedDepartment);
    }

    @Transactional
    public void deleteDepartment(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with ID: " + departmentId);
        }
        // Consider implications: What if doctors are assigned to this department?
        // You might need to check for associated doctors and either prevent deletion,
        // reassign them, or nullify their department (if allowed).
        // For now, a simple delete.
        // If using bi-directional mapping with Doctor and CascadeType.ALL on department,
        // deleting department might cascade to doctors if not handled carefully.
        // Here, assuming uni-directional from Doctor to Department, or bi-directional without problematic cascades.
        departmentRepository.deleteById(departmentId);
    }
}