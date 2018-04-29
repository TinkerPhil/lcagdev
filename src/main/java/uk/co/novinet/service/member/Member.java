package uk.co.novinet.service.member;

import uk.co.novinet.service.mail.PasswordDetails;

public class Member {
    private String emailAddress;
    private String username;
    private String name;
    private String group;
    private PasswordDetails passwordDetails;

    public Member(String emailAddress, String username, String name, String group, PasswordDetails passwordDetails) {
        this.emailAddress = emailAddress;
        this.username = username;
        this.group = group;
        this.passwordDetails = passwordDetails;
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public PasswordDetails getPasswordDetails() {
        return passwordDetails;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }
}
