package fortesting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.hunter.medicare.test.GetLastRow;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * Called by RunTransformTim or from commandline to load data into tables
 * (Assumes scheme already created)
 * 
 * Loads the given CSV into the given Cassandra database arg0 = host arg1 =
 * keyspace arg2 = csv file
 */
public class TransformTim {

    // column indices from file;
    public static final int NPI = 0;
    public static final int NPPES_PROVIDER_LAST_ORG_NAME = 1;
    public static final int NPPES_PROVIDER_FIRST_NAME = 2;
    public static final int NPPES_PROVIDER_MI = 3;
    public static final int NPPES_CREDENTIALS = 4;
    public static final int NPPES_PROVIDER_GENDER = 5;
    public static final int NPPES_ENTITY_CODE = 6;
    public static final int NPPES_PROVIDER_STREET1 = 7;
    public static final int NPPES_PROVIDER_STREET2 = 8;
    public static final int NPPES_PROVIDER_CITY = 9;
    public static final int NPPES_PROVIDER_ZIP = 10;
    public static final int NPPES_PROVIDER_STATE = 11;
    public static final int NPPES_PROVIDER_COUNTRY = 12;
    public static final int PROVIDER_TYPE = 13;
    public static final int MEDICARE_PARTICIPATION_INDICATOR = 14;
    public static final int PLACE_OF_SERVICE = 15;
    public static final int HCPCS_CODE = 16;
    public static final int HCPCS_DESCRIPTION = 17;
    public static final int HCPCS_DRUG_INDICATOR = 18;
    public static final int LINE_SRVC_CNT = 19;
    public static final int BENE_UNIQUE_CNT = 20;
    public static final int BENE_DAY_SRVC_CNT = 21;
    public static final int AVERAGE_MEDICARE_ALLOWED_AMT = 22;
    public static final int STDEV_MEDICARE_ALLOWED_AMT = 23;
    public static final int AVERAGE_SUBMITTED_CHRG_AMT = 24;
    public static final int STDEV_SUBMITTED_CHRG_AMT = 25;
    public static final int AVERAGE_MEDICARE_PAYMENT_AMT = 26;
    public static final int STDEV_MEDICARE_PAYMENT_AMT = 27;

    // constants for querying the 3 different tables
    public static final String SPACE = " ";
    public static final String COMMA = ",";
    public static final String OPEN_STRING = "'";
    public static final String CLOSE_STRING = "'";
    public static final String LEFT = "(";
    public static final String RIGHT = ")";
    public static final String INSERT_INTO = "INSERT INTO";
    public static final String QUERY_PROVIDERS = "providers ( npi, nppes_provider_last_org_name, nppes_provider_first_name, nppes_provider_mi, nppes_credentials, nppes_provider_gender, nppes_entity_code, nppes_provider_street1, nppes_provider_street2, nppes_provider_city, nppes_provider_zip, nppes_provider_state, nppes_provider_country, provider_type, medicare_participation_indicator )";
    public static final String QUERY_PROCEDURES_INFO = "proceduresInfo ( hcpcs_code, hcpcs_description, hcpcs_drug_indicator )";
    public static final String QUERY_PROCEDURES_STATS = "proceduresStats ( id, npi, place_of_service, hcpcs_code, year, line_srvc_cnt, bene_unique_cnt, bene_day_srvc_cnt, average_Medicare_allowed_amt, stdev_Medicare_allowed_amt, average_submitted_chrg_amt, stdev_submitted_chrg_amt, average_Medicare_payment_amt, stdev_Medicare_payment_amt )";
    public static final String VALUES = "VALUES";

    // flag denoting whether the 2nd row is the medicare copyright statement.
    public static final boolean COPYRIGHT_STATEMENT_IN_FILE = false;

    /**
     * arg0 : ip address arg1 : keyspace arg2 : csv file script to ingest csv
     * data into a cassandra db
     */
    public static void main(String[] args) throws IOException {
        String host = args[0];
        String keyspace = args[1];
        String file = args[2];
        String year = getYearFromFile(file);
        String start = "";
        injest(host, keyspace, file, year, "");

        // host = "127.0.0.1"
        // keyspace = "new"

    }

