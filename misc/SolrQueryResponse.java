package com.crunchify.controller; // TODO: change this when we know what our package name is

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
//import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class SolrQueryResponse {
 
  // ToDo: get this from config instead?
  public static String solrUrlBase = "http://localhost:8983/solr/";
  public static String collectionName = "csvtest";
  public static String solrQueryBase = solrUrlBase + collectionName;

  public enum SortField {
    DAY_COUNT
  }
  
  // Define a few nested classes specific to the query response
  // I expect these to be mostly used internally to solr queries.
  public class RequestParams {
    public String q;
    public boolean indent;
    public String wt;
    public String rows;
    public String sort;  
    public String start;
  } 

  public class ResponseHeader {
    
    public int status;
    public int QTime;
    
    public RequestParams params;  
  }
  
  public class ResponseBody {
    //"\"response\":{\"numFound\":19,\"start\":0,\"maxScore\":0.7061373,\"docs:\"[]}}";

    public long numFound;
    public long start;
    public double maxScore;
    
    public List<Provider> providers;
     
    public ResponseBody()
    {
      this.providers = new ArrayList<Provider>();
    }
  }
  
  public ResponseHeader header;
  
  public ResponseBody body; 
 
  public SolrQueryResponse()
  {
    this.header = new ResponseHeader();
    this.body = new ResponseBody();    
  }
  
  public SolrQueryResponse(QueryResponse solrJresponse)
  {   
    SolrDocumentList list = solrJresponse.getResults();
    
    this.header = new ResponseHeader();
    this.body = new ResponseBody();    
 
    // Hydrate our object from the SolrJ results (maybe this could be a constructor)
    
    this.header.status = solrJresponse.getStatus();
    this.header.QTime = solrJresponse.getQTime();
    this.body.start = list.getStart();
    this.body.maxScore = list.getMaxScore();
    this.body.numFound = list.getNumFound(); 
    
    for (int i=0; i<list.size(); i++)
    {
      SolrDocument doc = list.get(i);
      this.body.providers.add(new Provider(doc.getFieldValueMap()));       
    }   
  }
 
  // Return just states
  public static Set<String> getStates() throws IOException, SolrServerException
  {
    return getCountsByState().keySet();
  }
  
  // state and count of providers
  public static HashMap<String, Long> getCountsByState() throws IOException, SolrServerException
  {
    // http://localhost:8983/solr/csvtest/select?q=NPPES_PROVIDER_STATE%3A*&fl=NPPES_PROVIDER_STATE&wt=json&indent=true&facet=true&facet.field=NPPES_PROVIDER_STATE
    
    HashMap<String, Long> stateCounts = new HashMap<String, Long>();
    
    SolrClient solr = null;
    
    // Todo: refactor the internal query to one method
    try {    
      solr = new HttpSolrClient(solrQueryBase); 
      String facetField = "NPPES_PROVIDER_STATE";
      
      SolrQuery query = new SolrQuery();
      
      //query.set("rows", numRows);
      //query.set("fl", facetField);
      query.setQuery("NPPES_PROVIDER_STATE:*");
      
      query.setFacet(true);
      query.setFields(facetField);
     
      query.set("facet.field", facetField);
       
      query.setStart(0);
      
      QueryResponse solrJresponse = solr.query(query);
      
      FacetField field=solrJresponse.getFacetField(facetField);
      for (int i=0; i < field.getValues().size(); i++) {
        Count stateWithCount = field.getValues().get(i);
        
        stateCounts.put(stateWithCount.getName(), (Long)stateWithCount.getCount());
      }    
    }    
    finally {
      if (solr != null)
      {
        solr.close();
      }
    }      
    
    return stateCounts;
  }
  
  // Key = code, value = description
  public static HashMap<String, String> getProcedures(int numRows, boolean sortByCount) throws IOException, SolrServerException
  {
    HashMap<String,String> codesWithDescriptions = new HashMap<String,String>();

    // This isn't perfect - we query "all" to get all codes/descriptions and then chop the top "x" from that result 
    // http://localhost:8983/solr/csvtest/select?q=HCPCS_CODE%3A*&fl=HCPCS_CODE,HCPCS_DESCRIPTION&wt=json&indent=true&rows=1000&facet=true&facet.field=HCPCS_CODE&facet.sort=index
   
  
    SolrClient solr = null;
    
    try {    
      solr = new HttpSolrClient(solrQueryBase); 
      String facetField = "HCPCS_CODE";
      
      SolrQuery query = new SolrQuery();
      
      //query.set("rows", numRows);
      //query.set("fl", facetField);
      query.setQuery("HCPCS_CODE:*");
      
      query.setFacet(true);
      query.setFields(facetField + ",HCPCS_DESCRIPTION");
     
      query.set("facet.field", facetField);
      query.set("facet.limit", -1);  // Return all facets so we can match them up
      query.setRows(10000);
      if (sortByCount)
      {
        query.setFacetSort("count");  // "top" means by count
      }
      else
      {
        query.setFacetSort("index"); // lexigraphical sort
      }
       
      query.setStart(0);
      
      QueryResponse solrJresponse = solr.query(query);
      
      FacetField field=solrJresponse.getFacetField(facetField);
      for (int i=0; i < numRows; i++) {
        Count codeWithCounts = field.getValues().get(i);
        
        codesWithDescriptions.put(codeWithCounts.getName().toUpperCase(), "");
      } 
      
      // Now match up codes with descriptions 
      // (ideally we would do this in one pass, but haven't worked that query out)
      // To do - could we use a copy field for this since there's a 1:1 mapping
      // between hcpcs code and description?
      SolrDocumentList list = solrJresponse.getResults();
      for (SolrDocument doc : list)
      {
        String code = doc.get("HCPCS_CODE").toString().toUpperCase();
        if (codesWithDescriptions.containsKey(code))
        {
          if (codesWithDescriptions.get(code).isEmpty())
          {
            String description = doc.get("HCPCS_DESCRIPTION").toString();
            codesWithDescriptions.put(code, description);
          }
        }        
      }      
    }    
    finally {
      if (solr != null)
      {
        solr.close();
      }
    }
    
    return codesWithDescriptions;  
  }
  
  public static List<Provider> getProviders(int numRows, String state, String procedure) throws IOException
  {  
    List<Provider> providers = new ArrayList<Provider>();
    
    return providers;
  }
  
  public static List<Provider> getProviders(int numRows, String procedure, SortField sortBy, boolean ascending) throws IOException
  {   
    List<Provider> providers = new ArrayList<Provider>();
   
    return providers;  
  }
  
  public static List<Provider> getProviders(int numRows, String queryTerm) throws IOException
  {    
    List<Provider> providers = new ArrayList<Provider>();
    SolrClient solr = null;
    
    try {    
      solr = new HttpSolrClient(solrUrlBase + "csvtest");      
      
      SolrQuery query = new SolrQuery();
      
      query.set("rows", numRows);
      query.set("q", queryTerm);
      query.setStart(0);
    }    
    finally {
      if (solr != null)
      {
        solr.close();
      }
    }
    
    return providers;
  }
  
  public static SolrQueryResponse getQueryResponse(int numRows, String queryTerm) throws IOException
  {
    SolrQueryResponse response = null;
    SolrClient solr = null;
    
    try {
      
      solr = new HttpSolrClient(solrQueryBase);      
      
      SolrQuery query = new SolrQuery();
      
      query.set("rows", numRows);
      query.set("q", queryTerm);
      
      QueryResponse solrJresponse = solr.query(query);
      response = new SolrQueryResponse(solrJresponse);
      
      List<Provider> list = response.body.providers;
      
      System.out.println("Query returned " + list.size() + " results out of " + response.body.numFound);
      
      if (response.body.numFound > 0)
      {
        if (list.size() > 0)
        {
          for (Provider p : list)
          {
            System.out.println("Query result id = " + p.id);
          }          
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally {
      if (solr != null)
      {
        solr.close();
      }
    }
        
    return response;
  }
  
  public static void main(String[] args) throws IOException 
  {
    System.out.println("testing...");
    
    SolrClient solr = null;
    
    try {

      // Test out a query
      getQueryResponse(10, "knee");      
      
      // Test out state count query
      Set<String> states = SolrQueryResponse.getStates();
      for(String state: states)
      {
         System.out.println("State: " + state); 
      }
      
      // Test out "get codes and descriptions query"
      HashMap<String, String> codesWithDescriptions = SolrQueryResponse.getProcedures(20, true);
      for (String key : codesWithDescriptions.keySet())
      {
         String value = codesWithDescriptions.get(key);
         System.out.println("Code " + key + " : " + value);
      }      
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally {
      if (solr != null)
      {
        solr.close();
      }
    }
    
  }

}

