package uk.co.novinet.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.novinet.service.member.Member;
import uk.co.novinet.service.member.MemberService;
import uk.co.novinet.service.member.StatisticsService;

import java.util.Date;

@RestController
public class StatisticsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private MemberService memberService;

    @Autowired
    private StatisticsService statisticsService;

    @CrossOrigin
    @GetMapping(path = "/statistics")
    public Statistics getMembers() {
        return statisticsService.buildStatistics();
    }

}