package uk.co.novinet.service.member;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class MemberGroup {
    private Long id;
    private String groupName;

    public MemberGroup(Long id, String groupName) {
        this.id = id;
        this.groupName = groupName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }
}
