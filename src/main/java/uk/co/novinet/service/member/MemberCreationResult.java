package uk.co.novinet.service.member;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class MemberCreationResult {
    private boolean memberAlreadyExisted;
    private Member member;

    public MemberCreationResult(boolean memberAlreadyExisted, Member member) {
        this.memberAlreadyExisted = memberAlreadyExisted;
        this.member = member;
    }

    public boolean memberAlreadyExisted() {
        return memberAlreadyExisted;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }
}
