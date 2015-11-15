package org.hunter.medicare.controller;

public class PagingRequest {

	String state;
	String zip;
	String keyword;
	int startIndex;
	int endIndex;
	
	public PagingRequest(){}
			
	
	public PagingRequest(String state, String zip, String keyword, 
			int startIndex, int endIndex) {
		super();
		this.state = state;
		this.zip = zip;
		this.keyword = keyword;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public String getKeyword() {
		
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
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
	
	
}
