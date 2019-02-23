package uk.co.novinet.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import uk.co.novinet.service.member.MemberService;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private MyBbUserDetailsService userDetailsService;

    @Autowired
    private MyBbPasswordEncoder myBbPasswordEncoder;

    @Autowired
    private MemberService memberService;

    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new DaoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(myBbPasswordEncoder);
        return authProvider;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().and().authorizeRequests()
                .antMatchers("/status").permitAll()
                .antMatchers("/**").hasAnyRole("Administrators", "LCAG Dashboard Administrator", "Moderators", "Super Moderators").and().csrf().disable().headers().frameOptions().disable();
    }

}