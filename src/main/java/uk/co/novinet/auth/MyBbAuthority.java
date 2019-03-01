package uk.co.novinet.auth;

import static java.lang.String.format;

public enum MyBbAuthority {

    GUESTS("Guests"),
    REGISTERED("Registered"),
    SUPER_MODERATORS("Super Moderators"),
    ADMINISTRATORS("Administrators"),
    AWAITING_ACTIVIATION("Awaiting Activation"),
    MODERATORS("Moderators"),
    BANNED("Banned"),
    LCAG_GUESTS("LCAG Guests"),
    LCAG_FFC_CONTRIBUTOR("LCAG FFC Contributor"),
    SUSPENDED("Suspended"),
    LCAG_DASHBOARD_ADMINISTRATOR("LCAG Dashboard Administrator");

    private String friendlyName;

    MyBbAuthority(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public static MyBbAuthority fromFriendlyName(String friendlyName) throws IllegalArgumentException {
        for (MyBbAuthority myBbRole : MyBbAuthority.values()) {
            if (friendlyName.equals(myBbRole.getFriendlyName())) {
                return myBbRole;
            }
        }

        throw new IllegalArgumentException(format("Unknown mybb role: %s", friendlyName));
    }
}
