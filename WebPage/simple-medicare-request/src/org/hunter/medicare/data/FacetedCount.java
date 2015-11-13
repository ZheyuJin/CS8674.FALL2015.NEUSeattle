package org.hunter.medicare.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FacetedCount {
 
        // Indicates the type of facet contained in the facetedCount.
        public FacetType facetType;

        // This indicates what filters were in place for this
        // faceted query - for example, if we filtered on
        // state, and we are returning provider types for WA,
        // this would have the entry "State", "WA".
        // If there are more than one entry, all have been applied
        // (ie: treat these filters as an AND, not an OR)
        // If this is empty/null, then no filters were used.
        // Note that a query term, if applicable, will be in this list as
        // "query", "query term or phrase" 
        public List<FilterPair> facetFilters;

        // List of facet property values and counts
        public List<CountedPropertyValue> facetedCount;
        
        public static List<CountedPropertyValue> convertToFacetList(Map<String, Long> mapFacets)
        {            
            ArrayList<CountedPropertyValue> facets = new ArrayList<CountedPropertyValue>();
            for (String k : mapFacets.keySet())
            {
                CountedPropertyValue cp = new CountedPropertyValue(k, mapFacets.get(k));
                facets.add(cp);
            }
            
            return facets;
        }
         
        public static List<FilterPair> convertToFilterList(Map<String, String> mapFilters)
        {            
            ArrayList<FilterPair> filters = new ArrayList<FilterPair>();
            for (String k : mapFilters.keySet())
            {
                FilterPair f = new FilterPair(k, mapFilters.get(k));
                filters.add(f);
            }
            
            return filters;
        }
 
}
