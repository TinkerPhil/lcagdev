package uk.co.novinet.e2e;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.codemonkey.simplejavamail.email.Email;
import org.jsoup.Jsoup;
import uk.co.novinet.auth.MyBbPasswordEncoder;
import uk.co.novinet.service.PersistenceUtils;
import uk.co.novinet.service.enquiry.Enquiry;
import uk.co.novinet.service.payments.BankTransaction;
import uk.co.novinet.service.payments.PaymentSource;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.io.*;
import java.sql.*;
import java.time.Instant;
import java.util.Date;
import java.util.*;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static uk.co.novinet.service.PersistenceUtils.dateFromMyBbRow;

public class TestUtils {

    static final String DB_URL = "jdbc:mysql://127.0.0.1:4306/mybb";
    static final String DB_USERNAME = "user";
    static final String DB_PASSWORD = "p@ssword";

    static final String WIX_ENQUIRY_FROM_ADDRESS = "enquiry_form_submission@address.com";

    static final String SMTP_HOST = "localhost";
    static final int    SMTP_PORT = 3025;
    static final String SMTP_USERNAME = "anything";
    static final String SMTP_PASSWORD = "password";

    static final String IMAP_HOST = "localhost";
    static final int    IMAP_PORT = 3143;

    static final String LCAG_INBOX_EMAIL_ADDRESS = "lcag-testing@lcag.com";

