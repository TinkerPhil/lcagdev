package uk.co.novinet.service;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Service
public class MailListenerService {
    private static final List<Boolean> SEEN_FLAG_STATES = Arrays.asList(TRUE, FALSE);

    private static final Logger LOGGER = LoggerFactory.getLogger(MailListenerService.class);

    private static final Pattern ENQUIRY_PATTERN = Pattern.compile("Message Details: Email (?<emailAddress>.*) Name (?<name>.*) Subject");

    @Value("${imapHost}")
    private String imapHost;

    @Value("${imapPort}")
    private int imapPort;

    @Value("${imapUsername}")
    private String imapUsername;

    @Value("${imapPassword}")
    private String imapPassword;

    @Value("${forumDatabaseTablePrefix}")
    private String forumDatabaseTablePrefix;

    @Value("${imapProtocol}")
    private String imapProtocol;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MailSenderService mailSenderService;

    @Scheduled(initialDelayString = "${retrieveMailInitialDelayMilliseconds}", fixedRateString = "${retrieveMailIntervalMilliseconds}")
    public void retrieveMail() {
        LOGGER.info("Checking for new mail");

        Folder inbox = null;
        Store store = null;
        Message message = null;

        try {
            Session session = Session.getDefaultInstance(new Properties());
            store = session.getStore(imapProtocol);
            store.connect(imapHost, imapPort, imapUsername, imapPassword);
            inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_WRITE);

            for (Boolean originalSeenFlagState : SEEN_FLAG_STATES) {
                LOGGER.info("Going to check messages with SEEN FLAG={}", originalSeenFlagState);

                Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), originalSeenFlagState));

                LOGGER.info("Found {} messages to process", messages.length);

                for (Message m : messages) {
                    message = m;

                    String emailBody = getTextFromMessage(message);
                    Enquiry enquiry = extractEnquiry(emailBody);

                    if (enquiry == null) {
                        LOGGER.info("Enquiry not found in email body", emailBody);
                        LOGGER.info("Skipping...");
                        restoreOriginalMessageFlags(message, originalSeenFlagState);
                        continue;
                    }

                    LOGGER.info("Found enquiry: {}", enquiry);

                    ForumUser forumUser = createForumUserIfNecessary(enquiry);

                    if (forumUser == null) {
                        LOGGER.info("We didn't create a forum user, so set this message to SEEN={} again", originalSeenFlagState);
                        restoreOriginalMessageFlags(message, originalSeenFlagState);
                    } else {
                        mailSenderService.sendFollowUpEmail(forumUser);
                        LOGGER.info(">>> " + enquiry.getName() + "\t " + forumUser.getUsername() + "\t " + enquiry.getEmailAddress() + "\t " + message.getReceivedDate());
                    }
                }
            }

            LOGGER.info("Finished checking mail. Going back to sleep now.");
        } catch (Exception e) {
            LOGGER.error("An error occurred while trying to read emails", e);
        } finally {
            if (inbox != null) {
                try {
                    inbox.close(false);
                } catch (MessagingException e) {
                    LOGGER.error("Error occurred trying to close inbox", e);
                }
            }
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    LOGGER.error("Error occurred trying to close store", e);
                }
            }
        }
    }

    private void restoreOriginalMessageFlags(Message message, boolean originalSeenFlagState) {
        try {
            LOGGER.info("Going to set current email status back to SEEN={}", originalSeenFlagState);
            if (message != null) {
                message.setFlag(Flags.Flag.SEEN, originalSeenFlagState);
            }
        } catch (MessagingException me) {
            LOGGER.error("An error occurred while trying to set email read status to unread", me);
        }
    }

    private ForumUser createForumUserIfNecessary(Enquiry enquiry) {
        List<ForumUser> existingForumUsers = findExistingForumUsersByEmailAddress(enquiry.getEmailAddress());

        if (!existingForumUsers.isEmpty()) {
            LOGGER.info("Already existing forum user with email address {}", enquiry.getEmailAddress());
            LOGGER.info("Skipping");
        } else {
            LOGGER.info("No existing forum user found with email address: {}", enquiry.getEmailAddress());
            LOGGER.info("Going to create one");

            ForumUser forumUser = new ForumUser(enquiry.getEmailAddress(), extractUsername(enquiry.getEmailAddress()), enquiry.getName(), PasswordSource.getRandomPasswordDetails());

            Long max = jdbcTemplate.queryForObject("select max(uid) from " + forumDatabaseTablePrefix + "users", Long.class);

            if (max == null) {
                max = (long) 1;
            } else {
                max = max + 1;
            }

            String insertSql = "insert into `" + forumDatabaseTablePrefix + "users` (`uid`, `username`, `password`, `salt`, `loginkey`, `email`, `postnum`, `threadnum`, `avatar`, " +
                    "`avatardimensions`, `avatartype`, `usergroup`, `additionalgroups`, `displaygroup`, `usertitle`, `regdate`, `lastactive`, `lastvisit`, `lastpost`, `website`, `icq`, " +
                    "`aim`, `yahoo`, `skype`, `google`, `birthday`, `birthdayprivacy`, `signature`, `allownotices`, `hideemail`, `subscriptionmethod`, `invisible`, `receivepms`, `receivefrombuddy`, " +
                    "`pmnotice`, `pmnotify`, `buddyrequestspm`, `buddyrequestsauto`, `threadmode`, `showimages`, `showvideos`, `showsigs`, `showavatars`, `showquickreply`, `showredirect`, `ppp`, `tpp`, " +
                    "`daysprune`, `dateformat`, `timeformat`, `timezone`, `dst`, `dstcorrection`, `buddylist`, `ignorelist`, `style`, `away`, `awaydate`, `returndate`, `awayreason`, `pmfolders`, `notepad`, " +
                    "`referrer`, `referrals`, `reputation`, `regip`, `lastip`, `language`, `timeonline`, `showcodebuttons`, `totalpms`, `unreadpms`, `warningpoints`, `moderateposts`, `moderationtime`, " +
                    "`suspendposting`, `suspensiontime`, `suspendsignature`, `suspendsigtime`, `coppauser`, `classicpostbit`, `loginattempts`, `usernotes`, `sourceeditor`) " +
                    "VALUES (?, ?, ?, ?, 'lvhLksjhHGcZIWgtlwNTJNr3bjxzCE2qgZNX6SBTBPbuSLx21u', ?, 0, 0, '', '', '', 8, '', 0, '', ?, ?, ?, 0, '', '0', '', '', '', '', '', " +
                    "'all', '', 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 'linear', 1, 1, 1, 1, 1, 1, 0, 0, 0, '', '', '', 0, 0, '', '', 0, 0, 0, '0', '', '', '', 0, 0, 0, '', '', '', 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, " +
                    "0, 0, 1, '', 0);";

            LOGGER.info("Going to execute insert sql: {}", insertSql);

            int result = jdbcTemplate.update(insertSql, new Object[] {
                    max,
                    forumUser.getUsername(),
                    forumUser.getPasswordDetails().getPasswordHash(),
                    forumUser.getPasswordDetails().getSalt(),
                    forumUser.getEmailAddress(),
                    unixTime(),
                    unixTime(),
                    unixTime()
            });

            LOGGER.info("Insertion result: {}", result);

            return forumUser;
        }

        return null;
    }

    private long unixTime() {
        return new Date().getTime() / 1000;
    }

    private String extractUsername(String emailAddress) {
        String usernameCandidate = firstBitOfEmailAddress(emailAddress);
        LOGGER.info("Candidate username: {}", usernameCandidate);

        if (usernameCandidate.length() < 3 || !findExistingForumUsersByUsername(usernameCandidate).isEmpty()) {
            do {
                LOGGER.info("Candidate username: {} already exists! Going to try creating another one.", usernameCandidate);
                usernameCandidate = usernameCandidate(emailAddress);
                LOGGER.info("New candidate username: {}", usernameCandidate);
            } while (!findExistingForumUsersByUsername(usernameCandidate).isEmpty());
        }

        LOGGER.info("Settled on username: {}", usernameCandidate);

        return usernameCandidate;
    }

    private List<ForumUser> findExistingForumUsersByUsername(String username) {
        return jdbcTemplate.query("select * from " + forumDatabaseTablePrefix + "users where username = ?", new Object[]{ username },
                (rs, rowNum) -> new ForumUser(rs.getString("email"), rs.getString("username"), null, null)
        );
    }

    private List<ForumUser> findExistingForumUsersByEmailAddress(String emailAddress) {
        return jdbcTemplate.query("select * from " + forumDatabaseTablePrefix + "users where email = ?", new Object[]{ emailAddress },
                (rs, rowNum) -> new ForumUser(rs.getString("email"), rs.getString("username"), null, null)
        );
    }

    private String usernameCandidate(String emailAddress) {
        return firstBitOfEmailAddress(emailAddress) + randomDigit() + randomDigit();
    }

    private char randomDigit() {
        return String.valueOf(new Random().nextInt()).charAt(0);
    }

    private String firstBitOfEmailAddress(String emailAddress) {
        return emailAddress.substring(0, emailAddress.indexOf("@")).replace("\\.", "_");
    }

    private Enquiry extractEnquiry(String emailBody) {
        Matcher matcher = ENQUIRY_PATTERN.matcher(emailBody);

        if (matcher.find()) {
            return new Enquiry(matcher.group("emailAddress"), matcher.group("name"));
        }

        return null;
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart)  throws MessagingException, IOException {
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
