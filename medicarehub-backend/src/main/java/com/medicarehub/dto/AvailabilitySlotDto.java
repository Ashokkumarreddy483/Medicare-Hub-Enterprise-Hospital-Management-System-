package com.medicarehub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilitySlotDto {
    private String startTime; // Format "HH:mm"
    private String endTime;   // Format "HH:mm"
    // Could add a boolean 'isBooked' if you want to show all potential slots and mark booked ones,
    // but typically the service layer would only return truly available ones.
}