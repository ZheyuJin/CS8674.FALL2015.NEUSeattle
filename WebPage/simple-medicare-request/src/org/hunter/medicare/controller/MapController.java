package org.hunter.medicare.controller;

import java.io.IOException;
import java.util.*;

import org.hunter.medicare.controller.*;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.hunter.medicare.data.CassandraQueryResponse;
import org.hunter.medicare.data.FacetedProviderResult;
import org.hunter.medicare.data.Procedure;
import org.hunter.medicare.data.Provider;
import org.hunter.medicare.data.SolrProviderSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/map")
public class MapController {
    static Logger logger = Logger.getLogger("MapController");

    @RequestMapping(value = "/usa", method = RequestMethod.GET)
    public String getUrbanRuralUsaMap() {
        return "urbanrural_map";
    }
}