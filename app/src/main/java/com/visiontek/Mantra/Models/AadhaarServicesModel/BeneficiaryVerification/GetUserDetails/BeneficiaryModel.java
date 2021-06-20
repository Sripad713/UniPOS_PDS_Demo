package com.visiontek.Mantra.Models.AadhaarServicesModel.BeneficiaryVerification.GetUserDetails;

import com.visiontek.Mantra.Models.AadhaarServicesModel.BeneficiaryVerification.GetURLDetails.rcMemberDetVerify;
import com.visiontek.Mantra.Models.RDModel;

import java.io.Serializable;

public class BeneficiaryModel implements Serializable {

   // public rcMemberDetVerify rcMemberDetVerify=new rcMemberDetVerify();
    public String
            memberId,
            memberName,
            memberNamell,
            member_fusion,
            uid,
            verification,
            verifyStatus_en,
            verifyStatus_ll,
            w_uid_status;
    public String

    err_code = "1",
    //-----------------------
    fCount = "0",
            fType = "0",
            iCount = "0",
            iType = "0",

    Enter_UID;

    public int RD_SERVICE = 0;

    public int
            EKYC;

    public boolean click = false;


    public RDModel rdModel=new RDModel();
}
