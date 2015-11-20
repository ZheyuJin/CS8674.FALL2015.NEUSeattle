package fortesting;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * run by MainMVGenerator, this function builds the new usecase #2 and #3 tables
 * requires providers, proceduresstats, and proceduresinfo to be fully loaded
 * 
 * it reads the fully loaded source tables from ORIGN_HOST and places the new
 * tables on DEST_HOST
 * 
 * has a bug that creates 5 bad entries. I will fix later
 * 
 * @author tim
 *
 */
// I think we need a materialized view here
// Use this code to create one
public class MVPatientResponsibility {
    private static String ORIGN_HOST = "127.0.0.1"; // If mock=false and run
                                                    // local
    // private static String DEST_HOST = "54.200.138.99"; // mock=false and EC2
    // brian
    private static String DEST_HOST = "52.32.209.104";
    // private static String HOST = "54.191.107.167"; // ec2 josh
    private static String ORIGN_KEYSPACE = "new";
    private static String DEST_KEYSPACE = "main";
    private static String USERNAME = "cassandra";
    private static String PASSWORD = "cassandra";

    public static void createPatientResponsibilityMV() {
        Cluster originCluster = null;
        Session originSession = null;
        Cluster destCluster = null;
        Session destSession = null;

        try {
            destCluster = Cluster.builder().addContactPoint(DEST_HOST)
                    // .withCredentials(USERNAME, PASSWORD)
                    .build();
            destSession = destCluster.connect(DEST_KEYSPACE);

            originCluster = Cluster.builder().addContactPoint(ORIGN_HOST)
                    // .withCredentials(USERNAME, PASSWORD)
                    .build();
            originSession = originCluster.connect(ORIGN_KEYSPACE);

            // CREATE INDEX IF NOT ALREADY EXISTING
            // String createIndex = "CREATE INDEX ON proceduresstats
            // (hcpcs_code);";
            // session.execute(createIndex);

            // DROP TABLES IF ALREADY EXIST
            // String dropPRMV = "DROP TABLE mv_patient_responsibility;";
            // session.execute(dropPRMV);
            // String dropGapMV = "DROP TABLE mv_charged_medicare_payment_gap;";
            // session.execute(dropGapMV);

            // CREATE TABLES
            // String createPRMV = "CREATE TABLE mv_patient_responsibility
            // (mv_id int, hcpcs_code text, hcpcs_description text,"
            // + " hcpcs_drug_indicator text, average_medicare_allowed_amt
            // float,"
            // + "average_medicare_payment_amt float, fraction_responsible
            // float, "
            // + " PRIMARY KEY ((mv_id), fraction_responsible, "
            // + " hcpcs_drug_indicator, hcpcs_code)) WITH CLUSTERING ORDER BY "
            // + "(fraction_responsible DESC, hcpcs_drug_indicator DESC,
            // hcpcs_code ASC);";
            // destSession.execute(createPRMV);
            //
            // String createGapMV = "CREATE TABLE "
            // + "mv_charged_medicare_payment_gap (mv_id int, hcpcs_code text,"
            // + " hcpcs_description text, hcpcs_drug_indicator text, "
            // + " average_submitted_chrg_amt float,
            // average_medicare_payment_amt"
            // + " float, charge_medicare_pay_gap float, "
            // + " PRIMARY KEY((mv_id), charge_medicare_pay_gap, "
            // + " hcpcs_drug_indicator, hcpcs_code)) WITH CLUSTERING ORDER BY "
            // + " (charge_medicare_pay_gap DESC, hcpcs_drug_indicator DESC, "
            // + " hcpcs_code ASC);";
            // destSession.execute(createGapMV);

            // GET PROCEDURE INFO
            String allProcedureQuery = "SELECT * FROM proceduresinfo;";
            ResultSet procedureResult = originSession.execute(allProcedureQuery);
            for (Row procedureRow : procedureResult) {
                String pcode = procedureRow.getString("hcpcs_code");
                String description = procedureRow.getString("hcpcs_description").replace("'", "''");
                String drugInd = procedureRow.getString("hcpcs_drug_indicator");

                // GET PROCEDURE STATISTICS
                String providersProceduresQuery = "SELECT average_medicare_allowed_amt, average_medicare_payment_amt, average_submitted_chrg_amt"
                        + " FROM proceduresstats WHERE hcpcs_code = '" + pcode + "';";
                ResultSet providersProcedures = originSession.execute(providersProceduresQuery);
                double sumOfPercents = 0;
                double sumOfAllowed = 0;
                double sumOfPayment = 0;
                double sumOfSubmitted = 0;
                double sumOfPayGap = 0;
                int rows = 0;
                for (Row statsRow : providersProcedures) {
                    float allowedAmount = statsRow.getFloat("average_medicare_allowed_amt");
                    float medicarePayment = statsRow.getFloat("average_medicare_payment_amt");
                    float submittedAmount = statsRow.getFloat("average_submitted_chrg_amt");
                    double percentPatientResponsibility = (allowedAmount - medicarePayment)
                            / allowedAmount;
                    double payGap = submittedAmount - medicarePayment;
                    sumOfPercents = sumOfPercents + percentPatientResponsibility;
                    sumOfPayGap = sumOfPayGap + payGap;
                    sumOfAllowed = sumOfAllowed + allowedAmount;
                    sumOfSubmitted = sumOfSubmitted + submittedAmount;
                    sumOfPayment = sumOfPayment + medicarePayment;
                    rows++;
                }
                double avgPercentPatientRespons = sumOfPercents / rows;
                double avgGap = sumOfPayGap / rows;
                double avgAllowed = sumOfAllowed / rows;
                double avgPayment = sumOfPayment / rows;
                double avgSubmitted = sumOfSubmitted / rows;
                // might want to change this so that years are considered
                // seperately

                // INSERT VALUES INTO TABLE
                String insertValue = "INSERT INTO mv_patient_responsibility (mv_id, hcpcs_code, hcpcs_description, hcpcs_drug_indicator, average_medicare_allowed_amt, average_medicare_payment_amt, fraction_responsible) "
                        + "VALUES (1, '" + pcode + "', '" + description + "', '" + drugInd + "', "
                        + avgAllowed + ", " + avgPayment + ", " + avgPercentPatientRespons + ");";
                destSession.execute(insertValue);

                String insertValue2 = "INSERT INTO mv_charged_medicare_payment_gap (mv_id, hcpcs_code, hcpcs_description, hcpcs_drug_indicator, average_submitted_chrg_amt, average_medicare_payment_amt, charge_medicare_pay_gap) "
                        + "VALUES (2, '" + pcode + "', '" + description + "', '" + drugInd + "', "
                        + avgSubmitted + ", " + avgPayment + ", " + avgGap + ");";
                destSession.execute(insertValue2);
            }

        } catch (Exception e) {
            System.out.println("An error occured " + e);
        } finally {
            if (originSession != null) {
                originSession.close();
            }
            if (originCluster != null) {
                originCluster.close();
            }
            if (destSession != null) {
                destSession.close();
            }
            if (destCluster != null) {
                destCluster.close();
            }
            System.out.println("session closed");
        }
    }

}
