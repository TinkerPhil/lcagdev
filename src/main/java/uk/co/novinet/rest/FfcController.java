package uk.co.novinet.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.novinet.service.payments.FfcContribution;
import uk.co.novinet.service.payments.PaymentDao;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class FfcController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FfcController.class);

    @Autowired
    private PaymentDao paymentDao;

    @CrossOrigin
    @GetMapping(path = "/litigationContributionTotal")
    public BigDecimal getLitigationContributionTotal() {
        return paymentDao.getFfcTotalContributions();
    }

    @CrossOrigin
    @GetMapping(path = "/litigationContributions")
    public List<FfcContribution> getLitigationContributions() {
        return paymentDao.getFfcContributions();
    }

}
