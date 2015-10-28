package org.hunter.medicare.test;

import org.hunter.medicare.data.CassandraQueryResponse;
import org.hunter.medicare.data.Provider;

/**
 * Just to see if {@link CassandraQueryResponse} works correctly for mock.
 * 
 * @author Zheyu
 *
 */
public class CassandraQueryTest {

    public static void main(String[] args) {
	System.out.println("gonna print test result");

	for (Provider p : CassandraQueryResponse.getInstance().getMostExpensive("CA", "*")) {
	    System.out.println(p.providerDetails.averageSubmittedChargeAmount);
	}
    }

}
