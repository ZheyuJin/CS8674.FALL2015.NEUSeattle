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

	@RequestMapping(value = "/request", method = RequestMethod.GET, 
	        params = {"amount", "sortDesc", "isPercentage" })
	public @ResponseBody List<ProcedureDetails> search(
			@RequestParam(value = "amount", required = true, defaultValue="10") int amount,
			@RequestParam(value = "sortDesc", required = false, defaultValue="true") boolean sortDesc,
			@RequestParam(value = "isPercentage", required = false, defaultValue="false") boolean isPercentage) throws Exception {
		System.out.println("Received Cassandra2/3 Request");
		System.out.println("Reading Amount as " + Integer.toString(amount) + " inOrder as " + 
					Boolean.toString(sortDesc) + " and isPercentage " + Boolean.toString(isPercentage));
		if(isPercentage){
		} else {
		}
		MainController main = new MainController();
		List<ProcedureDetails> procedures = new ArrayList<ProcedureDetails>();
        try {
            if (isPercentage)
            {
                procedures = main.getProcedurePatientCopay(sortDesc, 0, amount);
            }
            else {
                procedures = main.getProcedureMedicarePayGap(sortDesc,  0, amount);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
		
		return procedures;
	}

}