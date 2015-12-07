package org.hunter.medicare.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hunter.medicare.data.CountedPropertyValue;
import org.hunter.medicare.data.FacetType;
import org.hunter.medicare.data.FacetedCount;
import org.hunter.medicare.data.FacetedProviderResult;
import org.hunter.medicare.data.FilterPair;
import org.hunter.medicare.data.Provider;
import org.hunter.medicare.data.SolrProviderSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/provider")
public class PagingController {
    static Logger logger = Logger.getLogger("PagingController");

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String getCase1Form() {
        System.out.println("made it here");
        return "pagedProviderQueries";
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET, params = { "state" })
    @ResponseBody
    public FacetedProviderResult getProvidersWithFacets(
            @RequestParam(value = "state", required = false, defaultValue = "") String state,
            @RequestParam(value = "zip", required = false, defaultValue = "") String zip,
            @RequestParam(value = "provider_type", required = false, defaultValue = "") String provider_type,
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "facet", required = false, defaultValue = "Query") String facetType,
            @RequestParam(value = "start", required = false, defaultValue = "-1") Integer start,
            @RequestParam(value = "end", required = false, defaultValue = "-1") Integer end)
            throws Exception {

        FacetedProviderResult ret = new FacetedProviderResult();

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
        // In result, start row is always what they requested (zero based)
        // The size of provider list = number returned in this "slice"
        // The total number = full number of results available
        // Notice that a query with start > total will return nothing
        ret.startRow = startRow;

        ret.facets = new FacetedCount();

        ret.providers = new ArrayList<Provider>();
        ret.facets.facetedCount = new ArrayList<CountedPropertyValue>();

        if (!query.isEmpty() || !state.isEmpty() || !zip.isEmpty() || !provider_type.isEmpty()) {
            ret.facets.facetFilters = new ArrayList<FilterPair>();
        }
        if (query != null && !query.isEmpty()) {
            ret.facets.facetFilters.add(new FilterPair(FacetType.Query.toString(), query));
        }
        if (zip != null && !zip.isEmpty()) {
            ret.facets.facetFilters.add(new FilterPair(FacetType.Zip.toString(), zip));
        }
        if (state != null && !state.isEmpty()) {
            ret.facets.facetFilters.add(new FilterPair(FacetType.State.toString(), state));
        }
        if (provider_type != null && !provider_type.isEmpty()) {
            ret.facets.facetFilters.add(new FilterPair(FacetType.ProviderType.toString(),
                    provider_type));
        }

        boolean useMock = false;
        try {

            if (useMock) {

                // We set the start = to the input.. the size of the provider
                // list is the number returned
                // starting at that start row (can be zero)
                // For the mock, this is a fake value (obviously)
                ret.numProvidersTotal = startRow + ret.providers.size() - 1L;

                // ToDo: mock some providers in the list too?

                ret.facets.facetType = FacetType.State;

                ret.facets.facetedCount.add(new CountedPropertyValue("tx", 135L));
                ret.facets.facetedCount.add(new CountedPropertyValue("fl", 70L));
                ret.facets.facetedCount.add(new CountedPropertyValue("nv", 7L));
                ret.facets.facetedCount.add(new CountedPropertyValue("ny", 86L));

            } else {
                // Query Solr for the provider count per state
                // TODO: maybe we should sort these?
                FacetType facetOn = FacetType.Query;
                if (!facetType.isEmpty()) {
                    facetOn = FacetType.valueOf(facetType);
                }
                ret.facets.facetType = facetOn;

                ret.numProvidersTotal = SolrProviderSource.getProvidersWithFacets(ret.providers,
                        facetOn, ret.facets.facetedCount, ret.facets.facetFilters, startRow,
                        numRows);
            }

            return ret;

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Exception querying Solr; rethrowing...");
            throw e;
        }

    }

    /**
     * Returns JSON
     * 
     * @throws Exception
     */
    // TODO: check if we still need this, think we flipped over to the general
    // faceted method in paging controller.
    @RequestMapping(value = "/count/states", method = RequestMethod.GET)
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