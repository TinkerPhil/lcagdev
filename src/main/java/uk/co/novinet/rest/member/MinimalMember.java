package uk.co.novinet.rest.member;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.novinet.service.enquiry.Enquiry;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public class MinimalMember {
    private Long id;

    @NotEmpty(message = "Name cannot be empty")
    private String name;

    @NotEmpty(message = "Email address cannot be empty")
    @Email
    private String emailAddress;

    @NotEmpty(message = "Phone number cannot be empty")
    private String phoneNumber;

    public MinimalMember() {
    }

    public MinimalMember(Long id, String name, String emailAddress, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
    }

    public MinimalMember(Enquiry enquiry) {
        this.id = enquiry.getId();
        this.emailAddress = enquiry.getEmailAddress();
        this.name = enquiry.getName();
        this.phoneNumber = enquiry.getPhoneNumber();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
