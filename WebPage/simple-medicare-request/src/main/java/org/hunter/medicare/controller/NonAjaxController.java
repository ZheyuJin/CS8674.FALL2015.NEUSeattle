package org.hunter.medicare.controller;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hunter.medicare.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Handles and retrieves the main requests
 */
@Controller
@RequestMapping("/main/nonajax")
public class NonAjaxController {

	protected static Logger logger = Logger.getLogger("controller");
	
	@Resource(name="springService")
	private StringAppend stringAppender;
	
    /**
     * Handles and retrieves the non-AJAX, ordinary Add page
     */
    @RequestMapping(value="/add", method = RequestMethod.GET)
    public String getNonAjaxAddPage() {
    	logger.debug("Request to get page");
    
    	// This will resolve to /WEB-INF/jsp/nonajax-add-page.jsp
    	return "nonajax-add-page";
	}
    
    /**
     * Handles request for adding two numbers
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String add(@RequestParam(value="gender", required=true) String str1,
    							@RequestParam(value="state", required=true) String str2,
    							Model model) {
		logger.debug("Received request to add two numbers");
		
		// Delegate to service to do the actual adding
		String output = stringAppender.add(str1, str2);
		
		// Add to model
		model.addAttribute("appendedString", output);
		
    	// This will resolve to /WEB-INF/jsp/nonajax-add-result-page.jsp
		return "nonajax-add-result-page";
	}
    
}
