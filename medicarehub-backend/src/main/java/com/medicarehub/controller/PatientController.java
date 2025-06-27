package com.medicarehub.controller;

import com.medicarehub.dto.PatientRequestDto;
import com.medicarehub.dto.PatientResponseDto;
import com.medicarehub.service.PatientService;
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
@RequestMapping("/api/patients")
// Add @CrossOrigin here if not handled globally by WebConfig
public class PatientController {

    @Autowired
    private PatientService patientService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponseDto> createPatient(@Valid @RequestBody PatientRequestDto patientRequestDto) {
        PatientResponseDto createdPatient = patientService.createPatient(patientRequestDto);
        return new ResponseEntity<>(createdPatient, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'NURSE') or (hasRole('PATIENT') and @patientSecurityService.isSelf(authentication, #id))")
    // For patient self-access, you'd need a @PatientSecurityService bean to check ownership.
    // For now, keeping it simpler for staff roles.
    // @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<PatientResponseDto> getPatientById(@PathVariable Long id) {
        // TODO: For PATIENT role, check if they are requesting their own record.
        // This requires more complex security expression or service method.
        // For simplicity, let's assume staff can access, and patient access is handled via a dedicated "/api/me/profile" endpoint.
        // If 'id' here is the Patient's own DB ID.
        // If it was patientUniqueId: getPatientByPatientUniqueId(String patientUniqueId)
        PatientResponseDto patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/unique/{patientUniqueId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<PatientResponseDto> getPatientByPatientUniqueId(@PathVariable String patientUniqueId) {
        PatientResponseDto patient = patientService.getPatientByPatientUniqueId(patientUniqueId);
        return ResponseEntity.ok(patient);
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<Page<PatientResponseDto>> getAllPatients(
            @PageableDefault(size = 10, sort = "user.lastName") Pageable pageable) {
        Page<PatientResponseDto> patients = patientService.getAllPatients(pageable);
        return ResponseEntity.ok(patients);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponseDto> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientRequestDto patientRequestDto) {
        // Note: The PatientRequestDto for update might be different (e.g., password not required/allowed)
        // For now, reusing the same DTO.
        PatientResponseDto updatedPatient = patientService.updatePatient(id, patientRequestDto);
        return ResponseEntity.ok(updatedPatient);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only Admin can hard delete
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    // TODO: Add endpoint for patients to view/update their own profile
    // e.g., @GetMapping("/me") for logged-in patient
}