package uk.co.novinet.e2e;

public class User {
    private int id;
    private String username;
    private String emailAddress;

    public User(int id, String username, String emailAddress) {
        this.id = id;
        this.username = username;
        this.emailAddress = emailAddress;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
}
