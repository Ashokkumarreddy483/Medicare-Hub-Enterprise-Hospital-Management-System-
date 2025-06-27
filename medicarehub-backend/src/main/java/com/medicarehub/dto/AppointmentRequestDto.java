package com.medicarehub.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class AppointmentRequestDto {

    @NotNull(message = "Doctor ID is required.")
    private Long doctorId;

    // patientId might be inferred from the logged-in user if a patient is booking for themselves.
    // If a receptionist is booking, they would provide it.
    private Long patientId;

    @NotNull(message = "Appointment date is required.")
    @FutureOrPresent(message = "Appointment date must be today or in the future.")
    private LocalDate appointmentDate;

    @NotNull(message = "Appointment time is required.")
    private LocalTime appointmentTime; // This should be the start time of an available slot

    @NotBlank(message = "Reason for visit is required.")
    @Size(min = 5, max = 255, message = "Reason must be between 5 and 255 characters.")
    private String reasonForVisit;

    @Size(max = 500)
    private String notesByPatient; // Optional notes from the patient
}