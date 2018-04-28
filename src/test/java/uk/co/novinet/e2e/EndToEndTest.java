package uk.co.novinet.e2e;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static uk.co.novinet.e2e.TestUtils.*;

public class EndToEndTest {

    private String enquirerEmailAddress;

    @Before
    public void setup() throws Exception {
        deleteAllMessages(LCAG_INBOX_EMAIL_ADDRESS);

        int sqlRetryCounter = 0;
        boolean needToRetry = true;

        while (needToRetry && sqlRetryCounter < 20) {
            try {
                runSqlScript("drop_user_table.sql");
                runSqlScript("create_user_table.sql");
                needToRetry = false;
            } catch (Exception e) {
                sqlRetryCounter++;
                sleep(500);
            }
        }

        enquirerEmailAddress = "enquirer" + System.currentTimeMillis() + "@victim.com";
    }

    @After
    public void teardown() {
        deleteAllMessages(LCAG_INBOX_EMAIL_ADDRESS);
        deleteAllMessages(enquirerEmailAddress);
    }

    @Test
    public void doesNotCreateMyBbUserWhenEmailAddressAlreadyInUserTable() throws InterruptedException {
        // enquirer initially has no emails
        assertEquals(0, getEmails(enquirerEmailAddress, true).size());

        insertUser(1, "testuser", enquirerEmailAddress);
        sendEnquiryEmail(enquirerEmailAddress, "Testy Test");
        sleep(1000);
        assertEquals(1, getEmails(LCAG_INBOX_EMAIL_ADDRESS, false).size());
        sleep(3000); //wait for lcag automation to process emails
        assertEquals(1, getUserRows().size());
        assertEquals(enquirerEmailAddress, getUserRows().get(0).getEmailAddress());

        //enquirer does not receive an email
        assertEquals(0, getEmails(enquirerEmailAddress, false).size());
    }

    @Test
    public void createsMyBbUserWhenEmailAddressNotInUserTable() throws InterruptedException {
        assertEquals(0, getUserRows().size());
        // enquirer initially has no emails
        assertEquals(0, getEmails(enquirerEmailAddress, true).size());

        sendEnquiryEmail(enquirerEmailAddress, "Testy Test");
        sleep(1000);
        assertEquals(1, getEmails(LCAG_INBOX_EMAIL_ADDRESS, true).size());
        sleep(3000); //wait for lcag automation to process emails
        assertEquals(1, getUserRows().size());
        assertEquals(enquirerEmailAddress, getUserRows().get(0).getEmailAddress());

        // enquirer receives the welcome email
        assertEquals(1, getEmails(enquirerEmailAddress, false).size());
    }

}
