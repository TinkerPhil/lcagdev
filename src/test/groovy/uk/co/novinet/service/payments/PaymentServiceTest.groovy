package uk.co.novinet.service.payments

import spock.lang.Specification

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
"""

    PaymentService testObj = new PaymentService()

    def "buildBankTransactions builds a list of BankTransaction objects"() {
        given:
        List<BankTransaction> bankTransactions = testObj.buildBankTransactions(transactions)

        expect:
        bankTransactions.size() == 4

        bankTransactions[0].date.time == time("25/05/2018")
        bankTransactions[0].description == "FASTER PAYMENTS RECEIPT REF.ABC123 FROM A Smith"
        bankTransactions[0].amount == 100.00d
        bankTransactions[0].runningBalance == 5050.00d
        bankTransactions[0].counterParty == "A Smith"
        bankTransactions[0].reference == "ABC123"

        bankTransactions[1].date.time == time("24/05/2018")
        bankTransactions[1].description == "FASTER PAYMENTS RECEIPT REF.BOBWINKS FROM JONES TD"
        bankTransactions[1].amount == 100.00d
        bankTransactions[1].runningBalance == 4950.00d
        bankTransactions[1].counterParty == "JONES TD"
        bankTransactions[1].reference == "BOBWINKS"

        bankTransactions[2].date.time == time("24/05/2018")
        bankTransactions[2].description == "FASTER PAYMENTS RECEIPT REF.KDMP FROM WILLIAMS MICHAEL"
        bankTransactions[2].amount == 50.00d
        bankTransactions[2].runningBalance == 4850.00d
        bankTransactions[2].counterParty == "WILLIAMS MICHAEL"
        bankTransactions[2].reference == "KDMP"

        bankTransactions[3].date.time == time("24/05/2018")
        bankTransactions[3].description == "BILL PAYMENT FROM MR JAMES ANDREW HARRISON SMYTHE, REFERENCE jim65"
        bankTransactions[3].amount == 250.00d
        bankTransactions[3].runningBalance == 4800.00d
        bankTransactions[3].counterParty == "MR JAMES ANDREW HARRISON SMYTHE"
        bankTransactions[3].reference == "jim65"
    }

    private long time(String dateString) {
        new SimpleDateFormat("dd/MM/yyyy").parse(dateString).time
    }

}
