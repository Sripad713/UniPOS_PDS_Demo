package com.visiontek.Mantra.Models.DATAModels;

public class PrintListModel {
    public String name;
    public String prev;
    public String issue;
    public String price;
    public String amount;
    public boolean isSelected = false;

    public PrintListModel(String name, String prev, String issue,String price,String amount) {
        this.name = name;
        this.prev = prev;
        this.issue = issue;
        this.price = price;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public String getPrev() {
        return prev;
    }

    public String getIssue() {
        return issue;
    }

    public String getPrice() {
        return price;
    }

    public String getAmount() {
        return amount;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}