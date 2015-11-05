package org.hunter.medicare.controller;

import java.util.List;

import org.hunter.medicare.data.Provider;

import com.fasterxml.jackson.annotation.JsonView;

//@JsonView(Views.Public.class)
public class AjaxResponseBody {

    @JsonView(Views.Public.class)
    String msg;

    @JsonView(Views.Public.class)
    String code;

    @JsonView(Views.Public.class)
    List<Provider> result;

    public void setResults(List<Provider> result) {
        this.result = result;
    }

    public List<Provider> getResult() {
        return result;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
