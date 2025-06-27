package com.medicarehub.service;

import com.medicarehub.dto.AvailabilitySlotDto;
import com.medicarehub.dto.DoctorScheduleRequestDto;
import com.medicarehub.dto.DoctorScheduleResponseDto;
import com.medicarehub.entity.Appointment;
import com.medicarehub.entity.Doctor;
import com.medicarehub.entity.DoctorSchedule;
import com.medicarehub.exception.ResourceNotFoundException;
import com.medicarehub.repository.AppointmentRepository;
import com.medicarehub.repository.DoctorRepository;
import com.medicarehub.repository.DoctorScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorScheduleService {

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository; // To check for booked slots

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // --- Mapper ---
    private DoctorScheduleResponseDto mapToDto(DoctorSchedule schedule) {
        if (schedule == null) return null;
        return new DoctorScheduleResponseDto(
                schedule.getId(),
                schedule.getDoctor().getId(),
                schedule.getDoctor().getUser().getFirstName() + " " + schedule.getDoctor().getUser().getLastName(),
                schedule.getDayOfWeek(),
                schedule.getSpecificDate(),
                schedule.getStartTime().format(TIME_FORMATTER),
                schedule.getEndTime().format(TIME_FORMATTER),
                schedule.getSlotDurationMinutes(),
                schedule.isAvailable(),
                schedule.getNotes()
        );
    }

    // --- CRUD for Doctor Schedules (typically by Admin or Doctor) ---

    @Transactional
    public DoctorScheduleResponseDto addSchedule(Long doctorId, DoctorScheduleRequestDto requestDto) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        // Basic validation: startTime must be before endTime
        if (requestDto.getStartTime().isAfter(requestDto.getEndTime()) || requestDto.getStartTime().equals(requestDto.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
        // Ensure either dayOfWeek or specificDate is provided, but not neither (can be handled by DTO validation too)
        if (requestDto.getDayOfWeek() == null && requestDto.getSpecificDate() == null) {
            throw new IllegalArgumentException("Either dayOfWeek or specificDate must be provided for a schedule.");
        }
        // Potentially disallow both dayOfWeek and specificDate set simultaneously, or define precedence
        if (requestDto.getDayOfWeek() != null && requestDto.getSpecificDate() != null) {
            // For simplicity, let's assume specificDate takes precedence, so nullify dayOfWeek if specificDate is present
            // Or throw an error: throw new IllegalArgumentException("Cannot set both dayOfWeek and specificDate.");
            requestDto.setDayOfWeek(null);
        }


        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(requestDto.getDayOfWeek());
        schedule.setSpecificDate(requestDto.getSpecificDate());
        schedule.setStartTime(requestDto.getStartTime());
        schedule.setEndTime(requestDto.getEndTime());
        schedule.setSlotDurationMinutes(requestDto.getSlotDurationMinutes());
        schedule.setAvailable(requestDto.isAvailable());
        schedule.setNotes(requestDto.getNotes());

        // TODO: Add logic to check for overlapping schedule entries for the same doctor
        // This is crucial to avoid defining conflicting schedules (e.g., Mon 9-5 and Mon 10-6)
        // For now, we'll save directly.

        DoctorSchedule savedSchedule = doctorScheduleRepository.save(schedule);
        return mapToDto(savedSchedule);
    }

    public List<DoctorScheduleResponseDto> getSchedulesForDoctor(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with ID: " + doctorId);
        }
        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorId(doctorId);
        return schedules.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public DoctorScheduleResponseDto updateSchedule(Long scheduleId, DoctorScheduleRequestDto requestDto) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor schedule not found with ID: " + scheduleId));

        if (requestDto.getStartTime().isAfter(requestDto.getEndTime()) || requestDto.getStartTime().equals(requestDto.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
        if (requestDto.getDayOfWeek() == null && requestDto.getSpecificDate() == null) {
            throw new IllegalArgumentException("Either dayOfWeek or specificDate must be provided.");
        }
        if (requestDto.getDayOfWeek() != null && requestDto.getSpecificDate() != null) {
            requestDto.setDayOfWeek(null);
        }

        schedule.setDayOfWeek(requestDto.getDayOfWeek());
        schedule.setSpecificDate(requestDto.getSpecificDate());
        schedule.setStartTime(requestDto.getStartTime());
        schedule.setEndTime(requestDto.getEndTime());
        schedule.setSlotDurationMinutes(requestDto.getSlotDurationMinutes());
        schedule.setAvailable(requestDto.isAvailable());
        schedule.setNotes(requestDto.getNotes());
        // TODO: Add overlap check for update as well

        DoctorSchedule updatedSchedule = doctorScheduleRepository.save(schedule);
        return mapToDto(updatedSchedule);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        if (!doctorScheduleRepository.existsById(scheduleId)) {
            throw new ResourceNotFoundException("Doctor schedule not found with ID: " + scheduleId);
        }
        doctorScheduleRepository.deleteById(scheduleId);
    }


    // --- Logic for Getting Doctor Availability ---

    public List<AvailabilitySlotDto> getDoctorAvailability(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<AvailabilitySlotDto> availableSlots = new ArrayList<>();

        // 1. Get all relevant schedule entries for the doctor (general for the day, specific for the date)
        // Specific date entries take precedence.
        List<DoctorSchedule> specificDateSchedules = doctorScheduleRepository.findByDoctorAndSpecificDate(doctor, date);
        List<DoctorSchedule> generalDaySchedules;

        if (!specificDateSchedules.isEmpty()) {
            // If specific date schedules exist, these define the entire availability for that day.
            // We only consider the ones marked as available.
            generalDaySchedules = specificDateSchedules.stream()
                    .filter(DoctorSchedule::isAvailable)
                    .collect(Collectors.toList());
        } else {
            // No specific overrides, use general weekly schedule for that day of the week
            generalDaySchedules = doctorScheduleRepository.findByDoctorAndDayOfWeek(doctor, dayOfWeek)
                    .stream()
                    .filter(DoctorSchedule::isAvailable) // Should always be true for general, but good check
                    .collect(Collectors.toList());
        }

        // Sort by start time to process them in order
        generalDaySchedules.sort(Comparator.comparing(DoctorSchedule::getStartTime));

        // 2. Get already booked appointments for the doctor on that date
        List<Appointment> bookedAppointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date);

        // 3. Generate available slots
        for (DoctorSchedule schedule : generalDaySchedules) {
            LocalTime slotStartTime = schedule.getStartTime();
            LocalTime scheduleEndTime = schedule.getEndTime();
            int slotDuration = schedule.getSlotDurationMinutes();

            while (slotStartTime.plusMinutes(slotDuration).isBefore(scheduleEndTime) ||
                    slotStartTime.plusMinutes(slotDuration).equals(scheduleEndTime)) {

                LocalTime slotEndTime = slotStartTime.plusMinutes(slotDuration);

                // Check if this potential slot overlaps with any booked appointments
                // Also check if it's in the future (if the date is today)
                boolean isSlotBooked = isSlotOverlappingWithBooked(slotStartTime, slotEndTime, bookedAppointments);
                boolean isSlotInFuture = !date.isEqual(LocalDate.now()) || slotStartTime.isAfter(LocalTime.now());


                if (!isSlotBooked && isSlotInFuture) {
                    availableSlots.add(new AvailabilitySlotDto(
                            slotStartTime.format(TIME_FORMATTER),
                            slotEndTime.format(TIME_FORMATTER)
                    ));
                }
                slotStartTime = slotEndTime; // Move to the next potential slot
            }
        }
        return availableSlots;
    }

    private boolean isSlotOverlappingWithBooked(LocalTime slotStart, LocalTime slotEnd, List<Appointment> bookedAppointments) {
        for (Appointment booked : bookedAppointments) {
            // Skip cancelled appointments
            if (booked.getStatus() == com.medicarehub.entity.AppointmentStatus.CANCELLED_BY_PATIENT ||
                    booked.getStatus() == com.medicarehub.entity.AppointmentStatus.CANCELLED_BY_STAFF) {
                continue;
            }

            LocalTime bookedStart = booked.getAppointmentTime();
            LocalTime bookedEnd = bookedStart.plusMinutes(booked.getDurationMinutes());
            // Check for overlap: (StartA < EndB) and (EndA > StartB)
            if (slotStart.isBefore(bookedEnd) && slotEnd.isAfter(bookedStart)) {
                return true; // Slot overlaps with a booked appointment
            }
        }
        return false; // Slot is not booked
    }
}