package org.hunter.medicare.test;

import java.util.List;

import org.hunter.medicare.data.CassandraQueryResponse;
import org.hunter.medicare.data.ProcedureDetails;

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

    }

}
