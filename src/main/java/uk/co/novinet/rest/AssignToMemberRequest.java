package uk.co.novinet.rest;

public class AssignToMemberRequest {
    private Long memberId;
    private Long paymentId;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    @Override
    public String toString() {
        return "AssignToMemberRequest{" +
                "memberId=" + memberId +
                ", paymentId=" + paymentId +
                '}';
    }
}
