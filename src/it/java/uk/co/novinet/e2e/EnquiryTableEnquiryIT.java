package uk.co.novinet.e2e;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;
import static uk.co.novinet.e2e.TestUtils.*;

public class EnquiryTableEnquiryIT {

    private String enquirerEmailAddress;
    private String enquirierUsername;

    private String duplicateEnquirerEmailAddress1;
    private String duplicateEnquirerEmailAddress2;

    @BeforeClass
    public static void beforeClass() throws Exception {
        setupDatabaseSchema();
    }

    @Before
    public void before() {
        runSqlScript("sql/delete_all_users.sql");
        runSqlScript("sql/delete_all_enquiries.sql");

        duplicateEnquirerEmailAddress1 = "test@victim1" + Math.random() + ".com";
        duplicateEnquirerEmailAddress2 = "test@victim2" + Math.random() + ".com";

        enquirierUsername = "enquirer" + System.currentTimeMillis();
        enquirerEmailAddress = enquirierUsername + "@victim.com";

        deleteAllMessages(LCAG_INBOX_EMAIL_ADDRESS);
        deleteAllMessages(enquirerEmailAddress);
        deleteAllMessages(duplicateEnquirerEmailAddress1);
        deleteAllMessages(duplicateEnquirerEmailAddress2);
        deleteMailFolder("History", LCAG_INBOX_EMAIL_ADDRESS);

        createMailFolder("History", LCAG_INBOX_EMAIL_ADDRESS);
    }

    @After
    public void teardown() {
        deleteAllMessages(LCAG_INBOX_EMAIL_ADDRESS);
        deleteAllMessages(enquirerEmailAddress);
        deleteAllMessages(duplicateEnquirerEmailAddress1);
        deleteAllMessages(duplicateEnquirerEmailAddress2);
        deleteMailFolder("History", LCAG_INBOX_EMAIL_ADDRESS);
    }

    @Test
    public void doesNotCreateMyBbUserWhenEmailAddressAlreadyInUserTable() throws Exception {
        noEmailsSentAndNoMyBbUsersExist();

        insertUser(1, "testuser", enquirerEmailAddress, "Testy Test", 8);
        assertEquals(1, getUserRows().size());
        assertEquals(enquirerEmailAddress, getUserRows().get(0).getEmailAddress());

        insertEnqiry(1, "John Smith", enquirerEmailAddress, "Mp Name", "Constituency", "Party", true, true, "Schemes", "Industry", "How did you find out about LCAG", false, "");
        assertFalse(getEnquiryRows().get(0).getProcessed());

        waitUntilEnquiryRowProcessed();

        assertEquals(1, getUserRows().size());
        assertEquals(enquirerEmailAddress, getUserRows().get(0).getEmailAddress());

        //enquirer receives an email saying they already have a forum account
        assertEquals(1, getEmails(enquirerEmailAddress, "Inbox").size());

        System.out.println(getEmails(enquirerEmailAddress, "Inbox").get(0).getContent());

        assertNotNull(getUserRows().get(0).getToken());
        assertTrue(getUserRows().get(0).getToken().length() > 10);
<<<<<<< HEAD
=======
        //assertTrue(getEmails(enquirerEmailAddress, "Inbox").get(0).getContent().contains("Dear Testy Test, Thank you for completing the Loan Charge Action Group sign up form. It looks like you already have a Loan Charge Action Group account set up. If you haven’t already done so, can you please complete the new member on-boarding form here: https://membership.hmrcloancharge.info/?token=" + getUserRows().get(0).getToken() + " Once we have verified your ID and scheme documents and confirmed receipt of your joining fee, we will move you over to full membership. If you can’t remember your forum account details, please use the forgotten password facility and a new password will be sent to you: https://forum.hmrcloancharge.info/member.php?action=lostpw Your forum username is: " + getUserRows().get(0).getUsername() + " Thank you, Richard Horsley Membership Team"));
>>>>>>> More email changes - goodbye Richard Horsley
        assertTrue(getEmails(enquirerEmailAddress, "Inbox").get(0).getContent().contains("Dear Testy Test, Thank you for completing the Loan Charge Action Group sign up form. It looks like you already have a Loan Charge Action Group account set up. If you haven’t already done so, can you please complete the new member on-boarding form here: https://membership.hmrcloancharge.info/?token=" + getUserRows().get(0).getToken() + " Once we have verified your ID and scheme documents and confirmed receipt of your joining fee, we will move you over to full membership. If you can’t remember your forum account details, please use the forgotten password facility and a new password will be sent to you: https://forum.hmrcloancharge.info/member.php?action=lostpw Your forum username is: " + getUserRows().get(0).getUsername() + " Thank you, LCAG Membership Team"));
        assertEquals("Thank you for registering your interest in The Loan Charge Action Group", getEmails(enquirerEmailAddress, "Inbox").get(0).getSubject());
        assertEquals(1, getEnquiryRows().size());
        assertTrue(getEnquiryRows().get(0).getProcessed());
    }

