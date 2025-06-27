package com.medicarehub.entity; // Or com.medicarehub.enums if you created that package

public enum AppointmentStatus {
    SCHEDULED,          // Appointment is booked and upcoming
    COMPLETED,          // Appointment has taken place
    CANCELLED_BY_PATIENT, // Patient cancelled the appointment
    CANCELLED_BY_STAFF,   // Doctor, Receptionist, or Admin cancelled
    NO_SHOW,            // Patient did not show up for the appointment
    RESCHEDULED         // Optional: If you want to track original appointment before rescheduling
}