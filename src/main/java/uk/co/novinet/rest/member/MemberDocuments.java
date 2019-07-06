package uk.co.novinet.rest.member;

import uk.co.novinet.service.member.Member;
import uk.co.novinet.service.member.SftpDocument;

import java.util.List;

public class MemberDocuments {
    private Member member;
    private List<SftpDocument> documents;

    public MemberDocuments(Member member, List<SftpDocument> documents) {
        this.member = member;
        this.documents = documents;
    }

    public Member getMember() {
        return member;
    }

    public List<SftpDocument> getDocuments() {
        return documents;
    }
}
