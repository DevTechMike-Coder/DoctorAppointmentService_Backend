package com.example.doctorappointmentservice.security;

import com.example.doctorappointmentservice.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security {@link UserDetails} adapter wrapping the app's {@link User}
 * entity, so Spring Security's authentication machinery (and JWT-derived
 * principals) can work with our own domain model. Account flags are all
 * hard-coded to true since this app doesn't support locking/expiry.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;
    private final boolean enabled;

    public CustomUserDetails(User user) {
        this(user, true);
    }

    public CustomUserDetails(User user, boolean enabled) {
        this.user = user;
        this.enabled = enabled;
    }

    /** Convenience accessor for the wrapped user's database id. */
    public Long getId() {
        return user.getId();
    }

    /** Maps the user's single {@link com.example.doctorappointmentservice.entity.Role} to a Spring Security "ROLE_*" authority. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    /** Returns the stored bcrypt password hash. */
    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    /** The email address is used as the Spring Security "username". */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}