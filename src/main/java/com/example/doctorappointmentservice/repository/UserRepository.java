package com.example.doctorappointmentservice.repository;

import com.example.doctorappointmentservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Looks up a user by their (unique) login email. */
    Optional<User> findByEmail(String email);

    /** Checks whether an account already exists for the given email. */
    boolean existsByEmail(String email);

    /** Case-insensitive lookup, used for login/registration so "A@x.com" and "a@x.com" are the same account. */
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
