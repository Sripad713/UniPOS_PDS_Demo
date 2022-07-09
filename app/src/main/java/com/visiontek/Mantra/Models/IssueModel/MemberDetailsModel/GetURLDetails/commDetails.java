package com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetURLDetails;

import java.io.Serializable;

public class commDetails implements Serializable {
    public String
            allocationType,
            allotedMonth,
            allotedYear,
            availedQty,
            balQty,
            closingBal,
            commName,
            commNamell,
            commcode,
            measureUnit,
            minQty,
            price,
            requiredQty,
            totQty,
            weighing,
            amount,
            totalPrice;

    /************************** OFFLINE *****************************/

    public String getAllocationType() {
        return allocationType;
    }

    public void setAllocationType(String allocationType) {
        this.allocationType = allocationType;
    }

    public String getAllotedMonth() {
        return allotedMonth;
    }

    public void setAllotedMonth(String allotedMonth) {
        this.allotedMonth = allotedMonth;
    }

    public String getAllotedYear() {
        return allotedYear;
    }

    public void setAllotedYear(String allotedYear) {
        this.allotedYear = allotedYear;
    }

    public String getAvailedQty() {
        return availedQty;
    }

    public void setAvailedQty(String availedQty) {
        this.availedQty = availedQty;
    }

    public String getBalQty() {
        return balQty;
    }

    public void setBalQty(String balQty) {
        this.balQty = balQty;
    }

    public String getClosingBal() {
        return closingBal;
    }

    public void setClosingBal(String closingBal) {
        this.closingBal = closingBal;
    }

    public String getCommName() {
        return commName;
    }

    public void setCommName(String commName) {
        this.commName = commName;
    }

    public String getCommNamell() {
        return commNamell;
    }

    public void setCommNamell(String commNamell) {
        this.commNamell = commNamell;
    }

    public String getCommcode() {
        return commcode;
    }

    public void setCommcode(String commcode) {
        this.commcode = commcode;
    }

    public String getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(String measureUnit) {
        this.measureUnit = measureUnit;
    }

    public String getMinQty() {
        return minQty;
    }

    public void setMinQty(String minQty) {
        this.minQty = minQty;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRequiredQty() {
        return requiredQty;
    }

    public void setRequiredQty(String requiredQty) {
        this.requiredQty = requiredQty;
    }

    public String getTotQty() {
        return totQty;
    }

    public void setTotQty(String totQty) {
        this.totQty = totQty;
    }

    public String getWeighing() {
        return weighing;
    }

    public void setWeighing(String weighing) {
        this.weighing = weighing;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }
}
