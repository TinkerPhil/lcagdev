package uk.co.novinet.web

import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import uk.co.novinet.auth.MyBbPasswordEncoder
import uk.co.novinet.e2e.TestUtils

import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZonedDateTime

import static org.junit.Assert.*
import static uk.co.novinet.e2e.TestUtils.*
import static uk.co.novinet.service.PersistenceUtils.dateFromMyBbRow
import static uk.co.novinet.service.PersistenceUtils.toUnixTimestamp

class PaymentImportIT {


    public static final Instant DATE_7 = Instant.parse("2018-01-03T13:00:00Z")
    public static final Instant DATE_6 = Instant.parse("2018-01-02T05:00:00Z")
    public static final Instant DATE_5 = Instant.parse("2018-01-02T00:00:00Z")
    public static final Instant DATE_4 = Instant.parse("2018-01-01T03:00:00Z")
    public static final Instant DATE_3 = Instant.parse("2018-01-01T02:00:00Z")
    public static final Instant DATE_2 = Instant.parse("2018-01-01T01:00:00Z")
    public static final Instant DATE_1 = Instant.parse("2018-01-01T00:00:00Z")

    @BeforeClass
    static void beforeClass() throws Exception {
        setupDatabaseSchema()
    }

    @Before
    void before() {
        runSqlScript("sql/delete_all_bank_transactions.sql")
        deleteAllMessages("roundabout23@test.com")
        runSqlScript("sql/delete_all_users.sql")
        insertUser(9999, "admin", "admin@lcag.com", "Administrators", 4, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")
    }

    @Test
    void importsNewBankTransactions() throws Exception {
        assertEquals(0, allBankTransactionRows().size())

        uploadBankTransactionFile("http://${dashboardHost()}:${dashboardPort()}/payment/upload", tempFile("santander_transactions_1.txt"))
        assertEquals(4, allBankTransactionRows().size())
    }

    @Test
    void doesNotReImportDuplicateTransactions() throws Exception {
        assertEquals(0, allBankTransactionRows().size())

        uploadBankTransactionFile("http://${dashboardHost()}:${dashboardPort()}/payment/upload", tempFile("santander_transactions_1.txt"))
        assertEquals(4, allBankTransactionRows().size())

        uploadBankTransactionFile("http://${dashboardHost()}:${dashboardPort()}/payment/upload", tempFile("santander_transactions_1.txt"))
        assertEquals(4, allBankTransactionRows().size())
    }

    @Test
    void canImportSecondBatchOfDifferentTransactions() throws Exception {
        assertEquals(0, allBankTransactionRows().size())

        uploadBankTransactionFile("http://${dashboardHost()}:${dashboardPort()}/payment/upload", tempFile("santander_transactions_1.txt"))
        assertEquals(4, allBankTransactionRows().size())

        uploadBankTransactionFile("http://${dashboardHost()}:${dashboardPort()}/payment/upload", tempFile("santander_transactions_2.txt"))
        assertEquals(8, allBankTransactionRows().size())

        def transactions = allBankTransactionRows()

        assertNotNull(transactions[0].id)
        assertNull(transactions[0].userId)
        assertNull(transactions[0].username)
        assertNull(transactions[0].emailAddress)
        assertEquals(ZonedDateTime.parse("2018-05-21T00:00:00Z").toEpochSecond(), ((BigDecimal) transactions[0].date).longValue())
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
        assertEquals(ZonedDateTime.parse("2018-05-22T00:00:00Z").toEpochSecond(), ((BigDecimal) transactions[1].date).longValue())
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
        assertEquals(ZonedDateTime.parse("2018-05-23T00:00:00Z").toEpochSecond(), ((BigDecimal) transactions[2].date).longValue())
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
        assertEquals(ZonedDateTime.parse("2018-05-24T00:00:00Z").toEpochSecond(), ((BigDecimal) transactions[3].date).longValue())
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
        assertEquals(ZonedDateTime.parse("2018-05-25T00:00:00Z").toEpochSecond(), ((BigDecimal) transactions[4].date).longValue())
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
        assertEquals(ZonedDateTime.parse("2018-05-26T00:00:00Z").toEpochSecond(), ((BigDecimal) transactions[5].date).longValue())
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
        assertEquals(ZonedDateTime.parse("2018-05-27T00:00:00Z").toEpochSecond(), ((BigDecimal) transactions[6].date).longValue())
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
        assertEquals(ZonedDateTime.parse("2018-05-28T00:00:00Z").toEpochSecond(), ((BigDecimal) transactions[7].date).longValue())
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

        uploadBankTransactionFile("http://${dashboardHost()}:${dashboardPort()}/payment/upload", tempFile("santander_transactions_2_identical_transactions.txt"))
        assertEquals(2, allBankTransactionRows().size())
    }

    @Test
    void matchingUserIsTiedToTransaction() {
        assertEquals(0, TestUtils.getEmails("roundabout23@test.com", "Inbox").size())

        insertUser(1, "roundabout23", "roundabout23@test.com", "Bert Cooper", 2)

        uploadBankTransactionFile("http://${dashboardHost()}:${dashboardPort()}/payment/upload", tempFile("santander_transactions_1.txt"))
        assertEquals(4, allBankTransactionRows().size())

        def transactions = allBankTransactionRows()

        assertNotNull(transactions[0].id)
        assertEquals(1, transactions[0].userId)
        assertEquals("roundabout23", transactions[0].username)
        assertEquals("roundabout23@test.com", transactions[0].emailAddress)
        assertEquals(ZonedDateTime.parse("2018-05-21T00:00:00Z").toEpochSecond(), ((BigDecimal) transactions[0].date).longValue())
        assertEquals("FASTER PAYMENTS RECEIPT REF.roundabout23 FROM COOPER B", transactions[0].description)
        assertEquals(250.00, transactions[0].amount)
        assertEquals(4800.00, transactions[0].runningBalance)
        assertEquals("COOPER B", transactions[0].counterParty)
        assertEquals("roundabout23", transactions[0].reference)

        waitForNEmailsToAppearInFolder(1, "Inbox", "roundabout23@test.com")

        assertEquals(1, TestUtils.getEmails("roundabout23@test.com", "Inbox").size())
        assertEquals("Dear Bert Cooper, Your subscription of £250 has now been received. If you are a newly joined full member, you will be upgraded to full membership as soon as we have reconciled the payment. Note there can be a lag of 1-2 days before this occurs so please be patient. Please remember that you may not be made upgraded until you have provided Photo ID and proof of Loan Scheme usage. If you are an existing full member making an additional donation, we THANK YOU for it. Thank you, LCAG Membership Team", TestUtils.getEmails("roundabout23@test.com", "Inbox")[0].getContent().trim())
        assertEquals("Your payment has been received", TestUtils.getEmails("roundabout23@test.com", "Inbox")[0].getSubject().trim())
    }

    @Test
    void canImportTransactionFilesWithPartiallyOverlappingDates() throws Exception {
        assertEquals(0, allBankTransactionRows().size())

        uploadBankTransactionFile("http://${dashboardHost()}:${dashboardPort()}/payment/upload", tempFile("santander_overlapping_transaction_dates_1.txt"))
        sleep(2000)
        assertEquals(4, allBankTransactionRows().size())

        uploadBankTransactionFile("http://${dashboardHost()}:${dashboardPort()}/payment/upload", tempFile("santander_overlapping_transaction_dates_2.txt"))
        sleep(2000)
        assertEquals(6, allBankTransactionRows().size())

        def transactions = allBankTransactionRows()

        assertNotNull(transactions[0].id)
        assertNull(transactions[0].userId)
        assertNull(transactions[0].username)
        assertNull(transactions[0].emailAddress)
        assertEquals(ZonedDateTime.parse("2018-05-21T00:00:00Z").toEpochSecond(), ((BigDecimal) transactions[0].date).longValue())
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
        assertEquals(ZonedDateTime.parse("2018-05-21T00:00:00Z").toEpochSecond(), ((BigDecimal) transactions[1].date).longValue())
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
        assertEquals(ZonedDateTime.parse("2018-05-21T00:00:00Z").toEpochSecond(), ((BigDecimal) transactions[2].date).longValue())
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
        assertEquals(ZonedDateTime.parse("2018-05-21T00:00:00Z").toEpochSecond(), ((BigDecimal) transactions[3].date).longValue())
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
        assertEquals(ZonedDateTime.parse("2018-05-22T00:00:00Z").toEpochSecond(), ((BigDecimal) transactions[4].date).longValue())
        assertEquals("FASTER PAYMENTS RECEIPT REF.FROM FINK FROM FINK KITCHENS LTD", transactions[4].description)
        assertEquals(100.00, transactions[4].amount)
        assertEquals(4950.00, transactions[4].runningBalance)
        assertEquals("FINK KITCHENS LTD", transactions[4].counterParty)
        assertEquals("FROM FINK", transactions[4].reference)
        assertEquals("SANTANDER", transactions[4].paymentSource)

        assertNotNull(transactions[5].id)
        assertNull(transactions[5].userId)
        assertNull(transactions[5].username)
        assertNull(transactions[5].emailAddress)
        assertEquals(ZonedDateTime.parse("2018-05-22T00:00:00Z").toEpochSecond(), ((BigDecimal) transactions[5].date).longValue())
        assertEquals("FASTER PAYMENTS RECEIPT REF.BOB FRENCH FROM BOB FRENCH", transactions[5].description)
        assertEquals(100.00, transactions[5].amount)
        assertEquals(5050.00, transactions[5].runningBalance)
        assertEquals("BOB FRENCH", transactions[5].counterParty)
        assertEquals("BOB FRENCH", transactions[5].reference)
        assertEquals("SANTANDER", transactions[5].paymentSource)
    }

    @Test
    void transactionsOrderedByDateDesc() throws NoSuchAlgorithmException, IOException {
        insertUser(1, "test", "test@lcag.com", "test", 4, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")

        insertBankTransaction(1, "1", "test", true, "100.00", "100", DATE_1, 0)
        insertBankTransaction(2, "1", "test", true, "100.00", "200", DATE_2, 1)
        insertBankTransaction(3, "1", "test", true, "100.00", "300", DATE_3, 2)
        insertBankTransaction(4, "1", "test", true, "100.00", "400", DATE_4, 3)
        insertBankTransaction(5, "1", "test", true, "100.00", "500", DATE_5, 0)
        insertBankTransaction(6, "1", "test", true, "100.00", "600", DATE_6, 1)
        insertBankTransaction(7, "1", "test", true, "100.00", "700", DATE_7, 0)

        def json = new JsonSlurper().parseText(getRequest("http://${dashboardHost()}:${dashboardPort()}/payment", "admin", "lcag"))

        assertEquals(json.rows[0].date as Long, toUnixTimestamp(DATE_7))
        assertEquals(json.rows[1].date as Long, toUnixTimestamp(DATE_6))
        assertEquals(json.rows[2].date as Long, toUnixTimestamp(DATE_5))
        assertEquals(json.rows[3].date as Long, toUnixTimestamp(DATE_4))
        assertEquals(json.rows[4].date as Long, toUnixTimestamp(DATE_3))
        assertEquals(json.rows[5].date as Long, toUnixTimestamp(DATE_2))
        assertEquals(json.rows[6].date as Long, toUnixTimestamp(DATE_1))
    }

    @Test
    void transactionsOrderedByIndexOnDayAscWhenDatesAreTheSame() throws NoSuchAlgorithmException, IOException {
        insertUser(1, "test", "test@lcag.com", "test", 4, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")

        insertBankTransaction(1, "1", "test", true, "100.00", "100", DATE_1, 0)
        insertBankTransaction(2, "1", "test", true, "100.00", "200", DATE_1, 1)
        insertBankTransaction(3, "1", "test", true, "100.00", "300", DATE_1, 2)
        insertBankTransaction(4, "1", "test", true, "100.00", "400", DATE_1, 3)
        insertBankTransaction(5, "1", "test", true, "100.00", "500", DATE_1, 4)
        insertBankTransaction(6, "1", "test", true, "100.00", "600", DATE_1, 5)
        insertBankTransaction(7, "1", "test", true, "100.00", "700", DATE_1, 6)

        def json = new JsonSlurper().parseText(getRequest("http://${dashboardHost()}:${dashboardPort()}/payment", "admin", "lcag"))

        assertEquals(json.rows[0].id, 1)
        assertEquals(json.rows[1].id, 2)
        assertEquals(json.rows[2].id, 3)
        assertEquals(json.rows[3].id, 4)
        assertEquals(json.rows[4].id, 5)
        assertEquals(json.rows[5].id, 6)
        assertEquals(json.rows[6].id, 7)
    }

    @Test
    void transactionsOrderedByUnassignedThenDateDescThenIndexOnDayAscWhenDatesAreTheSame() throws NoSuchAlgorithmException, IOException {
        insertUser(1, "test", "test@lcag.com", "test", 4, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")

        insertBankTransaction(1, "1", "test", true, "100.00", "100", DATE_1, 0)
        insertBankTransaction(2, "1", "test", true, "100.00", "200", DATE_1, 1)
        insertBankTransaction(3, "1", "test", true, "100.00", "300", DATE_1, 2)
        insertBankTransaction(4, "1", "test", true, "100.00", "400", DATE_1, 3)
        insertBankTransaction(5, null, "test", true, "100.00", "500", DATE_1, 4)
        insertBankTransaction(6, null, "test", true, "100.00", "600", DATE_1, 5)
        insertBankTransaction(7, "1", "test", true, "100.00", "700", DATE_1, 6)

        def json = new JsonSlurper().parseText(getRequest("http://${dashboardHost()}:${dashboardPort()}/payment", "admin", "lcag"))

        assertEquals(json.rows[0].id, 5)
        assertEquals(json.rows[1].id, 6)
        assertEquals(json.rows[2].id, 1)
        assertEquals(json.rows[3].id, 2)
        assertEquals(json.rows[4].id, 3)
        assertEquals(json.rows[5].id, 4)
        assertEquals(json.rows[6].id, 7)
    }

    @Test
    void transactionsOrderedByDateDescThenIndexOnDayAsc() throws NoSuchAlgorithmException, IOException {
        insertUser(1, "test", "test@lcag.com", "test", 4, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt")

        insertBankTransaction(1, "1", "test", true, "100.00", "100", DATE_1, 0)
        insertBankTransaction(2, "1", "test", true, "100.00", "200", DATE_1, 1)
        insertBankTransaction(3, "1", "test", true, "100.00", "300", DATE_1, 2)
        insertBankTransaction(4, "1", "test", true, "100.00", "400", DATE_2, 0)
        insertBankTransaction(5, "1", "test", true, "100.00", "500", DATE_2, 1)
        insertBankTransaction(6, "1", "test", true, "100.00", "600", DATE_2, 2)
        insertBankTransaction(7, "1", "test", true, "100.00", "700", DATE_2, 3)

        def json = new JsonSlurper().parseText(getRequest("http://${dashboardHost()}:${dashboardPort()}/payment", "admin", "lcag"))

        assertEquals(json.rows[0].id, 4)
        assertEquals(json.rows[1].id, 5)
        assertEquals(json.rows[2].id, 6)
        assertEquals(json.rows[3].id, 7)
        assertEquals(json.rows[4].id, 1)
        assertEquals(json.rows[5].id, 2)
        assertEquals(json.rows[6].id, 3)
    }

    def allBankTransactionRows() {
        return new JsonSlurper().parseText(getRequest("http://${dashboardHost()}:${dashboardPort()}/payment?rows=1000&sidx=date&sord=asc", "admin", "lcag")).rows
    }

    File tempFile(filename) {
        File temp = File.createTempFile("lcagtransactions", "txt")
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("/payments/${filename}"), temp)
        return temp
    }
}
