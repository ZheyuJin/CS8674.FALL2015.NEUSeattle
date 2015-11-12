package org.hunter.medicare.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hunter.medicare.data.CassandraQueryResponse;
import org.hunter.medicare.data.Procedure;
import org.hunter.medicare.data.Provider;
import org.hunter.medicare.data.SolrProviderSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
            List<Provider> providers = CassandraQueryResponse.getInstance().getMostExpensive(
                    num_rows, state, proc_code); // mock
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
    // http://localhost:8080/simple-medicare-request/assessment/main/provider/count/states/
    @RequestMapping(value = "/provider/count/states", method = RequestMethod.GET)
    @ResponseBody
    public FacetedCount getProviderCountsPerState() throws Exception {

        // TODO: remove this (but Hunter might need it early on for UI)
        boolean useMock = true;

        try {
            FacetedCount ret = new FacetedCount();
            ret.facetType = FacetedCount.FacetType.State;

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
                ret.facetedCount = new HashMap<String, Long>();
                ret.facetedCount.put("tx", 135L);
                ret.facetedCount.put("fl", 70L);
                ret.facetedCount.put("nv", 7L);
                ret.facetedCount.put("ny", 86L);

            } else {
                // Query Solr for the provider count per state
                // TODO: maybe we should sort these?
                Map<String, Long> providerCounts = SolrProviderSource.getCountsForStates();
                ret.facetedCount = providerCounts;
            }

            return ret;

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Exception querying Solr; rethrowing...");
            throw e;
        }

    }
}

// Container class for returning a faceted count to the UI
class FacetedCount {

    // Valid facet types
    public enum FacetType {
        State, Zip, ProviderType
    }

    // Indicates the type of facet contained in the facetedCount.
    public FacetType facetType;

    // This indicates what filters were in place for this
    // faceted query - for example, if we filtered on
    // state, and we are returning provider types for WA,
    // this would have the entry "State", "WA".
    // If there are more than one entry, all have been applied
    // (ie: treat these filters as an AND, not an OR)
    // If this is empty/null, then no filters were used.
    public Map<String, String> facetFilters;

    public Map<String, Long> facetedCount;
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
        return (o1.providerDetails.averageSubmittedChargeAmount - o2.providerDetails.averageSubmittedChargeAmount) > 0 ? -1
                : 1;
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