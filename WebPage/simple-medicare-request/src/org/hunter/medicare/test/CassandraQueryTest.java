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

    public static void main(String[] args) throws Exception {
        System.out.println("gonna print test result");

        getProvidersOverCostThresholdTest();
    }

    static void getProvidersOverCostThresholdTest() {
        for (Provider p : CassandraQueryResponse.getProvidersOverCostThreshold("22524", 1500,
                false))
            System.out.println(p);
    }

}
