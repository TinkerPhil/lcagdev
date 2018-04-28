package uk.co.novinet.service;

public class Enquiry {
    private String emailAddress;
    private String name;

    public Enquiry(String emailAddress, String name) {
        this.emailAddress = emailAddress;
        this.name = name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Enquiry{" +
                "emailAddress='" + emailAddress + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
