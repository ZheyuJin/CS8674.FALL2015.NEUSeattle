package org.hunter.medicare.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//import org.apache.log4j.BasicConfigurator;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Ordering;
import com.datastax.driver.core.querybuilder.QueryBuilder;

/**
 * 
 * Handles all Cassandra Queries
 * 
 * @author Tim, Josh, Doyle (others?)
 *
 */
public class CassandraQueryResponse {
    // Set to true for mock data, false if you want to connect to Cassandra
    private static boolean mock = false;

    private static String host = "52.32.209.104"; // EC2 Tim
    // private static String host = "127.0.0.1"; // Local
    // private static String host = "54.200.138.99"; // EC2 Brian
    // private static String host = "54.191.107.167"; // EC2 Josh
    private static String KEYSPACE = "main";
    // private static String USERNAME = "cassandra";
    // private static String PASSWORD = "cassandra";

    private static CassandraQueryResponse instance = null;

    // tables
    private static final String PATIENT_RESPONSIBILITY = "mv_patient_responsibility";
    private static final String CHARGED_MEDICARE_GAP = "mv_charged_medicare_payment_gap";
    private static final String PROVIDERS_COST_STATE = "mv_providers_cost";
    private static final String PROVIDERS_COST_NATIONAL = "mv_providers_cost_national";
    private static final String PROVIDERS = "providers";
    private static final String PROCEDURES_STATS = "proceduresstats";
    private static final String PROCEDURES_INFO = "proceduresinfo";

    /**
     * Get Instance
     * 
     * @return CassandraQueryResponse
     */
    public static CassandraQueryResponse getInstance() {
        if (instance == null) {
            instance = new CassandraQueryResponse();
        }
        return instance;
    }

