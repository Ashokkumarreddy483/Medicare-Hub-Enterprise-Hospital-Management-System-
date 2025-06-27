package com.medicarehub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the User account for login and basic details
    // A Patient IS A User in our system.
    @OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true) // If user is deleted, patient is deleted
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    @NotNull
    private User user; // This user will store firstName, lastName, email, phone, address, dob, etc.

    @Column(unique = true, nullable = false, length = 20)
    private String patientUniqueId; // e.g., P000001 - to be generated

    @Size(max = 20)
    private String bloodGroup; // e.g., O+, A-

    @Column(columnDefinition = "TEXT")
    private String medicalHistorySummary; // Allergies, chronic conditions, past surgeries

    @Size(max = 100)
    private String emergencyContactName;

    @Size(max = 15)
    private String emergencyContactPhone;

    @Size(max = 50)
    private String emergencyContactRelationship;

    @NotNull
    @PastOrPresent
    private LocalDate registrationDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPatientUniqueId() {
        return patientUniqueId;
    }

    public void setPatientUniqueId(String patientUniqueId) {
        this.patientUniqueId = patientUniqueId;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getMedicalHistorySummary() {
        return medicalHistorySummary;
    }

    public void setMedicalHistorySummary(String medicalHistorySummary) {
        this.medicalHistorySummary = medicalHistorySummary;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getEmergencyContactRelationship() {
        return emergencyContactRelationship;
    }

    public void setEmergencyContactRelationship(String emergencyContactRelationship) {
        this.emergencyContactRelationship = emergencyContactRelationship;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
// Timestamps for auditing, inherited from User or add specific ones if needed
    // @Column(nullable = false, updatable = false)
    // private LocalDateTime createdAt;
    // private LocalDateTime updatedAt;

    // Other patient-specific fields could be added here:
    // - preferredLanguage
    // - maritalStatus
    // - occupation
    // - etc.

    // Constructor for convenience (User object will be created and set separately)
    public Patient(User user, String patientUniqueId, LocalDate registrationDate) {
        this.user = user;
        this.patientUniqueId = patientUniqueId;
        this.registrationDate = registrationDate;
    }

    // @PrePersist
    // protected void onCreate() {
    //     createdAt = LocalDateTime.now();
    //     updatedAt = LocalDateTime.now();
    //     if (registrationDate == null) {
    //         registrationDate = LocalDate.now();
    //     }
    // }

    // @PreUpdate
    // protected void onUpdate() {
    //     updatedAt = LocalDateTime.now();
    // }
}