package uk.co.novinet.web

import geb.spock.GebSpec
import uk.co.novinet.e2e.TestSftpService

import static uk.co.novinet.e2e.TestUtils.*
import static uk.co.novinet.web.GebTestUtils.*

class BankTransactionAssignmentIT extends GebSpec {

    static def MEMBER_EMAIL_ADDRESS = "member@test.com"
    static TestSftpService testSftpService;

    def setup() {
        setupDatabaseSchema()
        deleteAllMessages(MEMBER_EMAIL_ADDRESS);
        runSqlScript("sql/delete_all_users.sql")
        runSqlScript("sql/delete_all_bank_transactions.sql")
        testSftpService = new TestSftpService()
        testSftpService.removeAllDocsForEmailAddress(MEMBER_EMAIL_ADDRESS)
    }

    def "manual bank transaction assignment triggers payment receipt email"() {
        given:
            insertUnassignedBankTransactionRow()
            assert getEmails(MEMBER_EMAIL_ADDRESS, "Inbox").size() == 0
            insertUser(1, "newguest", MEMBER_EMAIL_ADDRESS, "John Smith", 2, true)
            go("http://admin:lcag@localhost:8282")
            at DashboardPage

        and: "wait for member grid to load and confirm it has the guest we inserted"
            switchToMemberTabIfNecessaryAndAssertGridHasNRows(browser, 1)
            memberGridContributionAmountTds[0].find("input").value() == 0.00

        and: "wait for payments grid to load and confirm it has the transaction we inserted"
            paymentsTab.click()
            waitFor { paymentsGrid.displayed }
            switchToPaymentsTabIfNecessaryAndAssertGridHasNRows(browser, 1)

        when: "we assign the transaction to the member"
            $(".select2-selection__arrow").click()
            waitFor { select2FirstMemberChoice.displayed }
            select2FirstMemberChoice.click()

        then: "toast success message apears"
            waitFor { toastSuccess.displayed }
            assert toastSuccess.text() == "Updated successfully"

        and: "the payment amount has been assigned to the member"
            switchToMemberTabIfNecessaryAndAssertGridHasNRows(browser, 1)
            waitFor { memberGridContributionAmountTds[0].find("input").value() == "50.00" }

        and: "new member receives email saying their payment has been received"
            waitFor { getEmails(MEMBER_EMAIL_ADDRESS, "Inbox").size() == 1 }
            getEmails(MEMBER_EMAIL_ADDRESS, "Inbox")[0].content.contains("Dear John Smith, Your payment of £50 has now been received. Thank you, Richard Horsley Membership Team")
            getEmails(MEMBER_EMAIL_ADDRESS, "Inbox")[0].subject.contains("Your payment has been received")
    }

    private void insertUnassignedBankTransactionRow() {
        runSqlUpdate("INSERT INTO `i7b0_bank_transactions` (`id`, `date`, `description`, `amount`, `running_balance`, `counter_party`, `reference`, " +
                "`transaction_index_on_day`, `payment_source`) VALUES ( 1, '" + unixTime() + "', 'test bank transaction', " +
                "50, 100, 'Mr Smith', 'some_username', 0, 'SANTANDER')")
    }

}
