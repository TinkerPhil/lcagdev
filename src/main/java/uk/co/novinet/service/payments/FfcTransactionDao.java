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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static uk.co.novinet.service.PersistenceUtils.*;

@Service
public class FfcTransactionDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberService.class);

    private Map<String, String> FIELD_TO_COLUMN_TRANSLATIONS = new HashMap<String, String>() {{
        put("id", "fb.id");
        put("uid", "fb.user_id");
        put("txnDate", "fb.txnDate");
        put("description", "fb.description");
        put("amount", "fb.amount");
    }};

    @Value("${forumDatabaseTablePrefix}")
    private String forumDatabaseTablePrefix;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void create(FfcTransaction ffcTransaction) {
        LOGGER.info("Going to create FFC transaction: {}", ffcTransaction);

        Long nextAvailableId = findNextAvailableId("id", ffcTransactionsTableName());

        ffcTransaction.setId(nextAvailableId);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

        String sql = "insert into " + ffcTransactionsTableName() + " (`id`, `txnDate`, `description`, `amount`, `uid`) values (?, ?, ?, ?, ?)";

        LOGGER.info("sql: {}", sql);

        int result = jdbcTemplate.update(
                sql,
                nextAvailableId,
                ffcTransaction.getTxnDateAsYYYYMMDD(),
                ffcTransaction.getDescription() == null ? "" : ffcTransaction.getDescription(),
                ffcTransaction.getAmount(),
                ffcTransaction.getUserId()
        );

        LOGGER.info("Insertion result: {}", result);
    }

    public FfcTransaction getFfcTransactionById(Long id) {
        return jdbcTemplate.queryForObject(buildFfcTransactionTableSelect() + " where fb.id = ?", new Object[] { id }, (rs, rowNum) -> buildFfcTransaction(rs));
    }

    public List<FfcTransaction> getFfcTransactions() {
        return jdbcTemplate.query("select * from " + ffcTransactionsTableName(), (rs, rowNum) -> buildFfcTransaction(rs));
   }

    public BigDecimal getFfcTotalTransactions() {
        return jdbcTemplate.queryForObject("select sum(amount) from " + ffcTransactionsTableName(), BigDecimal.class);
    }


    private FfcTransaction buildFfcTransaction(ResultSet rs) {
        try {
            Date d = rs.getDate("txnDate");
            return new FfcTransaction(new Long( rs.getLong("id")),
                        rs.getDate("txnDate"),
                        rs.getString("description"),
                        new BigDecimal(rs.getDouble("amount")),
                        rs.getLong("uid")
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<FfcTransaction> findExistingFfcTransaction(FfcTransaction ffcTransaction) {
        LOGGER.info("Going to try and find existing FFC transaction like: {}", ffcTransaction);

        String sql = buildFfcTransactionTableSelect() + " where fb.txnDate = ? and fb.description = ? and fb.amount = ?";

        Object[] arguments = {
                ffcTransaction.getTxnDateAsYYYYMMDD(),
                ffcTransaction.getDescription(),
                ffcTransaction.getAmount()
        };

        LOGGER.info("Going to execute sql: {}", sql);
        LOGGER.info("With arguments: {}", asList(arguments));

        return jdbcTemplate.query(sql, arguments, (rs, rowNum) -> buildFfcTransaction(rs));
    }

    public FfcTransaction getMostRecentFfcTransaction() {
        LOGGER.info("Finding most recent FFC transaction");

        String sql = buildFfcTransactionTableSelect() + " order by `txnDate` desc limit 1";

        LOGGER.info("Going to execute sql: {}", sql);

        List<FfcTransaction> results = jdbcTemplate.query(sql, (rs, rowNum) -> buildFfcTransaction(rs));

        if (results == null || results.isEmpty()) {
            return null;
        }

        return results.get(0);
    }
/*
    private FfcTransaction buildFfcTransaction(ResultSet rs) throws SQLException {
        String userId = rs.getString("uid");

        FfcTransaction ffcTransaction = new FfcTransaction(
                rs.getLong("fb.id"),
                rs.getDate("fb.txnDate"),
                rs.getString("fb.description"),
                rs.getBigDecimal("fb.amount"),
                userId == null ? null : Long.parseLong(userId)
        );

        LOGGER.info("Retrieved ffc transaction: {}", ffcTransaction);

        return ffcTransaction;
    }
*/
    private String buildFfcTransactionTableSelect() {
        return "select * from " + ffcTransactionsTableName() + " fb left outer join " + usersTableName() + " u on u.uid = fb.uid ";
    }

    public long searchCountFfcTransactions(FfcTransaction ffcTransaction, String operator) {
        Where where = buildWhereClause(ffcTransaction, operator);
        final boolean hasWhere = !"".equals(where.getSql());
        String sql = "select count(*) from (" + buildFfcTransactionTableSelect() + where.getSql() + ") as t";
        LOGGER.info("sql: {}", sql);
        return jdbcTemplate.queryForObject(sql, hasWhere ? where.getArguments().toArray() : null, Long.class);
    }

    public List<FfcTransaction> searchFfcTransactions(long offset, long itemsPerPage, FfcTransaction ffcTransaction, String sortField, String sortDirection, String operator) {
        String pagination = "";

        if (offset > -1 && itemsPerPage > -1) {
            pagination = " limit " + offset + ", " + itemsPerPage + " ";
        }

        Where where = buildWhereClause(ffcTransaction, operator);

        String orderBy = "";

        if (sortField != null && sortDirection != null) {
            orderBy = " order by " + FIELD_TO_COLUMN_TRANSLATIONS.get(sortField) + " " + sortDirection + " ";
        }

        final boolean hasWhere = !"".equals(where.getSql());

        String sql = buildFfcTransactionTableSelect() + where.getSql() + orderBy + pagination;

        LOGGER.info("sql: {}", sql);

        return jdbcTemplate.query(sql,
                hasWhere ? where.getArguments().toArray() : null,
                (rs, rowNum) -> {
                    FfcTransaction ffcTransactionFromDatabase = buildFfcTransaction(rs);
                    LOGGER.info("Found FFC Transaction in database: {}", ffcTransactionFromDatabase);
                    return ffcTransactionFromDatabase;
                }
        );
    }

    private Where buildWhereClause(FfcTransaction ffcTransaction, String operator) {
        List<String> clauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (ffcTransaction.getId() != null) {
            clauses.add("fb.id = ?");
            parameters.add(ffcTransaction.getId());
        }

        if (ffcTransaction.getTxnDate() != null) {
            clauses.add("fb.txndate = ?");
            parameters.add(ffcTransaction.getTxnDate());
        }

        if (ffcTransaction.getDescription() != null) {
            clauses.add("lower(fb.description) like ?");
            parameters.add(like(ffcTransaction.getDescription()));
        }

        if (ffcTransaction.getAmount() != null) {
            clauses.add("fb.amount = ?");
            parameters.add(ffcTransaction.getAmount());
        }

        if (ffcTransaction.getUserId() != null) {
            clauses.add("fb.uid = ?");
            parameters.add(ffcTransaction.getUserId());
        }

        return PersistenceUtils.buildWhereClause(clauses, parameters, operator);
    }

    public void updateMemberId(Long Id, Long memberId) {
        jdbcTemplate.update("update " + ffcTransactionsTableName() + " fb set fb.uid = ? where fb.id = ?", memberId, Id);
    }
}
