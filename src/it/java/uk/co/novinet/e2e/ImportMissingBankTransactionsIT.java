package uk.co.novinet.e2e;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static org.apache.commons.lang3.StringUtils.truncate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.co.novinet.e2e.TestUtils.*;

public class ImportMissingBankTransactionsIT {

    private String contributorUsername1;
    private String contributorUsername2;
    private String contributorUsername3;
    private String contributorUsername4;

    @BeforeClass
    public static void beforeClass() throws Exception {
        setupDatabaseSchema();
    }

    @Before
    public void before() {
        runSqlScript("sql/delete_all_users.sql");
        runSqlScript("sql/delete_all_bank_transactions.sql");
        runSqlScript("sql/delete_all_bank_transactions_infull.sql");

        contributorUsername1 = truncate("contributor1" + System.currentTimeMillis(), 18);
        contributorUsername2 = truncate("contributor2" + System.currentTimeMillis(), 18);
        contributorUsername3 = truncate("contributor3" + System.currentTimeMillis(), 18);
        contributorUsername4 = truncate("contributor4" + System.currentTimeMillis(), 18);

        insertUser(1, contributorUsername1, contributorUsername1 + "@victim.com", "Testy Test1", 8);
        insertUser(2, contributorUsername2, contributorUsername2 + "@victim.com", "Testy Test2", 8);
        insertUser(3, contributorUsername3, contributorUsername3 + "@victim.com", "Testy Test3", 8);
        insertUser(4, contributorUsername4, contributorUsername4 + "@victim.com", "Testy Test4", 8);

        deleteAllMessages(contributorUsername1 + "@victim.com");
        deleteAllMessages(contributorUsername2 + "@victim.com");
        deleteAllMessages(contributorUsername3 + "@victim.com");
        deleteAllMessages(contributorUsername4 + "@victim.com");
    }

    @After
    public void teardown() {
        runSqlScript("sql/delete_all_users.sql");
        runSqlScript("sql/delete_all_bank_transactions.sql");
        runSqlScript("sql/delete_all_bank_transactions_infull.sql");

        deleteAllMessages(contributorUsername1 + "@victim.com");
        deleteAllMessages(contributorUsername2 + "@victim.com");
        deleteAllMessages(contributorUsername3 + "@victim.com");
        deleteAllMessages(contributorUsername4 + "@victim.com");
    }

    @Test
    public void importsMissingBankTransactionsWhenThereAreNoExistingBankTransactions() throws Exception {
        assertTrue(getBankTransactionRows().isEmpty());

        runSqlUpdate(format("INSERT INTO `i7b0_bank_transactions_infull` (`id`, `moneyIn`, `rollingBalance`, `description`, `date`)\n" +
                        "VALUES\n" +
                        "\t(588, 50.00, 69178.86, 'FASTER PAYMENTS RECEIPT REF.%s FROM TEST1 S P ', '2019-02-13 00:00:00'),\n" +
                        "\t(603, 50.00, 68328.86, 'FASTER PAYMENTS RECEIPT REF.%s FROM TEST2 ', '2019-02-13 00:00:00'),\n" +
                        "\t(606, 100.00, 68178.86, 'FASTER PAYMENTS RECEIPT REF.%s FROM TEST3 ', '2019-02-13 00:00:00'),\n" +
                        "\t(608, 100.00, 67978.86, 'FASTER PAYMENTS RECEIPT REF.%s FROM TEST4 ', '2019-02-13 00:00:00');\n",
                contributorUsername1,
                contributorUsername2,
                contributorUsername3,
                contributorUsername4)
        );

        waitForNEmailsToAppearInFolder(1, "Inbox", contributorUsername1 + "@victim.com");
        waitForNEmailsToAppearInFolder(1, "Inbox", contributorUsername2 + "@victim.com");
        waitForNEmailsToAppearInFolder(1, "Inbox", contributorUsername3 + "@victim.com");
        waitForNEmailsToAppearInFolder(1, "Inbox", contributorUsername4 + "@victim.com");

        assertEquals(1, getEmails(contributorUsername1 + "@victim.com", "Inbox").size());
        assertEquals(1, getEmails(contributorUsername2 + "@victim.com", "Inbox").size());
        assertEquals(1, getEmails(contributorUsername3 + "@victim.com", "Inbox").size());
        assertEquals(1, getEmails(contributorUsername4 + "@victim.com", "Inbox").size());

        getEmails(contributorUsername1 + "@victim.com", "Inbox").get(0).getContent().contains("Dear Testy Test1, Your donation of £100 has now been received. You will be upgraded to full membership as soon as we have reconciled the payment. Note there can be a lag of 1-2 days before this occurs so please be patient. Thank you, LCAG Membership Team");
        getEmails(contributorUsername1 + "@victim.com", "Inbox").get(0).getContent().contains("Your payment has been received");

        getEmails(contributorUsername2 + "@victim.com", "Inbox").get(0).getContent().contains("Dear Testy Test2, Your donation of £100 has now been received. You will be upgraded to full membership as soon as we have reconciled the payment. Note there can be a lag of 1-2 days before this occurs so please be patient. Thank you, LCAG Membership Team");
        getEmails(contributorUsername2 + "@victim.com", "Inbox").get(0).getContent().contains("Your payment has been received");

        getEmails(contributorUsername3 + "@victim.com", "Inbox").get(0).getContent().contains("Dear Testy Test3, Your donation of £50 has now been received. You will be upgraded to full membership as soon as we have reconciled the payment. Note there can be a lag of 1-2 days before this occurs so please be patient. Thank you, LCAG Membership Team");
        getEmails(contributorUsername3 + "@victim.com", "Inbox").get(0).getContent().contains("Your payment has been received");

        getEmails(contributorUsername4 + "@victim.com", "Inbox").get(0).getContent().contains("Dear Testy Test4, Your donation of £100 has now been received. You will be upgraded to full membership as soon as we have reconciled the payment. Note there can be a lag of 1-2 days before this occurs so please be patient. Thank you, LCAG Membership Team");
        getEmails(contributorUsername4 + "@victim.com", "Inbox").get(0).getContent().contains("Your payment has been received");

        assertEquals(4, getBankTransactionRows().size());
    }