    static void setupDatabaseSchema() throws Exception {
        deleteAllMessages(LCAG_INBOX_EMAIL_ADDRESS);

        int sqlRetryCounter = 0;
        boolean needToRetry = true;

        while (needToRetry && sqlRetryCounter < 60) {
            try {
                runSqlScript("sql/create_user_table.sql");
                runSqlScript("sql/create_userFundingSummary_table.sql");
                runSqlScript("sql/delete_all_users.sql");
                runSqlScript("sql/create_mp_details_table.sql");
                runSqlScript("sql/create_mpDetails_table.sql");
                runSqlScript("sql/create_mpCampaignVolunteers_table.sql");
                runSqlScript("sql/create_ffc_contributions_table.sql");
                runSqlScript("sql/create_enquiry_table.sql");
                runSqlScript("sql/create_usergroups_table.sql");
                runSqlScript("sql/create_bank_transaction_table.sql");
                runSqlScript("sql/create_bank_transaction_infull_table.sql");
                runSqlScript("sql/populate_usergroups_table.sql");

                needToRetry = false;
            } catch (Exception e) {
                e.printStackTrace();
                sqlRetryCounter++;
                sleep(1000);
            }

            insertUser(9999, "admin", "admin@lcag.com", "Administrators", 4, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt");
        }
    }

    static void sendEnquiryEmail(String emailAddress, String name) {
        String messageBody = "You have a new message:  Via: https://www.hmrcloancharge.info/ Message Details: Email " + emailAddress + " Name " + name + " Subject Retrospective Charge Phone No. 07841143296 Message Sent on: 24 April, 2018 Thank you!";

        Email email = new Email();

        email.setFromAddress(WIX_ENQUIRY_FROM_ADDRESS, WIX_ENQUIRY_FROM_ADDRESS);
        email.addRecipient(LCAG_INBOX_EMAIL_ADDRESS, LCAG_INBOX_EMAIL_ADDRESS, MimeMessage.RecipientType.TO);
        email.setText(messageBody);
        email.setSubject("Subject");

        new Mailer(SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, TransportStrategy.SMTP_PLAIN).sendMail(email);
    }

    static void sendNewsletterEmail(String emailAddress, String name, String phoneNumber) {
        String messageBody = "Testy Test has completed the Newsletter Form and their details are listed below.\n" +
                "\n" +
                "---------------------------\n" +
                "\n" +
                "Name:\n" +
                name + "\n" +
                "\n" +
                "Email:\n" +
                emailAddress + "\n" +
                "\n" +"\n" +
                "Phone number:\n" +
                phoneNumber + "\n" +
                "\n" +
                "Terms acceptance:\n" +
                "Consented: I agree to the use of my data as specified in the Privacy Policy\n" +
                "\n" +
                "---------------------------\n" +
                "\n" +
                "This e-mail was sent from the Contact Form on the LCAG website.";

        Email email = new Email();

        email.setFromAddress(WIX_ENQUIRY_FROM_ADDRESS, WIX_ENQUIRY_FROM_ADDRESS);
        email.addRecipient(LCAG_INBOX_EMAIL_ADDRESS, LCAG_INBOX_EMAIL_ADDRESS, MimeMessage.RecipientType.TO);
        email.setText(messageBody);
        email.setSubject("Subject");

        new Mailer(SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, TransportStrategy.SMTP_PLAIN).sendMail(email);
    }



    public static void insertUser(int id, String username, String emailAddress, String name, int group) {
        insertUser(id, username, emailAddress, name, group, false);
    }

    public static void insertUser(int id, String username, String emailAddress, String name, int group, boolean hasCompletedMembershipForm) {
        insertUser(id, username, emailAddress, name, group, hasCompletedMembershipForm, "63e5314b6c31334d75ac74e4ed7fdc69", "bSS1l899");
    }

    public static void insertUser(int id, String username, String emailAddress, String name, int group, boolean hasCompletedMembershipForm, String hashedPassword, String salt) {
        insertUser(id, username, emailAddress, name, group, Collections.emptyList(), hasCompletedMembershipForm, hashedPassword, salt);
    }

    public static void insertUser(int id, String username, String emailAddress, String name, int group, List<Integer> additionalGroups, boolean hasCompletedMembershipForm, String hashedPassword, String salt) {
        runSqlUpdate("INSERT INTO `i7b0_users` (`uid`, `username`, `password`, `salt`, `loginkey`, `email`, `postnum`, `threadnum`, `avatar`, " +
                "`avatardimensions`, `avatartype`, `usergroup`, `additionalgroups`, `displaygroup`, `usertitle`, `regdate`, `lastactive`, `lastvisit`, " +
                "`lastpost`, `website`, `icq`,  `yahoo`, `skype`, `google`, `birthday`, `birthdayprivacy`, `signature`, `allownotices`, `hideemail`, " +
                "`subscriptionmethod`, `invisible`, `receivepms`, `receivefrombuddy`, `pmnotice`, `pmnotify`, `buddyrequestspm`, `buddyrequestsauto`, " +
                "`threadmode`, `showimages`, `showvideos`, `showsigs`, `showavatars`, `showquickreply`, `showredirect`, `ppp`, `tpp`, `daysprune`, " +
                "`dateformat`, `timeformat`, `timezone`, `dst`, `dstcorrection`, `buddylist`, `ignorelist`, `style`, `away`, `awaydate`, `returndate`, " +
                "`awayreason`, `pmfolders`, `notepad`, `referrer`, `referrals`, `reputation`, `regip`, `lastip`, `language`, `timeonline`, `showcodebuttons`, " +
                "`totalpms`, `unreadpms`, `warningpoints`, `moderateposts`, `moderationtime`, `suspendposting`, `suspensiontime`, `suspendsignature`, " +
                "`suspendsigtime`, `coppauser`, `classicpostbit`, `loginattempts`, `usernotes`, `sourceeditor`, `name`, `has_completed_membership_form`, `token`, `claim_token`) " +
                "VALUES (" +
                id + ", '" + username + "', '" + hashedPassword + "', '" + salt + "', 'lvhLksjhHGcZIWgtlwNTJNr3bjxzCE2qgZNX6SBTBPbuSLx21u', '" + emailAddress +
                "', 0, 0, '', '', '', " + group + ", '" + additionalGroups.stream().map(Object::toString).collect(joining(",")) + "', 0, '', " + unixTime() + ", 0, 0, 0, '', '0', '', '', '', '', 'all', '', 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, " +
                "'linear', 1, 1, 1, 1, 1, 1, 0, 0, 0, '', '', '', 0, 0, '', '', 0, 0, 0, '0', '', '', '', 0, 0, 0, '', '', '', 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, " +
                "1, '', 0, '" + name + "', '" + (hasCompletedMembershipForm ? "1" : "0") + "', 'aaaaaaaaaaaaaaaaaaaaa', 'bbbbbbbbbbbbbbbbbbbb');");
    }

    public static void insertEnqiry(
            int id,
            String name,
            String emailAddress,
            String mpName,
            String mpConstituency,
            String mpParty,
            Boolean mpEngaged,
            Boolean mpSympethetic,
            String schemes,
            String industry,
            String howDidYouFindOutAboutLcag,
            Boolean memberOfBigGroup,
            String bigGroupUsername) {

        runSqlUpdate(format("INSERT INTO `i7b0_enquiry` (" +
                "`id`, `name`, `email_address`, `mp_name`, `mp_constituency`, `mp_party`, `mp_engaged`, " +
                "`mp_sympathetic`, `schemes`, `industry`, `how_did_you_hear_about_lcag`, `member_of_big_group`, " +
                "`big_group_username`) " +
                "VALUES (%s, '%s', '%s', '%s', '%s', '%s', %s, %s, '%s', '%s', '%s', %s, '%s');",
                id, name, emailAddress, mpName, mpConstituency, mpParty, mpEngaged ? "1" : "0", mpSympethetic ? "1" : "0",
                schemes, industry, howDidYouFindOutAboutLcag, memberOfBigGroup ? "1" : "0", bigGroupUsername));
    }

    static long unixTime() {
        return new Date().getTime() / 1000;
    }

    static void runSqlUpdate(String sql) {
        Connection connection = null;
        Statement statement = null;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (statement != null) connection.close();
            } catch (SQLException ignored) { }
            try {
                if (connection != null) connection.close();
            } catch (SQLException ignored) { }
        }
    }

    static List<User> getUserRows() {
        Connection connection = null;
        Statement statement = null;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from i7b0_users");
            List<User> users = new ArrayList<>();

            while (resultSet.next()) {
                users.add(new User(
                        resultSet.getInt("uid"),
                        resultSet.getString("username"),
                        resultSet.getString("email"),
                        resultSet.getString("name"),
                        resultSet.getString("token"),
                        resultSet.getString("mp_name"),
                        resultSet.getString("mp_constituency"),
                        resultSet.getString("mp_party"),
                        resultSet.getBoolean("mp_engaged"),
                        resultSet.getBoolean("mp_sympathetic"),
                        resultSet.getString("schemes"),
                        resultSet.getString("industry"),
                        resultSet.getString("how_did_you_hear_about_lcag"),
                        resultSet.getBoolean("member_of_big_group"),
                        resultSet.getString("big_group_username")
                    )
                );
            }

            return users;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (statement != null) connection.close();
            } catch (SQLException ignored) { }
            try {
                if (connection != null) connection.close();
            } catch (SQLException ignored) { }
        }
    }

    static List<BankTransaction> getBankTransactionRows() {
        Connection connection = null;
        Statement statement = null;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select * from i7b0_bank_transactions bt left outer join i7b0_users u on bt.user_id = u.uid");
            List<BankTransaction> bankTransactions = new ArrayList<>();

            while (rs.next()) {
                String userId = rs.getString("bt.user_id");

                bankTransactions.add(new BankTransaction(
                        rs.getLong("bt.id"),
                        userId == null ? null : Long.parseLong(userId),
                        rs.getString("u.username"),
                        rs.getString("u.email"),
                        dateFromMyBbRow(rs, "bt.date"),
                        rs.getString("bt.description"),
                        rs.getBigDecimal("bt.amount"),
                        rs.getBigDecimal("bt.running_balance"),
                        rs.getString("bt.counter_party"),
                        rs.getString("bt.reference"),
                        rs.getInt("bt.transaction_index_on_day"),
                        PaymentSource.valueOf(rs.getString("bt.payment_source")),
                        rs.getBoolean("bt.email_sent"),
                        rs.getBoolean("bt.excludeFromMemberReconciliation")
                    ));
            }

            return bankTransactions;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (statement != null) connection.close();
            } catch (SQLException ignored) { }
            try {
                if (connection != null) connection.close();
            } catch (SQLException ignored) { }
        }
    }

    static List<Enquiry> getEnquiryRows() {
        Connection connection = null;
        Statement statement = null;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from i7b0_enquiry");
            List<Enquiry> enquiries = new ArrayList<>();

            while (resultSet.next()) {
                enquiries.add(new Enquiry(
                        resultSet.getLong("id"),
                        resultSet.getString("email_address"),
                        resultSet.getString("name"),
                        resultSet.getString("mp_name"),
                        resultSet.getString("mp_constituency"),
                        resultSet.getString("mp_party"),
                        resultSet.getBoolean("mp_engaged"),
                        resultSet.getBoolean("mp_sympathetic"),
                        resultSet.getString("schemes"),
                        resultSet.getString("industry"),
                        resultSet.getString("how_did_you_hear_about_lcag"),
                        resultSet.getBoolean("member_of_big_group"),
                        resultSet.getString("big_group_username"),
                        resultSet.getBoolean("has_been_processed"),
                        ""
                    )
                );
            }

            return enquiries;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (statement != null) connection.close();
            } catch (SQLException ignored) { }
            try {
                if (connection != null) connection.close();
            } catch (SQLException ignored) { }
        }
    }

    static List<StaticMessage> getEmails(String emailAddress, String folderName) {
        List<StaticMessage> emails = getEmails(emailAddress, folderName, true);
        emails.addAll(getEmails(emailAddress, folderName, false));
        return emails;
    }

    public static List<StaticMessage> getEmails(String emailAddress, String folderName, Boolean alreadyRead) {
        Folder folder = null;
        IMAPStore store = null;

        try {
            Session session = Session.getDefaultInstance(new Properties());
            store = (IMAPStore) session.getStore("imap");
            store.connect(IMAP_HOST, IMAP_PORT, emailAddress, "password");
            folder = store.getFolder(folderName);
            if (!folder.exists()) {
                return Collections.emptyList();
            }
            folder.open(Folder.READ_WRITE);
            List<Message> messages = asList(folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), alreadyRead)));
            messages.forEach(message -> {
                try {
                    message.getContent();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            return messages.stream().map(message -> {
                try {
                    return new StaticMessage(message);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (folder != null) {
                try {
                    if (folder.exists()) {
                        folder.close(false);
                    }
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static void createMailFolder(String folderName, String emailAddress) {
        Store store = null;

        try {
            Session session = Session.getDefaultInstance(new Properties());
            store = session.getStore("imap");
            store.connect(IMAP_HOST, IMAP_PORT, emailAddress, "password");

            IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);

            if (folder.exists()) {
                return;
            }

            folder.create(IMAPFolder.HOLDS_MESSAGES);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static void deleteMailFolder(String folderName, String emailAddress) {
        Store store = null;

        try {
            Session session = Session.getDefaultInstance(new Properties());
            store = session.getStore("imap");
            store.connect(IMAP_HOST, IMAP_PORT, emailAddress, "password");

            IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);

            if (folder.exists()) {
                if (folder.isOpen()) {
                    folder.close(true);
                }
                folder.delete(true);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static void deleteAllMessages(String emailAddress) {
        Folder inbox = null;
        Store store = null;

        try {
            Session session = Session.getDefaultInstance(new Properties());
            store = session.getStore("imap");
            store.connect(IMAP_HOST, IMAP_PORT, emailAddress, "password");

            IMAPFolder folder = (IMAPFolder) store.getFolder("Inbox");
            folder.open(Folder.READ_WRITE);

            List<Message> toDelete = new ArrayList<>();

            for (Message message : folder.getMessages()) {
                message.setFlag(Flags.Flag.DELETED, true);
                toDelete.add(message);
            }

            if (!toDelete.isEmpty()) {
                folder.expunge(toDelete.toArray(new Message[]{}));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (inbox != null) {
                try {
                    inbox.close(false);
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static void runSqlScript(String scriptName) {
        InputStream is = TestUtils.class.getClassLoader().getResourceAsStream(scriptName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        runSqlUpdate(reader.lines().collect(joining(System.lineSeparator())));
    }

    static String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    static String getTextFromMimeMultipart(MimeMultipart mimeMultipart)  throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }


    static void waitUntilEnquiryRowProcessed() throws InterruptedException {
        int attempts = 0;

        while (getEnquiryRows().get(0).getProcessed() == false && attempts < 20) {
            sleep(500); //wait for lcag automation to process emails
            attempts++;
        }

        if (getEnquiryRows().get(0).getProcessed() == false) {
            throw new RuntimeException("Waited for first enquiry row to be processed but it was still unprocessed after 20 attempts.");
        }
    }

    static void waitForNEmailsToAppearInFolder(int numberOfProcessedEmailsToWaitFor, String folderName, String recipientEmailAddress) throws InterruptedException {
        int attempts = 0;

        while (getEmails(recipientEmailAddress, folderName).size() < numberOfProcessedEmailsToWaitFor || attempts > 20) {
            sleep(500); //wait for lcag automation to process emails
            attempts++;
        }

        if (getEmails(recipientEmailAddress, folderName).size() < numberOfProcessedEmailsToWaitFor) {
            throw new RuntimeException("Waited for " + numberOfProcessedEmailsToWaitFor + " emails to appear in [" + recipientEmailAddress + "] " + folderName + " folder, but there are still only " + getEmails(recipientEmailAddress, folderName).size() + " after 20 attempts.");
        }
    }

    static void waitForUserRows(int numberOfRowsToWaitFor) throws InterruptedException {
        int attempts = 0;

        while (getUserRows().size() < numberOfRowsToWaitFor || attempts > 20) {
            sleep(500); //wait for lcag automation to process emails
            attempts++;
        }

        if (getUserRows().size() < numberOfRowsToWaitFor) {
            throw new RuntimeException("Waited for " + numberOfRowsToWaitFor + " rows, but there are still only " + getUserRows().size() + " after 20 attempts.");
        }
    }

    static String uploadBankTransactionFile(String url, File file) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setHeader("Accept", "application/json");
        post.setHeader("Authorization", "Basic " + new String(Base64.getEncoder().encode("admin:lcag".getBytes())));
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("file", new FileBody(file));
        post.setEntity(builder.build());
        CloseableHttpResponse response = httpclient.execute(post);
        int httpStatus = response.getStatusLine().getStatusCode();

        String responseMsg = EntityUtils.toString(response.getEntity(), "UTF-8");

        if (httpStatus < 200 || httpStatus > 300) {
            throw new IOException("HTTP " + httpStatus + " - Error during upload of file: " + responseMsg);
        }

        return responseMsg;
    }

    public static String getRequest(String url, String username, String password) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        get.setHeader("Accept", "application/json");
        get.setHeader("Authorization", "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes())));

        CloseableHttpResponse response = httpclient.execute(get);

        int httpStatus = response.getStatusLine().getStatusCode();

        String responseMsg = EntityUtils.toString(response.getEntity(), "UTF-8");

        if (httpStatus < 200 || httpStatus > 300) {
            throw new IOException(responseMsg);
        }

        return responseMsg;
    }

    public static int getRequestStatusCode(String url, String username, String password) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        get.setHeader("Accept", "application/json");
        get.setHeader("Authorization", "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes())));

        CloseableHttpResponse response = httpclient.execute(get);

        return response.getStatusLine().getStatusCode();
    }

    public static void insertBankTransaction(
            int bankTransactionId,
            String userId,
            String username,
            boolean emailSent,
            String amount,
            String runningBalance,
            Instant transactionDate,
            int transactionIndexOnDay) {

        if (userId == null) {
            runSqlUpdate("INSERT INTO `i7b0_bank_transactions` (`id`, `date`, `description`, `amount`, `running_balance`, `counter_party`, `reference`, " +
                    "`transaction_index_on_day`, `payment_source`, `email_sent`) VALUES ( " + bankTransactionId + ", '" + PersistenceUtils.unixTime(transactionDate) + "', 'test bank transaction', " +
                    amount + ", " + runningBalance + ", 'Mr Smith', '" + username + "', " + transactionIndexOnDay + ", 'SANTANDER', " + (emailSent ? "1" : "0") + ")");
        } else {
            runSqlUpdate("INSERT INTO `i7b0_bank_transactions` (`id`, `date`, `description`, `amount`, `running_balance`, `counter_party`, `reference`, " +
                    "`transaction_index_on_day`, `payment_source`, `user_id`, `email_sent`) VALUES ( " + bankTransactionId + ", '" + PersistenceUtils.unixTime(transactionDate) + "', 'test bank transaction', " +
                    amount + ", " + runningBalance + ", 'Mr Smith', '" + username + "', " + transactionIndexOnDay + ", 'SANTANDER', '" + userId + "', " + (emailSent ? "1" : "0") + ")");
        }
    }


}
