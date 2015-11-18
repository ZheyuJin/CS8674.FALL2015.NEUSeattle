package org.hunter.medicare.controller;

import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.hunter.medicare.data.ProviderTypeNBModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/ml1")
public class MachineLearning1Controller {
	static Logger logger = Logger.getLogger("MachineLearning1Controller");

	@RequestMapping(value = "/request", method = RequestMethod.GET)
	public String getCase1Form() {
		System.out.println("Initialized ML1 query page");
		return "ml_request";
	}

	@RequestMapping(value = "/request", method = RequestMethod.GET, params = {"queries"})
	public @ResponseBody String search(
			@RequestParam(value = "queries", required = false) String queries,
			Model model) throws FileNotFoundException{
		System.out.println("Received ML1 query request");
		

		return "Hello";
	}
	
	
}