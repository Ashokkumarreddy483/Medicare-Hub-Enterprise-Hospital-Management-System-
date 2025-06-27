package com.medicarehub.service;

import com.medicarehub.dto.PatientRequestDto;
import com.medicarehub.dto.PatientResponseDto;
import com.medicarehub.entity.ERole;
import com.medicarehub.entity.Patient;
import com.medicarehub.entity.Role;
import com.medicarehub.entity.User;
import com.medicarehub.exception.ResourceNotFoundException;
import com.medicarehub.exception.UserAlreadyExistsException;
import com.medicarehub.repository.PatientRepository;
import com.medicarehub.repository.RoleRepository;
import com.medicarehub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicLong; // For a slightly better (but still not robust) ID generator

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // In-memory counter for unique ID - NOT SUITABLE FOR PRODUCTION.
    // Replace with database sequence or UUID.
    private final AtomicLong patientIdCounter = new AtomicLong(0);

    @Transactional
    public PatientResponseDto createPatient(PatientRequestDto patientRequestDto) {
        if (userRepository.existsByUsername(patientRequestDto.getUsername())) {
            throw new UserAlreadyExistsException("Username '" + patientRequestDto.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(patientRequestDto.getEmail())) {
            throw new UserAlreadyExistsException("Email '" + patientRequestDto.getEmail() + "' is already in use.");
        }

        // Create User part
        User user = new User();
        user.setUsername(patientRequestDto.getUsername());
        user.setEmail(patientRequestDto.getEmail());
        user.setPassword(passwordEncoder.encode(patientRequestDto.getPassword()));
        user.setFirstName(patientRequestDto.getFirstName());
        user.setLastName(patientRequestDto.getLastName());
        user.setPhoneNumber(patientRequestDto.getPhoneNumber());
        user.setAddress(patientRequestDto.getAddress());
        user.setDateOfBirth(patientRequestDto.getDateOfBirth());
        user.setActive(true);

        Role patientRole = roleRepository.findByName(ERole.ROLE_PATIENT)
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_PATIENT is not found."));
        Set<Role> roles = new HashSet<>();
        roles.add(patientRole);
        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        // Create Patient part
        Patient patient = new Patient();
        patient.setUser(savedUser);
        patient.setPatientUniqueId(generatePatientUniqueId());
        patient.setBloodGroup(patientRequestDto.getBloodGroup());
        patient.setMedicalHistorySummary(patientRequestDto.getMedicalHistorySummary());
        patient.setEmergencyContactName(patientRequestDto.getEmergencyContactName());
        patient.setEmergencyContactPhone(patientRequestDto.getEmergencyContactPhone());
        patient.setEmergencyContactRelationship(patientRequestDto.getEmergencyContactRelationship());
        patient.setRegistrationDate(patientRequestDto.getRegistrationDate() != null ? patientRequestDto.getRegistrationDate() : LocalDate.now());

        // Initialize counter if it's the first patient (very basic)
        if (patientIdCounter.get() == 0) {
            long currentMax = patientRepository.count(); // Simplified
            patientIdCounter.set(currentMax);
        }


        Patient savedPatient = patientRepository.save(patient);
        return mapToPatientResponseDto(savedPatient);
    }

    public PatientResponseDto getPatientById(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
        return mapToPatientResponseDto(patient);
    }

    public PatientResponseDto getPatientByPatientUniqueId(String patientUniqueId) {
        Patient patient = patientRepository.findByPatientUniqueId(patientUniqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with unique ID: " + patientUniqueId));
        return mapToPatientResponseDto(patient);
    }

    public Page<PatientResponseDto> getAllPatients(Pageable pageable) {
        Page<Patient> patients = patientRepository.findAll(pageable);
        return patients.map(this::mapToPatientResponseDto);
    }

    @Transactional
    public PatientResponseDto updatePatient(Long patientId, PatientRequestDto patientRequestDto) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        User user = patient.getUser();
        // Only update user fields that are safe to change and present in DTO
        if (patientRequestDto.getFirstName() != null) user.setFirstName(patientRequestDto.getFirstName());
        if (patientRequestDto.getLastName() != null) user.setLastName(patientRequestDto.getLastName());
        if (patientRequestDto.getPhoneNumber() != null) user.setPhoneNumber(patientRequestDto.getPhoneNumber());
        if (patientRequestDto.getAddress() != null) user.setAddress(patientRequestDto.getAddress());
        if (patientRequestDto.getDateOfBirth() != null) user.setDateOfBirth(patientRequestDto.getDateOfBirth());
        // NOTE: Username, email, password changes should be handled via separate, dedicated processes.

        // Update patient-specific fields
        if (patientRequestDto.getBloodGroup() != null) patient.setBloodGroup(patientRequestDto.getBloodGroup());
        if (patientRequestDto.getMedicalHistorySummary() != null) patient.setMedicalHistorySummary(patientRequestDto.getMedicalHistorySummary());
        if (patientRequestDto.getEmergencyContactName() != null) patient.setEmergencyContactName(patientRequestDto.getEmergencyContactName());
        if (patientRequestDto.getEmergencyContactPhone() != null) patient.setEmergencyContactPhone(patientRequestDto.getEmergencyContactPhone());
        if (patientRequestDto.getEmergencyContactRelationship() != null) patient.setEmergencyContactRelationship(patientRequestDto.getEmergencyContactRelationship());
        // Registration date is typically not updated.

        // User entity will be updated by cascade if changes are made and Patient entity is saved.
        // Or, explicitly save user if not using CascadeType.ALL or MERGE or if User is an independent aggregate.
        // userRepository.save(user); // May not be needed depending on cascade settings

        Patient updatedPatient = patientRepository.save(patient);
        return mapToPatientResponseDto(updatedPatient);
    }

    @Transactional
    public void deletePatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
        // If Patient.user has CascadeType.REMOVE or orphanRemoval=true, user might be deleted too.
        // If you want to deactivate user instead:
        // User user = patient.getUser();
        // user.setActive(false);
        // userRepository.save(user);
        patientRepository.delete(patient);
    }


    // --- Helper Methods ---

    private String generatePatientUniqueId() {
        // THIS IS STILL A VERY SIMPLISTIC AND NOT PRODUCTION-READY ID GENERATOR.
        // It's prone to race conditions and issues if records are deleted.
        // For a real system, use a database sequence, UUID, or a more robust strategy.
        long nextVal = patientIdCounter.incrementAndGet(); // Atomically increment
        return "P" + String.format("%07d", nextVal);
    }

    private PatientResponseDto mapToPatientResponseDto(Patient patient) {
        PatientResponseDto dto = new PatientResponseDto(); // Using the no-argument constructor

        dto.setId(patient.getId());
        dto.setPatientUniqueId(patient.getPatientUniqueId());

        User user = patient.getUser();
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setPhoneNumber(user.getPhoneNumber());
            dto.setAddress(user.getAddress());
            dto.setDateOfBirth(user.getDateOfBirth());
            dto.setUserIsActive(user.isActive());
            dto.setUserCreatedAt(user.getCreatedAt());
            if (user.getRoles() != null) {
                dto.setRoles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList()));
            }
        }

        dto.setBloodGroup(patient.getBloodGroup());
        dto.setMedicalHistorySummary(patient.getMedicalHistorySummary());
        dto.setEmergencyContactName(patient.getEmergencyContactName());
        dto.setEmergencyContactPhone(patient.getEmergencyContactPhone());
        dto.setEmergencyContactRelationship(patient.getEmergencyContactRelationship());
        dto.setRegistrationDate(patient.getRegistrationDate());

        return dto;
    }
}