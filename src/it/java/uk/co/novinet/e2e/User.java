package uk.co.novinet.e2e;

public class User {
    private int id;
    private String username;
    private String emailAddress;
    private String name;
    private String token;

    public User(int id, String username, String emailAddress, String name, String token) {
        this.id = id;
        this.username = username;
        this.emailAddress = emailAddress;
        this.name = name;
        this.token = token;
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

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }
}
