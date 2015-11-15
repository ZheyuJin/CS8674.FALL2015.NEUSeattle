package org.hunter.medicare.controller;

import java.util.*;

public class PagingResponseBody {

	int startIndex;
	int endIndex;
	int totalEntries;

	List<Integer> facetList;
	List<Integer> testData;
	
	public PagingResponseBody(){}
	
	public PagingResponseBody(int startIndex, int endIndex, int totalEntries,
			List<Integer> testData) {
		super();
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.totalEntries = totalEntries;
		this.testData = testData;
	}

	public List<Integer> getFacetList() {
		return facetList;
	}

	public void setFacetList(List<Integer> facetList) {
		this.facetList = facetList;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public int getTotalEntries() {
		return totalEntries;
	}

	public void setTotalEntries(int totalEntries) {
		this.totalEntries = totalEntries;
	}

	public List<Integer> getTestData() {
		return testData;
	}

	public void setTestData(List<Integer> testData) {
		this.testData = testData;
	}
	
	

	
}
