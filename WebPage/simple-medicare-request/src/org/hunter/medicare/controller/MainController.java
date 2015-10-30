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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

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
    public List<Provider> getTopExpensiveProvider(@RequestParam(value = "state", required = true) String state,
	    @RequestParam(value = "proc_code", required = true) String proc_code) throws Exception {
	int num_rows = 10;

	try {
	    List<Provider> providers = CassandraQueryResponse.getInstance().getMostExpensive(num_rows, state,
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
    public List<Provider> getTopBusyProvider(@RequestParam(value = "state", required = true) String state,
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
    public List<Procedure> getProcedureAvgCost(@RequestParam(value = "state", required = true) String state,
	    @RequestParam(value = "proc_desc", required = true) String proc_desc) throws Exception {
	int num_rows = 10;

	try {
	    Map<String, String> procsMap = SolrProviderSource.getProcedures(num_rows, proc_desc);
	    System.err.println("procsMap key:" + procsMap.keySet());
	    // ID, 
	    Map<String, Double> avgPriceMap = CassandraQueryResponse.getCodeToAvgCostMappingForState(procsMap.keySet(),
		    state);
	    System.err.println("avgPriceMap key:" + avgPriceMap.keySet());
	    System.err.println("avgPriceMap values:" + avgPriceMap.values());
	    System.err.println("avgPriceMap :" + avgPriceMap);

	    List<Procedure> ret = new ArrayList<>();
	    for (String procId : procsMap.keySet()) {
		String desc = procsMap.get(procId);
		double avgCost = avgPriceMap.get(procId);
		if(avgCost >0)
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
	return (o1.providerDetails.averageSubmittedChargeAmount - o2.providerDetails.averageSubmittedChargeAmount) > 0
		? -1 : 1;
    }
}

/**
 * sort on beneficiaries_day_service_count field, descending order.
 */
class TopDayCountComp implements Comparator<Provider> {
    @Override
    public int compare(Provider p1, Provider p2) {
	return (p2.beneficiaries_day_service_count - p1.beneficiaries_day_service_count) > 0 ? 1 : -1;
    }
}