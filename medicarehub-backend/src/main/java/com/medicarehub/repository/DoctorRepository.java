package com.medicarehub.repository;

import com.medicarehub.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByLicenseNumber(String licenseNumber); // For fetching

    // VVVVVV ADD THIS METHOD SIGNATURE VVVVVV
    boolean existsByLicenseNumber(String licenseNumber); // For checking existence
    // ^^^^^^ ADD THIS METHOD SIGNATURE ^^^^^^

    Optional<Doctor> findByUserUsername(String username); // We added this earlier

    @Query("SELECT d FROM Doctor d JOIN d.user u JOIN d.department dept " +
            "WHERE lower(u.firstName) LIKE lower(concat('%', :searchTerm, '%')) " +
            "OR lower(u.lastName) LIKE lower(concat('%', :searchTerm, '%')) " +
            "OR lower(d.specialization) LIKE lower(concat('%', :searchTerm, '%')) " +
            "OR lower(dept.name) LIKE lower(concat('%', :searchTerm, '%'))")
    Page<Doctor> searchDoctors(@Param("searchTerm") String searchTerm, Pageable pageable);

    List<Doctor> findByDepartmentId(Long departmentId);
}