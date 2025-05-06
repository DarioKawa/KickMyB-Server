package org.kickmyb.server.account;

import org.kickmyb.transfer.SignupRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

// extends UserDetailsService which is one of the Spring Security entry points
public interface ServiceAccount extends UserDetailsService {

    class UsernameTooShort extends Exception {
        public UsernameTooShort() {
            super("Username too short");
        }
    }
    class UsernameAlreadyTaken extends Exception {
        public UsernameAlreadyTaken() {
            super("Username already taken");
        }
    }
    class PasswordTooShort extends Exception {
        public PasswordTooShort() {
            super("Password too short");
        }
    }

    void signup(SignupRequest req) throws BadCredentialsException, UsernameTooShort, PasswordTooShort, UsernameAlreadyTaken;

}
