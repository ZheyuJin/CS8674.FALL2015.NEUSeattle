package org.hunter.medicare.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
//import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class SolrProviderSource {

    // ToDo: get this from config instead?
    // public static String solrUrlBase = "http://localhost:8983/solr/";
    public static String solrUrlBase = "http://54.200.138.99:8983/solr/";
    public static String collectionName = "csvtest";
    public static String solrQueryBase = solrUrlBase + collectionName;

    public enum SortField {
	DEFAULT, // Solr default, no explicit sort specified.
	UNIQUE_COUNT
    }

    // Define a few nested classes specific to the query response
    // I expect these to be used internally to solr queries.
    protected class RequestParams {
	public String q;
	public boolean indent;
	public String wt;
	public String rows;
	public String sort;
	public String start;
    }

    protected class ResponseHeader {

	public int status;
	public int QTime;

	public RequestParams params;
    }

    protected class ResponseBody {
	// "\"response\":{\"numFound\":19,\"start\":0,\"maxScore\":0.7061373,\"docs:\"[]}}";

	public long numFound;
	public long start;
	// public double maxScore; // Not always present, and not needed.

	public List<Provider> providers;

	public ResponseBody() {
	    this.providers = new ArrayList<Provider>();
	}
    }

    protected ResponseHeader header;
    protected ResponseBody body;

    public SolrProviderSource() {
	this.header = new ResponseHeader();
	this.body = new ResponseBody();
    }

    protected SolrProviderSource(QueryResponse solrJresponse) {
	SolrDocumentList list = solrJresponse.getResults();

	this.header = new ResponseHeader();
	this.body = new ResponseBody();

	this.header.status = solrJresponse.getStatus();
	this.header.QTime = solrJresponse.getQTime();
	this.body.start = list.getStart();

	// Not always available - get call throws if it's not there.
	// this.body.maxScore = list.getMaxScore();

	this.body.numFound = list.getNumFound();

	for (int i = 0; i < list.size(); i++) {
	    SolrDocument doc = list.get(i);
	    this.body.providers.add(new Provider(doc.getFieldValueMap()));
	}
    }

    // Return just states
    public static Set<String> getStates() throws IOException,
	    SolrServerException {
	return getCountsByState().keySet();
    }

    // state and count of providers
    public static HashMap<String, Long> getCountsByState() throws IOException,
	    SolrServerException {

	HashMap<String, Long> stateCounts = new HashMap<String, Long>();
	SolrClient solr = null;

	// Todo: refactor the internal query to one method
	try {
	    solr = new HttpSolrClient(solrQueryBase);
	    String facetField = "NPPES_PROVIDER_STATE";

	    SolrQuery query = new SolrQuery();

	    query.setQuery("NPPES_PROVIDER_STATE:*");

	    query.setFacet(true);
	    query.setFields(facetField);

	    query.set("facet.field", facetField);

	    query.setStart(0);

	    System.out.println("SolrJ query = " + solrQueryBase + "/select?"
		    + query);
	    QueryResponse solrJresponse = solr.query(query);

	    FacetField field = solrJresponse.getFacetField(facetField);
	    for (int i = 0; i < field.getValues().size(); i++) {
		Count stateWithCount = field.getValues().get(i);

		stateCounts.put(stateWithCount.getName(),
			(Long) stateWithCount.getCount());

	    }
	} finally {
	    if (solr != null) {
		solr.close();
	    }
	}

	return stateCounts;
    }

    // Map of procedure values and codes. Key = code, value = description
    // Note that we return only the procedures defined for the "top numrows"
    // providers who had a procedure with the query term, as ordered by
    // beneficiary unique count.
    public static HashMap<String, String> getProcedures(int numRows,
	    String queryTerm) throws IOException, SolrServerException {

	HashMap<String, String> codesWithDescriptions = new HashMap<String, String>();

	SolrClient solr = null;

	try {
	    solr = new HttpSolrClient(solrQueryBase);

	    SolrQuery query = new SolrQuery();

	    query.setQuery(queryTerm);
	    query.setFields("HCPCS_CODE,HCPCS_DESCRIPTION");

	    query.setRows(numRows);
	    query.setStart(0);
	    query.setSort("BENE_UNIQUE_CNT", ORDER.desc); // TODO: other sorts?

	    System.out.println("SolrJ query = " + solrQueryBase + "/select?"
		    + query);
	    QueryResponse solrJresponse = solr.query(query);

	    SolrDocumentList list = solrJresponse.getResults();
	    for (SolrDocument doc : list) {
		String code = doc.get("HCPCS_CODE").toString().toUpperCase();

		if (!codesWithDescriptions.containsKey(code)) {
		    String description = doc.get("HCPCS_DESCRIPTION")
			    .toString();
		    codesWithDescriptions.put(code.toUpperCase(), description);
		}
	    }
	} finally {
	    if (solr != null) {
		solr.close();
	    }
	}

	return codesWithDescriptions;

    }

    // Map of procedure values and codes. Key = code, value = description
    // Note that we return only the procedures defined for the "top numrows"
    // providers, as ordered by beneficiary unique count.
    public static HashMap<String, String> getProcedures(int numRows)
	    throws IOException, SolrServerException {

	HashMap<String, String> codesWithDescriptions = new HashMap<String, String>();

	SolrClient solr = null;

	try {
	    solr = new HttpSolrClient(solrQueryBase);

	    SolrQuery query = new SolrQuery();

	    query.setQuery("HCPCS_CODE:*");
	    query.setFields("HCPCS_CODE,HCPCS_DESCRIPTION");

	    query.setRows(numRows);
	    query.setStart(0);
	    query.setSort("BENE_UNIQUE_CNT", ORDER.desc);

	    System.out.println("SolrJ query = " + solrQueryBase + "/select?"
		    + query);
	    QueryResponse solrJresponse = solr.query(query);

	    SolrDocumentList list = solrJresponse.getResults();
	    for (SolrDocument doc : list) {
		String code = doc.get("HCPCS_CODE").toString().toUpperCase();

		if (!codesWithDescriptions.containsKey(code)) {
		    String description = doc.get("HCPCS_DESCRIPTION")
			    .toString();
		    codesWithDescriptions.put(code.toUpperCase(), description);
		}
	    }
	} finally {
	    if (solr != null) {
		solr.close();
	    }
	}

	return codesWithDescriptions;
    }

    public static List<Provider> getProviders(int numRows, String state,
	    String procedure) throws IOException, SolrServerException {
	return SolrProviderSource.getProviders(numRows, state, procedure,
		SortField.DEFAULT, false);
    }

    // Get sorted list of providers for the given state and procedure.
    // Returns up to numRows results (less if not that many providers exist
    // for the state + procedure combination).
    public static List<Provider> getProviders(int numRows, String state,
	    String procedure, SortField sortBy, boolean ascending)
	    throws IOException, SolrServerException {

	List<Provider> providers = new ArrayList<Provider>();
	// Default = return empty list

	String queryString = "q=NPPES_PROVIDER_STATE:* AND HCPCS_CODE:whatever&wt=json&indent=true";

	SolrClient solr = null;

	try {
	    solr = new HttpSolrClient(solrQueryBase);

	    SolrQuery query = new SolrQuery(queryString);

	    query.set("rows", numRows);
	    query.setQuery("NPPES_PROVIDER_STATE:" + state + " AND HCPCS_CODE:"
		    + procedure);
	    query.setStart(0);

	    // TODO: Turn into common "get sort field" method
	    String sortField = "id";
	    if (sortBy == SortField.UNIQUE_COUNT) {
		sortField = "BENE_UNIQUE_CNT";
	    }

	    if (ascending) {
		query.setSort(sortField, ORDER.asc);
	    } else {
		query.setSort(sortField, ORDER.desc);
	    }

	    System.out.println("SolrJ query = " + solrQueryBase + "/select?"
		    + query);
	    QueryResponse solrJresponse = solr.query(query);

	    SolrProviderSource response = new SolrProviderSource(solrJresponse);

	    // TODO: This assignment does not preserve sort order
	    // (ie: you get the right "top" values, but not in order in the
	    // list)
	    // Currently sorting in controller.
	    List<Provider> list = response.body.providers;
	    providers = list;

	    System.out.println("Query returned " + list.size()
		    + " results out of " + response.body.numFound);

	    if (response.body.numFound > 0) {
		if (list.size() > 0) {
		    for (Provider p : list) {
			System.out.println("Query result id = " + p.id);
		    }
		}
	    }
	} finally {
	    if (solr != null) {
		solr.close();
	    }
	}
	return providers;
    }

    // Get list of providers who perform procedures containing the given
    // queryTerm (for example: knee or inpatient)
    public static List<Provider> getProviders(int numRows, String queryTerm)
	    throws IOException, SolrServerException {
	List<Provider> providers = new ArrayList<Provider>();
	SolrClient solr = null;

	try {
	    solr = new HttpSolrClient(solrQueryBase);

	    SolrQuery query = new SolrQuery();
	    query.setQuery("HCPCS_DESCRIPTION:" + queryTerm);

	    query.set("rows", numRows);
	    query.setStart(0);

	    // By default, sort based on which procedure is performed often
	    query.setSort("BENE_UNIQUE_CNT", ORDER.desc);

	    System.out.println("SolrJ query = " + solrQueryBase + "/select?"
		    + query);
	    QueryResponse solrJresponse = solr.query(query);

	    SolrProviderSource response = new SolrProviderSource(solrJresponse);

	    List<Provider> list = response.body.providers;
	    providers = list;

	    System.out.println("Query returned " + list.size()
		    + " results out of " + response.body.numFound);

	    if (response.body.numFound > 0) {
		if (list.size() > 0) {
		    for (Provider p : list) {
			System.out.println("Query result id = " + p.id);
		    }
		}
	    }
	} finally {
	    if (solr != null) {
		solr.close();
	    }
	}
	return providers;
    }

    // Free text search, like "knee"
    protected static SolrProviderSource getQueryResponse(int numRows,
	    String queryTerm) throws IOException {
	SolrProviderSource response = null;
	SolrClient solr = null;

	try {

	    solr = new HttpSolrClient(solrQueryBase);

	    SolrQuery query = new SolrQuery();

	    query.set("rows", numRows);
	    query.set("q", queryTerm);

	    QueryResponse solrJresponse = solr.query(query);
	    System.out.println("SolrJ query = " + solrQueryBase + "/select?"
		    + query);
	    response = new SolrProviderSource(solrJresponse);

	    List<Provider> list = response.body.providers;

	    System.out.println("Query returned " + list.size()
		    + " results out of " + response.body.numFound);

	    if (response.body.numFound > 0) {
		if (list.size() > 0) {
		    for (Provider p : list) {
			System.out.println("Query result id = " + p.id);
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    if (solr != null) {
		solr.close();
	    }
	}

	return response;
    }

    // For testing...
    public static void main(String[] args) throws IOException {
	System.out.println("testing...");

	SolrClient solr = null;

	try {

	    // Test out a query
	    getQueryResponse(10, "knee");

	    // Test out state count query
	    System.out.println("Querying for list of states providers are in:");
	    Set<String> states = SolrProviderSource.getStates();
	    for (String state : states) {
		System.out.println("  State: " + state);
	    }

	    // Test out "get codes and descriptions query"
	    System.out
		    .println("Querying for list of procedures providers reported:");
	    HashMap<String, String> codesWithDescriptions = SolrProviderSource
		    .getProcedures(20);
	    for (String key : codesWithDescriptions.keySet()) {
		String value = codesWithDescriptions.get(key);
		System.out.println("  Code " + key + " : " + value);
	    }

	    // Test out "get codes and descriptions query" #2
	    System.out
		    .println("Querying for list of procedures providers reported for knees:");
	    codesWithDescriptions = SolrProviderSource
		    .getProcedures(20, "knee");
	    for (String key : codesWithDescriptions.keySet()) {
		String value = codesWithDescriptions.get(key);
		System.out.println("  Code " + key + " : " + value);
	    }

	    // Test "get provider by (any) state and procedure, sort based on
	    // beneficiary unique count
	    System.out
		    .println("Querying for providers by state = * and procedure = 69210:");
	    List<Provider> providers = getProviders(10, "*", "99214");
	    for (Provider p : providers) {
		System.out.println("  Provider " + p.last_or_org_name
			+ ", procedure " + p.hcpcs_code + " ("
			+ p.hcpcs_description + "): "
			+ p.beneficiaries_unique_count);
	    }

	    // Test "get provider by state and procedure, sort based on
	    // beneficiary unique count
	    System.out
		    .println("Querying for providers by state = CA and procedure = 99213:");
	    providers = getProviders(10, "FL", "99213");
	    for (Provider p : providers) {
		System.out.println("  Provider " + p.last_or_org_name
			+ ", procedure " + p.hcpcs_code + " ("
			+ p.hcpcs_description + "): "
			+ p.beneficiaries_unique_count);
	    }

	    // Test "get providers who perform something to do with "knee"
	    System.out
		    .println("Querying for providers who did something related to knees:");
	    providers = getProviders(10, "knee");
	    for (Provider p : providers) {
		System.out.println("  Provider " + p.last_or_org_name
			+ ", procedure " + p.hcpcs_code + " ("
			+ p.hcpcs_description + "): "
			+ p.beneficiaries_unique_count);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    if (solr != null) {
		solr.close();
	    }
	}
    }
}
