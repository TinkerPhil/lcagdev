package uk.co.novinet.web

import geb.spock.GebSpec
import spock.lang.Unroll
import uk.co.novinet.auth.MyBbPasswordEncoder

import static uk.co.novinet.e2e.TestUtils.*

class AuthenticationIT extends GebSpec {

    public static final int USER_GROUP_REGISTERED = 2
    public static final int USER_GROUP_ADMINISTRATORS = 4
    public static final int USER_GROUP_MODERATORS = 6
    public static final int USER_GROUP_SUPER_MODERATORS = 3
    public static final int USER_GROUP_LCAG_DASHBOARD_ADMINISTRATOR = 13
    public static final int USER_GROUP_LCAG_FFC_CONTRIBUTOR = 9
    public static final int USER_GROUP_LCAG_GUESTS = 8
    public static final int USER_GROUP_BANNED = 7
    public static final int USER_GROUP_SUSPENDED = 12

    def setup() {
        setupDatabaseSchema()
        runSqlScript("sql/delete_all_users.sql")
    }

    @Unroll
    def "can login to dashboard with main group: #mainGroup and additional groups: #additionalGroups"() {
        given:
        insertUser(9999, "user", "user@lcag.com", "user", mainGroup, additionalGroups as List, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")

        expect:
        assert getRequestStatusCode("http://localhost:8282", "user", "lcag") == httpCode

        where:
        mainGroup                               | additionalGroups                                                           | httpCode
        USER_GROUP_ADMINISTRATORS               | []                                                                         | 200
        USER_GROUP_MODERATORS                   | []                                                                         | 200
        USER_GROUP_SUPER_MODERATORS             | []                                                                         | 200
        USER_GROUP_LCAG_DASHBOARD_ADMINISTRATOR | []                                                                         | 200
        USER_GROUP_REGISTERED                   | [USER_GROUP_ADMINISTRATORS]                                                | 200
        USER_GROUP_REGISTERED                   | [USER_GROUP_MODERATORS]                                                    | 200
        USER_GROUP_REGISTERED                   | [USER_GROUP_SUPER_MODERATORS]                                              | 200
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_DASHBOARD_ADMINISTRATOR]                                  | 200
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_FFC_CONTRIBUTOR, USER_GROUP_ADMINISTRATORS]               | 200
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_FFC_CONTRIBUTOR, USER_GROUP_MODERATORS]                   | 200
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_FFC_CONTRIBUTOR, USER_GROUP_SUPER_MODERATORS]             | 200
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_FFC_CONTRIBUTOR, USER_GROUP_LCAG_DASHBOARD_ADMINISTRATOR] | 200
    }

    @Unroll
    def "cannot login to dashboard with main group: #mainGroup and additional groups: #additionalGroups"() {
        given:
        insertUser(9999, "user", "user@lcag.com", "user", mainGroup, additionalGroups as List, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")

        expect:
        assert getRequestStatusCode("http://localhost:8282", "user", "lcag") == httpCode

        where:
        mainGroup                               | additionalGroups                                                                                 | httpCode
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_FFC_CONTRIBUTOR]                                                                | 403
        USER_GROUP_LCAG_GUESTS                  | [USER_GROUP_LCAG_FFC_CONTRIBUTOR]                                                                | 403
        USER_GROUP_REGISTERED                   | []                                                                                               | 403
        USER_GROUP_LCAG_GUESTS                  | []                                                                                               | 403
        USER_GROUP_ADMINISTRATORS               | [USER_GROUP_BANNED]                                                                              | 401
        USER_GROUP_MODERATORS                   | [USER_GROUP_BANNED]                                                                              | 401
        USER_GROUP_SUPER_MODERATORS             | [USER_GROUP_BANNED]                                                                              | 401
        USER_GROUP_LCAG_DASHBOARD_ADMINISTRATOR | [USER_GROUP_BANNED]                                                                              | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_ADMINISTRATORS, USER_GROUP_BANNED]                                                   | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_MODERATORS, USER_GROUP_BANNED]                                                       | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_SUPER_MODERATORS, USER_GROUP_BANNED]                                                 | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_DASHBOARD_ADMINISTRATOR, USER_GROUP_BANNED]                                     | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_FFC_CONTRIBUTOR, USER_GROUP_ADMINISTRATORS, USER_GROUP_BANNED]                  | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_FFC_CONTRIBUTOR, USER_GROUP_MODERATORS, USER_GROUP_BANNED]                      | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_FFC_CONTRIBUTOR, USER_GROUP_SUPER_MODERATORS, USER_GROUP_BANNED]                | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_FFC_CONTRIBUTOR, USER_GROUP_LCAG_DASHBOARD_ADMINISTRATOR, USER_GROUP_BANNED]    | 401
        USER_GROUP_ADMINISTRATORS               | [USER_GROUP_SUSPENDED]                                                                           | 401
        USER_GROUP_MODERATORS                   | [USER_GROUP_SUSPENDED]                                                                           | 401
        USER_GROUP_SUPER_MODERATORS             | [USER_GROUP_SUSPENDED]                                                                           | 401
        USER_GROUP_LCAG_DASHBOARD_ADMINISTRATOR | [USER_GROUP_SUSPENDED]                                                                           | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_ADMINISTRATORS, USER_GROUP_SUSPENDED]                                                | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_MODERATORS, USER_GROUP_SUSPENDED]                                                    | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_SUPER_MODERATORS, USER_GROUP_SUSPENDED]                                              | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_DASHBOARD_ADMINISTRATOR, USER_GROUP_SUSPENDED]                                  | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_FFC_CONTRIBUTOR, USER_GROUP_ADMINISTRATORS, USER_GROUP_SUSPENDED]               | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_FFC_CONTRIBUTOR, USER_GROUP_MODERATORS, USER_GROUP_SUSPENDED]                   | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_FFC_CONTRIBUTOR, USER_GROUP_SUPER_MODERATORS, USER_GROUP_SUSPENDED]             | 401
        USER_GROUP_REGISTERED                   | [USER_GROUP_LCAG_FFC_CONTRIBUTOR, USER_GROUP_LCAG_DASHBOARD_ADMINISTRATOR, USER_GROUP_SUSPENDED] | 401
    }


}
