package com.medicarehub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set; // If you want a bi-directional relationship to Doctors

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(unique = true, nullable = false)
    private String name;

    @Size(max = 500)
    private String description;

    // Optional: Bi-directional mapping if you want to easily get all doctors in a department
    // @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private Set<Doctor> doctors;

    public Department(String name, String description) {
        this.name = name;
        this.description = description;
    }
}