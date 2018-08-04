package uk.co.novinet.service.enquiry;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Enquiry {
    private Long id;
    private String name;
    private String emailAddress;
    private String mpName;
    private String mpConstituency;
    private String mpParty;
    private Boolean mpEngaged = false;
    private Boolean mpSympathetic = false;
    private String schemes;
    private String industry;
    private String howDidYouHearAboutLcag;
    private Boolean memberOfBigGroup = false;
    private String bigGroupUsername;
    private Boolean processed;

    public Enquiry(String emailAddress, String name) {
        this.emailAddress = emailAddress;
        this.name = name;
    }

    public Enquiry(
            Long id,
            String emailAddress,
            String name,
            String mpName,
            String mpConstituency,
            String mpParty,
            Boolean mpEngaged,
            Boolean mpSympathetic,
            String schemes,
            String industry,
            String howDidYouHearAboutLcag,
            Boolean memberOfBigGroup,
            String bigGroupUsername,
            Boolean processed) {
        this.id = id;
        this.emailAddress = emailAddress;
        this.name = name;
        this.mpName = mpName;
        this.mpConstituency = mpConstituency;
        this.mpParty = mpParty;
        this.mpEngaged = mpEngaged;
        this.mpSympathetic = mpSympathetic;
        this.schemes = schemes;
        this.industry = industry;
        this.howDidYouHearAboutLcag = howDidYouHearAboutLcag;
        this.memberOfBigGroup = memberOfBigGroup;
        this.bigGroupUsername = bigGroupUsername;
        this.processed = processed;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getName() {
        return name;
    }


    public Long getId() {
        return id;
    }

    public String getMpName() {
        return mpName;
    }

    public String getMpConstituency() {
        return mpConstituency;
    }

    public String getMpParty() {
        return mpParty;
    }

    public Boolean getMpEngaged() {
        return mpEngaged;
    }

    public Boolean getMpSympathetic() {
        return mpSympathetic;
    }

    public String getSchemes() {
        return schemes;
    }

    public String getIndustry() {
        return industry;
    }

    public String getHowDidYouHearAboutLcag() {
        return howDidYouHearAboutLcag;
    }

    public Boolean getMemberOfBigGroup() {
        return memberOfBigGroup;
    }

    public String getBigGroupUsername() {
        return bigGroupUsername;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Boolean getProcessed() {
        return processed;
    }
}
