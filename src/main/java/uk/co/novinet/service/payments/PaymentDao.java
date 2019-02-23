package uk.co.novinet.service.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.PersistenceUtils;
import uk.co.novinet.service.member.MemberService;
import uk.co.novinet.service.member.Where;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static uk.co.novinet.service.PersistenceUtils.*;

@Service
public class PaymentDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberService.class);

    private Map<String, String> FIELD_TO_COLUMN_TRANSLATIONS = new HashMap<String, String>() {{
        put("id", "bt.id");
        put("userId", "bt.user_id");
        put("date", "bt.date");
        put("description", "bt.description");
        put("amount", "bt.amount");
        put("runningBalance", "bt.running_balance");
        put("counterParty", "bt.counter_party");
        put("reference", "bt.reference");
        put("transactionIndexOnDay", "bt.transaction_index_on_day");
        put("paymentSource", "bt.payment_source");
        put("emailSent", "bt.email_sent");
    }};

    @Value("${forumDatabaseTablePrefix}")
    private String forumDatabaseTablePrefix;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void create(BankTransaction bankTransaction) {
        LOGGER.info("Going to create bank transaction: {}", bankTransaction);

        Long nextAvailableId = findNextAvailableId("id", bankTransactionsTableName());

        bankTransaction.setId(nextAvailableId);

        String sql = "insert into " + bankTransactionsTableName() + " (`id`, `user_id`, `date`, `description`, `amount`, `running_balance`, `counter_party`, `reference`, `transaction_index_on_day`, `payment_source`, `email_sent`) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LOGGER.info("sql: {}", sql);

        int result = jdbcTemplate.update(
                sql,
                nextAvailableId,
                bankTransaction.getUserId(),
                unixTime(bankTransaction.getDate()),
                bankTransaction.getDescription() == null ? "" : bankTransaction.getDescription(),
                bankTransaction.getAmount(),
                bankTransaction.getRunningBalance(),
                bankTransaction.getCounterParty() == null ? "" : bankTransaction.getCounterParty(),
                bankTransaction.getReference() == null ? "" : bankTransaction.getReference(),
                bankTransaction.getTransactionIndexOnDay(),
                String.valueOf(bankTransaction.getPaymentSource()),
                bankTransaction.getEmailSent()
        );

        LOGGER.info("Insertion result: {}", result);
    }

    public BankTransaction getBankTransactionById(Long id) {
        return jdbcTemplate.queryForObject(buildBankTransactionTableSelect() + " where bt.id = ?", new Object[] { id }, (rs, rowNum) -> buildBankTransaction(rs));
    }

    public List<FfcContribution> getFfcContributions() {
        return jdbcTemplate.query("select * from " + ffcContributionsTableName() + " where status = 'AUTHORIZED'", (rs, rowNum) -> buildFfcContribution(rs));
    }

    public BigDecimal getFfcTotalContributions() {
        return jdbcTemplate.queryForObject("select sum((gross_amount * 0.983) - 0.2) from " + ffcContributionsTableName() + " where status = 'AUTHORIZED'", BigDecimal.class);
    }


    private FfcContribution buildFfcContribution(ResultSet rs) {
        try {
            return new FfcContribution(dateFromMyBbRow(rs, "payment_received"), new BigDecimal((rs.getDouble("gross_amount") * 0.983) - 0.20));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<BankTransaction> findExistingBankTransaction(BankTransaction bankTransaction) {
        LOGGER.info("Going to try and find existing bank transaction like: {}", bankTransaction);

        String sql = buildBankTransactionTableSelect() + " where bt.date = ? and bt.description = ? and bt.amount = ? and bt.running_balance = ? and bt.transaction_index_on_day = ? and bt.payment_source = ?";

        Object[] arguments = {
                unixTime(bankTransaction.getDate()),
                bankTransaction.getDescription(),
                bankTransaction.getAmount(),
                bankTransaction.getRunningBalance(),
                bankTransaction.getTransactionIndexOnDay(),
                String.valueOf(bankTransaction.getPaymentSource())
        };

        LOGGER.info("Going to execute sql: {}", sql);
        LOGGER.info("With arguments: {}", asList(arguments));

        return jdbcTemplate.query(sql, arguments, (rs, rowNum) -> buildBankTransaction(rs));
    }

    public BankTransaction getMostRecentBankTransaction() {
        LOGGER.info("Finding most recent bank transaction");

        String sql = buildBankTransactionTableSelect() + " order by `date`, `transaction_index_on_day` desc limit 1";

        LOGGER.info("Going to execute sql: {}", sql);

        List<BankTransaction> results = jdbcTemplate.query(sql, (rs, rowNum) -> buildBankTransaction(rs));

        if (results == null || results.isEmpty()) {
            return null;
        }

        return results.get(0);
    }

    private BankTransaction buildBankTransaction(ResultSet rs) throws SQLException {
        String userId = rs.getString("user_id");

        BankTransaction bankTransaction = new BankTransaction(
                rs.getLong("bt.id"),
                userId == null ? null : Long.parseLong(userId),
                rs.getString("u.username"),
                rs.getString("u.email"),
                dateFromMyBbRow(rs, "bt.date"),
                rs.getString("bt.description"),
                rs.getBigDecimal("bt.amount"),
                rs.getBigDecimal("bt.running_balance"),
                rs.getString("bt.counter_party"),
                rs.getString("bt.reference"),
                rs.getInt("bt.transaction_index_on_day"),
                PaymentSource.valueOf(rs.getString("bt.payment_source")),
                rs.getBoolean("bt.email_sent"));

        LOGGER.info("Retrieved bank transaction: {}", bankTransaction);

        return bankTransaction;
    }

    private String buildBankTransactionTableSelect() {
        return "select * from " + bankTransactionsTableName() + " bt left outer join " + usersTableName() + " u on u.uid = bt.user_id ";
    }

    public long searchCountBankTransactions(BankTransaction bankTransaction, String operator) {
        Where where = buildWhereClause(bankTransaction, operator);
        final boolean hasWhere = !"".equals(where.getSql());
        String sql = "select count(*) from (" + buildBankTransactionTableSelect() + where.getSql() + ") as t";
        LOGGER.info("sql: {}", sql);
        return jdbcTemplate.queryForObject(sql, hasWhere ? where.getArguments().toArray() : null, Long.class);
    }

    public List<BankTransaction> searchBankTransactions(long offset, long itemsPerPage, BankTransaction bankTransaction, String sortField, String sortDirection, String operator) {
        String pagination = "";

        if (offset > -1 && itemsPerPage > -1) {
            pagination = " limit " + offset + ", " + itemsPerPage + " ";
        }

        Where where = buildWhereClause(bankTransaction, operator);

        String orderBy = "";

        if (sortField != null && sortDirection != null) {
            orderBy = " order by " + FIELD_TO_COLUMN_TRANSLATIONS.get(sortField) + " " + sortDirection + " ";
        }

        final boolean hasWhere = !"".equals(where.getSql());

        String sql = buildBankTransactionTableSelect() + where.getSql() + orderBy + pagination;

        LOGGER.info("sql: {}", sql);

        return jdbcTemplate.query(sql,
                hasWhere ? where.getArguments().toArray() : null,
                (rs, rowNum) -> {
                    BankTransaction bankTransactionFromDatabase = buildBankTransaction(rs);
                    LOGGER.info("Found bankTransaction in database: {}", bankTransactionFromDatabase);
                    return bankTransactionFromDatabase;
                }
        );
    }

    private Where buildWhereClause(BankTransaction bankTransaction, String operator) {
        List<String> clauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (bankTransaction.getId() != null) {
            clauses.add("bt.id = ?");
            parameters.add(bankTransaction.getId());
        }

        if (bankTransaction.getUsername() != null) {
            clauses.add("u.username like ?");
            parameters.add(like(bankTransaction.getUsername()));
        }

        if (bankTransaction.getEmailAddress() != null) {
            clauses.add("u.email like ?");
            parameters.add(like(bankTransaction.getEmailAddress()));
        }

        if (bankTransaction.getUserId() != null) {
            clauses.add("bt.user_id = ?");
            parameters.add(bankTransaction.getUserId());
        }

        if (bankTransaction.getDate() != null) {
            clauses.add("bt.date = ?");
            parameters.add(bankTransaction.getDate());
        }

        if (bankTransaction.getDescription() != null) {
            clauses.add("lower(bt.description) like ?");
            parameters.add(like(bankTransaction.getDescription()));
        }

        if (bankTransaction.getAmount() != null) {
            clauses.add("bt.amount = ?");
            parameters.add(bankTransaction.getAmount());
        }

        if (bankTransaction.getRunningBalance() != null) {
            clauses.add("bt.running_balance = ?");
            parameters.add(bankTransaction.getRunningBalance());
        }

        if (bankTransaction.getCounterParty() != null) {
            clauses.add("bt.counter_party like ?");
            parameters.add(like(bankTransaction.getCounterParty()));
        }

        if (bankTransaction.getReference() != null) {
            clauses.add("bt.reference like ?");
            parameters.add(like(bankTransaction.getReference()));
        }

        if (bankTransaction.getTransactionIndexOnDay() != null) {
            clauses.add("bt.transaction_index_on_day = ?");
            parameters.add(bankTransaction.getTransactionIndexOnDay());
        }

        if (bankTransaction.getPaymentSource() != null) {
            clauses.add("bt.payment_source like ?");
            parameters.add(like(String.valueOf(bankTransaction.getPaymentSource())));
        }

        if (bankTransaction.getEmailSent() != null) {
            clauses.add("bt.email_sent = ?");
            parameters.add(bankTransaction.getEmailSent());
        }

        return PersistenceUtils.buildWhereClause(clauses, parameters, operator);
    }

    public void updateMemberId(Long paymentId, Long memberId) {
        jdbcTemplate.update("update " + bankTransactionsTableName() + " bt set bt.user_id = ? where bt.id = ?", memberId, paymentId);
    }

    public void updateEmailSent(boolean emailSent, Long paymentId) {
        jdbcTemplate.update("update " + bankTransactionsTableName() + " bt set bt.email_sent = ? where bt.id = ?", emailSent, paymentId);
    }

    public List<BankTransaction> findAssignedBankTransactionsRequiringEmails() {
        LOGGER.info("Finding bank transactions that require emails");

        String sql = buildBankTransactionTableSelect() + "where email_sent = ? and user_id is not null";

        LOGGER.info("Going to execute sql: {}", sql);

        return jdbcTemplate.query(sql, new Object[] { false }, (rs, rowNum) -> buildBankTransaction(rs));
    }

    public List<BankTransaction> findMissingBankTransactions() {
        LOGGER.info("Finding missing bank transactions");

        return jdbcTemplate.query(
                "SELECT t.id, t.moneyIn, t.rollingBalance, t.description, t.date\n" +
                "FROM i7b0_bank_transactions_infull t\n" +
                "LEFT JOIN i7b0_bank_transactions b ON (REPLACE(b.description, '  ', ' ') = REPLACE(t.description, '&', '&amp;') or b.description = t.description)\n" +
                "WHERE b.description IS NULL\n" +
                "AND (moneyOut = 0 or moneyOut is null) \n" +
                "ORDER BY t.id;", (rs, rowNum) -> new BankTransaction(
                rs.getLong("id"),
                null,
                null,
                null,
                Instant.now(),
                rs.getString("description"),
                rs.getBigDecimal("moneyIn"),
                rs.getBigDecimal("rollingBalance"),
                null,
                null,
                rowNum,
                PaymentSource.SANTANDER,
                false));
    }
}
