package com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel;

import java.io.Serializable;

public class printBeans implements Serializable {
    public String
            bal_qty,
            carry_over,
            commIndividualAmount,
            comm_name,
            comm_name_ll,
            member_name,
            member_name_ll,
            reciept_id,
            retail_price,
            scheme_desc_en,
            scheme_desc_ll,
            tot_amount,
            total_quantity,
            transaction_time,
            uid_refer_no;

    /************************** OFFLINE *****************************/
    public String allocationType,
                    allotedMonth,
                    allotedYear,
                    closingBal,
                    commCode;

    /************************** OFFLINE *****************************/

    public String getBal_qty() {
        return bal_qty;
    }

    public void setBal_qty(String bal_qty) {
        this.bal_qty = bal_qty;
    }

    public String getCarry_over() {
        return carry_over;
    }

    public void setCarry_over(String carry_over) {
        this.carry_over = carry_over;
    }

    public String getCommIndividualAmount() {
        return commIndividualAmount;
    }

    public void setCommIndividualAmount(String commIndividualAmount) {
        this.commIndividualAmount = commIndividualAmount;
    }

    public String getComm_name() {
        return comm_name;
    }

    public void setComm_name(String comm_name) {
        this.comm_name = comm_name;
    }

    public String getComm_name_ll() {
        return comm_name_ll;
    }

    public void setComm_name_ll(String comm_name_ll) {
        this.comm_name_ll = comm_name_ll;
    }

    public String getMember_name() {
        return member_name;
    }

    public void setMember_name(String member_name) {
        this.member_name = member_name;
    }

    public String getMember_name_ll() {
        return member_name_ll;
    }

    public void setMember_name_ll(String member_name_ll) {
        this.member_name_ll = member_name_ll;
    }

    public String getReciept_id() {
        return reciept_id;
    }

    public void setReciept_id(String reciept_id) {
        this.reciept_id = reciept_id;
    }

    public String getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(String retail_price) {
        this.retail_price = retail_price;
    }

    public String getScheme_desc_en() {
        return scheme_desc_en;
    }

    public void setScheme_desc_en(String scheme_desc_en) {
        this.scheme_desc_en = scheme_desc_en;
    }

    public String getScheme_desc_ll() {
        return scheme_desc_ll;
    }

    public void setScheme_desc_ll(String scheme_desc_ll) {
        this.scheme_desc_ll = scheme_desc_ll;
    }

    public String getTot_amount() {
        return tot_amount;
    }

    public void setTot_amount(String tot_amount) {
        this.tot_amount = tot_amount;
    }

    public String getTotal_quantity() {
        return total_quantity;
    }

    public void setTotal_quantity(String total_quantity) {
        this.total_quantity = total_quantity;
    }

    public String getTransaction_time() {
        return transaction_time;
    }

    public void setTransaction_time(String transaction_time) {
        this.transaction_time = transaction_time;
    }

    public String getUid_refer_no() {
        return uid_refer_no;
    }

    public void setUid_refer_no(String uid_refer_no) {
        this.uid_refer_no = uid_refer_no;
    }

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

    public String getCommCode() {
        return commCode;
    }

    public void setCommCode(String commCode) {
        this.commCode = commCode;
    }

    public String getClosingBal() {
        return closingBal;
    }

    public void setClosingBal(String closingBal) {
        this.closingBal = closingBal;
    }
}
