package org.hunter.medicare.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hunter.medicare.data.CountedPropertyValue;
import org.hunter.medicare.data.FacetType;
import org.hunter.medicare.data.FacetedCount;
import org.hunter.medicare.data.SolrProviderSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author Zheyu
 *
 *         Main controller for Case 1,2,3. Dispatches control to each part.
 */

@Controller
@RequestMapping("/main")
public class MainController {
    static Logger logger = Logger.getLogger("MainController");

    /**
     * Returns JSON
     * 
     * @throws Exception
     */
    // TODO: check if we still need this, think we flipped over to the general
    // faceted method in paging controller.
    @RequestMapping(value = "/provider/count/states", method = RequestMethod.GET)
    @ResponseBody
    public FacetedCount getProviderCountsPerState() throws Exception {

        // TODO: remove this (but Hunter might need it early on for UI)
        boolean useMock = false;

        try {
            FacetedCount ret = new FacetedCount();
            ret.facetType = FacetType.State;

            if (useMock) {
                // JSON:
                // {
                // facetType: "State",
                // facetFilters: null,
                // facetedCount: {
                // tx: 135,
                // fl: 70,
                // nv: 7,
                // ny: 86
                // }
                // }

                // Here's our hint that this is mock data
                ret.facetedCount.add(new CountedPropertyValue("mk", 19999L));

                ret.facetedCount = new ArrayList<CountedPropertyValue>();
                ret.facetedCount.add(new CountedPropertyValue("tx", 135L));
                ret.facetedCount.add(new CountedPropertyValue("fl", 70L));
                ret.facetedCount.add(new CountedPropertyValue("nv", 7L));
                ret.facetedCount.add(new CountedPropertyValue("ny", 86L));

            } else {
                // Query Solr for the provider count per state
                // TODO: maybe we should sort these?
                List<CountedPropertyValue> providerCounts = SolrProviderSource.getCountsForStates();
                ret.facetedCount = providerCounts;
            }

            return ret;

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception getProviderCountsPerState; rethrowing...");
            throw e;
        }

    }

    // http://localhost:8080/simple-medicare-request/assessment/main/exception
    // Test the exception page
    @RequestMapping(value = "/exception", method = RequestMethod.GET)
    public void getExceptionPage() throws Exception {
        throw new Exception("This is an error");
    }

    @ExceptionHandler({ Exception.class })
    public String genericError() {
        // Returns the logical view name of an error page, passed to
        // the view-resolver(s) in usual way.
        // See
        // https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
        // for more options.
        return "genericError";
    }
}
