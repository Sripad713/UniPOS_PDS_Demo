package com.visiontek.Mantra.Models.InspectionModel;

import java.io.Serializable;
import java.util.ArrayList;

public class InspectionDetails implements Serializable {
    public ArrayList<approvals> approvals=new ArrayList<>();
    public ArrayList<InspectioncommDetails> commDetails=new ArrayList<>();
}
