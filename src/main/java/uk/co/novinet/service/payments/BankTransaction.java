package uk.co.novinet.service.payments;

import java.math.BigDecimal;
import java.time.Instant;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class BankTransaction {
    private Long id;
    private Long userId;
    private String username;
    private String emailAddress;
    private Instant date;
    private String description;
    private BigDecimal amount;
    private BigDecimal runningBalance;
    private String counterParty;
    private String reference;
    private Integer transactionIndexOnDay;
    private PaymentSource paymentSource;

    public BankTransaction(
            Long id,
            Long userId,
            String username,
            String emailAddress,
            Instant date,
            String description,
            BigDecimal amount,
            BigDecimal runningBalance,
            String counterParty,
            String reference,
            Integer transactionIndexOnDay,
            PaymentSource paymentSource) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.emailAddress = emailAddress;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.runningBalance = runningBalance;
        this.counterParty = counterParty;
        this.reference = reference;
        this.transactionIndexOnDay = transactionIndexOnDay;
        this.paymentSource = paymentSource;
    }

    void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Instant getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getRunningBalance() {
        return runningBalance;
    }

    public String getCounterParty() {
        return counterParty;
    }

    public String getReference() {
        return reference;
    }

    public String toString() {
        return reflectionToString(this);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setRunningBalance(BigDecimal runningBalance) {
        this.runningBalance = runningBalance;
    }

    public void setCounterParty(String counterParty) {
        this.counterParty = counterParty;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Integer getTransactionIndexOnDay() {
        return transactionIndexOnDay;
    }

    public void setTransactionIndexOnDay(Integer transactionIndexOnDay) {
        this.transactionIndexOnDay = transactionIndexOnDay;
    }

    public PaymentSource getPaymentSource() {
        return paymentSource;
    }
}
