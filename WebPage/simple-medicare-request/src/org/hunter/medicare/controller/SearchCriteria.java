package org.hunter.medicare.controller;

public class SearchCriteria {

    public String state;
    public String providerID;

    public void setState(String state) {
	this.state = state;
    }

    public String getState() {
	return this.state;
    }

    public void setProviderID(String providerID) {
	this.providerID = providerID;
    }

    public String getproviderID() {
	return this.providerID;
    }

}
