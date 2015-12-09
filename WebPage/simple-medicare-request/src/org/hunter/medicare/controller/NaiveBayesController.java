package org.hunter.medicare.controller;

import org.apache.log4j.Logger;
import org.hunter.medicare.data.SpecializationClassifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/nb")
public class NaiveBayesController {
    Logger log = Logger.getLogger("[NaiveBayesController]");

    /**
     * @return the form view.
     */
    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String getForm() {
        return "nb-form";
    }

    /**
     * 
     * @param proc_code
     * @param percentage
     * @param model
     * @return which percent of others paid more than this price.
     * @throws Exception
     */
    // http://localhost:8080/simple-medicare-request/assessment/nb/query?request=one%20two%20three
    @RequestMapping(value = "/query", method = RequestMethod.GET, params = { "request" })
    @ResponseBody
    public String[] getPrecentageByPrice_ResultJson(
            @RequestParam(value = "request", required = true) String request) throws Exception {
	String[] res = {SpecializationClassifier.predictDocs(request.toLowerCase().trim())};
	return res;
        //return request.split(" ");
    }
}
