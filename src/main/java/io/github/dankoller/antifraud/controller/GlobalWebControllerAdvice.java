package io.github.dankoller.antifraud.controller;

import io.github.dankoller.antifraud.entity.user.User;
import io.github.dankoller.antifraud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collection;

/**
 * This class is used to add attributes to the model of every request.
 * It is used to add the current user and its role to the model.
 */

@ControllerAdvice
public class GlobalWebControllerAdvice {

    @Autowired
    private UserService userService;

    @ModelAttribute("username")
    public String addUsernameToModel() {
        // Null check to see if the user is logged in
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext == null || securityContext.getAuthentication() == null) return "";

        // Return the display name of the currently logged-in user
        return securityContext.getAuthentication().getName();
    }

    @ModelAttribute("fullname")
    public String addFullnameToModel() {
        User user = userService.findByUsername(addUsernameToModel());
        return user != null ? user.getName() : "";
    }

    @ModelAttribute("userrole")
    @SuppressWarnings("unchecked")
    public String addUserRoleToModel() {
        // Null check to see if the user is logged in
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext == null || securityContext.getAuthentication() == null) return "";

        // Get the role of the currently logged-in user
        Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities();
        String userrole = authorities.iterator().next().getAuthority();

        // Remove ROLE_ from the role
        userrole = userrole.substring(5);
        // Make the first letter uppercase
        userrole = userrole.substring(0, 1).toUpperCase() + userrole.substring(1).toLowerCase();

        return userrole;
    }
}
