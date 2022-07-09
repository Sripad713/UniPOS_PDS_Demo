package com.visiontek.Mantra.Models;

import com.visiontek.Mantra.Models.DealerDetailsModel.GetURLDetails.Dealer;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetURLDetails.Member;
import com.visiontek.Mantra.Models.MenuDetailsModel.Menus;

public class AppConstants {
    /*public static String DEVICEID = "0110000106";*/
    public static String DEVICEID = "";
    public static boolean Debug = true;
    public static String VERSION_NO = "1.7";

    public static Dealer dealerConstants;
    public static Member memberConstants;
    public static Menus menuConstants;


    public static String Dealername;
    public static int Mdealer;
    public static String MemberName,MemberUid;
    public static double TOTALAMOUNT;

    public static String longitude;
    public static String latitude;

    /**************** OFFLINE FLAGS *****************/
    public static int txnType = 0;
    public static int offlineEligible = 0;
    public static String OFFLINE_TOKEN = "9f943748d8c1ff6ded5145c59d0b2ae7";
    /***********************************************/
    /**
     * subclass 6 means that the usb mass storage device implements the SCSI
     * transparent command set
     */
    public static final int INTERFACE_SUBCLASS = 6;

    /**
     * protocol 80 means the communication happens only via bulk transfers
     */
    public static final int INTERFACE_PROTOCOL = 80;

    public final static int CACHE_THRESHOLD = 1; // 20 MB

}
