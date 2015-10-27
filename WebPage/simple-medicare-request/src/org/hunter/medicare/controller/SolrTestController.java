package org.hunter.medicare.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hunter.medicare.data.Provider;
import org.hunter.medicare.data.SolrProviderSource;
import org.hunter.medicare.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles and retrieves the main requests
 */
@Controller
@RequestMapping("/solr")
public class SolrTestController {
	protected static Logger logger = Logger.getLogger("controller");

	// @Resource(name="springService")
	// private StringAppend springService;

	// Redirect to the page for entering query parameters and submitting the
	// query
	// http://localhost:8080/simple-medicare-request/hunter/main/solr/ui
	@RequestMapping(value = "/ui", method = RequestMethod.GET)
	public String getQueryPage() {
		logger.debug("Request to get query page");

		// This will resolve to /WEB-INF/jsp/solr-query-page.jsp
		return "solr-query-page";
	}

	// Get the provider results for the given state and procedure code and
	// return them in a results page.
	// http://localhost:8080/simple-medicare-request/hunter/main/solr/ui/list?state=AZ&proc=92213
	@RequestMapping(value = "/ui/list", method = RequestMethod.GET)
	public String getProviderPage(@RequestParam(value = "state", required = true) String state, // Abbreviated
																								// state
																								// value
			@RequestParam(value = "proc", required = true) String proc_code, // hcpcs
																				// code
			Model model) {
		logger.debug("Received request to query and display providers from Solr");

		List<Provider> list = new ArrayList<Provider>();

		try {
			list = getTop(state, proc_code, model);
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
			// TODO: Maybe we should redirect to an error page here?
			// Or add an error to the model?
			// For now - just return an empty array
		}

		// Add to model
		model.addAttribute("providerlist", list);

		return ("solr-listprovider-page");
	}

	/**
	 * "rest" endpoint - returns JSON rather than redirecting to a page
	 * http://localhost:8080/simple-medicare-request/hunter/main/solr/provider?
	 * state=AZ&proc=92213
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/provider", method = RequestMethod.GET)
	public @ResponseBody List<Provider> getTop(@RequestParam(value = "state", required = true) String state, // Abbreviated
																												// state
																												// value
			@RequestParam(value = "proc", required = true) String proc_code, // hcpcs
																				// code
			Model model) throws Exception {
		logger.debug("Received request to query providers in Solr");

		int num_rows = 10;

		try {
			// TODO: Should this be a service like Hunter did?
			List<Provider> providers = SolrProviderSource.getProviders(num_rows, state, proc_code);

			// @ResponseBody will automatically convert the returned value into
			// JSON format
			// You must have Jackson in your classpath
			return providers;
		} catch (Exception e) {

			logger.debug("Exception querying Solr; rethrowing...");
			throw e;
		}

	}
}
