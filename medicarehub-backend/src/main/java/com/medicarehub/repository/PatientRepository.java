package com.medicarehub.repository;

import com.medicarehub.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Keep if you use @Query for other methods
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPatientUniqueId(String patientUniqueId);
    // Optional<Patient> findByUserId(Long userId); // This one is fine if you need it elsewhere

    // VVVVVV ADD THIS METHOD SIGNATURE VVVVVV
    Optional<Patient> findByUserUsername(String username);
    // ^^^^^^ ADD THIS METHOD SIGNATURE ^^^^^^

    boolean existsByPatientUniqueId(String patientUniqueId);

    // This was for a simplistic unique ID generation, consider removing or improving
    // @Query("SELECT MAX(p.id) FROM Patient p")
    // Long findMaxId();

    // If you still need findByUserId for other purposes (e.g., AuthContext on frontend or other services):
    Optional<Patient> findByUserId(Long userId);
}