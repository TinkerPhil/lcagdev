package uk.co.novinet.service.member;

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
}
