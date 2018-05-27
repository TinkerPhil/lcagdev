package uk.co.novinet.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

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
}
