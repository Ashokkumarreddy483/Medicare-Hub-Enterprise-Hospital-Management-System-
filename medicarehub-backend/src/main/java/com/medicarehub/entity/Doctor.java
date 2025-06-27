package com.medicarehub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
// import java.util.Set; // For DoctorSchedules

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    @NotNull
    private User user; // Holds name, email, login credentials etc.

    @ManyToOne(fetch = FetchType.EAGER) // Eager fetch department as it's often needed with doctor info
    @JoinColumn(name = "department_id", nullable = false)
    @NotNull
    private Department department;

    @NotBlank
    @Size(max = 100)
    private String specialization; // e.g., Cardiologist, Pediatrician

    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = false)
    private String licenseNumber;

    @PositiveOrZero
    private Integer yearsOfExperience;

    @PositiveOrZero
    @Column(precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Size(max = 500)
    private String qualifications; // e.g., MBBS, MD

    // Optional: Link to schedules
    // @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // private Set<DoctorSchedule> schedules;

    public Doctor(User user, Department department, String specialization, String licenseNumber) {
        this.user = user;
        this.department = department;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
    }
}