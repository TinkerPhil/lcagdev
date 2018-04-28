package uk.co.novinet.service;

public class ForumUser {
    private String emailAddress;
    private String username;
    private String name;
    private PasswordDetails passwordDetails;

    public ForumUser(String emailAddress, String username, String name, PasswordDetails passwordDetails) {
        this.emailAddress = emailAddress;
        this.username = username;
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
}
