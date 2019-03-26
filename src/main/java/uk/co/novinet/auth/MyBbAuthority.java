package uk.co.novinet.auth;

import static java.lang.String.format;

public enum MyBbAuthority {

    GUESTS("Guests", 1, false, true, false),
    REGISTERED("Registered", 2, false, false, false),
    SUPER_MODERATORS("Super Moderators", 3, true, false, false),
    ADMINISTRATORS("Administrators", 4, true, false, false),
    AWAITING_ACTIVIATION("Awaiting Activation", 5, false, true, true),
    MODERATORS("Moderators", 6, true, false, false),
    BANNED("Banned", 7, false, false, true),
    LCAG_GUESTS("LCAG Guests", 8, false, true, false),
    LCAG_FFC_CONTRIBUTOR("LCAG FFC Contributor", 9, false, false, false),
    SUSPENDED("Suspended", 12, false, true, true),
    LCAG_DASHBOARD_ADMINISTRATOR("LCAG Dashboard Administrator", 13, true, false, false);

    private String friendlyName;
    private final long id;
    private final boolean canAuthenticate;
    private boolean preRegisteredGroup;
    private boolean blocked;

    MyBbAuthority(String friendlyName, long id, boolean canAuthenticate, boolean preRegisteredGroup, boolean blocked) {
        this.friendlyName = friendlyName;
        this.id = id;
        this.canAuthenticate = canAuthenticate;
        this.preRegisteredGroup = preRegisteredGroup;
        this.blocked = blocked;
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

    public long getId() {
        return id;
    }

    public boolean canAuthenticate() {
        return canAuthenticate;
    }

    public boolean isPreRegisteredGroup() {
        return preRegisteredGroup;
    }

    public boolean isBlocked() {
        return blocked;
    }
}
