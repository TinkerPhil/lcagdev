package uk.co.novinet.web

import geb.spock.GebSpec
import uk.co.novinet.auth.MyBbPasswordEncoder

import static java.util.Collections.singletonList
import static uk.co.novinet.e2e.TestUtils.*

class AuthenticationIT extends GebSpec {

    public static final int USER_GROUP_REGISTERED = 2
    public static final int USER_GROUP_ADMINISTRATORS = 4
    public static final int USER_GROUP_MODERATORS = 6
    public static final int USER_GROUP_SUPER_MODERATORS = 3
    public static final int USER_GROUP_LCAG_DASHBOARD_ADMINISTRATOR = 13

    def setup() {
        setupDatabaseSchema()
        runSqlScript("sql/delete_all_users.sql")
    }

    def "can login when main group is administrator"() {
        given:
        insertUser(9999, "user", "user@lcag.com", "user", USER_GROUP_ADMINISTRATORS, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")

        expect:
        assert getRequestStatusCode("http://localhost:8282", "user", "lcag") == 200
    }

    def "can login when main group is  lcag dashboard administrator"() {
        given:
        insertUser(9999, "user", "user@lcag.com", "user", USER_GROUP_LCAG_DASHBOARD_ADMINISTRATOR, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")

        expect:
        assert getRequestStatusCode("http://localhost:8282", "user", "lcag") == 200
    }

    def "can login when main group is super moderator"() {
        given:
        insertUser(9999, "user", "user@lcag.com", "user", USER_GROUP_SUPER_MODERATORS, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")

        expect:
        assert getRequestStatusCode("http://localhost:8282", "user", "lcag") == 200
    }

    def "can login when main group is moderator"() {
        given:
        insertUser(9999, "user", "user@lcag.com", "user", USER_GROUP_MODERATORS, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")

        expect:
        assert getRequestStatusCode("http://localhost:8282", "user", "lcag") == 200
    }

    def "cannot login when main group is registered"() {
        given:
        insertUser(9999, "user", "user@lcag.com", "user", USER_GROUP_REGISTERED, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")

        expect:
        assert getRequestStatusCode("http://localhost:8282", "user", "lcag") == 403
    }

    def "can login when main group is registered but additional group contains administrator"() {
        given:
        insertUser(9999, "user", "user@lcag.com", "user", USER_GROUP_REGISTERED, singletonList(USER_GROUP_ADMINISTRATORS), true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")

        expect:
        assert getRequestStatusCode("http://localhost:8282", "user", "lcag") == 200
    }

    def "can login when main group is registered but additional group contains lcag dashboard administrator"() {
        given:
        insertUser(9999, "user", "user@lcag.com", "user", USER_GROUP_REGISTERED, singletonList(USER_GROUP_LCAG_DASHBOARD_ADMINISTRATOR), true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")

        expect:
        assert getRequestStatusCode("http://localhost:8282", "user", "lcag") == 200
    }

    def "can login when main group is registered but additional group contains  super moderator"() {
        given:
        insertUser(9999, "user", "user@lcag.com", "user", USER_GROUP_REGISTERED, singletonList(USER_GROUP_SUPER_MODERATORS), true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")

        expect:
        assert getRequestStatusCode("http://localhost:8282", "user", "lcag") == 200
    }

    def "can login when main group is registered but additional group contains  moderator"() {
        given:
        insertUser(9999, "user", "user@lcag.com", "user", USER_GROUP_REGISTERED, singletonList(USER_GROUP_MODERATORS), true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")

        expect:
        assert getRequestStatusCode("http://localhost:8282", "user", "lcag") == 200
    }


}
