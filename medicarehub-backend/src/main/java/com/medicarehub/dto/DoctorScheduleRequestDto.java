package com.medicarehub.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class DoctorScheduleRequestDto {

    // doctorId will typically be a path variable in the controller, not part of this DTO body
    // private Long doctorId;

    // For recurring weekly schedules
    private DayOfWeek dayOfWeek; // e.g., MONDAY. If null, specificDate must be present.

    // For specific date overrides or one-time availability
    @FutureOrPresent(message = "Specific date must be today or in the future.")
    private LocalDate specificDate; // If null, dayOfWeek must be present.

    @NotNull(message = "Start time is required.")
    private LocalTime startTime; // Format: HH:mm, e.g., "09:00"

    @NotNull(message = "End time is required.")
    private LocalTime endTime;   // Format: HH:mm, e.g., "17:00"

    @NotNull(message = "Slot duration is required.")
    @Min(value = 5, message = "Slot duration must be at least 5 minutes.") // Min 5 minutes
    private Integer slotDurationMinutes;

    private boolean isAvailable = true; // Default to available when creating a slot

    private String notes; // Optional notes

    // Custom validation: either dayOfWeek or specificDate must be present, but not both ideally (or handle precedence)
    // This can be handled with a class-level constraint or in the service layer.
}