
package org.hunter.medicare.controller;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;
import org.hunter.medicare.gaussian.GMM;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/nb")
public class NaiveBayesController {
    Logger log = Logger.getLogger("[IsRipoffController]");

    /**
     * @return the form view.
     */
    @RequestMapping(value = "/request", method = RequestMethod.GET)
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
    @RequestMapping(value = "/request", method = RequestMethod.GET, params = {"request"})
    @ResponseBody
    public String[] getPrecentageByPrice_ResultJson(
            @RequestParam(value = "request", required = true) String request
            ) throws Exception {

    	return request.split(" ");
    }

    @ExceptionHandler({ Exception.class })
    public String genericError() {
        return "genericError";
    }

}
