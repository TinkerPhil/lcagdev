package uk.co.novinet.service.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.PersistenceUtils;
import uk.co.novinet.service.enquiry.MailSenderService;
import uk.co.novinet.service.payments.FfcPayment;
//import uk.co.novinet.service.member.SftpService;
import uk.co.novinet.service.member.Where;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static uk.co.novinet.service.PersistenceUtils.*;

@Service
public class FfcPaymentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FfcPaymentService.class);

    @Value("${forumDatabaseTablePrefix}")
    private String forumDatabaseTablePrefix;

    @Autowired
    private JdbcTemplate jdbcTemplate;

//    @Autowired
//    private SftpService sftpService;

    @Autowired
    private MailSenderService mailSenderService;

    private Map<String, String> FIELD_TO_COLUMN_TRANSLATIONS = new HashMap<String, String>() {{
        put("id", "f.id");
        put("user_id", "f.user_id");
        put("username", "f.username");
        put("membership_token", "f.membership_token");
        put("firstName", "f.first_name");
        put("lastName", "f.last_name");
        put("email", "f.email_address");
        put("address_line_1", "f.address_line_1");
        put("address_line_2", "f.address_line_2");
        put("city", "f.city");
        put("postal_code", "f.postal_code");
        put("country", "f.country");
        put("gross_amount", "f.gross_amount");
        put("invoice_created", "f.invoice_created");
        put("payment_received", "f.payment_received");
        put("stripe_token", "f.stripe_token");
        put("reference", "f.reference");
        put("status", "f.status");
        put("error_description", "f.error_description");
        put("paymentType", "f.payment_type");
        put("paymentMethod", "f.payment_method");
        put("email_sent", "f.email_sent");
        put("esig", "f.guid");
        put("signature_data", "f.signature_data");
        put("hasProvidedSignature", "f.has_provided_signature");
        //put("signed_contribution_agreement", "f.signed_contribution_agreement");
        put("contribution_agreement_signature_date", "f.contribution_agreement_signature_date");
    }};

    private String buildFfcTableSelect() {
        return "select f.id, f.user_id, f.username, f.membership_token, f.first_name, f.last_name, f.email_address, f.address_line_1, " +
                "f.address_line_2, f.city, f.postal_code, f.country, f.gross_amount, f.invoice_created, f.payment_received, " +
                "f.stripe_token, f.reference, f.status, f.error_description, f.payment_type, f.payment_method, " +
                "f.email_sent, f.guid, f.signature_data, f.has_provided_signature, " +
                //"f.signed_contribution_agreement " +
                "f.contribution_agreement_signature_date " +
                "from " + ffcContributionsTableName() + " f ";
    }

    private String buildFfcTableGroupBy() {
        return " group by f.id ";
    }

    public long searchCountFfcPayment(FfcPayment ffcPayment, String operator) {
        Where where = buildWhereClause(ffcPayment, operator);
        final boolean hasWhere = !"".equals(where.getSql());
        String sql = "select count(*) from (" + buildFfcTableSelect() + where.getSql() + buildFfcTableGroupBy() + ") as t";
        LOGGER.info("Going to execute this sql: {}", sql);
        return jdbcTemplate.queryForObject(sql, hasWhere ? where.getArguments().toArray() : null, Long.class);
    }

    public List<FfcPayment> searchFfcPayment(long offset, long itemsPerPage, FfcPayment ffcPayment, String sortField, String sortDirection, String operator) {
        String pagination = "";

        if (offset > -1 && itemsPerPage > -1) {
            pagination = " limit " + offset + ", " + itemsPerPage + " ";
        }

        Where where = buildWhereClause(ffcPayment, operator);

        String orderBy = "";

        if (sortField != null && sortDirection != null) {
            orderBy = " order by " + FIELD_TO_COLUMN_TRANSLATIONS.get(sortField) + " " + sortDirection + " ";
        }

        final boolean hasWhere = !"".equals(where.getSql());

        String sql = buildFfcTableSelect() + where.getSql() + buildFfcTableGroupBy() + orderBy + pagination;

        LOGGER.info("sql: {}", sql);

        return jdbcTemplate.query(sql,
                hasWhere ? where.getArguments().toArray() : null,
                (rs, rowNum) -> buildFfcPayment(rs)
        );
    }

    private Where buildWhereClause(FfcPayment ffcPayment, String operator) {
        List<String> clauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (ffcPayment.getUsername() != null) {
            clauses.add("lower(f.username) like ?");
            parameters.add(like(ffcPayment.getUsername()));
        }
        if (ffcPayment.getFirstName() != null) {
            clauses.add("lower(f.first_name) like ?");
            parameters.add(like(ffcPayment.getFirstName()));
        }
        if (ffcPayment.getLastName() != null) {
            clauses.add("lower(f.last_name) like ?");
            parameters.add(like(ffcPayment.getLastName()));
        }
        if (ffcPayment.getEmail() != null) {
            clauses.add("lower(f.email_address) like ?");
            parameters.add(like(ffcPayment.getEmail()));
        }
        if (ffcPayment.getEsig() != null) {
            clauses.add("lower(f.guid) like ?");
            parameters.add(like(ffcPayment.getEsig()));
        }
        if (ffcPayment.getHasProvidedSignature() != null) {
            clauses.add("f.has_provided_signature = ?");
            parameters.add(ffcPayment.getHasProvidedSignature());
        }
        if (ffcPayment.getStatus() != null) {
            clauses.add("lower(f.status) like ?");
            parameters.add(like(ffcPayment.getStatus()));
        }
        if (ffcPayment.getReference() != null) {
            clauses.add("lower(f.reference) like ?");
            parameters.add(like(ffcPayment.getReference()));
        }
        if (ffcPayment.getPaymentType() != null) {
            clauses.add("lower(f.payment_type) like ?");
            parameters.add(like(ffcPayment.getPaymentType()));
        }
        if (ffcPayment.getPaymentMethod() != null) {
            clauses.add("lower(f.payment_method) like ?");
            parameters.add(like(ffcPayment.getPaymentMethod()));
        }

        return PersistenceUtils.buildWhereClause(clauses, parameters, operator);
    }

    private FfcPayment buildFfcPayment(ResultSet rs) throws SQLException {
        return new FfcPayment(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("username"),
                rs.getString("membership_token"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email_address"),
                rs.getString("address_line_1"),
                rs.getString("address_line_2"),
                rs.getString("city"),
                rs.getString("postal_code"),
                rs.getString("country"),
                rs.getBigDecimal("gross_amount"),
                rs.getBoolean("invoice_created"),
                rs.getBoolean("payment_received"),
                rs.getString("stripe_token"),
                rs.getString("reference"),
                rs.getString("status"),
                rs.getString("error_description"),
                rs.getString("payment_type"),
                rs.getString("payment_method"),
                rs.getBoolean("email_sent"),
                rs.getString("guid"),  //guid on DB!!
                rs.getString("signature_data"),
                rs.getBoolean("has_provided_signature"),
                // String signed_contribution_agreement;   //BLOB on DB
                dateFromMyBbRow(rs, "contribution_agreement_signature_date")
        );
    }
}
