package com.medicarehub.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor; // Can be useful for constructing in service or tests

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // If you want a constructor for all fields
public class DoctorResponseDto {

    private Long id; // Doctor's own ID (from Doctor entity)
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth; // Assuming User entity has dateOfBirth
    private String address;       // Assuming User entity has address
    private List<String> userRoles; // e.g., ["ROLE_DOCTOR", "ROLE_USER"]

    // Doctor-specific details
    private Long departmentId;
    private String departmentName; // Denormalized for convenience
    private String specialization;
    private String licenseNumber;
    private Integer yearsOfExperience;
    private BigDecimal consultationFee;
    private String qualifications;

    private boolean isActive; // From User entity
}