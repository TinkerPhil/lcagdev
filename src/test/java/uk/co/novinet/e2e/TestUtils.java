package uk.co.novinet.e2e;

import com.sun.mail.imap.IMAPFolder;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.codemonkey.simplejavamail.email.Email;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

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

    static void insertUser(int id, String username, String emailAddress) {
        runSqlUpdate("INSERT INTO `i7b0_users` (`uid`, `username`, `password`, `salt`, `loginkey`, `email`, `postnum`, `threadnum`, `avatar`, `avatardimensions`, `avatartype`, `usergroup`, `additionalgroups`, `displaygroup`, `usertitle`, `regdate`, `lastactive`, `lastvisit`, `lastpost`, `website`, `icq`, `aim`, `yahoo`, `skype`, `google`, `birthday`, `birthdayprivacy`, `signature`, `allownotices`, `hideemail`, `subscriptionmethod`, `invisible`, `receivepms`, `receivefrombuddy`, `pmnotice`, `pmnotify`, `buddyrequestspm`, `buddyrequestsauto`, `threadmode`, `showimages`, `showvideos`, `showsigs`, `showavatars`, `showquickreply`, `showredirect`, `ppp`, `tpp`, `daysprune`, `dateformat`, `timeformat`, `timezone`, `dst`, `dstcorrection`, `buddylist`, `ignorelist`, `style`, `away`, `awaydate`, `returndate`, `awayreason`, `pmfolders`, `notepad`, `referrer`, `referrals`, `reputation`, `regip`, `lastip`, `language`, `timeonline`, `showcodebuttons`, `totalpms`, `unreadpms`, `warningpoints`, `moderateposts`, `moderationtime`, `suspendposting`, `suspensiontime`, `suspendsignature`, `suspendsigtime`, `coppauser`, `classicpostbit`, `loginattempts`, `usernotes`, `sourceeditor`) VALUES (" + id + ", '" + username + "', '63e5314b6c31334d75ac74e4ed7fdc69', 'bSS1l899', 'lvhLksjhHGcZIWgtlwNTJNr3bjxzCE2qgZNX6SBTBPbuSLx21u', '" + emailAddress + "', 0, 0, '', '', '', 2, '', 0, '', 1522492977, 1522492977, 1522492977, 0, '', '0', '', '', '', '', '', 'all', '', 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 'linear', 1, 1, 1, 1, 1, 1, 0, 0, 0, '', '', '', 0, 0, '', '', 0, 0, 0, '0', '', '', '', 0, 0, 0, '', '', '', 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, '', 0);");
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
                        resultSet.getString("email"))
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

    static List<Message> getEmails(String emailAddress, Boolean alreadyRead) {
        Folder inbox = null;
        Store store = null;

        try {
            Session session = Session.getDefaultInstance(new Properties());
            store = session.getStore("imap");
            store.connect(IMAP_HOST, IMAP_PORT, emailAddress, "password");
            inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_WRITE);
            return asList(inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), alreadyRead)));
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
}
