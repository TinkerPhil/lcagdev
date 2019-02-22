package uk.co.novinet.service.extract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@Service
public class ExtractService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractService.class);

    @Value("${forumDatabaseTablePrefix}")
    private String forumDatabaseTablePrefix;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String extract (
            String type,
            String special,
            String columns,
            String mpName,
            String mpConstituency,
            String mpParty,
            String mpTags,
            String name,
            String email,
            String username,
            String tags,
            String extraField1,
            String extraValue1,
            String extraField2,
            String extraValue2
        ) {
        LOGGER.info("Going to extract {}", type);
        LOGGER.info( "mpName={}, mpConstituency={}, mpParty={}, mpTags={}, "
                + "name={}, email={}, username={}, tags={}"
                + "{}={}, {}={}",
                mpName, mpConstituency, mpParty, mpTags,
                name, email, username, tags,
                extraField1, extraValue1, extraField2, extraValue2
        );
        String view = "i7b0_extract_" + (type.equalsIgnoreCase("special")? special : type + "BROKEN").toLowerCase();

        String sql = "SELECT " + columns + " from " + view + " WHERE 1 = 1";

        if( mpName != null && mpName != "") { sql += " AND lower(mpName) LIKE '%" + mpName + "%'"; }
        if( mpConstituency != null && mpConstituency != "") { sql += " AND lower(mpConstituency) LIKE '%" + mpConstituency + "%'"; }
        if( mpParty != null && mpParty != "") { sql += " AND lower(mpParty) LIKE '%" + mpParty + "%'"; }
        if( mpTags != null && mpTags != "") { sql += " AND lower(mpTags) LIKE '%" + mpTags + "%'"; }

        if( name != null && name != "") { sql += " AND lower(name) LIKE '%" + name + "%'"; }
        if( email != null && email != "") { sql += " AND lower(email) LIKE '%" + email + "%'"; }
        if( username != null && username != "") { sql += " AND lower(username) LIKE '%" + username + "%'"; }
        if( tags != null && tags != "") { sql += " AND lower(tags) LIKE '%" + tags + "%'"; }

        if( extraField1 != null && extraField1 != "" && extraValue1 != null && extraValue1 != "") { sql += " AND lower("+extraField1+") LIKE '%" + extraValue1 + "%'"; }
        if( extraField2 != null && extraField2 != "" && extraValue2 != null && extraValue2 != "") { sql += " AND lower("+extraField2+") LIKE '%" + extraValue2 + "%'"; }

        LOGGER.info("Created sql: {}", sql);


        String rows = (String)jdbcTemplate.query(sql, new ResultSetExtractor(){

            public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                StringBuffer ret = new StringBuffer();
                final ResultSetMetaData rsmd = rs.getMetaData();
                final int columnCount = rsmd.getColumnCount();

                for(int i = 1; i<=columnCount; i++) {
                    if(i>1){ ret.append("\t"); }
                    ret.append(rsmd.getColumnName(i));
                }
                ret.append("\n");

                while(rs.next()) {
                    for(int i = 1; i<=columnCount; i++) {
                        if(i>1){ ret.append("\t"); }
                        ret.append(rs.getObject(i));
                    }
                    ret.append("\n");

                }
                return ret.toString();
            }});

        return rows;
    }

}
