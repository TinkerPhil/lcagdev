package uk.co.novinet.rest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.co.novinet.service.payments.BankTransaction;
import uk.co.novinet.service.payments.ImportOutcome;
import uk.co.novinet.service.payments.PaymentDao;
import uk.co.novinet.service.payments.PaymentService;

@RestController
public class PaymentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentDao paymentDao;

    @Value("${bankExportCharacterEncoding}")
    private String bankExportCharacterEncoding;

    @CrossOrigin
    @GetMapping(path = "/payments")
    public DataContainer getPayments(BankTransaction bankTransaction,
                                    @RequestParam(value = "page", required = false) Long current,
                                    @RequestParam(value = "rows", required = false) Long rowCount,
                                    @RequestParam(value = "searchPhrase", required = false) String searchPhrase,
                                    @RequestParam(value = "sidx", required = false) String sortBy,
                                    @RequestParam(value = "sord", required = false) String sortDirection) {
        return retrieveData(current, rowCount, searchPhrase, sortBy, sortDirection, bankTransaction, "and");
    }

    @CrossOrigin
    @PostMapping(path = "/paymentUpload")
    public ResponseEntity handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            ImportOutcome importOutcome = paymentService.importTransactions(IOUtils.toString(file.getInputStream(), bankExportCharacterEncoding));
            return new ResponseEntity(importOutcome, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CrossOrigin
    @PostMapping(path = "/assignToMember")
    public ResponseEntity assignToMember(AssignToMemberRequest assignToMemberRequest) {
        paymentService.assignToMember(assignToMemberRequest.getMemberId(), assignToMemberRequest.getPaymentId());
        return ResponseEntity.ok().build();
    }

    private DataContainer retrieveData(Long current, Long rowCount, String searchPhrase, String sortBy, String sortDirection, BankTransaction bankTransaction, String operator) {
        current = current == null ? 1 : current;
        rowCount = rowCount == null ? 25 : rowCount;

        LOGGER.info("bankTransaction: {}", bankTransaction);
        LOGGER.info("current: {}", current);
        LOGGER.info("rowCount: {}", rowCount);
        LOGGER.info("searchPhrase: {}", searchPhrase);
        LOGGER.info("sortBy: {}", sortBy);
        LOGGER.info("sortDirection: {}", sortDirection);

        long totalCount = paymentDao.searchCountBankTransactions(bankTransaction, operator);

        LOGGER.info("totalCount: {}", totalCount);

        return new DataContainer(current, rowCount, totalCount, (long) Math.ceil(totalCount / rowCount) + 1, paymentDao.searchBankTransactions((current - 1) * rowCount, rowCount, bankTransaction, sortBy, sortDirection, "operator"));
    }
}
