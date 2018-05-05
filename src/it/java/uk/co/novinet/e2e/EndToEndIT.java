package uk.co.novinet.e2e;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.lang.Thread.sleep;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static uk.co.novinet.e2e.TestUtils.*;

public class EndToEndIT {

    private String enquirerEmailAddress;
    private String enquirierUsername;

    @Before
    public void setup() throws Exception {
        deleteAllMessages(LCAG_INBOX_EMAIL_ADDRESS);

        int sqlRetryCounter = 0;
        boolean needToRetry = true;

        while (needToRetry && sqlRetryCounter < 60) {
            try {
                runSqlScript("drop_user_table.sql");
                runSqlScript("create_user_table.sql");
                runSqlScript("create_usergroups_table.sql");
                runSqlScript("populate_usergroups_table.sql");
                needToRetry = false;
            } catch (Exception e) {
                sqlRetryCounter++;
                sleep(1000);
            }
        }

        enquirierUsername = "enquirer" + System.currentTimeMillis();
        enquirerEmailAddress = enquirierUsername + "@victim.com";
    }

    @After
    public void teardown() {
        deleteAllMessages(LCAG_INBOX_EMAIL_ADDRESS);
        deleteAllMessages(enquirerEmailAddress);
    }

    @Test
    public void doesNotCreateMyBbUserWhenEmailAddressAlreadyInUserTable() throws Exception {
        // enquirer initially has no emails
        assertEquals(0, getEmails(enquirerEmailAddress).size());

        insertUser(1, "testuser", enquirerEmailAddress, "Testy Test", 8);
        assertEquals(1, getUserRows().size());
        assertEquals(enquirerEmailAddress, getUserRows().get(0).getEmailAddress());

        sendEnquiryEmail(enquirerEmailAddress, "Testy Test");
        sleep(1000);
        assertEquals(1, getEmails(LCAG_INBOX_EMAIL_ADDRESS).size());
        sleep(3000); //wait for lcag automation to process emails

        assertEquals(1, getUserRows().size());
        assertEquals(enquirerEmailAddress, getUserRows().get(0).getEmailAddress());

        //enquirer does not receive an email
        assertEquals(0, getEmails(enquirerEmailAddress).size());
    }

    @Test
    public void createsMyBbUserWhenEmailAddressNotInUserTable() throws Exception {
        assertEquals(0, getUserRows().size());
        // enquirer initially has no emails
        assertEquals(0, getEmails(enquirerEmailAddress).size());

        sendEnquiryEmail(enquirerEmailAddress, "Testy Test");
        sleep(1000);
        assertEquals(1, getEmails(LCAG_INBOX_EMAIL_ADDRESS).size());
        sleep(3000); //wait for lcag automation to process emails
        assertEquals(1, getUserRows().size());
        assertEquals(enquirerEmailAddress, getUserRows().get(0).getEmailAddress());
        assertEquals("Testy Test", getUserRows().get(0).getName());
        assertEquals(enquirierUsername, getUserRows().get(0).getUsername());

        // enquirer receives the welcome email
        List<StaticMessage> messages = getEmails(enquirerEmailAddress);
        assertEquals(1, messages.size());
        StaticMessage enquiryReply = messages.get(0);

        assertTrue(enquiryReply.getContentType().startsWith("multipart"));
        assertEquals("LCAG Enquiry", enquiryReply.getSubject());
        assertEquals("lcag-testing@lcag.com", enquiryReply.getFrom());
        assertTrue(enquiryReply.getContent().contains("Testy Test " + enquirierUsername));
    }

}
