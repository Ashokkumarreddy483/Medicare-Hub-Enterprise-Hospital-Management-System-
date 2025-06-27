package com.medicarehub.dto;

import com.medicarehub.entity.AppointmentStatus; // Assuming AppointmentStatus is an enum
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDto {

    private Long id;

    // Patient Details
    private Long patientId;
    private String patientFirstName;
    private String patientLastName;
    private String patientUniqueId; // Hospital's unique ID for the patient

    // Doctor Details
    private Long doctorId;
    private String doctorFirstName;
    private String doctorLastName;
    private String doctorSpecialization;
    private String doctorDepartmentName;

    // Appointment Details
    private String appointmentDate;     // "YYYY-MM-DD"
    private String appointmentTime;     // "HH:mm"
    private Integer durationMinutes;
    private AppointmentStatus status;
    private String reasonForVisit;
    private String notesByPatient;
    private String notesByDoctorOrStaff;

    private String createdAt;           // ISO DateTime string
    private String updatedAt;           // ISO DateTime string
}