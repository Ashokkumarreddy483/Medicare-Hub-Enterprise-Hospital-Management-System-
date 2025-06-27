package com.medicarehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor; // Useful for testing or service layer construction

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto {

    private Long id; // Will be present in response, null/ignored in request for creation

    @NotBlank(message = "Department name is required.")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters.")
    private String name;

    @Size(max = 500, message = "Description can be up to 500 characters.")
    private String description;

    // You could add other fields like 'headOfDepartmentId' or 'numberOfStaff' in response if needed
}