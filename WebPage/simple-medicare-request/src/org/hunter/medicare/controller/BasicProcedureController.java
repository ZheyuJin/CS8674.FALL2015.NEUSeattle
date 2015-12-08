package org.hunter.medicare.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hunter.medicare.data.CassandraQueryResponse;
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
 * Handles basic procedure queries
 */
@Controller
@RequestMapping("/procedure")
public class BasicProcedureController {

    protected static Logger logger = Logger.getLogger("controller");

    @RequestMapping(value = "/form", method = RequestMethod.GET, params = {})
    public String getProviderQueryForm() {

        return "basicProcedureQueries";
    }

    @RequestMapping(value = "/queryMostExpensiveProc", 
    		method = RequestMethod.GET, params = { "query", "state"})
    public @ResponseBody List<Provider> getMostExpensiveByProcedureCode(
            @RequestParam(value = "query", required = false) String proc_code,
            @RequestParam(value = "state", required = true) String state)
            throws Exception {
        logger.debug("Received query request for a procedure code");


        List<Provider> list = new ArrayList<Provider>();
        int numRows = 1000;

        try {
                list = CassandraQueryResponse.getMostExpensive(numRows, state, proc_code);
                Collections.sort(list, new TopChargeSComp());

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return list;
    }

    
    @RequestMapping(value = "/queryBusiestProvider", 
    		method = RequestMethod.GET, params = { "query", "state"})
    public @ResponseBody List<Provider> queryProceduresByCode(
            @RequestParam(value = "query", required = false) String proc_code,
            @RequestParam(value = "state", required = true) String state)
            throws Exception {
        logger.debug("Received query request for a procedure code");

        List<Provider> list = new ArrayList<Provider>();
        int numRows = 1000;

        try {
                list = SolrProviderSource.getProviders(numRows, state, proc_code);
                Collections.sort(list, new TopDayCountComp());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return list;
    }
    
    // http://localhost:8080/simple-medicare-request/assessment/procedure/query?state=AZ&keyword=knee
    @RequestMapping(value = "/queryAvgProcedureCost", 
    		method = RequestMethod.GET, params = { "query", "state" })
    public @ResponseBody List<Procedure> queryProceduresByKeyword(
            @RequestParam(value = "query", required = true) String keyword,
            @RequestParam(value = "state", required = true) String state) throws Exception {
        logger.debug("Received query request for average cost for a state");
        int num_rows = 1000;
        Map<String, String> procsMap = null;

        try {
            procsMap = SolrProviderSource.getTopProceduresByKeyword(num_rows, keyword);
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
            logger.error("Exception getProcedureAvgCost; rethrowing...", e);
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
