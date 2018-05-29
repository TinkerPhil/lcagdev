package uk.co.novinet.service.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Double.parseDouble;
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
            Pattern.compile("BILL PAYMENT FROM (?<counterParty>.*), REFERENCE (?<reference>.{0,18})")
    );

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PaymentDao paymentDao;

    public ImportOutcome importTransactions(String transactions) {
        int numberOfTransactions = 0;
        int numberOfNewTransactions = 0;

        try {
            for (BankTransaction bankTransaction : buildBankTransactions(transactions)) {
                if (paymentDao.findExistingBankTransaction(bankTransaction) == null || paymentDao.findExistingBankTransaction(bankTransaction).isEmpty()) {
                    paymentDao.create(bankTransaction);
                    numberOfNewTransactions++;
                }
                numberOfTransactions++;
            }
            return new ImportOutcome(numberOfNewTransactions, numberOfTransactions);
        } catch (Exception e) {
            LOGGER.error("Could not import transactions: {}", transactions, e);
            throw new RuntimeException(e);
        }
    }

    List<BankTransaction> buildBankTransactions(String transactions) {
        Matcher matcher = TRANSACTION_PATTERN.matcher(transactions);

        List<BankTransaction> bankTransactions = new ArrayList<>();

        while (matcher.find()) {
            String date = matcher.group("date");
            String description = matcher.group("description").replace('\u00A0',' ').trim();
            String amount = matcher.group("amount");
            String balance = matcher.group("balance");
            try {
                bankTransactions.add(new BankTransaction(
                        0L,
                        null,
                        null,
                        null,
                        new SimpleDateFormat("dd/MM/yyyy").parse(date),
                        description,
                        parseDouble(amount),
                        parseDouble(balance),
                        findInDescription(description, "counterParty"),
                        findInDescription(description, "reference"))
                );
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        //oldest first
        Collections.reverse(bankTransactions);

        return bankTransactions;
    }

    private String findInDescription(String description, String groupName) {
        for (Pattern pattern : DESCRIPTION_PATTERNS) {
            Matcher matcher = pattern.matcher(description);
            if (matcher.find()) {
                return matcher.group(groupName);
            }
        }
        return null;
    }

    public void assignToMember(Long memberId, Long paymentId) {
        paymentDao.updateMemberId(paymentId, memberId);
    }
}
