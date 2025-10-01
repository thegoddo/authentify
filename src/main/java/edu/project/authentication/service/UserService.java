package edu.project.authentication.service;

import edu.project.authentication.dto.ResetPasswordRequest;
import edu.project.authentication.model.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    public User registerNewUser(User user);
    public boolean verifyUserAccount(String token);
    public boolean resetPassword(ResetPasswordRequest request);
    public boolean createPasswordResetToken(String email, String frontendBaseUrl);
}
