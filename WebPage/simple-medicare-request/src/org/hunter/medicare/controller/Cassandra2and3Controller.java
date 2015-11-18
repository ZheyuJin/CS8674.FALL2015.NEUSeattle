package org.hunter.medicare.controller;

import java.util.*;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.hunter.medicare.data.*;
import org.hunter.medicare.controller.*;

@Controller
@RequestMapping("/cassandra")
public class Cassandra2and3Controller {
	static Logger logger = Logger.getLogger("CassandraUseCase2and3Controller");

	@RequestMapping(value = "/request", method = RequestMethod.GET)
	public String getCase1Form() {
		System.out.println("Initialized Cassandra query page");
		return "cassandra23-form";
	}

	@RequestMapping(value = "/request", method = RequestMethod.GET, params = {
			"amount", "inOrder", "isPercentage" })
	public @ResponseBody List<Procedure> search(
			@RequestParam(value = "amount", required = false) int amount,
			@RequestParam(value = "inOrder", required = false) boolean inOrder,
			@RequestParam(value = "isPercentage", required = false) boolean isPercentage,

			Model model) {
		System.out.println("Received Cassandra2/3 Request");
		System.out.println("Reading Amount as " + Integer.toString(amount) + " inOrder as " + 
					Boolean.toString(inOrder) + " and isPercentage " + Boolean.toString(isPercentage));
		if(isPercentage){
		} else {
		}
		MainController main = new MainController();
		List<Procedure> procedures = new ArrayList<Procedure>();
        try {
            procedures = main.getProcedureAvgCost("", "");

        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return procedures;
	}

}