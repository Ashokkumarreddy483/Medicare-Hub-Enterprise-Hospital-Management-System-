package com.medicarehub.dto;

import com.medicarehub.entity.AppointmentStatus; // Make sure this is correctly imported
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppointmentStatusUpdateDto {

    @NotNull(message = "New status is required.")
    private AppointmentStatus newStatus; // e.g., CANCELLED_BY_PATIENT, COMPLETED

    private String cancellationReason; // Optional, for when status is CANCELLED_...
    private String notes; // Optional, e.g., doctor's notes if status is COMPLETED
}