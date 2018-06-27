package uk.co.novinet.web

import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZonedDateTime

import static org.junit.Assert.*
import static uk.co.novinet.e2e.TestUtils.*

class PaymentImportIT {

    @BeforeClass
    static void beforeClass() throws Exception {
        setupDatabaseSchema()
    }

    @Before
    void before() {
        runSqlScript("sql/delete_all_bank_transactions.sql")
        runSqlScript("sql/delete_all_users.sql")
    }

    @Test
    void importsNewBankTransactions() throws Exception {
        assertEquals(0, allBankTransactionRows().size())

        uploadBankTransactionFile("http://localhost:8282/paymentUpload", tempFile("santander_transactions_1.txt"))
        assertEquals(4, allBankTransactionRows().size())
    }

    @Test
    void doesNotReImportDuplicateTransactions() throws Exception {
        assertEquals(0, allBankTransactionRows().size())

        uploadBankTransactionFile("http://localhost:8282/paymentUpload", tempFile("santander_transactions_1.txt"))
        assertEquals(4, allBankTransactionRows().size())

        uploadBankTransactionFile("http://localhost:8282/paymentUpload", tempFile("santander_transactions_1.txt"))
        assertEquals(4, allBankTransactionRows().size())
    }

    @Test
    void canImportSecondBatchOfDifferentTransactions() throws Exception {
        assertEquals(0, allBankTransactionRows().size())

        uploadBankTransactionFile("http://localhost:8282/paymentUpload", tempFile("santander_transactions_1.txt"))
        assertEquals(4, allBankTransactionRows().size())

        uploadBankTransactionFile("http://localhost:8282/paymentUpload", tempFile("santander_transactions_2.txt"))
        assertEquals(8, allBankTransactionRows().size())

        def transactions = allBankTransactionRows()

        assertNotNull(transactions[0].id)
        assertNull(transactions[0].userId)
        assertNull(transactions[0].username)
        assertNull(transactions[0].emailAddress)
        assertEquals(ZonedDateTime.parse("2018-05-21T00:00:00Z").toEpochSecond(), Instant.parse(transactions[0].date).epochSecond)
        assertEquals("FASTER PAYMENTS RECEIPT REF.roundabout23 FROM COOPER B", transactions[0].description)
        assertEquals(250.00, transactions[0].amount)
        assertEquals(4800.00, transactions[0].runningBalance)
        assertEquals("COOPER B", transactions[0].counterParty)
        assertEquals("roundabout23", transactions[0].reference)
        assertEquals("SANTANDER", transactions[0].paymentSource)

        assertNotNull(transactions[1].id)
        assertNull(transactions[1].userId)
        assertNull(transactions[1].username)
        assertNull(transactions[1].emailAddress)
        assertEquals(ZonedDateTime.parse("2018-05-22T00:00:00Z").toEpochSecond(), Instant.parse(transactions[1].date).epochSecond)
        assertEquals("FASTER PAYMENTS RECEIPT REF.MIKE BOWLER FROM M Bowler", transactions[1].description)
        assertEquals(50.00, transactions[1].amount)
        assertEquals(4850.00, transactions[1].runningBalance)
        assertEquals("M Bowler", transactions[1].counterParty)
        assertEquals("MIKE BOWLER", transactions[1].reference)
        assertEquals("SANTANDER", transactions[1].paymentSource)

        assertNotNull(transactions[2].id)
        assertNull(transactions[2].userId)
        assertNull(transactions[2].username)
        assertNull(transactions[2].emailAddress)
        assertEquals(ZonedDateTime.parse("2018-05-23T00:00:00Z").toEpochSecond(), Instant.parse(transactions[2].date).epochSecond)
        assertEquals("FASTER PAYMENTS RECEIPT REF.FROM FINK FROM FINK KITCHENS LTD", transactions[2].description)
        assertEquals(100.00, transactions[2].amount)
        assertEquals(4950.00, transactions[2].runningBalance)
        assertEquals("FINK KITCHENS LTD", transactions[2].counterParty)
        assertEquals("FROM FINK", transactions[2].reference)
        assertEquals("SANTANDER", transactions[2].paymentSource)

        assertNotNull(transactions[3].id)
        assertNull(transactions[3].userId)
        assertNull(transactions[3].username)
        assertNull(transactions[3].emailAddress)
        assertEquals(ZonedDateTime.parse("2018-05-24T00:00:00Z").toEpochSecond(), Instant.parse(transactions[3].date).epochSecond)
        assertEquals("FASTER PAYMENTS RECEIPT REF.BOB FRENCH FROM BOB FRENCH", transactions[3].description)
        assertEquals(100.00, transactions[3].amount)
        assertEquals(5050.00, transactions[3].runningBalance)
        assertEquals("BOB FRENCH", transactions[3].counterParty)
        assertEquals("BOB FRENCH", transactions[3].reference)
        assertEquals("SANTANDER", transactions[3].paymentSource)

        assertNotNull(transactions[4].id)
        assertNull(transactions[4].userId)
        assertNull(transactions[4].username)
        assertNull(transactions[4].emailAddress)
        assertEquals(ZonedDateTime.parse("2018-05-25T00:00:00Z").toEpochSecond(), Instant.parse(transactions[4].date).epochSecond)
        assertEquals("BILL PAYMENT FROM MR JAMES SMITH HENRY JONES, REFERENCE james45", transactions[4].description)
        assertEquals(250.00, transactions[4].amount)
        assertEquals(4800.00, transactions[4].runningBalance)
        assertEquals("MR JAMES SMITH HENRY JONES", transactions[4].counterParty)
        assertEquals("james45", transactions[4].reference)
        assertEquals("SANTANDER", transactions[4].paymentSource)

        assertNotNull(transactions[5].id)
        assertNull(transactions[5].userId)
        assertNull(transactions[5].username)
        assertNull(transactions[5].emailAddress)
        assertEquals(ZonedDateTime.parse("2018-05-26T00:00:00Z").toEpochSecond(), Instant.parse(transactions[5].date).epochSecond)
        assertEquals("FASTER PAYMENTS RECEIPT REF.QSHJ FROM STUART PETERS", transactions[5].description)
        assertEquals(50.00, transactions[5].amount)
        assertEquals(4850.00, transactions[5].runningBalance)
        assertEquals("STUART PETERS", transactions[5].counterParty)
        assertEquals("QSHJ", transactions[5].reference)
        assertEquals("SANTANDER", transactions[5].paymentSource)

        assertNotNull(transactions[6].id)
        assertNull(transactions[6].userId)
        assertNull(transactions[6].username)
        assertNull(transactions[6].emailAddress)
        assertEquals(ZonedDateTime.parse("2018-05-27T00:00:00Z").toEpochSecond(), Instant.parse(transactions[6].date).epochSecond)
        assertEquals("FASTER PAYMENTS RECEIPT REF.JOHNCLOCK FROM CROW VD", transactions[6].description)
        assertEquals(100.00, transactions[6].amount)
        assertEquals(4950.00, transactions[6].runningBalance)
        assertEquals("CROW VD", transactions[6].counterParty)
        assertEquals("JOHNCLOCK", transactions[6].reference)
        assertEquals("SANTANDER", transactions[6].paymentSource)

        assertNotNull(transactions[7].id)
        assertNull(transactions[7].userId)
        assertNull(transactions[7].username)
        assertNull(transactions[7].emailAddress)
        assertEquals(ZonedDateTime.parse("2018-05-28T00:00:00Z").toEpochSecond(), Instant.parse(transactions[7].date).epochSecond)
        assertEquals("FASTER PAYMENTS RECEIPT REF.POP15 FROM B Zen", transactions[7].description)
        assertEquals(100.00, transactions[7].amount)
        assertEquals(5050.00, transactions[7].runningBalance)
        assertEquals("B Zen", transactions[7].counterParty)
        assertEquals("POP15", transactions[7].reference)
        assertEquals("SANTANDER", transactions[7].paymentSource)
    }

