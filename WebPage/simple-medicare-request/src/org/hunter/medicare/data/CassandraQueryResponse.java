package org.hunter.medicare.data;

import java.util.ArrayList;
import java.util.List;

import org.hunter.medicare.controller.SolrTestController;

//import org.apache.log4j.BasicConfigurator;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class CassandraQueryResponse {
  
  private boolean mock=true;
  
  private static CassandraQueryResponse instance = null;
  
  public static CassandraQueryResponse getInstance() {
    if (instance == null) {
      instance = new CassandraQueryResponse();
    }
    return instance; 
  }
  
  public List<Provider> getMostExpensive(String state, String procedure) {    
    List<Provider> providers = new ArrayList<Provider>();
    
    if (mock)
    {
      providers = buildMockResponse(state, procedure);
    }
    else {
      //uncomment this to use logger
    //BasicConfigurator.configure();
    
    //connect to server 
    Cluster cluster;
    Session session;
    cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
    //pick KEYSPACE
    session = cluster.connect("demo");
    
    //compose query
    String query = "SELECT npi FROM procedures_stats"
                     + " WHERE hcpcs_code = '" + procedure + "' AND nppes_provider_state = '" + state + "'"
                     + " ALLOW FILTERING;";
    
    
    
    
    //cannot ORDER BY unless we make a materialized view where they are ordered by price
    // at least that is my understanding
    
    //send query to cassandra
      ResultSet results = session.execute(query);
      
      List<Row> rl = new ArrayList<Row>(); 
      for (Row row : results) {
        int npi = row.getInt("npi");
        
         String query2 = "SELECT * FROM providers" 
               + " WHERE npi = " + npi + ";";
            //   + " GROUP BY npi;";
         
         ResultSet results2 = session.execute(query);
         Row s = results2.one();
         rl.add(s);
         }
  
    
    
    for (Row row : rl) {
      Provider resultProvider = new Provider(row);
      providers.add(resultProvider);
      
    }
  
      
         //don't forget to close connection
      cluster.close();
      //convert result to a string
  //    StringBuilder sb = new StringBuilder();
  //    for (Row row : results) {
  //      sb.append(row.getString("lastname"));
  //      sb.append(" ");
  //      sb.append(row.getString("firstname"));
  //      sb.append("/n");
  //    }
  //    String resultString = sb.toString();
  //    return resultString;
    }
    return providers;
    
  }
  
  /**
   * used for integration phase. returns mock data. 
   * @param state
   * @param procedure
   * @return
 * @throws Exception 
   */
  private List<Provider> buildMockResponse(String state, String procedure)
  {
    List<Provider> providers = new ArrayList<Provider>();
    
    try{
    	// always mock in this way.
    	providers = new SolrTestController().getTop("CA","*",null);
    }
    catch(Exception e){
    	e.printStackTrace();
    }
    
    return providers;
  }
}