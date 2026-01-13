package com.dushy.tenantmanage.controller;

import com.dushy.tenantmanage.dto.UserDto;
import com.dushy.tenantmanage.dto.auth.JwtResponse;
import com.dushy.tenantmanage.dto.auth.LoginRequest;
import com.dushy.tenantmanage.dto.auth.MessageResponse;
import com.dushy.tenantmanage.dto.auth.SignupRequest;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.security.CustomUserDetailsService;
import com.dushy.tenantmanage.security.JwtUtils;
import com.dushy.tenantmanage.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication operations.
 * Handles user signup, login, and logout.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager,
            UserService userService,
            JwtUtils jwtUtils,
            CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Register a new user.
     *
     * @param signupRequest the registration details
     * @return success message
     */
    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        // Convert SignupRequest to UserDto
        UserDto userDto = UserDto.builder()
                .email(signupRequest.getEmail())
                .password(signupRequest.getPassword())
                .fullName(signupRequest.getFullName())
                .phone(signupRequest.getPhone())
                .userType(signupRequest.getUserType())
                .build();

        userService.registerUser(userDto);

        return ResponseEntity.ok(MessageResponse.builder()
                .message("User registered successfully!")
                .build());
    }

    /**
     * Authenticate user and return JWT token.
     *
     * @param loginRequest the login credentials
     * @return JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails);

        // Get user entity for response
        User user = userDetailsService.loadUserEntityByEmail(loginRequest.getEmail());

        return ResponseEntity.ok(JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .userType(user.getUserType())
                .build());
    }

    /**
     * Logout user (client-side token invalidation).
     * With stateless JWT, logout is handled client-side by discarding the token.
     *
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(MessageResponse.builder()
                .message("User logged out successfully!")
                .build());
    }
}
