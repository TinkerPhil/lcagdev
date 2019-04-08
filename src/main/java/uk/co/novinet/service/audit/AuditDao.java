package uk.co.novinet.service.audit;

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
import uk.co.novinet.service.payments.FfcContribution;
import uk.co.novinet.service.payments.PaymentSource;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static uk.co.novinet.service.PersistenceUtils.*;

@Service
public class AuditDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void create(AuditEvent auditEvent) {
        LOGGER.info("Going to create audit event: {}", auditEvent);

        String sql = "insert into " + auditEventTableName() + " (`date`, `user_id`, `event_name`, `event_parameters`) values (?, ?, ?, ?)";

        LOGGER.info("sql: {}", sql);

        int result = jdbcTemplate.update(
                sql,
                unixTime(auditEvent.getDate()),
                auditEvent.getUserId(),
                auditEvent.getEventName(),
                auditEvent.getEventParameters() == null ? "" : auditEvent.getEventParameters()
        );

        LOGGER.info("Insertion result: {}", result);
    }
}
