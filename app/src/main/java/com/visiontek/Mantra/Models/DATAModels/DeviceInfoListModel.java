package com.visiontek.Mantra.Models.DATAModels;

public class DeviceInfoListModel {
    public String name;
    public String uid;
    public boolean isSelected = false;

    public DeviceInfoListModel(String name,String uid) {
        this.name = name;
        this.uid = uid;
    }

    public String getName() {
        return name;
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