package uk.co.novinet.e2e;

import com.sun.mail.imap.IMAPFolder;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.codemonkey.simplejavamail.email.Email;
import org.jsoup.Jsoup;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class TestUtils {

    static final String DB_URL = "jdbc:mysql://localhost:4306/mybb";
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

    static void sendEnquiryEmail(String emailAddress, String name) {
        String messageBody = "You have a new message:  Via: https://www.hmrcloancharge.info/ Message Details: Email " + emailAddress + " Name " + name + " Subject Retrospective Charge Phone No. 07841143296 Message Sent on: 24 April, 2018 Thank you!";

        Email email = new Email();

        email.setFromAddress(WIX_ENQUIRY_FROM_ADDRESS, WIX_ENQUIRY_FROM_ADDRESS);
        email.addRecipient(LCAG_INBOX_EMAIL_ADDRESS, LCAG_INBOX_EMAIL_ADDRESS, MimeMessage.RecipientType.TO);
        email.setText(messageBody);
        email.setSubject("Subject");

        new Mailer(SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, TransportStrategy.SMTP_PLAIN).sendMail(email);
    }

    static void insertUser(int id, String username, String emailAddress, String name, int group) {
        runSqlUpdate("INSERT INTO `i7b0_users` (`uid`, `username`, `password`, `salt`, `loginkey`, `email`, `postnum`, `threadnum`, `avatar`, `avatardimensions`, `avatartype`, `usergroup`, `additionalgroups`, `displaygroup`, `usertitle`, `regdate`, `lastactive`, `lastvisit`, `lastpost`, `website`, `icq`, `aim`, `yahoo`, `skype`, `google`, `birthday`, `birthdayprivacy`, `signature`, `allownotices`, `hideemail`, `subscriptionmethod`, `invisible`, `receivepms`, `receivefrombuddy`, `pmnotice`, `pmnotify`, `buddyrequestspm`, `buddyrequestsauto`, `threadmode`, `showimages`, `showvideos`, `showsigs`, `showavatars`, `showquickreply`, `showredirect`, `ppp`, `tpp`, `daysprune`, `dateformat`, `timeformat`, `timezone`, `dst`, `dstcorrection`, `buddylist`, `ignorelist`, `style`, `away`, `awaydate`, `returndate`, `awayreason`, `pmfolders`, `notepad`, `referrer`, `referrals`, `reputation`, `regip`, `lastip`, `language`, `timeonline`, `showcodebuttons`, `totalpms`, `unreadpms`, `warningpoints`, `moderateposts`, `moderationtime`, `suspendposting`, `suspensiontime`, `suspendsignature`, `suspendsigtime`, `coppauser`, `classicpostbit`, `loginattempts`, `usernotes`, `sourceeditor`, `name`) VALUES (" + id + ", '" + username + "', '63e5314b6c31334d75ac74e4ed7fdc69', 'bSS1l899', 'lvhLksjhHGcZIWgtlwNTJNr3bjxzCE2qgZNX6SBTBPbuSLx21u', '" + emailAddress + "', 0, 0, '', '', '', " + group + ", '', 0, '', " + unixTime() + ", 0, 0, 0, '', '0', '', '', '', '', '', 'all', '', 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 'linear', 1, 1, 1, 1, 1, 1, 0, 0, 0, '', '', '', 0, 0, '', '', 0, 0, 0, '0', '', '', '', 0, 0, 0, '', '', '', 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, '', 0, '" + name + "');");
    }

    private static long unixTime() {
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
                        resultSet.getString("name"))
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

    static List<StaticMessage> getEmails(String emailAddress) {
        List<StaticMessage> emails = getEmails(emailAddress, true);
        emails.addAll(getEmails(emailAddress, false));
        return emails;
    }

    static List<StaticMessage> getEmails(String emailAddress, Boolean alreadyRead) {
        Folder inbox = null;
        Store store = null;

        try {
            Session session = Session.getDefaultInstance(new Properties());
            store = session.getStore("imap");
            store.connect(IMAP_HOST, IMAP_PORT, emailAddress, "password");
            inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_WRITE);
            List<Message> messages = asList(inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), alreadyRead)));
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
        runSqlUpdate(reader.lines().collect(Collectors.joining(System.lineSeparator())));
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
}
