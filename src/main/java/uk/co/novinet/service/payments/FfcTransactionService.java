package uk.co.novinet.service.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.PersistenceUtils;
import uk.co.novinet.service.enquiry.MailSenderService;
import uk.co.novinet.service.member.Member;
import uk.co.novinet.service.member.MemberService;
import uk.co.novinet.service.member.Where;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static uk.co.novinet.service.PersistenceUtils.*;

@Service
public class FfcTransactionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FfcTransactionService.class);

    private static final Pattern TRANSACTION_PATTERN = Pattern.compile(
            "(?<date>\\d{2}/\\d{2}/\\d{4}),"
            + "(?<card>[^,]+),"
            + "(?<type>[^,]+),"
            + "(?<description>[^,]+),"
            + "[^\\d]{1,3}(?<credit>[0-9,]+\\.\\d+)\"*,"
            + "[^\\d]{1,3}(?<debit>[0-9,]+\\.\\d+)\"*,"
    );

    @Value("${forumDatabaseTablePrefix}")
    private String forumDatabaseTablePrefix;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MemberService memberService;

    @Autowired
    private FfcTransactionDao ffcDao;

    public ImportOutcome importTransactions(String transactions) {
        LOGGER.info("importTransactions called for: {}", transactions);

        int numberOfTransactions = 0;
        int numberOfNewTransactions = 0;

        try {
            FfcTransaction mostRecentDbFfcTransaction = ffcDao.getMostRecentFfcTransaction();
            LOGGER.info("mostRecentDbFfcTransaction: {}", mostRecentDbFfcTransaction);

            for (FfcTransaction ffcTransaction : buildFfcTransactions(transactions)) {
                LOGGER.info("Comparing most recent FFC transaction to: {}", ffcTransaction);

                if (mostRecentDbFfcTransaction != null) {
                    LOGGER.info("ffcTransaction.getTxnDate().equals(mostRecentDbFfcTransaction.getDate()): {}", ffcTransaction.getTxnDate().equals(mostRecentDbFfcTransaction.getTxnDate()));
                    LOGGER.info("ffcTransaction.getTxnDate().isAfter(mostRecentDbFfcTransaction.getDate()): {}", ffcTransaction.getTxnDate().after(mostRecentDbFfcTransaction.getTxnDate()));
                }

                if (mostRecentDbFfcTransaction == null ||
                        ffcTransaction.getTxnDate().before(Date.from(Instant.now().minus(1, ChronoUnit.DAYS))) &&
                                (ffcTransaction.getTxnDate().equals(mostRecentDbFfcTransaction.getTxnDate()))
                                    || ffcTransaction.getTxnDate().after(mostRecentDbFfcTransaction.getTxnDate())) {
                    List<FfcTransaction> existingFfcTransactions = ffcDao.findExistingFfcTransaction(ffcTransaction);
                    if (existingFfcTransactions == null || existingFfcTransactions.isEmpty()) {
                        ffcDao.create(ffcTransaction);

                        numberOfNewTransactions++;
                        LOGGER.info("Persisting new FFC transaction: {}", ffcTransaction);
                    }
                }

                numberOfTransactions++;
            }
            ImportOutcome importOutcome = new ImportOutcome(numberOfNewTransactions, numberOfTransactions);
            LOGGER.info("importOutcome: {}", importOutcome);
            return importOutcome;
        } catch (Exception e) {
            LOGGER.error("Could not import transactions: {}", transactions, e);
            throw new RuntimeException(e);
        }
    }

    List<FfcTransaction> buildFfcTransactions(String transactions) {
        Matcher matcher = TRANSACTION_PATTERN.matcher(transactions);
        Pattern splitter = Pattern.compile("^.*?(?<username>[^,\\s]+)$");
        Matcher split;

        List<FfcTransaction> ffcTransactions = new ArrayList<>();

        Map<String, Integer> transactionsOnDayMap = new HashMap<>();

        while (matcher.find()) {
            String date = matcher.group("date");
            String description = matcher.group("description").trim();
            String credit = matcher.group("credit");
            credit = credit.replaceAll(",", "");
            String debit = matcher.group("debit");
            debit = debit.replaceAll(",", "");

            String amount = credit.equalsIgnoreCase("0.00") ? debit : credit;

            Integer numberOfTransactions = transactionsOnDayMap.get(date);

            int transactionIndexOnDay = numberOfTransactions == null ? 0 : numberOfTransactions + 1;

            transactionsOnDayMap.put(date, transactionIndexOnDay);

            try {
                String username;
                Member member = null;
                split = splitter.matcher(description);
                if (split.find()) {
                    username = split.group("username").trim();
                    username = username.replaceAll("-", "_");
                    member = exactMatchingMember(username);
                }
                ffcTransactions.add(new FfcTransaction(
                        0L,
                        new SimpleDateFormat("dd/MM/yyyy").parse(date),
                        description,
                        new BigDecimal(amount),
                        member == null ? null : member.getId()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        //oldest first
        Collections.reverse(ffcTransactions);

        return ffcTransactions;
    }

    private Member exactMatchingMember(String username) {
        LOGGER.info("exactMatchingMember called for username: {}", username);

        if (username == null) {
            return null;
        }

        List<Member> members = memberService.findExistingForumUsersByField("username", username);
        if ((members == null || members.size() == 0) && username.length() > 4) {
            members = memberService.findExistingForumUsersByFieldWild("username", username + "%");
        }

        if (members == null || members.size() == 0) {
            LOGGER.info("No matching members found for reference: {}", username);
            return null;
        }

        if (members.size() == 1) {
            Member member = members.get(0);
            LOGGER.info("Found one member: {} for reference: {}", member, username);

            if (member.getUsername().length() < 5) {
                LOGGER.info("Member username is not at least 5 characters in length, so certainty of match is not good enough. Returning no match.");
                return null;
            }

            return member;
        }

        LOGGER.warn("Found members: {} who are potential matches for reference: {}", members, username);
        return null;
    }

    private Map<String, String> FIELD_TO_COLUMN_TRANSLATIONS = new HashMap<String, String>() {{
        put("id", "fb.id");
        put("txnDate", "fb.txnDate");
        put("description", "fb.description");
        put("amount", "fb.amount");
        put("uid", "fb.uid");
    }};

}
