package uk.co.novinet.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static uk.co.novinet.auth.MyBbAuthority.*;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private MyBbUserDetailsService userDetailsService;

    @Autowired
    private MyBbPasswordEncoder myBbPasswordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider authProvider = new MyBbDaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(myBbPasswordEncoder);

        http.authenticationProvider(authProvider)
                .httpBasic().and().authorizeRequests()
                .antMatchers("/status").permitAll()
                .antMatchers("/error").permitAll()
                .antMatchers("/api/**").hasAnyAuthority(LCAG_DASHBOARD_API_USER.getFriendlyName())
                .antMatchers("/**").hasAnyAuthority(
                    ADMINISTRATORS.getFriendlyName(),
                    LCAG_DASHBOARD_ADMINISTRATOR.getFriendlyName(),
                    MODERATORS.getFriendlyName(),
                    SUPER_MODERATORS.getFriendlyName()
                ).and().csrf().disable().headers().frameOptions().disable();
    }

}