package org.hunter.medicare.data;

public class CountedPropertyValue {
    public String propertyValue;
    public Long propertyCount;

    public CountedPropertyValue(String propertyValue, Long propertyCount) {
        this.propertyValue = propertyValue;
        this.propertyCount = propertyCount;
    }

}
