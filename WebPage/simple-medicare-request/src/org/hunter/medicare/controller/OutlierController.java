package org.hunter.medicare.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;
import org.hunter.medicare.data.Provider;
import org.hunter.medicare.gaussian.GMM;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Using Normal distribution to detect outlier. Loading trained model form local
 * file, which includes data for each of 5000+ procedures.
 * 
 * @author Zheyu
 *
 */
@Controller
@RequestMapping("/outlier")
public class OutlierController {
    Logger log = Logger.getLogger("[outlier]");

    /**
     * @return the form view for outlier detection.
     */
    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String getForm() {
        return "outlier-form";
    }

    /**
     * 
     * @param proc_code
     * @param percentage
     * @param model
     * @return JSP page as view.
     * @throws Exception
     */
    @RequestMapping(value = "/result-jsp", method = RequestMethod.GET)
    public String getOutlier_ResultJSP(
            @RequestParam(value = "proc_code", required = true) String proc_code,
            @RequestParam(value = "percentage", required = true) String percentage, Model model)
            throws Exception {
        List<Provider> list = new ArrayList<Provider>();

        try {
            list = getGaussianOutliers(proc_code, Double.parseDouble(percentage));
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("Exception getting outlier results; rethrowing...");
            throw e;
        }

        // Add to model
        model.addAttribute("providerlist", list);

        return "ProviserListView";
    }

    /**
     * 
     * @param proc_code
     * @param percentage
     * @param model
     * @return JSON result.
     * @throws Exception
     */
    @RequestMapping(value = "/result-json", method = RequestMethod.GET)
    @ResponseBody
    public List<Provider> getOutlier_ResultJson(
            @RequestParam(value = "proc_code", required = true) String proc_code,
            @RequestParam(value = "percentage", required = true) String percentage, Model model)
            throws Exception {
        List<Provider> list = new ArrayList<Provider>();

        try {
            list = getGaussianOutliers(proc_code, Double.parseDouble(percentage));
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("Exception getting outlier json; rethrowing...");
            throw e;
        }

        // Add to model
        model.addAttribute("providerlist", list);

        return list;
    }

    /**
     * Filter outliers by percentage.
     * 
     * @param proc_code
     * @param topPercentage
     * @return
     * @throws Exception
     */
    private List<Provider> getGaussianOutliers(String proc_code, double topPercentage)
            throws Exception {

        /* load GMM from file */
        GMM gmm = new GMM(null);
        gmm.load();
        NormalDistribution nd = gmm.getIndividualModel(proc_code);

        double cuttingPrice = nd.inverseCumulativeProbability(1 - topPercentage / 100);

        log.info(String.format("mu: %f, stddev: %f cutting price: %f\n", nd.getMean(),
                nd.getStandardDeviation(), cuttingPrice));

        return filterByPrice(cuttingPrice);
    }

    /**
     * filter all provisers who submitted charge for certain proc more than
     * given price.
     * 
     * @param price
     * @return
     * @throws Exception
     */
    private List<Provider> filterByPrice(double price) throws IOException {
        List<Provider> ret = new ArrayList<>();
        // TODO will feed input read from Cassandra in the future.
        try (CSVReader stream = new CSVReader(new InputStreamReader(new ClassPathResource(
                "resources/22524.txt").getInputStream()), '\t');) {
            String[] ss = null;

            while (null != (ss = stream.readNext())) {
                Provider p = parseProvider(ss);
                if (p.providerDetails.averageSubmittedChargeAmount >= price) {
                    ret.add(p);
                }
            }
        }

        return ret;
    }

    /**
     * Parse fields of interests from the given String[].
     * 
     * @param ss
     * @return object of Provider.
     */
    @SuppressWarnings("unused")
    private Provider parseProvider(String[] ss) {
        int NPI = 0;
        int NPPES_PROVIDER_LAST_ORG_NAME = 1;
        int NPPES_PROVIDER_FIRST_NAME = 2;
        int NPPES_PROVIDER_MI = 3;
        int NPPES_CREDENTIALS = 4;
        int NPPES_PROVIDER_GENDER = 5;
        int NPPES_ENTITY_CODE = 6;
        int NPPES_PROVIDER_STREET1 = 7;
        int NPPES_PROVIDER_STREET2 = 8;
        int NPPES_PROVIDER_CITY = 9;
        int NPPES_PROVIDER_ZIP = 10;
        int NPPES_PROVIDER_STATE = 11;
        int NPPES_PROVIDER_COUNTRY = 12;
        int PROVIDER_TYPE = 13;
        int MEDICARE_PARTICIPATION_INDICATOR = 14;
        int PLACE_OF_SERVICE = 15;
        int HCPCS_CODE = 16;
        int HCPCS_DESCRIPTION = 17;
        int HCPCS_DRUG_INDICATOR = 18;
        int LINE_SRVC_CNT = 19;
        int BENE_UNIQUE_CNT = 20;
        int BENE_DAY_SRVC_CNT = 21;
        int AVERAGE_MEDICARE_ALLOWED_AMT = 22;
        int STDEV_MEDICARE_ALLOWED_AMT = 23;
        int AVERAGE_SUBMITTED_CHRG_AMT = 24;
        int STDEV_SUBMITTED_CHRG_AMT = 25;
        int AVERAGE_MEDICARE_PAYMENT_AMT = 26;
        int STDEV_MEDICARE_PAYMENT_AMT = 27;

        Provider p = new Provider();

        // basic info
        p.beneficiaries_day_service_count = Integer.parseInt(ss[BENE_DAY_SRVC_CNT]);
        p.first_name = ss[NPPES_PROVIDER_FIRST_NAME];
        p.last_or_org_name = ss[NPPES_PROVIDER_LAST_ORG_NAME];
        p.zip = ss[NPPES_PROVIDER_ZIP];

        // extended info
        p.providerDetails = p.new ExtendedInfo();
        p.providerDetails.averageSubmittedChargeAmount = Float
                .parseFloat(ss[AVERAGE_SUBMITTED_CHRG_AMT]);

        return p;
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

    /**
     * for test only
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        OutlierController con = new OutlierController();
        for (Object obj : con.getGaussianOutliers("22524" /* spine procedure */, 0.1 /* percent */)) {
            System.out.println(obj);
        }
    }
}
