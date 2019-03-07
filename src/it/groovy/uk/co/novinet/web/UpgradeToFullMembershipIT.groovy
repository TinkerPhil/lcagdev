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
    }

    def "guest member verification flow when guest has no docs"() {
        given:
            assert getEmails(GUEST_EMAIL_ADDRESS, "Inbox").size() == 0
            insertUser(1, "newguest", GUEST_EMAIL_ADDRESS, "John Smith", 8, true)
            go("http://admin:lcag@localhost:8282")
            at DashboardPage


        when: "set the user docs as verified"
            switchToMemberTabIfNecessaryAndAssertGridHasNRows(browser, 2)
            memberGridHmrcLetterCheckedTds.find("input")[1].click()
            memberGridIdentityCheckedTds.find("input")[1].click()
            memberGridVerifiedOnTds[1].find("input").value(new SimpleDateFormat("dd/MM/yyyy").format(new Date()))
            memberGridVerifiedByTds[1].find("input").value("RG")
            memberGridActionButtonTds.find("button")[1].click()
            waitFor { toastSuccess.text() == "Updated successfully" }

        then: "we set their group to 'Registered'"
            memberGridGroupSelectTds.find("select").value("Registered")
            memberGridActionButtonTds.find("button")[0].click()
            waitFor { toastSuccess.text() == "Updated successfully" }

        and: "member receives email saying they are now a full member"
            checkboxValue(memberGridHmrcLetterCheckedTds[1].find("input")) == true
            checkboxValue(memberGridIdentityCheckedTds[1].find("input")) == true
            memberGridVerifiedOnTds[1].find("input").value() == new SimpleDateFormat("dd/MM/yyyy").format(new Date())
            memberGridVerifiedByTds[1].find("input").value() == "RG"
            waitFor(10) { getEmails(GUEST_EMAIL_ADDRESS, "Inbox").size() == 1 }
            getEmails(GUEST_EMAIL_ADDRESS, "Inbox")[0].content.contains("Dear John Smith, You have now been upgraded to full membership. Welcome to being a full member of the LCAG and thank you for increasing our number and making us even stronger. If you would like to volunteer any support, we would be delighted to hear from you. We have a diverse spectrum of abilities and together we complement each other. If you know of anyone also affected by the Retrospective Loan Charge please direct them to us as our strength is in unity. A reminder of your full Membership Benefits: - Thousands of colleagues all working together for each one of us - How to contribute by being the 'A' in LCAG - Being informed about what is happening and what needs to be done - Access to a wealth of knowledge and shared experience - HUGE support network - Secure, discreet and totally confidential Forum access. - Specialist chat groups (using Telegram, details here https://forum.hmrcloancharge.info/showthread.php?tid=906) - How to contact your local MP to support you and us. - How to create an Impact Statement to support communications to your MP - Letting you know if there are other LCAG members in your constituency. In addition, Social Media is an excellent tool in our armoury. Use Social Media as much as you can for us to unite. Links are: - FaceBook - https://en-gb.facebook.com/pages/category/Community/Loan-Charge-Action-Group-906797766164348/ - Twitter - (https://twitter.com/LCAG_2019) @LCAG_2019. If you create a new username try to relate it to the loan charge. If you search \"#2019loancharge\" you will see relevant tweets and who you can follow, comment on, disagree with. Don't worry if you are new and have a lack of followers. They will build up quickly. - Linkedin - https://www.linkedin.com/company/loan-charge-action-group You should now have full access to the LCAG members forum. A getting started guide is here: https://forum.hmrcloancharge.info/showthread.php?tid=341 Your forum username is: newguest If you can’t remember your forum account details, please use the forgotten password facility and a new password will be sent to you: https://forum.hmrcloancharge.info/member.php?action=lostpw Thank you, LCAG Membership Team")
            getEmails(GUEST_EMAIL_ADDRESS, "Inbox")[0].subject.contains("You are now a full member")
    }

}
