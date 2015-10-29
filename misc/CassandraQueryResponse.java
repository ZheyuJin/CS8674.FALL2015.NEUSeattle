package org.hunter.medicare.data;

import java.util.ArrayList;
import java.util.List;

import org.hunter.medicare.data.SolrProviderSource;

//import org.apache.log4j.BasicConfigurator;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class CassandraQueryResponse {
//Set to true for mock data, false if you want to connect to Cassandra
    private boolean mock = false;

    private static CassandraQueryResponse instance = null;

    public static CassandraQueryResponse getInstance() {
	if (instance == null) {
	    instance = new CassandraQueryResponse();
	}
	return instance;
    }

    public List<Provider> getMostExpensive(String state, String procedure) {
	List<Provider> providers = new ArrayList<Provider>();

	if (mock) {
	    providers = buildMockResponse(state, procedure);
	    /* assign values from 1 to n to proviser's charge. need this for mocking.*/	    
	    int charge =1;
	    for(Provider p : providers){
		p.providerDetails = p.new ExtendedInfo();
		p.providerDetails.averageSubmittedChargeAmount = charge;
		charge ++;
	    }
    	} else {
    		//FIXME 
    		// CALL Josh's function here
    	}
    	return providers;
        }
        
        /**
         * Returns the Provider when given the corresponding Id
         * 
         * @param id
         * @return Provider, if no matching ID or bad connection, will return null
         */
        //Josh's query will return a list of IDs and will call this method for 
        //each ID to get the Provider
        //Josh needs to package these into a List for Jin
        //TODO Note: This function returns null if a Provider is not found that matches the id
        //or if the connection is bad
        public Provider getProviderById(String id) {
        	//TODO Logger
        	    // uncomment this to use logger
        	    // BasicConfigurator.configure();
        	
    		Provider provider = null;
    		
        	    // connect to server
        	    Cluster cluster;
        	    Session session = null;
        	    cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
        	    // pick KEYSPACE
//       	    try {
        	    session = cluster.connect("demo");
        	    System.out.println("connecting to cassandra");
        	    // compose query
        	    String query1 = "SELECT * FROM proceduresstats"
        	    		+ " WHERE id = '" + id + "';";
        	    
        	    ResultSet procedureResult = session.execute(query1);
        	    Row procedureRow = procedureResult.one();
        	    if (procedureRow == null) {
        	    	System.out.println("failed to return a result");
        	    }
        	    
        	    String npi = procedureRow.getString("npi");
        	    System.out.println("The npi = " + npi);
        	    String hcpcs_code = procedureRow.getString("hcpcs_code");
        	   
        	    String query2 = "SELECT * FROM proceduresinfo"
        	    		+ " WHERE hcpcs_code = '" + hcpcs_code + "';";
        	    
        	    ResultSet procedureInfoResult = session.execute(query2);
        	    Row procedureInfoRow = procedureInfoResult.one();
        	    
        	    String query3 = "SELECT * FROM providers "
        	    		+ " WHERE npi = '" + npi + "';";
        	    
        	    ResultSet providerResult = session.execute(query3);
        	    Row providerRow = providerResult.one();
        	    
        	    provider = new Provider(providerRow, procedureRow, procedureInfoRow);
        	   
//        	    } catch (Exception e) {
//        	    	//TODO seperate out exceptions
//        	    	System.out.println("An Error Occured");
//        	    } finally {
//        	    	if (session != null) {
//        	    		cluster.close();	 
//        	    	}
//        	    }
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
}