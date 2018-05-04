package uk.co.novinet.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.novinet.service.member.MemberService;

@RestController
public class MemberController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberService memberService;

    @CrossOrigin
    @PostMapping(path = "/member")
    public DataContainer getMembers(
            @RequestParam(value = "current", required = false) Long current,
            @RequestParam(value = "rowCount", required = false) Long rowCount,
            @RequestParam(value = "searchPhrase", required = false) String searchPhrase,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortDirection", required = false) String sortDirection) {
        return retrieveData(current, rowCount, searchPhrase, sortBy, sortDirection);
    }

    @CrossOrigin
    @PostMapping(path = "/member/group")
    public ResponseEntity setGroup(@RequestParam("memberId") Long memberId, @RequestParam("group") String group) {
        memberService.setGroup(memberId, group);
        return new ResponseEntity(HttpStatus.OK);
    }

    private DataContainer retrieveData(Long current, Long rowCount, String searchPhrase, String sortBy, String sortDirection) {
        current = current == null ? 1 : current;
        rowCount = rowCount == null ? 25 : rowCount;

        LOGGER.info("current: {}", current);
        LOGGER.info("rowCount: {}", rowCount);
        LOGGER.info("searchPhrase: {}", searchPhrase);
        LOGGER.info("sortBy: {}", sortBy);
        LOGGER.info("sortDirection: {}", sortDirection);

        long totalCount = memberService.totalCountMembers();

        LOGGER.info("totalCount: {}", totalCount);

        return new DataContainer(current, rowCount, totalCount, memberService.getAllMembers((current - 1) * rowCount, rowCount, searchPhrase, sortBy, sortDirection));
    }
}