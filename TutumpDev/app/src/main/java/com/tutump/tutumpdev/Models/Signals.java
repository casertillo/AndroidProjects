package com.tutump.tutumpdev.Models;

import weka.core.expressionlanguage.common.Primitives;

/**
 * Created by casertillo on 29/08/16.
 */
public class Signals {
    Integer hr;
    Double rr;
    Integer gsr;

    public Integer getHr() {
        return hr;
    }

    public void setHr(Integer hr) {
        this.hr = hr;
    }

    public Double getRr() {
        return rr;
    }

    public void setRr(Double rr) {
        this.rr = rr;
    }

    public Integer getGsr() {
        return gsr;
    }

    public void setGsr(Integer gsr) {
        this.gsr = gsr;
    }
    public void addSignals(Integer hr, Double rr, Integer gsr){
        this.hr = hr;
        this.rr = rr;
        this.gsr = gsr;
    }
    public Signals(Integer hr, Double rr, Integer gsr){
        this.hr = hr;
        this.rr = rr;
        this.gsr = gsr;
    }
}
