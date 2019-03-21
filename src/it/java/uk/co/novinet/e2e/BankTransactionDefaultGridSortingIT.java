package uk.co.novinet.e2e;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.co.novinet.auth.MyBbPasswordEncoder;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

import static uk.co.novinet.e2e.TestUtils.*;

public class BankTransactionDefaultGridSortingIT {

    @BeforeClass
    public static void beforeClass() throws Exception {
        setupDatabaseSchema();
    }

    @Before
    public void before() {
        runSqlScript("sql/delete_all_bank_transactions.sql");
    }

    @Test
    public void transactionsOrderedByDateDescThenIndexOnDayAscWhenThereAreNoUnassignedTransactions() throws NoSuchAlgorithmException, IOException {
        insertUser(1, "test", "test@lcag.com", "test", 4, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt");

        insertBankTransaction(1, "1", "test", true, "100.00", "100", Instant.parse("2018-01-01T00:00:00Z"), 0);
        insertBankTransaction(2, "1", "test", true, "100.00", "200", Instant.parse("2018-01-01T01:00:00Z"), 1);
        insertBankTransaction(3, "1", "test", true, "100.00", "300", Instant.parse("2018-01-01T02:00:00Z"), 2);
        insertBankTransaction(4, "1", "test", true, "100.00", "400", Instant.parse("2018-01-01T03:00:00Z"), 3);
        insertBankTransaction(5, "1", "test", true, "100.00", "500", Instant.parse("2018-01-02T00:00:00Z"), 0);
        insertBankTransaction(6, "1", "test", true, "100.00", "600", Instant.parse("2018-01-02T05:00:00Z"), 1);
        insertBankTransaction(7, "1", "test", true, "100.00", "700", Instant.parse("2018-01-03T13:00:00Z"), 0);

        System.out.println(getRequest("http://localhost:8282/payment", "admin", "lcag"));
    }

}
