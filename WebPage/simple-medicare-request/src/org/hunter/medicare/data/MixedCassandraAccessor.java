package org.hunter.medicare.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * given a set of hcpcs_codes and a state
 * returns a hashmap mapping the codes to their average values in the state
 *
 */
public class MixedCassandraAccessor {
	
	// cql constants
	public static final String SPACE = " ";
	public static final String COMMA = ",";
	public static final String LEFT = "(";
	public static final String RIGHT = ")";
	public static final String OPEN_STRING = "'";
	public static final String CLOSE_STRING = "'";
	public static final String TEXT = "text";
	public static final String PRIMARY_KEY = "primary key";
	public static final String INSERT_INTO = "insert into";
	public static final String VALUES = "values";
	public static final String SELECT = "select";
	public static final String WILDCARD = "*";
	public static final String FROM = "from";
	public static final String WHERE = "where";
	public static final String STATE = "nppes_provider_state";
	public static final String EQUALS = "=";
	public static final String CREATE_TABLE = "create table";
	public static final String LIMIT = "limit";
	
	// for testing
//	public static void main(String[] args) { 
//		HashSet<String> asdf = new HashSet<String>();
//		asdf.add("99238");
//		asdf.add("99204");
//		asdf.add("99223");
//		System.out.println(getCodeToAvgCostMappingForState(asdf, "CA"));
//	}

	public static HashMap<String, Double> getCodeToAvgCostMappingForState(HashSet<String> codes, String state) {
		HashMap<String, Double> codeToAvgCostMappingForState = new HashMap<String, Double>();
		Iterator<String> codesIterator = codes.iterator();
		while ( codesIterator.hasNext() ) {
			String code = codesIterator.next();
			Double avgCost = GetAveragePerStateAndCode.getAverage(state, code);
			codeToAvgCostMappingForState.put(code, avgCost);
		}
		
		return codeToAvgCostMappingForState;
	}
}
