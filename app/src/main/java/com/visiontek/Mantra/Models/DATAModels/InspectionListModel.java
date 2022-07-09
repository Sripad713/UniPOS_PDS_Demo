package com.visiontek.Mantra.Models.DATAModels;

public class InspectionListModel {
    public String comm;
    public String cb;
    public String obs;
    public String var;
    public boolean isSelected = false;

    public InspectionListModel(String comm, String cb, String obs,String var) {
        this.comm = comm;
        this.cb = cb;
        this.obs = obs;
        this.var = var;
    }

    public String getComm() {
        return comm;
    }

    public String getCb() {
        return cb;
    }

    public String getObs() {
        return obs;
    }

    public String getVar() {
        return var;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}