    @Test
    public void doesNotImportMissingBankTransactionsWhenLatestBankTransactionIsEarlierThanTransactionsFromInFullTable() throws InterruptedException {
        assertTrue(getBankTransactionRows().isEmpty());

        runSqlUpdate(format("INSERT INTO `i7b0_bank_transactions` (`id`, `user_id`, `date`, `description`, `amount`, `running_balance`, `counter_party`, `reference`, `transaction_index_on_day`, `payment_source`, `email_address`, `email_sent`)\n" +
                        "VALUES\n" +
                        "\t(1, 1, 1550927661, 'FASTER PAYMENTS RECEIPT REF.contributor1155092 FROM TEST1 S P ', 50.00, 69178.86, 'TEST1 S P ', 'contributor1155092', 0, 'SANTANDER', '', 1);\n")
        );

        runSqlUpdate(format("INSERT INTO `i7b0_bank_transactions_infull` (`id`, `moneyIn`, `rollingBalance`, `description`, `date`)\n" +
                        "VALUES\n" +
                        "\t(588, 50.00, 69178.86, 'FASTER PAYMENTS RECEIPT REF.%s FROM TEST1 S P ', '2019-02-23 00:00:00'),\n" +
                        "\t(603, 50.00, 68328.86, 'FASTER PAYMENTS RECEIPT REF.%s FROM TEST2 ', '2019-02-23 00:00:00'),\n" +
                        "\t(606, 100.00, 68178.86, 'FASTER PAYMENTS RECEIPT REF.%s FROM TEST3 ', '2019-02-23 00:00:00'),\n" +
                        "\t(608, 100.00, 67978.86, 'FASTER PAYMENTS RECEIPT REF.%s FROM TEST4 ', '2019-02-23 00:00:00');\n",
                contributorUsername1,
                contributorUsername2,
                contributorUsername3,
                contributorUsername4)
        );

        sleep(5000);

        assertEquals(0, getEmails(contributorUsername1 + "@victim.com", "Inbox").size());
        assertEquals(0, getEmails(contributorUsername2 + "@victim.com", "Inbox").size());
        assertEquals(0, getEmails(contributorUsername3 + "@victim.com", "Inbox").size());
        assertEquals(0, getEmails(contributorUsername4 + "@victim.com", "Inbox").size());

        assertEquals(1, getBankTransactionRows().size());
    }


}
