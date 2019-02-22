package uk.co.novinet.e2e;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static uk.co.novinet.e2e.TestUtils.*;

public class ResendFailedBankTransactionEmailsIT {

    private String contributorEmailAddress;
    private String contributorUsername;

    @BeforeClass
    public static void beforeClass() throws Exception {
        setupDatabaseSchema();
    }

    @Before
    public void before() {
        runSqlScript("sql/delete_all_users.sql");
        runSqlScript("sql/delete_all_bank_transactions.sql");

        contributorUsername = "contributor" + System.currentTimeMillis();
        contributorEmailAddress = contributorUsername + "@victim.com";

        deleteAllMessages(LCAG_INBOX_EMAIL_ADDRESS);
        deleteAllMessages(contributorEmailAddress);
        deleteMailFolder("History", LCAG_INBOX_EMAIL_ADDRESS);
        createMailFolder("History", LCAG_INBOX_EMAIL_ADDRESS);
    }

    @After
    public void teardown() {
        deleteAllMessages(LCAG_INBOX_EMAIL_ADDRESS);
        deleteAllMessages(contributorEmailAddress);
        deleteMailFolder("History", LCAG_INBOX_EMAIL_ADDRESS);
    }

    @Test
    public void sendsBankTransactionEmailWhenEmailNotAlreadySent() throws Exception {
        insertUser(1, contributorUsername, contributorEmailAddress, "Testy Test", 8);
        insertBankTransaction(1, contributorUsername, false);
        assertEquals(1, getUserRows().size());

        waitForNEmailsToAppearInFolder(1, "Inbox", contributorEmailAddress);

        assertEquals(1, getEmails(contributorEmailAddress, "Inbox").size());

        getEmails(contributorEmailAddress, "Inbox").get(0).getContent().contains("Dear Testy Test, Your donation of £50 has now been received. You will be upgraded to full membership as soon as we have reconciled the payment. Note there can be a lag of 1-2 days before this occurs so please be patient. Thank you, LCAG Membership Team");
        getEmails(contributorEmailAddress, "Inbox").get(0).getContent().contains("Your payment has been received");
    }

    @Test
    public void doesNotSendBankTransactionEmailWhenEmailAlreadySent() throws Exception {
        insertUser(1, contributorUsername, contributorEmailAddress, "Testy Test", 8);
        insertBankTransaction(1, contributorUsername, true);
        assertEquals(1, getUserRows().size());

        sleep(5000);

        assertEquals(0, getEmails(contributorEmailAddress, "Inbox").size());
    }

    private void insertBankTransaction(int userId, String username, boolean emailSent) {
        runSqlUpdate("INSERT INTO `i7b0_bank_transactions` (`id`, `date`, `description`, `amount`, `running_balance`, `counter_party`, `reference`, " +
                "`transaction_index_on_day`, `payment_source`, `user_id`, `email_sent`) VALUES ( 1, '" + unixTime() + "', 'test bank transaction', " +
                "50, 100, 'Mr Smith', '" + username + "', 0, 'SANTANDER', '" + userId + "', " + (emailSent ? "1" : "0") + ")");
    }

}
