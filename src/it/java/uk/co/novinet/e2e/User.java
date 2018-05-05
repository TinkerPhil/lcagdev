package uk.co.novinet.e2e;

public class User {
    private int id;
    private String username;
    private String emailAddress;
    private String name;

    public User(int id, String username, String emailAddress, String name) {
        this.id = id;
        this.username = username;
        this.emailAddress = emailAddress;
        this.name = name;
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
}
