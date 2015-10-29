package org.hunter.medicare.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.hunter.medicare.data.SolrProviderSource;

//import org.apache.log4j.BasicConfigurator;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.ColumnDefinitions.Definition;

public class CassandraQueryResponse {
    // Set to true for mock data, false if you want to connect to Cassandra
    private boolean mock = true;

    //private static String host = "127.0.0.1";  // If mock=false and run local
    private static String host = "54.200.138.99"; // mock=false and EC2
    private static String keyspace = "demo";
    private static String mvTable = "mv";

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

    public List<Provider> getMostExpensive(Integer numRows, String state, String procedure) {
        List<Provider> providers = new ArrayList<Provider>();

        if (mock) {
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
            // FIXME
            // CALL Josh's function here
            List<String> ids = getProviders(state, procedure, MOST, numRows);
            for (String id : ids) {
                Provider p = getProviderById(id);
                providers.add(p);
            }
        }
        return providers;
    }

    public static ArrayList<String> getProviders(String state, String code,
            String order, int limit) {

        // incremented until $limit or EOF
        int numberOfInstances = 0;
        ArrayList<String> orderedIds = new ArrayList<String>();

        Cluster cluster = null;
        Session session = null;

        try {
            cluster = Cluster.builder().addContactPoint(host).build();
            session = cluster.connect(keyspace);

            String selectCostsByStateQuery = SELECT + SPACE + WILDCARD + SPACE
                    + FROM + SPACE + mvTable + SPACE + WHERE + SPACE + STATE
                    + SPACE + EQUALS + SPACE + OPEN_STRING + state
                    + CLOSE_STRING;
            ResultSet resultSet = session.execute(selectCostsByStateQuery);
            Row row;
            if ((row = resultSet.one()) != null) {
                
                ColumnDefinitions columnDefinitions = row
                        .getColumnDefinitions();
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
                                if (idCode.equals(code)
                                        && numberOfInstances < limit) {
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
                                if (idCode.equals(code)
                                        && numberOfInstances < limit) {
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

            String selectCostsByStateQuery = SELECT + SPACE + WILDCARD + SPACE
                    + FROM + SPACE + mvTable + SPACE + WHERE + SPACE + STATE
                    + SPACE + EQUALS + SPACE + OPEN_STRING + state
                    + CLOSE_STRING;
            ResultSet resultSet = session.execute(selectCostsByStateQuery);
            Row row;
            Double sumOfCosts = 0.0;
            Double numberOfInstances = 0.0;

            if ((row = resultSet.one()) != null) {
                ColumnDefinitions columnDefinitions = row
                        .getColumnDefinitions();
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
                                double cost = Double.parseDouble(columnName
                                        .substring(1));
                                sumOfCosts += cost;
                                numberOfInstances++;
                            }
                        }
                    }
                }

                average = sumOfCosts / numberOfInstances;
            }
        } catch (Exception e) {
            // TODO seperate out exceptions
            System.out.println("An error occured:  " + e);
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

    public static HashMap<String, Double> getCodeToAvgCostMappingForState(
            HashSet<String> codes, String state) {
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
    public Provider getProviderById(String id) {
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
            String query1 = "SELECT * FROM proceduresstats" + " WHERE id = '"
                    + id + "';";

            ResultSet procedureResult = session.execute(query1);
            Row procedureRow = procedureResult.one();
            if (procedureRow == null) {
                System.out.println("failed to return a result");
            }

            String npi = procedureRow.getString("npi");
            String hcpcs_code = procedureRow.getString("hcpcs_code");

            String query2 = "SELECT * FROM proceduresinfo"
                    + " WHERE hcpcs_code = '" + hcpcs_code + "';";

            ResultSet procedureInfoResult = session.execute(query2);
            Row procedureInfoRow = procedureInfoResult.one();

            String query3 = "SELECT * FROM providers " + " WHERE npi = '" + npi
                    + "';";

            ResultSet providerResult = session.execute(query3);
            Row providerRow = providerResult.one();

            provider = new Provider(providerRow, procedureRow, procedureInfoRow);

        } catch (Exception e) {
            // TODO seperate out exceptions
            System.out.println("An error occured:  " + e);
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
     * used for integration phase. returns mock data.
     * 
     * @param state
     * @param procedure
     * @return
     * @throws Exception
     */
    private List<Provider> buildMockResponse(String state, String procedure) {
        List<Provider> providers = new ArrayList<Provider>();

        try {
            // always mock in this way.
            providers = SolrProviderSource.getProviders(10, state, procedure);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return providers;
    }

    // for testing
    public static void main(String[] args) {
     ArrayList<String> x = getProviders("CA", "99223", LEAST, 10);
     System.out.println(x);

     HashSet<String> asdf = new HashSet<String>();
     asdf.add("99238");
     asdf.add("99204");
     asdf.add("99223");
     System.out.println(getCodeToAvgCostMappingForState(asdf, "CA"));

    }

}