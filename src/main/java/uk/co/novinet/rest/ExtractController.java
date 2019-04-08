package uk.co.novinet.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.novinet.service.audit.Audit;
import uk.co.novinet.service.extract.ExtractService;


@RestController
public class ExtractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractController.class);

    @Autowired
    private ExtractService extractService;

    @CrossOrigin
    @GetMapping(path = "/extract")
    @Audit
    public String extract(
            @RequestParam(value = "extractType") String type,
            @RequestParam(value = "extractSpecial") String special,
            @RequestParam(value = "extractColumns") String columns,
            @RequestParam(value = "extractMpName") String mpName,
            @RequestParam(value = "extractMpConstituency") String mpConstituency,
            @RequestParam(value = "extractMpParty") String mpParty,
            @RequestParam(value = "extractMpTags") String mpTags,
            @RequestParam(value = "extractName") String name,
            @RequestParam(value = "extractEmail") String email,
            @RequestParam(value = "extractUsername") String username,
            @RequestParam(value = "extractTags") String tags,
            @RequestParam(value = "extractField1") String extraField1,
            @RequestParam(value = "extractValue1") String extraValue1,
            @RequestParam(value = "extractField2") String extraField2,
            @RequestParam(value = "extractValue2") String extraValue2
    ) {
        LOGGER.info("Going to extract {}", type);
        return extractService.extract(
                type,
                special,
                columns,
                mpName,
                mpConstituency,
                mpParty,
                mpTags,
                name,
                email,
                username,
                tags,
                extraField1,
                extraValue1,
                extraField2,
                extraValue2
        );
        //return new ResponseEntity(HttpStatus.OK);
    }

}