package com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetUserDetails;

import com.visiontek.Mantra.Models.RDModel;

import java.io.Serializable;

public class MemberModel implements Serializable {
    public String
            memberNamell,
            memberName,
            member_fusion,
            uid,
            w_uid_status,
            xfinger,
            zmanual,
            zmemberId,
            zwgenWadhAuth,
    //-----------------------
            MEMBER_AUTH_TYPE,
    //-----------------------
            err_code = "1",
    //-----------------------
            fCount = "0",
            fType = "0",
            iCount = "0",
            iType = "0",
    //-----------------------
            Aadhaar,
    //----------------------
            Enter_UID,
            trans_type;

    public int RD_SERVICE = 0;

    public int
            wadhflag,
            FIRflag,
            Fusionflag,
            fusionflag,
            EKYC;

    public boolean click = false;

    public boolean
            mMan,
            mBIO,
            mDeal;

    public RDModel rdModel=new RDModel();
}
