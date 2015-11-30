package org.hunter.medicare.data;

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
}
