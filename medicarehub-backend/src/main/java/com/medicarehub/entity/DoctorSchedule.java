package com.medicarehub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull; // Ensure this is imported if you add validation annotations directly here
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDate;

@Entity
@Table(name = "doctor_schedules", indexes = { // Adding indexes can improve query performance
        @Index(name = "idx_doctorschedule_doctor_date", columnList = "doctor_id, specificDate"),
        @Index(name = "idx_doctorschedule_doctor_day", columnList = "doctor_id, dayOfWeek")
})
@Getter
@Setter
@NoArgsConstructor
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY is good here, only load doctor if needed
    @JoinColumn(name = "doctor_id", nullable = false)
    // @NotNull // Validation is good, but Spring Data JPA might handle null checks
    private Doctor doctor;

    // For recurring weekly availability
    @Enumerated(EnumType.STRING)
    @Column(length = 10) // e.g., MONDAY, TUESDAY
    private DayOfWeek dayOfWeek; // Null if this is a specific date override/entry

    // For specific date overrides/availability or one-time schedules
    private LocalDate specificDate; // Null if this is a recurring weekly schedule entry

    @NotNull(message = "Start time cannot be null") // Example of direct entity validation
    private LocalTime startTime; // e.g., 09:00

    @NotNull(message = "End time cannot be null")
    private LocalTime endTime;   // e.g., 17:00 (Appointments should end before or at this time)

    @NotNull(message = "Slot duration cannot be null")
    // @Min(value = 5, message = "Slot duration must be at least 5 minutes") // If using @Valid on entity
    private Integer slotDurationMinutes; // e.g., 15, 30, 60 minutes

    @Column(nullable = false) // Ensures DB constraint
    private boolean isAvailable = true; // True for available working time, false for explicitly booked off/unavailable override

    @Column(columnDefinition = "TEXT")
    private String notes; // e.g., "Lunch Break", "Unavailable due to conference", "Regular Clinic Hours"

    // Constructor for recurring weekly schedule
    public DoctorSchedule(Doctor doctor, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, Integer slotDurationMinutes, String notes) {
        this.doctor = doctor;
        this.dayOfWeek = dayOfWeek;
        this.specificDate = null; // Explicitly null for recurring
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes;
        this.isAvailable = true; // Recurring entries are typically for availability
        this.notes = notes;
    }

    // Constructor for specific date entry (availability or unavailability override)
    public DoctorSchedule(Doctor doctor, LocalDate specificDate, LocalTime startTime, LocalTime endTime, Integer slotDurationMinutes, boolean isAvailable, String notes) {
        this.doctor = doctor;
        this.dayOfWeek = null; // Explicitly null for specific date
        this.specificDate = specificDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes;
        this.isAvailable = isAvailable;
        this.notes = notes;
    }

    // Consider adding a validation constraint at the class level (using a custom validator)
    // to ensure that either dayOfWeek OR specificDate is set, but not both,
    // or that if specificDate is set, dayOfWeek is null, and vice-versa.
    // For example:
    // @AssertTrue(message = "Either dayOfWeek or specificDate must be set, but not both if they signify different types of schedule entries.")
    // public boolean isValidScheduleType() {
    //     return (dayOfWeek != null && specificDate == null) || (dayOfWeek == null && specificDate != null);
    // }
    // However, this simple AssertTrue might be too restrictive if you want specificDate to override a dayOfWeek for a portion of time.
    // The logic in DoctorScheduleService currently handles precedence.
}