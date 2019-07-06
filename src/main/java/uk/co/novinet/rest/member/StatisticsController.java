package uk.co.novinet.rest.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.co.novinet.service.audit.Audit;
import uk.co.novinet.service.member.MemberService;
import uk.co.novinet.service.member.StatisticsService;

@RestController
public class StatisticsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private MemberService memberService;

    @Autowired
    private StatisticsService statisticsService;

    @CrossOrigin
    @GetMapping(path = "/statistics")
    @Audit
    public Statistics getMembers() {
        return statisticsService.buildStatistics();
    }

}