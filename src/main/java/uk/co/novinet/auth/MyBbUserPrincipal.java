package uk.co.novinet.auth;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uk.co.novinet.service.member.Member;
import uk.co.novinet.service.member.MemberGroup;
import uk.co.novinet.service.member.MemberService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static uk.co.novinet.auth.MyBbPasswordEncoder.SALT_DELIMITER;

public class MyBbUserPrincipal implements UserDetails {

    private Member member;
    private MemberService memberService;

    public MyBbUserPrincipal(Member member, MemberService memberService) {
        this.member = member;
        this.memberService = memberService;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(member.getGroup()));
        List<MemberGroup> memberGroups = memberService.getAllMemberGroups().stream().filter(memberGroup -> member.getAdditionalGroupIds().contains(memberGroup.getId())).collect(Collectors.toList());
        memberGroups.forEach(memberGroup -> authorities.add(new SimpleGrantedAuthority(memberGroup.getGroupName())));
        return authorities;
    }

    @Override
    public String getPassword() {
        return member.getPasswordDetails().getSalt() + SALT_DELIMITER + member.getPasswordDetails().getPasswordHash();
    }

    @Override
    public String getUsername() {
        return member.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
