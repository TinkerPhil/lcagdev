package uk.co.novinet.service.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MpDetailsUpdaterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MpDetailsUpdaterService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void updateMpDetails() {
        LOGGER.info("Going to run MP details update SQL");

        try {
            String updateMpNameSql = "UPDATE i7b0_users u JOIN i7b0_mp_details d ON u.mp_constituency = d.mp_constituency " +
                    "SET u.mp_name = CONCAT(d.mp_first_name, ' ', d.mp_last_name), u.mp_party = d.mp_party;";

            LOGGER.info("Executing: {}", updateMpNameSql);

            LOGGER.info("Result: {}", jdbcTemplate.update(updateMpNameSql));

            String updateMpConstituencyAndPartySql = "UPDATE i7b0_users u JOIN i7b0_mp_details d ON u.mp_name = CONCAT(d.mp_first_name, ' ', d.mp_last_name) " +
                    "SET u.mp_constituency = d.mp_constituency, u.mp_party = d.mp_party;";

            LOGGER.info("Executing: {}", updateMpConstituencyAndPartySql);

            LOGGER.info("Result: {}", jdbcTemplate.update(updateMpConstituencyAndPartySql));
        } catch (Exception e) {
            LOGGER.error("Could not execute MP details update SQL", e);
        }

    }
}
