package org.hunter.medicare.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
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
    public String getCase2Form() {
	return "case1-form";
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
    public String getCase2_ResultForm(@RequestParam(value = "state", required = true) String state,
	    @RequestParam(value = "proc_code", required = true) String proc_code, Model model) {
	List<Provider> list = new ArrayList<Provider>();

	try {
	    list = getTopExpensiveProvider(state, proc_code);
	    /**
	     * sort on beneficiaries_day_service_count field, descending order.
	     */
	    class TopDayCountComp implements Comparator<Provider> {
		@Override
		public int compare(Provider p1, Provider p2) {
		    return (p2.beneficiaries_day_service_count - p1.beneficiaries_day_service_count) > 0 ? 1 : -1;
		}
	    }
	    // sort
	    Collections.sort(list, new TopDayCountComp());
	} catch (Exception e) {
	    e.printStackTrace();
	}

	// Add to model
	model.addAttribute("providerlist", list);

	return "case1-result";
    }

    /**
     * "rest" endpoint - returns JSON rather than redirecting to a page
     * http://localhost:8080/simple-medicare-request/hunter/main/solr/provider?
     * state=AZ&proc=92213
     * 
     * @throws Exception
     */
    @RequestMapping(value = "/case1-result-json", method = RequestMethod.GET)
    @ResponseBody
    public List<Provider> getTopExpensiveProvider(@RequestParam(value = "state", required = true) String state,
	    @RequestParam(value = "proc_code", required = true) String proc_code) throws Exception {
	int num_rows = 10;

	try {
	    // TODO: Should this be a service like Hunter did?
	    List<Provider> providers = SolrProviderSource.getProviders(num_rows, state, proc_code);
	    return providers;
	} catch (Exception e) {
	    e.printStackTrace();
	    logger.debug("Exception querying Solr; rethrowing...");
	    throw e;
	}

    }
}
