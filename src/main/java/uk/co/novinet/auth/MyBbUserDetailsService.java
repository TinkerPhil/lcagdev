package uk.co.novinet.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.member.Member;
import uk.co.novinet.service.member.MemberService;

import java.util.List;

@Service
public class MyBbUserDetailsService implements UserDetailsService {

    @Autowired
    private MemberService memberService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<Member> members = memberService.findExistingForumUsersByField("username", username);

        if (members.size() > 1) {
            throw new RuntimeException("More than one member with username: " + username);
        }

        if (members.isEmpty()) {
            return null;
        }

        return new MyBbUserPrincipal(members.get(0));
    }

}
