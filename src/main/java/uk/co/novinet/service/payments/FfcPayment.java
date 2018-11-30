package uk.co.novinet.service.payments;

import java.math.BigDecimal;
import java.time.Instant;

public class FfcPayment {
    private Long id;
    private Long userId;
    private String username;
    private String membershipToken;
    private String firstName;
    private String lastName;
    private String email;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postalCode;
    private String country;
    private BigDecimal grossAmount;
    private Boolean invoiceCreated;
    private Boolean paymentReceived;
    private String stripeToken;
    private String reference;
    private String status;
    private String errorDescription;
    private String paymentType;
    private String paymentMethod;
    private Boolean emailSent;
    private String esig;  //guid on DB!!
    private String signatureData;
    private Boolean hasProvidedSignature;
    //private String signed_contribution_agreement;   //BLOB on DB
    private Instant contributionAgreementSignatureDate;

    public FfcPayment(
            Long id,
            Long userId,
            String username,
            String membershipToken,
            String firstName,
            String lastName,
            String email,
            String addressLine1,
            String addressLine2,
            String city,
            String postalCode,
            String country,
            BigDecimal grossAmount,
            Boolean invoiceCreated,
            Boolean paymentReceived,
            String stripeToken,
            String reference,
            String status,
            String errorDescription,
            String paymentType,
            String paymentMethod,
            Boolean emailSent,
            String esig,  //guid on DB!!
            String signatureData,
            Boolean hasProvidedSignature,
            //String signedContributionAgreement;   //BLOB on DB
            Instant contributionAgreementSignatureDate
    ) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.membershipToken = membershipToken;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
        this.grossAmount = grossAmount;
        this.invoiceCreated = invoiceCreated;
        this.paymentReceived = paymentReceived;
        this.stripeToken = stripeToken;
        this.reference = reference;
        this.status = status;
        this.errorDescription = errorDescription;
        this.paymentType = paymentType;
        this.paymentMethod = paymentMethod;
        this.emailSent = emailSent;
        this.esig = esig;
        this.signatureData = signatureData;
        this.hasProvidedSignature = hasProvidedSignature;
        this.contributionAgreementSignatureDate = contributionAgreementSignatureDate;
    }
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long user_id) {
        this.userId = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMembershipToken() {
        return membershipToken;
    }

    public void setMembershipToken(String membership_token) {
        this.membershipToken = membership_token;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public Boolean getInvoiceCreated() {
        return invoiceCreated;
    }

    public void setInvoiceCreated(Boolean invoiceCreated) {
        this.invoiceCreated = invoiceCreated;
    }

    public Boolean getPaymentReceived() {
        return paymentReceived;
    }

    public void setPaymentReceived(Boolean paymentReceived) {
        this.paymentReceived = paymentReceived;
    }

    public String getStripeToken() {
        return stripeToken;
    }

    public void setStripeToken(String stripeToken) {
        this.stripeToken = stripeToken;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Boolean getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(Boolean emailSent) {
        this.emailSent = emailSent;
    }

    public String getEsig() {
        return esig;
    }

    public void setEsig(String esig) {
        this.esig = esig;
    }

    public String getSignatureData() {
        return signatureData;
    }

    public void setSignatureData(String signatureData) {
        this.signatureData = signatureData;
    }

    public Boolean getHasProvidedSignature() {
        return hasProvidedSignature;
    }

    public void setHasProvidedSignature(Boolean hasProvidedSignature) {
        this.hasProvidedSignature = hasProvidedSignature;
    }

    public Instant getContributionAgreementSignatureDate() {
        return contributionAgreementSignatureDate;
    }

    public void setContributionAgreementSignatureDate(Instant contributionAgreementSignatureDate) {
        this.contributionAgreementSignatureDate = contributionAgreementSignatureDate;
    }
}
