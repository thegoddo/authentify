package edu.project.authentication.service.impl;

import edu.project.authentication.model.User;
import edu.project.authentication.model.VerificationToken;
import edu.project.authentication.repository.UserRepository;
import edu.project.authentication.repository.VerificationTokenRepository;
import edu.project.authentication.service.MailService;
import edu.project.authentication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Inject PasswordEncoder

    private final MailService mailService;
    private final VerificationTokenRepository verificationTokenRepository;

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
}
