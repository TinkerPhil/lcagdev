package uk.co.novinet.service.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.auth.MyBbAuthority;
import uk.co.novinet.service.PersistenceUtils;
import uk.co.novinet.service.enquiry.Enquiry;
import uk.co.novinet.service.enquiry.EnquiryDao;
import uk.co.novinet.service.enquiry.MailSenderService;
import uk.co.novinet.service.enquiry.PasswordDetails;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.primitives.Longs.asList;
import static java.lang.Long.parseLong;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.co.novinet.auth.MyBbPasswordEncoder.hashPassword;
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

    @Autowired
    private EnquiryDao enquiryDao;

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
        put("contributionAmount", "contribution_amount");
        put("howDidYouHearAboutLcag", "u.how_did_you_hear_about_lcag");
        put("hasCompletedMembershipForm", "u.has_completed_membership_form");
        put("memberOfBigGroup", "u.member_of_big_group");
        put("bigGroupUsername", "u.big_group_username");
        put("verifiedBy", "u.verified_by");
        put("verifiedOn", "u.verified_on");
        put("alreadyHaveAnLcagAccountEmailSent", "u.already_have_an_lcag_account_email_sent");
        put("registeredForClaim", "u.registered_for_claim");
        put("hasCompletedClaimParticipantForm", "u.has_completed_claim_participant_form");
        put("hasBeenSentClaimConfirmationEmail", "u.has_been_sent_claim_confirmation_email");
        put("hasOptedOutOfClaim", "u.opted_out_of_claim");
        put("claimToken", "u.claim_token");
        put("country", "u.country");
        put("phoneNumber", "u.phone_number");
        put("sendEmailStatement", "u.send_email_statement");
    }};

    public void update(
            Long memberId,
            String name,
            String group,
            String[] additionalGroups,
            boolean identificationChecked,
            boolean hmrcLetterChecked,
            Boolean agreedToContributeButNotPaid,
            String mpName,
            Boolean mpEngaged,
            Boolean mpSympathetic,
            String mpConstituency,
            String mpParty,
            String schemes,
            String notes,
            String industry,
            Boolean hasCompletedMembershipForm,
            String howDidYouHearAboutLcag,
            Boolean memberOfBigGroup,
            String bigGroupUsername,
            Instant verifiedOn,
            String verifiedBy,
            Boolean registeredForClaim,
            Boolean hasCompletedClaimParticipantForm,
            Boolean hasBeenSentClaimConfirmationEmail,
            Boolean hasOptedOutOfClaim,
            String country,
            String phoneNumber,
            Boolean sendEmailStatement) {
        LOGGER.info("Going to update user with id {}", memberId);
        LOGGER.info("name={}, group={}, identificationChecked={}, hmrcLetterChecked={}, agreedToContributeButNotPaid={}, mpName={}, mpEngaged={}, mpSympathetic={}, " +
                        "mpConstituency={}, mpParty={}, schemes={}, notes={}, industry={}, hasCompletedMembershipForm={}, howDidYouHearAboutLcag={}, " +
                        "memberOfBigGroup={}, bigGroupUsername={}, verifiedOn={}, verifiedBy={}, registeredForClaim={}, hasCompletedClaimParticipantForm={}, " +
                        "hasBeenSentClaimConfirmationEmail={}, hasOptedOutOfClaim={}",
                name, group, identificationChecked, hmrcLetterChecked, agreedToContributeButNotPaid, mpName, mpEngaged, mpSympathetic, mpConstituency, mpParty,
                schemes, notes, industry, hasCompletedMembershipForm, howDidYouHearAboutLcag, verifiedOn, verifiedBy, registeredForClaim,
                hasCompletedClaimParticipantForm, hasBeenSentClaimConfirmationEmail, hasOptedOutOfClaim
        );

        String sql = "update " + usersTableName() + " u " +
                "set u.name = ?," +
                "u.usergroup = (select `gid` from " + userGroupsTableName() + " ug where ug.title = ?), " +
                "u.additionalgroups = ?, " +
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
                "u.verified_by = ?, " +
                "u.registered_for_claim = ?, " +
                "u.has_completed_claim_participant_form = ?, " +
                "u.has_been_sent_claim_confirmation_email = ?, " +
                "u.opted_out_of_claim = ?, " +
                "u.country = ?, " +
                "u.phone_number = ?, " +
                "u.send_email_statement = ? " +
                "where u.uid = ?";

        LOGGER.info("Created sql: {}", sql);

        Member memberBeforeUpdate = getMemberById(memberId);

        int result = jdbcTemplate.update(
                sql,
                name,
                group,
                toMyBbAuthorityIds(additionalGroups),
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
                registeredForClaim,
                hasCompletedClaimParticipantForm,
                hasBeenSentClaimConfirmationEmail,
                hasOptedOutOfClaim,
                country,
                phoneNumber,
                sendEmailStatement,
                memberId
        );

        if (result == 1 && shouldSendFullMembershipEmail(group, memberBeforeUpdate.getGroup())) {
            mailSenderService.sendUpgradedToFullMembershipEmail(memberBeforeUpdate);
        }

        LOGGER.info("Update result: {}", result);
    }

    private String toMyBbAuthorityIds(String[] additionalGroups) {
        if (additionalGroups == null || additionalGroups.length == 0) {
            return "";
        }

        return stream(additionalGroups).map(string -> String.valueOf(MyBbAuthority.fromFriendlyName(string.trim()).getId())).collect(Collectors.joining(","));
    }

    private boolean shouldSendFullMembershipEmail(String newGroup, String oldGroup) {
        MyBbAuthority currentMyBbAuthority = MyBbAuthority.fromFriendlyName(oldGroup);
        return currentMyBbAuthority.isPreRegisteredGroup() && MyBbAuthority.fromFriendlyName(newGroup) == MyBbAuthority.REGISTERED;
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

    public void addNote(Member member, String notes) {
        LOGGER.info("Going to add note for member {}", member);

        String sql = "update " + usersTableName() + " u set " +
                "u.notes = ? " +
                "where u.uid = ?";

        LOGGER.info("Created sql: {}", sql);

        int result = jdbcTemplate.update(
                sql,
                notes,
                member.getId()
        );

        LOGGER.info("Update result: {}", result);
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

    public Enquiry createEnquiry(String name, String emailAddress, String phoneNumber) {
        return enquiryDao.create(new Enquiry(emailAddress, name, phoneNumber));
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

            Member member = new Member(
                    null,
                    enquiry.getEmailAddress(),
                    extractUsername(enquiry.getEmailAddress()),
                    enquiry.getName(),
                    null,
                    emptyList(),
                    Instant.now(),
                    false,
                    false,
                    enquiry.getMpName(),
                    enquiry.getSchemes(),
                    enquiry.getMpEngaged(),
                    enquiry.getMpSympathetic(),
                    enquiry.getMpConstituency(),
                    enquiry.getMpParty(),
                    false,
                    "",
                    enquiry.getIndustry(),
                    guid(),
                    false,
                    generateRandomPasswordDetails(),
                    new BigDecimal("0.00"),
                    enquiry.getHowDidYouHearAboutLcag(),
                    enquiry.getMemberOfBigGroup(),
                    enquiry.getBigGroupUsername(),
                    "",
                    null,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    guid(),
                    null,
                    enquiry.getPhoneNumber());

            Long nextAvailableId = findNextAvailableId("uid", usersTableName());

            String insertSql = "insert into " + usersTableName() + " (`uid`, `username`, `password`, `salt`, `loginkey`, `email`, `postnum`, `threadnum`, `avatar`, " +
                    "`avatardimensions`, `avatartype`, `usergroup`, `additionalgroups`, `displaygroup`, `usertitle`, `regdate`, `lastactive`, `lastvisit`, `lastpost`, `website`, `icq`, " +
                    "`yahoo`, `skype`, `google`, `birthday`, `birthdayprivacy`, `signature`, `allownotices`, `hideemail`, `subscriptionmethod`, `invisible`, `receivepms`, `receivefrombuddy`, " +
                    "`pmnotice`, `pmnotify`, `buddyrequestspm`, `buddyrequestsauto`, `threadmode`, `showimages`, `showvideos`, `showsigs`, `showavatars`, `showquickreply`, `showredirect`, `ppp`, `tpp`, " +
                    "`daysprune`, `dateformat`, `timeformat`, `timezone`, `dst`, `dstcorrection`, `buddylist`, `ignorelist`, `style`, `away`, `awaydate`, `returndate`, `awayreason`, `pmfolders`, `notepad`, " +
                    "`referrer`, `referrals`, `reputation`, `regip`, `lastip`, `language`, `timeonline`, `showcodebuttons`, `totalpms`, `unreadpms`, `warningpoints`, `moderateposts`, `moderationtime`, " +
                    "`suspendposting`, `suspensiontime`, `suspendsignature`, `suspendsigtime`, `coppauser`, `classicpostbit`, `loginattempts`, `usernotes`, `sourceeditor`, `name`, `token`, `has_completed_membership_form`, `claim_token`, " +
                    "`mp_name`, `mp_constituency`, `mp_party`, `mp_engaged`, `mp_sympathetic`, `schemes`, `industry`, `how_did_you_hear_about_lcag`, `member_of_big_group`, `big_group_username`) " +
                    "VALUES (?, ?, ?, ?, 'lvhLksjhHGcZIWgtlwNTJNr3bjxzCE2qgZNX6SBTBPbuSLx21u', ?, 0, 0, '', '', '', 8, '', 0, '', ?, ?, ?, 0, '', '0', '', '', '', '', " +
                    "'all', '', 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 'linear', 1, 1, 1, 1, 1, 1, 0, 0, 0, '', '', '', 0, 0, '', '', 0, 0, 0, '0', '', '', '', 0, 0, 0, '', '', '', 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, " +
                    "0, 0, 1, '', 0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

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
                    false,
                    member.getClaimToken(),
                    emptyStringIfNull(enquiry.getMpName()),
                    emptyStringIfNull(enquiry.getMpConstituency()),
                    emptyStringIfNull(enquiry.getMpParty()),
                    enquiry.getMpEngaged(),
                    enquiry.getMpSympathetic(),
                    emptyStringIfNull(enquiry.getSchemes()),
                    emptyStringIfNull(enquiry.getIndustry()),
                    emptyStringIfNull(enquiry.getHowDidYouHearAboutLcag()),
                    enquiry.getMemberOfBigGroup(),
                    emptyStringIfNull(enquiry.getBigGroupUsername())
            );

            member.setId(nextAvailableId);

            LOGGER.info("Insertion result: {}", result);

            return new MemberCreationResult(false, member);
        }
    }

    public PasswordDetails generateRandomPasswordDetails() {
        try {
            String salt = randomAlphanumeric(8);
            String rawPassword = randomAlphanumeric(15);
            return new PasswordDetails(salt, hashPassword(rawPassword, salt), rawPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    private String emptyStringIfNull(String string) {
        return string == null ? "" : string;
    }

    public static String guid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public Member getMemberById(Long id) {
        return jdbcTemplate.queryForObject(buildUserTableSelect() + " where u.uid = ?", new Object[] { id }, (rs, rowNum) -> buildMember(rs));
    }

    public List<Member> findExistingForumUsersByField(String field, String value) {
        return jdbcTemplate.query(buildUserTableSelect() + "where lower(u." + field + ") = ?" + buildUserTableGroupBy(), new Object[] { value.toLowerCase() }, (rs, rowNum) -> buildMember(rs));
    }
    public List<Member> findExistingForumUsersByFieldWild(String field, String regex) {
        return jdbcTemplate.query(buildUserTableSelect() + "where lower(u." + field + ") like ?" + buildUserTableGroupBy(), new Object[] { regex.toLowerCase() }, (rs, rowNum) -> buildMember(rs));
    }

    private String buildUserTableSelect() {
        return "select u.uid, u.username, u.name, u.email, u.additionalgroups, u.salt, u.password, u.regdate, u.hmrc_letter_checked, u.identification_checked, u.agreed_to_contribute_but_not_paid, " +
                "u.mp_name, u.mp_engaged, u.mp_sympathetic, u.mp_constituency, u.mp_party, u.schemes, u.notes, u.industry, u.token, u.has_completed_membership_form, " +
                "u.how_did_you_hear_about_lcag, u.member_of_big_group, u.big_group_username, u.verified_on, u.verified_by, u.already_have_an_lcag_account_email_sent, " +
                "u.registered_for_claim, u.has_completed_claim_participant_form, u.has_been_sent_claim_confirmation_email, u.opted_out_of_claim, " +
                "u.country, u.claim_token, ug.title as `group`, u.send_email_statement,  sum(ufs.lcagAmount) as `contribution_amount`, u.phone_number " +
                "from " + usersTableName() + " u inner join " + userGroupsTableName() + " ug on u.usergroup = ug.gid " +
                "left outer join " + userFundingSummaryTableName() + " ufs on ufs.uid = u.uid ";
    }

    private String buildUserTableGroupBy() {
        return " group by u.uid ";
    }

    public long searchCountMembers(Member member, String operator) {
        Where where = buildWhereClause(member, operator);
        Having having = buildHavingClause(member, operator);

        String sql = "select count(*) from (" + buildUserTableSelect() + where.getSql() + buildUserTableGroupBy() + having.getSql() + ") as t";
        LOGGER.info("Going to execute this sql: {}", sql);
        return jdbcTemplate.queryForObject(sql, buildArgumentArray(where, having), Long.class);
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

        Having having = buildHavingClause(member, operator);

        String sql = buildUserTableSelect() + where.getSql() + buildUserTableGroupBy() + having.getSql() + orderBy + pagination;

        LOGGER.info("sql: {}", sql);

        return jdbcTemplate.query(sql,
                buildArgumentArray(where, having),
                (rs, rowNum) -> buildMember(rs)
        );
    }

    private Object[] buildArgumentArray(Where where, Having having) {
        boolean hasWhere = !"".equals(where.getSql());
        boolean hasHaving = !"".equals(having.getSql());

        List<Object> result = new ArrayList<>();

        if (hasWhere) {
            result.addAll(where.getArguments());
        }

        if (hasHaving) {
            result.addAll(having.getArguments());
        }

        return result.isEmpty() ? null : result.toArray();
    }

    private Having buildHavingClause(Member member, String operator) {
        List<String> clauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (member.getContributionAmount() != null) {
            clauses.add("contribution_amount = ?");
            parameters.add(member.getContributionAmount());
        }

        return PersistenceUtils.buildHavingClause(clauses, parameters, operator);
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

        String group = member.getGroup();
        if (group != null) {
            if (group.contains(",")) {
                StringBuilder clause = new StringBuilder("(");
                boolean shouldAppendOr = false;
                for (String grp : group.split(",")) {
                    if (shouldAppendOr) {
                        clause.append(" or ");
                    }
                    clause.append("lower(ug.title) like ?");
                    parameters.add(like(grp));
                    shouldAppendOr = true;
                }
                clause.append(")");
                clauses.add(clause.toString());
            } else {
                clauses.add("lower(ug.title) like ?");
                parameters.add(like(group));
            }
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

        if (member.alreadyHaveAnLcagAccountEmailSent() != null) {
            clauses.add("u.already_have_an_lcag_account_email_sent = ?");
            parameters.add(member.alreadyHaveAnLcagAccountEmailSent());
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

        if (member.getClaimToken() != null) {
            clauses.add("lower(u.claim_token) like ?");
            parameters.add(like(member.getClaimToken()));
        }

        if (member.getRegisteredForClaim() != null) {
            clauses.add("u.registered_for_claim = ?");
            parameters.add(member.getRegisteredForClaim());
        }

        if (member.getHasCompletedClaimParticipantForm() != null) {
            clauses.add("u.has_completed_claim_participant_form = ?");
            parameters.add(member.getHasCompletedClaimParticipantForm());
        }

        if (member.getHasBeenSentClaimConfirmationEmail() != null) {
            clauses.add("u.has_been_sent_claim_confirmation_email = ?");
            parameters.add(member.getHasBeenSentClaimConfirmationEmail());
        }

        if (member.getHasOptedOutOfClaim() != null) {
            clauses.add("u.opted_out_of_claim = ?");
            parameters.add(member.getHasOptedOutOfClaim());
        }

        if (member.getSendEmailStatement() != null) {
            clauses.add("u.send_email_statement = ?");
            parameters.add(member.getSendEmailStatement());
        }

        if (member.getCountry() != null) {
            clauses.add("lower(u.country) like ?");
            parameters.add(like(member.getCountry()));
        }

        if (member.getPhoneNumber() != null) {
            clauses.add("lower(u.phone_number) like ?");
            parameters.add(like(member.getPhoneNumber()));
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
                buildAdditionalGroupIds(rs.getString("additionalgroups")),
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
                new PasswordDetails(rs.getString("salt"), rs.getString("password"), null),
                rs.getBigDecimal("contribution_amount"),
                rs.getString("how_did_you_hear_about_lcag"),
                rs.getBoolean("member_of_big_group"),
                rs.getString("big_group_username"),
                rs.getString("verified_by"),
                dateFromMyBbRow(rs, "verified_on"),
                rs.getBoolean("already_have_an_lcag_account_email_sent"),
                rs.getBoolean("registered_for_claim"),
                rs.getBoolean("has_completed_claim_participant_form"),
                rs.getBoolean("has_been_sent_claim_confirmation_email"),
                rs.getBoolean("opted_out_of_claim"),
                rs.getBoolean("send_email_statement"),
                rs.getString("claim_token"),
                rs.getString("country"),
                rs.getString("phone_number"));
    }

    private List<Long> buildAdditionalGroupIds(String additionalGroups) {
        if (isBlank(additionalGroups)) {
            return emptyList();
        }

        if (!additionalGroups.contains(",")) {
            return singletonList(parseLong(additionalGroups.trim()));
        }

        return asList(stream(additionalGroups.split(",")).mapToLong(Long::parseLong).toArray());
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

    public List<MemberGroup> getAllMemberGroups() {
        return jdbcTemplate.query("select * from i7b0_usergroups", (rs, rowNum) -> new MemberGroup(rs.getLong("gid"), rs.getString("title")));
    }

}
