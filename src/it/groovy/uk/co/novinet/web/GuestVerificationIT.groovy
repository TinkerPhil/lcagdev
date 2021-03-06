package uk.co.novinet.web

import geb.spock.GebSpec
import uk.co.novinet.e2e.TestSftpService

import java.text.SimpleDateFormat

import static uk.co.novinet.e2e.TestUtils.*
import static uk.co.novinet.web.GebTestUtils.*

class GuestVerificationIT extends GebSpec {

    static def GUEST_EMAIL_ADDRESS = "newguest@test.com"
    static TestSftpService testSftpService;

    def setup() {
        setupDatabaseSchema()
        deleteAllMessages(GUEST_EMAIL_ADDRESS);
        testSftpService = new TestSftpService()
        testSftpService.removeAllDocsForEmailAddress(GUEST_EMAIL_ADDRESS)
    }

    def "add note"() {
        given:
            insertUser(1, "newguest", GUEST_EMAIL_ADDRESS, "John Smith", 8, true)
            go("http://admin:lcag@${dashboardHost()}:${dashboardPort()}")
            at DashboardPage

        when: "when we click on the verification tab we can see the guest we're waiting to verify"
            switchToGuestVerificationTabIfNecessaryAndAssertGridHasNRows(browser, 1)

        and: "when we click on the first grid verification button"
            gridVerificationButtons.click()

        then: "document verification modal appears, guest has uploaded no docs"
            waitFor { documentVerificationModal.displayed }

        when: "when i click on the add note button without entering a note value"
            addNoteButton.click()

        then: "we get a validation error and modal is still displayed"
            waitFor(10) { toastError.displayed }
            assert toastError.text() == "Please enter a note"
            documentVerificationModal.displayed

        when: "we enter a note and click the add note button"
            notesInput.value("some note about something")
            waitFor(20) {
                $("div.toast.toast-info").displayed == false &&
                        $("div.toast.toast-error").displayed == false &&
                        $("div.toast.toast-success").displayed == false
            }
            addNoteButton.click()

        then: "we get a toast success message"
            waitFor(20) { toastSuccess.text() == "Updated successfully" }

        and: "modal closes"
            waitFor { documentVerificationModal.displayed == false }

        and: "the guest is still in the 'to verify' grid"
            waitFor { verificationGridRows.size() == 2 }

        and: "the note appears alongside the user in the grid"
            waitFor { verificationGridNotesTds[0].text() == "some note about something" }
    }

    def "guest member verification flow when guest has no docs"() {
        given:
            assert getEmails(GUEST_EMAIL_ADDRESS, "Inbox").size() == 0
            insertUser(1, "newguest", GUEST_EMAIL_ADDRESS, "John Smith", 8, true)
            go("http://admin:lcag@${dashboardHost()}:${dashboardPort()}")
            at DashboardPage

        when: "wait for member grid to load and confirm it has the guest we inserted"
            switchToMemberTabIfNecessaryAndAssertGridHasNRows(browser, 2)

        then: "when we click on the verification tab we can see the guest we're waiting to verify"
            switchToGuestVerificationTabIfNecessaryAndAssertGridHasNRows(browser, 1)

        when: "when we click on the first grid verification button"
            gridVerificationButtons.click()

        then: "document verification modal appears, guest has uploaded no docs"
            waitFor { documentVerificationModal.displayed }
            waitFor { documentVerificationTarget.text() ==  "No documents found for this member" }

        when: "when i click on the verification button without entering a 'verified by' value"
            confirmVerifyButton.click()

        then: "we get a validation error and modal is still displayed"
            waitFor { toastError.displayed }
            assert toastError.text() == "Please enter your initials in the 'Verified By' input field"
            documentVerificationModal.displayed

        when: "we enter verified by initials and click verify"
            verifiedByInput.value("RG")
            confirmVerifyButton.click()

        then: "we get a toast success message"
            waitFor(10) { toastSuccess.text() == "Updated successfully" }

        and: "modal closes"
            waitFor { documentVerificationModal.displayed == false }

        and: "the guest disappears from the 'to verify' grid"
            waitFor { verificationGridRows.size() == 1 }

        and: "the guest is now a verified member"
            switchToMemberTabIfNecessaryAndAssertGridHasNRows(browser, 2)
            memberGridNameTds[1].find("input").value() == "John Smith"
            checkboxValue(memberGridHmrcLetterCheckedTds[1].find("input")) == true
            checkboxValue(memberGridIdentityCheckedTds[1].find("input")) == true
            memberGridVerifiedOnTds[1].find("input").value() == new SimpleDateFormat("dd/MM/yyyy").format(new Date())
            memberGridVerifiedByTds[1].find("input").value() == "RG"

        and: "new member receives email saying their docs have been verified"
            waitFor { getEmails(GUEST_EMAIL_ADDRESS, "Inbox").size() == 1 }
            getEmails(GUEST_EMAIL_ADDRESS, "Inbox")[0].content.contains("Dear John Smith We have now verified your ID and scheme documentation (or Big Group account details if you are an existing Big Group member). You can access the forum here: http://forum.hmrcloancharge.info/ Your forum username is: " + getUserRows().get(1).getUsername() + " If you can’t remember your forum account details, please use the forgotten password facility and a new password will be sent to you: https://forum.hmrcloancharge.info/member.php?action=lostpw NOTE: Although you have been verified, you will still be a Guest member until we receive your donation and reconcile your payment. Regards, LCAG Membership Team")
    }

