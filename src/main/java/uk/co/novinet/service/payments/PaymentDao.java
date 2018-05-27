package uk.co.novinet.service.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.member.Member;
import uk.co.novinet.service.member.MemberService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import static java.lang.String.format;
import static uk.co.novinet.service.PersistenceUtils.dateFromMyBbRow;
import static uk.co.novinet.service.PersistenceUtils.unixTime;

@Service
public class PaymentDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberService.class);

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


}
