package com.medicarehub.controller;

import com.medicarehub.dto.DepartmentDto;
import com.medicarehub.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
// Add @CrossOrigin here or ensure global CORS configuration in WebConfig covers this path.
// If WebConfig has addMapping("/api/**"), this controller is covered.
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    // Create a new department
    // Accessible only by ADMIN
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentDto> createDepartment(@Valid @RequestBody DepartmentDto departmentDto) {
        DepartmentDto createdDepartment = departmentService.createDepartment(departmentDto);
        return new ResponseEntity<>(createdDepartment, HttpStatus.CREATED);
    }

    // Get a department by its ID
    // Accessible by ADMIN, DOCTOR, RECEPTIONIST, NURSE (for selection or info)
    // Patients might get a list of departments via a different, more restricted endpoint if needed.
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE')")
    public ResponseEntity<DepartmentDto> getDepartmentById(@PathVariable Long id) {
        DepartmentDto departmentDto = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(departmentDto);
    }

    // Get all departments
    // Accessible by ADMIN, DOCTOR, RECEPTIONIST, NURSE (e.g., for dropdowns in forms)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE')")
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        List<DepartmentDto> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    // Update an existing department
    // Accessible only by ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentDto> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentDto departmentDto) {
        DepartmentDto updatedDepartment = departmentService.updateDepartment(id, departmentDto);
        return ResponseEntity.ok(updatedDepartment);
    }

    // Delete a department
    // Accessible only by ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build(); // 204 No Content on successful deletion
    }
}