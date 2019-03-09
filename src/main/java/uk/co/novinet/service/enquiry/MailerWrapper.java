package uk.co.novinet.service.enquiry;

import org.codemonkey.simplejavamail.email.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MailerWrapper {

    @Autowired
    private MailerProvider mailerProvider;

    public void sendMail(Email email) {
        mailerProvider.provide().sendMail(email);
    }
}
