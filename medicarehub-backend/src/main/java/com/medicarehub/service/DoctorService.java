package com.medicarehub.service;

import com.medicarehub.dto.DoctorRequestDto;
import com.medicarehub.dto.DoctorResponseDto;
import com.medicarehub.entity.Department;
import com.medicarehub.entity.Doctor;
import com.medicarehub.entity.ERole;
import com.medicarehub.entity.Role;
import com.medicarehub.entity.User;
import com.medicarehub.exception.ResourceAlreadyExistsException;
import com.medicarehub.exception.ResourceNotFoundException;
import com.medicarehub.repository.DepartmentRepository;
import com.medicarehub.repository.DoctorRepository;
import com.medicarehub.repository.RoleRepository;
import com.medicarehub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Mapper Method ---
    private DoctorResponseDto mapToDoctorResponseDto(Doctor doctor) {
        if (doctor == null) {
            return null;
        }
        User user = doctor.getUser();
        Department department = doctor.getDepartment();

        DoctorResponseDto dto = new DoctorResponseDto();
        dto.setId(doctor.getId());

        if (user != null) {
            dto.setUserId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setPhoneNumber(user.getPhoneNumber());
            dto.setDateOfBirth(user.getDateOfBirth()); // Assuming User has DoB
            dto.setAddress(user.getAddress());       // Assuming User has Address
            dto.setActive(user.isActive());
            if (user.getRoles() != null) {
                dto.setUserRoles(user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList()));
            }
        }

        if (department != null) {
            dto.setDepartmentId(department.getId());
            dto.setDepartmentName(department.getName());
        }

        dto.setSpecialization(doctor.getSpecialization());
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setYearsOfExperience(doctor.getYearsOfExperience());
        dto.setConsultationFee(doctor.getConsultationFee());
        dto.setQualifications(doctor.getQualifications());

        return dto;
    }

    // --- CRUD Methods ---

    @Transactional
    public DoctorResponseDto createDoctor(DoctorRequestDto doctorRequestDto) {
        // Validate user details
        if (userRepository.existsByUsername(doctorRequestDto.getUsername())) {
            throw new ResourceAlreadyExistsException("Username '" + doctorRequestDto.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(doctorRequestDto.getEmail())) {
            throw new ResourceAlreadyExistsException("Email '" + doctorRequestDto.getEmail() + "' is already in use.");
        }
        if (doctorRepository.existsByLicenseNumber(doctorRequestDto.getLicenseNumber())) {
            throw new ResourceAlreadyExistsException("License number '" + doctorRequestDto.getLicenseNumber() + "' is already registered.");
        }
        if (doctorRequestDto.getPassword() == null || doctorRequestDto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required for creating a new doctor user.");
        }


        // Fetch Department
        Department department = departmentRepository.findById(doctorRequestDto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + doctorRequestDto.getDepartmentId()));

        // Create User account for the doctor
        User user = new User();
        user.setUsername(doctorRequestDto.getUsername());
        user.setEmail(doctorRequestDto.getEmail());
        user.setPassword(passwordEncoder.encode(doctorRequestDto.getPassword()));
        user.setFirstName(doctorRequestDto.getFirstName());
        user.setLastName(doctorRequestDto.getLastName());
        user.setPhoneNumber(doctorRequestDto.getPhoneNumber());
        // User's dateOfBirth and address can be set here if they are part of DoctorRequestDto and meant for User entity
        // For now, assuming they are not directly set on User for doctor creation via this DTO, or are optional

        Role doctorRole = roleRepository.findByName(ERole.ROLE_DOCTOR)
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_DOCTOR is not found."));
        // Optionally, add a generic ROLE_USER as well if doctors should also have basic user privileges
        // Role userRole = roleRepository.findByName(ERole.ROLE_USER)
        // .orElseThrow(() -> new RuntimeException("Error: Role ROLE_USER is not found."));
        Set<Role> roles = new HashSet<>();
        roles.add(doctorRole);
        // roles.add(userRole);
        user.setRoles(roles);
        user.setActive(true);
        User savedUser = userRepository.save(user);

        // Create Doctor entity
        Doctor doctor = new Doctor();
        doctor.setUser(savedUser);
        doctor.setDepartment(department);
        doctor.setSpecialization(doctorRequestDto.getSpecialization());
        doctor.setLicenseNumber(doctorRequestDto.getLicenseNumber());
        doctor.setYearsOfExperience(doctorRequestDto.getYearsOfExperience());
        doctor.setConsultationFee(doctorRequestDto.getConsultationFee());
        doctor.setQualifications(doctorRequestDto.getQualifications());

        Doctor savedDoctor = doctorRepository.save(doctor);
        return mapToDoctorResponseDto(savedDoctor);
    }

    public DoctorResponseDto getDoctorById(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));
        return mapToDoctorResponseDto(doctor);
    }

    public Page<DoctorResponseDto> getAllDoctors(Pageable pageable) {
        Page<Doctor> doctors = doctorRepository.findAll(pageable);
        return doctors.map(this::mapToDoctorResponseDto);
    }

    // Example: Search doctors
    public Page<DoctorResponseDto> searchDoctors(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return getAllDoctors(pageable);
        }
        Page<Doctor> doctors = doctorRepository.searchDoctors(searchTerm.toLowerCase(), pageable);
        return doctors.map(this::mapToDoctorResponseDto);
    }


    @Transactional
    public DoctorResponseDto updateDoctor(Long doctorId, DoctorRequestDto doctorRequestDto) {
        Doctor existingDoctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        User user = existingDoctor.getUser();

        // Update User fields (excluding username, email, password for simplicity in this main update method)
        // These should ideally be handled by separate user profile management or specific change requests.
        if (doctorRequestDto.getFirstName() != null) user.setFirstName(doctorRequestDto.getFirstName());
        if (doctorRequestDto.getLastName() != null) user.setLastName(doctorRequestDto.getLastName());
        if (doctorRequestDto.getPhoneNumber() != null) user.setPhoneNumber(doctorRequestDto.getPhoneNumber());
        // If password needs to be updated, it should be a separate secure process
        // and should not use the plain password from DoctorRequestDto directly without hashing.
        // For now, password in DoctorRequestDto is ignored on update.

        // Update Department if departmentId is provided and different
        if (doctorRequestDto.getDepartmentId() != null &&
                !doctorRequestDto.getDepartmentId().equals(existingDoctor.getDepartment().getId())) {
            Department newDepartment = departmentRepository.findById(doctorRequestDto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("New Department not found with ID: " + doctorRequestDto.getDepartmentId()));
            existingDoctor.setDepartment(newDepartment);
        }

        // Update Doctor-specific fields
        if (doctorRequestDto.getSpecialization() != null) existingDoctor.setSpecialization(doctorRequestDto.getSpecialization());

        // License number update needs care due to uniqueness
        if (doctorRequestDto.getLicenseNumber() != null &&
                !doctorRequestDto.getLicenseNumber().equals(existingDoctor.getLicenseNumber())) {
            if (doctorRepository.existsByLicenseNumber(doctorRequestDto.getLicenseNumber())) {
                throw new ResourceAlreadyExistsException("Another doctor with license number '" + doctorRequestDto.getLicenseNumber() + "' already exists.");
            }
            existingDoctor.setLicenseNumber(doctorRequestDto.getLicenseNumber());
        }

        if (doctorRequestDto.getYearsOfExperience() != null) existingDoctor.setYearsOfExperience(doctorRequestDto.getYearsOfExperience());
        if (doctorRequestDto.getConsultationFee() != null) existingDoctor.setConsultationFee(doctorRequestDto.getConsultationFee());
        if (doctorRequestDto.getQualifications() != null) existingDoctor.setQualifications(doctorRequestDto.getQualifications());

        // User entity is part of Doctor, changes to User object will be cascaded if Doctor is saved.
        // userRepository.save(user); // May not be needed due to cascading from Doctor save
        Doctor updatedDoctor = doctorRepository.save(existingDoctor);
        return mapToDoctorResponseDto(updatedDoctor);
    }

    @Transactional
    public void deleteDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        // Option 1: Hard delete (will also delete associated User due to orphanRemoval=true)
        // doctorRepository.delete(doctor);

        // Option 2: Soft delete (deactivate user, doctor record might remain or also have an active flag)
        User user = doctor.getUser();
        user.setActive(false);
        userRepository.save(user);
        // If Doctor entity also has an 'active' flag, set it here:
        // doctor.setActive(false);
        // doctorRepository.save(doctor);
        // For now, just deactivating the user makes the doctor effectively inactive for login.
        // If you keep the doctor record, it can still be listed as "inactive".
        // If you truly want to remove the doctor association, consider nullifying Doctor fields or deleting the Doctor record
        // but keeping the User record if the user might have other roles or history.
        // For simplicity of full removal from active system:
        doctorRepository.delete(doctor); // This will also trigger user deletion via cascade.
    }
}