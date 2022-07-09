package com.visiontek.Mantra.Models.ReceiveGoodsModel;

public class ReceiveGoodsOfflineListModel {
    public String comm;
    public String scheme;
    public String received;
    public String unit;

    public boolean isSelected = false;

    public ReceiveGoodsOfflineListModel(String comm, String scheme, String received, String units) {
        this.comm = comm;
        this.scheme = scheme;
        this.received = received;
        this.unit = units;
    }

    public String getComm() {
        return comm;
    }

    public String getScheme() {
        return scheme;
    }

    public String getUnits() {
        return unit;
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