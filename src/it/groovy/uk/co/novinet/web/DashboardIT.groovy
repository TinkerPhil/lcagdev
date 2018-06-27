package uk.co.novinet.web

import geb.spock.GebSpec

import java.text.SimpleDateFormat

import static uk.co.novinet.e2e.TestUtils.insertUser
import static uk.co.novinet.e2e.TestUtils.runSqlScript
import static uk.co.novinet.e2e.TestUtils.setupDatabaseSchema
import static uk.co.novinet.web.GebTestUtils.switchToGuestVerificationTabIfNecessaryAndAssertGridHasNRows
import static uk.co.novinet.web.GebTestUtils.switchToMemberTabIfNecessaryAndAssertGridHasNRows

class DashboardIT extends GebSpec {

    def setup() {
        setupDatabaseSchema()
        runSqlScript("sql/delete_all_users.sql");
    }

    def "guest member verification flow when guest has no docs"() {
        given:
            insertUser(1, "newguest", "newguest@test.com", "John Smith", 8, true)
            go("http://admin:lcag@localhost:8282")
            at DashboardPage

        when: "wait for member grid to load and confirm it has the guest we inserted"
            switchToMemberTabIfNecessaryAndAssertGridHasNRows(browser, 1)

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
            waitFor { toastSuccess.text() == "Updated successfully" }

        and: "modal closes"
            waitFor { documentVerificationModal.displayed == false }

        and: "the guest disappears from the 'to verify' grid"
            waitFor { verificationGridRows.size() == 1 }

        and: "the guest is now a verified member"
            switchToMemberTabIfNecessaryAndAssertGridHasNRows(browser, 1)
            memberGridNameTds[0].attr("title") == "John Smith"
            memberGridHmrcLetterCheckedTds[0].find("input").value() == true
            memberGridIdentificationCheckedTds[0].find("input").value() == true
            memberGridVerifiedOnTds[0].find("input").text() == new SimpleDateFormat("dd/MM/yyyy").format(new Date())
            memberGridVerifiedByTds[0].find("input").text() == "RG"
    }
}
