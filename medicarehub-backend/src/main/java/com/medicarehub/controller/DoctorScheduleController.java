package com.medicarehub.controller;

import com.medicarehub.dto.AvailabilitySlotDto;
import com.medicarehub.dto.DoctorScheduleRequestDto;
import com.medicarehub.dto.DoctorScheduleResponseDto;
import com.medicarehub.service.DoctorScheduleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
// Base path /api, specific paths will be defined below
public class DoctorScheduleController {

    @Autowired
    private DoctorScheduleService doctorScheduleService;

    // --- Endpoints for Managing a Doctor's Own Schedule (if a Doctor is logged in) ---
    // These would require checking if the authenticated principal is the doctorId in the path.
    // For simplicity, we'll assume Admin or the Doctor themselves can manage via a doctorId path param.
    // A more robust way for doctors to manage their own: @PreAuthorize("hasRole('DOCTOR') and @doctorSecurityService.isSelf(authentication, #doctorId)")
    // or a dedicated endpoint like /api/me/doctor/schedule

    // Add a new schedule entry for a specific doctor
    @PostMapping("/doctors/{doctorId}/schedules")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @customSecurityService.isDoctorSelf(authentication, #doctorId))")
    // Note: @customSecurityService.isDoctorSelf would be a custom bean method for authorization
    public ResponseEntity<DoctorScheduleResponseDto> addDoctorSchedule(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorScheduleRequestDto scheduleRequestDto) {
        DoctorScheduleResponseDto createdSchedule = doctorScheduleService.addSchedule(doctorId, scheduleRequestDto);
        return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
    }

    // Get all schedule entries for a specific doctor
    @GetMapping("/doctors/{doctorId}/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST') or (hasRole('DOCTOR') and @customSecurityService.isDoctorSelf(authentication, #doctorId))")
    public ResponseEntity<List<DoctorScheduleResponseDto>> getSchedulesForDoctor(@PathVariable Long doctorId) {
        List<DoctorScheduleResponseDto> schedules = doctorScheduleService.getSchedulesForDoctor(doctorId);
        return ResponseEntity.ok(schedules);
    }

    // Update an existing schedule entry by its ID
    @PutMapping("/schedules/{scheduleId}") // General endpoint, needs careful authorization
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @customSecurityService.isOwnerOfSchedule(authentication, #scheduleId))")
    // @customSecurityService.isOwnerOfSchedule would check if the scheduleId belongs to the authenticated doctor
    public ResponseEntity<DoctorScheduleResponseDto> updateDoctorSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody DoctorScheduleRequestDto scheduleRequestDto) {
        DoctorScheduleResponseDto updatedSchedule = doctorScheduleService.updateSchedule(scheduleId, scheduleRequestDto);
        return ResponseEntity.ok(updatedSchedule);
    }

    // Delete a schedule entry by its ID
    @DeleteMapping("/schedules/{scheduleId}") // General endpoint, needs careful authorization
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @customSecurityService.isOwnerOfSchedule(authentication, #scheduleId))")
    public ResponseEntity<Void> deleteDoctorSchedule(@PathVariable Long scheduleId) {
        doctorScheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }


    // --- Endpoint for Fetching Doctor Availability (e.g., for Patients/Receptionists) ---

    @GetMapping("/doctors/{doctorId}/availability")
    @PreAuthorize("isAuthenticated()") // Any logged-in user can check availability (Patient, Receptionist, etc.)
    public ResponseEntity<List<AvailabilitySlotDto>> getDoctorAvailability(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // @DateTimeFormat ensures the date string from query param is parsed to LocalDate
        List<AvailabilitySlotDto> availableSlots = doctorScheduleService.getDoctorAvailability(doctorId, date);
        return ResponseEntity.ok(availableSlots);
    }
}