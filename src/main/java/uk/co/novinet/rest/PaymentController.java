package uk.co.novinet.rest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.co.novinet.service.payments.ImportOutcome;
import uk.co.novinet.service.payments.PaymentService;

@RestController
public class PaymentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Value("${transactionFileCharacterEncoding")
    private String transactionFileCharacterEncoding;

    @CrossOrigin
    @PostMapping(path = "/paymentUpload")
    public ResponseEntity handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            ImportOutcome importOutcome = paymentService.importTransactions(IOUtils.toString(file.getInputStream(), transactionFileCharacterEncoding));
            return new ResponseEntity(importOutcome, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
