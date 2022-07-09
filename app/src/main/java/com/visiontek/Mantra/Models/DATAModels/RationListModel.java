package com.visiontek.Mantra.Models.DATAModels;

public class RationListModel {
    public String name;
    public String price;
    public String bal;
    public String clbal;
    public String issue;
    public String amount;
    public boolean isSelected = false;

    public RationListModel(String name, String price, String bal,String clbal,String issue,String amount) {
        this.name = name;
        this.price = price;
        this.bal = bal;
        this.clbal = clbal;
        this.issue = issue;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getBal() {
        return bal;
    }

    public String getClbal() {
        return clbal;
    }

    public String getIssue() {
        return issue;
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