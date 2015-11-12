package org.hunter.medicare.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public static String solrUrlBase = "http://localhost:8983/solr/";
    //public static String solrUrlBase = "http://54.200.138.99:8983/solr/";
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

        // Queries are typically either for providers or facet counts.
        public List<Provider> providers;
        public HashMap<String, Long> facets;

        public ResponseBody() {
            this.providers = new ArrayList<Provider>();
            this.facets = new HashMap<String, Long>();
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

        // If we have provider results, fill in our provider array
        if (this.body.numFound > 0) {
            for (int i = 0; i < list.size(); i++) {
                SolrDocument doc = list.get(i);
                Map<String, Object> fields = doc.getFieldValueMap();
                if (fields != null && fields.containsKey("id")) {
                    this.body.providers.add(new Provider(fields));
                }
            }
        }

        // Fill in the first facet and count, if it exists
        if ((solrJresponse.getFacetFields() != null) && solrJresponse.getFacetFields().size() > 0) {
            FacetField field = solrJresponse.getFacetFields().get(0);
            for (int i = 0; i < field.getValues().size(); i++) {
                Count facetWithCount = field.getValues().get(i);

                this.body.facets.put(facetWithCount.getName(), (Long) facetWithCount.getCount());
            }
        }

    }

    // Return just states
    public static Set<String> getStates() throws IOException, SolrServerException {
        return getCountsForStates().keySet();
    }

    // Provider counts by state (state = key)
    public static HashMap<String, Long> getCountsForStates() throws IOException,
            SolrServerException {

        // http://localhost:8983/solr/csvtest/select?q=NPPES_PROVIDER_STATE%3A*&fl=NPPES_PROVIDER_STATE&wt=json&indent=true&facet=true&facet.field=NPPES_PROVIDER_STATE

        String facetField = "NPPES_PROVIDER_STATE";
        SolrQuery query = new SolrQuery();

        query.setQuery("NPPES_PROVIDER_STATE:*");
        query.setFacet(true);
        query.setFields(facetField);
        query.set("facet.field", facetField);
        query.setStart(0);

        SolrProviderSource solrData = getQueryResponse(query);
        System.out.println("Facet query returned " + solrData.body.facets.size());

        return solrData.body.facets;
    }

    // Zip with provider counts per state (input state, output map key = zip)
    public static HashMap<String, Long> getZipCountsByState(String state) throws IOException,
            SolrServerException {
        String facetField = "NPPES_PROVIDER_ZIP";
        SolrQuery query = new SolrQuery();

        query.setQuery("NPPES_PROVIDER_STATE:*");
        query.setFacet(true);
        query.setFields(facetField);
        query.set("facet.field", facetField);
        query.setStart(0);

        SolrProviderSource solrData = getQueryResponse(query);
        System.out.println("Facet query returned " + solrData.body.facets.size());

        return solrData.body.facets;
    }

    // Map of procedure values and codes. Key = code, value = description
    // Note that we return only the procedures defined for the "top numrows"
    // providers who had a procedure with the query term, as ordered by
    // beneficiary unique count.
    public static HashMap<String, String> getProcedures(int numRows, String queryTerm)
            throws IOException, SolrServerException {

        HashMap<String, String> codesWithDescriptions = new HashMap<String, String>();

        SolrQuery query = new SolrQuery();

        if (queryTerm != null && queryTerm != "") {
            query.setQuery(queryTerm);
        }
        query.setFields("id,HCPCS_CODE,HCPCS_DESCRIPTION");

        query.setRows(numRows);
        query.setStart(0);
        query.setSort("BENE_UNIQUE_CNT", ORDER.desc); // TODO: other sorts?

        SolrProviderSource solrData = getQueryResponse(query);
        for (Provider p : solrData.body.providers) {
            String code = p.hcpcs_code.toUpperCase();

            if (!codesWithDescriptions.containsKey(code)) {
                String description = p.hcpcs_description.toString();
                codesWithDescriptions.put(code.toUpperCase(), description);
            }
        }
        System.out.println("Code/Description query returned " + codesWithDescriptions.size()
                + " codes");

        return codesWithDescriptions;

    }

    // Map of procedure values and codes. Key = code, value = description
    // Note that we return only the procedures defined for the "top numrows"
    // providers, as ordered by beneficiary unique count.
    public static HashMap<String, String> getProcedures(int numRows) throws IOException,
            SolrServerException {

        return getProcedures(numRows, "");
    }

    public static List<Provider> getProviders(int numRows, String state, String procedure)
            throws IOException, SolrServerException {

        return SolrProviderSource.getProviders(numRows, state, procedure, SortField.DEFAULT, false);
    }

    // Get the providers for the given state and/or zip
    public static List<Provider> getProvidersByStateZipOrType(String state, String zip, String providerType, Integer start, Integer numRows)
            throws IOException, SolrServerException {

        List<Provider> providers = new ArrayList<Provider>();

 
        String stateQuery = "NPPES_PROVIDER_STATE:";
        if (state != null && !state.isEmpty()) {
            stateQuery = stateQuery + state;
        } else {
            stateQuery = stateQuery + "*";
        }
       
        String zipQuery = "NPPES_PROVIDER_ZIP:";
        if (zip != null && !zip.isEmpty()) {
            zipQuery = zipQuery + state;
        } else {
            zipQuery = zipQuery + "*";
        }

        String providerQuery = "PROVIDER_TYPE:";
        if (providerType != null && !providerType.isEmpty()) {
            providerQuery = providerQuery + providerType;
        } else {
            providerQuery = providerQuery + "*";
        }
 
 
        String queryString = stateQuery + " AND " + zipQuery + " AND " + providerQuery + "&wt=json&indent=true";
        SolrQuery query = new SolrQuery(queryString);

        // Set other things and do the actual query
        query.setRows(numRows);
        query.setStart(start);
 
        SolrProviderSource solrData = getQueryResponse(query);
        providers = solrData.body.providers;

        System.out.println("Query returned " + providers.size() + " results out of "
                + solrData.body.numFound);

        return new ArrayList<Provider>();
    }

    // Get sorted list of providers for the given state and procedure.
    // Returns up to numRows results (less if not that many providers exist
    // for the state + procedure combination).
    public static List<Provider> getProviders(int numRows, String state, String procedure,
            SortField sortBy, boolean ascending) throws IOException, SolrServerException {

        List<Provider> providers = new ArrayList<Provider>();
        // Default = return empty list

        String queryString = "q=NPPES_PROVIDER_STATE:* AND HCPCS_CODE:whatever&wt=json&indent=true";

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

        SolrProviderSource solrData = getQueryResponse(query);
        providers = solrData.body.providers;

        System.out.println("Query returned " + providers.size() + " results out of "
                + solrData.body.numFound);

        return providers;
    }

    // Get list of providers who perform procedures containing the given
    // queryTerm (for example: knee or inpatient)
    public static List<Provider> getProviders(int numRows, String queryTerm) throws IOException,
            SolrServerException {
        List<Provider> providers = new ArrayList<Provider>();

        SolrQuery query = new SolrQuery();
        query.setQuery("HCPCS_DESCRIPTION:" + queryTerm);

        query.set("rows", numRows);
        query.setStart(0);

        // By default, sort based on which procedure is performed often
        query.setSort("BENE_UNIQUE_CNT", ORDER.desc);

        SolrProviderSource solrData = getQueryResponse(query);
        providers = solrData.body.providers;

        System.out.println("Query returned " + providers.size() + " results out of "
                + solrData.body.numFound);

        return providers;
    }

    // Internal query helper
    protected static QueryResponse doQuery(SolrQuery query) throws SolrServerException, IOException {

        SolrClient solr = null;
        QueryResponse solrJresponse = null;

        try {
            solr = new HttpSolrClient(solrQueryBase);

            System.out.println("SolrJ query = " + solrQueryBase + "/select?" + query);
            solrJresponse = solr.query(query);

        } finally {
            if (solr != null) {
                solr.close();
            }
        }

        return solrJresponse;
    }

    // Internal query helper - converts the solrJ response to our list
    // of providers and/or facets.
    protected static SolrProviderSource getQueryResponse(SolrQuery query) throws IOException,
            SolrServerException {
        SolrProviderSource response = null;

        QueryResponse solrJresponse = doQuery(query);

        response = new SolrProviderSource(solrJresponse);

        return response;
    }

    // For testing...
    public static void main(String[] args) throws IOException {
        System.out.println("testing...");

        try {

            // Test out a query
            // getQueryResponse(10, "knee");

            // Test out state count query
            System.out.println("Querying for list of states providers are in:");
            Set<String> states = SolrProviderSource.getStates();
            for (String state : states) {
                System.out.println("  State: " + state);
            }

            // Test out "get codes and descriptions query"
            System.out.println("Querying for list of procedures providers reported:");
            HashMap<String, String> codesWithDescriptions = SolrProviderSource.getProcedures(20);
            for (String key : codesWithDescriptions.keySet()) {
                String value = codesWithDescriptions.get(key);
                System.out.println("  Code " + key + " : " + value);
            }

            // Test out "get codes and descriptions query" #2
            System.out.println("Querying for list of procedures providers reported for knees:");
            codesWithDescriptions = SolrProviderSource.getProcedures(20, "knee");
            for (String key : codesWithDescriptions.keySet()) {
                String value = codesWithDescriptions.get(key);
                System.out.println("  Code " + key + " : " + value);
            }

            // Test "get provider by (any) state and procedure, sort based on
            // beneficiary unique count
            System.out.println("Querying for providers by state = * and procedure = 69210:");
            List<Provider> providers = getProviders(10, "*", "99214");
            for (Provider p : providers) {
                System.out.println("  Provider " + p.last_or_org_name + ", procedure "
                        + p.hcpcs_code + " (" + p.hcpcs_description + "): "
                        + p.beneficiaries_unique_count);
            }

            // Test "get provider by state and procedure, sort based on
            // beneficiary unique count
            System.out.println("Querying for providers by state = CA and procedure = 99213:");
            providers = getProviders(10, "FL", "99213");
            for (Provider p : providers) {
                System.out.println("  Provider " + p.last_or_org_name + ", procedure "
                        + p.hcpcs_code + " (" + p.hcpcs_description + "): "
                        + p.beneficiaries_unique_count);
            }

            // Test "get providers who perform something to do with "knee"
            System.out.println("Querying for providers who did something related to knees:");
            providers = getProviders(10, "knee");
            for (Provider p : providers) {
                System.out.println("  Provider " + p.last_or_org_name + ", procedure "
                        + p.hcpcs_code + " (" + p.hcpcs_description + "): "
                        + p.beneficiaries_unique_count);
            }

            HashMap<String, Long> stateCounts = getCountsForStates();
            for (String state : stateCounts.keySet()) {
                System.out.println(state + "(" + stateCounts.get(state) + ")");
            }

            HashMap<String, Long> zipsForTexas = getZipCountsByState("tx");
            for (String zip : zipsForTexas.keySet()) {
                System.out.println(zip + "(" + zipsForTexas.get(zip) + ")");
            }

            List<Provider> providersInTx = getProvidersByStateZipOrType("tx", null, null, 0, 10);
            for (Provider p : providersInTx) {
                System.out.println("  " + p.id);
            }

            List<Provider> providersInTxZip = getProvidersByStateZipOrType("tx", "78654", null, 0, 10);
            for (Provider p : providersInTxZip) {
                System.out.println("  " + p.id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
