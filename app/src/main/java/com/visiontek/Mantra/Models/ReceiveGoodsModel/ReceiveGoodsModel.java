package com.visiontek.Mantra.Models.ReceiveGoodsModel;

import java.io.Serializable;
import java.util.ArrayList;

public class ReceiveGoodsModel implements Serializable {
    public String
            length,
            fps,
            month,
            year,
            chit,
            cid,
            orderno,
            truckno;
    public int select;
    public String received;
    public ArrayList<tcCommDetails> tcCommDetails=new ArrayList<>();


}
