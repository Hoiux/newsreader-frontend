package org.hoiux.newsreader.security;

import org.springframework.security.crypto.password.PasswordEncoder;

/*
The Vaadin web security stuff uses the Spring BCryptPasswordEncoder to decode
passwords. My passwords are stored in the database in plain text because I don't
have time to create a way of encoding user passwords right now. So the security
stuff will use this plain text password 'decoder' instead.
 */
public class PasswordNonEncoder implements PasswordEncoder {
    @Override
    public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return rawPassword.toString().equals(encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return PasswordEncoder.super.upgradeEncoding(encodedPassword);
    }
}
