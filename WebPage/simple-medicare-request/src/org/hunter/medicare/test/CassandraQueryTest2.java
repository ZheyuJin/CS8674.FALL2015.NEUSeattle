package org.hunter.medicare.test;

import java.util.List;

import org.hunter.medicare.data.CassandraQueryResponse;
import org.hunter.medicare.data.Provider;


/**
 * Cassandra tests with mock test commented out
 * 
 * @author Zheyu
 * @author Tim
 *
 */
public class CassandraQueryTest2 {

    public static void main(String[] args) {
	System.out.println("gonna print test result");
//
//	for (Provider p : CassandraQueryResponse.getInstance().getMostExpensive("CA", "*")) {
//	    System.out.println(p);
//	}
	
	//get

//		CassandraQueryResponse cqr = new CassandraQueryResponse();
//		List<ProviderT> pt = cqr.getMostExpensive("CA", "*");
//		System.out.println("Returned " + pt.size() + " results");
//		for (ProviderT p : pt) {
//			System.out.println("Provider Id is: " + p.npi);
//		}
	CassandraQueryResponse cqr = new CassandraQueryResponse();
	Provider test = cqr.getProviderById("1003000522F992132012");
	System.out.println("provider npi is: " + test.npi);
	
	}
 

}
