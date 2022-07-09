package com.visiontek.Mantra.Models.DATAModels;

public class ReceiveGoodsListModel {
    public String comm;
    public String scheme;
    public String allot;
    public String dispatch;
    public String received;

    public boolean isSelected = false;

    public ReceiveGoodsListModel(String comm, String scheme, String allot,String dispatch,String received) {
        this.comm = comm;
        this.scheme = scheme;
        this.allot = allot;
        this.dispatch = dispatch;
        this.received = received;
    }

    public String getComm() {
        return comm;
    }

    public String getScheme() {
        return scheme;
    }

    public String getAllot() {
        return allot;
    }

    public String getDispatch() {
        return dispatch;
    }

    public String getReceived() {
        return received;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}