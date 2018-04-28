package uk.co.novinet.service;

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


}
