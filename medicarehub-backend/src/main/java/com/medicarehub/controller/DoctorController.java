package com.medicarehub.controller;

import com.medicarehub.dto.DoctorRequestDto;
import com.medicarehub.dto.DoctorResponseDto;
import com.medicarehub.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
// Add @CrossOrigin here or ensure global CORS configuration in WebConfig covers this path.
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // Create a new doctor
    // Accessible only by ADMIN
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponseDto> createDoctor(@Valid @RequestBody DoctorRequestDto doctorRequestDto) {
        DoctorResponseDto createdDoctor = doctorService.createDoctor(doctorRequestDto);
        return new ResponseEntity<>(createdDoctor, HttpStatus.CREATED);
    }

    // Get a doctor by their Doctor ID
    // Accessible by various roles for informational purposes
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'NURSE', 'PATIENT')") // Patients might view doctor profiles
    public ResponseEntity<DoctorResponseDto> getDoctorById(@PathVariable Long id) {
        DoctorResponseDto doctorDto = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctorDto);
    }

    // Get all doctors with pagination and optional search
    // Publicly accessible list for patients to find doctors, or restricted for staff
    // For more fine-grained control, could have separate public/internal endpoints
    @GetMapping
    @PreAuthorize("permitAll()") // Example: Allow all to view list of doctors
    // Or @PreAuthorize("isAuthenticated()") to require login
    // Or @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'NURSE', 'PATIENT')")
    public ResponseEntity<Page<DoctorResponseDto>> getAllDoctors(
            @RequestParam(required = false) String searchTerm, // For search functionality
            @PageableDefault(size = 10, sort = "user.lastName") Pageable pageable) {
        Page<DoctorResponseDto> doctorsPage;
        if (searchTerm != null && !searchTerm.isBlank()) {
            doctorsPage = doctorService.searchDoctors(searchTerm, pageable);
        } else {
            doctorsPage = doctorService.getAllDoctors(pageable);
        }
        return ResponseEntity.ok(doctorsPage);
    }

    // Update an existing doctor
    // Accessible only by ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponseDto> updateDoctor(@PathVariable Long id, @Valid @RequestBody DoctorRequestDto doctorRequestDto) {
        // Note: doctorRequestDto for update might ideally be a different DTO (e.g., DoctorUpdateDto)
        // that doesn't require password or makes certain fields like username/email non-updatable.
        // The service layer currently handles ignoring password for update.
        DoctorResponseDto updatedDoctor = doctorService.updateDoctor(id, doctorRequestDto);
        return ResponseEntity.ok(updatedDoctor);
    }

    // Delete a doctor (could be soft delete - deactivating user)
    // Accessible only by ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id); // Service handles logic (hard delete or soft delete)
        return ResponseEntity.noContent().build();
    }

    // Potential future endpoints:
    // - Get doctors by department: /api/departments/{departmentId}/doctors
    // - Endpoints for doctors to manage their own profile or schedule (would require different @PreAuthorize logic)
}