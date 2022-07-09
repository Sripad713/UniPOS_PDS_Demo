package com.visiontek.Mantra.Models.DATAModels;

public class DealerListModel {
    public String name;
    public String type;
    public String uid;
    public boolean isSelected = false;

    public DealerListModel(String name, String type, String uid) {
        this.name = name;
        this.type = type;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getUid() {
        return uid;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}