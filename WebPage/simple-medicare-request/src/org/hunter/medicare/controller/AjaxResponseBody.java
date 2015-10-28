package org.hunter.medicare.controller;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonView;
import org.hunter.medicare.data.*;

//@JsonView(Views.Public.class)
public class AjaxResponseBody {

	String msg;
	
	@JsonView(Views.Public.class)
	String code;
	
	@JsonView(Views.Public.class)
	ArrayList<Integer> intList = new ArrayList<Integer>();
	
	public void addInt(int i){
		intList.add(i);
	}
	
	public List<Integer> getIntList(){
		return intList;
	}
	
	@JsonView(Views.Public.class)
	List<Provider> result;
	

	public void setResults(List<Provider> result){
		this.result = result;
	}
	
	public List<Provider> getResult(){
		return result;
	}
	
	
	public void setMsg(String msg){
		this.msg = msg;
	}
	
	public String getMsg(){
		return msg;
	}
	
	public void setCode(String code){
		this.code = code;
	}
	
	public String getCode(){
		return code;
	}
	
}
