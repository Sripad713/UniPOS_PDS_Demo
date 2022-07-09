package com.visiontek.Mantra.Models.DATAModels;

public class MemberListModel {
    public String name;
    public String uid;
    public boolean isSelected = false;

    public MemberListModel(String name,String uid) {
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