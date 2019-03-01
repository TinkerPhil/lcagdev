package uk.co.novinet.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uk.co.novinet.service.member.Member;
import uk.co.novinet.service.member.MemberGroup;
import uk.co.novinet.service.member.MemberService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static uk.co.novinet.auth.MyBbAuthority.BANNED;
import static uk.co.novinet.auth.MyBbAuthority.SUSPENDED;

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
        return member.getPasswordDetails().getPasswordHash();
    }

    public String getSalt() {
        return member.getPasswordDetails().getSalt();
    }

    @Override
    public String getUsername() {
        return member.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return getAuthorities().stream().anyMatch(grantedAuthority -> !isAuthorityLocked(grantedAuthority));
    }

    private boolean isAuthorityLocked(GrantedAuthority grantedAuthority) {
        return asList(SUSPENDED.getFriendlyName(), BANNED.getFriendlyName()).contains(grantedAuthority.getAuthority());
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
