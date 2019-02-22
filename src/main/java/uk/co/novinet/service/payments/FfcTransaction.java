package uk.co.novinet.service.payments;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class FfcTransaction {
    private Long id;
    private Date txnDate;
    private String description;
    private BigDecimal amount;
    private String username;
    private Long userId;

    public FfcTransaction(
            Long id,
            Date txnDate,
            String description,
            BigDecimal amount,
//            String username,
            Long userId) {
        this.id = id;
        this.txnDate = txnDate;
        this.description = description;
        this.amount = amount;
//        this.username = username;
        this.userId = userId;
    }

    void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Date getTxnDate() {
        return txnDate;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setTxnDate(Date txnDate) {
        this.txnDate = txnDate;
    }

    public String getTxnDateAsYYYYMMDD() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        return formatter.format(getTxnDate());
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }


    public String toString() {
        return reflectionToString(this);
    }

}
