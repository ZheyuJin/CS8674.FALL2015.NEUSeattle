import java.util.HashMap;
import java.util.HashSet;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class MostExpensiveStateCodeMV {
	
	// metrics
	public static final String ID = "id";
	public static final String NPI = "npi";
	public static final String NPPES_PROVIDER_STATE = "nppes_provider_state";
	public static final String PRICE = "average_submitted_chrg_amt";
	public static final String TEN = "10";
	public static final String PROCEDURES_STATS = "proceduresStats";
	public static final String PROVIDERS = "providers";
	public static final int SERIAL_LENGTH = 10;
	public static final String STRINGIFIER = "x";
	
	// cql constants
	public static final String SPACE = " ";
	public static final String COMMA = ",";
	public static final String LEFT = "(";
	public static final String RIGHT = ")";
	public static final String OPEN_STRING = "'";
	public static final String CLOSE_STRING = "'";
	public static final String TEXT = "text";
	public static final String PRIMARY_KEY = "primary key";
	public static final String INSERT_INTO = "insert into";
	public static final String VALUES = "values";
	public static final String SELECT = "select";
	public static final String FROM = "from";
	public static final String CREATE_TABLE = "create table";
	public static final String LIMIT = "limit";
	
	public static void main(String[] args) {
		
//	String host = "127.0.0.1";
//	String keyspace = "demo";
//	String mvTable = "mv";
	
	String host = args[0];
	String keyspace = args[1];
	String mvTable = args[2];
	
	
		
		
	// store npi to state
	HashMap<String, String> npiToState = new HashMap<String, String>();
	
	HashSet<String> mvColumnNames = new HashSet<String>();
	
	HashMap<String, HashMap<String, String>> stateToPriceToId = new HashMap<String, HashMap<String, String>>();
	
	// then we iterate over the keyset to run the insert+values query for each state.
	
	Cluster cluster;
	Session session;
	cluster = Cluster.builder().addContactPoint(host).build();
	session = cluster.connect(keyspace);
	
	String npiToStateQuery = SELECT + SPACE + NPI + COMMA + NPPES_PROVIDER_STATE + SPACE + FROM + SPACE + PROVIDERS;
	ResultSet npiToStateRS = session.execute(npiToStateQuery);
	Row npiToStateRow;
	while ( (npiToStateRow = npiToStateRS.one()) != null) {
		String npi = npiToStateRow.getString(NPI);
		String state = npiToStateRow.getString(NPPES_PROVIDER_STATE);
		npiToState.put(npi, state);
	}
	
	String query = SELECT + SPACE + ID + COMMA + NPI + COMMA + PRICE + SPACE + FROM + SPACE + PROCEDURES_STATS;
	
	ResultSet statsRS = session.execute(query);
	Row statsRow = null;

	// CREATE THE MV TABLE'S COLUMNS & POPULATE HASH MAP
	// THEN USE HASH MAP TO POPULATE THE TABLE	
	String createTableQuery = CREATE_TABLE + SPACE + mvTable + SPACE + LEFT;
	
	while ( (statsRow = statsRS.one()) != null ) {
		// truncates the float into an integer (string)
		String price = String.valueOf(statsRow.getFloat(PRICE)).split("\\.")[0];
		// then serializes it so that they can be sorted by cassandra
		String serializedPrice = serializeValue(price);
		
		// adds first instance of a serialized price to the create table statement
		if (!mvColumnNames.contains(serializedPrice)) {
			mvColumnNames.add(serializedPrice);
			createTableQuery += serializedPrice + SPACE + TEXT + COMMA;
		}
		
		String npi = statsRow.getString(NPI);
		String state = npiToState.get(npi);
		String id = statsRow.getString(ID);
		
		if (stateToPriceToId.containsKey(state)) {
			if (stateToPriceToId.get(state).containsKey(serializedPrice)) {
				stateToPriceToId.get(state).put(
						serializedPrice, 
						stateToPriceToId.get(state).get(serializedPrice) + COMMA + id
				);
			} else {
				stateToPriceToId.get(state).put(
						serializedPrice,
						id
				);
			}
		} else {
			HashMap<String, String> priceToId = new HashMap<String, String>();
			priceToId.put(serializedPrice,  id);
			stateToPriceToId.put(state, priceToId);
		}
	}

	// handle the data now
	if (!mvColumnNames.isEmpty()) {
		// make the table
		createTableQuery += NPPES_PROVIDER_STATE + SPACE + TEXT + SPACE + PRIMARY_KEY + RIGHT;
		session.execute(createTableQuery);
		
		// TABLE HAS BEEN CREATED >>> POPULATE IT
		for (String state : stateToPriceToId.keySet()) {
			// build the insert and values statements, then execute their concatenation
			String insert = INSERT_INTO + SPACE + mvTable + SPACE + LEFT;
			String values = VALUES + SPACE + LEFT;
			HashMap<String, String> priceToId = stateToPriceToId.get(state);
			
			for (String price : priceToId.keySet()) {
				insert += price + COMMA;
				// values must be in single quotations because they are of type TEXT
				values += OPEN_STRING + priceToId.get(price) + CLOSE_STRING + COMMA;
			}
			
			insert += NPPES_PROVIDER_STATE + RIGHT;
			values += OPEN_STRING + state + CLOSE_STRING + RIGHT;
			String insertQuery = insert + SPACE + values;
			session.execute(insertQuery);
		}
		
		System.out.println("Success!");
		
	} else {
		// if data set is empty, print error message
		System.err.println("Data set was empty!!!");
	}
	
	// close resources
	session.close();
	
	}
	
	// Takes a value and makes sure it's of a standard length: 10 characters, first char is 'x'
	// e.g., 12345 -> x000012345
	public static String serializeValue(String value) {
		String serializedValue = STRINGIFIER;
		for (int i = value.length(); i<SERIAL_LENGTH; i++) {
			serializedValue += "0";
		}
		serializedValue += value;
		return serializedValue;
	}
}
