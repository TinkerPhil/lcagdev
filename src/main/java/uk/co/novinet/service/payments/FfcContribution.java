package uk.co.novinet.service.payments;

import java.math.BigDecimal;
import java.time.Instant;

public class FfcContribution {
    private Instant contributionDate;
    private BigDecimal contributionAmount;

    public FfcContribution(Instant contributionDate, BigDecimal contributionAmount) {
        this.contributionDate = contributionDate;
        this.contributionAmount = contributionAmount;
    }

    public Instant getContributionDate() {
        return contributionDate;
    }

    public void setContributionDate(Instant contributionDate) {
        this.contributionDate = contributionDate;
    }

    public BigDecimal getContributionAmount() {
        return contributionAmount;
    }

    public void setContributionAmount(BigDecimal contributionAmount) {
        this.contributionAmount = contributionAmount;
    }
}
