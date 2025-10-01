package edu.project.authentication.service;

import edu.project.authentication.model.User;
import edu.project.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password(user.getPassword()) // Use hashed password
                .roles(user.getRole().replace("ROLE_", "")) // Remove "ROLE_" prefix if present
                .disabled(!user.isEnabled())
                .accountLocked(!user.isAccountNonLocked())
                .build();
    }
}
