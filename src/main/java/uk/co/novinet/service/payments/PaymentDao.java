package uk.co.novinet.service.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.PersistenceUtils;
import uk.co.novinet.service.member.Member;
import uk.co.novinet.service.member.MemberService;
import uk.co.novinet.service.member.Where;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.co.novinet.service.PersistenceUtils.dateFromMyBbRow;
import static uk.co.novinet.service.PersistenceUtils.like;
import static uk.co.novinet.service.PersistenceUtils.unixTime;

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
    }};

    @Value("${forumDatabaseTablePrefix}")
    private String forumDatabaseTablePrefix;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public BankTransaction create(BankTransaction bankTransaction) {
        LOGGER.info("Going to create bank transaction {}", bankTransaction);

        Number id = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(bankTransactionsTableName())
                .executeAndReturnKey(new HashMap<String, Object>() {{
                    put("user_id", bankTransaction.getUserId());
                    put("date", bankTransaction.getDate());
                    put("description", bankTransaction.getDescription());
                    put("amount", bankTransaction.getAmount());
                    put("running_balance", bankTransaction.getRunningBalance());
                    put("counter_party", bankTransaction.getCounterParty());
                    put("reference", bankTransaction.getReference());
                }});

        if (id != null) {
            bankTransaction.setId(id.longValue());
        } else {
            throw new RuntimeException(format("Count not persist bank transaction {}", bankTransaction));
        }

        return bankTransaction;
    }

    private String bankTransactionsTableName() {
        return forumDatabaseTablePrefix + "bank_transactions";
    }

    public List<BankTransaction> findExistingBankTransaction(BankTransaction bankTransaction) {
        return jdbcTemplate.query("select * from " + bankTransactionsTableName() + " bt where " +
                "bt.date = ? and bt.description = ? and bt.amount = ? and bt.running_balance = ?",
                new Object[] {
                    unixTime(bankTransaction.getDate()),
                    bankTransaction.getDescription(),
                    bankTransaction.getAmount(),
                    bankTransaction.getRunningBalance()
                }, (rs, rowNum) -> buildBankTransaction(rs));
    }

    private BankTransaction buildBankTransaction(ResultSet rs) throws SQLException {
        String userId = rs.getString("user_id");
        return new BankTransaction(
                rs.getLong("id"),
                userId == null ? null : Long.parseLong(userId),
                dateFromMyBbRow(rs, "date"),
                rs.getString("description"),
                Double.parseDouble(rs.getString("amount")),
                Double.parseDouble(rs.getString("running_balance")),
                rs.getString("counter_party"),
                rs.getString("reference")
        );
    }

    private String buildBankTransactionTableSelect() {
        return "select bt.id, bt.user_id, bt.date, bt.description, bt.amount, bt.running_balance, bt.counter_party, bt.reference from " + forumDatabaseTablePrefix + "bank_transactions bt ";
    }

    public long searchCountBankTransactions(BankTransaction bankTransaction) {
        Where where = buildWhereClause(bankTransaction);
        final boolean hasWhere = !"".equals(where.getSql());
        return jdbcTemplate.queryForObject("select count(*) from (" + buildBankTransactionTableSelect() + where.getSql() + ") as t", hasWhere ? where.getArguments().toArray() : null, Long.class);
    }

    public List<BankTransaction> searchBankTransactions(long offset, long itemsPerPage, BankTransaction bankTransaction, String sortField, String sortDirection) {
        String pagination = "";

        if (offset > -1 && itemsPerPage > -1) {
            pagination = " limit " + offset + ", " + itemsPerPage + " ";
        }

        Where where = buildWhereClause(bankTransaction);

        String orderBy = "";

        if (sortField != null && sortDirection != null) {
            orderBy = " order by " + FIELD_TO_COLUMN_TRANSLATIONS.get(sortField) + " " + sortDirection + " ";
        }

        final boolean hasWhere = !"".equals(where.getSql());

        String sql = buildBankTransactionTableSelect() + where.getSql() + orderBy + pagination;

        LOGGER.info("sql: {}", sql);

        return jdbcTemplate.query(sql,
                hasWhere ? where.getArguments().toArray() : null,
                (rs, rowNum) -> buildBankTransaction(rs)
        );
    }

    private Where buildWhereClause(BankTransaction bankTransaction) {
        List<String> clauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (bankTransaction.getId() != null) {
            clauses.add("bt.id = ?");
            parameters.add(bankTransaction.getId());
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
            clauses.add("bt.amount like ?");
            parameters.add(like(String.valueOf(bankTransaction.getAmount())));
        }

        if (bankTransaction.getRunningBalance() != null) {
            clauses.add("bt.running_balance like ?");
            parameters.add(like(String.valueOf(bankTransaction.getRunningBalance())));
        }

        if (bankTransaction.getCounterParty() != null) {
            clauses.add("bt.counter_party like ?");
            parameters.add(like(bankTransaction.getCounterParty()));
        }

        if (bankTransaction.getReference() != null) {
            clauses.add("bt.reference like ?");
            parameters.add(like(bankTransaction.getReference()));
        }

        return PersistenceUtils.buildWhereClause(clauses, parameters);
    }




}
