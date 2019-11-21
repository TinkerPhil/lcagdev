package uk.co.novinet.auth;

import static java.lang.String.format;

public enum MyBbAuthority {

    // These strings MUST match the database entries in i7b0_usergroups!!!
    GUESTS("Guests", 1, false, true, false, false),
    REGISTERED("Registered", 2, false, false, false, true),
    SUPER_MODERATORS("Super Moderators", 3, true, false, false, true),
    ADMINISTRATORS("Administrators", 4, true, false, false, true),
    AWAITING_ACTIVIATION("Awaiting Activation", 5, false, true, true, false),
    MODERATORS("Moderators", 6, true, false, false, true),
    BANNED("Banned", 7, false, false, true, true),
    LCAG_GUESTS("LCAG Guests", 8, false, true, false, true),
    LCAG_FFC_CONTRIBUTOR("LCAG FFC Contributor", 9, false, false, false, false),
    SUSPENDED("Suspended", 12, false, true, true, true),
    LCAG_DASHBOARD_ADMINISTRATOR("LCAG Dashboard Administrator", 13, true, false, false, true),
    LCAG_DASHBOARD_API_USER("LCAG Dashboard API User", 14, true, false, false, true),
    LCAG_DASHBOARD_EXTRACT_USER("LCAG Dash Extract", 15, true, false, false, true),
    LCAG_DASHBOARD_MEMBER_USER("LCAG Dash Member", 16, true, false, false, true),
    LCAG_DASHBOARD_MPC_USER("LCAG Dash MPC", 17, true, false, false, true),
    LCAG_DASHBOARD_MP_USER("LCAG Dash MP", 18, true, false, false, true),
    LCAG_DASHBOARD_MPCD_USER("LCAG Dash MPCD", 19, true, false, false, true),

    UNKNOWN("UNNOWN", 99, false, false, false, false);

    private String friendlyName;
    private final long id;
    private final boolean canAuthenticate;
    private boolean preRegisteredGroup;
    private boolean blocked;
    private boolean selectableAsPrimaryGroup;

    MyBbAuthority(String friendlyName, long id, boolean canAuthenticate, boolean preRegisteredGroup, boolean blocked, boolean selectableAsPrimaryGroup) {
        this.friendlyName = friendlyName;
        this.id = id;
        this.canAuthenticate = canAuthenticate;
        this.preRegisteredGroup = preRegisteredGroup;
        this.blocked = blocked;
        this.selectableAsPrimaryGroup = selectableAsPrimaryGroup;
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

    public boolean isSelectableAsPrimaryGroup() {
        return selectableAsPrimaryGroup;
    }
}
