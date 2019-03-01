package uk.co.novinet.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

public class MyBbDaoAuthenticationProvider extends DaoAuthenticationProvider {
    public MyBbDaoAuthenticationProvider() {
    }

    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            this.logger.debug("Authentication failed: no credentials provided");
            throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        } else {
            String presentedPassword = authentication.getCredentials().toString();

            MyBbUserPrincipal myBbUserPrincipal = (MyBbUserPrincipal) userDetails;
            MyBbPasswordEncoder myBbPasswordEncoder = (MyBbPasswordEncoder) this.getPasswordEncoder();

            if (!myBbPasswordEncoder.matches(presentedPassword, myBbUserPrincipal.getSalt(), myBbUserPrincipal.getPassword())) {
                this.logger.debug("Authentication failed: password does not match stored value");
                throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
            }
        }
    }
}
