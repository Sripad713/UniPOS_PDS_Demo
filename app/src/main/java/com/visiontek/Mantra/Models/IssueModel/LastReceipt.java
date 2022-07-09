package com.visiontek.Mantra.Models.IssueModel;

import com.visiontek.Mantra.Models.IssueModel.LastReceiptComm;

import java.io.Serializable;
import java.util.ArrayList;

public class LastReceipt implements Serializable {
    //=========================lastReceiptComm=======================
    public ArrayList<LastReceiptComm> lastReceiptComm=new ArrayList<>();

    public String
            rcId,
            retail_price;

}
