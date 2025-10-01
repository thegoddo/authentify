package edu.project.authentication.controller;

import edu.project.authentication.dto.LoginRequest;
import edu.project.authentication.dto.ResetPasswordRequest;
import edu.project.authentication.model.User;
import edu.project.authentication.service.AuthService;
import edu.project.authentication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        // Error handling for existing email should be added here
        userService.registerNewUser(user);
        return ResponseEntity.ok("Registration successful. Please check your email for verification.");
    }

    @GetMapping("/verify-account")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        boolean verified = userService.verifyUserAccount(token);

        if (verified) {
            return ResponseEntity.ok("Account successfully verified! You can now log in.");
        } else {
            return ResponseEntity.badRequest().body("Verification failed. Token is invalid or expired.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        try {
            String jwt = authService.authenticateAndGenerateToken(loginRequest);
            return ResponseEntity.ok(jwt); // Return the JWT token
        } catch (Exception e) {
            // Detailed exception handling should be implemented here (e.g., specific 401 response)
            return ResponseEntity.status(401).body("Invalid credentials or account not verified.");
        }
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<String> requestPasswordReset(@RequestBody String email) {
        // In a real app, you'd validate the email format here
        String frontendBaseUrl = "http://localhost:3000"; // Get this from @Value or context
        userService.createPasswordResetToken(email, frontendBaseUrl);

        return ResponseEntity.ok("If a matching account was found, a password reset link has been sent.");
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<String> confirmPasswordReset(@RequestBody ResetPasswordRequest request) {
        // You should add validation for the new password strength here

        boolean success = userService.resetPassword(request);

        if (success) {
            return ResponseEntity.ok("Password successfully reset! You can now log in.");
        } else {
            return ResponseEntity.badRequest().body("Password reset failed. Token is invalid or expired.");
        }
    }
}
