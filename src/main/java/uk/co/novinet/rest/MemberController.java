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
    public DataContainer getMembers(
            @RequestParam("current") Long current,
            @RequestParam("rowCount") Long rowCount,
            @RequestParam(value = "searchPhrase", required = false) String searchPhrase,
            @RequestParam(value = "sort[group]", required = false) String groupSortDirection,
            @RequestParam(value = "sort[emailAddress]", required = false) String emailAddressSortDirection,
            @RequestParam(value = "sort[username]", required = false) String usernameSortDirection,
            @RequestParam(value = "sort[name]", required = false) String nameSortDirection,
            @RequestParam(value = "sort[registrationDate]", required = false) String registrationSortDirection) {
        return retrieveData(current, rowCount, searchPhrase,
                sortField(groupSortDirection, emailAddressSortDirection, usernameSortDirection, nameSortDirection, registrationSortDirection),
                sortDirection(groupSortDirection, emailAddressSortDirection, usernameSortDirection, nameSortDirection, registrationSortDirection)
        );
    }

    private String sortDirection(String groupSortDirection, String emailAddressSortDirection, String usernameSortDirection, String nameSortDirection, String registrationSortDirection) {
        LOGGER.info("groupSortDirection: {}, emailAddressSortDirection: {}, usernameSortDirection: {}, nameSortDirection: {}, registrationSortDirection: {}", groupSortDirection, emailAddressSortDirection, usernameSortDirection, nameSortDirection, registrationSortDirection);
        if (groupSortDirection != null) {
            return groupSortDirection;
        }

        if (emailAddressSortDirection != null) {
            return emailAddressSortDirection;
        }

        if (usernameSortDirection != null) {
            return usernameSortDirection;
        }

        if (nameSortDirection != null) {
            return nameSortDirection;
        }

        if (registrationSortDirection != null) {
            return registrationSortDirection;
        }

        return null;
    }

    private String sortField(String groupSortDirection, String emailAddressSortDirection, String usernameSortDirection, String nameSortDirection, String registrationSortDirection) {
        if (groupSortDirection != null) {
            return "group";
        }

        if (emailAddressSortDirection != null) {
            return "emailAddress";
        }

        if (usernameSortDirection != null) {
            return "username";
        }

        if (nameSortDirection != null) {
            return "name";
        }

        if (registrationSortDirection != null) {
            return "registrationDate";
        }

        return null;
    }

    private DataContainer retrieveData(Long current, Long rowCount, String searchPhrase, String sortField, String sortDirection) {
        LOGGER.info("current: {}", current);
        LOGGER.info("rowCount: {}", rowCount);
        LOGGER.info("searchPhrase: {}", searchPhrase);
        LOGGER.info("sortField: {}", sortField);
        LOGGER.info("sortDirection: {}", sortDirection);

        long totalCount = memberService.totalCountMembers();

        LOGGER.info("totalCount: {}", totalCount);

        return new DataContainer(current, rowCount, totalCount, memberService.getAllMembers((current - 1) * rowCount, rowCount, searchPhrase, sortField, sortDirection));
    }
}