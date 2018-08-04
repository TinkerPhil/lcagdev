package uk.co.novinet.service.enquiry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.PersistenceUtils;
import uk.co.novinet.service.member.MemberService;
import uk.co.novinet.service.member.Where;
import uk.co.novinet.service.payments.BankTransaction;
import uk.co.novinet.service.payments.PaymentSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static uk.co.novinet.service.PersistenceUtils.*;

@Service
public class EnquiryDao {
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
    }};

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Enquiry> getUnprocessed() {
        String sql = "select * from " + enquiryTableName() + " where has_been_processed = ?";

        LOGGER.info("sql: {}", sql);

        return jdbcTemplate.query(
                sql,
                new Object[] { false }, (rs, rowNum) -> buildEnquiry(rs)
        );
    }

    public void markAsProcessed(Long enquiryId) {
        String sql = "update " + enquiryTableName() + " set has_been_processed = ? where id = ?";

        LOGGER.info("sql: {}", sql);

        int result = jdbcTemplate.update(
                sql,
                new Object[]{true, enquiryId}
        );

        LOGGER.info("Update result: {}", result);
    }

    private Enquiry buildEnquiry(ResultSet resultSet) throws SQLException {
        return new Enquiry(
                resultSet.getLong("id"),
                resultSet.getString("email_address"),
                resultSet.getString("name"),
                resultSet.getString("mp_name"),
                resultSet.getString("mp_constituency"),
                resultSet.getString("mp_party"),
                resultSet.getBoolean("mp_engaged"),
                resultSet.getBoolean("mp_sympathetic"),
                resultSet.getString("schemes"),
                resultSet.getString("industry"),
                resultSet.getString("how_did_you_hear_about_lcag"),
                resultSet.getBoolean("member_of_big_group"),
                resultSet.getString("big_group_username"),
                resultSet.getBoolean("has_been_processed")
        );
    }

}
