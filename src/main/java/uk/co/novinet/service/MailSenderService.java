package uk.co.novinet.service;

import org.apache.commons.io.IOUtils;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.codemonkey.simplejavamail.email.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

@Service
public class MailSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailSenderService.class);

    @Value("${smtpHost}")
    private String smtpHost;

    @Value("${smtpPort}")
    private int smtpPort;

    @Value("${smtpUsername}")
    private String smtpUsername;

    @Value("${smtpPassword}")
    private String smtpPassword;

    @Value("${emailSourceUrl}")
    private String emailSourceUrl;

    @Value("${emailAttachmentId}")
    private String emailAttachmentId;

    @Value("${emailSubject}")
    private String emailSubject;

    @Value("${emailFromName}")
    private String emailFromName;

    @Value("#{'${bccEmailRecipients}'.split(',')}")
    private List<String> bccEmailRecipients;

    public void sendFollowUpEmail(ForumUser forumUser) throws Exception {
        Email email = new Email();

        email.setFromAddress(emailFromName, smtpUsername);

        if (bccEmailRecipients != null && !bccEmailRecipients.isEmpty()) {
            bccEmailRecipients.forEach(bccEmailRecipient -> {
                if (bccEmailRecipient != null && !bccEmailRecipient.trim().isEmpty()) {
                    email.addRecipient(bccEmailRecipient, bccEmailRecipient, MimeMessage.RecipientType.BCC);
                }
            });
        }

        email.addRecipient(forumUser.getEmailAddress(), forumUser.getEmailAddress(), MimeMessage.RecipientType.TO);
        email.setTextHTML(replaceTokens(retrieveEmailBodyHtmlFromGoogleDocs(), forumUser));
        email.setSubject(emailSubject);
        byte[] pdfBytes = retrievePdfFromGoogleDrive();

        if (pdfBytes != null) {
            email.addAttachment("Response for Membership Request.pdf", pdfBytes, "application/pdf");
        } else {
            LOGGER.error("Could not get pdf bytes from google drive!");
        }

        LOGGER.error("Going to try sending email to new memeber {}", forumUser.getEmailAddress());
        new Mailer(smtpHost, smtpPort, smtpUsername, smtpPassword, TransportStrategy.SMTP_TLS).sendMail(email);
        LOGGER.error("Email successfully sent to new member {}", forumUser.getEmailAddress());
    }

    private byte[] retrievePdfFromGoogleDrive() {
        try {
            return IOUtils.toByteArray(new URL("https://drive.google.com/uc?export=download&id=" + emailAttachmentId));
        } catch (IOException e) {
            LOGGER.error("Error occurred trying to download attachment", e);
        }

        return null;
    }

    private String replaceTokens(String emailTemplate, ForumUser forumUser) {
        return emailTemplate.replace("$USERNAME", forumUser.getUsername())
                .replace("$PASSWORD", forumUser.getPasswordDetails().getPassword())
                .replace("$NAME", forumUser.getName());
    }

    private String retrieveEmailBodyHtmlFromGoogleDocs() throws IOException {
        try (Scanner scanner = new Scanner(new URL(emailSourceUrl).openStream(), StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}
