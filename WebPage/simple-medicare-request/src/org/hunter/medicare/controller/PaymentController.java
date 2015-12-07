package org.hunter.medicare.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hunter.medicare.data.CassandraQueryResponse;
import org.hunter.medicare.data.ProcedureDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/payment")
public class PaymentController {
    static Logger logger = Logger.getLogger("PaymentController");

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String getQueryForm() {
        logger.debug("Returning payment gap query page");
        return "paymentPatientForm";
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET, params = { "numRows", "start",
            "sortDesc", "isPercentage" })
    public @ResponseBody List<ProcedureDetails> getPatientPaymentResponsibility(
            @RequestParam(value = "numRows", required = true, defaultValue = "10") int numRows,
            @RequestParam(value = "start", required = false, defaultValue = "true") int start,
            @RequestParam(value = "sortDesc", required = false, defaultValue = "true") boolean sortDesc,
            @RequestParam(value = "isPercentage", required = false, defaultValue = "false") boolean isPercentage)
            throws Exception {
        logger.debug("Received Payment gap request");
        System.out.println("Reading numRows as " + Integer.toString(numRows) + " start as "
                + Integer.toString(start) + " sortDesc as " + Boolean.toString(sortDesc)
                + " and isPercentage " + Boolean.toString(isPercentage));

        List<ProcedureDetails> procedures = new ArrayList<ProcedureDetails>();
        try {
            int end = start + numRows - 1;

            if (isPercentage) {
                procedures = getProcedurePatientCopay(sortDesc, start, end);
            } else {
                procedures = getProcedureMedicarePayGap(sortDesc, start, end);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return procedures;
    }

    /**
     * Returns JSON
     * 
     * @throws Exception
     */
    // http://localhost:8080/simple-medicare-request/assessment/main/procedure/paygap/
    @RequestMapping(value = "/paygap", method = RequestMethod.GET)
    @ResponseBody
    public List<ProcedureDetails> getProcedureMedicarePayGap(
            @RequestParam(value = "top", required = false, defaultValue = "true") boolean sortDesc,
            @RequestParam(value = "start", required = false, defaultValue = "-1") int start,
            @RequestParam(value = "end", required = false, defaultValue = "-1") int end)
            throws Exception {

        // Input parameter processing...

        // If they don't have start/end set, default to first 10 (= 0-9)
        Integer startRow = 0;
        Integer numRows = 10; // Default at 10 rows (inclusive)

        // Note: start = end = 0 is one row (the first one)
        if (start >= 0) {
            startRow = start;

            if (end >= start) {
                numRows = end - start + 1;
            }
        }

        try {

            return CassandraQueryResponse.getChargedMedicarePayGap(sortDesc, startRow, numRows);

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Exception querying Cassandra; rethrowing...");
            throw e;
        }

    }

    /**
     * Returns JSON
     * 
     * @throws Exception
     */
    // http://localhost:8080/simple-medicare-request/assessment/main/procedure/copay/
    @RequestMapping(value = "/copay", method = RequestMethod.GET)
    @ResponseBody
    public List<ProcedureDetails> getProcedurePatientCopay(
            @RequestParam(value = "top", required = false, defaultValue = "true") boolean sortDesc,
            @RequestParam(value = "start", required = false, defaultValue = "-1") int start,
            @RequestParam(value = "end", required = false, defaultValue = "-1") int end)
            throws Exception {

        // Input parameter processing...

        // If they don't have start/end set, default to first 10 (= 0-9)
        Integer startRow = 0;
        Integer numRows = 10; // Default at 10 rows (inclusive)

        // Note: start = end = 0 is one row (the first one)
        if (start >= 0) {
            startRow = start;

            if (end >= start) {
                numRows = end - start + 1;
            }
        }

        try {

            return CassandraQueryResponse.getPatientResponsibility(sortDesc, startRow, numRows);

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Exception querying Cassandra; rethrowing...");
            throw e;
        }
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