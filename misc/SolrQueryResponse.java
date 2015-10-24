package com.crunchify.controller;  // ToDo - change this once we know our package name

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
//import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
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
 
  public static List<String> getStates(int numRows) throws IOException, SolrServerException
  {
    List<String> states = new ArrayList<String>();
    
    SolrClient solr = null;
    
    // Todo: refactor the internal query to one method
    try {    
      solr = new HttpSolrClient(solrQueryBase);      
      
      SolrQuery query = new SolrQuery();
      
      query.set("rows", numRows);
      query.set("q", "NPPES_PROVIDER_STATE:*");
      // set fl or group by?
      query.setStart(0);
      
      QueryResponse solrJresponse = solr.query(query);
      
      SolrDocumentList list = solrJresponse.getResults();
      
    }    
    finally {
      if (solr != null)
      {
        solr.close();
      }
    }      
    
    return states;
  }
  
  // Key = code, value = description
  public static HashMap<String, String> getProcedures(int numRows) throws IOException
  {
    HashMap<String,String> codes = new HashMap<String,String>();
    
    return codes;    
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

      // Using SolrJ to parse results.

      // Try SolrJ
      // - add to maven pom.xml
      // - add imports
      // - know what your zookeeper names/ports are...
      
      //String zkHostString = "localhost:9983/solr";
      //solr = new CloudSolrClient(zkHostString);
      // set default collection?
      
      solr = new HttpSolrClient("http://localhost:8983/solr/csvtest");      
      
      SolrQuery query = new SolrQuery();
      
      query.set("rows", "10");
      query.set("q", "knee");
      
      QueryResponse solrJresponse = solr.query(query);
      
      SolrDocumentList list = solrJresponse.getResults();
      
      System.out.println("Query returned " + list.size() + " results out of " + list.getNumFound());
      
      if (list.getNumFound() > 0)
      {
        if (list.size() > 0)
        {
          for (SolrDocument doc : list)
          {
            System.out.println("Query result id = " + doc.getFieldValue("id").toString());
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
    
  }

}

