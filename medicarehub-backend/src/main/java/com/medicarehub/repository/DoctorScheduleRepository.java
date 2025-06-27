package com.medicarehub.repository;

import com.medicarehub.entity.Doctor;
import com.medicarehub.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    List<DoctorSchedule> findByDoctor(Doctor doctor);

    List<DoctorSchedule> findByDoctorId(Long doctorId);

    // Find recurring weekly schedules for a doctor
    List<DoctorSchedule> findByDoctorAndDayOfWeekIsNotNull(Doctor doctor);

    // Find specific date overrides/availabilities for a doctor
    List<DoctorSchedule> findByDoctorAndSpecificDateIsNotNull(Doctor doctor);

    // Find schedule entries for a doctor on a specific day of the week (recurring)
    List<DoctorSchedule> findByDoctorAndDayOfWeek(Doctor doctor, DayOfWeek dayOfWeek);

    // Find schedule entries for a doctor on a specific date (overrides)
    List<DoctorSchedule> findByDoctorAndSpecificDate(Doctor doctor, LocalDate specificDate);

    // More complex query to get active schedules for a doctor on a given date, considering overrides
    // This might be better handled in the service layer by fetching both types and merging logic
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor = :doctor " +
            "AND (" +
            "  (ds.specificDate = :date AND ds.isAvailable = true) OR " + // Specific available override for the date
            "  (ds.dayOfWeek = :dayOfWeek AND ds.specificDate IS NULL AND ds.isAvailable = true " + // Recurring weekly slot
            "   AND NOT EXISTS (SELECT dso FROM DoctorSchedule dso WHERE dso.doctor = :doctor AND dso.specificDate = :date AND dso.isAvailable = false " + // And no unavailable override exists for this date
            "                 AND dso.startTime < ds.endTime AND dso.endTime > ds.startTime))" + // Check for time overlap if needed more granularly
            ") ORDER BY ds.startTime ASC")
    List<DoctorSchedule> findActiveSchedulesForDoctorOnDate(
            @Param("doctor") Doctor doctor,
            @Param("date") LocalDate date,
            @Param("dayOfWeek") DayOfWeek dayOfWeek
    );

    // Find if a doctor has a specific unavailable override for a date and time range
    @Query("SELECT CASE WHEN COUNT(ds) > 0 THEN TRUE ELSE FALSE END FROM DoctorSchedule ds " +
            "WHERE ds.doctor = :doctor AND ds.specificDate = :date AND ds.isAvailable = false " +
            "AND ds.startTime < :endTime AND ds.endTime > :startTime")
    boolean hasUnavailableOverride(
            @Param("doctor") Doctor doctor,
            @Param("date") LocalDate date,
            @Param("startTime") java.time.LocalTime startTime,
            @Param("endTime") java.time.LocalTime endTime
    );
}