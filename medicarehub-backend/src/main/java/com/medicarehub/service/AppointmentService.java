package com.medicarehub.service;

import com.medicarehub.dto.AppointmentRequestDto;
import com.medicarehub.dto.AppointmentResponseDto;
import com.medicarehub.dto.AppointmentStatusUpdateDto;
import com.medicarehub.dto.AvailabilitySlotDto; // <--- IMPORT THIS
import com.medicarehub.entity.*;
import com.medicarehub.exception.BadRequestException;
import com.medicarehub.exception.ForbiddenAccessException;
import com.medicarehub.exception.ResourceNotFoundException;
import com.medicarehub.repository.AppointmentRepository;
import com.medicarehub.repository.DoctorRepository;
import com.medicarehub.repository.PatientRepository;
import com.medicarehub.repository.DoctorScheduleRepository;
import com.medicarehub.repository.UserRepository; // Import UserRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder; // Not directly used here for now
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository; // Added for direct user lookup

    @Autowired
    private DoctorScheduleService doctorScheduleService;

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;


    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME; // Use consistent formatter


    private AppointmentResponseDto mapToAppointmentResponseDto(Appointment appointment) {
        if (appointment == null) return null;

        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setId(appointment.getId());

        Patient patient = appointment.getPatient();
        if (patient != null && patient.getUser() != null) {
            dto.setPatientId(patient.getId());
            dto.setPatientFirstName(patient.getUser().getFirstName());
            dto.setPatientLastName(patient.getUser().getLastName());
            dto.setPatientUniqueId(patient.getPatientUniqueId());
        }

        Doctor doctor = appointment.getDoctor();
        if (doctor != null && doctor.getUser() != null && doctor.getDepartment() != null) {
            dto.setDoctorId(doctor.getId());
            dto.setDoctorFirstName(doctor.getUser().getFirstName());
            dto.setDoctorLastName(doctor.getUser().getLastName());
            dto.setDoctorSpecialization(doctor.getSpecialization());
            dto.setDoctorDepartmentName(doctor.getDepartment().getName());
        }

        dto.setAppointmentDate(appointment.getAppointmentDate().format(DATE_FORMATTER));
        dto.setAppointmentTime(appointment.getAppointmentTime().format(TIME_FORMATTER));
        dto.setDurationMinutes(appointment.getDurationMinutes());
        dto.setStatus(appointment.getStatus());
        dto.setReasonForVisit(appointment.getReasonForVisit());
        dto.setNotesByPatient(appointment.getNotesByPatient());
        dto.setNotesByDoctorOrStaff(appointment.getNotesByDoctorOrStaff());
        // Ensure createdAt and updatedAt are not null before formatting
        dto.setCreatedAt(appointment.getCreatedAt() != null ? appointment.getCreatedAt().format(DATETIME_FORMATTER) : null);
        dto.setUpdatedAt(appointment.getUpdatedAt() != null ? appointment.getUpdatedAt().format(DATETIME_FORMATTER) : null);

        return dto;
    }


    @Transactional
    public AppointmentResponseDto bookAppointment(AppointmentRequestDto requestDto, Authentication authentication) {
        Long patientIdToBookFor;
        String loggedInUsername = authentication.getName();

        // Find the User object for the logged-in principal
        User loggedInUser = userRepository.findByUsername(loggedInUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user '" + loggedInUsername + "' not found in system."));

        boolean isPatientBookingForSelf = authentication.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_PATIENT"));
        boolean isStaffBooking = authentication.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN") || ga.getAuthority().equals("ROLE_RECEPTIONIST"));

        if (isPatientBookingForSelf) {
            Patient patient = patientRepository.findByUserId(loggedInUser.getId()) // Use findByUserId
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for logged-in user: " + loggedInUsername));
            patientIdToBookFor = patient.getId();
            if (requestDto.getPatientId() != null && !requestDto.getPatientId().equals(patientIdToBookFor)) {
                throw new ForbiddenAccessException("Patients can only book appointments for themselves.");
            }
        } else if (isStaffBooking) {
            if (requestDto.getPatientId() == null) {
                throw new BadRequestException("Patient ID is required when staff is booking an appointment.");
            }
            patientIdToBookFor = requestDto.getPatientId();
        } else {
            throw new ForbiddenAccessException("User role not authorized to book appointments.");
        }

        Patient patient = patientRepository.findById(patientIdToBookFor)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientIdToBookFor));
        Doctor doctor = doctorRepository.findById(requestDto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + requestDto.getDoctorId()));

        // Determine slot duration from doctor's schedule
        // This is a simplification: assumes all slots for a doctor have same duration or picks the first one found.
        // A more robust system would link appointment booking to a *specific* DoctorSchedule entry or pass duration.
        Integer slotDuration = doctorScheduleRepository.findByDoctorId(doctor.getId())
                .stream()
                .filter(ds -> ds.getSpecificDate() == null || ds.getSpecificDate().equals(requestDto.getAppointmentDate())) // Prioritize specific date or general
                .map(DoctorSchedule::getSlotDurationMinutes)
                .findFirst()
                .orElse(30); // Default to 30 minutes if no specific schedule found (this is a fallback)


        // Check Doctor's Availability using DoctorScheduleService
        List<AvailabilitySlotDto> availableSlots = doctorScheduleService.getDoctorAvailability(doctor.getId(), requestDto.getAppointmentDate());

        final LocalTime requestedStartTime = requestDto.getAppointmentTime(); // final for use in lambda
        boolean slotIsValidAndAvailable = availableSlots.stream().anyMatch(slot -> {
            LocalTime slotStartTimeInList = LocalTime.parse(slot.getStartTime(), TIME_FORMATTER);
            return requestedStartTime.equals(slotStartTimeInList);
            // We could also check if requestedStartTime is within slotStartTimeInList and slotEndTimeInList
            // but getDoctorAvailability should only return exact start times of available slots.
        });

        if (!slotIsValidAndAvailable) {
            throw new BadRequestException("The selected time slot " + requestedStartTime.format(TIME_FORMATTER) +
                    " is not available for Dr. " + doctor.getUser().getLastName() +
                    " on " + requestDto.getAppointmentDate().format(DATE_FORMATTER) + ".");
        }

        // Check for patient's overlapping appointments (excluding cancelled)
        LocalTime requestedEndTime = requestDto.getAppointmentTime().plusMinutes(slotDuration);
        List<Appointment> patientOverlaps = appointmentRepository.findOverlappingAppointmentsForPatient(
                patient, requestDto.getAppointmentDate(), requestDto.getAppointmentTime(), requestedEndTime
        );
        if (!patientOverlaps.isEmpty()) {
            throw new BadRequestException("Patient " + patient.getUser().getFirstName() + " " + patient.getUser().getLastName() +
                    " already has an overlapping appointment at the selected time.");
        }

        // Also check for doctor's overlapping appointments again (as a final safeguard, though getDoctorAvailability should prevent this)
        List<Appointment> doctorOverlaps = appointmentRepository.findOverlappingAppointmentsForDoctor(
                doctor, requestDto.getAppointmentDate(), requestDto.getAppointmentTime(), requestedEndTime
        );
        if (!doctorOverlaps.isEmpty()) {
            throw new BadRequestException("Dr. " + doctor.getUser().getLastName() +
                    " already has an overlapping appointment at the selected time. Please try a different slot.");
        }


        Appointment appointment = new Appointment(
                patient,
                doctor,
                requestDto.getAppointmentDate(),
                requestDto.getAppointmentTime(),
                slotDuration,
                requestDto.getReasonForVisit()
        );
        appointment.setNotesByPatient(requestDto.getNotesByPatient());
        // Status defaults to SCHEDULED via @PrePersist or constructor in Appointment entity

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return mapToAppointmentResponseDto(savedAppointment);
    }

    public AppointmentResponseDto getAppointmentById(Long appointmentId, Authentication authentication) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        User loggedInUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found: " + authentication.getName()));

        boolean isAdminOrReceptionist = authentication.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN") || ga.getAuthority().equals("ROLE_RECEPTIONIST"));

        boolean isDoctorViewingOwn = authentication.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_DOCTOR")) &&
                appointment.getDoctor().getUser().getId().equals(loggedInUser.getId());

        boolean isPatientViewingOwn = authentication.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_PATIENT")) &&
                appointment.getPatient().getUser().getId().equals(loggedInUser.getId());

        if (!isAdminOrReceptionist && !isDoctorViewingOwn && !isPatientViewingOwn) {
            throw new ForbiddenAccessException("You are not authorized to view this appointment.");
        }

        return mapToAppointmentResponseDto(appointment);
    }

    public Page<AppointmentResponseDto> getAppointmentsForPatient(Long patientId, Pageable pageable, Authentication authentication) {
        User loggedInUser = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Patient patientToView = patientRepository.findById(patientId).orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));

        boolean isPatientSelf = authentication.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_PATIENT")) &&
                patientToView.getUser().getId().equals(loggedInUser.getId());
        boolean isAllowedStaff = authentication.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN") || ga.getAuthority().equals("ROLE_RECEPTIONIST") ||
                        ga.getAuthority().equals("ROLE_DOCTOR") || ga.getAuthority().equals("ROLE_NURSE")); // Doctor/Nurse might view patient's appts

        if (!isPatientSelf && !isAllowedStaff) {
            throw new ForbiddenAccessException("You are not authorized to view appointments for this patient.");
        }

        return appointmentRepository.findByPatientId(patientId, pageable).map(this::mapToAppointmentResponseDto);
    }

    public Page<AppointmentResponseDto> getAppointmentsForDoctor(Long doctorId, Pageable pageable, Authentication authentication) {
        User loggedInUser = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Doctor doctorToView = doctorRepository.findById(doctorId).orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        boolean isDoctorSelf = authentication.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_DOCTOR")) &&
                doctorToView.getUser().getId().equals(loggedInUser.getId());
        boolean isAllowedStaff = authentication.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN") || ga.getAuthority().equals("ROLE_RECEPTIONIST"));

        if (!isDoctorSelf && !isAllowedStaff) {
            throw new ForbiddenAccessException("You are not authorized to view appointments for this doctor.");
        }
        return appointmentRepository.findByDoctorId(doctorId, pageable).map(this::mapToAppointmentResponseDto);
    }

    public Page<AppointmentResponseDto> getAllAppointmentsFiltered(
            Optional<LocalDate> dateFilter,
            Optional<AppointmentStatus> statusFilter,
            Optional<String> patientNameSearch,
            Optional<String> doctorNameSearch,
            Pageable pageable) {
        return appointmentRepository.findAllFiltered(dateFilter, statusFilter, patientNameSearch, doctorNameSearch, pageable)
                .map(this::mapToAppointmentResponseDto);
    }


    @Transactional
    public AppointmentResponseDto updateAppointmentStatus(Long appointmentId, AppointmentStatusUpdateDto statusUpdateDto, Authentication authentication) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        User loggedInUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
        AppointmentStatus newStatus = statusUpdateDto.getNewStatus();

        boolean canUpdate = false;
        // Admin/Receptionist can change to most statuses (except perhaps re-opening a completed one without specific logic)
        if (authentication.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN") || ga.getAuthority().equals("ROLE_RECEPTIONIST"))) {
            canUpdate = true;
        } else if (authentication.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_DOCTOR")) &&
                appointment.getDoctor().getUser().getId().equals(loggedInUser.getId())) {
            if (newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.NO_SHOW || newStatus == AppointmentStatus.CANCELLED_BY_STAFF) {
                canUpdate = true;
            }
        } else if (authentication.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_PATIENT")) &&
                appointment.getPatient().getUser().getId().equals(loggedInUser.getId())) {
            if (newStatus == AppointmentStatus.CANCELLED_BY_PATIENT && appointment.getStatus() == AppointmentStatus.SCHEDULED) {
                canUpdate = true;
            }
        }

        if (!canUpdate) {
            throw new ForbiddenAccessException("You are not authorized to update this appointment to status: " + newStatus);
        }

        // Prevent changing status of already finalized appointments (completed/cancelled) unless it's a specific allowed transition
        if ((appointment.getStatus() == AppointmentStatus.COMPLETED ||
                appointment.getStatus() == AppointmentStatus.CANCELLED_BY_PATIENT ||
                appointment.getStatus() == AppointmentStatus.CANCELLED_BY_STAFF)
                && appointment.getStatus() != newStatus) { // Allow setting to same status idempotently
            throw new BadRequestException("Cannot change status of an already finalized (completed/cancelled) appointment to " + newStatus + ".");
        }
        if (appointment.getStatus() == AppointmentStatus.NO_SHOW && appointment.getStatus() != newStatus) {
            throw new BadRequestException("Cannot change status of a NO_SHOW appointment to " + newStatus + " (except by admin override not yet implemented).");
        }


        appointment.setStatus(newStatus);
        if (statusUpdateDto.getNotes() != null && !statusUpdateDto.getNotes().isBlank()) {
            String notePrefix = "[" + loggedInUser.getUsername() + " - " + newStatus + "]: ";
            String existingNotes = appointment.getNotesByDoctorOrStaff() == null ? "" : appointment.getNotesByDoctorOrStaff() + "\n";
            appointment.setNotesByDoctorOrStaff(existingNotes + notePrefix + statusUpdateDto.getNotes());
        } else if (newStatus == AppointmentStatus.CANCELLED_BY_PATIENT && statusUpdateDto.getCancellationReason() != null) {
            String notePrefix = "[Patient Cancelled]: ";
            String existingNotes = appointment.getNotesByDoctorOrStaff() == null ? "" : appointment.getNotesByDoctorOrStaff() + "\n";
            appointment.setNotesByDoctorOrStaff(existingNotes + notePrefix + statusUpdateDto.getCancellationReason());
        }

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        return mapToAppointmentResponseDto(updatedAppointment);
    }
}