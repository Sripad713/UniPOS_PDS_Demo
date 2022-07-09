package com.visiontek.Mantra.Models.DealerDetailsModel.GetUserDetails;

import com.visiontek.Mantra.Models.RDModel;

import java.io.Serializable;

public class DealerModel implements Serializable {
    public String
            DUid,
            DName,
            Dtype,
            DAtype,
            Dfusion,
            Dnamell,
            Dwadh,
            DaadhaarAuthType,
    //-----------------------
            DEALER_AUTH_TYPE,
    //-----------------------
             err_code = "1",
    //-----------------------
            fCount = "0",
            fType = "0",  //******CHANGEfType=2******//
            iCount = "0",
            iType = "0",
    //-----------------------
             dealertype,
    //----------------------
             EnterPassword;

    public int RD_SERVICE = 0;

    public int
            wadhflag,
            FIRflag,
            Fusionflag,
            fusionflag;

    public boolean click = false;

    public RDModel rdModel=new RDModel();

}
