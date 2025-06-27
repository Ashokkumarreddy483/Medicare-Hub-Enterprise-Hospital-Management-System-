package com.medicarehub.repository;

import com.medicarehub.entity.Appointment;
import com.medicarehub.entity.AppointmentStatus;
import com.medicarehub.entity.Doctor;
import com.medicarehub.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Find appointments for a specific patient
    Page<Appointment> findByPatient(Patient patient, Pageable pageable);
    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    // Find appointments for a specific doctor
    Page<Appointment> findByDoctor(Doctor doctor, Pageable pageable);
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    // Find appointments for a doctor on a specific date
    List<Appointment> findByDoctorAndAppointmentDate(Doctor doctor, LocalDate appointmentDate);
    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate appointmentDate);

    // Find appointments for a patient on a specific date
    List<Appointment> findByPatientAndAppointmentDate(Patient patient, LocalDate appointmentDate);

    // Check for overlapping appointments for a doctor
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor " +
            "AND a.appointmentDate = :date " +
            "AND a.status <> com.medicarehub.entity.AppointmentStatus.CANCELLED_BY_PATIENT " + // Exclude cancelled
            "AND a.status <> com.medicarehub.entity.AppointmentStatus.CANCELLED_BY_STAFF " +  // Exclude cancelled
            "AND a.appointmentTime < :endTime " +
            "AND FUNCTION('ADDTIME', a.appointmentTime, FUNCTION('SEC_TO_TIME', a.durationMinutes * 60)) > :startTime")
    List<Appointment> findOverlappingAppointmentsForDoctor(
            @Param("doctor") Doctor doctor,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    // Check for overlapping appointments for a patient
    @Query("SELECT a FROM Appointment a WHERE a.patient = :patient " +
            "AND a.appointmentDate = :date " +
            "AND a.status <> com.medicarehub.entity.AppointmentStatus.CANCELLED_BY_PATIENT " +
            "AND a.status <> com.medicarehub.entity.AppointmentStatus.CANCELLED_BY_STAFF " +
            "AND a.appointmentTime < :endTime " +
            "AND FUNCTION('ADDTIME', a.appointmentTime, FUNCTION('SEC_TO_TIME', a.durationMinutes * 60)) > :startTime")
    List<Appointment> findOverlappingAppointmentsForPatient(
            @Param("patient") Patient patient,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    // Find all appointments with optional filters (example for Admin/Receptionist)
    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN a.patient p LEFT JOIN p.user pu " + // Join for patient user details
            "LEFT JOIN a.doctor d LEFT JOIN d.user du " +  // Join for doctor user details
            "WHERE (:dateFilter IS NULL OR a.appointmentDate = :dateFilter) " +
            "AND (:statusFilter IS NULL OR a.status = :statusFilter) " +
            "AND (:patientNameSearch IS NULL OR lower(pu.firstName) LIKE lower(concat('%', :patientNameSearch, '%')) OR lower(pu.lastName) LIKE lower(concat('%', :patientNameSearch, '%'))) " +
            "AND (:doctorNameSearch IS NULL OR lower(du.firstName) LIKE lower(concat('%', :doctorNameSearch, '%')) OR lower(du.lastName) LIKE lower(concat('%', :doctorNameSearch, '%')))")
    Page<Appointment> findAllFiltered(
            @Param("dateFilter") Optional<LocalDate> dateFilter,
            @Param("statusFilter") Optional<AppointmentStatus> statusFilter,
            @Param("patientNameSearch") Optional<String> patientNameSearch,
            @Param("doctorNameSearch") Optional<String> doctorNameSearch,
            Pageable pageable
    );
}