package uk.co.novinet.web

import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertEquals
import static uk.co.novinet.e2e.TestUtils.*

class PaymentImportIT {

    static File tempTransactionFile1
    static File tempTransactionFile2

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
    }

    def allBankTransactionRows() {
        return new JsonSlurper().parseText(getRequest("http://localhost:8282/payments?rows=1000")).rows
    }
}
