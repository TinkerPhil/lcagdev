package uk.co.novinet.service.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.PersistenceUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

import static uk.co.novinet.service.PersistenceUtils.*;

@Service
public class MemberMpCampaignService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberMpCampaignService.class);

    @Value("${forumDatabaseTablePrefix}")
    private String forumDatabaseTablePrefix;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Map<String, String> FIELD_TO_COLUMN_TRANSLATIONS = new HashMap<String, String>() {{
        put("id", "umc.uid");
        put("name", "u.name");
        put("mpName", "u.mp_name");
        put("email", "u.email");
        put("administratorName", "a.name");
        put("allowEmailShareStatus", "umc.allowEmailShareStatus");
        put("sentInitialEmail", "umc.sentInitialEmail");
        put("meetingNext", "umc.meetingNext");
        put("meetingCount", "umc.meetingCount");
        put("telephoneCount", "umc.telephoneCount");
        put("writtenCount", "umc.writtenCount");
        put("involved", "umc.involved");
        put("edmUrl", "m.edmUrl");
        put("campaignNotes", "umc.campaignNotes");
        put("telNo", "umc.telNo");
        put("tags", "umc.tags");
    }};

    public void update(
            Long id,
            String allowEmailShareStatus,
            String sentInitialEmail,
            String campaignNotes,
            String telNo,
            String tags,
            String meetingNext,
            Integer meetingCount,
            Integer telephoneCount,
            Integer writtenCount,
            Integer involved
            ) {
        LOGGER.info("Going to update usersMpCampaign with id {}", id);
        LOGGER.info("allowEmailShareStatus={}, sentInitialEmail=(), campaignNotes={}, telNo={}, tags={}, meetingNext={}, meetingCount={}, telephoneCount={}, writtenCount={}, involved={}",
                allowEmailShareStatus, sentInitialEmail, campaignNotes,
                telNo,tags, meetingNext, meetingCount, telephoneCount,
                writtenCount, involved
        );

        MemberMpCampaign existingMember = getMemberById(id);

        String sql = "update " + mpCampaignUsersTableName() + " umc " +
                "set umc.allowEmailShareStatus = ?," +
                " umc.sentInitialEmail = ?," +
                " umc.campaignNotes = ?, " +
                " umc.telNo = ?, " +
                " umc.tags = ?, " +
                " umc.meetingNext = STR_TO_DATE(?, '%Y%m%d %H:%i'), " +
                " umc.meetingCount = ?, " +
                " umc.telephoneCount = ?, " +
                " umc.writtenCount = ?, " +
                " umc.involved = ? " +
                "where umc.uid = ?";

        LOGGER.info("Created sql: {}", sql);

        int result = jdbcTemplate.update(
                sql,
                allowEmailShareStatus,
                sentInitialEmail,
                campaignNotes,
                telNo,
                tags,
                meetingNext,
                meetingCount,
                telephoneCount,
                writtenCount,
                involved,
                id
        );

        LOGGER.info("Update result: {}", result);
    }


    public MemberMpCampaign getMemberById(Long id) {
        return jdbcTemplate.queryForObject(buildUserTableSelect() + " where umc.uid = ?", new Object[] { id }, (rs, rowNum) -> buildMember(rs));
    }

    private String buildUserTableSelect() {
        return "select umc.uid, u.name, ug.title as usergroup, m.mpName, a.name as administratorName, u.email, " +
                "umc.allowEmailShareStatus, umc.sentInitialEmail, umc.campaignNotes, umc.telNo, " +
                "umc.tags, " +
                "DATE_FORMAT(umc.meetingNext, '%Y%m%d %H:%i') as meetingNext, umc.meetingCount, umc.telephoneCount, umc.writtenCount, umc.involved, " +
                "u.username, u.postnum, u.threadnum, u.lastvisit, u.schemes, u.lobbying_day_attending as lobbyingDayAttending, m.edmUrl " +
                "from " + mpCampaignUsersTableName() + " umc " +
                " inner join " + usersTableName() + " u on u.uid = umc.uid " +
                " inner join " + userGroupsTableName() + " ug on ug.gid=u.usergroup " +
                "left outer join " + mpTableName() + " m on m.mpId = umc.mpId " +
                "left outer join "+ mpCampaignVolunteerTableName() + " mcv on mcv.mpGroupNo = m.mpGroupNo " +
                "left outer join " + usersTableName() + " a on a.uid = mcv.uid ";
    }

    private String buildUserTableGroupBy() {
        return " group by umc.uid ";
    }

    public long searchCountMembers(MemberMpCampaign member, String operator) {
        Where where = buildWhereClause(member, operator);
        final boolean hasWhere = !"".equals(where.getSql());
        String sql = "select count(*) from (" + buildUserTableSelect() + where.getSql() + buildUserTableGroupBy() + ") as t";
        LOGGER.info("Going to execute this sql: {}", sql);
        return jdbcTemplate.queryForObject(sql, hasWhere ? where.getArguments().toArray() : null, Long.class);
    }

    public List<MemberMpCampaign> searchMembers(long offset, long itemsPerPage, MemberMpCampaign member, String sortField, String sortDirection, String operator) {
        String pagination = "";

        if (offset > -1 && itemsPerPage > -1) {
            pagination = " limit " + offset + ", " + itemsPerPage + " ";
        }

        Where where = buildWhereClause(member, operator);

        String orderBy = "";

        if (sortField != null && sortDirection != null) {
            orderBy = " order by " + FIELD_TO_COLUMN_TRANSLATIONS.get(sortField) + " " + sortDirection + " ";
        }

        final boolean hasWhere = !"".equals(where.getSql());

        String sql = buildUserTableSelect() + where.getSql() + buildUserTableGroupBy() + orderBy + pagination;

        LOGGER.info("sql: {}", sql);

        return jdbcTemplate.query(sql,
                hasWhere ? where.getArguments().toArray() : null,
                (rs, rowNum) -> buildMember(rs)
        );
    }

    private Where buildWhereClause(MemberMpCampaign member, String operator) {
        List<String> clauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (member.getName() != null) {
            clauses.add("lower(u.name) like ?");
            parameters.add(like(member.getName()));
        }

        if (member.getMpName() != null) {
            clauses.add("lower(m.mpName) like ?");
            parameters.add(like(member.getMpName()));
        }

        if(member.getAdministratorName() != null ) {
            clauses.add("lower(a.name) like ?");
            parameters.add(like(member.getAdministratorName()));
        }
        if(member.getMeetingCount() != null ) {
            clauses.add("umc.meetingCount = ?");
            parameters.add(member.getMeetingCount());
        }
        if(member.getTelephoneCount() != null ) {
            clauses.add("umc.telephoneCount = ?");
            parameters.add(member.getTelephoneCount());
        }
        if(member.getWrittenCount() != null ) {
            clauses.add("umc.writtenCount = ?");
            parameters.add(member.getWrittenCount());
        }
        if(member.getInvolved() != null ) {
            clauses.add("umc.involved = ?");
            parameters.add(member.getInvolved());
        }
        if(member.getMeetingNext() != null ) {
            clauses.add("DATE(umc.meetingNext) = DATE(?)");
            parameters.add(member.getMeetingNext());
        }
        if(member.getTags() != null ) {
            String[] tags = member.getTags().split(" *, *");
            String clause = "(";
            for(int i = 0; i < tags.length; i++) {
                if( i > 0 ) {
                    clause = clause + " OR ";
                }
                clause = clause + "umc.tags like ?";
                parameters.add(like(tags[i]));
            }
            clause = clause + ")";
            clauses.add(clause);
        }



        return PersistenceUtils.buildWhereClause(clauses, parameters, operator);
    }

    private MemberMpCampaign buildMember(ResultSet rs) throws SQLException {
        return new MemberMpCampaign(
                rs.getLong("uid"),
                rs.getString("name"),
                rs.getString("mpName"),
                rs.getString("email"),
                rs.getString("username"),
                rs.getString("usergroup"),
                rs.getInt("postnum"),
                rs.getInt("threadnum"),
                rs.getString("lastvisit"),
                rs.getString("schemes"),
                rs.getString("allowEmailShareStatus"),
                rs.getString("sentInitialEmail"),
                rs.getString("campaignNotes"),
                rs.getString("telNo"),
                rs.getString("tags"),
                rs.getString("meetingNext"),
                rs.getInt("meetingCount"),
                rs.getInt("telephoneCount"),
                rs.getInt( "writtenCount"),
                rs.getInt("involved"),
                rs.getString("edmUrl"),
                rs.getString("lobbyingDayAttending"),
                rs.getString("administratorName")
        );
    }
}
