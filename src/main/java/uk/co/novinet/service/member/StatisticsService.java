package uk.co.novinet.service.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.rest.Statistics;
import uk.co.novinet.service.PersistenceUtils;
import uk.co.novinet.service.mail.Enquiry;
import uk.co.novinet.service.mail.PasswordSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static uk.co.novinet.service.PersistenceUtils.bankTransactionsTableName;
import static uk.co.novinet.service.PersistenceUtils.userGroupsTableName;
import static uk.co.novinet.service.PersistenceUtils.usersTableName;

@Service
public class StatisticsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsService.class);

    @Value("${forumDatabaseTablePrefix}")
    private String forumDatabaseTablePrefix;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Statistics buildStatistics() {
        Double totalContributions = jdbcTemplate.queryForObject("select sum(amount) from " + bankTransactionsTableName() + " bt where bt.user_id is not null", new Object[] { }, Double.class);
        Integer totalContributors = jdbcTemplate.queryForObject("select count(distinct(user_id)) from " + bankTransactionsTableName(), new Object[] { }, Integer.class);
        Integer numberOfRegisteredMembers = jdbcTemplate.queryForObject("select count(u.uid) from " + usersTableName() + " u where u.usergroup in (select gid from " + userGroupsTableName() + " where title = ? or title = ?)", new Object[] { "Registered", "Moderators" }, Integer.class);
        Integer numberOfGuests = jdbcTemplate.queryForObject("select count(*) from " + usersTableName() + " u where u.usergroup in (select gid from " + userGroupsTableName() + " where title = ?)", new Object[] { "LCAG Guests" }, Integer.class);
        Integer totalUsers = numberOfRegisteredMembers + numberOfGuests;

        return new Statistics(totalContributions, totalContributors, numberOfRegisteredMembers, numberOfGuests, totalUsers);
    }

}
