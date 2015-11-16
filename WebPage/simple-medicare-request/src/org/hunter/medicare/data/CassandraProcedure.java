package org.hunter.medicare.data;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Row;

public class CassandraProcedure extends ProcedureDetails {

    public CassandraProcedure(Row row) {
        ColumnDefinitions columns = row.getColumnDefinitions();
        procCode = row.getString("hcpcs_code");
        desc = row.getString("hcpcs_description");
        drugIndicator = row.getString("hcpcs_drug_indicator").equals('Y');

        if (columns.contains("average_medicare_allowed_amt")) {
            allowedAmt = row.getFloat("average_medicare_allowed_amt");
        }
        if (columns.contains("average_submitted_chrg_amt")) {
            submittedChrg = row.getFloat("average_submitted_chrg_amt");
        }
        medicarePay = row.getFloat("average_medicare_payment_amt");
        if (columns.contains("stdev_average_medicare_allowed_amt")) {
            stdevAllowedAmt = row.getFloat("stdev_average_medicare_allowed_amt");
        }
        if (columns.contains("stdev_average_submitted_chrg_amt")) {
            stdevSubmittedChrg = row.getFloat("stdev_average_submitted_chrg_amt");
        }
        if (columns.contains("stdev_average_medicare_payment_amt")) {
            stdevMedicarePay = row.getFloat("stdev_average_medicare_payment_amt");
        }
        if (columns.contains("charge_medicare_pay_gap")) {
            payGap = row.getFloat("charge_medicare_pay_gap");
        }
        if (columns.contains("fraction_responsible")) {
            patientResponsibility = row.getFloat("fraction_responsible");
        }
        System.out.println(row.getColumnDefinitions().contains("hcpcs_code"));
    }

    public CassandraProcedure() {
        // TODO Auto-generated constructor stub
    }
}
