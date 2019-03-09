package uk.co.novinet.service.enquiry;

import org.apache.commons.io.IOUtils;
import org.codemonkey.simplejavamail.email.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.member.Member;
import uk.co.novinet.service.payments.BankTransaction;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;

import static java.text.NumberFormat.getNumberInstance;
import static java.util.Locale.UK;

@Service
public class MailSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailSenderService.class);

    @Autowired
    private MailerWrapper mailerWrapper;

    @Autowired
    private GoogleDocsReader googleDocsReader;

    @Value("${smtpUsername}")
    private String smtpUsername;

    @Value("${emailSourceUrl}")
    private String emailSourceUrl;

    @Value("${verificationEmailSourceUrl}")
    private String verificationEmailSourceUrl;

    @Value("${verificationEmailSubject}")
    private String verificationEmailSubject;

    @Value("${paymentReceivedEmailSourceUrl}")
    private String paymentReceivedEmailSourceUrl;

    @Value("${paymentReceivedEmailSubject}")
    private String paymentReceivedEmailSubject;

    @Value("${upgradedToFullMembershipEmailSourceUrl}")
    private String upgradedToFullMembershipEmailSourceUrl;

    @Value("${upgradedToFullMembershipEmailSubject}")
    private String upgradedToFullMembershipEmailSubject;

    @Value("${alreadyHaveAnLcagAccountEmailSourceUrl}")
    private String alreadyHaveAnLcagAccountEmailSourceUrl;

    @Value("${alreadyHaveAnLcagAccountEmailSubject}")
    private String alreadyHaveAnLcagAccountEmailSubject;

    @Value("${refundProcessedEmailSourceUrl}")
    private String refundProcessedEmailSourceUrl;

    @Value("${refundProcessedEmailSubject}")
    private String refundProcessedEmailSubject;

    @Value("${emailSubject}")
    private String emailSubject;

    @Value("${emailFromName}")
    private String emailFromName;

    @Value("#{'${bccEmailRecipients}'.split(',')}")
    private List<String> bccEmailRecipients;

    public void sendFollowUpEmail(Member member) {
        LOGGER.info("Going to send initial follow up email to member: {}", member);
        sendEmail(member, null, emailSubject, emailSourceUrl, null);
    }

    public void sendAccountAlreadyExistsEmail(Member member) {
        LOGGER.info("Going to send account already exists email to member: {}", member);
        sendEmail(member, null, alreadyHaveAnLcagAccountEmailSubject, alreadyHaveAnLcagAccountEmailSourceUrl, null);
    }

    public void sendVerificationEmail(Member member) {
        LOGGER.info("Going to send document verification email to member: {}", member);
        sendEmail(member, null, verificationEmailSubject, verificationEmailSourceUrl, null);
    }

    public void sendBankTransactionAssignmentEmail(Member member, BankTransaction bankTransaction) {
        LOGGER.info("Going to send payment or refund processed email to member: {}", member);
        if (bankTransaction.getAmount().compareTo(BigDecimal.ZERO) >= 0) {
            sendEmail(member, bankTransaction, paymentReceivedEmailSubject, paymentReceivedEmailSourceUrl, null);
        } else {
            sendEmail(member, bankTransaction, refundProcessedEmailSubject, refundProcessedEmailSourceUrl, null);
        }
    }

    public void sendUpgradedToFullMembershipEmail(Member member) {
        LOGGER.info("Going to send upgraded to full membership email to member: {}", member);
        sendEmail(member, null, upgradedToFullMembershipEmailSubject, upgradedToFullMembershipEmailSourceUrl, null);
    }

    private byte[] retrievePdfFromGoogleDrive(String googleDriveAttachmentId) {
        try {
            return IOUtils.toByteArray(new URL("https://drive.google.com/uc?export=download&id=" + googleDriveAttachmentId));
        } catch (IOException e) {
            LOGGER.error("Error occurred trying to download attachment", e);
        }

        return null;
    }

    public void sendEmail(Member member, BankTransaction bankTransaction, String subject, String emailSourceUrl, GoogleDriveMailAttachment googleDriveMailAttachment) {
        try {
            Email email = new Email();

            email.setFromAddress(emailFromName, smtpUsername);

            if (bccEmailRecipients != null && !bccEmailRecipients.isEmpty()) {
                bccEmailRecipients.forEach(bccEmailRecipient -> {
                    if (bccEmailRecipient != null && !bccEmailRecipient.trim().isEmpty()) {
                        email.addRecipient(bccEmailRecipient, bccEmailRecipient, MimeMessage.RecipientType.BCC);
                    }
                });
            }

            email.addRecipient(member.getEmailAddress(), member.getEmailAddress(), MimeMessage.RecipientType.TO);
            email.setTextHTML(replaceTokens(googleDocsReader.retrieveEmailBodyHtmlFromGoogleDocs(emailSourceUrl), member, bankTransaction));
            email.setSubject(subject);

            if (googleDriveMailAttachment != null) {
                byte[] pdfBytes = retrievePdfFromGoogleDrive(googleDriveMailAttachment.getGoogleDriveAttachmentId());

                if (pdfBytes != null) {
                    email.addAttachment(googleDriveMailAttachment.getAttachmentFilename(), pdfBytes, googleDriveMailAttachment.getAttachmentContentType());
                } else {
                    LOGGER.error("Could not get pdf bytes from google drive!");
                }
            }

            LOGGER.info("Going to try sending email with sourceUrl: {} to member {}", emailSourceUrl, member.getEmailAddress());
            mailerWrapper.sendMail(email);
            LOGGER.info("Email successfully sent to member {}", member.getEmailAddress());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String replaceTokens(String emailTemplate, Member member, BankTransaction bankTransaction) {
        String substitutedMemberTokens = emailTemplate
                .replace("$USERNAME", member.getUsername())
                .replace("$PASSWORD", member.getPasswordDetails() == null ? "" : (member.getPasswordDetails().getPassword() == null ? "" : member.getPasswordDetails().getPassword()))
                .replace("$NAME", member.getName())
                .replace("$TOKEN", member.getToken())
                .replace("$CLAIM_TOKEN", member.getClaimToken());

        if (bankTransaction != null) {
            return substitutedMemberTokens.replace("$AMOUNT", getNumberInstance(UK).format(bankTransaction.getAmount()));
        }

        return substitutedMemberTokens;
    }

    public void setGoogleDocsReader(GoogleDocsReader googleDocsReader) {
        this.googleDocsReader = googleDocsReader;
    }
}
