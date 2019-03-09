package uk.co.novinet.service.payments

import spock.lang.Specification
import uk.co.novinet.service.enquiry.MailSenderService
import uk.co.novinet.service.member.Member
import uk.co.novinet.service.member.MemberService

import java.text.SimpleDateFormat

class PaymentServiceTest extends Specification {
    String transactions = """
From: 26/11/2017 to 26/05/2018
\t\t\t\t\t\t\t
Account: XXXX XXXX XXXX 0057
\t\t\t\t\t\t
Date: 25/05/2018
Description:  FASTER PAYMENTS RECEIPT REF.ABC123 FROM A Smith                                            
Amount: 100.00 \t
Balance: 5050.00 
\t\t\t\t\t\t
Date: 24/05/2018
Description:  FASTER PAYMENTS RECEIPT REF.BOBWINKS FROM JONES TD                                       
Amount: 100.00 \t
Balance: 4950.00 
\t\t\t\t\t\t
Date: 24/05/2018
Description:  FASTER PAYMENTS RECEIPT REF.KDMP FROM WILLIAMS MICHAEL                                     
Amount: 50.00 \t
Balance: 4850.00 
\t\t\t\t\t\t
Date: 24/05/2018
Description: BILL PAYMENT FROM MR JAMES ANDREW HARRISON SMYTHE, REFERENCE jim65                          
Amount: 250.00 \t
Balance: 4800.00 
\t\t\t\t\t\t
Date: 23/05/2018
Description: FASTER PAYMENTS RECEIPT  FROM MR L&D STEVENS                          
Amount: 250.00 \t
Balance: 4550.00 
"""

    PaymentService testObj = new PaymentService()
    MemberService memberServiceMock = Mock()
    Member memberMock = Mock()
    PaymentDao paymentDaoMock = Mock()
    MailSenderService mailSenderServiceMock = Mock()

    def setup() {
        testObj.memberService = memberServiceMock
        testObj.paymentDao = paymentDaoMock
        testObj.mailSenderService = mailSenderServiceMock
    }

    def "buildBankTransactions builds a list of BankTransaction objects in oldest date first order"() {
        given:
        List<BankTransaction> bankTransactions = testObj.buildBankTransactions(transactions)

        expect:
        bankTransactions.size() == 5

        bankTransactions[0].date.toEpochMilli() == time("23/05/2018")
        bankTransactions[0].description == "FASTER PAYMENTS RECEIPT  FROM MR L&D STEVENS"
        bankTransactions[0].amount == 250.00d
        bankTransactions[0].runningBalance == 4550.00d
        bankTransactions[0].counterParty == "MR L&D STEVENS"
        bankTransactions[0].reference == ""
        bankTransactions[0].transactionIndexOnDay == 0

        bankTransactions[1].date.toEpochMilli() == time("24/05/2018")
        bankTransactions[1].description == "BILL PAYMENT FROM MR JAMES ANDREW HARRISON SMYTHE, REFERENCE jim65"
        bankTransactions[1].amount == 250.00d
        bankTransactions[1].runningBalance == 4800.00d
        bankTransactions[1].counterParty == "MR JAMES ANDREW HARRISON SMYTHE"
        bankTransactions[1].reference == "jim65"
        bankTransactions[1].transactionIndexOnDay == 2

        bankTransactions[2].date.toEpochMilli() == time("24/05/2018")
        bankTransactions[2].description == "FASTER PAYMENTS RECEIPT REF.KDMP FROM WILLIAMS MICHAEL"
        bankTransactions[2].amount == 50.00d
        bankTransactions[2].runningBalance == 4850.00d
        bankTransactions[2].counterParty == "WILLIAMS MICHAEL"
        bankTransactions[2].reference == "KDMP"
        bankTransactions[2].transactionIndexOnDay == 1

        bankTransactions[3].date.toEpochMilli() == time("24/05/2018")
        bankTransactions[3].description == "FASTER PAYMENTS RECEIPT REF.BOBWINKS FROM JONES TD"
        bankTransactions[3].amount == 100.00d
        bankTransactions[3].runningBalance == 4950.00d
        bankTransactions[3].counterParty == "JONES TD"
        bankTransactions[3].reference == "BOBWINKS"
        bankTransactions[3].transactionIndexOnDay == 0

        bankTransactions[4].date.toEpochMilli() == time("25/05/2018")
        bankTransactions[4].description == "FASTER PAYMENTS RECEIPT REF.ABC123 FROM A Smith"
        bankTransactions[4].amount == 100.00d
        bankTransactions[4].runningBalance == 5050.00d
        bankTransactions[4].counterParty == "A Smith"
        bankTransactions[4].reference == "ABC123"
        bankTransactions[4].transactionIndexOnDay == 0
    }

