package uk.co.novinet.service.member;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class MemberMpCampaign {
    private Long id;
    private String name;
    private String mpName;
    private String email;
    private String username;
    private String usergroup;
    private Integer postnum;
    private Integer threadnum;
    private String lastvisit;
    private String schemes;
    private String lobbyingDayAttending;
    private String allowEmailShareStatus;
    private String sentInitialEmail;
    private String campaignNotes;
    private String meetingNext;
    private Integer meetingCount;
    private Integer letterCount;
    private Integer emailCount;
    private Integer involved;
    private String administratorName;

    public MemberMpCampaign() {}

    public MemberMpCampaign(
            Long id,
            String name,
            String mpName,
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
            String meetingNext,
            Integer meetingCount,
            Integer letterCount,
            Integer emailCount,
            Integer involved,
            String lobbyingDayAttending,
            String administratorName
            ) {
        this.id = id;
        this.name = name;
        this.mpName = mpName;
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
        this.setMeetingNext(meetingNext);
        this.meetingCount = meetingCount;
        this.letterCount = letterCount;
        this.emailCount = emailCount;
        this.involved = involved;
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
    public String getMeetingNext() { return meetingNext;}
    public void setMeetingNext(String meetingNext) { if( meetingNext == null || meetingNext.equals("null")) { meetingNext=""; } this.meetingNext= meetingNext; }
    public Integer getMeetingCount() { return meetingCount;}
    public void setMeetingCount(Integer meetingCount) { this.meetingCount= meetingCount; }
    public Integer getLetterCount() { return letterCount;}
    public void setLetterCount(Integer letterCount) { this.letterCount= letterCount; }
    public Integer getEmailCount() { return emailCount;}
    public void setEmailCount(Integer emailCount) { this.emailCount= emailCount; }
    public Integer getInvolved() { return involved;}
    public void setInvolved(Integer involved) { this.involved= involved;}

    public String getLobbyingDayAttending() { return lobbyingDayAttending; }
    public void setLobbyingDayAttending(String lobbyingDatAttending) { this.lobbyingDayAttending = lobbyingDayAttending; }
    public String getAdministratorName() { return administratorName;}
    public void setAdministratorName(String administratorName) { this.administratorName = administratorName; }

}
