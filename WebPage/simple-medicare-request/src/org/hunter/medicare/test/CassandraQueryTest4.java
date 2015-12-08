package org.hunter.medicare.test;

import org.hunter.medicare.data.CassandraQueryResponse;

public class CassandraQueryTest4 {

    public static void main(String[] args) {
        // CassandraQueryResponse cr = new CassandraQueryResponse();
        // List<ProcedureDetails> pr = cr.getPatientResponsibility(true, 10);
        // List<ProcedureDetails> pg = cr.getChargedMedicarePayGap(true, 10);
        // for (ProcedureDetails i : pr) {
        // System.out.println(i.medicarePay);
        // }
        // for (ProcedureDetails i : pg) {
        // System.out.println(i.medicarePay);
        // }

        System.out.println(CassandraQueryResponse.getAverage("FL", "99213"));

    }

}
