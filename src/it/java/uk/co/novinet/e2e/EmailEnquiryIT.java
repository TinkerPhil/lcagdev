package uk.co.novinet.e2e;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static java.lang.Thread.sleep;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.co.novinet.e2e.TestUtils.*;

public class EmailEnquiryIT {

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

        sendEnquiryEmail(enquirerEmailAddress, "Testy Test");

        waitForNEmailsToAppearInFolder(1, "History", LCAG_INBOX_EMAIL_ADDRESS);

        assertEquals(0, getEmails(LCAG_INBOX_EMAIL_ADDRESS, "Inbox").size());
        assertEquals(1, getEmails(LCAG_INBOX_EMAIL_ADDRESS, "History").size());

        assertEquals(1, getUserRows().size());
        assertEquals(enquirerEmailAddress, getUserRows().get(0).getEmailAddress());

        //enquirer receives an email saying they already have a forum account
        assertEquals(1, getEmails(enquirerEmailAddress, "Inbox").size());

        System.out.println(getEmails(enquirerEmailAddress, "Inbox").get(0).getContent());

        assertNotNull(getUserRows().get(0).getToken());
        assertTrue(getUserRows().get(0).getToken().length() > 10);
        assertTrue(getEmails(enquirerEmailAddress, "Inbox").get(0).getContent().contains("Dear Testy Test, Thank you for completing the Loan Charge Action Group sign up form. It looks like you already have a Loan Charge Action Group account set up. If you haven’t already done so, can you please complete the new member on-boarding form here: https://membership.hmrcloancharge.info/?token=" + getUserRows().get(0).getToken() + " Once we have verified your ID and scheme documents and confirmed receipt of your joining fee, we will move you over to full membership. If you can’t remember your forum account details, please use the forgotten password facility and a new password will be sent to you: https://forum.hmrcloancharge.info/member.php?action=lostpw Your forum username is: " + getUserRows().get(0).getUsername() + " Thank you, Richard Horsley Membership Team"));
        assertEquals("Thank you for registering your interest in The Loan Charge Action Group", getEmails(enquirerEmailAddress, "Inbox").get(0).getSubject());
    }

    private void noEmailsSentAndNoMyBbUsersExist() {
        assertEquals(0, getUserRows().size());
        enquirerHasNoEmails();
        lcagHasNoEmails();
    }

    @Test
    public void createsMyBbUserWhenEmailAddressNotInUserTableAndMovesEnquiryEmailToHistoryFolder() throws Exception {
        noEmailsSentAndNoMyBbUsersExist();

        sendEnquiryEmail(enquirerEmailAddress, "Testy Test");

        waitForNEmailsToAppearInFolder(1, "History", LCAG_INBOX_EMAIL_ADDRESS);

        assertEquals(1, getEmails(LCAG_INBOX_EMAIL_ADDRESS, "History").size());

        sleep(3000); //wait for lcag automation to process emails
        assertEquals(1, getUserRows().size());
        assertEquals(enquirerEmailAddress, getUserRows().get(0).getEmailAddress());
        assertEquals("Testy Test", getUserRows().get(0).getName());
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
        assertTrue(enquiryReply.getContent().contains("Dear Testy Test"));
        assertTrue(enquiryReply.getContent().contains("https://membership.hmrcloancharge.info/?token=" + getUserRows().get(0).getToken()));
    }

    @Test
    public void createsMyBbUserWhenEmailAddressNotInUserTableAndDoesNotMoveEnquiryEmailToHistoryFolderWhenNoHistoryFolderExists() throws Exception {
        noEmailsSentAndNoMyBbUsersExist();

        deleteMailFolder("History", LCAG_INBOX_EMAIL_ADDRESS);

        sendEnquiryEmail(enquirerEmailAddress, "Testy Test");

        waitForNEmailsToAppearInFolder(1, "Inbox", LCAG_INBOX_EMAIL_ADDRESS);

        sleep(3000); //wait for lcag automation to process emails

        assertEquals(1, getEmails(LCAG_INBOX_EMAIL_ADDRESS, "Inbox").size());

        assertEquals(1, getUserRows().size());
        assertEquals(enquirerEmailAddress, getUserRows().get(0).getEmailAddress());
        assertEquals("Testy Test", getUserRows().get(0).getName());
        assertEquals(enquirierUsername, getUserRows().get(0).getUsername());
        assertTrue(StringUtils.isNotBlank(getUserRows().get(0).getToken()));

        // enquirer receives the welcome email
        waitForNEmailsToAppearInFolder(1, "Inbox", enquirerEmailAddress);

        List<StaticMessage> messages = getEmails(enquirerEmailAddress, "Inbox");

        System.out.println(messages);

        assertEquals(1, messages.size());
        StaticMessage enquiryReply = messages.get(0);

        assertTrue(enquiryReply.getContentType().startsWith("multipart"));
        assertEquals("LCAG Enquiry", enquiryReply.getSubject());
        assertEquals("lcag-testing@lcag.com", enquiryReply.getFrom());
        assertTrue(enquiryReply.getContent().contains("Dear Testy Test "));
        assertTrue(enquiryReply.getContent().contains("Username: " + enquirierUsername));
        assertTrue(enquiryReply.getContent().contains("https://membership.hmrcloancharge.info/?token=" + getUserRows().get(0).getToken()));
    }

    private void enquirerHasNoEmails() {
        assertEquals(0, getEmails(enquirerEmailAddress, "Inbox").size());
    }

    private void lcagHasNoEmails() {
        assertEquals(0, getEmails(LCAG_INBOX_EMAIL_ADDRESS, "Inbox").size());
        assertEquals(0, getEmails(LCAG_INBOX_EMAIL_ADDRESS, "History").size());
    }

    @Test
    public void createsMyBbUserWithNumericallySuffixedUsernameWhenDuplicateUsernameExistsInUserTable() throws Exception {
        assertEquals(0, getUserRows().size());
        // enquirer initially has no emails
        lcagHasNoEmails();
        assertEquals(0, getEmails(duplicateEnquirerEmailAddress1, "Inbox").size());
        assertEquals(0, getEmails(duplicateEnquirerEmailAddress2, "Inbox").size());

        sendEnquiryEmail(duplicateEnquirerEmailAddress1, "Testy Test1");
        sendEnquiryEmail(duplicateEnquirerEmailAddress2, "Testy Test2");

        waitForUserRows(2);

        assertEquals(2, getUserRows().size());

        waitForNEmailsToAppearInFolder(2, "History", LCAG_INBOX_EMAIL_ADDRESS);

        assertEquals(2, getEmails(LCAG_INBOX_EMAIL_ADDRESS, "History").size());

        boolean duplicateEnquirerEmailAddress1UserCreated = false;
        boolean duplicateEnquirerEmailAddress2UserCreated = false;

        String duplicateEnquirerUsername1 = "";
        String duplicateEnquirerUsername2 = "";

        for (User user : getUserRows()) {
            if (user.getEmailAddress().equals(duplicateEnquirerEmailAddress1)) {
                assertEquals("Testy Test1", user.getName());
                assertEquals("test", user.getUsername());
                duplicateEnquirerUsername1 = user.getUsername();
                duplicateEnquirerEmailAddress1UserCreated = true;
            } else if (user.getEmailAddress().equals(duplicateEnquirerEmailAddress2)) {
                assertEquals("Testy Test2", user.getName());
                assertTrue(user.getUsername().startsWith("test"));
                int numericSuffix = Integer.parseInt(user.getUsername().substring("test".length()));
                assertTrue(numericSuffix != 0);
                duplicateEnquirerEmailAddress2UserCreated = true;
                duplicateEnquirerUsername2 = user.getUsername();
            }
        }

        assertTrue(duplicateEnquirerEmailAddress1UserCreated);
        assertTrue(duplicateEnquirerEmailAddress2UserCreated);

        // enquirer receives the welcome email
        List<StaticMessage> messages1 = getEmails(duplicateEnquirerEmailAddress1, "Inbox");
        assertEquals(1, messages1.size());
        StaticMessage enquiryReply1 = messages1.get(0);

        List<StaticMessage> messages2 = getEmails(duplicateEnquirerEmailAddress2, "Inbox");
        assertEquals(1, messages2.size());
        StaticMessage enquiryReply2 = messages2.get(0);

        assertTrue(enquiryReply1.getContentType().startsWith("multipart"));
        assertEquals("LCAG Enquiry", enquiryReply1.getSubject());
        assertEquals("lcag-testing@lcag.com", enquiryReply1.getFrom());
        assertTrue(enquiryReply1.getContent().contains("Dear Testy Test1"));
        assertTrue(enquiryReply1.getContent().contains("Username: " + duplicateEnquirerUsername1));
        assertTrue(enquiryReply1.getContent().contains("https://membership.hmrcloancharge.info/?token=" + getUserRows().get(0).getToken()));

        assertTrue(enquiryReply2.getContentType().startsWith("multipart"));
        assertEquals("LCAG Enquiry", enquiryReply2.getSubject());
        assertEquals("lcag-testing@lcag.com", enquiryReply2.getFrom());
        assertTrue(enquiryReply2.getContent().contains("Dear Testy Test2"));
        assertTrue(enquiryReply2.getContent().contains("Username: " + duplicateEnquirerUsername2));
        assertTrue(enquiryReply2.getContent().contains("https://membership.hmrcloancharge.info/?token=" + getUserRows().get(1).getToken()));
    }
}
