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
import uk.co.novinet.service.member.SftpDocument;
import uk.co.novinet.service.member.SftpService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MemberController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberService memberService;

    @Autowired
    private SftpService sftpService;

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
    @GetMapping(path = "/member/emailAddresses")
    public List<String> getMemberEmailAddresses(Member member) {
        return memberService.searchMembers(0, 10000, member, "emailAddress", "asc", "and").stream().map(m -> m.getEmailAddress()).collect(Collectors.toList());
    }

    @CrossOrigin
    @PostMapping(path = "/member/verify")
    public ResponseEntity verify(@RequestParam("id") Long memberId, @RequestParam("verifiedBy") String verifiedBy) {
        Member member = memberService.getMemberById(memberId);

        if (member == null) {
            return ResponseEntity.notFound().build();
        }

        memberService.verify(
                member,
                verifiedBy
        );

        return new ResponseEntity(HttpStatus.OK);
    }

    @CrossOrigin
    @PostMapping(path = "/member/update")
    public ResponseEntity update(
            @RequestParam("id") Long memberId,
            @RequestParam(value = "identificationChecked", required = false) boolean identificationChecked,
            @RequestParam(value = "hmrcLetterChecked", required = false) boolean hmrcLetterChecked,
            @RequestParam(value = "mpName", required = false) String mpName,
            @RequestParam(value = "mpEngaged", required = false) Boolean mpEngaged,
            @RequestParam(value = "mpSympathetic", required = false) Boolean mpSympathetic,
            @RequestParam(value = "agreedToContributeButNotPaid", required = false) Boolean agreedToContributeButNotPaid,
            @RequestParam(value = "mpConstituency", required = false) String mpConstituency,
            @RequestParam(value = "mpParty", required = false) String mpParty,
            @RequestParam(value = "schemes", required = false) String schemes,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "industry", required = false) String industry,
            @RequestParam(value = "hasCompletedMembershipForm", required = false) Boolean hasCompletedMembershipForm,
            @RequestParam(value = "howDidYouHearAboutLcag", required = false) String howDidYouHearAboutLcag,
            @RequestParam(value = "memberOfBigGroup", required = false) Boolean memberOfBigGroup,
            @RequestParam(value = "bigGroupUsername", required = false) String bigGroupUsername,
            @RequestParam(value = "verifiedOn", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date verifiedOn,
            @RequestParam(value = "verifiedBy", required = false) String verifiedBy,
            @RequestParam("group") String group
    ) {
        memberService.update(memberId, group, identificationChecked, hmrcLetterChecked, agreedToContributeButNotPaid, mpName, mpEngaged, mpSympathetic, mpConstituency, mpParty, schemes, notes, industry, hasCompletedMembershipForm, howDidYouHearAboutLcag, memberOfBigGroup, bigGroupUsername, verifiedOn == null ? null : verifiedOn.toInstant(), verifiedBy);
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

    @CrossOrigin
    @GetMapping(path = "/member/documents")
    public List<SftpDocument> getDocuments(@RequestParam("memberId") Long memberId) {
        Member member = memberService.getMemberById(memberId);

        if (member.getMemberOfBigGroup()) {
            return Collections.emptyList();
        }

        return sftpService.getAllDocumentsForMember(member);
    }

    @CrossOrigin
    @GetMapping(path = "/member/document/download")
    public void downloadDocument(@RequestParam("path") String path, HttpServletResponse response) {
        try {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + path.substring(path.lastIndexOf("/") + 1) + "\"");
            sftpService.downloadDocument(path, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}