package com.sprintly.controller;

import com.sprintly.dao.UserDAO;
import com.sprintly.dto.ApiResponse;
import com.sprintly.dto.AuthRequest;
import com.sprintly.dto.AuthResponse;
import com.sprintly.dto.RegisterRequest;
import com.sprintly.model.User;
import com.sprintly.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserDAO userDAO;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          UserDAO userDAO,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDAO = userDAO;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user.
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        try {
            // Validate input
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Username is required", 400));
            }
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email is required", 400));
            }
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Password must be at least 6 characters", 400));
            }
            if (request.getRole() == null || !isValidRole(request.getRole())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Role must be ADMIN, MANAGER, or DEVELOPER", 400));
            }

            // Check if username already exists
            if (userDAO.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Username already exists", 400));
            }

            // Check if email already exists
            if (userDAO.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email already exists", 400));
            }

            // Create user with hashed password
            User user = User.builder()
                    .username(request.getUsername().trim())
                    .email(request.getEmail().trim())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .role(request.getRole().toUpperCase())
                    .build();

            long userId = userDAO.insert(user);

            if (userId == -1) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to create user", 500));
            }

            // Generate JWT token
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPasswordHash(),
                    java.util.Collections.singletonList(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole())
                    )
            );

            String token = jwtUtil.generateToken(userDetails);

            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .id(userId)
                    .username(user.getUsername())
                    .role(user.getRole())
                    .build();

            logger.info("User registered successfully: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(authResponse, "User registered successfully"));

        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed: " + e.getMessage(), 500));
        }
    }

    /**
     * Login and receive JWT token.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest request) {
        try {
            // Validate input
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Username is required", 400));
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Password is required", 400));
            }

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername().trim(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            // Get user details from database
            User user = userDAO.findByUsername(request.getUsername().trim())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .id(user.getId())
                    .username(user.getUsername())
                    .role(user.getRole())
                    .build();

            logger.info("User logged in: {}", user.getUsername());
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));

        } catch (Exception e) {
            logger.error("Login failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid username or password", 401));
        }
    }

    /**
     * Check if the role is valid.
     */
    private boolean isValidRole(String role) {
        return "ADMIN".equals(role) || "MANAGER".equals(role) || "DEVELOPER".equals(role);
    }
}
