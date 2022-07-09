package com.visiontek.Mantra.Models.ReportsModel.Stockdetails;

import java.io.Serializable;

public class astockBean implements Serializable {
    public String
            allot_qty,
            closing_balance,
            commNamell,
            comm_name,
            commoditycode,
            issued_qty,
            opening_balance,
            received_qty,
            scheme_desc_en,
            scheme_desc_ll,
            total_quantity;

    public String getAllot_qty() {
        return allot_qty;
    }

    public void setAllot_qty(String allot_qty) {
        this.allot_qty = allot_qty;
    }

    public String getClosing_balance() {
        return closing_balance;
    }

    public void setClosing_balance(String closing_balance) {
        this.closing_balance = closing_balance;
    }

    public String getCommNamell() {
        return commNamell;
    }

    public void setCommNamell(String commNamell) {
        this.commNamell = commNamell;
    }

    public String getComm_name() {
        return comm_name;
    }

    public void setComm_name(String comm_name) {
        this.comm_name = comm_name;
    }

    public String getCommoditycode() {
        return commoditycode;
    }

    public void setCommoditycode(String commoditycode) {
        this.commoditycode = commoditycode;
    }

    public String getIssued_qty() {
        return issued_qty;
    }

    public void setIssued_qty(String issued_qty) {
        this.issued_qty = issued_qty;
    }

    public String getOpening_balance() {
        return opening_balance;
    }

    public void setOpening_balance(String opening_balance) {
        this.opening_balance = opening_balance;
    }

    public String getReceived_qty() {
        return received_qty;
    }

    public void setReceived_qty(String received_qty) {
        this.received_qty = received_qty;
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

    public String getTotal_quantity() {
        return total_quantity;
    }

    public void setTotal_quantity(String total_quantity) {
        this.total_quantity = total_quantity;
    }
}
