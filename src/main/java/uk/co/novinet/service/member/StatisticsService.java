package uk.co.novinet.service.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.rest.member.Statistics;

import static uk.co.novinet.service.PersistenceUtils.*;

@Service
public class StatisticsService {

    @Value("${forumDatabaseTablePrefix}")
    private String forumDatabaseTablePrefix;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Statistics buildStatistics() {
        Double totalContributions = jdbcTemplate.queryForObject("select sum(ufs.lcagAmount) from " + userFundingSummaryTableName() + " ufs where ufs.uid is not null", new Object[] { }, Double.class);
        Integer totalContributors = jdbcTemplate.queryForObject("select count(distinct(user_id)) from " + bankTransactionsTableName(), new Object[] { }, Integer.class);
        Integer numberOfRegisteredMembers = jdbcTemplate.queryForObject("select count(u.uid) from " + usersTableName() + " u where u.usergroup in (select gid from " + userGroupsTableName() + " where title = ? or title = ?)", new Object[] { "Registered", "Moderators" }, Integer.class);
        Integer numberOfGuests = jdbcTemplate.queryForObject("select count(*) from " + usersTableName() + " u where u.usergroup in (select gid from " + userGroupsTableName() + " where title = ?)", new Object[] { "LCAG Guests" }, Integer.class);
        Integer numberOfSuspended = jdbcTemplate.queryForObject("select count(*) from " + usersTableName() + " u where u.usergroup in (select gid from " + userGroupsTableName() + " where title = ?)", new Object[] { "Suspended" }, Integer.class);
        Integer totalUsers = numberOfRegisteredMembers + numberOfGuests + numberOfSuspended;

        return new Statistics(totalContributions, totalContributors, numberOfRegisteredMembers, numberOfGuests, numberOfSuspended, totalUsers);
    }

}
