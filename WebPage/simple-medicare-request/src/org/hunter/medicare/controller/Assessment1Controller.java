package org.hunter.medicare.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
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
 * Handles and retrieves the main requests
 */
@Controller
@RequestMapping("/assessment")
public class Assessment1Controller {

	private MainController access = new MainController();
	
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
    public @ResponseBody List<Procedure> submit(
    		@RequestParam(value = "keyword", required = true) String keyword,
    		@RequestParam(value = "state", required = true) String state, 
    		@RequestParam(value = "use_case", required = true) String use_case, Model model)
		    throws IOException, SolrServerException {
	logger.debug("Received submit request for case 3");
	List<Procedure> output = new ArrayList<Procedure>();
	try {
		output = access.getProcedureAvgCost(state, keyword);
	} catch (Exception e) {
		e.printStackTrace();
	}
			return output;
    }
    
    public static List<SampleProcedure> procGen(){
    	List<SampleProcedure> list = new ArrayList<SampleProcedure>();
    	SampleProcedure temp = new SampleProcedure("99123", "Knee injury", 453463, "WA");
    	list.add(temp);
    	temp = new SampleProcedure("91223", "Bullet to the knee", 45, "FL");
    	list.add(temp);
    	temp = new SampleProcedure("99135", "Extra knee", 234, "GA");
    	list.add(temp);
    	temp = new SampleProcedure("59123", "Dislocated knee", 99, "AL");
    	list.add(temp);

    	return list;
    }
    
}
