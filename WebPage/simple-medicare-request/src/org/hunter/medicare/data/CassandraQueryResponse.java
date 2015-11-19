package org.hunter.medicare.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



//import org.apache.log4j.BasicConfigurator;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class CassandraQueryResponse {
    // Set to true for mock data, false if you want to connect to Cassandra
    private static boolean mock = false;

    private static String host = "52.32.209.104"; // Tim's
    // private static String host = "127.0.0.1"; // If mock=false and run local
    // private static String host = "54.200.138.99"; // mock=false and EC2 brian
    // private static String host = "54.191.107.167"; // ec2 josh
    private static String keyspace = "demo";
    private static String keyspaceMain = "main";
    private static String mvTable = "mv";
    private static String USERNAME = "cassandra";
    private static String PASSWORD = "cassandra";

    private static CassandraQueryResponse instance = null;

    public static final String STRINGIFIER = "x";
    public static final String LEAST = "least";
    public static final String MOST = "most";

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
    public static final String WILDCARD = "*";
    public static final String FROM = "from";
    public static final String WHERE = "where";
    public static final String STATE = "nppes_provider_state";
    public static final String EQUALS = "=";
    public static final String CREATE_TABLE = "create table";
    public static final String LIMIT = "limit";

    public static CassandraQueryResponse getInstance() {
	if (instance == null) {
	    instance = new CassandraQueryResponse();
	}
	return instance;
    }

    public static List<Provider> getMostExpensive(Integer numRows, String state, String procedure) throws Exception {
	List<Provider> providers = new ArrayList<Provider>();

	if (mock) {
	    System.err.println("Warning: Cassandra mock flag is ON");

	    providers = buildMockResponse(state, procedure);
	    /*
	     * assign values from 1 to n to proviser's charge. need this for
	     * mocking.
	     */
	    int charge = 1;
	    for (Provider p : providers) {
		p.providerDetails = p.new ExtendedInfo();
		p.providerDetails.averageSubmittedChargeAmount = charge;
		charge++;
	    }
	} else {
	    List<String> ids = getProviders(state, procedure, MOST, numRows);
	    for (String id : ids) {
		Provider p = getProviderById(id);
		providers.add(p);
	    }
	}
	return providers;
    }

    public static ArrayList<String> getProviders(String state, String code, String order, int limit) {

	// incremented until $limit or EOF
	int numberOfInstances = 0;
	ArrayList<String> orderedIds = new ArrayList<String>();

	Cluster cluster = null;
	Session session = null;

	try {
	    cluster = Cluster.builder().addContactPoint(host).build();
	    session = cluster.connect(keyspace);

	    String selectCostsByStateQuery = SELECT + SPACE + WILDCARD + SPACE + FROM + SPACE + mvTable + SPACE + WHERE
		    + SPACE + STATE + SPACE + EQUALS + SPACE + OPEN_STRING + state + CLOSE_STRING;
	    ResultSet resultSet = session.execute(selectCostsByStateQuery);
	    Row row;
	    if ((row = resultSet.one()) != null) {

		ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
		List<Definition> columns = columnDefinitions.asList();

		if (order.equals(LEAST)) {
		    // columnsIndex starts at 1 because 0 is the index for
		    // 'nppes_provider_state'
		    for (int columnsIndex = 1; columnsIndex < columns.size(); columnsIndex++) {
			Definition column = columns.get(columnsIndex);
			String columnName = column.getName();
			String ids = row.getString(columnName);
			if (ids != null) {
			    String[] idsArray = ids.split(",");
			    for (int idsIndex = 0; idsIndex < idsArray.length; idsIndex++) {
				String id = idsArray[idsIndex];
				int idLength = id.length();
				// start at 11 because 10 for npi, 1 for
				// office/faculty. subtract 4 to peel the year.
				String idCode = id.substring(11, idLength - 4);
				if (idCode.equals(code) && numberOfInstances < limit) {
				    orderedIds.add(id);
				    numberOfInstances++;
				}
			    }
			}
		    }
		} else if (order.equals(MOST)) {
		    // columnsIndex ends at 1 because 0 is the index for
		    // 'nppes_provider_state'
		    for (int columnsIndex = columns.size() - 1; columnsIndex > 0; columnsIndex--) {
			Definition column = columns.get(columnsIndex);
			String columnName = column.getName();
			String ids = row.getString(columnName);
			if (ids != null) {
			    String[] idsArray = ids.split(",");
			    for (int idsIndex = 0; idsIndex < idsArray.length; idsIndex++) {
				String id = idsArray[idsIndex];
				int idLength = id.length();
				// start at 11 because 10 for npi, 1 for
				// place_of_service
				// subtract 4 from the end to peel the year.
				String idCode = id.substring(11, idLength - 4);
				if (idCode.equals(code) && numberOfInstances < limit) {
				    orderedIds.add(id);
				    numberOfInstances++;
				}
			    }
			}
		    }
		}
	    }
	} catch (Exception e) {
	    // TODO seperate out exceptions
	    System.out.println("An error occured:  " + e);
            e.printStackTrace();
            throw e;
	} finally {
	    if (session != null) {
		session.close();
	    }
	    if (cluster != null) {
		cluster.close();
	    }
	    System.out.println("session closed");
	}

	return orderedIds;
    }

    public static Double getAverage(String state, String code) {

	Double average = 0.0;
	Cluster cluster = null;
	Session session = null;

	try {
	    cluster = Cluster.builder().addContactPoint(host).build();
	    session = cluster.connect(keyspace);

	    String selectCostsByStateQuery = SELECT + SPACE + WILDCARD + SPACE + FROM + SPACE + mvTable + SPACE + WHERE
		    + SPACE + STATE + SPACE + EQUALS + SPACE + OPEN_STRING + state + CLOSE_STRING;
	    ResultSet resultSet = session.execute(selectCostsByStateQuery);
	    Row row;
	    Double sumOfCosts = 0.0;
	    Double numberOfInstances = 0.0;

	    if ((row = resultSet.one()) != null) {
		ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
		List<Definition> columns = columnDefinitions.asList();

		// columnsIndex starts at 1 because 0 is the index for
		// 'nppes_provider_state'
		for (int columnsIndex = 1; columnsIndex < columns.size(); columnsIndex++) {
		    Definition column = columns.get(columnsIndex);
		    String columnName = column.getName();
		    String ids = row.getString(columnName);
		    if (ids != null) {
			String[] idsArray = ids.split(",");
			for (int idsIndex = 0; idsIndex < idsArray.length; idsIndex++) {
			    String id = idsArray[idsIndex];
			    int idLength = id.length();
			    // start at 11 because 10 for npi, 1 for
			    // office/faculty.
			    // subtract 4 to peel the year.
			    String idCode = id.substring(11, idLength - 4);
			    if (idCode.equals(code)) {
				double cost = Double.parseDouble(columnName.substring(1));
				sumOfCosts += cost;
				numberOfInstances++;
			    }
			}
		    }
		}

		if (numberOfInstances == 0.0) {
		    return -1.0;
		}
		average = sumOfCosts / numberOfInstances;

	    }
	} catch (Exception e) {
	    // TODO seperate out exceptions
	    System.out.println("An error occured:  " + e);
            e.printStackTrace();
            throw e;
	} finally {
	    if (session != null) {
		session.close();
	    }
	    if (cluster != null) {
		cluster.close();
	    }
	    System.out.println("session closed");
	}

	return average;
    }

    public static HashMap<String, Double> getCodeToAvgCostMappingForState(Set<String> codes, String state) {
	HashMap<String, Double> codeToAvgCostMappingForState = new HashMap<String, Double>();
	Iterator<String> codesIterator = codes.iterator();
	while (codesIterator.hasNext()) {
	    String code = codesIterator.next();
	    Double avgCost = getAverage(state, code);
	    codeToAvgCostMappingForState.put(code, avgCost);
	}

	return codeToAvgCostMappingForState;
    }

    /**
     * Returns the Provider when given the corresponding Id
     * 
     * @param id
     * @return Provider, if no matching ID or bad connection, will return null
     */
    // Josh's query will return a list of IDs and will call this method for
    // each ID to get the Provider
    // Josh needs to package these into a List for Jin
    // TODO Note: This function returns null if a Provider is not found that
    // matches the id
    // or if the connection is bad
    public static Provider getProviderById(String id) {
	// TODO Logger
	// uncomment this to use logger
	// BasicConfigurator.configure();

	Provider provider = null;

	// connect to server
	Cluster cluster;
	Session session = null;
	cluster = Cluster.builder().addContactPoint(host).build();
	// pick KEYSPACE
	try {
	    session = cluster.connect(keyspace);
	    System.out.println("connecting to cassandra");
	    // compose query
	    String query1 = "SELECT * FROM proceduresstats" + " WHERE id = '" + id + "';";

	    ResultSet procedureResult = session.execute(query1);
	    Row procedureRow = procedureResult.one();
	    if (procedureRow == null) {
		System.out.println("failed to return a result");
	    }

	    String npi = procedureRow.getString("npi");
	    String hcpcs_code = procedureRow.getString("hcpcs_code");

	    String query2 = "SELECT * FROM proceduresinfo" + " WHERE hcpcs_code = '" + hcpcs_code + "';";

	    ResultSet procedureInfoResult = session.execute(query2);
	    Row procedureInfoRow = procedureInfoResult.one();

	    String query3 = "SELECT * FROM providers " + " WHERE npi = '" + npi + "';";

	    ResultSet providerResult = session.execute(query3);
	    Row providerRow = providerResult.one();

	    provider = new Provider(providerRow, procedureRow, procedureInfoRow);

	} catch (Exception e) {
	    // TODO seperate out exceptions
	    System.out.println("An error occured:  " + e);
            e.printStackTrace();
            throw e;
	} finally {
	    if (session != null) {
		session.close();
	    }
	    if (cluster != null) {
		cluster.close();
	    }
	    System.out.println("session closed");
	}
	return provider;
    }

    /**
     * NEW USE CASE #2 Provides the Top N treatments that have the
     * largest/smallest gap between cost of treatment and what medicare pays
     * 
     * @param largest
     *            true to return in descending order from largest to smallest
     * @param numReturn
     *            the top N to return
     * @return
     */
    public static List<ProcedureDetails> getChargedMedicarePayGap(boolean largest, int numReturn) {
	return getChargedMedicarePayGap(largest, 0, numReturn);
    }

    public static List<ProcedureDetails> getChargedMedicarePayGap(boolean largest, int start, int numReturn) {

	if (start < 0) {
	    start = 0; // Fix start if it's negative
	}
	if (numReturn == 0) {
	    return new ArrayList<ProcedureDetails>();
	}

	if (mock) {
	    System.err.println("Warning: Cassandra mock flag is ON");

	    List<ProcedureDetails> lp = new ArrayList<ProcedureDetails>();
	    ProcedureDetails p1 = new CassandraProcedure();
	    p1.procCode = "22525";
	    p1.desc = "Injection of bone cement body of middle or lower spine bone";
	    p1.drugIndicator = false;
	    p1.submittedChrg = (float) 12744.692;
	    p1.medicarePay = (float) 4106.93;
	    p1.payGap = (float) 8637.762;

	    ProcedureDetails p2 = new CassandraProcedure();
	    p2.procCode = "22524";
	    p2.desc = "Injection of bone cement into cavity of body of lower spind bone";
	    p2.drugIndicator = false;
	    p2.submittedChrg = (float) 11605.895;
	    p2.medicarePay = (float) 5698.3804;
	    p2.payGap = (float) 5907.514;

	    ProcedureDetails p3 = new CassandraProcedure();
	    p3.procCode = "52648";
	    p3.desc = "Laser vaporization of prostate including control of bleeding";
	    p3.drugIndicator = false;
	    p3.submittedChrg = (float) 5000;
	    p3.medicarePay = (float) 1444.0548;
	    p3.payGap = (float) 3555.9453;
	    lp.add(p1);
	    lp.add(p2);
	    lp.add(p3);
	    return lp;

	} else {
	    Cluster cluster = null;
	    Session session = null;
	    List<ProcedureDetails> procedureList = new ArrayList<ProcedureDetails>();
	    try {
		cluster = Cluster.builder().addContactPoint(host).withCredentials(USERNAME, PASSWORD).build();
		session = cluster.connect(keyspaceMain);

		String ordering = "ASC";
		if (largest) {
		    ordering = "DESC";
		}
		String query = "SELECT * FROM mv_charged_medicare_payment_gap "
			+ "WHERE mv_id = 2 ORDER BY charge_medicare_pay_gap " + ordering + " LIMIT " + numReturn + ";";

		ResultSet result = session.execute(query);

		int i = -1;
		for (Row row : result) {
		    // TODO: Check on this - trying to get the right slice
		    i++;
		    if (i < start) {
			continue; // keep looping
		    }

		    // Add this row to the slice we will return
		    ProcedureDetails procedure = new CassandraProcedure(row);
		    procedureList.add(procedure);

		    if (i >= start + numReturn - 1) {
			break;
		    }
		}
	    } catch (Exception e) {
		System.out.println("An error occured " + e);
                e.printStackTrace();
                throw e;
	    } finally {
		if (session != null) {
		    session.close();
		}
		if (cluster != null) {
		    cluster.close();
		}
		System.out.println("session closed");
	    }
	    return procedureList;
	}
    }

    /**
     * NEW USE CASE #3 Provide the Top N treatments that have the
     * largest/smallest gap between cost of treatment and what medicare pays in
     * terms of percentage (the percent the patient is responsible for)
     * 
     * @param largest
     *            when true results are in descending order from biggest gap
     *            percentage to least, when false it is in ascending order
     * @param numReturn
     *            the top N returns
     * @return
     */
    public static List<ProcedureDetails> getPatientResponsibility(boolean largest, int numReturn) {
	return getPatientResponsibility(largest, 0, numReturn);
    }

    public static List<ProcedureDetails> getPatientResponsibility(boolean largest, int start, int numReturn) {
	if (start < 0) {
	    start = 0; // Fix start if it's negative
	}
	if (numReturn == 0) {
	    return new ArrayList<ProcedureDetails>();
	}

	if (mock) {
	    System.err.println("Warning: Cassandra mock flag is ON");

	    List<ProcedureDetails> lp = new ArrayList<ProcedureDetails>();
	    ProcedureDetails p1 = new CassandraProcedure();
	    p1.procCode = "90853";
	    p1.desc = "Group psychotherapy";
	    p1.drugIndicator = false;
	    p1.allowedAmt = (float) 33.45;
	    p1.medicarePay = (float) 7.8686666;
	    p1.patientResponsibility = (float) 0.7653945;

	    ProcedureDetails p2 = new CassandraProcedure();
	    p2.procCode = "90804";
	    p2.desc = "Individual office or outpatient psychotherapy, approximately 20 to 30 minutes";
	    p2.drugIndicator = false;
	    p2.allowedAmt = (float) 53.1975;
	    p2.medicarePay = (float) 22.3465;
	    p2.patientResponsibility = (float) 0.46279326;

	    ProcedureDetails p3 = new CassandraProcedure();
	    p3.procCode = "90806";
	    p3.desc = "Individual office or outpatient psychotherapy, approximately 45 to 50 minutes";
	    p3.drugIndicator = false;
	    p3.allowedAmt = (float) 85.06606;
	    p3.medicarePay = (float) 45.82522;
	    p3.patientResponsibility = (float) 0.46279326;

	    lp.add(p1);
	    lp.add(p2);
	    lp.add(p3);
	    return lp;

	} else {
	    Cluster cluster = null;
	    Session session = null;
	    List<ProcedureDetails> procedureList = new ArrayList<ProcedureDetails>();
	    try {
		cluster = Cluster.builder().addContactPoint(host).withCredentials(USERNAME, PASSWORD).build();
		session = cluster.connect(keyspaceMain);

		String ordering = "ASC";
		if (largest) {
		    ordering = "DESC";
		}
		String query = "SELECT * FROM mv_patient_responsibility "
			+ "WHERE mv_id = 1 ORDER BY fraction_responsible " + ordering + " LIMIT " + numReturn + ";";

		ResultSet result = session.execute(query);
		int i = -1;
		for (Row row : result) {
		    // TODO: Check on this - trying to get the right slice
		    i++;
		    if (i < start) {
			continue; // keep looping
		    }

		    // Add this row to the slice we will return
		    ProcedureDetails procedure = new CassandraProcedure(row);
		    procedureList.add(procedure);

		    if (i >= start + numReturn - 1) {
			break;
		    }
		}
	    } catch (Exception e) {
		System.out.println("An error occured " + e);
                e.printStackTrace();
	        throw e;
	    } finally {
		if (session != null) {
		    session.close();
		}
		if (cluster != null) {
		    cluster.close();
		}
		System.out.println("session closed");
	    }
	    return procedureList;
	}

    }

    /**
     * used for integration phase. returns mock data.
     * 
     * @param state
     * @param procedure
     * @return
     * @throws Exception
     */
    private static List<Provider> buildMockResponse(String state, String procedure) throws Exception {
	List<Provider> providers = new ArrayList<Provider>();

	try {
	    // always mock in this way.
	    providers = SolrProviderSource.getProviders(10, state, procedure);
	} catch (Exception e) {
	    e.printStackTrace();
	    throw e;
	}

	return providers;
    }

    // for testing
    public static void main(String[] args) {
	ArrayList<String> x = getProviders("CA", "99223", LEAST, 10);
	System.out.println(x);
	getProviderById(x.get(0));

	HashSet<String> asdf = new HashSet<String>();
	asdf.add("99238");
	asdf.add("99204");
	asdf.add("99223");
	System.out.println(getCodeToAvgCostMappingForState(asdf, "CA"));

    }

}