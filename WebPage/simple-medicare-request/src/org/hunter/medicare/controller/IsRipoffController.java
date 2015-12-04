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

/**
 * Using Normal distribution to detect outlier. Loading trained model form local
 * file, which includes data for each of 5000+ procedures.
 * 
 * @author Zheyu
 *
 */
@Controller
@RequestMapping("/ripoff")
public class IsRipoffController {
    Logger log = Logger.getLogger("[IsRipoffController]");

    /**
     * @return the form view.
     */
    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String getForm() {
        return "ripoff-form";
    }

    /**
     * 
     * @param proc_code
     * @param percentage
     * @param model
     * @return which percent of others paid more than this price.
     * @throws Exception
     */
    @RequestMapping(value = "/result-json", method = RequestMethod.GET)
    @ResponseBody
    public double getPrecentageByPrice_ResultJson(
            @RequestParam(value = "proc_code", required = true) String proc_code,
            @RequestParam(value = "price", required = true) double price) throws Exception {

        log.info(String.format("proc_code :%s price: %f", proc_code, price));
        /* load GMM from file */
        GMM gmm = new GMM(null);
        gmm.load();
        NormalDistribution nd = gmm.getIndividualModel(proc_code);

        /*
         * which percent of others paid more than this price
         */
        double percentage = 1 - nd.cumulativeProbability(price);

        return 100 * percentage;
    }

    @ExceptionHandler({ Exception.class })
    public String genericError() {
        /*
         * Returns the logical view name of an error page, passed to the
         * view-resolver(s) in usual way. See
         * https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
         * for more options.
         */
        return "genericError";
    }

}
