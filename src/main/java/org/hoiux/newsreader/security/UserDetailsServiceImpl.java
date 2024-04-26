package org.hoiux.newsreader.security;

import org.hoiux.newsreader.data.User;
import org.hoiux.newsreader.services.BackendService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final BackendService backendService;

    public UserDetailsServiceImpl(BackendService backendService) {

        this.backendService = backendService;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = backendService.getUser(username);
        if (user == null) {
            throw new UsernameNotFoundException("No user present with username: " + username);
        } else {
            return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));
        }
    }

}
