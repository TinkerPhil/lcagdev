package uk.co.novinet.service.enquiry;

import org.apache.commons.codec.digest.Md5Crypt;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PasswordSource {

    private static List<PasswordDetails> PASSWORD_DETAILS = Arrays.asList(
            new PasswordDetails("TXpv6XE4", "34fd4df51ada6f1305ef89196b37bf17", "2019l0anCharg3"),
            new PasswordDetails("4McxEuH8", "f28aec15073d8fda5aecbfff1e89289a", "hm7cL04nch4rGe"),
            new PasswordDetails("5lV5YuUb", "395737b496bab310403fe3f1af0a4379", "lc4g2019Ch4rg3"),
            new PasswordDetails("JFRkOmgK", "20cd8eefbf0f666c3843da640d0ca93d", "ch4l1Eng3Hm7C")
    );

    public static PasswordDetails getRandomPasswordDetails() {
        return PASSWORD_DETAILS.get(new Random().nextInt(PASSWORD_DETAILS.size()));
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update("password".getBytes());
        String passwordHash = toHex(md.digest());

        md = MessageDigest.getInstance("MD5");
        md.update("salt".getBytes());
        String saltHash = toHex(md.digest());

        md = MessageDigest.getInstance("MD5");
        md.update((saltHash + passwordHash).getBytes());
        String combinedHash = toHex(md.digest());

        System.out.println(combinedHash);
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

}
