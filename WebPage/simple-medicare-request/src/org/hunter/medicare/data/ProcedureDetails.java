package org.hunter.medicare.data;

// container class for procedure statistics
public class ProcedureDetails extends Procedure {

    public boolean drugIndicator;
    public Float allowedAmt;
    public Float submittedChrg;
    public Float medicarePay;
    public Float stdevAllowedAmt;
    public Float stdevSubmittedChrg;
    public Float stdevMedicarePay;
    public Float payGap;
    public Float patientResponsibility;

}
