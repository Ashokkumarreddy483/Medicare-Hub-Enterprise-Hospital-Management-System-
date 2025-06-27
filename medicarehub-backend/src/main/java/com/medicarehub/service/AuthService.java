package com.medicarehub.service;

import com.medicarehub.dto.JwtResponseDto;
import com.medicarehub.dto.LoginRequestDto;
import com.medicarehub.dto.SignupRequestDto;
import com.medicarehub.entity.ERole;
import com.medicarehub.entity.Role;
import com.medicarehub.entity.User;
import com.medicarehub.exception.RoleNotFoundException;
import com.medicarehub.exception.UserAlreadyExistsException;
import com.medicarehub.repository.RoleRepository;
import com.medicarehub.repository.UserRepository;
import com.medicarehub.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Transactional
    public User registerUser(SignupRequestDto signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new UserAlreadyExistsException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new UserAlreadyExistsException("Error: Email is already in use!");
        }

        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getFirstName(),
                signUpRequest.getLastName());

        user.setPhoneNumber(signUpRequest.getPhoneNumber());
        user.setAddress(signUpRequest.getAddress());
        user.setDateOfBirth(signUpRequest.getDateOfBirth());


        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_PATIENT)
                    .orElseThrow(() -> new RoleNotFoundException("Error: Role PATIENT is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toUpperCase()) {
                    case "ADMIN":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RoleNotFoundException("Error: Role ADMIN is not found."));
                        roles.add(adminRole);
                        break;
                    case "DOCTOR":
                        Role modRole = roleRepository.findByName(ERole.ROLE_DOCTOR)
                                .orElseThrow(() -> new RoleNotFoundException("Error: Role DOCTOR is not found."));
                        roles.add(modRole);
                        break;
                    case "NURSE":
                        Role nurseRole = roleRepository.findByName(ERole.ROLE_NURSE)
                                .orElseThrow(() -> new RoleNotFoundException("Error: Role NURSE is not found."));
                        roles.add(nurseRole);
                        break;
                    // Add other roles as needed
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_PATIENT)
                                .orElseThrow(() -> new RoleNotFoundException("Error: Role PATIENT is not found."));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        return userRepository.save(user);
    }

    public JwtResponseDto authenticateUser(LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponseDto(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }

    // Helper to seed roles - call this on application startup
    @Transactional
    public void seedRoles() {
        for (ERole roleName : ERole.values()) {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(new Role(roleName));
                System.out.println("Seeded role: " + roleName);
            }
        }
    }
}