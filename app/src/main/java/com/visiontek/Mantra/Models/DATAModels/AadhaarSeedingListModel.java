package com.visiontek.Mantra.Models.DATAModels;

public class AadhaarSeedingListModel {
    public String name;
    public String uid;
    public String status;
    public boolean isSelected = false;

    public AadhaarSeedingListModel(String name,  String uid,String status) {
        this.name = name;
        this.uid = uid;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public String getStatus() {
        return status;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}