    public static void injest(String host, String keyspace, String file, String year, String start)
            throws IOException {

        Cluster cluster;
        Session session;
        cluster = Cluster.builder().addContactPoint(host)
                // .withCredentials("cassandra", "cassandra")
                .build();
        session = cluster.connect(keyspace);
        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            System.out.println("Injesting " + file);

            String createProviders = "CREATE TABLE IF NOT EXISTS providers ( npi text PRIMARY KEY, nppes_provider_last_org_name text, nppes_provider_first_name text, nppes_provider_mi text, nppes_credentials text, nppes_provider_gender text, nppes_entity_code text, nppes_provider_street1 text, nppes_provider_street2 text, nppes_provider_city text, nppes_provider_zip text, nppes_provider_state text, nppes_provider_country text, provider_type text, medicare_participation_indicator text );";
            String createProceduresInfo = "CREATE TABLE IF NOT EXISTS proceduresInfo ( hcpcs_code text PRIMARY KEY, hcpcs_description text, hcpcs_drug_indicator text );";
            String createProceduresStats = "CREATE TABLE IF NOT EXISTS proceduresStats ( id text PRIMARY KEY, npi text, place_of_service text, hcpcs_code text, year int, line_srvc_cnt float, bene_unique_cnt float, bene_day_srvc_cnt float, average_Medicare_allowed_amt float, stdev_Medicare_allowed_amt float, average_submitted_chrg_amt float, stdev_submitted_chrg_amt float, average_Medicare_payment_amt float, stdev_Medicare_payment_amt float );";

            session.execute(createProviders);
            session.execute(createProceduresInfo);
            session.execute(createProceduresStats);

            // read in all of the information

            String line = null;

            // remove the field names
            br.readLine();

            // remove the copyright statement if it exists
            if (COPYRIGHT_STATEMENT_IN_FILE) {
                br.readLine();
            }

            int count = 0;
            // iterate over the data set and store into cassandra
            boolean startAdding = false;
            while ((line = br.readLine()) != null) {
                // remove $ signs
                line = line.replace("$", "");

                // splits only on commas that are not inside of a string
                String[] row = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                if (row[NPI].equals(start) || start.equals("")) {
                    startAdding = true;
                }

                if (startAdding) {

                    // clean up columns

                    // escape apostrophies
                    row[NPPES_PROVIDER_LAST_ORG_NAME] = row[NPPES_PROVIDER_LAST_ORG_NAME]
                            .replace("'", "''");
                    row[NPPES_PROVIDER_FIRST_NAME] = row[NPPES_PROVIDER_FIRST_NAME].replace("'",
                            "''");
                    if (row[NPPES_PROVIDER_MI].equals("'")) {
                        row[NPPES_PROVIDER_MI] = "";
                    }
                    row[NPPES_CREDENTIALS] = row[NPPES_CREDENTIALS].replace("'", ".");

                    row[NPPES_PROVIDER_STREET1] = row[NPPES_PROVIDER_STREET1].replace("'", "''");
                    row[NPPES_PROVIDER_STREET2] = row[NPPES_PROVIDER_STREET2].replace("'", "''");
                    row[NPPES_PROVIDER_CITY] = row[NPPES_PROVIDER_CITY].replace("'", "''");
                    row[HCPCS_DESCRIPTION] = row[HCPCS_DESCRIPTION].replace("'", "''");
                    // public static final int NPPES_PROVIDER_ZIP = 10;
                    // public static final int NPPES_PROVIDER_COUNTRY = 12;
                    // public static final int HCPCS_CODE = 16;

                    // remove quotes and commas from numbers
                    row[LINE_SRVC_CNT] = row[LINE_SRVC_CNT].replace("\"", "").replace(",", "")
                            .trim();
                    row[BENE_UNIQUE_CNT] = row[BENE_UNIQUE_CNT].replace("\"", "").replace(",", "")
                            .trim();
                    row[BENE_DAY_SRVC_CNT] = row[BENE_DAY_SRVC_CNT].replace("\"", "")
                            .replace(",", "").trim();
                    row[AVERAGE_MEDICARE_ALLOWED_AMT] = row[AVERAGE_MEDICARE_ALLOWED_AMT]
                            .replace("\"", "").replace(",", "").trim();
                    row[STDEV_MEDICARE_ALLOWED_AMT] = row[STDEV_MEDICARE_ALLOWED_AMT]
                            .replace("\"", "").replace(",", "").trim();
                    row[AVERAGE_SUBMITTED_CHRG_AMT] = row[AVERAGE_SUBMITTED_CHRG_AMT]
                            .replace("\"", "").replace(",", "").trim();
                    row[STDEV_SUBMITTED_CHRG_AMT] = row[STDEV_SUBMITTED_CHRG_AMT].replace("\"", "")
                            .replace(",", "").trim();
                    row[AVERAGE_MEDICARE_PAYMENT_AMT] = row[AVERAGE_MEDICARE_PAYMENT_AMT]
                            .replace("\"", "").replace(",", "").trim();
                    row[STDEV_MEDICARE_PAYMENT_AMT] = row[STDEV_MEDICARE_PAYMENT_AMT]
                            .replace("\"", "").replace(",", "").trim();

                    // create PRIMARY KEY id from NPI + PLACE_OF_SERVICE +
                    // HCPS_CODE
                    // +
                    // YEAR
                    String id = row[NPI] + row[PLACE_OF_SERVICE] + row[HCPCS_CODE] + year;

                    // create providers query
                    String providersValues = OPEN_STRING + row[NPI] + CLOSE_STRING + COMMA
                            + OPEN_STRING + row[NPPES_PROVIDER_LAST_ORG_NAME] + CLOSE_STRING + COMMA
                            + OPEN_STRING + row[NPPES_PROVIDER_FIRST_NAME] + CLOSE_STRING + COMMA
                            + OPEN_STRING + row[NPPES_PROVIDER_MI] + CLOSE_STRING + COMMA
                            + OPEN_STRING + row[NPPES_CREDENTIALS] + CLOSE_STRING + COMMA
                            + OPEN_STRING + row[NPPES_PROVIDER_GENDER] + CLOSE_STRING + COMMA
                            + OPEN_STRING + row[NPPES_ENTITY_CODE] + CLOSE_STRING + COMMA
                            + OPEN_STRING + row[NPPES_PROVIDER_STREET1] + CLOSE_STRING + COMMA
                            + OPEN_STRING + row[NPPES_PROVIDER_STREET2] + CLOSE_STRING + COMMA
                            + OPEN_STRING + row[NPPES_PROVIDER_CITY] + CLOSE_STRING + COMMA
                            + OPEN_STRING + row[NPPES_PROVIDER_ZIP] + CLOSE_STRING + COMMA
                            + OPEN_STRING + row[NPPES_PROVIDER_STATE] + CLOSE_STRING + COMMA
                            + OPEN_STRING + row[NPPES_PROVIDER_COUNTRY] + CLOSE_STRING + COMMA
                            + OPEN_STRING + row[PROVIDER_TYPE] + CLOSE_STRING + COMMA + OPEN_STRING
                            + row[MEDICARE_PARTICIPATION_INDICATOR] + CLOSE_STRING;
                    String queryProviders = INSERT_INTO + SPACE + QUERY_PROVIDERS + SPACE + VALUES
                            + SPACE + LEFT + providersValues + RIGHT;

                    // create procedures info query
                    String proceduresInfoValues = OPEN_STRING + row[HCPCS_CODE] + CLOSE_STRING
                            + COMMA + OPEN_STRING + row[HCPCS_DESCRIPTION] + CLOSE_STRING + COMMA
                            + OPEN_STRING + row[HCPCS_DRUG_INDICATOR] + CLOSE_STRING;
                    String queryProceduresInfo = INSERT_INTO + SPACE + QUERY_PROCEDURES_INFO + SPACE
                            + VALUES + SPACE + LEFT + proceduresInfoValues + RIGHT;

                    // create procedures stats query
                    String proceduresStatsValues = OPEN_STRING + id + CLOSE_STRING + COMMA
                            + OPEN_STRING + row[NPI] + CLOSE_STRING + COMMA + OPEN_STRING
                            + row[PLACE_OF_SERVICE] + CLOSE_STRING + COMMA + OPEN_STRING
                            + row[HCPCS_CODE] + CLOSE_STRING + COMMA + year + COMMA
                            + row[LINE_SRVC_CNT] + COMMA + row[BENE_UNIQUE_CNT] + COMMA
                            + row[BENE_DAY_SRVC_CNT] + COMMA + row[AVERAGE_MEDICARE_ALLOWED_AMT]
                            + COMMA + row[STDEV_MEDICARE_ALLOWED_AMT] + COMMA
                            + row[AVERAGE_SUBMITTED_CHRG_AMT] + COMMA
                            + row[STDEV_SUBMITTED_CHRG_AMT] + COMMA
                            + row[AVERAGE_MEDICARE_PAYMENT_AMT] + COMMA
                            + row[STDEV_MEDICARE_PAYMENT_AMT];
                    String queryProceduresStats = INSERT_INTO + SPACE + QUERY_PROCEDURES_STATS
                            + SPACE + VALUES + SPACE + LEFT + proceduresStatsValues + RIGHT;

                    // order 66
                    session.execute(queryProviders);
                    session.execute(queryProceduresInfo);
                    session.execute(queryProceduresStats);
                    if ((count % 100) == 0) {
                        System.out.println(count + " entries generated");
                    }
                    count++;
                }
            }
        } catch (Exception e) {
            System.out.println("     !ERROR! : " + e);
            System.out.println("attemptiing to restart");
            String nextStart = GetLastRow.getLastEntry();
            injest(host, keyspace, file, year, nextStart);
        } finally {
            System.out.println("Success!");

            // close resources
            br.close();
            session.close();
            cluster.close();
        }
    }

    /**
     * Takes in a file of the form "...YEAR..." assumes there is only one
     * instance of numbers in the file: the year e.g., "asdf2012qwer.csv" bad
     * e.g., "2asdf2012.csv"
     */
    public static String getYearFromFile(String file) {
        String year = "0000"; // denotes error
        // iterate through every character to find the YEAR
        for (int i = 0; i < file.length(); i++) {
            // not very robust
            if (Character.isDigit(file.charAt(i))) {
                year = file.substring(i, i + 4);
                break;
            }
        }
        return year;
    }

}
