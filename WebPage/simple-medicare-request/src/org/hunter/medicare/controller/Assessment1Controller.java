package org.hunter.medicare.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.hunter.medicare.data.CassandraQueryResponse;
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
@RequestMapping("/assessment")
public class Assessment1Controller {

    protected static Logger logger = Logger.getLogger("controller");

    @RequestMapping(value = "/submit", method = RequestMethod.GET, params = {})
    public String getAjaxAddPage() {
	
	return "assessment-form";
    }

    @RequestMapping(value = "/submit", method = RequestMethod.GET, params = {"proc_code", "state", "use_case"})
    public @ResponseBody List<Provider> search(
    		@RequestParam(value = "proc_code", required = false) String proc_code,
    		@RequestParam(value = "state", required = true) String state, 
    		@RequestParam(value = "use_case", required = true) String use_case, Model model)
		    throws IOException, SolrServerException {
	logger.debug("Received submit request");

	List<Provider> list = new ArrayList<Provider>();
	
	switch(use_case){
	case "case_1" : list = CassandraQueryResponse.getInstance().getMostExpensive(10, state, proc_code); // mock
    				Collections.sort(list, new TopChargeSComp());
					break;
	case "case_2" : list = SolrProviderSource.getProviders(10, state, proc_code);
					Collections.sort(list, new TopDayCountComp());
					break;
	}

	return list;
    }
    
    @RequestMapping(value = "/submit", method = RequestMethod.GET, params = {"keyword", "state", "use_case"})
    public @ResponseBody String submit(
    		@RequestParam(value = "keyword", required = true) String keyword,
    		@RequestParam(value = "state", required = true) String state, 
    		@RequestParam(value = "use_case", required = true) String use_case, Model model)
		    throws IOException, SolrServerException {
	logger.debug("Received submit request for case 3");

	return "WHATEVER GOES HERE IN CASE 3";
    }
    
}