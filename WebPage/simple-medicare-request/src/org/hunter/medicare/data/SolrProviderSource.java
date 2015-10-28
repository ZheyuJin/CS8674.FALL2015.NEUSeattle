package org.hunter.medicare.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
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
		DEFAULT, UNIQUE_COUNT
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
		// "\"response\":{\"numFound\":19,\"start\":0,\"maxScore\":0.7061373,\"docs:\"[]}}";

		public long numFound;
		public long start;
		public double maxScore;

		public List<Provider> providers;

		public ResponseBody() {
			this.providers = new ArrayList<Provider>();
		}
	}

	public ResponseHeader header;

	public ResponseBody body;

	public SolrProviderSource() {
		this.header = new ResponseHeader();
		this.body = new ResponseBody();
	}

	public SolrProviderSource(QueryResponse solrJresponse) {
		SolrDocumentList list = solrJresponse.getResults();

		this.header = new ResponseHeader();
		this.body = new ResponseBody();

		// Hydrate our object from the SolrJ results (maybe this could be a
		// constructor)

		this.header.status = solrJresponse.getStatus();
		this.header.QTime = solrJresponse.getQTime();
		this.body.start = list.getStart();
		// this.body.maxScore = list.getMaxScore(); // Not always available -
		// throws if not.
		this.body.numFound = list.getNumFound();

		for (int i = 0; i < list.size(); i++) {
			SolrDocument doc = list.get(i);
			this.body.providers.add(new Provider(doc.getFieldValueMap()));
		}
	}

	// Return just states
	public static Set<String> getStates() throws IOException, SolrServerException {
		return getCountsByState().keySet();
	}

	// state and count of providers
	public static HashMap<String, Long> getCountsByState() throws IOException, SolrServerException {
		// http://localhost:8983/solr/csvtest/select?q=NPPES_PROVIDER_STATE%3A*&fl=NPPES_PROVIDER_STATE&wt=json&indent=true&facet=true&facet.field=NPPES_PROVIDER_STATE

		HashMap<String, Long> stateCounts = new HashMap<String, Long>();

		SolrClient solr = null;

		// Todo: refactor the internal query to one method
		try {
			solr = new HttpSolrClient(solrQueryBase);
			String facetField = "NPPES_PROVIDER_STATE";

			SolrQuery query = new SolrQuery();

			// query.set("rows", numRows);
			// query.set("fl", facetField);
			query.setQuery("NPPES_PROVIDER_STATE:*");

			query.setFacet(true);
			query.setFields(facetField);

			query.set("facet.field", facetField);

			query.setStart(0);

      System.out.println("SolrJ query = " + solrQueryBase + query);
			QueryResponse solrJresponse = solr.query(query);

			FacetField field = solrJresponse.getFacetField(facetField);
			for (int i = 0; i < field.getValues().size(); i++) {
				Count stateWithCount = field.getValues().get(i);

				stateCounts.put(stateWithCount.getName(), (Long) stateWithCount.getCount());
			}
		} finally {
			if (solr != null) {
				solr.close();
			}
		}

		return stateCounts;
	}

	// Map of procedure values and codes. Key = code, value = description
	public static HashMap<String, String> getProcedures(int numRows, boolean sortByCount)
			throws IOException, SolrServerException {
		HashMap<String, String> codesWithDescriptions = new HashMap<String, String>();

		// This isn't perfect - we query "all" to get all codes/descriptions and
		// then chop the top "x" from that result
		// http://localhost:8983/solr/csvtest/select?q=HCPCS_CODE%3A*&fl=HCPCS_CODE,HCPCS_DESCRIPTION&wt=json&indent=true&rows=1000&facet=true&facet.field=HCPCS_CODE&facet.sort=index
		// Once our data gets big, we will need to figure this out some other
		// way (could change id to a parseable value like id="<type>:<key for
		// type")

		SolrClient solr = null;

		try {
			solr = new HttpSolrClient(solrQueryBase);
			String facetField = "HCPCS_CODE";

			SolrQuery query = new SolrQuery();

			// query.set("rows", numRows);
			// query.set("fl", facetField);
			query.setQuery("HCPCS_CODE:*");

			query.setFacet(true);
			query.setFields(facetField + ",HCPCS_DESCRIPTION");

			query.set("facet.field", facetField);
			query.set("facet.limit", -1); // Return all facets so we can match
											// them up
			query.setRows(10000);
			if (sortByCount) {
				query.setFacetSort("count"); // "top" means by count
			} else {
				query.setFacetSort("index"); // lexigraphical sort
			}

			query.setStart(0);

      System.out.println("SolrJ query = " + solrQueryBase + query);
			QueryResponse solrJresponse = solr.query(query);

			FacetField field = solrJresponse.getFacetField(facetField);
			for (int i = 0; i < numRows; i++) {
				Count codeWithCounts = field.getValues().get(i);

				codesWithDescriptions.put(codeWithCounts.getName().toUpperCase(), "");
			}

			// Now match up codes with descriptions
			// (ideally we would do this in one pass, but haven't worked that
			// query or schema out)
			SolrDocumentList list = solrJresponse.getResults();
			for (SolrDocument doc : list) {
				String code = doc.get("HCPCS_CODE").toString().toUpperCase();
				if (codesWithDescriptions.containsKey(code)) {
					if (codesWithDescriptions.get(code).isEmpty()) {
						String description = doc.get("HCPCS_DESCRIPTION").toString();
						codesWithDescriptions.put(code, description);
					}
				}
			}
		} finally {
			if (solr != null) {
				solr.close();
			}
		}

		return codesWithDescriptions;
	}

	public static List<Provider> getProviders(int numRows, String state, String procedure)
			throws IOException, SolrServerException {
		return SolrProviderSource.getProviders(numRows, state, procedure, SortField.DEFAULT, false);
	}

	public static List<Provider> getProviders(int numRows, String state, String procedure, SortField sortBy,
			boolean ascending) throws IOException, SolrServerException {

		// http://localhost:8983/solr/csvtest/select?q=HCPCS_CODE:99232%20AND%20NPPES_PROVIDER_STATE:FL&wt=json&indent=true&rows=10&sort=BENE_UNIQUE_CNT+desc
		List<Provider> providers = new ArrayList<Provider>(); // Default =
																// return empty
																// list
		String queryString = "q=NPPES_PROVIDER_STATE:* AND HCPCS_CODE:whatever&wt=json&indent=true";

		SolrClient solr = null;

		try {
			solr = new HttpSolrClient(solrQueryBase);

			SolrQuery query = new SolrQuery(queryString);

			query.set("rows", numRows);
			query.setQuery("NPPES_PROVIDER_STATE:" + state + " AND HCPCS_CODE:" + procedure);
			query.setStart(0);

			String sortField = "id";
			if (sortBy == SortField.UNIQUE_COUNT) {
				sortField = "BENE_UNIQUE_CNT";
			}

			if (ascending) {
				query.setSort(sortField, ORDER.asc);
			} else {
				query.setSort(sortField, ORDER.desc);
			}

      System.out.println("SolrJ query = " + solrQueryBase + query);
			QueryResponse solrJresponse = solr.query(query);

			SolrProviderSource response = new SolrProviderSource(solrJresponse);

			// TODO: This assignment does not preserve sort order
			// (ie: you get the right "top" values, but not in order in the
			// list)
			// Do we care? If so, might need a helper method to copy and
			// preserve order of list.
			List<Provider> list = response.body.providers;
			providers = list;

			System.out.println("Query returned " + list.size() + " results out of " + response.body.numFound);

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

	public static List<Provider> getProviders(int numRows, String queryTerm) throws IOException, SolrServerException {
		List<Provider> providers = new ArrayList<Provider>();
		SolrClient solr = null;

		try {
			solr = new HttpSolrClient(solrQueryBase);

			SolrQuery query = new SolrQuery();

			query.set("rows", numRows);
			query.set("q", queryTerm);
			query.setStart(0);

      System.out.println("SolrJ query = " + solrQueryBase + query);
			QueryResponse solrJresponse = solr.query(query);

			SolrProviderSource response = new SolrProviderSource(solrJresponse);

			List<Provider> list = response.body.providers;
			providers = list;

			System.out.println("Query returned " + list.size() + " results out of " + response.body.numFound);

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
	public static SolrProviderSource getQueryResponse(int numRows, String queryTerm) throws IOException {
		SolrProviderSource response = null;
		SolrClient solr = null;

		try {

			solr = new HttpSolrClient(solrQueryBase);

			SolrQuery query = new SolrQuery();

			query.set("rows", numRows);
			query.set("q", queryTerm);

			QueryResponse solrJresponse = solr.query(query);
			response = new SolrProviderSource(solrJresponse);

			List<Provider> list = response.body.providers;

			System.out.println("Query returned " + list.size() + " results out of " + response.body.numFound);

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

	public static void main(String[] args) throws IOException {
		System.out.println("testing...");

		SolrClient solr = null;

		try {

			// Test out a query
			getQueryResponse(10, "knee");

			// Test out state count query
			Set<String> states = SolrProviderSource.getStates();
			for (String state : states) {
				System.out.println("State: " + state);
			}

			// Test out "get codes and descriptions query"
			HashMap<String, String> codesWithDescriptions = SolrProviderSource.getProcedures(20, true);
			for (String key : codesWithDescriptions.keySet()) {
				String value = codesWithDescriptions.get(key);
				System.out.println("Code " + key + " : " + value);
			}

			// Test "get provider by (any) state and procedure, sort based on
			// beneficiary unique count
			List<Provider> providers = getProviders(10, "*", "69210");
			for (Provider p : providers) {
				System.out.println("Provider " + p.last_or_org_name + ", procedure " + p.hcpcs_code + " ("
						+ p.hcpcs_description + "): " + p.beneficiaries_unique_count);
			}

			// Test "get provider by state and procedure, sort based on
			// beneficiary unique count
			providers = getProviders(10, "CA", "99213");
			for (Provider p : providers) {
				System.out.println("Provider " + p.last_or_org_name + ", procedure " + p.hcpcs_code + " ("
						+ p.hcpcs_description + "): " + p.beneficiaries_unique_count);
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
