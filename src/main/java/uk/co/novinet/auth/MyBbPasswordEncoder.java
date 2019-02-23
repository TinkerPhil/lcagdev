package uk.co.novinet.auth;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

import static java.lang.String.format;
import static uk.co.novinet.service.enquiry.PasswordSource.toHex;

@Service
public class MyBbPasswordEncoder implements PasswordEncoder {

    //hopefully an unusual enough sequence to not actually be part of someone's password...
    public static final String SALT_DELIMITER = "§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§";

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBbPasswordEncoder.class);

    @Override
    public String encode(CharSequence rawPassword) {
        throw new NotImplementedException("We're not going to be doing any encoding...");
    }

    @Override
    public boolean matches(CharSequence rawPasswordWithSalt, String encodedPassword) {
        try {
            String[] parts = rawPasswordWithSalt.toString().split(SALT_DELIMITER);

            String rawPassword = parts[0];
            String salt = parts[1];

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(rawPassword.getBytes());
            String passwordHash = toHex(md.digest());

            md = MessageDigest.getInstance("MD5");
            md.update(salt.getBytes());
            String saltHash = toHex(md.digest());

            md = MessageDigest.getInstance("MD5");
            md.update((saltHash + passwordHash).getBytes());
            String combinedHash = toHex(md.digest());

            return combinedHash.equals(encodedPassword);
        } catch (Exception e) {
            LOGGER.error(format("Unable to match rawPasswordWithSalt: %s with encodedPassword: %s", rawPasswordWithSalt, encodedPassword), e);
        }

        return false;
    }
}
