package io.github.dankoller.antifraud.entity.user;

import io.github.dankoller.antifraud.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("unused")
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * This method is used to load a user by username implementing the {@link UserDetailsService} interface.
     *
     * @param username the username identifying the user whose data is required.
     * @return A fully populated user record by passing the found user object to the UserDetailsImpl constructor.
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new UsernameNotFoundException("User not found");

        return new UserDetailsImpl(user);
    }

}
