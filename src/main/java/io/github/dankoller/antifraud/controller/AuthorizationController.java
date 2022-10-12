package io.github.dankoller.antifraud.controller;

import io.github.dankoller.antifraud.entity.user.User;
import io.github.dankoller.antifraud.request.LoginRequest;
import io.github.dankoller.antifraud.request.UserDTO;
import io.github.dankoller.antifraud.response.UserDataResponse;
import io.github.dankoller.antifraud.service.AuthorizationService;
import io.github.dankoller.antifraud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * This controller handles the authorization endpoints.
 * It receives requests for certain operations and passes them to the service layer.
 * The results of the operations are returned to the client.
 * 'Unused fields' warnings are suppressed because the fields are automatically filled at runtime.
 */

@RestController
@RequestMapping("/api/auth")
@SuppressWarnings("unused")
public class AuthorizationController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Login endpoint for existing users.
     *
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        UserDataResponse userDataResponse = userService.login(loginRequest);
        return new ResponseEntity<>(userDataResponse, HttpStatus.OK);
    }

    /**
     * Signup a new user.
     *
     * @param userDTO The user data object to signup with
     * @return ResponseEntity containing the user's information
     */
    @PostMapping("/user")
    public ResponseEntity<?> signup(@Valid @RequestBody UserDTO userDTO) {
        User user = userService.signup(userDTO);
        UserDataResponse userDataResponse = UserDataResponse.createUserDataResponse(user);
        return new ResponseEntity<>(userDataResponse, HttpStatus.CREATED);
    }

    /**
     * Get a list of all users.
     *
     * @return List of UserDataResponse objects
     */
    @GetMapping("/list")
    public ResponseEntity<?> getUserList() {
        List<UserDataResponse> userDataResponses = userService.getUserDataList();
        return new ResponseEntity<>(userDataResponses, HttpStatus.OK);
    }

    /**
     * Delete a certain user.
     *
     * @param username The username of the user to be deleted
     * @return A response entity with the username of the user that was deleted
     */
    @DeleteMapping("/user/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);

        Map<String, String> response = Map.of(
                "username", username,
                "status", "Deleted successfully!");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * An authorized admin can update the role of a user on this endpoint.
     *
     * @param usernameWithRole A JSON map containing the username of the user and the new role
     * @return A response entity containing the updated user
     */
    @PutMapping("/role")
    public ResponseEntity<?> updateRole(@RequestBody Map<String, String> usernameWithRole) {
        String username = usernameWithRole.get("username");
        String role = usernameWithRole.get("role");

        User user = authorizationService.updateRole(username, role);
        UserDataResponse userDataResponse = UserDataResponse.createUserDataResponse(user);

        return new ResponseEntity<>(userDataResponse, HttpStatus.OK);
    }

    /**
     * An authorized admin can update the access level of a user on this endpoint.
     *
     * @param usernameWithAccess A JSON map containing the username of the user and the new access level
     * @return A response entity containing the updated user
     */
    @PutMapping("/access")
    public ResponseEntity<?> updateAccess(@RequestBody Map<String, String> usernameWithAccess) {
        String username = usernameWithAccess.get("username");
        String access = usernameWithAccess.get("operation");

        Map<String, String> response = authorizationService.updateAccess(username, access);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
