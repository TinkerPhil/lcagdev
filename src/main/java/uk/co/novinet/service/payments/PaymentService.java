package uk.co.novinet.service.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.enquiry.MailSenderService;
import uk.co.novinet.service.member.Member;
import uk.co.novinet.service.member.MemberService;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;

@Service
public class PaymentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    private static final Pattern TRANSACTION_PATTERN = Pattern.compile(
            "Date:.(?<date>\\d{2}/\\d{2}/\\d{4})\\s+" +
            "Description:(?<description>.+)\\s*" +
            "Amount:.(?<amount>\\-?\\d+\\.\\d{2}).([A-Z]+)?\\s*" +
            "Balance:.(?<balance>\\d+\\.\\d{2})"
    );

    private static final List<Pattern> DESCRIPTION_PATTERNS = asList(
            Pattern.compile("FASTER PAYMENTS RECEIPT REF.(?<reference>.{0,18}) FROM (?<counterParty>.*)"),
            Pattern.compile("BILL PAYMENT FROM (?<counterParty>.*), REFERENCE (?<reference>.{0,18})"),
            Pattern.compile("FASTER PAYMENTS RECEIPT  FROM (?<counterParty>.*)")

    );

    private static final char STRANGE_WHITESPACE_CHARACTER = '\u00A0';

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MailSenderService mailSenderService;

    @Autowired
    private PaymentDao paymentDao;

    @Value("${resendFailedEmailsThrottleTimeMilliseconds}")
    private Long resendFailedEmailsThrottleTimeMilliseconds;

    @Scheduled(initialDelayString = "${resendFailedEmailsInitialDelayMilliseconds}", fixedRateString = "${resendFailedEmailsIntervalMilliseconds}")
    public void asynchronouslyProcessBankTransactions() {
        paymentDao.findAssignedBankTransactionsRequiringEmails().forEach(bankTransaction -> {
            attemptToSendBankTransactionEmail(bankTransaction);
            try {
                //throttle email sending so we don't overload hostinger
                sleep(resendFailedEmailsThrottleTimeMilliseconds);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public ImportOutcome importTransactions(String transactions) {
        LOGGER.info("importTransactions called for: {}", transactions);

        int numberOfTransactions = 0;
        int numberOfNewTransactions = 0;

        try {
            BankTransaction mostRecentDbBankTransaction = paymentDao.getMostRecentBankTransaction();
            LOGGER.info("mostRecentDbBankTransaction: {}", mostRecentDbBankTransaction);

            for (BankTransaction bankTransaction : buildBankTransactions(transactions)) {
                LOGGER.info("Comparing most recent bank transaction to: {}", bankTransaction);

                if (mostRecentDbBankTransaction != null) {
                    LOGGER.info("bankTransaction.getDate().equals(mostRecentDbBankTransaction.getDate()): {}", bankTransaction.getDate().equals(mostRecentDbBankTransaction.getDate()));
                    LOGGER.info("bankTransaction.getDate().isAfter(mostRecentDbBankTransaction.getDate()): {}", bankTransaction.getDate().isAfter(mostRecentDbBankTransaction.getDate()));
                }

                if (mostRecentDbBankTransaction == null ||
                        bankTransaction.getDate().isBefore(Instant.now().minus(1, ChronoUnit.DAYS)) && (bankTransaction.getTransactionIndexOnDay() > mostRecentDbBankTransaction.getTransactionIndexOnDay() &&
                                (bankTransaction.getDate().equals(mostRecentDbBankTransaction.getDate())) || bankTransaction.getDate().isAfter(mostRecentDbBankTransaction.getDate()))) {
                    List<BankTransaction> existingBankTransactions = paymentDao.findExistingBankTransaction(bankTransaction);
                    if (existingBankTransactions == null || existingBankTransactions.isEmpty()) {
                        paymentDao.create(bankTransaction);
                        numberOfNewTransactions++;
                        LOGGER.info("Persisting new bank transaction: {}", bankTransaction);
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

    private void attemptToSendBankTransactionEmail(BankTransaction bankTransaction) {
        LOGGER.info("Going to try and send bank transaction email for bankTransaction: {}", bankTransaction);

        Member member = memberService.getMemberById(bankTransaction.getUserId());

        try {
            mailSenderService.sendBankTransactionAssignmentEmail(member, bankTransaction);
            paymentDao.updateEmailSent(true, bankTransaction.getId());
        } catch (Exception e) {
            LOGGER.error("Unable to send email for bank transaction: {}", bankTransaction, e);
        }
    }

    List<BankTransaction> buildBankTransactions(String transactions) {
        String[] transactionChunks = transactions.split("\n\t+\n");
        List<BankTransaction> bankTransactions = new ArrayList<>();
        int bankTransactionCount = 0;

        for (String transactionChunk : transactionChunks) {
            Matcher matcher = TRANSACTION_PATTERN.matcher(transactionChunk);

            Map<String, Integer> transactionsOnDayMap = new HashMap<>();

            boolean foundTransactionInChunk = matcher.find();

            if (foundTransactionInChunk) {
                bankTransactionCount++;
                LOGGER.info("Building BankTransaction index: {}", bankTransactionCount);
                String date = matcher.group("date");
                String description = matcher.group("description").replace(STRANGE_WHITESPACE_CHARACTER, ' ').trim();
                String amount = matcher.group("amount");
                String balance = matcher.group("balance");

                Integer numberOfTransactions = transactionsOnDayMap.get(date);

                int transactionIndexOnDay = numberOfTransactions == null ? 0 : numberOfTransactions + 1;

                transactionsOnDayMap.put(date, transactionIndexOnDay);

                try {
                    String reference = findInDescription(description, "reference");
                    Member member = exactMatchingMember(reference);
                    bankTransactions.add(new BankTransaction(
                            0L,
                            member == null ? null : member.getId(),
                            member == null ? null : member.getUsername(),
                            member == null ? null : member.getEmailAddress(),
                            new SimpleDateFormat("dd/MM/yyyy").parse(date).toInstant(),
                            description,
                            new BigDecimal(amount),
                            new BigDecimal(balance),
                            findInDescription(description, "counterParty"),
                            reference,
                            transactionIndexOnDay,
                            PaymentSource.SANTANDER,
                            false,
                            new BigDecimal(amount).compareTo(BigDecimal.ZERO) > 0));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } else {
                LOGGER.info("Could not find BankTransaction in transaction chunk: {}", transactionChunk);
            }
        }

        //oldest first
        Collections.reverse(bankTransactions);

        return bankTransactions;
    }

    private Member exactMatchingMember(String reference) {
        LOGGER.info("exactMatchingMember called for reference: {}", reference);

        if (reference == null) {
            return null;
        }

        List<Member> members = memberService.findExistingForumUsersByField("username", reference);

        if (members == null || members.size() == 0) {
            LOGGER.info("No matching members found for reference: {}", reference);
            return null;
        }

        if (members.size() == 1) {
            Member member = members.get(0);
            LOGGER.info("Found one member: {} for reference: {}", member, reference);

            if (member.getUsername().length() < 5) {
                LOGGER.info("Member username is not at least 5 characters in length, so certainty of match is not good enough. Returning no match.");
                return null;
            }

            return member;
        }

        LOGGER.warn("Found members: {} who are potential matches for reference: {}", members, reference);
        return null;
    }

    private String findInDescription(String description, String groupName) {
        try {
            for (Pattern pattern : DESCRIPTION_PATTERNS) {
                Matcher matcher = pattern.matcher(description);
                if (matcher.find()) {
                    return matcher.group(groupName);
                }
            }
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Description: '" + description + "' does not contain groupName: " + groupName);
        }

        return "";
    }

    public void assignToMember(Long memberId, Long paymentId) {
        paymentDao.updateMemberId(paymentId, memberId);
        paymentDao.updateEmailSent(false, memberId);
    }

    public void update(Long bankTransactionId, Boolean excludeFromMemberReconciliation) {
        paymentDao.updateExcludeFromMemberReconciliation(bankTransactionId, excludeFromMemberReconciliation);
    }
}
