package uk.co.novinet.service.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.rest.Statistics;
import uk.co.novinet.service.mail.Enquiry;
import uk.co.novinet.service.mail.PasswordSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class StatisticsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsService.class);

    @Value("${forumDatabaseTablePrefix}")
    private String forumDatabaseTablePrefix;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Statistics buildStatistics() {
        Double totalContributions = jdbcTemplate.queryForObject("select sum(contribution_amount) from " + forumDatabaseTablePrefix + "users", new Object[] { }, Double.class);
        Integer totalContributors = jdbcTemplate.queryForObject("select count(contribution_amount) from " + forumDatabaseTablePrefix + "users where contribution_amount > 0", new Object[] { }, Integer.class);
        Integer numberOfRegisteredMembers = jdbcTemplate.queryForObject("select count(u.contribution_amount) from " + forumDatabaseTablePrefix + "users u where u.usergroup in (select gid from " + forumDatabaseTablePrefix + "usergroups where title = ? or title = ?)", new Object[] { "Registered", "Moderators" }, Integer.class);
        Integer numberOfGuests = jdbcTemplate.queryForObject("select count(u.contribution_amount) from " + forumDatabaseTablePrefix + "users u where u.usergroup in (select gid from " + forumDatabaseTablePrefix + "usergroups where title = ?)", new Object[] { "LCAG Guests" }, Integer.class);
        Integer totalUsers = numberOfRegisteredMembers + numberOfGuests;

        return new Statistics(totalContributions, totalContributors, numberOfRegisteredMembers, numberOfGuests, totalUsers);
    }

}
