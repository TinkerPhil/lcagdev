package uk.co.novinet.web

import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertEquals
import static uk.co.novinet.e2e.TestUtils.*

class PaymentImportIT {

    @BeforeClass
    static void beforeClass() throws Exception {
        setupDatabaseSchema()
    }

    @Before
    void before() {
        runSqlScript("sql/delete_all_bank_transactions.sql")
    }

    @Test
    void importsNewBankTransactions() throws Exception {
        assertEquals(0, allBankTransactionRows().size())

        File tempFile = File.createTempFile("lcagtransactions", "txt")
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("/payments/santander_transactions.txt"), tempFile)
        uploadBankTransactionFile("http://localhost:8282/paymentUpload", tempFile)
        assertEquals(48, allBankTransactionRows().size())

    }

    def allBankTransactionRows() {
        return new JsonSlurper().parseText(getRequest("http://localhost:8282/payments?rows=1000")).rows
    }
}
