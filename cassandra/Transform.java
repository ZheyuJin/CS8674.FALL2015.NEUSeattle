import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * Loads the given CSV into the
 * given Cassandra database
 * arg0 = host
 * arg1 = keyspace
 * arg2 = csv file
 */
public class Transform {

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
	 * arg0 : ip address
	 * arg1 : keyspace
	 * arg2 : csv file
	 * script to ingest csv data into a cassandra db
	 */
	public static void main(String[] args) throws IOException {

		String host = args[0];
		String keyspace = args[1];
		String file = args[2];
		String year = getYearFromFile(file);
		
		Cluster cluster;
		Session session;

		cluster = Cluster.builder().addContactPoint(host).build();
		session = cluster.connect(keyspace);

		// read in all of the information
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		
		// remove the field names
		br.readLine();
		
		// remove the copyright statement if it exists
		if (COPYRIGHT_STATEMENT_IN_FILE) {
			br.readLine();
		}

		// iterate over the data set and store into cassandra
		while ((line = br.readLine()) != null) { 
			// splits only on commas that are not inside of a string
			String[] row = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

			// create PRIMARY KEY id from NPI + PLACE_OF_SERVICE + HCPS_CODE + YEAR
			String id = row[NPI] + row[PLACE_OF_SERVICE] + row[HCPCS_CODE] + year; 

			// create providers query
			String providersValues = 
					OPEN_STRING + row[NPI] + CLOSE_STRING + COMMA 
					+ OPEN_STRING + row[NPPES_PROVIDER_LAST_ORG_NAME] + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[NPPES_PROVIDER_FIRST_NAME] + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[NPPES_PROVIDER_MI] + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[NPPES_CREDENTIALS] + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[NPPES_PROVIDER_GENDER] + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[NPPES_ENTITY_CODE] + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[NPPES_PROVIDER_STREET1] + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[NPPES_PROVIDER_STREET2] + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[NPPES_PROVIDER_CITY] + CLOSE_STRING + COMMA
					+ row[NPPES_PROVIDER_ZIP] + COMMA
					+ OPEN_STRING + row[NPPES_PROVIDER_STATE] + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[NPPES_PROVIDER_COUNTRY] + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[PROVIDER_TYPE] + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[MEDICARE_PARTICIPATION_INDICATOR] + CLOSE_STRING;
			String queryProviders = INSERT_INTO + SPACE + QUERY_PROVIDERS + SPACE + VALUES + SPACE + LEFT + providersValues + RIGHT;

			// create procedures info query
			String proceduresInfoValues = 
					OPEN_STRING + row[HCPCS_CODE] + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[HCPCS_DESCRIPTION] + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[HCPCS_DRUG_INDICATOR] + CLOSE_STRING;
			String queryProceduresInfo = INSERT_INTO + SPACE + QUERY_PROCEDURES_INFO + SPACE + VALUES + SPACE + LEFT + proceduresInfoValues + RIGHT;

			// create procedures stats query
			String proceduresStatsValues =
					OPEN_STRING + id + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[NPI] + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[PLACE_OF_SERVICE] + CLOSE_STRING + COMMA
					+ OPEN_STRING + row[HCPCS_CODE] + CLOSE_STRING + COMMA
					+ year + COMMA
					+ row[LINE_SRVC_CNT] + COMMA
					+ row[BENE_UNIQUE_CNT] + COMMA
					+ row[BENE_DAY_SRVC_CNT] + COMMA
					+ row[AVERAGE_MEDICARE_ALLOWED_AMT] + COMMA
					+ row[STDEV_MEDICARE_ALLOWED_AMT] + COMMA
					+ row[AVERAGE_SUBMITTED_CHRG_AMT] + COMMA
					+ row[STDEV_SUBMITTED_CHRG_AMT] + COMMA
					+ row[AVERAGE_MEDICARE_PAYMENT_AMT] + COMMA
					+ row[STDEV_MEDICARE_PAYMENT_AMT];
			String queryProceduresStats = INSERT_INTO + SPACE + QUERY_PROCEDURES_STATS + SPACE + VALUES + SPACE + LEFT + proceduresStatsValues + RIGHT;

			// order 66
			session.execute(queryProviders);
			session.execute(queryProceduresInfo);
			session.execute(queryProceduresStats);

		}
		System.out.println("Success!");
		
		// close resources
		br.close();
		session.close();
	}

	/**
	 *  Takes in a file of the form "...YEAR..."
	 *  assumes there is only one instance of numbers in the file: the year
	 *  e.g., "asdf2012qwer.csv"
	 *  bad e.g., "2asdf2012.csv"
	 */
	public static String getYearFromFile(String file) {
		String year = "0000"; // denotes error
		// iterate through every character to find the YEAR
		for (int i = 0; i < file.length(); i++) {
			// not very robust
			if (Character.isDigit(file.charAt(i))) {
				year = file.substring(i, i+4);
				break;
			}
		}
		return year;
	}


}
