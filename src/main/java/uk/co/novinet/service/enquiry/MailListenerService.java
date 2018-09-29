package uk.co.novinet.service.enquiry;

import com.sun.mail.imap.IMAPFolder;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.member.Member;
import uk.co.novinet.service.member.MemberCreationResult;
import uk.co.novinet.service.member.MemberService;
import uk.co.novinet.service.member.MpDetailsUpdaterService;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;

@Service
public class MailListenerService {
    private static final List<Boolean> SEEN_FLAG_STATES = asList(TRUE, FALSE);

    private static final Logger LOGGER = LoggerFactory.getLogger(MailListenerService.class);

    private static final List<Pattern> ENQUIRY_PATTERNS = asList(
            Pattern.compile("Message Details: Email (?<emailAddress>.*) Name (?<name>.*) Subject"),
            Pattern.compile("---------------------------\\s+Name:\\s+(?<name>.*)\\s+Email:\\s+(?<emailAddress>.*)")
    );

    @Value("${imapHost}")
    private String imapHost;

    @Value("${imapPort}")
    private int imapPort;

    @Value("${imapUsername}")
    private String imapUsername;

    @Value("${imapPassword}")
    private String imapPassword;

    @Value("${imapProtocol}")
    private String imapProtocol;

    @Value("${processedFolderName}")
    private String processedFolderName;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MailSenderService mailSenderService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MpDetailsUpdaterService mpDetailsUpdaterService;

    @Autowired
    private EnquiryTableListenerService enquiryTableListenerService;

    @Scheduled(initialDelayString = "${retrieveMailInitialDelayMilliseconds}", fixedRateString = "${retrieveMailIntervalMilliseconds}")
    public void retrieveMail() {
        enquiryTableListenerService.processNewEnquiryTableRows();
        mpDetailsUpdaterService.updateMpDetails();

        LOGGER.info("Checking for new enquiry");

        IMAPFolder inbox = null;
        Store store = null;
        Message message = null;

        try {
            Session session = Session.getDefaultInstance(new Properties());
            store = session.getStore(imapProtocol);
            store.connect(imapHost, imapPort, imapUsername, imapPassword);
            inbox = (IMAPFolder) store.getFolder("Inbox");
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

                    MemberCreationResult memberCreationResult = memberService.createForumUserIfNecessary(enquiry);
                    Member member = memberCreationResult.getMember();

                    Date receivedDate = null;

                    if (message != null) {
                        receivedDate = message.getReceivedDate();
                    }

                    if (memberCreationResult.memberAlreadyExisted() && !member.alreadyHaveAnLcagAccountEmailSent()) {
                        LOGGER.info("We already have a member with email address: {} and we haven't already sent them the chaser email. Going to send accountAlreadyExistsEmail...", enquiry.getEmailAddress());
                        mailSenderService.sendAccountAlreadyExistsEmail(member);
                        memberService.markAsAlreadyHaveAnLcagAccountEmailSent(member);
                        moveEmailToProcessedFolder(message, store, inbox, member);
                    } else if (!memberCreationResult.memberAlreadyExisted()) {
                        LOGGER.info("We created a new member with email address: {}. Going to send followUpEmail...", enquiry.getEmailAddress());
                        mailSenderService.sendFollowUpEmail(member);
                        moveEmailToProcessedFolder(message, store, inbox, member);
                    } else {
                        LOGGER.info("Skipping email from: {} as they already have an account and we've already sent them the alreadyHaveAnLcagAccountEmail", enquiry.getEmailAddress());
                    }

                    LOGGER.info(">>> " + enquiry.getName() + "\t " + member.getUsername() + "\t " + enquiry.getEmailAddress() + "\t " + receivedDate);
                }
            }

            LOGGER.info("Finished checking enquiry. Going back to sleep now.");
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

    private void moveEmailToProcessedFolder(Message message, Store store, IMAPFolder inbox, Member member) {
        LOGGER.info("Going to move message to processed folder");
        Folder processedFolder = null;
        try {
            processedFolder = store.getFolder(processedFolderName);
            if (processedFolder.exists()) {
                LOGGER.info("Processed folder {} exists", processedFolderName);
                processedFolder.open(Folder.READ_WRITE);

                LOGGER.info("Inbox contains {} messages", inbox.getMessageCount());
                LOGGER.info("{} contains {} messages", processedFolderName, processedFolder.getMessageCount());

                LOGGER.info("Copying message to {} folder", processedFolderName);
                inbox.copyMessages(new Message[]{message}, processedFolder);

                LOGGER.info("Deleting old message from Inbox");
                message.setFlag(Flags.Flag.DELETED, true);
                inbox.expunge();
            } else {
                LOGGER.info("Could not find processed folder '{}' so going to skip this and leave the message where it was. Also setting the alreadyHaveAnLcagAccountEmailSent flag to true", processedFolderName);
                memberService.markAsAlreadyHaveAnLcagAccountEmailSent(member);
            }
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (processedFolder != null && processedFolder.exists() && processedFolder.isOpen()) {
                    processedFolder.close(true);
                }
            } catch (MessagingException e) {
                throw new RuntimeException(e);
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

    private Enquiry extractEnquiry(String emailBody) {
        for (Pattern enquiryPattern : ENQUIRY_PATTERNS) {
            Matcher matcher = enquiryPattern.matcher(emailBody);

            if (matcher.find()) {
                return new Enquiry(matcher.group("emailAddress"), matcher.group("name"));
            }
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
