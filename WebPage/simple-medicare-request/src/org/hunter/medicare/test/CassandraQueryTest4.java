package org.hunter.medicare.test;

import java.util.List;

import org.hunter.medicare.data.CassandraQueryResponse;
import org.hunter.medicare.data.ProcedureDetails;
import org.hunter.medicare.data.Provider;

public class CassandraQueryTest4 {

    public static void main(String[] args) {
        CassandraQueryResponse cr = new CassandraQueryResponse();
        List<ProcedureDetails> pr = cr.getPatientResponsibility(true, 10);
        List<ProcedureDetails> pg = cr.getChargedMedicarePayGap(true, 10);
        for (ProcedureDetails i : pr) {
            System.out.println(i.medicarePay);
        }
        for (ProcedureDetails i : pg) {
            System.out.println(i.medicarePay);
        }

        System.out.println(CassandraQueryResponse.getAverage("FL", "99213"));

        List<Provider> lp = CassandraQueryResponse.getProvidersOverCostThreshold("99213", 1000,
                false);
        System.out.println(lp.size());

    }

}
