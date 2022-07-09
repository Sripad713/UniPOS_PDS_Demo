package com.visiontek.Mantra.Models.DATAModels;

public class DataModel1 {
    public boolean isSelected = false;
    private String avl;
    private final String bal;
    private final String close;
    private String name;
    private String code;
    private String unit;
    private String min;
    private final String rate;
    private final String req;
    private final String tot;
    private String wag;
    private int flag=0;

    public DataModel1(String tot, String bal,
                      String rate, String req, String close) {
        this.tot = tot;
        this.bal = bal;
        this.rate = rate;
        this.req = req;
        this.close = close;

    }

    /*public  setissue(String req) {
        this.tot = req;
    }*/
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getAvl() {
        return avl;
    }

    public String getBal() {
        return bal;
    }

    public String getClose() {
        return close;
    }

    public String getNames() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getUnit() {
        return unit;
    }

    public String getMin() {
        return min;
    }

    public String getRate() {
        return rate;
    }

    public String getReq() {
        return req;
    }

    public String getTot() {
        return tot;
    }

    public String getWag() {
        return wag;
    }
}