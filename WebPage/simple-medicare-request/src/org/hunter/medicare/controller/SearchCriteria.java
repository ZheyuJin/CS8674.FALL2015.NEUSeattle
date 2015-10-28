package org.hunter.medicare.controller;

public class SearchCriteria {

	public String state;
	public String gender;
	
	public void setState(String state){
		this.state = state;
	}
	
	public String getState(){
		return this.state;
	}
	
	public void setGender(String gender){
		this.gender = gender;
	}
	
	public String getGender(){
		return this.gender;
	}
	
	
}
