package org.hunter.medicare.data;

public class Procedure {
    public String procCode;
    public String desc;
    public double avgCost;
    public String state;

    public Procedure(String code, String desc, double avgcost, String state) {
        this.procCode = code;
        this.desc = desc;

        // avgCost is only filled in for a state, and is >0.0
        // ie: if state isn't set, average cost is likely zero (and vice
        // versa)
        this.avgCost = avgcost;
        this.state = state;
    }

    public Procedure() {
    }

    @Override
    public String toString() {
        return String.format("state: %s, code: %s, desc: %s, avgcost: %.1f, ", state, procCode,
                desc, avgCost);
    }
}