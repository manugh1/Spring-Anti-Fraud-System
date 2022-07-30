package io.github.dankoller.antifraud.service;

import io.github.dankoller.antifraud.entity.Role;
import io.github.dankoller.antifraud.entity.user.User;
import io.github.dankoller.antifraud.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@SuppressWarnings("unused")
public class AuthorizationService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Update a user's role according to the given role.
     *
     * @param username The username of the user to be updated
     * @param role     The new role of the user
     * @return An updated user object
     */
    public User updateRole(String username, String role) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!Role.getRolesAsString().contains(role) || role.equals("ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (user.getRoleWithoutPrefix().equals(role)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        // Assign role
        user.setRole("ROLE_" + role);

        // Save updated user to database
        userRepository.save(user);

        return user;
    }

    /**
     * Update a user's access level according to the given access level (can be LOCKED or UNLOCKED).
     *
     * @param username The username of the user to be updated
     * @param access   The new access level of the user
     * @return An updated user object
     */
    public Map<String, String> updateAccess(String username, String access) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Cannot update access level of an administrator
        if (user.getRole().equals("ROLE_ADMINISTRATOR")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        if (access.equals("LOCK")) {
            user.setAccountNonLocked(false);
            userRepository.save(user);
            return Map.of("status", "User " + user.getUsername() + " locked!");
        } else if (access.equals("UNLOCK")) {
            user.setAccountNonLocked(true);
            userRepository.save(user);
            return Map.of("status", "User " + user.getUsername() + " unlocked!");
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
