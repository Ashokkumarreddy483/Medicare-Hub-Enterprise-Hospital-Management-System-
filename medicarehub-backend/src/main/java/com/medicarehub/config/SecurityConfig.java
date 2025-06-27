package com.medicarehub.config;

// Make sure all necessary imports are present
import com.medicarehub.security.jwt.AuthEntryPointJwt; // Adjust if your path is different
import com.medicarehub.security.jwt.AuthTokenFilter;   // Adjust if your path is different
// UserDetailsServiceImpl should be in a service package, often within security
// e.g., com.medicarehub.security.services.UserDetailsServiceImpl
import com.medicarehub.service.UserDetailsServiceImpl; // Corrected path based on typical structure
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // Enables Spring Security's web security support
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true) // Enables method-level security like @PreAuthorize
public class SecurityConfig {

    // Using field injection, constructor injection is generally preferred for required dependencies
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;

    // Constructor Injection (Recommended)
    @Autowired
    public SecurityConfig(UserDetailsServiceImpl userDetailsService, AuthEntryPointJwt unauthorizedHandler) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        System.out.println("!!!!!!!!!!! SecurityConfig Instantiated !!!!!!!!!!"); // DEBUG LINE
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        // This bean will be created by Spring, and its @Autowired fields will be injected.
        System.out.println("!!!!!!!!!!! Creating AuthTokenFilter bean !!!!!!!!!!"); // DEBUG LINE
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        System.out.println("!!!!!!!!!!! Creating DaoAuthenticationProvider bean !!!!!!!!!!"); // DEBUG LINE
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // UserDetailsService for fetching user details
        authProvider.setPasswordEncoder(passwordEncoder());     // PasswordEncoder for password comparison
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        System.out.println("!!!!!!!!!!! Creating AuthenticationManager bean (exposed) !!!!!!!!!!"); // DEBUG LINE
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println("!!!!!!!!!!! Creating PasswordEncoder bean (BCrypt) !!!!!!!!!!"); // DEBUG LINE
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("!!!!!!!!!!! Configuring SecurityFilterChain (HttpSecurity) !!!!!!!!!!");

        http
                // Disable CSRF as we are using stateless JWT authentication
                .csrf(csrf -> csrf.disable())

                // Configure exception handling, specifically the entry point for authentication failures
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))

                // Configure session management to be stateless, as JWTs don't rely on sessions
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure authorization for HTTP requests
                .authorizeHttpRequests(auth ->
                        auth
                                // Allow OPTIONS requests globally (often needed for CORS pre-flight)
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                // Publicly accessible authentication endpoints
                                .requestMatchers("/api/auth/**").permitAll()
                                // Publicly accessible documentation and test endpoints
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**", "/api/test").permitAll() // Added /v3/api-docs for SpringDoc
                                // All other requests must be authenticated
                                .anyRequest().authenticated()
                );

        // Add the DaoAuthenticationProvider to HttpSecurity's list of providers
        // This is used by the AuthenticationManager for username/password authentication (e.g., during login)
        http.authenticationProvider(authenticationProvider());

        // Add our custom JWT token filter before the standard UsernamePasswordAuthenticationFilter
        // This filter will process the JWT from the header for every request.
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}