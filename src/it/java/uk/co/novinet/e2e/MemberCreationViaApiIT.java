package uk.co.novinet.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.co.novinet.auth.MyBbPasswordEncoder;
import uk.co.novinet.rest.member.MinimalMember;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;
import static uk.co.novinet.e2e.TestUtils.*;

public class MemberCreationViaApiIT {

    private String enquirerEmailAddress;
    private String enquirierUsername;

    private String duplicateEnquirerEmailAddress1;
    private String duplicateEnquirerEmailAddress2;

    @BeforeClass
    public static void beforeClass() throws Exception {
        setupDatabaseSchema();
    }

    @Before
    public void before() throws NoSuchAlgorithmException {
        runSqlScript("sql/delete_all_users.sql");
        runSqlScript("sql/delete_all_enquiries.sql");

        insertUser(10000, "apiuser", "apiuser@lcag.com", "apiuser", 14, true, MyBbPasswordEncoder.hashPassword("apipassword", "salt"), "salt");

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
        assertEquals(2, getUserRows().size());

        User testUser = getUserRows().stream().filter(user -> user.getUsername().equals("testuser")).findFirst().get();

        assertNotNull(testUser);

        assertNotNull(getUserRows().stream().filter(user -> user.getUsername().equals("apiuser")).findFirst().get());

        assertEquals(enquirerEmailAddress, testUser.getEmailAddress());

        String responseBody = EntityUtils.toString(createMemberViaApi("John Smith", enquirerEmailAddress, "01234567890", "apiuser", "apipassword").getEntity(), "UTF-8");

        MinimalMember minimalMember = new ObjectMapper().readValue(responseBody, MinimalMember.class);

        assertNotNull(minimalMember.getId());
        assertEquals(enquirerEmailAddress, minimalMember.getEmailAddress());
        assertEquals("John Smith", minimalMember.getName());
        assertEquals("01234567890", minimalMember.getPhoneNumber());

        waitUntilEnquiryRowProcessed();

        assertEquals(2, getUserRows().size());
        assertEquals(enquirerEmailAddress, testUser.getEmailAddress());

        //enquirer receives an email saying they already have a forum account
        assertEquals(1, getEmails(enquirerEmailAddress, "Inbox").size());

        System.out.println(getEmails(enquirerEmailAddress, "Inbox").get(0).getContent());

        assertNotNull(testUser.getToken());
        assertTrue(testUser.getToken().length() > 10);
        assertTrue(getEmails(enquirerEmailAddress, "Inbox").get(0).getContent().contains("Dear Testy Test, Thank you for completing the Loan Charge Action Group sign up form. It looks like you already have a Loan Charge Action Group account set up. If you haven’t already done so, can you please complete the new member on-boarding form here: https://membership.hmrcloancharge.info/?token=" + testUser.getToken() + " Once we have verified your ID and scheme documents and confirmed receipt of your joining fee, we will move you over to full membership. If you can’t remember your forum account details, please use the forgotten password facility and a new password will be sent to you: https://forum.hmrcloancharge.info/member.php?action=lostpw Your forum username is: " + testUser.getUsername() + " Thank you, LCAG Membership Team"));
        assertEquals("Thank you for registering your interest in The Loan Charge Action Group", getEmails(enquirerEmailAddress, "Inbox").get(0).getSubject());
        assertEquals(1, getEnquiryRows().size());
        assertTrue(getEnquiryRows().get(0).getProcessed());
    }

    private void noEmailsSentAndNoMyBbUsersExist() {
        assertEquals(1, getUserRows().size());
        assertEquals("apiuser", getUserRows().get(0).getUsername());
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

        String responseBody = EntityUtils.toString(createMemberViaApi("John Smith", enquirerEmailAddress, "01234567890", "apiuser", "apipassword").getEntity(), "UTF-8");

        MinimalMember minimalMember = new ObjectMapper().readValue(responseBody, MinimalMember.class);

        assertNotNull(minimalMember.getId());
        assertEquals(enquirerEmailAddress, minimalMember.getEmailAddress());
        assertEquals("John Smith", minimalMember.getName());
        assertEquals("01234567890", minimalMember.getPhoneNumber());

        assertFalse(getEnquiryRows().get(0).getProcessed());

        waitUntilEnquiryRowProcessed();

        assertTrue(getEnquiryRows().get(0).getProcessed());
        assertEquals(2, getUserRows().size());

        User testUser = getUserRows().stream().filter(user -> user.getUsername().equals(enquirierUsername)).findFirst().get();

        assertEquals(enquirerEmailAddress, testUser.getEmailAddress());
        assertEquals("John Smith", testUser.getName());
        assertEquals(enquirierUsername, testUser.getUsername());
        assertTrue(StringUtils.isNotBlank(testUser.getToken()));

        // enquirer receives the welcome email
        List<StaticMessage> messages = getEmails(enquirerEmailAddress, "Inbox");
        assertEquals(1, messages.size());
        StaticMessage enquiryReply = messages.get(0);

        assertTrue(enquiryReply.getContentType().startsWith("multipart"));
        assertEquals("LCAG Enquiry", enquiryReply.getSubject());
        assertEquals("lcag-testing@lcag.com", enquiryReply.getFrom());
        assertTrue(enquiryReply.getContent().contains("Dear John Smith"));
        assertTrue(enquiryReply.getContent().contains("https://membership.hmrcloancharge.info/?token=" + testUser.getToken()));
    }

    @Test
    public void returns201WhenCreationIsSuccessfulIsMissing() throws Exception {
        CloseableHttpResponse response = createMemberViaApi("John Smith", enquirerEmailAddress, "01234567890", "apiuser", "apipassword");
        assertEquals(201, response.getStatusLine().getStatusCode());
    }

    @Test
    public void returns401WhenNonApiBasicAuthCredentialsAreUsed() throws Exception {
        CloseableHttpResponse response = createMemberViaApi("John Smith", enquirerEmailAddress, "01234567890", "nonapiuser", "nonapipassword");
        assertEquals(401, response.getStatusLine().getStatusCode());
    }

    @Test
    public void returns400WhenNameIsMissing() throws Exception {
        CloseableHttpResponse response = createMemberViaApi(null, enquirerEmailAddress, "01234567890", "apiuser", "apipassword");
        assertEquals(400, response.getStatusLine().getStatusCode());
    }

    @Test
    public void returns400WhenEmailAddressIsMissing() throws Exception {
        CloseableHttpResponse response = createMemberViaApi("John Smith", null, "01234567890", "apiuser", "apipassword");
        assertEquals(400, response.getStatusLine().getStatusCode());
    }

    @Test
    public void returns400WhenPhoneNumberIsMissing() throws Exception {
        CloseableHttpResponse response = createMemberViaApi("John Smith", enquirerEmailAddress, null, "apiuser", "apipassword");
        assertEquals(400, response.getStatusLine().getStatusCode());
    }
}
