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
import org.hunter.medicare.data.Procedure;
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

    // http://localhost:8080/simple-medicare-request/assessment/main/case1-result-jsp?state=AZ&proc_code=99213
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
     * @throws Exception
     */
    @RequestMapping(value = "/case1-result-jsp", method = RequestMethod.GET)
    public String getCase1_ResultForm(@RequestParam(value = "state", required = true) String state,
            @RequestParam(value = "proc_code", required = true) String proc_code, Model model)
            throws Exception {
        List<Provider> list = new ArrayList<Provider>();

        try {
            list = getTopExpensiveProvider(state, proc_code);
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Exception querying Cassandra for getTopExpensiveProvider; rethrowing...");
            throw e;
        }

        // Add to model
        model.addAttribute("providerlist", list);

        return "ProviserListView";
    }

    // http://localhost:8080/simple-medicare-request/assessment/main/case1-result-json?state=AZ&proc_code=99213
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
                    proc_code);
            Collections.sort(providers, new TopChargeSComp());

            return providers;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Exception querying Cassandra for getMostExpensive providers; rethrowing...");
            throw e;
        }

    }

    // http://localhost:8080/simple-medicare-request/assessment/main/case2-result-jsp?state=AZ&proc_code=99213
    @RequestMapping(value = "/case2-result-jsp", method = RequestMethod.GET)
    public String getCase2_ResultForm(@RequestParam(value = "state", required = true) String state,
            @RequestParam(value = "proc_code", required = true) String proc_code, Model model)
            throws Exception {
        List<Provider> list = new ArrayList<Provider>();

        try {
            list = getTopBusyProvider(state, proc_code);

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Exception querying Solr for getTopBusy providers; rethrowing...");
            throw e;
        }

        // Add to model
        model.addAttribute("providerlist", list);

        // reuse case1's view since parameter is the same, will be parsed
        // correctly.
        return "ProviserListView";
    }

    // http://localhost:8080/simple-medicare-request/assessment/main/case2-result-json?state=AZ&proc_code=99213
    /**
     * "rest" endpoint - returns JSON rather than redirecting to a page
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

    // http://localhost:8080/simple-medicare-request/assessment/main/case3-result-json?state=AZ&proc_desc=knee
    /**
     * @throws Exception
     */
    @RequestMapping(value = "/case3-result-json", method = RequestMethod.GET)
    @ResponseBody
    public List<Procedure> getProcedureAvgCost(
            @RequestParam(value = "state", required = true) String state,
            @RequestParam(value = "proc_desc", required = true) String proc_desc) throws Exception {
        int num_rows = 10;
        Map<String, String> procsMap = null;

        try {
            procsMap = SolrProviderSource.getTopProceduresByKeyword(num_rows, proc_desc);
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
            logger.debug("Exception querying Solr or Cassandra; rethrowing...");
            throw e;
        }

    }

    // http://localhost:8080/simple-medicare-request/assessment/main/case3-result-jsp?state=AZ&proc_desc=knee
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
    // TODO: check if we still need this, think we flipped over to the general
    // faceted method in paging controller.
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

    // http://localhost:8080/simple-medicare-request/assessment/main/exception
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