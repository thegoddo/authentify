package edu.project.authentication.service;

import edu.project.authentication.model.User;

public interface UserService {

    public User registerNewUser(User user);
    public boolean verifyUserAccount(String token);
}
