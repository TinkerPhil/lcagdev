package uk.co.novinet;

import org.springframework.beans.factory.annotation.Value;
/*
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
*/
import java.util.Arrays;

import static java.util.Arrays.asList;

//@Configuration
public class SecurityConfig /* extends WebSecurityConfigurerAdapter */ {

    @Value("${dashboard.username}")
    private String username;

    @Value("${dashboard.password}")
    private String password;
/*
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().passwordEncoder(NoOpPasswordEncoder.getInstance()).withUser(username).password(password).roles("USER", "ADMIN");
    }

    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().and().authorizeRequests().antMatchers("/**").hasRole("ADMIN").and().csrf().disable().headers().frameOptions().disable();
    }
*/

}