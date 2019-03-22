package uk.co.novinet.web

import groovy.json.JsonSlurper
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import uk.co.novinet.auth.MyBbPasswordEncoder

import java.security.NoSuchAlgorithmException

import static org.junit.Assert.assertEquals
import static uk.co.novinet.e2e.TestUtils.*

class MemberPaymentCalculationIT {

    @BeforeClass
    static void beforeClass() throws Exception {
        setupDatabaseSchema()
    }

    @Before
    void setup() throws NoSuchAlgorithmException {
        runSqlScript("sql/delete_all_users.sql")
        runSqlScript("sql/delete_all_bank_transactions.sql")
        runSqlUpdate("truncate i7b0_userFundingSummary")
        insertUser(9999, "admin", "admin@lcag.com", "Administrators", 4, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")
        insertUser(1, "user1", "user1@lcag.com", "John Smith", 4, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")
        insertUser(2, "user2", "user2@lcag.com", "Tim Cook", 4, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")
        insertUser(3, "user3", "user3@lcag.com", "Sarah Brown", 4, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")
    }

    @Test
    void memberContributionAmountIsDerivedFromUserFundingSummaryTable() throws IOException {
        insertUserFundingContributionRow(1, "100.00", "0.00")
        insertUserFundingContributionRow(2, "200.00", "0.00")
        insertUserFundingContributionRow(3, "300.00", "0.00")

        def members = new JsonSlurper().parseText(getRequest("http://localhost:8282/member", "admin", "lcag")).rows

        assertEquals(1, members[0].id)
        assertEquals(100.00, members[0].contributionAmount)
        assertEquals(2, members[1].id)
        assertEquals(200.00, members[1].contributionAmount)
        assertEquals(3, members[2].id)
        assertEquals(300.00, members[2].contributionAmount)
    }

    @Test
    void memberContributionAmountIsDerivedFromUserFundingSummaryTableWhenThereAreMultipleContributionsForEachMember() throws IOException {
        insertUserFundingContributionRow(1, "100.00", "0.00")
        insertUserFundingContributionRow(1, "100.00", "0.00")
        insertUserFundingContributionRow(2, "200.00", "0.00")
        insertUserFundingContributionRow(2, "200.00", "0.00")
        insertUserFundingContributionRow(3, "300.00", "0.00")
        insertUserFundingContributionRow(3, "300.00", "0.00")

        def members = new JsonSlurper().parseText(getRequest("http://localhost:8282/member", "admin", "lcag")).rows

        assertEquals(1, members[0].id)
        assertEquals(200.00, members[0].contributionAmount)
        assertEquals(2, members[1].id)
        assertEquals(400.00, members[1].contributionAmount)
        assertEquals(3, members[2].id)
        assertEquals(600.00, members[2].contributionAmount)
    }

    void insertUserFundingContributionRow(int userId, String lcagAmount, String ffcAmount) {
        runSqlUpdate("INSERT INTO i7b0_userFundingSummary (uid, lcagAmount, ffcAmount) VALUES " +
                "('" + userId + "', '" + lcagAmount + "', '" + ffcAmount + "')")
    }

}
