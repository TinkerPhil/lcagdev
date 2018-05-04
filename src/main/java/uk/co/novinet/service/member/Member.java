package uk.co.novinet.service.member;

import uk.co.novinet.service.mail.PasswordDetails;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Member {
    private Map<String, Integer> STATUS_MAPPINGS = new HashMap<String, Integer>() {{
       put("Registered", 0);
       put("LCAG Guest", 1);
       put("Administrators", 2);
    }};

    private Long id;
    private String emailAddress;
    private String username;
    private String name;
    private String group;
    private Date registrationDate;
    private PasswordDetails passwordDetails;

    public Member(Long id, String emailAddress, String username, String name, String group, Date registrationDate, PasswordDetails passwordDetails) {
        this.id = id;
        this.emailAddress = emailAddress;
        this.username = username;
        this.group = group;
        this.registrationDate = registrationDate;
        this.passwordDetails = passwordDetails;
        this.name = name;
    }

    public Long getId() {
        return id;
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

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public Integer getStatus() {
        return STATUS_MAPPINGS.get(group);
    }
}
