package uk.co.novinet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.member.Where;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Service
public class PersistenceUtils {

    private static String forumDatabaseTablePrefix;

    private static JdbcTemplate jdbcTemplate;

    @Value("${forumDatabaseTablePrefix}")
    public void setForumDatabaseTablePrefix(String forumDatabaseTablePrefix) {
        PersistenceUtils.forumDatabaseTablePrefix = forumDatabaseTablePrefix;
    }

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        PersistenceUtils.jdbcTemplate = jdbcTemplate;
    }

    public static long unixTime(Date date) {
        if (date == null) {
            return 0;
        }

        return date.getTime() / 1000;
    }

    public static Date dateFromMyBbRow(ResultSet rs, String columnName) throws SQLException {
        Long dateInSeconds = rs.getLong(columnName);

        if (dateInSeconds != null && dateInSeconds > 0) {
            return new Date(dateInSeconds * 1000L);
        }

        return null;
    }

    public static Object like(String argument) {
        return "%" + argument.toLowerCase() + "%";
    }

    public static Where buildWhereClause(List<String> clauses, List<Object> parameters) {
        String sql = clauses.isEmpty() ? "" : "where ";

        for (int i = 0; i < clauses.size(); i++) {
            sql += clauses.get(i);
            if (i < clauses.size() - 1) {
                sql += " and ";
            }
        }

        return new Where(sql, parameters);
    }

    public static String bankTransactionsTableName() {
        return forumDatabaseTablePrefix + "bank_transactions";
    }

    public static String usersTableName() {
        return forumDatabaseTablePrefix + "users";
    }

    public static String userGroupsTableName() {
        return forumDatabaseTablePrefix + "usergroups";
    }

    public static Long findNextAvailableId(String idColumnName, String tableName) {
        Long max = jdbcTemplate.queryForObject("select max(" + idColumnName + ") from " + tableName, Long.class);

        if (max == null) {
            max = (long) 1;
        } else {
            max = max + 1;
        }

        return max;
    }


}
