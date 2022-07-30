package io.github.dankoller.antifraud.service;

import io.github.dankoller.antifraud.entity.Role;
import io.github.dankoller.antifraud.entity.user.User;
import io.github.dankoller.antifraud.persistence.UserRepository;
import io.github.dankoller.antifraud.request.UserDTO;
import io.github.dankoller.antifraud.response.UserDataResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("unused")
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Returns a certain user by username.
     *
     * @param username The username of the user to be returned
     * @return User object with the given username
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Creates a new user from the given UserDTO and saves it to the database.
     *
     * @param userDTO The UserDTO containing the raw data of the new user
     * @return The newly created user.
     */
    public User signup(UserDTO userDTO) {
        // Create a temporary user to see if the username is available
        User tmpUser = userRepository.findByUsername(userDTO.getUsername());
        if (tmpUser != null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");

        // Assign role to user. First user is assigned as an administrator.
        String role = userRepository.findAll()
                .isEmpty() ? Role.ADMINISTRATOR.stringWithRolePrefix : Role.MERCHANT.stringWithRolePrefix;
        boolean isAccountNonLocked = userRepository.findAll().isEmpty();

        // Create a new user from the DTO
        User user = new User(
                userDTO.getName(),
                userDTO.getUsername(),
                userDTO.getPassword(),
                role,
                isAccountNonLocked
        );

        // Salt password and encode it
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save user to database
        userRepository.save(user);

        return user;
    }


    /**
     * Removes a user from the database.
     *
     * @param username The username of the user to be removed
     */
    public void deleteUser(String username) {
        // Create a temporary user to see if the user even exists
        Optional<User> tmpUser = userRepository.findUserByUsername(username);
        if (tmpUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        } else {
            User userToDelete = tmpUser.get();
            userRepository.delete(userToDelete);
        }
    }

    /**
     * Return a list of all currently registered users.
     *
     * @return List of all registered users
     */
    public List<UserDataResponse> getUserDataList() {
        List<User> users = userRepository.findAll();
        List<UserDataResponse> userDataResponses = new ArrayList<>();

        // Convert User objects to UserDataResponse objects to exclude sensitive information
        for (User u : users) {
            UserDataResponse userDataResponse = UserDataResponse.createUserDataResponse(u);
            userDataResponses.add(userDataResponse);
        }

        return userDataResponses;
    }
}
