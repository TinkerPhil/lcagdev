package uk.co.novinet.service.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.enquiry.MailSenderService;
import uk.co.novinet.service.member.Member;
import uk.co.novinet.service.member.MemberService;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

@Service
public class PaymentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    private static final Pattern TRANSACTION_PATTERN = Pattern.compile(
            "Date:.(?<date>\\d{2}/\\d{2}/\\d{4})\\s+" +
            "Description:(?<description>.+)\\s*" +
            "Amount:.(?<amount>\\d+\\.\\d{2}).\\s*" +
            "Balance:.(?<balance>\\d+\\.\\d{2})"
    );

    private static final List<Pattern> DESCRIPTION_PATTERNS = asList(
            Pattern.compile("FASTER PAYMENTS RECEIPT REF.(?<reference>.{0,18}) FROM (?<counterParty>.*)"),
            Pattern.compile("BILL PAYMENT FROM (?<counterParty>.*), REFERENCE (?<reference>.{0,18})"),
            Pattern.compile("FASTER PAYMENTS RECEIPT  FROM (?<counterParty>.*)")

    );

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MailSenderService mailSenderService;

    @Autowired
    private PaymentDao paymentDao;

    public ImportOutcome importTransactions(String transactions) {
        int numberOfTransactions = 0;
        int numberOfNewTransactions = 0;

        try {
            for (BankTransaction bankTransaction : buildBankTransactions(transactions)) {
                List<BankTransaction> existingBankTransactions = paymentDao.findExistingBankTransaction(bankTransaction);
                if (existingBankTransactions == null || existingBankTransactions.isEmpty()) {
                    paymentDao.create(bankTransaction);

                    if (bankTransaction.getUserId() != null) {
                        Member member = memberService.getMemberById(bankTransaction.getUserId());
                        mailSenderService.sendBankTransactionAssignmentEmail(member, bankTransaction);
                    }

                    numberOfNewTransactions++;
                    LOGGER.info("Persisting new bank transaction: {}", bankTransaction);
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

    List<BankTransaction> buildBankTransactions(String transactions) {
        Matcher matcher = TRANSACTION_PATTERN.matcher(transactions);

        List<BankTransaction> bankTransactions = new ArrayList<>();

        Map<String, Integer> transactionsOnDayMap = new HashMap<>();

        while (matcher.find()) {
            String date = matcher.group("date");
            String description = matcher.group("description").replace('\u00A0',' ').trim();
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
                        PaymentSource.SANTANDER));
            } catch (ParseException e) {
                throw new RuntimeException(e);
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
        mailSenderService.sendBankTransactionAssignmentEmail(memberService.getMemberById(memberId), paymentDao.getBankTransactionById(paymentId));
    }
}
