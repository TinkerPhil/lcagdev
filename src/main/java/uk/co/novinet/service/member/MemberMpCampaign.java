package uk.co.novinet.service.member;

import java.time.Instant;
import java.util.Date;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class MemberMpCampaign {
    private Long id;
    private String name;
    private String mpName;
    private String mpConstituency;
    private String email;
    private String username;
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
    private String edmUrl;
    private String lobbyingDayAttending;
    private String administratorName;

    public MemberMpCampaign() {}

    public MemberMpCampaign(
            Long id,
            String name,
            String mpName,
            String mpConstituency,
            String email,
            String username,
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
            String edmUrl,
            String lobbyingDayAttending,
            String administratorName
            ) {
        this.id = id;
        this.name = name;
        this.mpName = mpName;
        this.mpConstituency = mpConstituency;
        this.email = email;
        this.username= username;
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
        this.edmUrl = edmUrl;
        this.lobbyingDayAttending = lobbyingDayAttending;

        this.administratorName = administratorName;
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
    public String getMpConstituency() { return mpConstituency;    }
    public void setMpConstituency(String mpConstituency) { this.mpConstituency = mpConstituency;    }
    public String getEmail() { return email;    }
    public void setEmail(String email) { this.email = email;    }

    public String getUsername() { return username;    }
    public void setUsername(String username) { this.username = username;    }
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
    public Integer getMeetingCount() { return meetingCount;}
    public void setMeetingCount(Integer meetingCount) { this.meetingCount= meetingCount; }
    public Integer getTelephoneCount() { return telephoneCount;}
    public void setTelephoneCount(Integer telephoneCount) { this.telephoneCount = telephoneCount; }
    public Integer getWrittenCount() { return writtenCount;}
    public void setWrittenCount(Integer writtenCount) { this.writtenCount = writtenCount; }
    public Integer getInvolved() { return involved;}
    public void setInvolved(Integer involved) { this.involved= involved;}
    public String getEdmUrl() { return edmUrl;}
    public void setEdmUrl(String edmUrl) { this.edmUrl= edmUrl; }

    public String getLobbyingDayAttending() { return lobbyingDayAttending; }
    public void setLobbyingDayAttending(String lobbyingDatAttending) { this.lobbyingDayAttending = lobbyingDayAttending; }
    public String getAdministratorName() { return administratorName;}
    public void setAdministratorName(String administratorName) { this.administratorName = administratorName; }

}
