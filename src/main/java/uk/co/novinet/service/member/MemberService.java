package uk.co.novinet.service.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.mail.Enquiry;
import uk.co.novinet.service.mail.PasswordSource;

import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class MemberService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberService.class);

    @Value("${forumDatabaseTablePrefix}")
    private String forumDatabaseTablePrefix;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Member createForumUserIfNecessary(Enquiry enquiry) {
        List<Member> existingMembers = findExistingForumUsersByField("email", enquiry.getEmailAddress());

        if (!existingMembers.isEmpty()) {
            LOGGER.info("Already existing forum user with email address {}", enquiry.getEmailAddress());
            LOGGER.info("Skipping");
        } else {
            LOGGER.info("No existing forum user found with email address: {}", enquiry.getEmailAddress());
            LOGGER.info("Going to create one");

            Member member = new Member(enquiry.getEmailAddress(), extractUsername(enquiry.getEmailAddress()), enquiry.getName(), null, PasswordSource.getRandomPasswordDetails());

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
                    "`suspendposting`, `suspensiontime`, `suspendsignature`, `suspendsigtime`, `coppauser`, `classicpostbit`, `loginattempts`, `usernotes`, `sourceeditor`) " +
                    "VALUES (?, ?, ?, ?, 'lvhLksjhHGcZIWgtlwNTJNr3bjxzCE2qgZNX6SBTBPbuSLx21u', ?, 0, 0, '', '', '', 8, '', 0, '', ?, ?, ?, 0, '', '0', '', '', '', '', '', " +
                    "'all', '', 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 'linear', 1, 1, 1, 1, 1, 1, 0, 0, 0, '', '', '', 0, 0, '', '', 0, 0, 0, '0', '', '', '', 0, 0, 0, '', '', '', 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, " +
                    "0, 0, 1, '', 0);";

            LOGGER.info("Going to execute insert sql: {}", insertSql);

            int result = jdbcTemplate.update(insertSql, new Object[] {
                    max,
                    member.getUsername(),
                    member.getPasswordDetails().getPasswordHash(),
                    member.getPasswordDetails().getSalt(),
                    member.getEmailAddress(),
                    unixTime(),
                    unixTime(),
                    unixTime()
            });

            LOGGER.info("Insertion result: {}", result);

            return member;
        }

        return null;
    }

    public List<Member> findExistingForumUsersByField(String field, String value) {
        return jdbcTemplate.query("select * from " + forumDatabaseTablePrefix + "users where " + field + " = ?", new Object[]{ value },
                (rs, rowNum) -> new Member(rs.getString("email"), rs.getString("username"), null, null, null)
        );
    }

    public long totalCountMembers() {
        return jdbcTemplate.queryForObject("select count(*) from " + forumDatabaseTablePrefix + "users", Long.class);
    }

    public List<Member> getAllMembers(long offset, long itemsPerPage, String searchPhrase) {
        String pagination = "";

        if (offset > -1 && itemsPerPage > -1) {
            pagination = " limit " + offset + ", " + itemsPerPage;
        }

        String where = "";

        if (searchPhrase != null && !searchPhrase.trim().equals("")) {
            where = " where u.uid like ? or u.username like ? or u.email like ? or ug.title like ? ";
        }

        final boolean hasWhere = !"".equals(where);

        String sql = "select u.uid, u.username, u.email, ug.title as `group` from " + forumDatabaseTablePrefix + "users u inner join " + forumDatabaseTablePrefix + "usergroups ug on u.usergroup = ug.gid" + where + pagination;

        LOGGER.info("sql: {}", sql);

        return jdbcTemplate.query(sql,
                hasWhere ? new Object[] {
                    "%" + searchPhrase + "%",
                    "%" + searchPhrase + "%",
                    "%" + searchPhrase + "%",
                    "%" + searchPhrase + "%" } : null,
                (rs, rowNum) -> new Member(
                        rs.getString("email"),
                        rs.getString("username"),
                        null,
                        rs.getString("group"),
                        null
                )
        );
    }

    private long unixTime() {
        return new Date().getTime() / 1000;
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
