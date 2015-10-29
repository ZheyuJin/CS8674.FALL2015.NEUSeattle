package org.hunter.medicare.data;

import org.hunter.medicare.controller.Views;
import org.hunter.medicare.controller.Views.Public;

import com.fasterxml.jackson.annotation.JsonView;

public class Procedure {
    @JsonView(Views.Public.class)
    public String procCode;
    @JsonView(Views.Public.class)
    public String desc;
    @JsonView(Views.Public.class)
    public double avgCost;
    @JsonView(Views.Public.class)
    public String state;
    
    
    public Procedure(String code, String desc, double avgcost, String state) {
	this.procCode = code;
	this.desc = desc;
	this.avgCost = avgcost;
	this.state = state;
    }

    @Override
    public String toString() {
	return String.format("state: %s, code: %s, desc: %s, avgcost: %.1f, ", state, procCode, desc, avgCost);
    }
}