    /**
     * OLD USECASE #1 Returns the N most expensive providers in terms of average
     * submitted charge for the given procedure and the given state.
     * 
     * @author Tim and others
     * 
     * @param numRows
     *            The number of Providers returned
     * @param state
     *            A US State given as the standard all-caps 2-letter
     *            abbreviation
     * @param procedure
     *            hcpcs_code
     * @return List<Provider> where each Provider only contains select fields,
     *         returns null if there are no results or if query fails, returns
     *         mock data when mock = true,
     * @throws Exception
     */
    public static List<Provider> getMostExpensive(Integer numRows, String state, String procedure)
            throws Exception {
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
            providers = getMostExpensiveByState(state, procedure, "DESC", numRows);
        }
        return providers;
    }

    /**
     * OLD USECASE #3 Returns the N most/least expensive providers in terms of
     * average submitted charge for the given procedure and the given state.
     * 
     * @param state
     *            A US State given as the standard all-caps 2-letter
     *            abbreviation
     * @param code
     *            hcpcs_code
     * @param order
     *            either "ACS" for least expensive providers or "DESC" for most
     *            expensive providers
     * @param limit
     *            The number of Providers returned
     * @return List<Providers> where each Provider only contains select fields
     */
    public static List<Provider> getMostExpensiveByState(String state, String code, String order,
            int limit) {

        List<Provider> providers = new ArrayList<Provider>();
        Cluster cluster = null;
        Session session = null;

        try {
            cluster = Cluster.builder().addContactPoint(host).build();
            session = cluster.connect(KEYSPACE);

            Ordering queryOrder = QueryBuilder.desc("average_submitted_chrg_amt");
            if (order.equals("ASC")) {
                queryOrder = QueryBuilder.asc("average_submitted_chrg_amt");
            }

            Statement selectStmt = QueryBuilder.select().all().from(PROVIDERS_COST_STATE)
                    .where(QueryBuilder.eq("hcpcs_code", code))
                    .and(QueryBuilder.eq("nppes_provider_state", state))
                    .and(QueryBuilder.eq("year", 2012)).orderBy(queryOrder).limit(limit);

            ResultSet results = session.execute(selectStmt);
            for (Row row : results) {
                Provider provider = new Provider(row);
                providers.add(provider);
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

        return providers;
    }

    /**
     * ML USECASE
     * 
     * Returns a List of Providers which have an average submitted charge amount
     * either above or below the given cost
     * 
     * @param code
     *            hcpcs_code
     * @param cost
     *            average submitted charge amount used for comparison
     * @param below
     *            when true returns the Providers that charge below cost, when
     *            false returns Providers that charge above cost
     * @return List<Provider> where Provider only contains certain relevant
     *         fields
     */
    public static List<Provider> getProvidersOverCostThreshold(String code, double cost,
            boolean below) {
        List<Provider> providers = new ArrayList<Provider>();
        Cluster cluster = null;
        Session session = null;

        try {

            cluster = Cluster.builder().addContactPoint(host).build();
            session = cluster.connect(KEYSPACE);

            Clause inequality = QueryBuilder.gt("average_submitted_chrg_amt", cost);
            if (below) {
                inequality = QueryBuilder.lt("average_submitted_chrg_amt", cost);
            }
            Statement selectStmt = QueryBuilder.select().all().from(PROVIDERS_COST_NATIONAL)
                    .where(QueryBuilder.eq("year", 2012)).and(QueryBuilder.eq("hcpcs_code", code))
                    .and(inequality);

            ResultSet results = session.execute(selectStmt);
            for (Row row : results) {
                Provider provider = new Provider(row);
                providers.add(provider);
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
        return providers;
    }

    public static Double getAverage(String state, String code) {

        Double average = 0.0;
        Cluster cluster = null;
        Session session = null;

        try {
            cluster = Cluster.builder().addContactPoint(host).build();
            session = cluster.connect(KEYSPACE);

            Statement selectStmt = QueryBuilder.select("average_submitted_chrg_amt")
                    .from(PROVIDERS_COST_STATE)
                    .where(QueryBuilder.eq("nppes_provider_state", state))
                    .and(QueryBuilder.eq("hcpcs_code", code)).and(QueryBuilder.eq("year", 2012));

            ResultSet results = session.execute(selectStmt);
            Double sumOfCosts = 0.0;
            Double numberOfInstances = 0.0;
            for (Row row : results) {
                float cost = row.getFloat("average_submitted_chrg_amt");
                sumOfCosts += cost;
                numberOfInstances++;
            }
            if (numberOfInstances == 0) {
                average = -1.0;
            } else {
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

    public static HashMap<String, Double> getCodeToAvgCostMappingForState(Set<String> codes,
            String state) {
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
     * Returns the Provider with all fields when given the corresponding Id
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
            session = cluster.connect(KEYSPACE);
            System.out.println("connecting to cassandra");
            // compose query
            Statement query1 = QueryBuilder.select().all().from(PROCEDURES_STATS)
                    .where(QueryBuilder.eq("id", id));

            System.out.println("zzz query: " + query1);
            ResultSet procedureResult = session.execute(query1);
            Row procedureRow = procedureResult.one();
            if (procedureRow == null) {
                System.out.println("failed to return a result");
            }

            String npi = procedureRow.getString("npi");
            String hcpcs_code = procedureRow.getString("hcpcs_code");

            Statement query2 = QueryBuilder.select().all().from(PROCEDURES_INFO)
                    .where(QueryBuilder.eq("hcpcs_code", hcpcs_code));

            ResultSet procedureInfoResult = session.execute(query2);
            Row procedureInfoRow = procedureInfoResult.one();

            Statement query3 = QueryBuilder.select().all().from(PROVIDERS)
                    .where(QueryBuilder.eq("npi", npi));

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
     * largest/smallest gap between cost of treatment and what Medicare pays
     * 
     * @param largest
     *            true to return in descending order from largest to smallest
     * @param numReturn
     *            the top N to return
     * @return List<ProcedureDetails>
     */
    public static List<ProcedureDetails> getChargedMedicarePayGap(boolean largest, int numReturn) {
        return getChargedMedicarePayGap(largest, 0, numReturn);
    }

    public static List<ProcedureDetails> getChargedMedicarePayGap(boolean largest, int start,
            int numReturn) {

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
                cluster = Cluster.builder().addContactPoint(host)
                        // .withCredentials(USERNAME, PASSWORD)
                        .build();
                session = cluster.connect(KEYSPACE);

                Ordering ordering = QueryBuilder.asc("charge_medicare_pay_gap");
                // String ordering = "ASC";
                if (largest) {
                    ordering = QueryBuilder.desc("charge_medicare_pay_gap");
                    // ordering = "DESC";
                }
                Statement stmt = QueryBuilder.select().all().from(CHARGED_MEDICARE_GAP)
                        .where(QueryBuilder.eq("mv_id", 2)).orderBy(ordering);

                stmt.setFetchSize(100);
                ResultSet result = session.execute(stmt);
                int i = -1;
                int remaining = result.getAvailableWithoutFetching();
                for (Row row : result) {
                    i++;
                    if (i < start) {
                        continue; // keep looping
                    }

                    // Add this row to the slice we will return
                    ProcedureDetails procedure = new CassandraProcedure(row);
                    procedureList.add(procedure);

                    if (i >= start + numReturn - 1) {
                        // We have our slice
                        break;
                    }

                    --remaining;
                    if (remaining == 0) {
                        // No rows left in result
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
     * largest/smallest gap between cost of treatment and what Medicare pays in
     * terms of percentage (the percent the patient is responsible for)
     * 
     * @param largest
     *            when true results are in descending order from biggest gap
     *            percentage to least, when false it is in ascending order
     * @param numReturn
     *            the top N returns
     * @return List<ProcedureDetails>
     */
    public static List<ProcedureDetails> getPatientResponsibility(boolean largest, int numReturn) {
        return getPatientResponsibility(largest, 0, numReturn);
    }

    public static List<ProcedureDetails> getPatientResponsibility(boolean largest, int start,
            int numReturn) {
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
                cluster = Cluster.builder().addContactPoint(host)
                        // .withCredentials(USERNAME, PASSWORD)
                        .build();
                session = cluster.connect(KEYSPACE);

                Ordering ordering = QueryBuilder.asc("fraction_responsible");
                if (largest) {
                    ordering = QueryBuilder.desc("fraction_responsible");
                }
                Statement stmt = QueryBuilder.select().all().from(PATIENT_RESPONSIBILITY)
                        .where(QueryBuilder.eq("mv_id", 1)).orderBy(ordering);

                stmt.setFetchSize(100);
                ResultSet result = session.execute(stmt);
                int i = -1;
                int remaining = result.getAvailableWithoutFetching();
                for (Row row : result) {
                    i++;
                    if (i < start) {
                        continue; // keep looping
                    }

                    // Add this row to the slice we will return
                    ProcedureDetails procedure = new CassandraProcedure(row);
                    procedureList.add(procedure);

                    if (i >= start + numReturn - 1) {
                        // We have our slice
                        break;
                    }

                    --remaining;
                    if (remaining == 0) {
                        // No rows left in result
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
    private static List<Provider> buildMockResponse(String state, String procedure)
            throws Exception {
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

}