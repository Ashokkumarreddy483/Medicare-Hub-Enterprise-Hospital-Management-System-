package com.medicarehub.service; // Or another appropriate package

import com.medicarehub.entity.Doctor;
import com.medicarehub.entity.DoctorSchedule;
import com.medicarehub.entity.User;
import com.medicarehub.repository.DoctorRepository;
import com.medicarehub.repository.DoctorScheduleRepository;
import com.medicarehub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("customSecurityService") // Name it so Spring Expression Language can find it
public class CustomSecurityService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;

    public boolean isDoctorSelf(Authentication authentication, Long doctorIdInPath) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String currentUsername = authentication.getName();
        Optional<User> currentUserOptional = userRepository.findByUsername(currentUsername);
        if (currentUserOptional.isEmpty()) {
            return false;
        }
        User currentUser = currentUserOptional.get();

        Optional<Doctor> currentDoctorOptional = doctorRepository.findByUserId(currentUser.getId());
        if (currentDoctorOptional.isEmpty()) {
            return false; // Authenticated user is not a doctor
        }

        return currentDoctorOptional.get().getId().equals(doctorIdInPath);
    }

    public boolean isOwnerOfSchedule(Authentication authentication, Long scheduleId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String currentUsername = authentication.getName();
        Optional<User> currentUserOptional = userRepository.findByUsername(currentUsername);
        if (currentUserOptional.isEmpty()) {
            return false;
        }
        User currentUser = currentUserOptional.get();

        Optional<Doctor> currentDoctorOptional = doctorRepository.findByUserId(currentUser.getId());
        if (currentDoctorOptional.isEmpty()) {
            return false; // Authenticated user is not a doctor
        }
        Doctor currentDoctor = currentDoctorOptional.get();

        Optional<DoctorSchedule> scheduleOptional = doctorScheduleRepository.findById(scheduleId);
        if (scheduleOptional.isEmpty()) {
            return false; // Schedule doesn't exist, or let controller handle 404
        }

        return scheduleOptional.get().getDoctor().getId().equals(currentDoctor.getId());
    }

    // You can add more methods here, e.g., isPatientSelf for appointments
}