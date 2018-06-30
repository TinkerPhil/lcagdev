package uk.co.novinet.service.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.PersistenceUtils;
import uk.co.novinet.service.mail.Enquiry;
import uk.co.novinet.service.mail.MailSenderService;
import uk.co.novinet.service.mail.PasswordSource;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

import static uk.co.novinet.service.PersistenceUtils.*;

@Service
public class MemberService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberService.class);

    @Value("${forumDatabaseTablePrefix}")
    private String forumDatabaseTablePrefix;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SftpService sftpService;

    @Autowired
    private MailSenderService mailSenderService;

    private Map<String, String> FIELD_TO_COLUMN_TRANSLATIONS = new HashMap<String, String>() {{
        put("id", "u.uid");
        put("emailAddress", "u.email");
        put("group", "ug.title");
        put("username", "u.username");
        put("name", "u.name");
        put("registrationDate", "u.regdate");
        put("hmrcLetterChecked", "u.hmrc_letter_checked");
        put("identificationChecked", "u.identification_checked");
        put("agreedToContributeButNotPaid", "u.agreed_to_contribute_but_not_paid");
        put("mpName", "u.mp_name");
        put("mpEngaged", "u.mp_engaged");
        put("mpSympathetic", "u.mp_sympathetic");
        put("mpConstituency", "u.mp_constituency");
        put("mpParty", "u.mp_party");
        put("schemes", "u.schemes");
        put("notes", "u.notes");
        put("industry", "u.industry");
        put("contributionAmount", "u.contribution_amount");
        put("howDidYouHearAboutLcag", "u.how_did_you_hear_about_lcag");
        put("hasCompletedMembershipForm", "u.has_completed_membership_form");
        put("memberOfBigGroup", "u.member_of_big_group");
        put("bigGroupUsername", "u.big_group_username");
        put("verifiedBy", "u.verified_by");
        put("verifiedOn", "u.verified_on");
        put("alreadyHaveAnLcagAccountEmailSent", "u.already_have_an_lcag_account_email_sent");
    }};

    public void update(Long memberId, String group, boolean identificationChecked, boolean hmrcLetterChecked, Boolean agreedToContributeButNotPaid, String mpName, Boolean mpEngaged, Boolean mpSympathetic, String mpConstituency, String mpParty, String schemes, String notes, String industry, Boolean hasCompletedMembershipForm, String howDidYouHearAboutLcag, Boolean memberOfBigGroup, String bigGroupUsername, Instant verifiedOn, String verifiedBy) {
        LOGGER.info("Going to update user with id {}", memberId);
        LOGGER.info("group={}, identificationChecked={}, hmrcLetterChecked={}, agreedToContributeButNotPaid={}, mpName={}, mpEngaged={}, mpSympathetic={}, mpConstituency={}, mpParty={}, schemes={}, notes={}, industry={}, hasCompletedMembershipForm={}, howDidYouHearAboutLcag={}, memberOfBigGroup={}, bigGroupUsername={}, verifiedOn={}, verifiedBy={}",
                group, identificationChecked, hmrcLetterChecked, agreedToContributeButNotPaid, mpName, mpEngaged, mpSympathetic, mpConstituency, mpParty, schemes, notes, industry, hasCompletedMembershipForm, howDidYouHearAboutLcag, verifiedOn, verifiedBy);

        Member existingMember = getMemberById(memberId);

        boolean shouldSendFullMembershipEmail = "LCAG Guests".equals(existingMember.getGroup()) && "Registered".equals(group);

        String sql = "update " + usersTableName() + " u " +
                "set u.usergroup = (select `gid` from " + userGroupsTableName() + " ug where ug.title = ?), " +
                "u.identification_checked = ?, " +
                "u.hmrc_letter_checked = ?, " +
                "u.agreed_to_contribute_but_not_paid = ?, " +
                "u.mp_name = ?, " +
                "u.mp_engaged = ?, " +
                "u.mp_sympathetic = ?, " +
                "u.mp_constituency = ?, " +
                "u.mp_party = ?, " +
                "u.schemes = ?, " +
                "u.notes = ?, " +
                "u.industry = ?, " +
                "u.has_completed_membership_form = ?, " +
                "u.how_did_you_hear_about_lcag = ?, " +
                "u.member_of_big_group = ?, " +
                "u.big_group_username = ?, " +
                "u.verified_on = ?, " +
                "u.verified_by = ? " +
                "where u.uid = ?";

        LOGGER.info("Created sql: {}", sql);

        int result = jdbcTemplate.update(
                sql,
                group,
                identificationChecked,
                hmrcLetterChecked,
                agreedToContributeButNotPaid,
                mpName,
                mpEngaged,
                mpSympathetic,
                mpConstituency,
                mpParty,
                schemes,
                notes,
                industry,
                hasCompletedMembershipForm,
                howDidYouHearAboutLcag,
                memberOfBigGroup,
                bigGroupUsername,
                unixTime(verifiedOn),
                verifiedBy,
                memberId
        );

        if (result == 1 && shouldSendFullMembershipEmail) {
            mailSenderService.sendUpgradedToFullMembershipEmail(existingMember);
        }

        LOGGER.info("Update result: {}", result);
    }

    public void verify(Member member, String verifiedBy) {
        LOGGER.info("Going to verify user with id {}", member.getId());

        String sql = "update " + usersTableName() + " u set " +
                "u.identification_checked = ?, " +
                "u.hmrc_letter_checked = ?, " +
                "u.verified_by = ?, " +
                "u.verified_on = ? " +
                "where u.uid = ?";

        LOGGER.info("Created sql: {}", sql);

        int result = jdbcTemplate.update(
                sql,
                true,
                true,
                verifiedBy,
                unixTime(Instant.now()),
                member.getId()
        );

        LOGGER.info("Update result: {}", result);

        sftpService.removeAllDocsForMember(member);

        mailSenderService.sendVerificationEmail(member);
    }

    public void markAsAlreadyHaveAnLcagAccountEmailSent(Member member) {
        LOGGER.info("Going to markAsAlreadyHaveAnLcagAccountEmailSent for member {}", member);

        String sql = "update " + usersTableName() + " u set " +
                "u.already_have_an_lcag_account_email_sent = ? " +
                "where u.uid = ?";

        LOGGER.info("Created sql: {}", sql);

        int result = jdbcTemplate.update(
                sql,
                true,
                member.getId()
        );

        LOGGER.info("Update result: {}", result);
    }

    public MemberCreationResult createForumUserIfNecessary(Enquiry enquiry) {
        List<Member> existingMembers = findExistingForumUsersByField("email", enquiry.getEmailAddress());

        if (!existingMembers.isEmpty()) {
            LOGGER.info("Already existing forum user with email address {}", enquiry.getEmailAddress());
            LOGGER.info("Skipping");
            return new MemberCreationResult(true, existingMembers.get(0));
        } else {
            LOGGER.info("No existing forum user found with email address: {}", enquiry.getEmailAddress());
            LOGGER.info("Going to create one");

            Member member = new Member(null, enquiry.getEmailAddress(), extractUsername(enquiry.getEmailAddress()), enquiry.getName(), null, Instant.now(), false, false, null, null, false, false, "", "", false, "", "", UUID.randomUUID().toString().replace("-", ""), false, PasswordSource.getRandomPasswordDetails(), new BigDecimal("0.00"), "", false, "", "", null, false);

            Long nextAvailableId = findNextAvailableId("uid", usersTableName());

            String insertSql = "insert into " + usersTableName() + " (`uid`, `username`, `password`, `salt`, `loginkey`, `email`, `postnum`, `threadnum`, `avatar`, " +
                    "`avatardimensions`, `avatartype`, `usergroup`, `additionalgroups`, `displaygroup`, `usertitle`, `regdate`, `lastactive`, `lastvisit`, `lastpost`, `website`, `icq`, " +
                    "`aim`, `yahoo`, `skype`, `google`, `birthday`, `birthdayprivacy`, `signature`, `allownotices`, `hideemail`, `subscriptionmethod`, `invisible`, `receivepms`, `receivefrombuddy`, " +
                    "`pmnotice`, `pmnotify`, `buddyrequestspm`, `buddyrequestsauto`, `threadmode`, `showimages`, `showvideos`, `showsigs`, `showavatars`, `showquickreply`, `showredirect`, `ppp`, `tpp`, " +
                    "`daysprune`, `dateformat`, `timeformat`, `timezone`, `dst`, `dstcorrection`, `buddylist`, `ignorelist`, `style`, `away`, `awaydate`, `returndate`, `awayreason`, `pmfolders`, `notepad`, " +
                    "`referrer`, `referrals`, `reputation`, `regip`, `lastip`, `language`, `timeonline`, `showcodebuttons`, `totalpms`, `unreadpms`, `warningpoints`, `moderateposts`, `moderationtime`, " +
                    "`suspendposting`, `suspensiontime`, `suspendsignature`, `suspendsigtime`, `coppauser`, `classicpostbit`, `loginattempts`, `usernotes`, `sourceeditor`, `name`, `token`, `has_completed_membership_form`) " +
                    "VALUES (?, ?, ?, ?, 'lvhLksjhHGcZIWgtlwNTJNr3bjxzCE2qgZNX6SBTBPbuSLx21u', ?, 0, 0, '', '', '', 8, '', 0, '', ?, ?, ?, 0, '', '0', '', '', '', '', '', " +
                    "'all', '', 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 'linear', 1, 1, 1, 1, 1, 1, 0, 0, 0, '', '', '', 0, 0, '', '', 0, 0, 0, '0', '', '', '', 0, 0, 0, '', '', '', 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, " +
                    "0, 0, 1, '', 0, ?, ?, ?);";

            LOGGER.info("Going to execute insert sql: {}", insertSql);

            int result = jdbcTemplate.update(insertSql,
                    nextAvailableId,
                    member.getUsername(),
                    member.getPasswordDetails().getPasswordHash(),
                    member.getPasswordDetails().getSalt(),
                    member.getEmailAddress(),
                    unixTime(member.getRegistrationDate()),
                    0L,
                    0L,
                    member.getName(),
                    member.getToken(),
                    false
            );

            member.setId(nextAvailableId);

            LOGGER.info("Insertion result: {}", result);

            return new MemberCreationResult(false, member);
        }
    }

    public Member getMemberById(Long id) {
        return jdbcTemplate.queryForObject(buildUserTableSelect() + " where u.uid = ?", new Object[] { id }, (rs, rowNum) -> buildMember(rs));
    }

    public List<Member> findExistingForumUsersByField(String field, String value) {
        return jdbcTemplate.query(buildUserTableSelect() + "where lower(u." + field + ") = ?" + buildUserTableGroupBy(), new Object[] { value.toLowerCase() }, (rs, rowNum) -> buildMember(rs));
    }

    private String buildUserTableSelect() {
        return "select u.uid, u.username, u.name, u.email, u.regdate, u.hmrc_letter_checked, u.identification_checked, u.agreed_to_contribute_but_not_paid, u.mp_name, u.mp_engaged, u.mp_sympathetic, u.mp_constituency, u.mp_party, u.schemes, u.notes, u.industry, u.token, u.has_completed_membership_form, u.how_did_you_hear_about_lcag, u.member_of_big_group, u.big_group_username, u.verified_on, u.verified_by, u.already_have_an_lcag_account_email_sent, ug.title as `group`, bt.id as `bank_transaction_id`, sum(bt.amount) as `contribution_amount`" +
                "from " + usersTableName() + " u inner join " + userGroupsTableName() + " ug on u.usergroup = ug.gid " +
                "left outer join " + bankTransactionsTableName() + " bt on bt.user_id = u.uid ";
    }

    private String buildUserTableGroupBy() {
        return " group by u.uid ";
    }

    public long searchCountMembers(Member member, String operator) {
        Where where = buildWhereClause(member, operator);
        final boolean hasWhere = !"".equals(where.getSql());
        String sql = "select count(*) from (" + buildUserTableSelect() + where.getSql() + buildUserTableGroupBy() + ") as t";
        LOGGER.info("Going to execute this sql: {}", sql);
        return jdbcTemplate.queryForObject(sql, hasWhere ? where.getArguments().toArray() : null, Long.class);
    }

    public List<Member> searchMembers(long offset, long itemsPerPage, Member member, String sortField, String sortDirection, String operator) {
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

    private Where buildWhereClause(Member member, String operator) {
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

        if (member.getMemberOfBigGroup() != null) {
            clauses.add("u.member_of_big_group = ?");
            parameters.add(member.getMemberOfBigGroup());
        }

        if (member.getBigGroupUsername() != null) {
            clauses.add("lower(u.big_group_username) like ?");
            parameters.add(like(member.getBigGroupUsername()));
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

        if (member.getSchemes() != null) {
            clauses.add("lower(u.schemes) like ?");
            parameters.add(like(member.getSchemes()));
        }

        if (member.getNotes() != null) {
            clauses.add("lower(u.notes) like ?");
            parameters.add(like(member.getNotes()));
        }

        if (member.getIndustry() != null) {
            clauses.add("lower(u.industry) like ?");
            parameters.add(like(member.getIndustry()));
        }

        if (member.getToken() != null) {
            clauses.add("lower(u.token) like ?");
            parameters.add(like(member.getToken()));
        }

        if (member.getHowDidYouHearAboutLcag() != null) {
            clauses.add("lower(u.how_did_you_hear_about_lcag) like ?");
            parameters.add(like(member.getHowDidYouHearAboutLcag()));
        }

        if (member.getHasCompletedMembershipForm() != null) {
            clauses.add("u.has_completed_membership_form = ?");
            parameters.add(member.getHasCompletedMembershipForm());
        }

        if (member.getHasCompletedMembershipForm() != null) {
            clauses.add("u.already_have_an_lcag_account_email_sent = ?");
            parameters.add(member.alreadyHaveAnLcagAccountEmailSent());
        }

        if (member.getContributionAmount() != null) {
            clauses.add("contribution_amount = ?");
            parameters.add(member.getContributionAmount());
        }

        if (member.getVerifiedBy() != null) {
            if ("<NULL>".equals(member.getVerifiedBy())) {
                clauses.add("(u.verified_by is null or u.verified_by = '')");
            } else {
                clauses.add("lower(u.verified_by) like ?");
                parameters.add(like(member.getVerifiedBy()));
            }
        }

        if (member.getToken() != null) {
            clauses.add("lower(u.token) like ?");
            parameters.add(like(member.getToken()));
        }

        return PersistenceUtils.buildWhereClause(clauses, parameters, operator);
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
                rs.getString("mp_name"),
                rs.getString("schemes"),
                rs.getBoolean("mp_engaged"),
                rs.getBoolean("mp_sympathetic"),
                rs.getString("mp_constituency"),
                rs.getString("mp_party"),
                rs.getBoolean("agreed_to_contribute_but_not_paid"),
                rs.getString("notes"),
                rs.getString("industry"),
                rs.getString("token"),
                rs.getBoolean("has_completed_membership_form"),
                null,
                rs.getBigDecimal("contribution_amount"),
                rs.getString("how_did_you_hear_about_lcag"),
                rs.getBoolean("member_of_big_group"),
                rs.getString("big_group_username"),
                rs.getString("verified_by"),
                dateFromMyBbRow(rs, "verified_on"),
                rs.getBoolean("already_have_an_lcag_account_email_sent")
        );
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

    private String randomDigit() {
        return String.valueOf(new Random().nextInt(9));
    }

    private String firstBitOfEmailAddress(String emailAddress) {
        return emailAddress.substring(0, emailAddress.indexOf("@"));
    }


}
