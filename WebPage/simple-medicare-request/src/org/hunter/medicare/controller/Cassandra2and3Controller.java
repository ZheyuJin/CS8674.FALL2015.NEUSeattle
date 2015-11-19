package org.hunter.medicare.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hunter.medicare.data.ProcedureDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/cassandra")
public class Cassandra2and3Controller {
    static Logger logger = Logger.getLogger("CassandraUseCase2and3Controller");

    @RequestMapping(value = "/request", method = RequestMethod.GET)
    public String getCase1Form() {
        System.out.println("Initialized Cassandra query page");
        return "cassandra23-form";
    }

    @RequestMapping(value = "/request", method = RequestMethod.GET, params = { "numRows", "start",
            "sortDesc", "isPercentage" })
    public @ResponseBody List<ProcedureDetails> search(
            @RequestParam(value = "numRows", required = true, defaultValue = "10") int numRows,
            @RequestParam(value = "start", required = false, defaultValue = "true") int start,
            @RequestParam(value = "sortDesc", required = false, defaultValue = "true") boolean sortDesc,
            @RequestParam(value = "isPercentage", required = false, defaultValue = "false") boolean isPercentage)
            throws Exception {
        System.out.println("Received Cassandra2/3 Request");
        System.out.println("Reading numRows as " + Integer.toString(numRows) + " start as "
                + Integer.toString(start) + " sortDesc as " + Boolean.toString(sortDesc)
                + " and isPercentage " + Boolean.toString(isPercentage));
        if (isPercentage) {
        } else {
        }
        MainController main = new MainController();
        List<ProcedureDetails> procedures = new ArrayList<ProcedureDetails>();
        try {
            int end = start + numRows - 1;

            if (isPercentage) {
                procedures = main.getProcedurePatientCopay(sortDesc, start, end);
            } else {
                procedures = main.getProcedureMedicarePayGap(sortDesc, start, end);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return procedures;
    }

}