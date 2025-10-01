package edu.project.authentication.config;

import edu.project.authentication.model.User;
import edu.project.authentication.repository.UserRepository;
import edu.project.authentication.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.frontend.url}")
    private String frontendBaseUrl; // e.g., http://localhost:3000

    public OAuth2LoginSuccessHandler(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        // The default URL to redirect to after successful login (we'll override this)
        this.setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();

        // 1. Extract necessary user details
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isEmpty()) {
            // 2. NEW USER: Register the user in our database
            user = new User();
            user.setEmail(email);
            // OAuth users don't need a password hash, but we set a placeholder
            user.setPassword("{noop}oauth2_user");
            user.setEnabled(true); // Automatically verified
            userRepository.save(user);
        } else {
            // 3. EXISTING USER: Retrieve the existing account
            user = userOptional.get();
        }

        // 4. Generate our custom JWT for the client to use with the REST API
        String token = jwtUtil.generateToken(user.getEmail());

        // 5. Redirect the client to the frontend, passing the JWT as a URL parameter
        // The frontend will extract the token and store it in localStorage.
        String redirectUrl = frontendBaseUrl + "/oauth-redirect?token=" + token;

        // Use the handler's method to perform the redirect
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}

