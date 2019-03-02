package uk.co.novinet.auth;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.lang.String.format;

@Service
public class MyBbPasswordEncoder implements PasswordEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBbPasswordEncoder.class);

    @Override
    public String encode(CharSequence rawPassword) {
        try {
            return hashPassword(rawPassword.toString(), null);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        throw new NotImplementedException("No salt provided");
    }

    public boolean matches(CharSequence rawPassword, String salt, String encodedPassword) {
        try {
            return hashPassword(rawPassword.toString(), salt).equals(encodedPassword);
        } catch (Exception e) {
            LOGGER.error(format("Unable to match rawPassword: '%s', salt: '%s' with encodedPassword: %s", rawPassword, salt, encodedPassword), e);
        }

        return false;
    }

    public static String hashPassword(String rawPassword, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(rawPassword.getBytes());
        String passwordHash = toHex(md.digest());

        String saltHash = hashSalt(salt);

        md = MessageDigest.getInstance("MD5");
        md.update((saltHash + passwordHash).getBytes());
        return toHex(md.digest());
    }

    private static String hashSalt(String salt) throws NoSuchAlgorithmException {
        if (salt == null) {
            return "";
        }

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(salt.getBytes());
        return toHex(md.digest());
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
