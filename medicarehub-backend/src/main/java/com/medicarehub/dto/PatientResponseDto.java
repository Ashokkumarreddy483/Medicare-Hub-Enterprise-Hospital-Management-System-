package com.medicarehub.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor; // Ensures a no-argument constructor is available

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor // This provides the new PatientResponseDto() constructor
public class PatientResponseDto {

    private Long id; // Patient's own ID
    private String patientUniqueId;

    // User details
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;
    private List<String> roles; // From the User entity
    private boolean userIsActive;
    private LocalDateTime userCreatedAt;


    // Patient-specific details
    private String bloodGroup;
    private String medicalHistorySummary;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;
    private LocalDate registrationDate;
}