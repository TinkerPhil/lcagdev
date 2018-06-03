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

import java.time.Instant;
import java.util.Date;

@RestController
public class MemberController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberService memberService;

    @CrossOrigin
    @GetMapping(path = "/member")
    public DataContainer getMembers(Member member,
            @RequestParam(value = "page", required = false) Long current,
            @RequestParam(value = "rows", required = false) Long rowCount,
            @RequestParam(value = "searchPhrase", required = false) String searchPhrase,
            @RequestParam(value = "sidx", required = false) String sortBy,
            @RequestParam(value = "sord", required = false) String sortDirection,
            @RequestParam(value = "operator", required = false) String operator) {
        return retrieveData(current, rowCount, searchPhrase, sortBy, sortDirection, member, operator == null ? "and" : operator);
    }

    @CrossOrigin
    @PostMapping(path = "/member/update")
    public ResponseEntity update(
            @RequestParam("id") Long memberId,
            @RequestParam(value = "identificationChecked", required = false) boolean identificationChecked,
            @RequestParam(value = "hmrcLetterChecked", required = false) boolean hmrcLetterChecked,
            @RequestParam(value = "contributionAmount", required = false) String contributionAmount,
            @RequestParam(value = "contributionDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Instant contributionDate,
            @RequestParam(value = "mpName", required = false) String mpName,
            @RequestParam(value = "mpEngaged", required = false) Boolean mpEngaged,
            @RequestParam(value = "mpSympathetic", required = false) Boolean mpSympathetic,
            @RequestParam(value = "agreedToContributeButNotPaid", required = false) Boolean agreedToContributeButNotPaid,
            @RequestParam(value = "mpConstituency", required = false) String mpConstituency,
            @RequestParam(value = "mpParty", required = false) String mpParty,
            @RequestParam(value = "schemes", required = false) String schemes,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "industry", required = false) String industry,
            @RequestParam("group") String group
    ) {
        memberService.update(memberId, group, identificationChecked, hmrcLetterChecked, contributionAmount, contributionDate, agreedToContributeButNotPaid, mpName, mpEngaged, mpSympathetic, mpConstituency, mpParty, schemes, notes, industry);
        return new ResponseEntity(HttpStatus.OK);
    }

    private DataContainer retrieveData(Long current, Long rowCount, String searchPhrase, String sortBy, String sortDirection, Member member, String operator) {
        current = current == null ? 1 : current;
        rowCount = rowCount == null ? 25 : rowCount;

        LOGGER.info("member: {}", member);
        LOGGER.info("current: {}", current);
        LOGGER.info("rowCount: {}", rowCount);
        LOGGER.info("searchPhrase: {}", searchPhrase);
        LOGGER.info("sortBy: {}", sortBy);
        LOGGER.info("sortDirection: {}", sortDirection);

        long totalCount = memberService.searchCountMembers(member, operator);

        LOGGER.info("totalCount: {}", totalCount);

        return new DataContainer(current, rowCount, totalCount, (long) Math.ceil(totalCount / rowCount) + 1, memberService.searchMembers((current - 1) * rowCount, rowCount, member, sortBy, sortDirection, operator));
    }
}