package com.fintech.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // REVIEW: production hardening required - configure specific origins
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody SignupRequest request) {
        Map<String, String> response = new HashMap<>();

        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            response.put("error", "User with this email already exists");
            return ResponseEntity.badRequest().body(response);
        }

        // Create new user (disabled by default)
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(false); // Disabled until verified
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Generate verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(savedUser);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24)); // Token expires in 24 hours
        verificationToken.setCreatedAt(LocalDateTime.now());

        // Save verification token
        verificationTokenRepository.save(verificationToken);

        // Send verification email
        try {
            String verificationUrl = "https://<APP>/api/auth/verify?token=" + token;
            emailService.sendVerificationEmail(request.getEmail(), verificationUrl);
            response.put("message", "User created successfully. Please check your email for verification link.");
            response.put("status", "success");
        } catch (Exception e) {
            response.put("error", "User created but failed to send verification email");
            response.put("status", "partial_success");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            response.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        User user = userOpt.get();

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            response.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Check if user is verified
        if (!user.isEnabled()) {
            response.put("error", "Account not verified. Please check your email for verification link.");
            response.put("status", "unverified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        // Generate JWT token
        String jwt = jwtUtil.generateToken(user.getEmail());
        response.put("token", jwt);
        response.put("user", Map.of(
            "id", user.getId(),
            "email", user.getEmail(),
            "firstName", user.getFirstName(),
            "lastName", user.getLastName()
        ));
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verify(@RequestParam String token) {
        Map<String, String> response = new HashMap<>();

        // Find verification token
        Optional<VerificationToken> tokenOpt = verificationTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            response.put("error", "Invalid verification token");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        VerificationToken verificationToken = tokenOpt.get();

        // Check if token is expired
        if (verificationToken.isExpired()) {
            response.put("error", "Verification token has expired");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if token is already used
        if (verificationToken.isUsed()) {
            response.put("error", "Verification token has already been used");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        // Enable the user
        User user = verificationToken.getUser();
        user.setEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Mark token as used
        verificationToken.markAsUsed();
        verificationTokenRepository.save(verificationToken);

        // Send welcome email
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        } catch (Exception e) {
            // Log error but don't fail verification
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        response.put("message", "Account verified successfully! You can now log in.");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

    // Inner classes for request/response
    public static class SignupRequest {
        private String email;
        private String password;
        private String firstName;
        private String lastName;

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
