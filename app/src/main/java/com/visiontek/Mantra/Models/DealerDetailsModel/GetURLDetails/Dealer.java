package com.visiontek.Mantra.Models.DealerDetailsModel.GetURLDetails;

import com.visiontek.Mantra.Models.DealerDetailsModel.GetURLDetails.fpsCommonInfoModel.fpsCommonInfo;

import java.io.Serializable;
import java.util.ArrayList;

public class Dealer implements Serializable {
//=============================CommonInfo====================================
    public fpsCommonInfo fpsCommonInfo=new fpsCommonInfo();

//=============================fpsURLInfo====================================
    public fpsURLInfo fpsURLInfo=new fpsURLInfo();

//=============================reasonBeanLists===============================
    public ArrayList<reasonBeanLists> reasonBeanLists=new ArrayList<>();

//=============================StateBeans====================================
    public stateBean stateBean=new stateBean();

//=============================transactionMode===============================
    public ArrayList<transactionMode> transactionMode=new ArrayList<>();

}
