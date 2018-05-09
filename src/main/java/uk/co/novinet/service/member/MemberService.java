package uk.co.novinet.service.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.mail.Enquiry;
import uk.co.novinet.service.mail.PasswordSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class MemberService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberService.class);

    @Value("${forumDatabaseTablePrefix}")
    private String forumDatabaseTablePrefix;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Map<String, String> FIELD_TO_COLUMN_TRANSLATIONS = new HashMap<String, String>() {{
        put("id", "u.uid");
        put("emailAddress", "u.email");
        put("group", "ug.title");
        put("username", "u.username");
        put("name", "u.name");
        put("registrationDate", "u.regdate");
        put("hmrcLetterChecked", "u.hmrc_letter_checked");
        put("identificationChecked", "u.identification_checked");
        put("contributionAmount", "u.contribution_amount");
        put("contributionDate", "u.contribution_date");
        put("agreedToContributeButNotPaid", "u.agreed_to_contribute_but_not_paid");
        put("mpName", "u.mp_name");
        put("mpEngaged", "u.mp_engaged");
        put("mpSympathetic", "u.mp_sympathetic");
        put("mpConstituency", "u.mp_constituency");
        put("mpParty", "u.mp_party");
        put("schemes", "u.schemes");

    }};

    public void update(Long memberId, String group, boolean identificationChecked, boolean hmrcLetterChecked, String contributionAmount, Date contributionDate, Boolean agreedToContributeButNotPaid, String mpName, Boolean mpEngaged, Boolean mpSympathetic, String mpConstituency, String mpParty, String schemes) {
        LOGGER.info("Going to update user with id {}", memberId);
        LOGGER.info("group={}, identificationChecked={}, hmrcLetterChecked={}, contributionAmount={}, contributionDate={}, agreedToContributeButNotPaid={}, mpName={}, mpEngaged={}, mpSympathetic={}, mpConstituency={}, mpParty={}, schemes={}", group, identificationChecked, hmrcLetterChecked, contributionAmount, contributionDate, agreedToContributeButNotPaid, mpName, mpEngaged, mpSympathetic, mpConstituency, mpParty, schemes);

        String sql = "update `" + forumDatabaseTablePrefix + "users` u " +
                "set u.usergroup = (select `gid` from `" + forumDatabaseTablePrefix + "usergroups` ug where ug.title = ?), " +
                "u.identification_checked = ?, " +
                "u.hmrc_letter_checked = ?, " +
                "u.contribution_amount = ?, " +
                "u.contribution_date = ?, " +
                "u.agreed_to_contribute_but_not_paid = ?, " +
                "u.mp_name = ?, " +
                "u.mp_engaged = ?, " +
                "u.mp_sympathetic = ?, " +
                "u.mp_constituency = ?, " +
                "u.mp_party = ?, " +
                "u.schemes = ? " +
                "where u.uid = ?";

        LOGGER.info("Created sql: {}", sql);

        int result = jdbcTemplate.update(sql, new Object[] {
                group,
                identificationChecked,
                hmrcLetterChecked,
                contributionAmount,
                unixTime(contributionDate),
                agreedToContributeButNotPaid,
                mpName,
                mpEngaged,
                mpSympathetic,
                mpConstituency,
                mpParty,
                schemes,
                memberId,
        });

        LOGGER.info("Update result: {}", result);
    }

    public Member createForumUserIfNecessary(Enquiry enquiry) {
        List<Member> existingMembers = findExistingForumUsersByField("email", enquiry.getEmailAddress());

        if (!existingMembers.isEmpty()) {
            LOGGER.info("Already existing forum user with email address {}", enquiry.getEmailAddress());
            LOGGER.info("Skipping");
        } else {
            LOGGER.info("No existing forum user found with email address: {}", enquiry.getEmailAddress());
            LOGGER.info("Going to create one");

            Member member = new Member(null, enquiry.getEmailAddress(), extractUsername(enquiry.getEmailAddress()), enquiry.getName(), null, new Date(), false, false, "0", null, null, null, false, false, "", "", false, PasswordSource.getRandomPasswordDetails());

            Long max = jdbcTemplate.queryForObject("select max(uid) from " + forumDatabaseTablePrefix + "users", Long.class);

            if (max == null) {
                max = (long) 1;
            } else {
                max = max + 1;
            }

            String insertSql = "insert into `" + forumDatabaseTablePrefix + "users` (`uid`, `username`, `password`, `salt`, `loginkey`, `email`, `postnum`, `threadnum`, `avatar`, " +
                    "`avatardimensions`, `avatartype`, `usergroup`, `additionalgroups`, `displaygroup`, `usertitle`, `regdate`, `lastactive`, `lastvisit`, `lastpost`, `website`, `icq`, " +
                    "`aim`, `yahoo`, `skype`, `google`, `birthday`, `birthdayprivacy`, `signature`, `allownotices`, `hideemail`, `subscriptionmethod`, `invisible`, `receivepms`, `receivefrombuddy`, " +
                    "`pmnotice`, `pmnotify`, `buddyrequestspm`, `buddyrequestsauto`, `threadmode`, `showimages`, `showvideos`, `showsigs`, `showavatars`, `showquickreply`, `showredirect`, `ppp`, `tpp`, " +
                    "`daysprune`, `dateformat`, `timeformat`, `timezone`, `dst`, `dstcorrection`, `buddylist`, `ignorelist`, `style`, `away`, `awaydate`, `returndate`, `awayreason`, `pmfolders`, `notepad`, " +
                    "`referrer`, `referrals`, `reputation`, `regip`, `lastip`, `language`, `timeonline`, `showcodebuttons`, `totalpms`, `unreadpms`, `warningpoints`, `moderateposts`, `moderationtime`, " +
                    "`suspendposting`, `suspensiontime`, `suspendsignature`, `suspendsigtime`, `coppauser`, `classicpostbit`, `loginattempts`, `usernotes`, `sourceeditor`, `name`) " +
                    "VALUES (?, ?, ?, ?, 'lvhLksjhHGcZIWgtlwNTJNr3bjxzCE2qgZNX6SBTBPbuSLx21u', ?, 0, 0, '', '', '', 8, '', 0, '', ?, ?, ?, 0, '', '0', '', '', '', '', '', " +
                    "'all', '', 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 'linear', 1, 1, 1, 1, 1, 1, 0, 0, 0, '', '', '', 0, 0, '', '', 0, 0, 0, '0', '', '', '', 0, 0, 0, '', '', '', 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, " +
                    "0, 0, 1, '', 0, ?);";

            LOGGER.info("Going to execute insert sql: {}", insertSql);

            int result = jdbcTemplate.update(insertSql, new Object[] {
                    max,
                    member.getUsername(),
                    member.getPasswordDetails().getPasswordHash(),
                    member.getPasswordDetails().getSalt(),
                    member.getEmailAddress(),
                    unixTime(member.getRegistrationDate()),
                    0L,
                    0L,
                    member.getName()
            });

            LOGGER.info("Insertion result: {}", result);

            return member;
        }

        return null;
    }

    public List<Member> findExistingForumUsersByField(String field, String value) {
        return jdbcTemplate.query(buildUserTableSelect() + "where u." + field + " = ?", new Object[]{ value }, (rs, rowNum) -> buildMember(rs));
    }

    private String buildUserTableSelect() {
        return "select u.uid, u.username, u.name, u.email, u.regdate, u.hmrc_letter_checked, u.identification_checked, u.contribution_amount, u.contribution_date, u.agreed_to_contribute_but_not_paid, u.mp_name, u.mp_engaged, u.mp_sympathetic, u.mp_constituency, u.mp_party, u.schemes, ug.title as `group` from " + forumDatabaseTablePrefix + "users u inner join " + forumDatabaseTablePrefix + "usergroups ug on u.usergroup = ug.gid ";
    }

    public long searchCountMembers(Member member) {
        Where where = buildWhereClause(member);
        final boolean hasWhere = !"".equals(where.getSql());
        return jdbcTemplate.queryForObject("select count(*) from (" + buildUserTableSelect() + where.getSql() + ") as t", hasWhere ? where.getArguments().toArray() : null, Long.class);
    }

    public List<Member> searchMembers(long offset, long itemsPerPage, Member member, String sortField, String sortDirection) {
        String pagination = "";

        if (offset > -1 && itemsPerPage > -1) {
            pagination = " limit " + offset + ", " + itemsPerPage + " ";
        }

        Where where = buildWhereClause(member);

        String orderBy = "";

        if (sortField != null && sortDirection != null) {
            orderBy = " order by " + FIELD_TO_COLUMN_TRANSLATIONS.get(sortField) + " " + sortDirection + " ";
        }

        final boolean hasWhere = !"".equals(where.getSql());

        String sql = buildUserTableSelect() + where.getSql() + orderBy + pagination;

        LOGGER.info("sql: {}", sql);

        return jdbcTemplate.query(sql,
                hasWhere ? where.getArguments().toArray() : null,
                (rs, rowNum) -> buildMember(rs)
        );
    }

    private Where buildWhereClause(Member member) {
        List<String> clauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (member.getHmrcLetterChecked() != null) {
            clauses.add("u.hmrc_letter_checked = ?");
            parameters.add(member.getHmrcLetterChecked());
        }

        if (member.getIdentificationChecked() != null) {
            clauses.add("u.identification_checked = ?");
            parameters.add(member.getIdentificationChecked());
        }

        if (member.getMpEngaged() != null) {
            clauses.add("u.mp_engaged = ?");
            parameters.add(member.getMpEngaged());
        }

        if (member.getMpSympathetic() != null) {
            clauses.add("u.mp_sympathetic = ?");
            parameters.add(member.getMpSympathetic());
        }

        if (member.getAgreedToContributeButNotPaid() != null) {
            clauses.add("u.agreed_to_contribute_but_not_paid = ?");
            parameters.add(member.getAgreedToContributeButNotPaid());
        }

        if (member.getName() != null) {
            clauses.add("lower(u.name) like ?");
            parameters.add(like(member.getName()));
        }

        if (member.getEmailAddress() != null) {
            clauses.add("lower(u.email) like ?");
            parameters.add(like(member.getEmailAddress()));
        }

        if (member.getUsername() != null) {
            clauses.add("lower(u.username) like ?");
            parameters.add(like(member.getUsername()));
        }

        if (member.getMpConstituency() != null) {
            clauses.add("lower(u.mp_constituency) like ?");
            parameters.add(like(member.getMpConstituency()));
        }

        if (member.getGroup() != null) {
            clauses.add("lower(ug.title) like ?");
            parameters.add(like(member.getGroup()));
        }

        if (member.getMpName() != null) {
            clauses.add("lower(u.mp_name) like ?");
            parameters.add(like(member.getMpName()));
        }

        if (member.getMpParty() != null) {
            clauses.add("lower(u.mp_party) like ?");
            parameters.add(like(member.getMpParty()));
        }

        String sql = clauses.isEmpty() ? "" : "where ";

        for (int i = 0; i < clauses.size(); i++) {
            sql += clauses.get(i);
            if (i < clauses.size() - 1) {
                sql += " and ";
            }
        }

        return new Where(sql, parameters);
    }

    private Object like(String argument) {
        return "%" + argument.toLowerCase() + "%";
    }

    private Member buildMember(ResultSet rs) throws SQLException {
        return new Member(
                rs.getLong("uid"),
                rs.getString("email"),
                rs.getString("username"),
                rs.getString("name"),
                rs.getString("group"),
                dateFromMyBbRow(rs, "regdate"),
                rs.getBoolean("hmrc_letter_checked"),
                rs.getBoolean("identification_checked"),
                rs.getString("contribution_amount"),
                dateFromMyBbRow(rs, "contribution_date"),
                rs.getString("mp_name"),
                rs.getString("schemes"),
                rs.getBoolean("mp_engaged"),
                rs.getBoolean("mp_sympathetic"),
                rs.getString("mp_constituency"),
                rs.getString("mp_party"),
                rs.getBoolean("agreed_to_contribute_but_not_paid"),
                null
        );
    }

    private Date dateFromMyBbRow(ResultSet rs, String columnName) throws SQLException {
        Long dateInSeconds = rs.getLong(columnName);

        if (dateInSeconds != null && dateInSeconds > 0) {
            return new Date(dateInSeconds * 1000L);
        }

        return null;
    }

    private long unixTime(Date date) {
        if (date == null) {
            return 0;
        }

        return date.getTime() / 1000;
    }

    private String extractUsername(String emailAddress) {
        String usernameCandidate = firstBitOfEmailAddress(emailAddress);
        LOGGER.info("Candidate username: {}", usernameCandidate);

        if (usernameCandidate.length() < 3 || !findExistingForumUsersByField("username", usernameCandidate).isEmpty()) {
            do {
                LOGGER.info("Candidate username: {} already exists! Going to try creating another one.", usernameCandidate);
                usernameCandidate = usernameCandidate(emailAddress);
                LOGGER.info("New candidate username: {}", usernameCandidate);
            } while (!findExistingForumUsersByField("username", usernameCandidate).isEmpty());
        }

        LOGGER.info("Settled on username: {}", usernameCandidate);

        return usernameCandidate;
    }

    private String usernameCandidate(String emailAddress) {
        return firstBitOfEmailAddress(emailAddress) + randomDigit() + randomDigit();
    }

    private char randomDigit() {
        return String.valueOf(new Random().nextInt()).charAt(0);
    }

    private String firstBitOfEmailAddress(String emailAddress) {
        return emailAddress.substring(0, emailAddress.indexOf("@")).replace("\\.", "_");
    }
}
