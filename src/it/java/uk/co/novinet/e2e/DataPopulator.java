package uk.co.novinet.e2e;

import uk.co.novinet.auth.MyBbPasswordEncoder;

import static java.lang.Thread.sleep;
import static uk.co.novinet.e2e.TestUtils.insertUser;
import static uk.co.novinet.e2e.TestUtils.runSqlScript;

public class DataPopulator {

    public static void main(String[] args) throws Exception {
        int sqlRetryCounter = 0;
        boolean needToRetry = true;

        while (needToRetry && sqlRetryCounter < 20) {
            try {
                String [] mySqls = {
                    "sql/drop_user_table.sql",
                    "sql/drop_enquiry_table.sql",
                    "sql/create_user_table.sql",
                    "sql/create_audit_events_table.sql",
                    "sql/create_userFundingSummary_table.sql",
                    "sql/create_mp_details_table.sql",
                    "sql/create_mpDetails_table.sql",
                    "sql/create_mpCampaignVolunteers_table.sql",
                    "sql/create_mpCampaignUsers_table.sql",
                    "sql/create_ffc_contributions_table.sql",
                    "sql/create_enquiry_table.sql",
                    "sql/create_usergroups_table.sql",
                    "sql/populate_usergroups_table.sql",
                    "sql/create_bank_transaction_table.sql",
                    "sql/create_bank_transaction_infull_table.sql",
                    "sql/populate_bank_transaction_table.sql"
                };
                for( String sql: mySqls) {
                    System.out.println( "Running " + sql);
                    runSqlScript(sql);
                }
                needToRetry = false;
            } catch (Exception e) {
                e.printStackTrace();
                sqlRetryCounter++;
                sleep(1000);
            }
        }

        for (int i = 1; i <= 200; i++) {
            insertUser(i, "testuser" + i, "user" + i + "@something.com", "Test Name" + i, 8);
        }

        for (int i = 201; i <= 210; i++) {
            insertUser(i, "testuser" + i, "user" + i + "@something.com", "Test Name" + i, 2);
        }

        insertUser(211, "testuser" + 211, "user" + 211 + "@something.com", "Test Name" + 211, 4);

        insertUser(212, "admin", "admin@lcag.com", "Administrators", 4, true, MyBbPasswordEncoder.hashPassword("lcag", "salt"), "salt");
    }

}
