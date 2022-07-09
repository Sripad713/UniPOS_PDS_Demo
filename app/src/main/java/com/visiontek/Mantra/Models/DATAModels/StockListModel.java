package com.visiontek.Mantra.Models.DATAModels;

public class StockListModel {
    public String comm;
    public String scheme;
    public String ob;
    public String issue;
    public String cb;
    public boolean isSelected = false;

    public StockListModel(String comm, String scheme, String ob,String issue,String cb) {
        this.comm = comm;
        this.scheme = scheme;
        this.ob = ob;
        this.issue = issue;
        this.cb = cb;
    }

    public String getName() {
        return comm;
    }

    public String getPrev() {
        return scheme;
    }

    public String getPrice() {
        return ob;
    }
    public String getIssue() {
        return issue;
    }
    public String getAmount() {
        return cb;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}