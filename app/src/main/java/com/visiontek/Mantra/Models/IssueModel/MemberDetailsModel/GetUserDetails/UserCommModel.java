package com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetUserDetails;

import java.io.Serializable;

public class UserCommModel implements Serializable {

    public String
            allocationType,
            allotedMonth,
            allotedYear,
            availedQty,
            closingBal,
            commName,
            commNamell,
            commcode,
            measureUnit,
            price,
            requiredQty,
            totQty,
            weighing;

    public float balQty;
    public float minQty;
    public float requiredQuantity;
    public int COMMPOSITION;
    public float commodityAmount;
}