    def "When importing a set of bank transactions - if the first email fails to send, we still continue to process and import the remaining transaction"() {
        when:
        ImportOutcome importOutcome = testObj.importTransactions(transactions)
        memberServiceMock.getMemberById(_) >> memberMock
        paymentDaoMock.updateEmailSent(_, _) >> { throw new RuntimeException() }

        then:
        importOutcome.importedTransactions == 5
        5 * paymentDaoMock.create(_)
    }

    def "Can import transactions with negative balances"() {
        given:
        String transactions = """
From: 26/11/2017 to 26/05/2018
\t\t\t\t\t\t\t
Account: XXXX XXXX XXXX 0057
\t\t\t\t\t\t
Date: 25/05/2018
Description:  FASTER PAYMENTS RECEIPT REF.ABC123 FROM A Smith                                            
Amount: -100.00 \t
Balance: 5050.00 
\t\t\t\t\t\t
Date: 24/05/2018
Description:  FASTER PAYMENTS RECEIPT REF.BOBWINKS FROM JONES TD                                       
Amount: -100.00 \t
Balance: 4950.00 
\t\t\t\t\t\t
Date: 24/05/2018
Description:  FASTER PAYMENTS RECEIPT REF.KDMP FROM WILLIAMS MICHAEL                                     
Amount: 50.00 \t
Balance: 4850.00 
\t\t\t\t\t\t
Date: 24/05/2018
Description: BILL PAYMENT FROM MR JAMES ANDREW HARRISON SMYTHE, REFERENCE jim65                          
Amount: 250.00 \t
Balance: 4800.00 
\t\t\t\t\t\t
Date: 23/05/2018
Description: FASTER PAYMENTS RECEIPT  FROM MR L&D STEVENS                          
Amount: 250.00 \t
Balance: 4550.00 
"""

        when:
        List<BankTransaction> bankTransactions = testObj.buildBankTransactions(transactions)

        then:
        bankTransactions.size() == 5

        bankTransactions[0].date.toEpochMilli() == time("23/05/2018")
        bankTransactions[0].description == "FASTER PAYMENTS RECEIPT  FROM MR L&D STEVENS"
        bankTransactions[0].amount == 250.00d
        bankTransactions[0].runningBalance == 4550.00d
        bankTransactions[0].counterParty == "MR L&D STEVENS"
        bankTransactions[0].reference == ""
        bankTransactions[0].transactionIndexOnDay == 0

        bankTransactions[1].date.toEpochMilli() == time("24/05/2018")
        bankTransactions[1].description == "BILL PAYMENT FROM MR JAMES ANDREW HARRISON SMYTHE, REFERENCE jim65"
        bankTransactions[1].amount == 250.00d
        bankTransactions[1].runningBalance == 4800.00d
        bankTransactions[1].counterParty == "MR JAMES ANDREW HARRISON SMYTHE"
        bankTransactions[1].reference == "jim65"
        bankTransactions[1].transactionIndexOnDay == 2

        bankTransactions[2].date.toEpochMilli() == time("24/05/2018")
        bankTransactions[2].description == "FASTER PAYMENTS RECEIPT REF.KDMP FROM WILLIAMS MICHAEL"
        bankTransactions[2].amount == 50.00d
        bankTransactions[2].runningBalance == 4850.00d
        bankTransactions[2].counterParty == "WILLIAMS MICHAEL"
        bankTransactions[2].reference == "KDMP"
        bankTransactions[2].transactionIndexOnDay == 1

        bankTransactions[3].date.toEpochMilli() == time("24/05/2018")
        bankTransactions[3].description == "FASTER PAYMENTS RECEIPT REF.BOBWINKS FROM JONES TD"
        bankTransactions[3].amount == -100.00d
        bankTransactions[3].runningBalance == 4950.00d
        bankTransactions[3].counterParty == "JONES TD"
        bankTransactions[3].reference == "BOBWINKS"
        bankTransactions[3].transactionIndexOnDay == 0

        bankTransactions[4].date.toEpochMilli() == time("25/05/2018")
        bankTransactions[4].description == "FASTER PAYMENTS RECEIPT REF.ABC123 FROM A Smith"
        bankTransactions[4].amount == -100.00d
        bankTransactions[4].runningBalance == 5050.00d
        bankTransactions[4].counterParty == "A Smith"
        bankTransactions[4].reference == "ABC123"
        bankTransactions[4].transactionIndexOnDay == 0

    }

    private long time(String dateString) {
        new SimpleDateFormat("dd/MM/yyyy").parse(dateString).time
    }

}
