package com.visiontek.Mantra.Models.ReportsModel.DailySalesDetails;

import java.io.Serializable;

public class drBean implements Serializable {
    public String
            commNamell,
            comm_name,
            sale,
            schemeName,
            total_cards;

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

    public String getSale() {
        return sale;
    }

    public void setSale(String sale) {
        this.sale = sale;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public String getTotal_cards() {
        return total_cards;
    }

    public void setTotal_cards(String total_cards) {
        this.total_cards = total_cards;
    }
}
