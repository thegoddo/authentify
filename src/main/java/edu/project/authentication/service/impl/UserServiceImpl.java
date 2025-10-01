package edu.project.authentication.service.impl;

import edu.project.authentication.dto.ResetPasswordRequest;
import edu.project.authentication.model.User;
import edu.project.authentication.model.VerificationToken;
import edu.project.authentication.repository.UserRepository;
import edu.project.authentication.repository.VerificationTokenRepository;
import edu.project.authentication.service.MailService;
import edu.project.authentication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Inject PasswordEncoder

    private final MailService mailService;
    private final VerificationTokenRepository verificationTokenRepository;

    private static final int PASSWORD_RESET_EXPIRATION_HOURS = 1;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           MailService mailService, VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.verificationTokenRepository = verificationTokenRepository;
    }


    @Override
    public User registerNewUser(User user) {
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        user.setEnabled(false);
        User savedUser = userRepository.save(user);

        VerificationToken token = new VerificationToken(savedUser);
        verificationTokenRepository.save(token);

        String verificationUrl = STR."\{frontendUrl}/verify-email?token=\{token.getToken()}";
        String emailBody = STR."Please verify your email by clicking the following link: \{verificationUrl}";
        mailService.sendEmail(savedUser.getEmail(), "Email Verification", emailBody);
        return savedUser;
    }


    public boolean verifyUserAccount(String token) {
        Optional<VerificationToken> tokenOptional = verificationTokenRepository.findByToken(token);
        if (tokenOptional.isEmpty()) return false;

        VerificationToken verificationToken = tokenOptional.get();
        if(verificationToken.getExpiryDate().before(new Date())) {
            verificationTokenRepository.delete(verificationToken);
            return false;
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
        return true;
    }


    public boolean createPasswordResetToken(String email, String frontendBaseUrl) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            // NOTE: For security, it's best not to confirm whether the email exists.
            // We return true even if the user isn't found to prevent enumeration attacks.
            return true;
        }

        User user = userOptional.get();

        // Generate a random token
        String token = UUID.randomUUID().toString();

        // Calculate expiry date (e.g., 1 hour from now)
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, PASSWORD_RESET_EXPIRATION_HOURS);
        Date expiryDate = cal.getTime();

        // Store token and expiry in the User record
        user.setResetToken(token);
        user.setResetTokenExpiry(expiryDate);
        userRepository.save(user);

        // Send Email
        String resetUrl = frontendBaseUrl + "/reset-password-confirm?token=" + token;
        String emailBody = String.format(
                "You requested a password reset. Click the link below to set a new password (expires in 1 hour):\n%s",
                resetUrl
        );
        mailService.sendEmail(user.getEmail(), "Password Reset Request", emailBody);

        return true;
    }

    public boolean resetPassword(ResetPasswordRequest request) {
        // Find user by token
        Optional<User> userOptional = userRepository.findByResetToken(request.getToken());

        if (userOptional.isEmpty()) {
            return false; // Invalid token
        }

        User user = userOptional.get();

        // 1. Check token expiry
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().before(new Date())) {
            // Invalidate the token even if it was found
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);
            return false; // Token expired
        }

        // 2. Hash and save the new password
        String hashedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(hashedPassword);

        // 3. Clear the reset token fields immediately after use
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
        return true;
    }
}
