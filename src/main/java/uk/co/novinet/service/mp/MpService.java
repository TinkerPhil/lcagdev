package uk.co.novinet.service.mp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.PersistenceUtils;
import uk.co.novinet.service.member.Where;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static uk.co.novinet.service.PersistenceUtils.*;

@Service
public class MpService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MpService.class);

    @Value("${forumDatabaseTablePrefix}")
    private String forumDatabaseTablePrefix;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Map<String, String> FIELD_TO_COLUMN_TRANSLATIONS = new HashMap<String, String>() {{
        put("id", "m.mpId");
        put("lastName", "m.lastName");
        put("firstName", "m.firstName");
        put("mpName", "m.mpName");
        put("party", "m.party");
        put("twitter", "m.twitter");
        put("email", "m.email");
        put("constituency", "m.constituency");
        put("constituencyAddress", "m.constituencyAddress");
        put("edmStatus", "m.edmStatus");
        put("edmUrl", "m.edmUrl");
        put("ministerialStatus", "m.ministerialStatus");
        put("url", "m.url");
        put("majority", "m.majority");
        put("telNo", "m.telNo");
        put("tags", "m.tags");
        put("campaignNotes", "m.campaignNotes");
    }};

    public void update(
            Long id,
            String lastName,
            String firstName,
            String mpName,
            String party,
            String twitter,
            String email,
            String constituency,
            String constituencyAddress,
            String edmStatus,
            String edmUrl,
            String ministerialStatus,
            String url,
            Integer majority,
            String telNo
        ) {
        LOGGER.info("Going to update MP with id {}", id);
        LOGGER.info( "mpId={}, lastName={}, firstName={}, mpName={}, party={}, twitter={}, email={}, constituency={}, "
			+ "constituencyAddress={}, edmStatus={}, ministerialStatus={}, url={}, "
			+ "telNo={}",
			id, lastName, firstName, mpName, party, twitter, email, constituency,
			constituencyAddress, edmStatus, ministerialStatus, url, majority,
			telNo
        );

        MP existingMP = getMpById(id);

        String sql = "update " + mpTableName() + " m " +
                "set m.lastName = ?, " +
                "m.firstName = ?, " +
                "m.mpName = ?, "+
                "m.party = ?, " +
                "m.twitter = ?, " +
                "m.email = ?, " +
                "m.constituency = ?, " +
                "m.constituencyAddress = ?, " +
                "m.edmStatus = ?, " +
                "m.edmUrl = ?, " +
                "m.ministerialStatus = ?, " +
                "m.url = ?, " +
                "m.majority = ?, " +
                "m.telNo = ?, " +
                "where m.mpId = ?";

        LOGGER.info("Created sql: {}", sql);

        int result = jdbcTemplate.update(
                sql,
                lastName,
                firstName,
                mpName,
                party,
                twitter,
                email,
                constituency,
                constituencyAddress,
                edmStatus,
                edmUrl,
                ministerialStatus,
                url,
                majority,
                telNo,
                id
        );

        LOGGER.info("Update result: {}", result);
    }

    public void updateCampaign(
            Long id,
            String edmStatus,
            String tags,
            String campaignNotes
    ) {
        LOGGER.info("Going to update MP with id {}", id);
        LOGGER.info( "mpId={}, edmStatus={}, campaignNotes={}",
                id, edmStatus, campaignNotes
        );

        MP existingMP = getMpById(id);

        String sql = "update " + mpTableName() + " m " +
                "set m.edmStatus = ?, " +
                "m.tags = ?, " +
                "m.campaignNotes = ?" +
                "where m.mpId = ?";

        LOGGER.info("Created sql: {}", sql);

        int result = jdbcTemplate.update(
                sql,
                edmStatus,
                tags,
                campaignNotes,
                id
        );

        LOGGER.info("Update result: {}", result);
    }


    public MP getMpById(Long id) {
        return jdbcTemplate.queryForObject(buildMpTableSelect() + " where m.mpId = ?", new Object[] { id }, (rs, rowNum) -> buildMP(rs));
    }

    private String buildMpTableSelect() {
        return "select m.mpId, m.lastName, m.firstName, m.mpName, m.party, m.twitter, m.email, " +
                "m.constituency, m.pCon, m.constituencyAddress, m.edmStatus, m.edmUrl, m.ministerialStatus, m.url, m.majority," +
                "m.telNo, " +
                "m.tags, " +
                "m.campaignNotes, m.sharedCampaignEmails, m.privateCampaignEmails, " +
                " mcv.signature as adminSig " +
                "from " + mpTableName() + " m " +
                "left join " + mpCampaignVolunteerTableName() + " mcv on mcv.uid=m.uidAdministrator "
                ;
    }

    private String buildMpTableGroupBy() {
        return " group by m.mpId ";
    }

    public long searchCountMps(MP mp, String operator) {
        Where where = buildWhereClause(mp, operator);
        final boolean hasWhere = !"".equals(where.getSql());
        String sql = "select count(*) from (" + buildMpTableSelect() + where.getSql() + buildMpTableGroupBy() + ") as t";
        LOGGER.info("Going to execute this sql: {}", sql);
        return jdbcTemplate.queryForObject(sql, hasWhere ? where.getArguments().toArray() : null, Long.class);
    }

    public List<MP> searchMps(long offset, long itemsPerPage, MP mp, String sortField, String sortDirection, String operator) {
        String pagination = "";

        if (offset > -1 && itemsPerPage > -1) {
            pagination = " limit " + offset + ", " + itemsPerPage + " ";
        }

        Where where = buildWhereClause(mp, operator);

        String orderBy = "";

        if (sortField != null && sortDirection != null) {
            orderBy = " order by " + FIELD_TO_COLUMN_TRANSLATIONS.get(sortField) + " " + sortDirection + " ";
        }

        final boolean hasWhere = !"".equals(where.getSql());

        String sql = buildMpTableSelect() + where.getSql() + buildMpTableGroupBy() + orderBy + pagination;

        LOGGER.info("sql: {}", sql);

        return jdbcTemplate.query(sql,
                hasWhere ? where.getArguments().toArray() : null,
                (rs, rowNum) -> buildMP(rs)
        );
    }

    private Where buildWhereClause(MP mp, String operator) {
        List<String> clauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        clauses.add("m.IsSystem != 1");

        if (mp.getLastName() != null) {
            clauses.add("lower(m.lastName) like ?");
            parameters.add(like(mp.getLastName()));
        }

        if (mp.getFirstName() != null) {
            clauses.add("lower(m.firstName) like ?");
            parameters.add(like(mp.getFirstName()));
        }

        if (mp.getMpName() != null) {
            clauses.add("lower(m.mpName) like ?");
            parameters.add(like(mp.getMpName()));
        }

        if (mp.getParty() != null) {
            clauses.add("lower(m.party) like ?");
            parameters.add(like(mp.getParty()));
        }

        if (mp.getTwitter() != null) {
            clauses.add("lower(m.twitter) like ?");
            parameters.add(like(mp.getTwitter()));
        }

        if (mp.getEmail() != null) {
            clauses.add("lower(m.email) like ?");
            parameters.add(like(mp.getEmail()));
        }

        if (mp.getConstituency() != null) {
            clauses.add("lower(m.constituency) like ?");
            parameters.add(like(mp.getConstituency()));
        }

        if (mp.getConstituencyAddress() != null) {
            clauses.add("lower(m.constituencyAddress) = ?");
            parameters.add(like(mp.getConstituencyAddress()));
        }

        if (mp.getEdmStatus() != null) {
            clauses.add("m.edmStatus = ?");
            parameters.add(mp.getEdmStatus());
        }

        if (mp.getMinisterialStatus() != null) {
            clauses.add("m.ministerialStatus = ?");
            parameters.add(mp.getMinisterialStatus());
        }

        if (mp.getUrl() != null) {
            clauses.add("lower(m.url) like ?");
            parameters.add(like(mp.getUrl()));
        }

        if (mp.getMajority() != null) {
            clauses.add("m.majority = ?");
            parameters.add(mp.getMajority());
        }

        if (mp.getTelNo() != null) {
            clauses.add("m.telNo = ?");
            parameters.add(mp.getTelNo());
        }

        if(mp.getTags() != null ) {
            String[] tags = mp.getTags().split(" *, *");
            String clause = "(";
            for(int i = 0; i < tags.length; i++) {
                if( i > 0 ) {
                    clause = clause + " OR ";
                }
                clause = clause + "m.tags like ?";
                parameters.add(like(tags[i]));
            }
            clause = clause + ")";
            clauses.add(clause);
        }

        if (mp.getCampaignNotes() != null) {
            clauses.add("lower(m.campaignNotes) like ?");
            parameters.add(like(mp.getCampaignNotes()));
        }

        if (mp.getAdminSig() != null) {
            clauses.add("lower(mcv.signature) like ?");
            parameters.add(like(mp.getAdminSig()));
        }
        return PersistenceUtils.buildWhereClause(clauses, parameters, operator);
    }

    private MP buildMP(ResultSet rs) throws SQLException {
        return new MP(
                rs.getLong("mpId"),
                rs.getString("lastName"),
                rs.getString("firstName"),
                rs.getString("mpName"),
                rs.getString("party"),
                rs.getString("twitter"),
                rs.getString("email"),
                rs.getString("constituency"),
                rs.getString("pCon"),
                rs.getString("constituencyAddress"),
                rs.getString("edmStatus"),
                rs.getString("edmUrl"),
                rs.getString("ministerialStatus"),
                rs.getString("url"),
                rs.getLong("majority"),
                rs.getString("telNo"),
                rs.getString("tags"),
                rs.getString("campaignNotes"),
                rs.getString("sharedCampaignEmails"),
                rs.getString("privateCampaignEmails"),
                rs.getString("adminSig")
        );
    }
}