    private void noEmailsSentAndNoMyBbUsersExist() {
        assertEquals(0, getUserRows().size());
        enquirerHasNoEmails();
        lcagHasNoEmails();
    }

    private void enquirerHasNoEmails() {
        assertEquals(0, getEmails(enquirerEmailAddress, "Inbox").size());
    }

    private void lcagHasNoEmails() {
        assertEquals(0, getEmails(LCAG_INBOX_EMAIL_ADDRESS, "Inbox").size());
        assertEquals(0, getEmails(LCAG_INBOX_EMAIL_ADDRESS, "History").size());
    }

    @Test
    public void createsMyBbUserWhenEmailAddressNotInUserTableMarksEnquiryAsProcessed() throws Exception {
        noEmailsSentAndNoMyBbUsersExist();

        insertEnqiry(1, "John Smith", enquirerEmailAddress, "Mp Name", "Constituency", "Party", true, true, "Schemes", "Industry", "How did you find out about LCAG", false, "");
        assertFalse(getEnquiryRows().get(0).getProcessed());

        waitUntilEnquiryRowProcessed();

        assertTrue(getEnquiryRows().get(0).getProcessed());
        assertEquals(1, getUserRows().size());
        assertEquals(enquirerEmailAddress, getUserRows().get(0).getEmailAddress());
        assertEquals("John Smith", getUserRows().get(0).getName());
        assertEquals(enquirierUsername, getUserRows().get(0).getUsername());
        assertTrue(StringUtils.isNotBlank(getUserRows().get(0).getToken()));

        // enquirer receives the welcome email
        List<StaticMessage> messages = getEmails(enquirerEmailAddress, "Inbox");
        assertEquals(1, messages.size());
        StaticMessage enquiryReply = messages.get(0);

        assertTrue(enquiryReply.getContentType().startsWith("multipart"));
        assertEquals("LCAG Enquiry", enquiryReply.getSubject());
        assertEquals("lcag-testing@lcag.com", enquiryReply.getFrom());
        System.out.println(enquiryReply.getContent());
        assertTrue(enquiryReply.getContent().contains("Dear John Smith"));
        assertTrue(enquiryReply.getContent().contains("https://membership.hmrcloancharge.info/?token=" + getUserRows().get(0).getToken()));
        assertEquals("Mp Name", getUserRows().get(0).getMpName());
        assertEquals("Constituency", getUserRows().get(0).getMpConstituency());
        assertEquals("Party", getUserRows().get(0).getMpParty());
        assertEquals(true, getUserRows().get(0).getMpEngaged());
        assertEquals(true, getUserRows().get(0).getMpSympathetic());
        assertEquals("Schemes", getUserRows().get(0).getSchemes());
        assertEquals("Industry", getUserRows().get(0).getIndustry());
        assertEquals("How did you find out about LCAG", getUserRows().get(0).getHowDidYouHearAboutLcag());
        assertEquals("", getUserRows().get(0).getBigGroupUsername());
    }
}
