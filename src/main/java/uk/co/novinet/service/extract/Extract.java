package uk.co.novinet.service.extract;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class Extract {

    private String type;
    private String special;
    private String columns;
    private String mpName;
    private String mpConstituency;
    private String mpParty;
    private String mpTags;
    private String adminSig;
    private String name;
    private String email;
    private String username;
    private String tags;
    private String extraField1;
    private String extraValue1;
    private String extraField2;
    private String extraValue2;

    public Extract() {}

    public Extract(
            String type,
            String special,
            String columns,
            String mpName,
            String mpConstituency,
            String mpParty,
            String mpTags,
            String adminSig,
            String name,
            String email,
            String username,
            String tags,
            String extraField1,
            String extraValue1,
            String extraField2,
            String extraValue2
    ) {
        this.type = type;
        this.special = special;
        this.columns = columns;
        this.mpName = mpName;
        this.mpConstituency = mpConstituency;
        this.mpParty = mpParty;
        this.mpTags = mpTags;
        this.adminSig = adminSig;
        this.name = name;
        this.email = email;
        this.username = username;
        this.tags = tags;
        this.extraField1 = extraField1;
        this.extraValue1 = extraValue1;
        this.extraField2 = extraField2;
        this.extraValue2 = extraValue2;
    }

    public String getType() { return type;    }
    public void setType(String type) {        this.type = type;    }

    public String getSpecial() { return special;    }
    public void setSpecial(String special) {        this.special = special;    }

    public String getColumns() { return columns;    }
    public void setColumns(String columns) {        this.columns = columns;    }

    public String getMpName() {        return mpName;    }
    public void setMpName(String mpName) {        this.mpName = mpName;    }

    public String getMpConstituency() {        return mpConstituency;    }
    public void setMpConstituency(String mpConstituency) {        this.mpConstituency = mpConstituency;    }

    public String getMpParty() {        return mpParty;    }
    public void setMpParty(String mpParty) {        this.mpParty = mpParty;    }

    public String getMpTags() {        return mpTags;    }
    public void setMpTags(String mpTags) {        this.mpTags = mpTags;    }

    public String getAdminSig() {        return adminSig;    }
    public void setAdminSig(String adminSig) {        this.adminSig = adminSig;    }

    public String getName() {        return name;    }
    public void setName(String name) {        this.name = name;    }

    public String getEmail() {        return email;    }
    public void setEmail(String email) {        this.email = email;    }

    public String getUsername() {        return username;    }
    public void setUsername(String username) {        this.username = username;    }

    public String getTags() {        return tags;    }
    public void setTags(String tags) {        this.tags = tags;    }

    public String getExtraField1() {        return extraField1;    }
    public void setExtraField1(String extraField1) {        this.extraField1 = extraField1;    }
    public String getExtraValue1() {        return extraValue1;    }
    public void setExtraValue1(String extraValue1) {        this.extraValue1 = extraValue1;    }

    public String getExtraField2() {        return extraField2;    }
    public void setExtraField2(String extraField2) {        this.extraField2 = extraField2;    }
    public String getExtraValue2() {        return extraValue2;    }
    public void setExtraValue2(String extraValue2) {        this.extraValue2 = extraValue2;    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }
}
