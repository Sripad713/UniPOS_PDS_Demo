package com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetURLDetails;

import java.io.Serializable;
import java.util.ArrayList;

public class Member implements Serializable {
    //==========================carddetails==================================
    public carddetails carddetails = new carddetails();

    // ==============================memberdetails1=========================
    public memberdetails memberdetails1 = new memberdetails();

    //==========================commDetails==================================
    public ArrayList<commDetails> commDetails = new ArrayList<>();

    //==========================memberdetails================================
    public ArrayList<memberdetails> memberdetails = new ArrayList<>();
}
