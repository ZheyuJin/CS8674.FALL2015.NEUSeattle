package org.hunter.medicare.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hunter.medicare.data.CassandraQueryResponse;
import org.hunter.medicare.data.CountedPropertyValue;
import org.hunter.medicare.data.FacetType;
import org.hunter.medicare.data.FacetedCount;
import org.hunter.medicare.data.FacetedProviderResult;
import org.hunter.medicare.data.FilterPair;
import org.hunter.medicare.data.Procedure;
import org.hunter.medicare.data.ProcedureDetails;
import org.hunter.medicare.data.Provider;
import org.hunter.medicare.data.SolrProviderSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author Zheyu
 *
 *         Main controller for Case 1,2,3. Dispatches control to each part.
 */

@Controller
@RequestMapping("/main")
public class MainController {
    static Logger logger = Logger.getLogger("MainController");

    @RequestMapping(value = "/case1-from", method = RequestMethod.GET)
    public String getCase1Form() {
        return "case1-form";
    }

    @RequestMapping(value = "/case2-from", method = RequestMethod.GET)
    public String getCase2Form() {
        return "case2-form";
    }

    @RequestMapping(value = "/case3-from", method = RequestMethod.GET)
    public String getCase3Form() {
        return "case3-form";
    }

    /**
     * TODO: confusing with case 1. get top 10 most expensive doctors given
     * state and hpcps code.
     * 
     * @param state
     *            e.g. WA
     * @param proc_code
     *            HPCPS code
     * @param model
     * @return
     */
    @RequestMapping(value = "/case1-result-jsp", method = RequestMethod.GET)
    public String getCase1_ResultForm(@RequestParam(value = "state", required = true) String state,
            @RequestParam(value = "proc_code", required = true) String proc_code, Model model) {
        List<Provider> list = new ArrayList<Provider>();

        try {
            list = getTopExpensiveProvider(state, proc_code);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add to model
        model.addAttribute("providerlist", list);

        return "ProviserListView";
    }

    /**
     * 
     * @param state
     * @param proc_code
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/case1-result-json", method = RequestMethod.GET)
    @ResponseBody
    public List<Provider> getTopExpensiveProvider(
            @RequestParam(value = "state", required = true) String state,
            @RequestParam(value = "proc_code", required = true) String proc_code) throws Exception {
        int num_rows = 10;

        try {
            List<Provider> providers = CassandraQueryResponse.getMostExpensive(num_rows, state,
                    proc_code); // mock
            Collections.sort(providers, new TopChargeSComp());

            return providers;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Exception querying Solr; rethrowing...");
            throw e;
        }

    }

    @RequestMapping(value = "/case2-result-jsp", method = RequestMethod.GET)
    public String getCase2_ResultForm(@RequestParam(value = "state", required = true) String state,
            @RequestParam(value = "proc_code", required = true) String proc_code, Model model) {
        List<Provider> list = new ArrayList<Provider>();

        try {
            list = getTopBusyProvider(state, proc_code);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add to model
        model.addAttribute("providerlist", list);

        // reuse case1's view since parameter is the same, will be parsed
        // correctly.
        return "ProviserListView";
    }

    /**
     * "rest" endpoint - returns JSON rather than redirecting to a page
     * http://localhost:8080/simple-medicare-request/hunter/main/solr/provider?
     * state=AZ&proc=92213
     * 
     * @throws Exception
     */
    @RequestMapping(value = "/case2-result-json", method = RequestMethod.GET)
    @ResponseBody
    public List<Provider> getTopBusyProvider(
            @RequestParam(value = "state", required = true) String state,
            @RequestParam(value = "proc_code", required = true) String proc_code) throws Exception {
        int num_rows = 10;

        try {
            // TODO: Should this be a service like Hunter did?
            List<Provider> providers = SolrProviderSource.getProviders(num_rows, state, proc_code);

            // sort
            Collections.sort(providers, new TopDayCountComp());

            return providers;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Exception querying Solr; rethrowing...");
            throw e;
        }

    }

    /**
     * @throws Exception
     */
    @RequestMapping(value = "/case3-result-json", method = RequestMethod.GET)
    @ResponseBody
    public List<Procedure> getProcedureAvgCost(
            @RequestParam(value = "state", required = true) String state,
            @RequestParam(value = "proc_desc", required = true) String proc_desc) throws Exception {
        int num_rows = 10;

        try {
            Map<String, String> procsMap = SolrProviderSource.getProcedures(num_rows, proc_desc);
            System.err.println("procsMap key:" + procsMap.keySet());
            // ID,
            Map<String, Double> avgPriceMap = CassandraQueryResponse
                    .getCodeToAvgCostMappingForState(procsMap.keySet(), state);
            System.err.println("avgPriceMap key:" + avgPriceMap.keySet());
            System.err.println("avgPriceMap values:" + avgPriceMap.values());
            System.err.println("avgPriceMap :" + avgPriceMap);

            List<Procedure> ret = new ArrayList<>();
            for (String procId : procsMap.keySet()) {
                String desc = procsMap.get(procId);
                double avgCost = avgPriceMap.get(procId);
                if (avgCost > 0)
                    ret.add(new Procedure(procId, desc, avgCost, state));
            }

            // sort procedures.
            Collections.sort(ret, new ProcedureComp());

            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Exception querying Solr; rethrowing...");
            throw e;
        }

    }

    @RequestMapping(value = "/case3-result-jsp", method = RequestMethod.GET)
    public String getCase3_ResultForm(@RequestParam(value = "state", required = true) String state,
            @RequestParam(value = "proc_desc", required = true) String proc_desc, Model model) {
        List<Procedure> list = new ArrayList<>();

        try {
            list = getProcedureAvgCost(state, proc_desc);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add to model
        model.addAttribute("providerlist", list);

        // reuse case1's view since parameter is just a list, will be parsed
        // correctly.
        return "ProviserListView";
    }

    /**
     * Returns JSON
     * 
     * @throws Exception
     */
    // http://localhost:8080/simple-medicare-request/assessment/main/provider/
    @RequestMapping(value = "/provider", method = RequestMethod.GET)
    @ResponseBody
    public FacetedProviderResult getProvidersWithFacets(
            @RequestParam(value = "state", required = false, defaultValue = "") String state,
            @RequestParam(value = "zip", required = false, defaultValue = "") String zip,
            @RequestParam(value = "provider_type", required = false, defaultValue = "") String provider_type,
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "facet", required = false, defaultValue = "Query") String facetType,
            @RequestParam(value = "start", required = false, defaultValue = "-1") Integer start,
            @RequestParam(value = "end", required = false, defaultValue = "-1") Integer end)
                    throws Exception {

        FacetedProviderResult ret = new FacetedProviderResult();

        // Input parameter processing...

        // If they don't have start/end set, default to first 10 (= 0-9)
        Integer startRow = 0;
        Integer numRows = 10; // Default at 10 rows (inclusive)

        // Note: start = end = 0 is one row (the first one)
        if (start >= 0) {
            startRow = start;

            if (end >= start) {
                numRows = end - start + 1;
            }
        }
        // In result, start row is always what they requested (zero based)
        // The size of provider list = number returned in this "slice"
        // The total number = full number of results available
        // Notice that a query with start > total will return nothing
        ret.startRow = startRow;

        ret.facets = new FacetedCount();

        ret.providers = new ArrayList<Provider>();
        ret.facets.facetedCount = new ArrayList<CountedPropertyValue>();

        if (!query.isEmpty() || !state.isEmpty() || !zip.isEmpty() || !provider_type.isEmpty()) {
            ret.facets.facetFilters = new ArrayList<FilterPair>();
        }
        if (query != null && !query.isEmpty()) {
            ret.facets.facetFilters.add(new FilterPair(FacetType.Query.toString(), query));
        }
        if (zip != null && !zip.isEmpty()) {
            ret.facets.facetFilters.add(new FilterPair(FacetType.Zip.toString(), zip));
        }
        if (state != null && !state.isEmpty()) {
            ret.facets.facetFilters.add(new FilterPair(FacetType.State.toString(), state));
        }
        if (provider_type != null && !provider_type.isEmpty()) {
            ret.facets.facetFilters
                    .add(new FilterPair(FacetType.ProviderType.toString(), provider_type));
        }

        // TODO: remove this (but Hunter might need it early on for UI)
        boolean useMock = false;
        try {

            if (useMock) {

                // We set the start = to the input.. the size of the provider
                // list is the number returned
                // starting at that start row (can be zero)
                // For the mock, this is a fake value (obviously)
                ret.numProvidersTotal = startRow + ret.providers.size() - 1L;

                // ToDo: mock some providers in the list too?

                ret.facets.facetType = FacetType.State;

                ret.facets.facetedCount.add(new CountedPropertyValue("tx", 135L));
                ret.facets.facetedCount.add(new CountedPropertyValue("fl", 70L));
                ret.facets.facetedCount.add(new CountedPropertyValue("nv", 7L));
                ret.facets.facetedCount.add(new CountedPropertyValue("ny", 86L));

            } else {
                // Query Solr for the provider count per state
                // TODO: maybe we should sort these?
                FacetType facetOn = FacetType.valueOf(facetType);
                ret.facets.facetType = facetOn;

                ret.numProvidersTotal = SolrProviderSource.getProvidersWithFacets(ret.providers,
                        facetOn, ret.facets.facetedCount, ret.facets.facetFilters, startRow,
                        numRows);
            }

            return ret;

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Exception querying Solr; rethrowing...");
            throw e;
        }

    }

    /**
     * Returns JSON
     * 
     * @throws Exception
     */
    // http://localhost:8080/simple-medicare-request/assessment/main/provider/count/states/
    @RequestMapping(value = "/provider/count/states", method = RequestMethod.GET)
    @ResponseBody
    public FacetedCount getProviderCountsPerState() throws Exception {

        // TODO: remove this (but Hunter might need it early on for UI)
        boolean useMock = false;

        try {
            FacetedCount ret = new FacetedCount();
            ret.facetType = FacetType.State;

            if (useMock) {
                // JSON:
                // {
                // facetType: "State",
                // facetFilters: null,
                // facetedCount: {
                // tx: 135,
                // fl: 70,
                // nv: 7,
                // ny: 86
                // }
                // }

                // Here's our hint that this is mock data
                ret.facetedCount.add(new CountedPropertyValue("mk", 19999L));

                ret.facetedCount = new ArrayList<CountedPropertyValue>();
                ret.facetedCount.add(new CountedPropertyValue("tx", 135L));
                ret.facetedCount.add(new CountedPropertyValue("fl", 70L));
                ret.facetedCount.add(new CountedPropertyValue("nv", 7L));
                ret.facetedCount.add(new CountedPropertyValue("ny", 86L));

            } else {
                // Query Solr for the provider count per state
                // TODO: maybe we should sort these?
                List<CountedPropertyValue> providerCounts = SolrProviderSource.getCountsForStates();
                ret.facetedCount = providerCounts;
            }

            return ret;

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Exception querying Solr; rethrowing...");
            throw e;
        }

    }

    /**
     * Returns JSON
     * 
     * @throws Exception
     */
    // http://localhost:8080/simple-medicare-request/assessment/main/procedure/paygap/
    @RequestMapping(value = "/procedure/paygap", method = RequestMethod.GET)
    @ResponseBody
    public List<ProcedureDetails> getProcedureMedicarePayGap(
            @RequestParam(value = "top", required = false, defaultValue = "true") boolean sortDesc,
            @RequestParam(value = "start", required = false, defaultValue = "-1") int start,
            @RequestParam(value = "end", required = false, defaultValue = "-1") int end)
                    throws Exception {

        // Input parameter processing...

        // If they don't have start/end set, default to first 10 (= 0-9)
        Integer startRow = 0;
        Integer numRows = 10; // Default at 10 rows (inclusive)

        // Note: start = end = 0 is one row (the first one)
        if (start >= 0) {
            startRow = start;

            if (end >= start) {
                numRows = end - start + 1;
            }
        }

        try {

            return CassandraQueryResponse.getChargedMedicarePayGap(sortDesc, startRow, numRows);

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Exception querying Cassandra; rethrowing...");
            throw e;
        }

    }

    /**
     * Returns JSON
     * 
     * @throws Exception
     */
    // http://localhost:8080/simple-medicare-request/assessment/main/procedure/copay/
    @RequestMapping(value = "/procedure/copay", method = RequestMethod.GET)
    @ResponseBody
    public List<ProcedureDetails> getProcedurePatientCopay(
            @RequestParam(value = "top", required = false, defaultValue = "true") boolean sortDesc,
            @RequestParam(value = "start", required = false, defaultValue = "-1") int start,
            @RequestParam(value = "end", required = false, defaultValue = "-1") int end)
                    throws Exception {

        // Input parameter processing...

        // If they don't have start/end set, default to first 10 (= 0-9)
        Integer startRow = 0;
        Integer numRows = 10; // Default at 10 rows (inclusive)

        // Note: start = end = 0 is one row (the first one)
        if (start >= 0) {
            startRow = start;

            if (end >= start) {
                numRows = end - start + 1;
            }
        }

        try {

            return CassandraQueryResponse.getPatientResponsibility(sortDesc, startRow, numRows);

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Exception querying Cassandra; rethrowing...");
            throw e;
        }
    }

    // Test the exception page
    @RequestMapping(value = "/exception", method = RequestMethod.GET)
    public void getExceptionPage() throws Exception {
        throw new Exception("This is an error");
    }

    @ExceptionHandler({ Exception.class })
    public String genericError() {
        // Returns the logical view name of an error page, passed to
        // the view-resolver(s) in usual way.
        // See
        // https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
        // for more options.
        return "genericError";
    }
}

/**
 * Used for sorting {@link Procedure} with descending avg cost.
 * 
 * @author Zheyu
 *
 */
class ProcedureComp implements Comparator<Procedure> {

    @Override
    public int compare(Procedure p1, Procedure p2) {
        return (p1.avgCost - p2.avgCost) > 0 ? 1 : -1;
    }

}

/**
 * sort by averageSubmittedChargeAmount in {@link Provider#providerDetails} ,
 * descending order
 */
class TopChargeSComp implements Comparator<Provider> {
    @Override
    public int compare(Provider o1, Provider o2) {
        return (o1.providerDetails.averageSubmittedChargeAmount
                - o2.providerDetails.averageSubmittedChargeAmount) > 0 ? -1 : 1;
    }
}

/**
 * sort on beneficiaries_day_service_count field, descending order.
 */
class TopDayCountComp implements Comparator<Provider> {
    @Override
    public int compare(Provider p1, Provider p2) {
        return (p2.beneficiaries_day_service_count - p1.beneficiaries_day_service_count) > 0 ? 1
                : -1;
    }
}