    @Test
    void importsBothTransactionsWhenThereAreTwoIdenticalTransactionsOnSameDay() throws Exception {
        assertEquals(0, allBankTransactionRows().size())

        uploadBankTransactionFile("http://localhost:8282/paymentUpload", tempFile("santander_transactions_2_identical_transactions.txt"))
        assertEquals(2, allBankTransactionRows().size())
    }

    @Test
    void matchingUserIsTiedToTransaction() {
        insertUser(1, "roundabout23", "roundabout23@test.com", "Bert Cooper", 2)

        uploadBankTransactionFile("http://localhost:8282/paymentUpload", tempFile("santander_transactions_1.txt"))
        assertEquals(4, allBankTransactionRows().size())

        def transactions = allBankTransactionRows()

        assertNotNull(transactions[0].id)
        assertEquals(1, transactions[0].userId)
        assertEquals("roundabout23", transactions[0].username)
        assertEquals("roundabout23@test.com", transactions[0].emailAddress)
        assertEquals(ZonedDateTime.parse("2018-05-21T00:00:00Z").toEpochSecond(), Instant.parse(transactions[0].date).epochSecond)
        assertEquals("FASTER PAYMENTS RECEIPT REF.roundabout23 FROM COOPER B", transactions[0].description)
        assertEquals(250.00, transactions[0].amount)
        assertEquals(4800.00, transactions[0].runningBalance)
        assertEquals("COOPER B", transactions[0].counterParty)
        assertEquals("roundabout23", transactions[0].reference)
    }

    def allBankTransactionRows() {
        return new JsonSlurper().parseText(getRequest("http://localhost:8282/payments?rows=1000&sidx=date&sord=asc")).rows
    }

    File tempFile(filename) {
        File temp = File.createTempFile("lcagtransactions", "txt")
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("/payments/${filename}"), temp)
        return temp
    }
}
