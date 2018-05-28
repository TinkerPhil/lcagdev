package uk.co.novinet.service;

import uk.co.novinet.service.member.Where;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class PersistenceUtils {

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
}
