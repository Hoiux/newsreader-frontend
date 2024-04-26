package org.hoiux.newsreader.security;

import com.vaadin.flow.spring.security.AuthenticationContext;

import java.util.Optional;

import org.hoiux.newsreader.data.User;

import org.hoiux.newsreader.services.BackendService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthenticatedUser {

    private final BackendService backendService;
    private final AuthenticationContext authenticationContext;

    public AuthenticatedUser(AuthenticationContext authenticationContext, BackendService backendService) {
        this.backendService = backendService;
        this.authenticationContext = authenticationContext;
    }

    @Transactional
    public Optional<User> get() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(userDetails -> backendService.getUser(userDetails.getUsername()));
    }

    public void logout() {
        authenticationContext.logout();
    }

}
