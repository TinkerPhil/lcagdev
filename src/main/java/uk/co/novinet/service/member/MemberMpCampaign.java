package uk.co.novinet.service.member;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class MemberMpCampaign {
    private Long id;
    private String name;
    private String mpName;
    private String party;
    private String mpConstituency;
    private String pCon;
    private String majority;
    private String email;
    private String username;
    private String bigGroupUsername;
    private String usergroup;
    private Integer postnum;
    private Integer threadnum;
    private String lastvisit;
    private String schemes;
    private String allowEmailShareStatus;
    private String sentInitialEmail;
    private String campaignNotes;
    private String telNo;
    private String tags;
    private Date meetingNext;
    private Integer meetingCount;
    private Integer telephoneCount;
    private Integer writtenCount;
    private Integer involved;
    private String lobbyingDayAttending;
    private String ministerialStatus;
    private String edmUrl;
    private String edmStatus;
    private String mpTelNo;
    private String mpTwitter;
    private String mpEmail;
    private String sharedCampaignEmails;
    private String privateCampaignEmails;
    private String adminName;
    private String adminUsername;
    private String adminSig;
    private String parliamentaryEmail;
    private String constituencyEmail;
    private String properName;

    public MemberMpCampaign() {}

    public MemberMpCampaign(
            Long id,
            String name,
            String mpName,
            String party,
            String mpConstituency,
            String pCon,
            String majority,
            String email,
            String username,
            String bigGroupUsername,
            String usergroup,
            Integer postnum,
            Integer threadnum,
            String lastvisit,
            String schemes,
            String allowEmailShareStatus,
            String sentInitialEmail,
            String campaignNotes,
            String telNo,
            String tags,
            Date meetingNext,
            Integer meetingCount,
            Integer telephoneCount,
            Integer writtenCount,
            Integer involved,
            String lobbyingDayAttending,
            String ministerialStatus,
            String edmUrl,
            String edmStatus,
            String mpTelNo,
            String mpTwitter,
            String mpEmail,
            String sharedCampaignEmails,
            String privateCampaignEmails,
            String parliamentaryEmail,
            String constituencyEmail,
            String properName,
            String adminUsername,
            String adminName,
            String adminSig
            ) {
        this.id = id;
        this.name = name;
        this.mpName = mpName;
        this.party= party;
        this.mpConstituency = mpConstituency;
        this.majority = majority;
        this.pCon = pCon;
        this.email = email;
        this.username= username;
        this.bigGroupUsername = bigGroupUsername;
        this.usergroup = usergroup;
        this.postnum = postnum;
        this.threadnum = threadnum;
        this.lastvisit = lastvisit;
        this.schemes = schemes;
        this.allowEmailShareStatus = allowEmailShareStatus;
        this.sentInitialEmail = sentInitialEmail;
        this.setCampaignNotes(campaignNotes);
        this.setTelNo(telNo);
        this.setTags(tags);
        this.meetingNext = meetingNext;
        this.meetingCount = meetingCount;
        this.telephoneCount = telephoneCount;
        this.writtenCount = writtenCount;
        this.involved = involved;
        this.lobbyingDayAttending = lobbyingDayAttending;

        this.ministerialStatus = ministerialStatus;
        this.edmUrl = edmUrl;
        this.edmStatus = edmStatus;
        this.mpTelNo = mpTelNo;
        this.mpTwitter = mpTwitter;
        this.mpEmail = mpEmail;
        this.sharedCampaignEmails = sharedCampaignEmails;
        this.privateCampaignEmails = privateCampaignEmails;
        this.parliamentaryEmail = parliamentaryEmail;
        this.constituencyEmail = constituencyEmail;
        this.properName = properName;

        this.adminUsername = adminUsername;
        this.adminName = adminName;
        this.adminSig = adminSig;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) { this.id = id; }
    public String getName() {
        return name;
    }
    public void setName(String name) { this.name = name; }
    public String getMpName() { return mpName;    }
    public void setMpName(String mpName) { this.mpName = mpName;    }
    public String getParty() { return party;    }
    public void setParty(String party) { this.party= party;    }
    public String getMpConstituency() { return mpConstituency;    }
    public void setMpConstituency(String mpConstituency) { this.mpConstituency = mpConstituency;    }
    public String getpCon() { return this.pCon; }
    public void setpCon(String pCon) {this.pCon = pCon; }
    public String getMajority() { return majority;    }
    public void setMajority(String majority) { this.majority= majority;    }
    public String getEmail() { return email;    }
    public void setEmail(String email) { this.email = email;    }

    public String getUsername() { return username;    }
    public void setUsername(String username) { this.username = username;    }
    public String getBigGroupUsername() { return bigGroupUsername;    }
    public void setBigGroupUsername(String bigGroupUsername) { this.bigGroupUsername= bigGroupUsername;    }
    public String getUsergroup() { return usergroup;    }
    public void setUsergroup(String usergroup) { this.usergroup = usergroup;    }
    public Integer  getPostnum() { return postnum;    }
    public void setPostnum(Integer postnum) { this.postnum= postnum;    }
    public Integer getThreadnum() { return threadnum;    }
    public void setThreadnum(Integer threadnum) { this.threadnum = threadnum;    }
    public String getLastvisit() { return lastvisit;    }
    public void setLastvisit(String lastvisit) { this.lastvisit= lastvisit;    }
    public String getSchemes() { return schemes;    }
    public void setSchemes(String schemes) { this.schemes = schemes;    }

    public String getAllowEmailShareStatus() { return allowEmailShareStatus; }
    public void setAllowEmailShareStatus(String allowEmailShareStatus) { this.allowEmailShareStatus = allowEmailShareStatus; }
    public String getSentInitialEmail() { return sentInitialEmail;}
    public void setSentInitialEmail(String sentInitialEmail) { this.sentInitialEmail = sentInitialEmail; }
    public String getCampaignNotes() { return campaignNotes;}
    public void setCampaignNotes(String campaignNotes) { if(campaignNotes == null || campaignNotes.equals("null")) { campaignNotes = ""; } this.campaignNotes = campaignNotes; }
    public String getTelNo() { return telNo;}
    public void setTelNo(String telNo) { if(telNo== null || telNo.equals("null")) { telNo = ""; } this.telNo= telNo; }
    public String getTags() { return tags;}
    public void setTags(String tags) { if(tags== null || tags.equals("null")) { tags = ""; } this.tags= tags; }
    public Date getMeetingNext() { return meetingNext;}
    public void setMeetingNext(Date meetingNext) { this.meetingNext = meetingNext; }
    public void setMeetingNext(String meetingNext) throws Exception { System.out.println("Boo"); this.meetingNext = new SimpleDateFormat("yyyyMMdd").parse(meetingNext); }
    public Integer getMeetingCount() { return meetingCount;}
    public void setMeetingCount(Integer meetingCount) { this.meetingCount= meetingCount; }
    public Integer getTelephoneCount() { return telephoneCount;}
    public void setTelephoneCount(Integer telephoneCount) { this.telephoneCount = telephoneCount; }
    public Integer getWrittenCount() { return writtenCount;}
    public void setWrittenCount(Integer writtenCount) { this.writtenCount = writtenCount; }
    public Integer getInvolved() { return involved;}
    public void setInvolved(Integer involved) { this.involved= involved;}

    public String getLobbyingDayAttending() { return lobbyingDayAttending; }
    public void setLobbyingDayAttending(String lobbyingDatAttending) { this.lobbyingDayAttending = lobbyingDayAttending; }

    public String getMinisterialStatus() { return ministerialStatus;}
    public void setMinisterialStatus(String ministerialStatus) { this.ministerialStatus=ministerialStatus; }
    public String getEdmUrl() { return edmUrl;}
    public void setEdmUrl(String edmUrl) { this.edmUrl= edmUrl; }
    public String getEdmStatus() { return edmStatus;}
    public void setEdmStatus(String edmStatus) { this.edmStatus= edmStatus; }
    public String getMpTelNo() { return mpTelNo;}
    public void setMpTelNo(String mpTelNo) { this.mpTelNo= mpTelNo; }
    public String getMpTwitter() { return mpTwitter;}
    public void setMpTwitter(String mpTwitter) { this.mpTwitter= mpTwitter; }
    public String getMpEmail() { return mpEmail;}
    public void setMpEmail(String mpEmail) { this.mpEmail= mpEmail; }
    public String getSharedCampaignEmails() { return sharedCampaignEmails;}
    public void setSharedCampaignEmails(String sharedCampaignEmails) { this.sharedCampaignEmails= sharedCampaignEmails; }
    public String getPrivateCampaignEmails() { return privateCampaignEmails;}
    public void setPrivateCampaignEmails(String privateCampaignEmails) { this.privateCampaignEmails= privateCampaignEmails; }
    public String getParliamentaryEmail() { return parliamentaryEmail;}
    public void setParliamentaryEmail(String parliamentaryEmail) { this.parliamentaryEmail= parliamentaryEmail; }
    public String getConstituencyEmail() { return constituencyEmail;}
    public void setConstituencyEmail(String constituencyEmail) { this.constituencyEmail= constituencyEmail; }
    public String getProperName() { return properName;}
    public void setProperName(String properName) { this.properName= properName; }

    public String getAdminUsername() { return adminUsername;}
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    public String getAdminName() { return adminName;}
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public String getAdminSig() { return adminSig;}
    public void setAdminSig(String adminSig) { this.adminSig= adminSig; }

}
