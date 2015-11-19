package org.hunter.medicare.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hunter.medicare.data.CassandraQueryResponse;
import org.hunter.medicare.data.Provider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/outlier")
public class OutlierController {
    static Logger logger = Logger.getLogger("OutlierController");

    @RequestMapping(value = "/outlier-form", method = RequestMethod.GET)
    public String getCase1Form() {
        System.out.println("Initialized Cassandra query page");
        return "outlier-form";
    }

    @RequestMapping(value = "/result-jsp", method = RequestMethod.GET)
    public String getCase1_ResultForm(
            @RequestParam(value = "proc_code", required = true) String proc_code,
            @RequestParam(value = "state", required = true) String state,
            @RequestParam(value = "percentage", required = true) String percentage, Model model) {
        List<Provider> list = new ArrayList<Provider>();

        try {
            list = getGaussianOutliers(state, proc_code, Double.parseDouble(percentage));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add to model
        model.addAttribute("providerlist", list);

        return "ProviserListView";
    }

    private List<Provider> getGaussianOutliers(String state, String proc_code, double percentage) {
        final String LEAST = "least";
        CassandraQueryResponse.getProviderById("");
        List<String> providerID = CassandraQueryResponse.getProviders(state, proc_code, LEAST,
                Integer.MAX_VALUE);

        List<Provider> providers = new ArrayList<>();
        for (String id : providerID)
            providers.add(CassandraQueryResponse.getProviderById(id));

        logger.error(providers.size());
        logger.error(providers);

        return providers;
    }

}
