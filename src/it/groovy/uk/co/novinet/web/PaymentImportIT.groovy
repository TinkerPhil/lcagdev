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

    static File tempTransactionFile1
    static File tempTransactionFile2
    static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy")

    @BeforeClass
    static void beforeClass() throws Exception {
        tempTransactionFile1 = File.createTempFile("lcagtransactions", "txt")
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("/payments/santander_transactions_1.txt"), tempTransactionFile1)

        tempTransactionFile2 = File.createTempFile("lcagtransactions", "txt")
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("/payments/santander_transactions_2.txt"), tempTransactionFile2)

        setupDatabaseSchema()
    }

    @Before
    void before() {
        runSqlScript("sql/delete_all_bank_transactions.sql")
    }

    @Test
    void importsNewBankTransactions() throws Exception {
        assertEquals(0, allBankTransactionRows().size())

        uploadBankTransactionFile("http://localhost:8282/paymentUpload", tempTransactionFile1)
        assertEquals(4, allBankTransactionRows().size())
    }

    @Test
    void doesNotReImportDuplicateTransactions() throws Exception {
        assertEquals(0, allBankTransactionRows().size())

        uploadBankTransactionFile("http://localhost:8282/paymentUpload", tempTransactionFile1)
        assertEquals(4, allBankTransactionRows().size())

        uploadBankTransactionFile("http://localhost:8282/paymentUpload", tempTransactionFile1)
        assertEquals(4, allBankTransactionRows().size())
    }

    @Test
    void canImportSecondBatchOfDifferentTransactions() throws Exception {
        assertEquals(0, allBankTransactionRows().size())

        uploadBankTransactionFile("http://localhost:8282/paymentUpload", tempTransactionFile1)
        assertEquals(4, allBankTransactionRows().size())

        uploadBankTransactionFile("http://localhost:8282/paymentUpload", tempTransactionFile2)
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

    }

    def allBankTransactionRows() {
        return new JsonSlurper().parseText(getRequest("http://localhost:8282/payments?rows=1000&sidx=date&sord=asc")).rows
    }
}
