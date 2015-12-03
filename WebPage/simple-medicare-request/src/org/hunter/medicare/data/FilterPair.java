package org.hunter.medicare.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilterPair {

    public String propertyName;
    public String propertyValue;

    public FilterPair(String propertyName, String propertyValue) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    public FilterPair(FacetType facet, String propertyValue) {
        this.propertyName = facet.toString();
        this.propertyValue = propertyValue;
    }

    public static List<FilterPair> convertToFilterList(Map<String, String> mapFilters) {
        ArrayList<FilterPair> filters = new ArrayList<FilterPair>();
        for (String k : mapFilters.keySet()) {
            FilterPair f = new FilterPair(k, mapFilters.get(k));
            filters.add(f);
        }

        return filters;
    }
}
