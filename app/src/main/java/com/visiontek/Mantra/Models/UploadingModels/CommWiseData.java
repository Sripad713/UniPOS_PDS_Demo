package com.visiontek.Mantra.Models.UploadingModels;

public class CommWiseData {
/*
    CommWiseData["rcId"]=query.value(0).toString().toAscii().data();
    CommWiseData["commCode"]=query.value(1).toString().toAscii().data();
    CommWiseData["totalEntitlement"]=query.value(2).toString().toAscii().data();
    CommWiseData["balanceEntitlement"]=query.value(3).toString().toAscii().data();
    CommWiseData["schemeId"]=query.value(4).toString().toAscii().data();
    CommWiseData["issueQty"]=query.value(5).toString().toAscii().data();
    CommWiseData["receiptId"]=query.value(6).toString().toAscii().data();
    CommWiseData["commAmount"]=query.value(7).toString().toAscii().data();
    CommWiseData["totalAmount"]=query.value(8).toString().toAscii().data();
    CommWiseData["commPrice"]=query.value(9).toString().toAscii().data();
    CommWiseData["headOfTheFamily"]=query.value(10).toString().toAscii().data();
    CommWiseData["transactionTime"]=query.value(11).toString().toAscii().data();
    CommWiseData["transMode"]=query.value(12).toString().toAscii().data();
    CommWiseData["month"]=query.value(13).toString().toAscii().data();
    CommWiseData["year"]=query.value(14).toString().toAscii().data();

 */
    private String rcId;
    private String commCode;
    private String totalEntitlement;
    private String balanceEntitlement;
    private String schemeId;
    private String issueQty;
    private String receiptId;
    private String commAmount;
    private String totalAmount;
    private String commPrice;
    private String headOfTheFamily;
    private String transactionTime;
    private String transMode;
    private String month;
    private String year;
    private String allotedMonth;
    private String allotedYear;
    private String allocationType;
    private String memberId;


//    CommWiseData["allotedMonth"]=query.value(13).toString().toAscii().data();
//    CommWiseData["allotedYear"]=query.value(14).toString().toAscii().data();
//    CommWiseData["allocationType"]=query.value(15).toString().toAscii().data();

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
    public String getRcId() {
        return rcId;
    }

    public void setRcId(String rcId) {
        this.rcId = rcId;
    }

    public String getCommCode() {
        return commCode;
    }

    public void setCommCode(String commCode) {
        this.commCode = commCode;
    }

    public String getTotalEntitlement() {
        return totalEntitlement;
    }

    public void setTotalEntitlement(String totalEntitlement) {
        this.totalEntitlement = totalEntitlement;
    }

    public String getBalanceEntitlement() {
        return balanceEntitlement;
    }

    public void setBalanceEntitlement(String balanceEntitlement) {
        this.balanceEntitlement = balanceEntitlement;
    }

    public String getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(String schemeId) {
        this.schemeId = schemeId;
    }

    public String getIssueQty() {
        return issueQty;
    }

    public void setIssueQty(String issueQty) {
        this.issueQty = issueQty;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public String getCommAmount() {
        return commAmount;
    }

    public void setCommAmount(String commAmount) {
        this.commAmount = commAmount;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCommPrice() {
        return commPrice;
    }

    public void setCommPrice(String commPrice) {
        this.commPrice = commPrice;
    }

    public String getHeadOfTheFamily() {
        return headOfTheFamily;
    }

    public void setHeadOfTheFamily(String headOfTheFamily) {
        this.headOfTheFamily = headOfTheFamily;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getTransMode() {
        return transMode;
    }

    public void setTransMode(String transMode) {
        this.transMode = transMode;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
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

    public String getAllocationType() {
        return allocationType;
    }

    public void setAllocationType(String allocationType) {
        this.allocationType = allocationType;
    }
}

