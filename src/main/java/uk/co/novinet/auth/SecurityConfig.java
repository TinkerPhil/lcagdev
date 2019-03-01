package uk.co.novinet.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import uk.co.novinet.service.member.MemberService;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private MyBbUserDetailsService userDetailsService;

    @Autowired
    private MyBbPasswordEncoder myBbPasswordEncoder;

    @Autowired
    private MemberService memberService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider authProvider = new MyBbDaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(myBbPasswordEncoder);

        http.authenticationProvider(authProvider)
                .httpBasic().and().authorizeRequests()
                .antMatchers("/status").permitAll()
                .antMatchers("/**").hasAnyAuthority(
                        MyBbAuthority.ADMINISTRATORS.getFriendlyName(),
                        MyBbAuthority.LCAG_DASHBOARD_ADMINISTRATOR.getFriendlyName(),
                        MyBbAuthority.MODERATORS.getFriendlyName(),
                        MyBbAuthority.SUPER_MODERATORS.getFriendlyName())
                .and().csrf().disable().headers().frameOptions().disable();
    }

}