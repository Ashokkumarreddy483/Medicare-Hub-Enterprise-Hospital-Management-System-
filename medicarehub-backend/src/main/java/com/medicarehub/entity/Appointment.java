package com.medicarehub.entity;

// If AppointmentStatus is in com.medicarehub.enums, you'd add:
// import com.medicarehub.enums.AppointmentStatus;
// If it's in the same com.medicarehub.entity package, no explicit import is needed for it.

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointment_patient_date", columnList = "patient_id, appointmentDate"),
        @Index(name = "idx_appointment_doctor_date", columnList = "doctor_id, appointmentDate"),
        @Index(name = "idx_appointment_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @NotNull
    private Doctor doctor;

    @NotNull(message = "Appointment date is required.")
    @FutureOrPresent(message = "Appointment date must be today or in the future at the time of creation/update.")
    private LocalDate appointmentDate;

    @NotNull(message = "Appointment time is required.")
    private LocalTime appointmentTime; // Start time of the appointment

    @NotNull(message = "Appointment duration is required.")
    @Min(value = 5, message = "Duration must be at least 5 minutes.")
    private Integer durationMinutes; // Duration of the appointment in minutes

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    @NotNull(message = "Appointment status is required.")
    private AppointmentStatus status; // Now refers to the external enum

    @NotBlank(message = "Reason for visit is required.")
    @Size(min = 3, max = 255, message = "Reason must be between 3 and 255 characters.")
    private String reasonForVisit;

    @Column(columnDefinition = "TEXT")
    private String notesByPatient;

    @Column(columnDefinition = "TEXT")
    private String notesByDoctorOrStaff;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = AppointmentStatus.SCHEDULED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getReasonForVisit() {
        return reasonForVisit;
    }

    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }

    public String getNotesByPatient() {
        return notesByPatient;
    }

    public void setNotesByPatient(String notesByPatient) {
        this.notesByPatient = notesByPatient;
    }

    public String getNotesByDoctorOrStaff() {
        return notesByDoctorOrStaff;
    }

    public void setNotesByDoctorOrStaff(String notesByDoctorOrStaff) {
        this.notesByDoctorOrStaff = notesByDoctorOrStaff;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Appointment(Patient patient, Doctor doctor, LocalDate appointmentDate, LocalTime appointmentTime, Integer durationMinutes, String reasonForVisit) {
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.durationMinutes = durationMinutes;
        this.reasonForVisit = reasonForVisit;
        this.status = AppointmentStatus.SCHEDULED;
    }
}