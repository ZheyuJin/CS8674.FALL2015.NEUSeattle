package org.hunter.medicare.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.hunter.medicare.data.CassandraQueryResponse;
import org.hunter.medicare.data.Procedure;
import org.hunter.medicare.data.Provider;
import org.hunter.medicare.data.SolrProviderSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

    @RequestMapping(value = "/submit", method = RequestMethod.GET, params = { "proc_code", "state",
            "use_case" })
    public @ResponseBody List<Provider> search(
            @RequestParam(value = "proc_code", required = false) String proc_code,
            @RequestParam(value = "state", required = true) String state,
            @RequestParam(value = "use_case", required = true) String use_case, Model model)
            throws Exception {
        logger.debug("Received submit request");

        List<Provider> list = new ArrayList<Provider>();

        try {
            switch (use_case) {
            case "case_1":
                list = CassandraQueryResponse.getMostExpensive(10, state, proc_code); // mock
                Collections.sort(list, new TopChargeSComp());
                break;
            case "case_2":
                list = SolrProviderSource.getProviders(10, state, proc_code);
                Collections.sort(list, new TopDayCountComp());
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return list;
    }

    @RequestMapping(value = "/submit", method = RequestMethod.GET, params = { "keyword", "state",
            "use_case" })
    public @ResponseBody List<Procedure> submit(
            @RequestParam(value = "keyword", required = true) String keyword,
            @RequestParam(value = "state", required = true) String state,
            @RequestParam(value = "use_case", required = true) String use_case, Model model)
            throws Exception {
        logger.debug("Received submit request for case 3");
        List<Procedure> output = new ArrayList<Procedure>();
        try {
            output = access.getProcedureAvgCost(state, keyword);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return output;
    }

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
