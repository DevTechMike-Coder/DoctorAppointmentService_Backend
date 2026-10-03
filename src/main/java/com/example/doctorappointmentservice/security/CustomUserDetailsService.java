package com.example.doctorappointmentservice.security;

import com.example.doctorappointmentservice.entity.User;
import com.example.doctorappointmentservice.repository.UserRepository;
import com.example.doctorappointmentservice.service.email.EmailSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads a {@link User} by email (used as the "username") and wraps it in a
 * {@link CustomUserDetails} for Spring Security. Used by the JWT filter and
 * the DAO authentication provider.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmailSettings emailSettings;

    /**
     * @param email the account's login email
     * @return Spring Security user details for the matching account
     * @throws UsernameNotFoundException if no account exists for that email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));
        boolean enabled = user.isEmailVerified() || !emailSettings.isVerificationRequired();
        return new CustomUserDetails(user, enabled);
    }
}