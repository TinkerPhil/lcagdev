package uk.co.novinet.web

import geb.spock.GebSpec
import uk.co.novinet.e2e.TestSftpService

import java.text.SimpleDateFormat

import static uk.co.novinet.e2e.TestUtils.*
import static uk.co.novinet.web.GebTestUtils.*

class UpgradeToFullMembershipIT extends GebSpec {

    static def GUEST_EMAIL_ADDRESS = "newguest@test.com"

    def setup() {
        setupDatabaseSchema()
        deleteAllMessages(GUEST_EMAIL_ADDRESS);
        runSqlScript("sql/delete_all_users.sql")
    }

    def "guest member verification flow when guest has no docs"() {
        given:
            assert getEmails(GUEST_EMAIL_ADDRESS, "Inbox").size() == 0
            insertUser(1, "newguest", GUEST_EMAIL_ADDRESS, "John Smith", 8, true)
            go("http://admin:lcag@localhost:8282")
            at DashboardPage
        

        when: "set the user docs as verified"
            switchToMemberTabIfNecessaryAndAssertGridHasNRows(browser, 1)
            memberGridHmrcLetterCheckedTds.find("input").click()
            memberGridIdentityCheckedTds.find("input").click()
            memberGridVerifiedOnTds[0].find("input").value(new SimpleDateFormat("dd/MM/yyyy").format(new Date()))
            memberGridVerifiedByTds[0].find("input").value("RG")
            memberGridActionButtonTds.find("button")[0].click()
            waitFor { toastSuccess.text() == "Updated successfully" }

        then: "we set their group to 'Registered'"
            memberGridGroupSelectTds.find("select").value("Registered")
            memberGridActionButtonTds.find("button")[0].click()
            waitFor { toastSuccess.text() == "Updated successfully" }

        and: "member receives email saying they are now a full member"
            checkboxValue(memberGridHmrcLetterCheckedTds[0].find("input")) == true
            checkboxValue(memberGridIdentityCheckedTds[0].find("input")) == true
            memberGridVerifiedOnTds[0].find("input").value() == new SimpleDateFormat("dd/MM/yyyy").format(new Date())
            memberGridVerifiedByTds[0].find("input").value() == "RG"
            waitFor { getEmails(GUEST_EMAIL_ADDRESS, "Inbox").size() == 1 }
            getEmails(GUEST_EMAIL_ADDRESS, "Inbox")[0].content.contains("Dear John Smith, You have now been upgraded to full membership. You should now have full access to the LCAG members forum: https://forum.hmrcloancharge.info Your forum username is: newguest If you can’t remember your forum account details, please use the forgotten password facility and a new password will be sent to you: https://forum.hmrcloancharge.info/member.php?action=lostpw Thank you, Richard Horsley Membership Team")
            getEmails(GUEST_EMAIL_ADDRESS, "Inbox")[0].subject.contains("You are now a full member")
    }

}
