package uk.co.novinet.service.member;

import uk.co.novinet.service.mail.PasswordDetails;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Member {
    private Map<String, Integer> STATUS_MAPPINGS = new HashMap<String, Integer>() {{
       put("Registered", 0);
       put("Administrators", 3);
       put("Moderators", 1);
    }};

    private Long id;
    private String emailAddress;
    private String username;
    private String name;
    private String group;
    private Date registrationDate;
    private Boolean hmrcLetterChecked;
    private Boolean identificationChecked;
    private String contributionAmount;
    private Date contributionDate;
    private String mpName;
    private String schemes;
    private Boolean mpEngaged;
    private Boolean mpSympathetic;
    private String mpConstituency;
    private String mpParty;
    private PasswordDetails passwordDetails;

    public Member(
            Long id,
            String emailAddress,
            String username,
            String name,
            String group,
            Date registrationDate,
            Boolean hmrcLetterChecked,
            Boolean identificationChecked,
            String contributionAmount,
            Date contributionDate,
            String mpName,
            String schemes,
            Boolean mpEngaged,
            Boolean mpSympathetic,
            String mpConstituency,
            String mpParty,
            PasswordDetails passwordDetails) {
        this.id = id;
        this.emailAddress = emailAddress;
        this.username = username;
        this.group = group;
        this.registrationDate = registrationDate;
        this.hmrcLetterChecked = hmrcLetterChecked;
        this.identificationChecked = identificationChecked;
        this.contributionAmount = contributionAmount;
        this.contributionDate = contributionDate;
        this.mpName = mpName;
        this.schemes = schemes;
        this.mpEngaged = mpEngaged;
        this.mpSympathetic = mpSympathetic;
        this.mpConstituency = mpConstituency;
        this.mpParty = mpParty;
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


    public Boolean getHmrcLetterChecked() {
        return hmrcLetterChecked;
    }

    public Boolean getIdentificationChecked() {
        return identificationChecked;
    }

    public String getContributionAmount() {
        return contributionAmount;
    }

    public Date getContributionDate() {
        return contributionDate;
    }

    public String getMpName() {
        return mpName;
    }

    public String getSchemes() {
        return schemes;
    }

    public Boolean getMpEngaged() {
        return mpEngaged;
    }

    public Boolean getMpSympathetic() {
        return mpSympathetic;
    }

    public String getMpConstituency() {
        return mpConstituency;
    }

    public String getMpParty() {
        return mpParty;
    }
}
