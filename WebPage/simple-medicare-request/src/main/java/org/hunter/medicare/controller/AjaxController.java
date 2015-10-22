package org.hunter.medicare.controller;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hunter.medicare.service.*;
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
@RequestMapping("/main/ajax")
public class AjaxController {

	protected static Logger logger = Logger.getLogger("controller");
	
	@Resource(name="springService")
	private StringAppend springService;
	
	/**
	 * Handles and retrieves the AJAX Add page
	 */
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String getAjaxAddPage() {
    	logger.debug("Received request to show AJAX, add page");
    	
    	// This will resolve to /WEB-INF/jsp/ajax-add-page.jsp
    	return "ajax-add-page";
	}
 
    /**
     * Handles request for adding two numbers
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public @ResponseBody String add(@RequestParam(value="gender", required=true) String str1,
    							@RequestParam(value="state", required=true) String str2,
    							Model model) {
		logger.debug("Received request to add two numbers");
		
		// Delegate to service to do the actual adding
		String output = springService.add(str1, str2);
		
		// @ResponseBody will automatically convert the returned value into JSON format
		// You must have Jackson in your classpath
		return output;
	}
}