    def "guest member verification flow when guest has 2 docs"() {
        given:
            testSftpService.uploadFileForEmailAddress(GUEST_EMAIL_ADDRESS, "Identification Document")
            testSftpService.uploadFileForEmailAddress(GUEST_EMAIL_ADDRESS, "HMRC Letter Document")
            assert testSftpService.getAllDocumentsForEmailAddress(GUEST_EMAIL_ADDRESS).size() == 2
            assert getEmails(GUEST_EMAIL_ADDRESS, "Inbox").size() == 0
            insertUser(1, "newguest", GUEST_EMAIL_ADDRESS, "John Smith", 8, true)
            go("http://admin:lcag@${dashboardHost()}:${dashboardPort()}")
            at DashboardPage

        when: "wait for member grid to load and confirm it has the guest we inserted"
            switchToMemberTabIfNecessaryAndAssertGridHasNRows(browser, 2)

        then: "when we click on the verification tab we can see the guest we're waiting to verify"
            switchToGuestVerificationTabIfNecessaryAndAssertGridHasNRows(browser, 1)

        when: "when we click on the first grid verification button"
            gridVerificationButtons.click()

        then: "document verification modal appears, guest has uploaded no docs"
            waitFor { documentVerificationModal.displayed }
            waitFor { documentLinksToVerify.size() ==  2 }
            documentLinksToVerify[0].text() == "HMRC Letter Document"
            documentLinksToVerify[1].text() == "Identification Document"

        when: "we enter verified by initials and click verify"
            verifiedByInput.value("RG")
            confirmVerifyButton.click()

        then: "we get a toast success message"
            waitFor(10) { toastSuccess.text() == "Updated successfully" }

        and: "modal closes"
            waitFor { documentVerificationModal.displayed == false }

        and: "the guest disappears from the 'to verify' grid"
            waitFor { verificationGridRows.size() == 1 }

            and: "the guest is now a verified member"
            switchToMemberTabIfNecessaryAndAssertGridHasNRows(browser, 2)
            memberGridNameTds[1].find("input").value() == "John Smith"
            checkboxValue(memberGridHmrcLetterCheckedTds[1].find("input")) == true
            checkboxValue(memberGridIdentityCheckedTds[1].find("input")) == true
            memberGridVerifiedOnTds[1].find("input").value() == new SimpleDateFormat("dd/MM/yyyy").format(new Date())
            memberGridVerifiedByTds[1].find("input").value() == "RG"

        and: "new member receives email saying their docs have been verified"
            waitFor { getEmails(GUEST_EMAIL_ADDRESS, "Inbox").size() == 1 }
            getEmails(GUEST_EMAIL_ADDRESS, "Inbox")[0].content.contains("Dear John Smith We have now verified your ID and scheme documentation (or Big Group account details if you are an existing Big Group member). You can access the forum here: http://forum.hmrcloancharge.info/ Your forum username is: " + getUserRows().get(1).getUsername() + " If you can’t remember your forum account details, please use the forgotten password facility and a new password will be sent to you: https://forum.hmrcloancharge.info/member.php?action=lostpw NOTE: Although you have been verified, you will still be a Guest member until we receive your donation and reconcile your payment. Regards, LCAG Membership Team")

        and: "the docs have been deleted"
            assert testSftpService.getAllDocumentsForEmailAddress(GUEST_EMAIL_ADDRESS).size() == 0
    }
}
