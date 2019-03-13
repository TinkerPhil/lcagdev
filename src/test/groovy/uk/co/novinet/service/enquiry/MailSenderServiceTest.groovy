package uk.co.novinet.service.enquiry


import spock.lang.Specification
import uk.co.novinet.service.member.Member
import uk.co.novinet.service.payments.BankTransaction

class MailSenderServiceTest extends Specification {

    MailSenderService testObj = new MailSenderService()

    Member memberMock = Mock()
    BankTransaction bankTransactionMock = Mock()
    GoogleDocsReader googleDocsReaderMock = Mock()
    MailerWrapper mailerWrapperMock = Mock()

    def setup() {
        testObj.paymentReceivedEmailSourceUrl = "paymentReceivedEmailSourceUrl"
        testObj.paymentReceivedEmailSubject = "paymentReceivedEmailSubject"
        testObj.refundProcessedEmailSourceUrl = "refundProcessedEmailSourceUrl"
        testObj.refundProcessedEmailSubject = "refundProcessedEmailSubject"

        testObj.mailerWrapper = mailerWrapperMock
        testObj.setGoogleDocsReader(googleDocsReaderMock)

        googleDocsReaderMock.retrieveEmailBodyHtmlFromGoogleDocs("refundProcessedEmailSourceUrl") >> "Refund processed email"
        googleDocsReaderMock.retrieveEmailBodyHtmlFromGoogleDocs("paymentReceivedEmailSourceUrl") >> "Payment received email"
        memberMock.getUsername() >> "username"
        memberMock.getPasswordDetails() >> new PasswordDetails("salt", "hashedPassword", "rawPassword")
        memberMock.getName() >> "name"
        memberMock.getToken() >> "token"
        memberMock.getClaimToken() >> "claimToken"

    }

    def "sends refund email when balance is negative"() {
        when:
        bankTransactionMock.getAmount() >> new BigDecimal("-100.00")
        testObj.sendBankTransactionAssignmentEmail(memberMock, bankTransactionMock)

        then:
        1 * mailerWrapperMock.sendMail(_) >> {arguments ->
            assert arguments[0].getTextHTML() == "Refund processed email"
            assert arguments[0].getSubject() == "refundProcessedEmailSubject"
        }
    }

    def "sends payment received email when balance is positive"() {
        when:
        bankTransactionMock.getAmount() >> new BigDecimal("100.00")
        testObj.sendBankTransactionAssignmentEmail(memberMock, bankTransactionMock)

        then:
        1 * mailerWrapperMock.sendMail(_) >> {arguments ->
            assert arguments[0].getTextHTML() == "Payment received email"
            assert arguments[0].getSubject() == "paymentReceivedEmailSubject"
        }
    }
}
