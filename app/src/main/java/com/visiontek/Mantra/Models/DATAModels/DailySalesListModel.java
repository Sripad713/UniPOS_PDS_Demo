package com.visiontek.Mantra.Models.DATAModels;

public class DailySalesListModel {
    public String comm;
    public String scheme;
    public String total;
    public boolean isSelected = false;

    public DailySalesListModel(String comm, String scheme, String total) {
        this.comm = comm;
        this.scheme = scheme;
        this.total = total;
    }

    public String getComm() {
        return comm;
    }

    public String getScheme() {
        return scheme;
    }

    public String getTotal() {
        return total;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}