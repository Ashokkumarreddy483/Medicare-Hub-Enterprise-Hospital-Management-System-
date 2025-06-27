package com.medicarehub.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorScheduleResponseDto {
    private Long id;
    private Long doctorId;
    private String doctorName; // Denormalized for display

    private DayOfWeek dayOfWeek;
    private LocalDate specificDate;
    private String startTime; // String representation "HH:mm"
    private String endTime;   // String representation "HH:mm"
    private Integer slotDurationMinutes;
    private boolean isAvailable;
    private String notes;
}