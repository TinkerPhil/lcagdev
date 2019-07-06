package uk.co.novinet.rest.ffc;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.co.novinet.service.audit.Audit;
import uk.co.novinet.service.payments.FfcTransactionService;
import uk.co.novinet.service.payments.ImportOutcome;

@RestController
public class FfcTransactionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FfcTransactionController.class);

    @Autowired
    private FfcTransactionService ffcTransactionService;

    @Value("${ffcExportCharacterEncoding}")
    private String ffcExportCharacterEncoding;

    @CrossOrigin
    @PostMapping(path = "/ffcUpload")
    @Audit
    public ResponseEntity handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            ImportOutcome importOutcome = ffcTransactionService.importTransactions(IOUtils.toString(file.getInputStream(), ffcExportCharacterEncoding));
            return new ResponseEntity(importOutcome, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}