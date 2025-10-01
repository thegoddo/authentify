package edu.project.authentication.controller;

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
}
