package uk.co.novinet.rest.mp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.novinet.rest.DataContainer;
import uk.co.novinet.service.audit.Audit;
import uk.co.novinet.service.mp.MpService;
import uk.co.novinet.service.mp.MP;


@RestController
public class MpController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MpController.class);

    @Autowired
    private MpService mpService;

    @CrossOrigin
    @GetMapping(path = "/mp")
    @Audit
    public DataContainer getMps(MP mp,
                                @RequestParam(value = "page", required = false) Long current,
                                @RequestParam(value = "rows", required = false) Long rowCount,
                                @RequestParam(value = "searchPhrase", required = false) String searchPhrase,
                                @RequestParam(value = "sidx", required = false) String sortBy,
                                @RequestParam(value = "sord", required = false) String sortDirection,
                                @RequestParam(value = "operator", required = false) String operator) {
        return retrieveData(current, rowCount, searchPhrase, sortBy, sortDirection, mp, operator == null ? "and" : operator);
    }

    @CrossOrigin
    @PostMapping(path = "/mp/update")
    @Audit
    public ResponseEntity update(
            @RequestParam("id") Long MpId,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "mpName", required = false) String mpName,
            @RequestParam(value = "party", required = false) String party,
            @RequestParam(value = "twitter", required = false) String twitter,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "constituency", required = false) String constituency,
            @RequestParam(value = "constituencyAddress", required = false) String constituencyAddress,
            @RequestParam(value = "edmStatus", required = false) String edmStatus,
            @RequestParam(value = "edmUrl", required = false) String edmUrl,
            @RequestParam(value = "ministerialStatus", required = false) String ministerialStatus,
            @RequestParam(value = "url", required = false) String url,
            @RequestParam(value = "majority", required = false) Integer majority,
            @RequestParam(value = "telNo", required = false) String telNo
    ) {
        mpService.update(
                MpId,
                lastName,
                firstName,
                mpName,
                party,
                twitter,
                email,
                constituency,
                constituencyAddress,
                edmStatus,
                edmUrl,
                ministerialStatus,
                url,
                majority,
                telNo
        );
        return new ResponseEntity(HttpStatus.OK);
    }

    @CrossOrigin
    @PostMapping(path = "/mp/updateCampaign")
    @Audit
    public ResponseEntity updateCampaign(
            @RequestParam("id") Long MpId,
            @RequestParam(value = "edmStatus", required = false) String edmStatus,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "campaignNotes", required= false) String campaignNotes
    ) {
        mpService.updateCampaign(
                MpId,
                edmStatus,
                tags,
                campaignNotes
        );
        return new ResponseEntity(HttpStatus.OK);
    }

    private DataContainer retrieveData(Long current, Long rowCount, String searchPhrase, String sortBy, String sortDirection, MP mp, String operator) {
        current = current == null ? 1 : current;
        rowCount = rowCount == null ? 25 : rowCount;

        LOGGER.info("mp: {}", mp);
        LOGGER.info("current: {}", current);
        LOGGER.info("rowCount: {}", rowCount);
        LOGGER.info("searchPhrase: {}", searchPhrase);
        LOGGER.info("sortBy: {}", sortBy);
        LOGGER.info("sortDirection: {}", sortDirection);

        long totalCount = mpService.searchCountMps(mp, operator);

        LOGGER.info("totalCount: {}", totalCount);

        return new DataContainer(current, rowCount, totalCount, (long) Math.ceil(totalCount / rowCount) + 1, mpService.searchMps((current - 1) * rowCount, rowCount, mp, sortBy, sortDirection, operator));
    }

}