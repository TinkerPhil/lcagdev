package uk.co.novinet.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.novinet.service.member.MemberService;

@RestController("/member")
public class MemberController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberService memberService;

    @CrossOrigin
    @PostMapping
    public DataContainer postMembers(@RequestParam("current") Long current, @RequestParam("rowCount") Long rowCount, @RequestParam("searchPhrase") String searchPhrase) {
        return retrieveData(current, rowCount, searchPhrase);
    }

    private DataContainer retrieveData(Long current, Long rowCount, String searchPhrase) {
        LOGGER.info("current: {}", current);
        LOGGER.info("rowCount: {}", rowCount);
        LOGGER.info("searchPhrase: {}", searchPhrase);

        long totalCount = memberService.totalCountMembers();

        LOGGER.info("totalCount: {}", totalCount);

        return new DataContainer(current, rowCount, totalCount, memberService.getAllMembers((current - 1) * rowCount, rowCount, searchPhrase));
    }
}