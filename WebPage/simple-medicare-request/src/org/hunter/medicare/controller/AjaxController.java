package org.hunter.medicare.controller;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.hunter.medicare.data.Provider;
import org.hunter.medicare.data.SolrProviderSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Handles and retrieves the main requests
 */
@Controller
@RequestMapping("/ajax")
public class AjaxController {

    protected static Logger logger = Logger.getLogger("controller");

    @RequestMapping(value = "/submit", method = RequestMethod.GET)
    public String getAjaxAddPage() {
	logger.debug("Received request to show AJAX, submit page");

	return "ajax-add-page";
    }

    @RequestMapping(value = "/submit", method = RequestMethod.GET)
    public @ResponseBody AjaxResponseBody submit(@RequestParam(value = "proc_code", required = true) String proc_code,
	    @RequestParam(value = "state", required = true) String state, Model model)
		    throws IOException, SolrServerException {
	logger.debug("Received submit request");

	AjaxResponseBody output = new AjaxResponseBody();

	output.setResults(SolrProviderSource.getProviders(10, state, proc_code));
	//SolrProviderSource.getProviders(num_rows, state, proc_code)
	//output.setResults(providers);

	logger.debug("returning request");
	return output;
    }
}
