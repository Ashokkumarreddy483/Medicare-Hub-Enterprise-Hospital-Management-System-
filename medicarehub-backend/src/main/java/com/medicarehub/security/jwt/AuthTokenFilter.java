package com.medicarehub.security.jwt;

import com.medicarehub.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        logger.info("====================================================================");
        logger.info("AuthTokenFilter: START Processing request for URI: {} (Method: {})", requestURI, request.getMethod());

        logger.info("AuthTokenFilter: ALL INCOMING HEADERS for URI: {}", requestURI);
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if ("authorization".equalsIgnoreCase(headerName)) {
                    logger.info("AuthTokenFilter: Header -> {}: {}", headerName, request.getHeader(headerName));
                } else {
                    logger.info("AuthTokenFilter: Header -> {}: {}", headerName, Collections.list(request.getHeaders(headerName)));
                }
            }
        } else {
            logger.info("AuthTokenFilter: No headers found in the request.");
        }
        logger.info("AuthTokenFilter: END ALL INCOMING HEADERS for URI: {}", requestURI);


        try {
            String jwt = parseJwt(request);

            if (jwt != null) {
                logger.info("AuthTokenFilter: JWT found by parseJwt method.");
                logger.debug("AuthTokenFilter: Parsed JWT (first 30 chars): {}...", jwt.substring(0, Math.min(jwt.length(), 30)));

                if (jwtUtils.validateJwtToken(jwt)) {
                    logger.info("AuthTokenFilter: JWT validation successful.");
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.info("AuthTokenFilter: Username extracted from JWT: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (userDetails != null) {
                        logger.info("AuthTokenFilter: UserDetails loaded for {}. Authorities: {}", username, userDetails.getAuthorities());
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("AuthTokenFilter: User '{}' successfully authenticated and set in SecurityContextHolder.", username);
                    } else {
                        logger.warn("AuthTokenFilter: UserDetails NOT loaded for username: {}", username);
                    }
                } else {
                    logger.warn("AuthTokenFilter: JWT validation FAILED for token (first 30 chars): {}...", jwt.substring(0, Math.min(jwt.length(), 30)));
                }
            } else {
                logger.info("AuthTokenFilter: parseJwt method returned null (No Bearer token found or malformed header).");
            }
        } catch (Exception e) {
            logger.error("AuthTokenFilter: Exception during JWT processing for URI: {}. Error: {}", requestURI, e.getMessage(), e);
        }

        logger.info("AuthTokenFilter: Proceeding with filter chain for URI: {}", requestURI);
        logger.info("====================================================================");
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (headerAuth == null) {
            logger.warn("AuthTokenFilter (parseJwt): 'Authorization' header is NULL.");
            return null;
        }

        logger.info("AuthTokenFilter (parseJwt): Raw 'Authorization' header value: [{}]", headerAuth);

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            if (StringUtils.hasText(token)) {
                logger.info("AuthTokenFilter (parseJwt): Extracted Bearer token successfully (first 30 chars): {}...", token.substring(0, Math.min(token.length(), 30)));
                return token;
            } else {
                logger.warn("AuthTokenFilter (parseJwt): 'Authorization' header starts with 'Bearer ' but token part is empty.");
                return null;
            }
        }
        logger.warn("AuthTokenFilter (parseJwt): 'Authorization' header does not start with 'Bearer ' or is empty. Value was: [{}]", headerAuth);
        return null;
    }
}