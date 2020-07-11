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
        System.out.println("t1 - 1");
        noEmailsSentAndNoMyBbUsersExist();

        System.out.println("t1 - 2");
        insertUser(1, "testuser", enquirerEmailAddress, "Testy Test", 8);
        System.out.println("t1 - 3");
        assertEquals(1, getUserRows().size());
        System.out.println("t1 - 4");
        assertEquals(enquirerEmailAddress, getUserRows().get(0).getEmailAddress());

        System.out.println("t1 - 5");
        sendEnquiryEmail(enquirerEmailAddress, "Testy Test");

        System.out.println("t1 - 6");
        waitForNEmailsToAppearInFolder(1, "History", LCAG_INBOX_EMAIL_ADDRESS);

        System.out.println("t1 - 7");
        assertEquals(0, getEmails(LCAG_INBOX_EMAIL_ADDRESS, "Inbox").size());
        System.out.println("t1 - 8");
        assertEquals(1, getEmails(LCAG_INBOX_EMAIL_ADDRESS, "History").size());

        System.out.println("t1 - 9");
        assertEquals(1, getUserRows().size());
        System.out.println("t1 - 10");
        assertEquals(enquirerEmailAddress, getUserRows().get(0).getEmailAddress());

        //enquirer receives an email saying they already have a forum account
        System.out.println("t1 - 11");
        assertEquals(1, getEmails(enquirerEmailAddress, "Inbox").size());

        System.out.println("t1 - 12");
        System.out.println(getEmails(enquirerEmailAddress, "Inbox").get(0).getContent());

        System.out.println("t1 - 13");
        assertNotNull(getUserRows().get(0).getToken());
        System.out.println("t1 - 14");
        assertTrue(getUserRows().get(0).getToken().length() > 10);
        System.out.println("t1 - 15");
        assertTrue(getEmails(enquirerEmailAddress, "Inbox").get(0).getContent().contains("Dear Testy Test, Thank you for completing the Loan Charge Action Group sign up form. It looks like you already have a Loan Charge Action Group account set up. If you haven’t already done so, can you please complete the new member on-boarding form here: https://membership.hmrcloancharge.info/?token=" + getUserRows().get(0).getToken() + " Once we have verified your ID and scheme documents and confirmed receipt of your joining fee, we will move you over to full membership. If you can’t remember your forum account details, please use the forgotten password facility and a new password will be sent to you: https://forum.hmrcloancharge.info/member.php?action=lostpw Your forum username is: " + getUserRows().get(0).getUsername() + " Thank you, LCAG Membership Team"));
        System.out.println("t1 - 16");
        assertEquals("Thank you for registering your interest in The Loan Charge Action Group", getEmails(enquirerEmailAddress, "Inbox").get(0).getSubject());
        System.out.println("t1 - 17");
    }

    private void noEmailsSentAndNoMyBbUsersExist() {
        assertEquals(0, getUserRows().size());
        enquirerHasNoEmails();
        lcagHasNoEmails();
    }

    @Test
    public void createsMyBbUserWhenEmailAddressNotInUserTableAndMovesEnquiryEmailToHistoryFolder() throws Exception {
        System.out.println("t2 - 1");
        noEmailsSentAndNoMyBbUsersExist();

        System.out.println("t2 - 2");
        sendEnquiryEmail(enquirerEmailAddress, "Testy Test");

        System.out.println("t2 - 3");
        waitForNEmailsToAppearInFolder(1, "History", LCAG_INBOX_EMAIL_ADDRESS);

        System.out.println("t2 - 4");
        assertEquals(1, getEmails(LCAG_INBOX_EMAIL_ADDRESS, "History").size());

        System.out.println("t2 - 5");
        sleep(3000); //wait for lcag automation to process emails
        System.out.println("t2 - 6");
        assertEquals(1, getUserRows().size());
        System.out.println("t2 - 7");
        assertEquals(enquirerEmailAddress, getUserRows().get(0).getEmailAddress());
        System.out.println("t2 - 8");
        assertEquals("Testy Test", getUserRows().get(0).getName());
        System.out.println("t2 - 9");
        assertEquals(enquirierUsername, getUserRows().get(0).getUsername());
        System.out.println("t2 - 10");
        assertTrue(StringUtils.isNotBlank(getUserRows().get(0).getToken()));

        // enquirer receives the welcome email
        System.out.println("t2 - 11");
        List<StaticMessage> messages = getEmails(enquirerEmailAddress, "Inbox");
        System.out.println("t2 - 12");
        assertEquals(1, messages.size());
        System.out.println("t2 - 13");
        StaticMessage enquiryReply = messages.get(0);

        System.out.println("t2 - 14");
        assertTrue(enquiryReply.getContentType().startsWith("multipart"));
        System.out.println("t2 - 15");
        assertEquals("LCAG Enquiry", enquiryReply.getSubject());
        System.out.println("t2 - 16");
        assertEquals("lcag-testing@lcag.com", enquiryReply.getFrom());
        System.out.println("t2 - 17");
        System.out.println(enquiryReply.getContent());
        System.out.println("t2 - 18");
        assertTrue(enquiryReply.getContent().contains("Dear Testy Test"));
        System.out.println("t2 - 19");
        assertTrue(enquiryReply.getContent().contains("https://membership.hmrcloancharge.info/?token=" + getUserRows().get(0).getToken()));
        System.out.println("t2 - 20");
    }

    @Test
    public void createsMyBbUserWhenEmailAddressNotInUserTableAndDoesNotMoveEnquiryEmailToHistoryFolderWhenNoHistoryFolderExists() throws Exception {
        System.out.println("t3 - 1");
        noEmailsSentAndNoMyBbUsersExist();

        System.out.println("t3 - 2");
        deleteMailFolder("History", LCAG_INBOX_EMAIL_ADDRESS);

        System.out.println("t3 - 3");
        sendEnquiryEmail(enquirerEmailAddress, "Testy Test");

        System.out.println("t3 - 4");
        waitForNEmailsToAppearInFolder(1, "Inbox", LCAG_INBOX_EMAIL_ADDRESS);

        System.out.println("t3 - 5");
        sleep(3000); //wait for lcag automation to process emails

        System.out.println("t3 - 6");
        assertEquals(1, getEmails(LCAG_INBOX_EMAIL_ADDRESS, "Inbox").size());

        System.out.println("t3 - 7");
        assertEquals(1, getUserRows().size());
        System.out.println("t3 - 8");
        assertEquals(enquirerEmailAddress, getUserRows().get(0).getEmailAddress());
        System.out.println("t3 - 9");
        assertEquals("Testy Test", getUserRows().get(0).getName());
        System.out.println("t3 - 10");
        assertEquals(enquirierUsername, getUserRows().get(0).getUsername());
        System.out.println("t3 - 11");
        assertTrue(StringUtils.isNotBlank(getUserRows().get(0).getToken()));

        // enquirer receives the welcome email
        System.out.println("t3 - 12");
        waitForNEmailsToAppearInFolder(1, "Inbox", enquirerEmailAddress);

        System.out.println("t3 - 13");
        List<StaticMessage> messages = getEmails(enquirerEmailAddress, "Inbox");

        System.out.println("t3 - 14");
        System.out.println(messages);

        System.out.println("t3 - 15");
        assertEquals(1, messages.size());
        System.out.println("t3 - 16");
        StaticMessage enquiryReply = messages.get(0);

        System.out.println("t3 - 17");
        assertTrue(enquiryReply.getContentType().startsWith("multipart"));
        System.out.println("t3 - 18");
        assertEquals("LCAG Enquiry", enquiryReply.getSubject());
        System.out.println("t3 - 19");
        assertEquals("lcag-testing@lcag.com", enquiryReply.getFrom());
        System.out.println("t3 - 20");
        assertTrue(enquiryReply.getContent().contains("Dear Testy Test "));
        System.out.println("t3 - 21");
        assertTrue(enquiryReply.getContent().contains("Username: " + enquirierUsername));
        System.out.println("t3 - 22");
        assertTrue(enquiryReply.getContent().contains("https://membership.hmrcloancharge.info/?token=" + getUserRows().get(0).getToken()));
        System.out.println("t3 - 23");
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
        System.out.println("t4 - 1");
        assertEquals(0, getUserRows().size());
        // enquirer initially has no emails
        System.out.println("t4 - 2");
        lcagHasNoEmails();
        System.out.println("t4 - 3");
        assertEquals(0, getEmails(duplicateEnquirerEmailAddress1, "Inbox").size());
        System.out.println("t4 - 4");
        assertEquals(0, getEmails(duplicateEnquirerEmailAddress2, "Inbox").size());

        System.out.println("t4 - 5");
        sendEnquiryEmail(duplicateEnquirerEmailAddress1, "Testy Test1");
        System.out.println("t4 - 6");
        sendEnquiryEmail(duplicateEnquirerEmailAddress2, "Testy Test2");

        System.out.println("t4 - 7");
        waitForUserRows(2);

        System.out.println("t4 - 8");
        assertEquals(2, getUserRows().size());

        System.out.println("t4 - 9");
        waitForNEmailsToAppearInFolder(2, "History", LCAG_INBOX_EMAIL_ADDRESS);

        System.out.println("t4 - 10");
        assertEquals(2, getEmails(LCAG_INBOX_EMAIL_ADDRESS, "History").size());

        boolean duplicateEnquirerEmailAddress1UserCreated = false;
        boolean duplicateEnquirerEmailAddress2UserCreated = false;

        String duplicateEnquirerUsername1 = "";
        String duplicateEnquirerUsername2 = "";

        for (User user : getUserRows()) {
            System.out.println("t4 - 11");
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
        System.out.println("t4 - 12");

        assertTrue(duplicateEnquirerEmailAddress1UserCreated);
        assertTrue(duplicateEnquirerEmailAddress2UserCreated);

        // enquirer receives the welcome email
        System.out.println("t4 - 13");
        List<StaticMessage> messages1 = getEmails(duplicateEnquirerEmailAddress1, "Inbox");
        System.out.println("t4 - 14");
        assertEquals(1, messages1.size());
        System.out.println("t4 - 15");
        StaticMessage enquiryReply1 = messages1.get(0);

        System.out.println("t4 - 16");
        List<StaticMessage> messages2 = getEmails(duplicateEnquirerEmailAddress2, "Inbox");
        System.out.println("t4 - 17");
        assertEquals(1, messages2.size());
        System.out.println("t4 - 18");
        StaticMessage enquiryReply2 = messages2.get(0);

        System.out.println("t4 - 19");
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
        System.out.println("t4 - 20");
    }
}
