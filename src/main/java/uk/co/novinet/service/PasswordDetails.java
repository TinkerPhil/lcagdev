package uk.co.novinet.service;

public class PasswordDetails {

    private String salt;
    private String passwordHash;
    private String password;

    public PasswordDetails(String salt, String passwordHash, String password) {
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "PasswordDetails{" +
                "salt='" + salt + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
