package uk.co.novinet.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.co.novinet.service.payments.FfcPayment;
import uk.co.novinet.service.payments.FfcPaymentService;

@RestController
public class FfcPaymentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FfcPaymentController.class);

    @Autowired
    private FfcPaymentService ffcPaymentService;

    @GetMapping("/ffc_payment")
    public String get() {
    return "ffc_payment";
}

    @CrossOrigin
    @GetMapping(path = "/ffcpayment")
    public DataContainer getFfcPayment(FfcPayment ffcPayment,
                                        @RequestParam(value = "page", required = false) Long current,
                                        @RequestParam(value = "rows", required = false) Long rowCount,
                                        @RequestParam(value = "searchPhrase", required = false) String searchPhrase,
                                        @RequestParam(value = "sidx", required = false) String sortBy,
                                        @RequestParam(value = "sord", required = false) String sortDirection,
                                        @RequestParam(value = "operator", required = false) String operator) {
        return retrieveData(current, rowCount, searchPhrase, sortBy, sortDirection, ffcPayment, operator == null ? "and" : operator);
    }

    private DataContainer retrieveData(Long current, Long rowCount, String searchPhrase, String sortBy, String sortDirection, FfcPayment ffcPayment, String operator) {
        current = current == null ? 1 : current;
        rowCount = rowCount == null ? 25 : rowCount;

        LOGGER.info("ffcPayment: {}", ffcPayment);
        LOGGER.info("current: {}", current);
        LOGGER.info("rowCount: {}", rowCount);
        LOGGER.info("searchPhrase: {}", searchPhrase);
        LOGGER.info("sortBy: {}", sortBy);
        LOGGER.info("sortDirection: {}", sortDirection);

        long totalCount = ffcPaymentService.searchCountFfcPayment(ffcPayment, operator);

        LOGGER.info("totalCount: {}", totalCount);

        return new DataContainer(current, rowCount, totalCount, (long) Math.ceil(totalCount / rowCount) + 1, ffcPaymentService.searchFfcPayment((current - 1) * rowCount, rowCount, ffcPayment, sortBy, sortDirection, operator));
    }

}