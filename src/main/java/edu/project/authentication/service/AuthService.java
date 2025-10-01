package edu.project.authentication.service;

import edu.project.authentication.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

        private final AuthenticationManager authenticationManager;
        private final JwtUtil jwtUtil;

        @Autowired
        public AuthService(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
            this.authenticationManager = authenticationManager;
            this.jwtUtil = jwtUtil;
        }

        // TODO: Create a LoginRequest class with email and password fields
        public String authenticateAndGenerateToken(LoginRequest request) throws AuthenticationException {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()`
                    )
            );

            if (authentication.isAuthenticated()) {
                return jwtUtil.generateToken(request.email());
            } else {
                throw new RuntimeException("Authentication failed");
            }

        }
}
