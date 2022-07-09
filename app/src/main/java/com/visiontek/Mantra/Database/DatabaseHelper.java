
package com.visiontek.Mantra.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.visiontek.Mantra.Activities.OfflineUploadNDownload;
import com.visiontek.Mantra.Models.DATAModels.MemberListModel;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetURLDetails.commDetails;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetURLDetails.memberdetails;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.Print;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.printBeans;
import com.visiontek.Mantra.Models.PartialOnlineData;
import com.visiontek.Mantra.Models.ReportsModel.DailySalesDetails.drBean;
import com.visiontek.Mantra.Models.ReportsModel.Stockdetails.astockBean;
import com.visiontek.Mantra.Models.UploadingModels.CommWiseData;
import com.visiontek.Mantra.Models.UploadingModels.StockData;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Util;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "PDS.db";
    //------------------------------DEALER TABLE
    private static final String DEALERS_TABLE_NAME = "Dealers_Table";

    private static final String DEALERS_AUTH_TYPE = "Dealers_Auth_Type";
    private static final String DEALERS_FUSION = "Dealers_Fusion";
    private static final String DEALERS_TYPE = "Dealers_Type";
    private static final String DEALERS_NAMES = "Dealers_Names";
    private static final String DEALERS_NAMES_LL = "Dealers_Names_Ll";
    private static final String DEALERS_UIDS = "Dealers_Uids";
    private static final String DEALERS_WADH = "Dealers_Wadh";

    //------------------------------MEMBERS TABLE
    private static final String MEMBERS_TABLE_NAME = "Members_Table";

    private static final String MEMBER_NAMES = "Member_Names";
    private static final String MEMBER_NAMESLL = "Member_Namesll";
    private static final String MEMBER_FUSION = "Member_Fusion";
    private static final String MEMBER_UIDS = "Member_Uids";
    private static final String MEMBER_WADH = "Member_Wadh";
    private static final String MEMBER_XFINGER = "Member_Xfinger";
    private static final String MEMBER_YIRIS = "Member_Yiris";
    private static final String MEMBER_ZMANUAL = "Member_Zmanual";
    private static final String MEMBER_ZMEMBERID = "Member_Zmemberid";
    private static final String MEMBER_ZOTP = "Member_Zotp";
    private static final String MEMBER_WADHAUTH = "Member_Wadhauth";

    //------------------------------RATION TABLE
    private static final String RATION_TABLE_NAME = "Ration_Table";
    private static final String TYPE = "Type";
    private static final String MONTH = "Month";
    private static final String YEAR = "Year";
    private static final String AVLQTY = "Avl";
    private static final String BALQTY = "Bal";
    private static final String CLOSEBAL = "Close";
    private static final String COMNAME = "Name";
    private static final String COMNAMELL = "Namell";
    private static final String COMCODE = "Code";
    private static final String MESUREUNIT = "Unit";
    private static final String MINQTY = "Min";
    private static final String PRICE = "Price";
    private static final String REQQTY = "Req";
    private static final String TOTQTY = "Tot";
    private static final String WEI = "Wei";

    //------------------------------PRINT TABLE
    private static final String PRINT_TABLE_NAME = "Print_Table";
    private static final String BAL = "bal_qty";
    private static final String CARRYOVER = "carry_over";
    private static final String INDIVIDUAL = "commIndividualAmount";
    private static final String CNAME = "comm_name";
    private static final String CNAMEL = "comm_name_ll";
    private static final String MNAME = "member_name";
    private static final String MNAMEL = "member_name_ll";
    private static final String RECIEPT = "reciept_id";
    private static final String RETAIL = "retail_price";
    private static final String PSCHEME = "scheme_desc_en";
    private static final String PSCHEMEL = "scheme_desc_ll";
    private static final String TOT = "tot_amount";
    private static final String TOTQ = "total_quantity";
    private static final String TTIME = "transaction_time";
    private static final String UIDREF = "uid_refer_no";

    //------------------------------MENU
    private static final String MENU_TABLE = "Menu_table";
    private static final String MAINMENU = "MainMenu";
    private static final String MENUNAME = "MenuName";
    private static final String SLNO = "Slno";
    private static final String STATUS = "Status";


    //------------------------------STOCK TABLE
    private static final String STOCK_TABLE_NAME = "Stock_Table";
    private static final String CB = "Cb";
    private static final String COMM = "Name";
    private static final String ISSUED = "Issue";
    private static final String STOCK = "Stock";
    private static final String SCHEME = "Shm";

    //------------------------------SALES TABLE
    private static final String SALES_TABLE_NAME = "Sales_Table";
    private static final String COMM_NAME = "Name";
    private static final String SALE = "Sale";
    private static final String SCHEMENAME = "Scheme";
    private static final String CARD = "Card";


    //------------------------------LANGUAGE TABLE
    private static final String LANGUAGE = "LANGUAGE";
    private static final String LANG_ = "LANG_";
    private static final String LANG = "LANG";


    //-------------------------------UIDSEEDING
    private static final String UIDSEEDING_TABLE_NAME = "UidSeeding_Table";

    private static final String UIDSEEDING_BFD_1 = "Uidseeding_Bfd_1";
    private static final String UIDSEEDING_BFD_2 = "Uidseeding_Bfd_2";
    private static final String UIDSEEDING_BFD_3 = "Uidseeding_Bfd_3";
    private static final String UIDSEEDING_MEMBERID = "Uidseeding_MemberId";
    private static final String UIDSEEDING_MEMBERNAME = "Uidseeding_MemberName";
    private static final String UIDSEEDING_MEMBERNAMELL = "Uidseeding_MemberNamell";
    private static final String UIDSEEDING_MEMBER_FUSION = "Uidseeding_MemberFusion";
    private static final String UIDSEEDING_UID = "Uidseeding_Uid";
    private static final String UIDSEEDING_W_UID_STATUS = "Uidseeding_W_Uid_Status";

    //-------------------------------BEN VERIFICATION
    private static final String BENEFICIARY_TABLE_NAME = "Beneficiary_Table";

    private static final String BENEFICIARY_MEMBERID = "Beneficiary_MemberId";
    private static final String BENEFICIARY_MEMEBERNAME = "Beneficiary_MemberName";
    private static final String BENEFICIARY_MEMBERNAMELL = "Beneficiary_MemberNamell";
    private static final String BENEFICIARY_MEMBERFUSION = "Beneficiary_Member_fusion";
    private static final String BENEFICIARY_UID = "Beneficiary_Uid";
    private static final String BENEFICIARY_VERIFICATION = "Beneficiary_Verification";
    private static final String BENEFICIARY_VERIFYSTATUSEN = "Beneficiary_VerifyStatus_en";
    private static final String BENEFICIARY_VERIFYSTATUSLL = "Beneficiary_VerifyStatus_ll";
    private static final String BENEFICIARY_W_UID_STATUS = "Beneficiary_W_Uid_Status";

    //-------------------------------INSPECTION
    private static final String INSPECTION_TABLE_NAME = "Inspection_Table";

    private static final String INSPECTION_CB = "Inspection_CB";
    private static final String INSPECTION_COMMCODE = "Inspection_CommCode";
    private static final String INSPECTION_COMMNAME = "Inspection_CommName";
    private static final String INSPECTION_COMMNAMELL = "Inspection_CommNamell";

    //-------------------------------INSPECTION APP
    private static final String INSPECTION_APP_TABLE_NAME = "Inspection_App_Table";

    private static final String INSPECTION_APP_KEY = "Inspection_App_Key";
    private static final String INSPECTION_APP_VALUE = "Inspection_App_Value";

    //-------------------------------RECIVE GOODS TRUCK

    private static final String RECIVEGOODS_TRUCK_TABLE_NAME = "Recivegoods_Truck_Table";

    private static final String RECIVEGOODS_TRUCK_FPSID = "Recivegoods_fpsId";
    private static final String RECIVEGOODS_TRUCK_AMONTH = "Recivegoods_allotedMonth";
    private static final String RECIVEGOODS_TRUCK_AYEAR = "Recivegoods_allotedYear";
    private static final String RECIVEGOODS_TRUCK_CHITNO = "Recivegoods_truckChitNo";
    private static final String RECIVEGOODS_TRUCK_CHALANID = "Recivegoods_challanId";
    private static final String RECIVEGOODS_TRUCK_AORDERNO = "Recivegoods_allocationOrderNo";
    private static final String RECIVEGOODS_TRUCK_TRUCKNO = "Recivegoods_truckNo";
    private static final String RECIVEGOODS_TRUCK_LENGTH = "Recivegoods_Length";

    //-------------------------------RECIVE GOODS COMM

    private static final String RECIVEGOODS_COMM_TABLE_NAME = "Recivegoods_Comm_Table";

    private static final String RECIVEGOODS_COMM_RELEASEDQTY = "Recivegoods_releasedQuantity";
    private static final String RECIVEGOODS_COMM_ALLOTMENT = "Recivegoods_allotment";
    private static final String RECIVEGOODS_COMM_COMMCODE = "Recivegoods_commCode";
    private static final String RECIVEGOODS_COMM_COMMNAME = "Recivegoods_commName";
    private static final String RECIVEGOODS_COMM_SCHEMEID = "Recivegoods_schemeId";
    private static final String RECIVEGOODS_COMM_SCHEMENAME = "Recivegoods_schemeName";

    //-------------------------------CANCEL REASON
    private static final String CANCEL_REQUEST_TABLE_NAME = "Cancel_Request_Table";
    private static final String CANCEL_ID = "Cancel_ID";
    private static final String CANCEL_REASON = "Cancel_Reason";



    private SQLiteDatabase db = this.getReadableDatabase();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + DEALERS_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,Dealers_Auth_Type TEXT ,Dealers_Fusion TEXT," +
                "Dealers_Type TEXT, Dealers_Names TEXT,Dealers_Names_Ll TEXT, Dealers_Uids TEXT,Dealers_Wadh TEXT )");

        //-------------------------------ISSUE------------------------------------------------------------------------------------------

        db.execSQL("create table " + MEMBERS_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,Member_Names TEXT ,Member_Namesll TEXT," +
                "Member_Fusion TEXT,Member_Uids TEXT,Member_Wadh TEXT, Member_Xfinger TEXT,Member_Yiris TEXT,Member_Zmanual TEXT," +
                " Member_Zmemberid TEXT,Member_Zotp TEXT, Member_Wadhauth TEXT)");

        db.execSQL("create table " + RATION_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,Type TEXT ,Month TEXT,Year TEXT,Avl TEXT ,Bal TEXT,Close TEXT," +
                "Name TEXT,Namell TEXT, Code TEXT, Unit TEXT,Min TEXT,Price TEXT,Req TEXT,Tot TEXT,Wei TEXT)");

        db.execSQL("create table " + PRINT_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,bal_qty TEXT ,carry_over TEXT,commIndividualAmount TEXT," +
                "comm_name TEXT, comm_name_ll TEXT, member_name TEXT,member_name_ll TEXT,reciept_id TEXT,retail_price TEXT,scheme_desc_en TEXT," +
                "scheme_desc_ll TEXT,tot_amount TEXT,total_quantity TEXT,transaction_time TEXT,uid_refer_no TEXT,allocationType TEXT,allotedMonth TEXT,allotedYear TEXT,commCode TEXT,closingBalance TEXT)");

        db.execSQL("create table " + MENU_TABLE + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,MainMenu TEXT,MenuName TEXT,Slno TEXT,Status TEXT)");

        //-----------------------------REPORTS-------------------------------------------------------------------------------------

        db.execSQL("create table " + STOCK_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,Cb TEXT,Name TEXT ,Shm TEXT,Stock TEXT," +
                "Issue TEXT)");

        db.execSQL("create table " + SALES_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT,Sale TEXT ,Scheme TEXT,Card TEXT)");

        //-----------------------------LANGUAGE--------------------------------------------------------------------------------------

        db.execSQL("create table " + LANGUAGE + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,LANG_ TEXT,LANG TEXT)");

        //-----------------------------AADHAAR SERVICES------------------------------------------------------------------------------------

        db.execSQL("create table " + UIDSEEDING_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,Uidseeding_Bfd_1 TEXT ,Uidseeding_Bfd_2 TEXT," +
                "Uidseeding_Bfd_3 TEXT, Uidseeding_MemberId TEXT,Uidseeding_MemberName TEXT, Uidseeding_MemberNamell TEXT,Uidseeding_MemberFusion TEXT,Uidseeding_Uid TEXT,Uidseeding_W_Uid_Status TEXT )");

        db.execSQL("create table " + BENEFICIARY_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,Beneficiary_MemberId TEXT ,Beneficiary_MemberName TEXT," +
                "Beneficiary_MemberNamell TEXT, Beneficiary_Member_fusion TEXT,Beneficiary_Uid TEXT, Beneficiary_Verification TEXT,Beneficiary_VerifyStatus_en TEXT,Beneficiary_VerifyStatus_ll TEXT,Beneficiary_W_Uid_Status TEXT )");

        //----------------------------INSPECTION-----------------------------------------------------------------------------------
        db.execSQL("create table " + INSPECTION_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,Inspection_CB TEXT,Inspection_CommCode TEXT ,Inspection_CommName TEXT,Inspection_CommNamell TEXT)");

        db.execSQL("create table " + INSPECTION_APP_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,Inspection_App_Key TEXT,Inspection_App_Value TEXT )");

        //----------------------------RECIVEGOODS
        db.execSQL("create table " + RECIVEGOODS_TRUCK_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,Recivegoods_fpsId TEXT,Recivegoods_allotedMonth TEXT " +
                ",Recivegoods_allotedYear TEXT,Recivegoods_truckChitNo TEXT,Recivegoods_challanId TEXT, Recivegoods_allocationOrderNo TEXT," +
                "Recivegoods_truckNo TEXT,Recivegoods_Length TEXT)");

        db.execSQL("create table " + RECIVEGOODS_COMM_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,Recivegoods_releasedQuantity TEXT,Recivegoods_allotment TEXT " +
                ",Recivegoods_commCode TEXT,Recivegoods_commName TEXT,Recivegoods_schemeId TEXT, Recivegoods_schemeName TEXT)");

        //-----------------------------CANCEL REQUEST
        db.execSQL("create table " + CANCEL_REQUEST_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,Cancel_ID TEXT,Cancel_Reason TEXT )");



        db.execSQL("CREATE TABLE IF NOT EXISTS PartialOnlineData(OffPassword text,OfflineLogin text,OfflineTxnTime text,Duration text,leftOfflineTime text,lastlogindate text,lastlogintime text,lastlogoutdate text,lastlogouttime text,AllotMonth text,AllotYear text,pOfflineStoppedDate text)");


        db.execSQL("CREATE TABLE IF NOT EXISTS KeyRegister(rcId text,commNameEn text,commNameLl text,commCode text,totalEntitlement text,balanceEntitlement text,schemeId text,schemeName text,commPrice text,Unit text,memberNameLl text,memberNameEn text,AllotMonth text,AllotYear text,allocationType text,allotedMonth text,allotedYear text)");


        db.execSQL("CREATE TABLE IF NOT EXISTS Pos_Ob(commCode text,commNameEn text,commNameLl text,closingBalance text)");


        db.execSQL("CREATE TABLE IF NOT EXISTS commodityMaster(commCode text,commNameEn text,commNameLl text,measurmentUnit text,commonCommCode text)");


        db.execSQL("CREATE TABLE IF NOT EXISTS schemeMaster(schemeId text,schemeName text)");


        db.execSQL("CREATE TABLE IF NOT EXISTS BenfiaryTxn(FpsId text,RcId text,SchemeId text,CommCode text,TotQty text,BalQty text,IssuedQty text,Rate text,commAmount text,TotAmt text,RecptId text,MemberName text,DateTime text,TxnUploadSts text,TxnType text,AllotMonth text, AllotYear text,allocationType text,memberId text)");


        db.execSQL("CREATE TABLE IF NOT EXISTS OfflineStockReceive(TruckChitNum text,Challan text,VechNum text,FpsId text,SchemeId text,CommCode text,RecvdQty text,Unit text,Month text,Year text,DateTime text,RecvdUploadSts text,RecvdMode text)");


        db.execSQL("CREATE TABLE IF NOT EXISTS StateInfo(stateCode TEXT, stateNameEn TEXT, stateNameLl TEXT, stateProfile TEXT, stateReceiptHeaderEn TEXT, stateReceiptHeaderLl TEXT, statefpsId TEXT,consentHeader TEXT,consentHeaderLl TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS memDetails (memberId text,memberNameEn text,memberNameLl text,rcId text)");
        //db.execSQL("CREATE TABLE IF NOT EXISTS OfflineStatusDetails(keyregisterDownloadstatus text)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

       /* db.execSQL("DROP TABLE IF EXISTS " + DEALERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MEMBERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RATION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PRINT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MENU_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + STOCK_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SALES_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UIDSEEDING_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BENEFICIARY_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + INSPECTION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + INSPECTION_APP_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RECIVEGOODS_TRUCK_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RECIVEGOODS_COMM_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CANCEL_REQUEST_TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS PartialOnlineData");
        db.execSQL("DROP TABLE IF EXISTS KeyRegister");
        db.execSQL("DROP TABLE IF EXISTS Pos_Ob");
        db.execSQL("DROP TABLE IF EXISTS commodityMaster");
        db.execSQL("DROP TABLE IF EXISTS schemeMaster");
        db.execSQL("DROP TABLE IF EXISTS BenfiaryTxn");
        db.execSQL("DROP TABLE IF EXISTS OfflineStockReceive");
         onCreate(db);*/


        if(oldVersion < 2){
            //db.execSQL("ALTER TABLE KeyRegister ADD COLUMN memberId text");
             db.execSQL("CREATE TABLE memDetails (memberId text,memberNameEn text,memberNameLl text,rcId text)");

        }
        if(oldVersion < 3){

            db.execSQL("ALTER TABLE BenfiaryTxn ADD COLUMN memberId text");

        }

    }


    public void deleteCancelRequest() {
        db = this.getWritableDatabase();
        db.execSQL("delete from " + CANCEL_REQUEST_TABLE_NAME);

        db.close();
    }
    public void deleterecivegoods_truck() {
        db = this.getWritableDatabase();
        db.execSQL("delete from " + RECIVEGOODS_TRUCK_TABLE_NAME);

        db.close();
    }

    public void deleterecivegoods_comm() {
        db = this.getWritableDatabase();
        db.execSQL("delete from " + RECIVEGOODS_COMM_TABLE_NAME);
        db.close();
    }

    public void deleteinspection_app() {
        db = this.getWritableDatabase();
        db.execSQL("delete from " + INSPECTION_APP_TABLE_NAME);
        db.close();
    }

    public void deleteinspection() {
        db = this.getWritableDatabase();
        db.execSQL("delete from " + INSPECTION_TABLE_NAME);
        db.close();
    }

    public void deletebenverification() {
        db = this.getWritableDatabase();
        db.execSQL("delete from " + BENEFICIARY_TABLE_NAME);
        db.close();
    }

    public void deleteuidseeding() {
        db = this.getWritableDatabase();
        db.execSQL("delete from " + UIDSEEDING_TABLE_NAME);
        db.close();
    }

    public void deletedealertable() {
        db = this.getWritableDatabase();
        db.execSQL("delete from " + DEALERS_TABLE_NAME);
        db.close();
    }

    public void deletemembertable() {
        db = this.getWritableDatabase();
        db.execSQL("delete from " + MEMBERS_TABLE_NAME);
        db.execSQL("delete from " + RATION_TABLE_NAME);
        db.close();
    }

    public void deleteprinttable() {
        db = this.getWritableDatabase();
        db.execSQL("delete from " + PRINT_TABLE_NAME);
        db.close();
    }

    public void deletestocktable() {
        db = this.getWritableDatabase();
        db.execSQL("delete from " + STOCK_TABLE_NAME);
        db.close();
    }

    public void deletesaletable() {
        db = this.getWritableDatabase();
        db.execSQL("delete from " + SALES_TABLE_NAME);
        db.close();
    }

    private void deletemenu() {
        db = this.getWritableDatabase();
        db.execSQL("delete from " + MENU_TABLE);
        db.close();
    }

    private void deleteDSD() {
        db = this.getWritableDatabase();
        db.execSQL("delete from " + SALES_TABLE_NAME);
        db.close();
    }

    private void deleteSD() {
        db = this.getWritableDatabase();
        db.execSQL("delete from " + STOCK_TABLE_NAME);
        db.close();
    }


    public void insert_L(String lang, String lng) {
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(LANG_, lang);
        cv.put(LANG, lng);
        long result = db.insert(LANGUAGE, null, cv);
        System.out.println("INSERTION RESULT = " + result);
        db.close();
    }

    public String get_L(String filename) {
        String st = "";
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.query(LANGUAGE, new String[]{LANG_, LANG}, LANG_ + "=?",
                new String[]{String.valueOf(filename)}, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            st = cursor.getString(cursor.getColumnIndex("LANG"));
            cursor.close();
            db.close();
            return st;
        } else {
            return st;
        }
    }

    public void update_L(String filename, String filedate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LANG_, filename);
        contentValues.put(LANG, filedate);
        db.update(LANGUAGE, contentValues, LANG_ + " = ?", new String[]{filename});
        db.close();
    }

    public int getfileCount() {
        String countQuery = "SELECT  * FROM " + LANGUAGE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public boolean isChallanExists(String ChallanID)
    {
        String query = "select * from OfflineStockReceive where Challan='" +ChallanID +"'";
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();
        if(count > 0)
            return true;
        return false;
    }

    public void insert_Cancel_Request(ArrayList<String> approveKey, ArrayList<String> approveValue) {
        deleteCancelRequest();
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (int i = 0; i < approveValue.size(); i++) {
            cv.put(CANCEL_ID, approveKey.get(i));
            cv.put(CANCEL_REASON, approveValue.get(i));

            long result = db.insert(CANCEL_REQUEST_TABLE_NAME, null, cv);
            System.out.println("INSERTION RESULT = " + result);
        }
        db.close();
    }

    public void insert_RECIVED_COMM(ArrayList<String> releasedQuantity, ArrayList<String> allotment, ArrayList<String> commCode,
                                    ArrayList<String> commName, ArrayList<String> schemeId, ArrayList<String> schemeName) {

        deleterecivegoods_comm();
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (int i = 0; i < releasedQuantity.size(); i++) {
            cv.put(RECIVEGOODS_COMM_RELEASEDQTY, releasedQuantity.get(i));
            cv.put(RECIVEGOODS_COMM_ALLOTMENT, allotment.get(i));
            cv.put(RECIVEGOODS_COMM_COMMCODE, commCode.get(i));
            cv.put(RECIVEGOODS_COMM_COMMNAME, commName.get(i));
            cv.put(RECIVEGOODS_COMM_SCHEMEID, schemeId.get(i));
            cv.put(RECIVEGOODS_COMM_SCHEMENAME, schemeName.get(i));
            long result = db.insert(RECIVEGOODS_COMM_TABLE_NAME, null, cv);
            System.out.println("INSERTION RESULT = " + result);
        }
        db.close();
    }

    public void insert_RECIVED_TRUCK(ArrayList<String> fpsId, ArrayList<String> allotedMonth, ArrayList<String> allotedYear, ArrayList<String> truckChitNo,
                                     ArrayList<String> challanId, ArrayList<String> allocationOrderNo, ArrayList<String> truckNo, ArrayList<String> commlength) {
        deleterecivegoods_truck();
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (int i = 0; i < fpsId.size(); i++) {
            cv.put(RECIVEGOODS_TRUCK_FPSID, fpsId.get(i));
            cv.put(RECIVEGOODS_TRUCK_AMONTH, allotedMonth.get(i));
            cv.put(RECIVEGOODS_TRUCK_AYEAR, allotedYear.get(i));
            cv.put(RECIVEGOODS_TRUCK_CHITNO, truckChitNo.get(i));
            cv.put(RECIVEGOODS_TRUCK_CHALANID, challanId.get(i));
            cv.put(RECIVEGOODS_TRUCK_AORDERNO, allocationOrderNo.get(i));
            cv.put(RECIVEGOODS_TRUCK_TRUCKNO, truckNo.get(i));
            cv.put(RECIVEGOODS_TRUCK_LENGTH, commlength.get(i));

            long result = db.insert(RECIVEGOODS_TRUCK_TABLE_NAME, null, cv);
            System.out.println("INSERTION RESULT = " + result);
        }
        db.close();
    }


    public void insert_INSPECTION_app(ArrayList<String> approveKey, ArrayList<String> approveValue) {
        deleteinspection_app();
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (int i = 0; i < approveValue.size(); i++) {
            cv.put(INSPECTION_APP_KEY, approveKey.get(i));
            cv.put(INSPECTION_APP_VALUE, approveValue.get(i));

            long result = db.insert(INSPECTION_APP_TABLE_NAME, null, cv);
            System.out.println("INSERTION RESULT = " + result);
        }
        db.close();
    }

    public void insert_INSPECTION(ArrayList<String> closingBalance, ArrayList<String> commCode, ArrayList<String> commNameEn, ArrayList<String> commNamell) {
        deleteinspection();
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (int i = 0; i < commCode.size(); i++) {
            cv.put(INSPECTION_CB, closingBalance.get(i));
            cv.put(INSPECTION_COMMCODE, commCode.get(i));
            cv.put(INSPECTION_COMMNAME, commNameEn.get(i));
            cv.put(INSPECTION_COMMNAMELL, commNamell.get(i));

            long result = db.insert(INSPECTION_TABLE_NAME, null, cv);
            System.out.println("INSERTION RESULT = " + result);
        }
        db.close();
    }


    public void insert_BEN(ArrayList<String> memberId, ArrayList<String> memberName, ArrayList<String> memberNamell, ArrayList<String> member_fusion,
                           ArrayList<String> uid, ArrayList<String> verification, ArrayList<String> verifyStatus_en, ArrayList<String> verifyStatus_ll,
                           ArrayList<String> w_uid_status) {
        deletebenverification();
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (int i = 0; i < memberId.size(); i++) {
            cv.put(BENEFICIARY_MEMBERID, memberId.get(i));
            cv.put(BENEFICIARY_MEMEBERNAME, memberName.get(i));
            cv.put(BENEFICIARY_MEMBERNAMELL, memberNamell.get(i));
            cv.put(BENEFICIARY_MEMBERFUSION, member_fusion.get(i));
            cv.put(BENEFICIARY_UID, uid.get(i));
            cv.put(BENEFICIARY_VERIFICATION, verification.get(i));
            cv.put(BENEFICIARY_VERIFYSTATUSEN, verifyStatus_en.get(i));
            cv.put(BENEFICIARY_VERIFYSTATUSLL, verifyStatus_ll.get(i));
            cv.put(BENEFICIARY_W_UID_STATUS, w_uid_status.get(i));
            long result = db.insert(BENEFICIARY_TABLE_NAME, null, cv);
            System.out.println("INSERTION RESULT = " + result);
        }
        db.close();
    }

    public void insert_UID(ArrayList<String> bfd1, ArrayList<String> bfd2, ArrayList<String> bfd3, ArrayList<String> memberId,
                           ArrayList<String> memberName, ArrayList<String> memberNamell, ArrayList<String> member_fusion, ArrayList<String> uid,
                           ArrayList<String> w_uid_status) {
        deleteuidseeding();
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (int i = 0; i < bfd1.size(); i++) {
            cv.put(UIDSEEDING_BFD_1, bfd1.get(i));
            cv.put(UIDSEEDING_BFD_2, bfd2.get(i));
            cv.put(UIDSEEDING_BFD_3, bfd3.get(i));
            cv.put(UIDSEEDING_MEMBERID, memberId.get(i));
            cv.put(UIDSEEDING_MEMBERNAME, memberName.get(i));
            cv.put(UIDSEEDING_MEMBERNAMELL, memberNamell.get(i));
            cv.put(UIDSEEDING_MEMBER_FUSION, member_fusion.get(i));
            cv.put(UIDSEEDING_UID, uid.get(i));
            cv.put(UIDSEEDING_W_UID_STATUS, w_uid_status.get(i));
            long result = db.insert(UIDSEEDING_TABLE_NAME, null, cv);
            System.out.println("INSERTION RESULT = " + result);
        }
        db.close();
    }

    public void insert_DD(String dauthtypes, String dfusion, String dtypes,
                          String dnames, String dnamell, String duids, String dwadh) {
        deletedealertable();
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
            cv.put(DEALERS_AUTH_TYPE, dauthtypes);
            cv.put(DEALERS_FUSION, dfusion);
            cv.put(DEALERS_TYPE, dtypes);
            cv.put(DEALERS_NAMES, dnames);
            cv.put(DEALERS_NAMES_LL, dnamell);
            cv.put(DEALERS_UIDS, duids);
            cv.put(DEALERS_WADH, dwadh);
            long result = db.insert(DEALERS_TABLE_NAME, null, cv);
            System.out.println("INSERTION RESULT = " + result);
        db.close();
    }

  /*  public void insert_DDD(ArrayList<String> ddetails) {
        deletedealerdetailstable();
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (int i = 0; i < ddetails.size(); i++) {
            cv.put(DEALER_DETAILS, ddetails.get(i));
            long result = db.insert(DEALERS_DETAILS_TABLE_NAME, null, cv);
            System.out.println("INSERTION RESULT = " + result);
        }
        db.close();
    }*/

    public void insert_MD(ArrayList<String> memberName, ArrayList<String> memberNamell, ArrayList<String> member_fusion,
                          ArrayList<String> uid, ArrayList<String> w_uid_status, ArrayList<String> xfinger, ArrayList<String> yiris,
                          ArrayList<String> zmanual, ArrayList<String> zmemberId, ArrayList<String> zotp, ArrayList<String> zwgenWadhAuth) {
        deletemembertable();
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (int i = 0; i < memberName.size(); i++) {
            cv.put(MEMBER_NAMES, memberName.get(i));
            System.out.println("MEMBER_NAMES=" + memberName.get(i));
            cv.put(MEMBER_NAMESLL, memberNamell.get(i));
            cv.put(MEMBER_FUSION, member_fusion.get(i));
            cv.put(MEMBER_UIDS, uid.get(i));
            cv.put(MEMBER_WADH, w_uid_status.get(i));
            cv.put(MEMBER_XFINGER, xfinger.get(i));
            cv.put(MEMBER_YIRIS, yiris.get(i));
            cv.put(MEMBER_ZMANUAL, zmanual.get(i));
            cv.put(MEMBER_ZMEMBERID, zmemberId.get(i));
            cv.put(MEMBER_ZOTP, zotp.get(i));
            cv.put(MEMBER_WADHAUTH, zwgenWadhAuth.get(i));
            long result = db.insert(MEMBERS_TABLE_NAME, null, cv);
            System.out.println("INSERTION RESULT = " + result);
        }
        db.close();
    }

    public void insert_RD(ArrayList<String> type, ArrayList<String> month, ArrayList<String> year,
                          ArrayList<String> avl, ArrayList<String> bal, ArrayList<String> close,
                          ArrayList<String> name, ArrayList<String> namell, ArrayList<String> code, ArrayList<String> unit,
                          ArrayList<String> min, ArrayList<String> price, ArrayList<String> req,
                          ArrayList<String> tot, ArrayList<String> wei) {
        db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        for (int i = 0; i < avl.size(); i++) {
            cv.put(TYPE, type.get(i));
            cv.put(MONTH, month.get(i));
            cv.put(YEAR, year.get(i));
            cv.put(AVLQTY, avl.get(i));
            System.out.println("AVLQTY=" + avl.get(i));
            cv.put(BALQTY, bal.get(i));
            cv.put(CLOSEBAL, close.get(i));
            cv.put(COMNAME, name.get(i));
            cv.put(COMNAMELL, namell.get(i));
            cv.put(COMCODE, code.get(i));
            cv.put(MESUREUNIT, unit.get(i));
            cv.put(MINQTY, min.get(i));
            cv.put(PRICE, price.get(i));
            cv.put(REQQTY, req.get(i));
            cv.put(TOTQTY, tot.get(i));
            cv.put(WEI, wei.get(i));
            db.insert(RATION_TABLE_NAME, null, cv);
        }
        db.close();
    }

    public void insert_PD(ArrayList<String> bal_qty, ArrayList<String> carry_over, ArrayList<String> commIndividualAmount,
                          ArrayList<String> comm_name, ArrayList<String> comm_name_ll, ArrayList<String> member_name,
                          ArrayList<String> member_name_ll, ArrayList<String> reciept_id, ArrayList<String> retail_price, ArrayList<String> scheme_desc_en,
                          ArrayList<String> scheme_desc_ll, ArrayList<String> tot_amount, ArrayList<String> total_quantity,
                          ArrayList<String> transaction_time, ArrayList<String> uid_refer_no) {
        deleteprinttable();
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        for (int i = 0; i < bal_qty.size(); i++) {
            cv.put(BAL, bal_qty.get(i));
            cv.put(CARRYOVER, carry_over.get(i));
            cv.put(INDIVIDUAL, commIndividualAmount.get(i));
            cv.put(CNAME, comm_name.get(i));
            System.out.println("BAL=" + bal_qty.get(i));
            cv.put(CNAMEL, comm_name_ll.get(i));
            cv.put(MNAME, member_name.get(i));
            cv.put(MNAMEL, member_name_ll.get(i));
            cv.put(RECIEPT, reciept_id.get(i));
            cv.put(RETAIL, retail_price.get(i));
            cv.put(PSCHEME, scheme_desc_en.get(i));
            cv.put(PSCHEMEL, scheme_desc_ll.get(i));
            cv.put(TOT, tot_amount.get(i));
            cv.put(TOTQ, total_quantity.get(i));
            cv.put(TTIME, transaction_time.get(i));
            cv.put(UIDREF, uid_refer_no.get(i));

            db.insert(PRINT_TABLE_NAME, null, cv);
        }

        db.close();
    }

    public void insert_MENU(ArrayList<String> mainmenu, ArrayList<String> menuname, ArrayList<String> salno, ArrayList<String> status) {

        deletemenu();
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (int i = 0; i < mainmenu.size(); i++) {
            cv.put(MAINMENU, mainmenu.get(i));
            System.out.println("MEMBER_NAMES=" + mainmenu.get(i));
            cv.put(MENUNAME, menuname.get(i));
            cv.put(SLNO, salno.get(i));
            cv.put(STATUS, status.get(i));
            long result = db.insert(MENU_TABLE, null, cv);
            System.out.println("INSERTION RESULT = " + result);
        }
        db.close();
    }

    public void insert_SD(ArrayList<String> stock) {
        deleteSD();
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CB, stock.get(0));
        cv.put(COMM, stock.get(1));
        System.out.println("STOCK=" + stock.get(1));
        cv.put(ISSUED, stock.get(2));
        cv.put(STOCK, stock.get(3));
        cv.put(SCHEME, stock.get(4));
        long result = db.insert(STOCK_TABLE_NAME, null, cv);
        System.out.println("INSERTION RESULT = " + result);
        db.close();
    }

    public void insert_DSD(ArrayList<String> sale) {
        deleteDSD();
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COMM_NAME, sale.get(0));
        cv.put(SALE, sale.get(1));
        System.out.println("STOCK=" + sale.get(1));
        cv.put(SCHEMENAME, sale.get(2));
        cv.put(CARD, sale.get(3));

        long result = db.insert(SALES_TABLE_NAME, null, cv);
        System.out.println("INSERTION RESULT = " + result);
        db.close();
    }

    public ArrayList<String> get_DD(int get) {
        ArrayList<String> array_list = new ArrayList<>();
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from Dealers_Table", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            switch (get) {
                case 0:
                    array_list.add(res.getString(res.getColumnIndex(DEALERS_AUTH_TYPE)));
                    break;
                case 1:
                    array_list.add(res.getString(res.getColumnIndex(DEALERS_TYPE)));
                    break;
                case 2:
                    array_list.add(res.getString(res.getColumnIndex(DEALERS_NAMES)));
                    break;
                case 3:
                    array_list.add(res.getString(res.getColumnIndex(DEALERS_UIDS)));
                    break;
                case 4:
                    array_list.add(res.getString(res.getColumnIndex(DEALERS_FUSION)));
                    break;
                case 5:
                    array_list.add(res.getString(res.getColumnIndex(DEALERS_NAMES_LL)));
                    break;
                case 6:
                    array_list.add(res.getString(res.getColumnIndex(DEALERS_WADH)));
                    break;
            }
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }

    public ArrayList<String> getStateDetails()
    {
        System.out.println("@@In getStateDetails...");
        ArrayList<String> array_list = new ArrayList<>();
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from StateInfo", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            System.out.println("@@Adding details to arraylist: "+res.getString(0));
            array_list.add(res.getString(0));
            array_list.add(res.getString(1));
            array_list.add(res.getString(2));
            array_list.add(res.getString(3));
            array_list.add(res.getString(4));
            array_list.add(res.getString(5));
            array_list.add(res.getString(6));
            array_list.add(res.getString(7));
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }

    public ArrayList<String> getCommodities() {
        ArrayList<String> array_list = new ArrayList<>();
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select distinct commNameEn from commodityMaster", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
                    array_list.add(res.getString(0));
                    res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }

    public ArrayList<String> getSchemeName() {
        ArrayList<String> array_list = new ArrayList<>();
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select distinct schemeName from schemeMaster", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            array_list.add(res.getString(0));
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }

    public ArrayList<String> get_MD(int get) {
        ArrayList<String> array_list = new ArrayList<>();
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from Members_Table", null);
        res.moveToFirst();
        System.out.println("COUNT" + res.getCount());
        while (!res.isAfterLast()) {
            switch (get) {
                case 0:
                    array_list.add(res.getString(res.getColumnIndex(MEMBER_NAMES)));
                    break;
                case 1:
                    array_list.add(res.getString(res.getColumnIndex(MEMBER_UIDS)));
                    break;
                case 2:
                    array_list.add(res.getString(res.getColumnIndex(MEMBER_XFINGER)));
                    break;
                case 3:
                    array_list.add(res.getString(res.getColumnIndex(MEMBER_YIRIS)));
                    break;
                case 4:
                    array_list.add(res.getString(res.getColumnIndex(MEMBER_ZMEMBERID)));
                    break;
                case 5:
                    array_list.add(res.getString(res.getColumnIndex(MEMBER_NAMESLL)));
                    break;
                case 6:
                    array_list.add(res.getString(res.getColumnIndex(MEMBER_FUSION)));
                    break;
                case 7:
                    array_list.add(res.getString(res.getColumnIndex(MEMBER_WADH)));
                    break;
                case 8:
                    array_list.add(res.getString(res.getColumnIndex(MEMBER_ZMANUAL)));
                    break;
                case 9:
                    array_list.add(res.getString(res.getColumnIndex(MEMBER_ZOTP)));
                    break;
                case 10:
                    array_list.add(res.getString(res.getColumnIndex(MEMBER_WADHAUTH)));
                    break;
            }
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }

    public ArrayList<String> get_RD(int get) {
        ArrayList<String> array_list = new ArrayList<>();
        //
        db= this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from Ration_Table", null);
        res.moveToFirst();
        System.out.println("COUNT" + res.getCount());
        while (!res.isAfterLast()) {
            switch (get) {
                case 0:
                    array_list.add(res.getString(res.getColumnIndex(AVLQTY)));
                    break;
                case 1:
                    array_list.add(res.getString(res.getColumnIndex(BALQTY)));
                    break;
                case 2:
                    array_list.add(res.getString(res.getColumnIndex(CLOSEBAL)));
                    break;
                case 3:
                    array_list.add(res.getString(res.getColumnIndex(COMNAME)));
                    break;
                case 4:
                    array_list.add(res.getString(res.getColumnIndex(COMCODE)));
                    break;
                case 5:
                    array_list.add(res.getString(res.getColumnIndex(MESUREUNIT)));
                    break;
                case 6:
                    array_list.add(res.getString(res.getColumnIndex(MINQTY)));
                    break;
                case 7:
                    array_list.add(res.getString(res.getColumnIndex(PRICE)));
                    break;
                case 8:
                    array_list.add(res.getString(res.getColumnIndex(REQQTY)));
                    break;
                case 9:
                    array_list.add(res.getString(res.getColumnIndex(TOTQTY)));
                    break;
                case 10:
                    array_list.add(res.getString(res.getColumnIndex(WEI)));
                    break;
                case 11:
                    array_list.add(res.getString(res.getColumnIndex(TYPE)));
                    break;
                case 12:
                    array_list.add(res.getString(res.getColumnIndex(MONTH)));
                    break;
                case 13:
                    array_list.add(res.getString(res.getColumnIndex(YEAR)));
                    break;
                case 14:
                    array_list.add(res.getString(res.getColumnIndex(COMNAMELL)));
                    break;
            }
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }

    public ArrayList<String> get_PD(int get) {
        ArrayList<String> array_list = new ArrayList<>();
        //
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from Print_Table", null);
        res.moveToFirst();
        System.out.println("COUNT" + res.getCount());
        while (!res.isAfterLast()) {
            switch (get) {
                case 0:
                    array_list.add(res.getString(res.getColumnIndex(BAL)));
                    break;
                case 1:
                    array_list.add(res.getString(res.getColumnIndex(CARRYOVER)));
                    break;
                case 2:
                    array_list.add(res.getString(res.getColumnIndex(INDIVIDUAL)));
                    break;
                case 3:
                    array_list.add(res.getString(res.getColumnIndex(CNAME)));
                    break;
                case 4:
                    array_list.add(res.getString(res.getColumnIndex(CNAMEL)));
                    break;
                case 5:
                    array_list.add(res.getString(res.getColumnIndex(MNAME)));
                    break;
                case 6:
                    array_list.add(res.getString(res.getColumnIndex(MNAMEL)));
                    break;
                case 7:
                    array_list.add(res.getString(res.getColumnIndex(RECIEPT)));
                    break;
                case 8:
                    array_list.add(res.getString(res.getColumnIndex(RETAIL)));
                    break;
                case 9:
                    array_list.add(res.getString(res.getColumnIndex(PSCHEME)));
                    break;
                case 10:
                    array_list.add(res.getString(res.getColumnIndex(PSCHEMEL)));
                    break;
                case 11:
                    array_list.add(res.getString(res.getColumnIndex(TOT)));
                    break;
                case 12:
                    array_list.add(res.getString(res.getColumnIndex(TOTQ)));
                    break;
                case 13:
                    array_list.add(res.getString(res.getColumnIndex(TTIME)));
                    break;
                case 14:
                    array_list.add(res.getString(res.getColumnIndex(UIDREF)));
                    break;
            }
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }

   /* public ArrayList<String> get_PD(int get) {
        ArrayList<String> array_list = new ArrayList<>();

        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from Print_Table", null);
        res.moveToFirst();
        System.out.println("COUNT" + res.getCount());
        while (!res.isAfterLast()) {
            switch (get) {
                case 0:
                    array_list.add(res.getString(res.getColumnIndex(BAL)));
                    break;
                case 1:
                    array_list.add(res.getString(res.getColumnIndex(CARRYOVER)));
                    break;
                case 2:
                    array_list.add(res.getString(res.getColumnIndex(INDIVIDUAL)));
                    break;
                case 3:
                    array_list.add(res.getString(res.getColumnIndex(CNAME)));
                    break;
                case 4:
                    array_list.add(res.getString(res.getColumnIndex(MNAME)));
                    break;
                case 5:
                    array_list.add(res.getString(res.getColumnIndex(RECIEPT)));
                    break;
                case 6:
                    array_list.add(res.getString(res.getColumnIndex(RETAIL)));
                    break;
                case 7:
                    array_list.add(res.getString(res.getColumnIndex(TOT)));
                    break;
                case 8:
                    array_list.add(res.getString(res.getColumnIndex(TOTQ)));
                    break;
                case 9:
                    array_list.add(res.getString(res.getColumnIndex(TTIME)));
                    break;

            }
            res.moveToNext();
        }
        return array_list;
    }*/

    public ArrayList<String> get_MENU(int get) {
        ArrayList<String> array_list = new ArrayList<>();
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from Menu_Table", null);
        res.moveToFirst();
        System.out.println("COUNT" + res.getCount());
        while (!res.isAfterLast()) {
            switch (get) {
                case 0:
                    array_list.add(res.getString(res.getColumnIndex(MAINMENU)));
                    break;
                case 1:
                    array_list.add(res.getString(res.getColumnIndex(MENUNAME)));
                    break;
                case 2:
                    array_list.add(res.getString(res.getColumnIndex(SLNO)));
                    break;
                case 3:
                    array_list.add(res.getString(res.getColumnIndex(STATUS)));
                    break;
            }
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }

    public ArrayList<Integer> get_MENU_serial(int get) {

        ArrayList<Integer> int_array_list = new ArrayList<>();
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from Menu_Table", null);
        res.moveToFirst();
        System.out.println("COUNT" + res.getCount());
        while (!res.isAfterLast()) {
            if (get == 1) {
                int_array_list.add(Integer.valueOf(res.getString(res.getColumnIndex(SLNO))));
            }
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return int_array_list;
    }


    public ArrayList<String> get_SD(int get) {
        ArrayList<String> array_list = new ArrayList<>();
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from STOCK_TABLE", null);
        res.moveToFirst();
        System.out.println("COUNT" + res.getCount());
        while (!res.isAfterLast()) {
            switch (get) {
                case 0:
                    array_list.add(res.getString(res.getColumnIndex(CB)));
                    break;
                case 1:
                    array_list.add(res.getString(res.getColumnIndex(COMM)));
                    break;
                case 2:
                    array_list.add(res.getString(res.getColumnIndex(ISSUED)));
                    break;
                case 3:
                    array_list.add(res.getString(res.getColumnIndex(STOCK)));
                    break;
                case 4:
                    array_list.add(res.getString(res.getColumnIndex(SCHEME)));
                    break;
            }
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }

    public ArrayList<String> get_DSD(int get) {
        ArrayList<String> array_list = new ArrayList<>();
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from Sales_TABLE", null);
        res.moveToFirst();
        System.out.println("COUNT" + res.getCount());
        while (!res.isAfterLast()) {
            switch (get) {
                case 0:
                    array_list.add(res.getString(res.getColumnIndex(COMM_NAME)));
                    break;
                case 1:
                    array_list.add(res.getString(res.getColumnIndex(SALE)));
                    break;
                case 2:
                    array_list.add(res.getString(res.getColumnIndex(SCHEMENAME)));
                    break;
                case 3:
                    array_list.add(res.getString(res.getColumnIndex(CARD)));
                    break;
            }
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }

    public ArrayList<String> get_UID(int get) {
        ArrayList<String> array_list = new ArrayList<>();
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from UidSeeding_Table", null);
        res.moveToFirst();
        System.out.println("COUNT" + res.getCount());
        while (!res.isAfterLast()) {
            switch (get) {
                case 0:
                    array_list.add(res.getString(res.getColumnIndex(UIDSEEDING_BFD_1)));
                    break;
                case 1:
                    array_list.add(res.getString(res.getColumnIndex(UIDSEEDING_BFD_2)));
                    break;
                case 2:
                    array_list.add(res.getString(res.getColumnIndex(UIDSEEDING_BFD_3)));
                    break;
                case 3:
                    array_list.add(res.getString(res.getColumnIndex(UIDSEEDING_MEMBERID)));
                    break;
                case 4:
                    array_list.add(res.getString(res.getColumnIndex(UIDSEEDING_MEMBERNAME)));
                    break;
                case 5:
                    array_list.add(res.getString(res.getColumnIndex(UIDSEEDING_MEMBERNAMELL)));
                    break;
                case 6:
                    array_list.add(res.getString(res.getColumnIndex(UIDSEEDING_MEMBER_FUSION)));
                    break;
                case 7:
                    array_list.add(res.getString(res.getColumnIndex(UIDSEEDING_UID)));
                    break;
                case 8:
                    array_list.add(res.getString(res.getColumnIndex(UIDSEEDING_W_UID_STATUS)));
                    break;

            }
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }

    public ArrayList<String> get_BEN(int get) {
        ArrayList<String> array_list = new ArrayList<>();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from Beneficiary_Table", null);
        res.moveToFirst();
        System.out.println("COUNT" + res.getCount());
        while (!res.isAfterLast()) {
            switch (get) {
                case 0:
                    array_list.add(res.getString(res.getColumnIndex(BENEFICIARY_MEMBERID)));
                    break;
                case 1:
                    array_list.add(res.getString(res.getColumnIndex(BENEFICIARY_MEMEBERNAME)));
                    break;
                case 2:
                    array_list.add(res.getString(res.getColumnIndex(BENEFICIARY_MEMBERNAMELL)));
                    break;
                case 3:
                    array_list.add(res.getString(res.getColumnIndex(BENEFICIARY_MEMBERFUSION)));
                    break;
                case 4:
                    array_list.add(res.getString(res.getColumnIndex(BENEFICIARY_UID)));
                    break;
                case 5:
                    array_list.add(res.getString(res.getColumnIndex(BENEFICIARY_VERIFICATION)));
                    break;
                case 6:
                    array_list.add(res.getString(res.getColumnIndex(BENEFICIARY_VERIFYSTATUSEN)));
                    break;
                case 7:
                    array_list.add(res.getString(res.getColumnIndex(BENEFICIARY_VERIFYSTATUSLL)));
                    break;
                case 8:
                    array_list.add(res.getString(res.getColumnIndex(BENEFICIARY_W_UID_STATUS)));
                    break;

            }
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }

    public ArrayList<String> get_INSPECTION(int get) {
        ArrayList<String> array_list = new ArrayList<>();
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from Inspection_Table", null);
        res.moveToFirst();
        System.out.println("COUNT" + res.getCount());
        while (!res.isAfterLast()) {
            switch (get) {
                case 0:
                    array_list.add(res.getString(res.getColumnIndex(INSPECTION_CB)));
                    break;
                case 1:
                    array_list.add(res.getString(res.getColumnIndex(INSPECTION_COMMCODE)));
                    break;
                case 2:
                    array_list.add(res.getString(res.getColumnIndex(INSPECTION_COMMNAME)));
                    break;
                case 3:
                    array_list.add(res.getString(res.getColumnIndex(INSPECTION_COMMNAMELL)));
                    break;
            }
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }

    public ArrayList<String> get_APP(int get) {
        ArrayList<String> array_list = new ArrayList<>();
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from Inspection_App_Table ", null);
        res.moveToFirst();
        System.out.println("COUNT" + res.getCount());
        while (!res.isAfterLast()) {
            switch (get) {
                case 0:
                    array_list.add(res.getString(res.getColumnIndex(INSPECTION_APP_KEY)));
                    break;
                case 1:
                    array_list.add(res.getString(res.getColumnIndex(INSPECTION_APP_VALUE)));
                    break;
            }
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }


    public ArrayList<String> get_RC_Truck(int get) {
        ArrayList<String> array_list = new ArrayList<>();
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from Recivegoods_Truck_Table", null);
        res.moveToFirst();
        System.out.println("COUNT" + res.getCount());
        while (!res.isAfterLast()) {
            switch (get) {
                case 0:
                    array_list.add(res.getString(res.getColumnIndex(RECIVEGOODS_TRUCK_FPSID)));
                    break;
                case 1:
                    array_list.add(res.getString(res.getColumnIndex(RECIVEGOODS_TRUCK_AMONTH)));
                    break;
                case 2:
                    array_list.add(res.getString(res.getColumnIndex(RECIVEGOODS_TRUCK_AYEAR)));
                    break;
                case 3:
                    array_list.add(res.getString(res.getColumnIndex(RECIVEGOODS_TRUCK_CHITNO)));
                    break;
                case 4:
                    array_list.add(res.getString(res.getColumnIndex(RECIVEGOODS_TRUCK_CHALANID)));
                    break;
                case 5:
                    array_list.add(res.getString(res.getColumnIndex(RECIVEGOODS_TRUCK_AORDERNO)));
                    break;
                case 6:
                    array_list.add(res.getString(res.getColumnIndex(RECIVEGOODS_TRUCK_TRUCKNO)));
                    break;
                case 7:
                    array_list.add(res.getString(res.getColumnIndex(RECIVEGOODS_TRUCK_LENGTH)));
                    break;
            }
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }

    public ArrayList<String> get_RC_Comm(int get) {
        ArrayList<String> array_list = new ArrayList<>();
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from Recivegoods_Comm_Table", null);
        res.moveToFirst();
        System.out.println("COUNT" + res.getCount());
        while (!res.isAfterLast()) {
            switch (get) {
                case 0:
                    array_list.add(res.getString(res.getColumnIndex(RECIVEGOODS_COMM_RELEASEDQTY)));
                    break;
                case 1:
                    array_list.add(res.getString(res.getColumnIndex(RECIVEGOODS_COMM_ALLOTMENT)));
                    break;
                case 2:
                    array_list.add(res.getString(res.getColumnIndex(RECIVEGOODS_COMM_COMMCODE)));
                    break;
                case 3:
                    array_list.add(res.getString(res.getColumnIndex(RECIVEGOODS_COMM_COMMNAME)));
                    break;
                case 4:
                    array_list.add(res.getString(res.getColumnIndex(RECIVEGOODS_COMM_SCHEMEID)));
                    break;
                case 5:
                    array_list.add(res.getString(res.getColumnIndex(RECIVEGOODS_COMM_SCHEMENAME)));
                    break;
            }
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }

    public ArrayList<String> get_CancelRequest(int get) {
        ArrayList<String> array_list = new ArrayList<>();
        db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from Cancel_Request_Table ", null);
        res.moveToFirst();
        System.out.println("COUNT" + res.getCount());
        while (!res.isAfterLast()) {
            switch (get) {
                case 0:
                    array_list.add(res.getString(res.getColumnIndex(CANCEL_ID)));
                    break;
                case 1:
                    array_list.add(res.getString(res.getColumnIndex(CANCEL_REASON)));
                    break;
            }
            res.moveToNext();
        }
        if(db.isOpen())
            db.close();
        return array_list;
    }


    //--------------------------OFFLINE------------------------------


    public int isDataAvailable(Context context) throws SQLException {
        DatabaseHelper helper = new DatabaseHelper(context);
        String query = "select * from PartialOnlineData";
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor res = db.rawQuery(query,null);
        String password = "";
        if(res.moveToFirst())
        {
            System.out.println("@@Data available in partialOnlineData");
            db.close();
            return 0;
        }
        System.out.println("@@Data is not available in partialOnlineData");
        db.close();
        return -1;
    }

    public String[] getMonthYear(Context context) throws SQLException {
        DatabaseHelper helper = new DatabaseHelper(context);
        String monthYear[] = new String[2];
        SQLiteDatabase db = helper.getWritableDatabase();

        try
        {
            String query = "select allotedMonth,allotedYear from KeyRegister";
            Cursor res =  db.rawQuery(query, null);
            //res.moveToFirst();

            if (res.moveToFirst()) {
                monthYear[0] = res.getString(0);
                monthYear[1] = res.getString(1);
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            db.close();
            helper.close();
            return monthYear;
        }
    }

    public long insertOrReplacePartialOnlineData(Context context,ContentValues contentValues)
    {
        System.out.println("@@ insertOrReplacePartialOnlineData");
        System.out.println("@@Data in contentValues: " +contentValues);
        System.out.println("@@Data in contentValues: " +contentValues.toString());
        long result = 0L;
        db = this.getWritableDatabase();
        try {
            db.delete("PartialOnlineData",null,null);
            result = db.insert("PartialOnlineData",null,contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            return result;
        }

    }

    public int insertStateDetails(String stateCode,String stateNameEn,String stateNameLl,String staterofile,String stateRecHeaderEn,String stateRecHeaderLl,String statefpsID,String consentHeader,String consentHeaderLl)
    {
        System.out.println("@@In insertStateDetails");
        System.out.println("@@Data received: StateCode:" +stateCode);
        System.out.println("@@Data received: StateCode:" +stateNameEn);
        System.out.println("@@Data received: StateCode:" +stateNameLl);
        System.out.println("@@Data received: StateCode:" +staterofile);
        System.out.println("@@Data received: StateCode:" +stateRecHeaderEn);
        System.out.println("@@Data received: StateCode:" +stateRecHeaderLl);
        System.out.println("@@Data received: StateCode:" +statefpsID);
        System.out.println("@@Data received: StateCode:" +consentHeader);
        System.out.println("@@Data received: StateCode:" +consentHeaderLl);

        consentHeader = consentHeader.replace(',',' ');
        consentHeaderLl = consentHeaderLl.replace(',',' ');

        stateRecHeaderEn = stateRecHeaderEn.replace(',',' ');
        stateRecHeaderLl = stateRecHeaderLl.replace(',',' ');

        int count = 0;
        try {
            String query = "INSERT INTO StateInfo values('" +stateCode +"','" +stateNameEn +"','" +stateNameLl +"','" +staterofile +"','" +stateRecHeaderEn +"','" +stateRecHeaderLl +"','" +statefpsID +"','" +consentHeader +"','" +consentHeaderLl +"')";
            System.out.println("@@Query: " +query);
            db = this.getWritableDatabase();
            db.delete("StateInfo", null, null);

            //System.out.println("@@Query: " +query);
            db.execSQL(query);
            System.out.println("@@Going to getEntitlementcount");
            count = getEntitlementCount("StateInfo");
        } catch (SQLException throwables) {
            System.out.println("@@Exception inserting into insertStateDetails: " +throwables.toString());
            throwables.printStackTrace();
        } finally {
            if(db.isOpen())
                db.close();
            return count;
        }
    }

    public int getEntitlementCount(String tableName) throws SQLException {
        System.out.println("@@In getEntitlementCount");
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select count(*) from "+tableName;
        Cursor res = db.rawQuery(query,null);
        System.out.println("RESULT>>>>>>>>"+res);
        if(res.moveToFirst())
        {
            int count = res.getInt(0);
            System.out.println("@@Data available in " +tableName);

            db.close();
            return count;
        }
        System.out.println("@@Data is not available in " +tableName);
        db.close();
        return 0;
    }

    public String loginByPassword(Context context,String password)
    {
        System.out.println("@@In loginByPassword");
        String ret = "";
        SQLiteDatabase sqLiteDatabase = null;
        try {
            PartialOnlineData partialOnlineData = getPartialOnlineData();
            if(!partialOnlineData.getOfflineLogin().equals("Y"))
            {
                System.out.println("@@Online login");
                Log.e("[loginByPassword]","No Network and Offline Login data not Available");
                ret = "No Network and Offline Login data not Available";
            }

            System.out.println("@@Password entered: " +password);
            System.out.println("@@Password in DB: " +partialOnlineData.getOffPassword());

            if(!partialOnlineData.getOffPassword().equals(password))
            {
                System.out.println("@@Password mismatch,,,");
                Log.e("[loginByPassword]","Invalid password");
                ret = "Invalid password";
            }
            {
                String time[];
                time = partialOnlineData.getOfflineTxnTime().split("-");
                SimpleDateFormat parser = new SimpleDateFormat("hhaa");
                Date startTime = parser.parse(time[0]);
                Date endTimme = parser.parse(time[1]);
                Date userDate = parser.parse(parser.format(new Date()));
                if (userDate.before(startTime) || userDate.after(endTimme)) {
                    Log.e("[loginByPassword]","This not allowed time to login");
                    ret = "This not allowed time to login";
                }
            }

            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date currentDatetime = new Date();
                Date currentDate = dateFormat.parse(dateFormat.format(currentDatetime));


                Date stoppedDate = dateFormat.parse(partialOnlineData.getpOfflineStoppedDate());

                System.out.println("@@Stopped date: " +stoppedDate);
                System.out.println("@@CURRENT date: " +currentDate);
                System.out.println("<<<<<COMPARE>>>");

                if(stoppedDate.compareTo(currentDate) < 0)
                {
                    Log.e("[loginByPassword]","Month Changed\nPlease login by authentication");
                    ret = "Month Changed\nPlease login by authentication";

                }
            }
            sqLiteDatabase = this.getWritableDatabase();
            sqLiteDatabase.execSQL("update PartialOnlineData set lastlogindate = Date('now','localtime'),lastlogintime = Time('HH:mm:ss'),lastlogoutdate = Date('now','localtime'),lastlogouttime = Time('HH:mm:ss')");



            /*Need to complete two more cases*/

        }  catch (ParseException e) {
            e.printStackTrace();
            ret = "Offline Data Not available,Please Start in online mode";
        }
        catch (Exception e) {
            e.printStackTrace();
            ret = "Offline Data Not available,Please Start in online mode";
        }
        finally {
            if(sqLiteDatabase != null && sqLiteDatabase.isOpen())
                sqLiteDatabase.close();
            return ret;
        }
    }

    public  String txnAllotedBetweenTime()
    {
        PartialOnlineData partialOnlineData = null;
        String errormessage = "";
        try {
            partialOnlineData = getPartialOnlineData();
            {
                String time[];
                time = partialOnlineData.getOfflineTxnTime().split("-");
                SimpleDateFormat parser = new SimpleDateFormat("hhaa");
                Date startTime = parser.parse(time[0]);
                Date endTimme = parser.parse(time[1]);
                Date userDate = parser.parse(parser.format(new Date()));
                if (userDate.before(startTime) || userDate.after(endTimme)) {
                    Log.e("[txnAllotedBetweenTime]","This not allowed time to login");
                    return  "This not allowed time to distribute";
                }
            }
            {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                Date currentDate = new Date();
                Date stoppedDate = df.parse(partialOnlineData.getpOfflineStoppedDate());
                System.out.println("@@Current date: " +currentDate);
                System.out.println("@@Distribution stopped date: " +stoppedDate);
                int dateFlag = Util.compareDates(currentDate,stoppedDate);
                if(dateFlag > 0)
                {
                    Log.e("[txnAllotedBetweenTime]","Month Changed\nPlease login by authentication");
                    return "Month Changed\nPlease login by authentication";

                }
            }

            /*
            * if(DealerLoginBy == 2)
    {
        query.clear();
        if(!query.exec("select leftOfflineTime from PartialOnlineData limit 1"))
        {
            return -1;
        }
        query.next();
        memset(TmpStr,0x00,sizeof(TmpStr));
        if(query.value(0).toString().toInt()<=0)
        {
            if(LangMode == ENGLISH)
                miscObj.DisplayWideMsgBox((char *)"Offline time duration completed\nLogin by authentication");
            else if(LangMode == HINDI)
                miscObj.tel_DisplayWideMsgBox((char *)"ऑफ़लाइन समय अवधि पूरी हुई\nकृपया प्रमाणीकरण द्वारा लॉगिन करें");
            PartialOnlineTimer->stop();
            ui->stackedWidget->setCurrentIndex(0);
            return -1;
        }
    }*/

        } catch (ParseException e) {
            e.printStackTrace();
            errormessage = e.getLocalizedMessage();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            errormessage = e.getLocalizedMessage();
        }
        return errormessage;
    }

    public boolean updateOfflineData(Context context, Print printData, String rcNumber, String txnType,String deviceTxnId,String orderDateTime,String membername,String memberId)
    {
        System.out.println("@@Updating offline data");
        System.out.println("MemberId =="+memberId);
        DatabaseHelper helper = new DatabaseHelper(context);
        boolean isSuccessful = false;
        int databaseUpdateCount = 0;
        ExcessData excessData = getExcessData(rcNumber);
        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();

        PartialOnlineData partialOnlineData = getPartialOnlineData();
        sqLiteDatabase.beginTransaction();
        try
        {

            for (printBeans printBeanItem:printData.printBeans)
            {

                System.out.println("@@RC: "+rcNumber +"ComCode: " +printBeanItem.getCommCode() +", Allocation type: "+printBeanItem.getAllocationType() +", Alloted month: " +printBeanItem.getAllotedMonth() +", aLLOTED YEAR: "+printBeanItem.getAllotedYear());

                ContentValues contentValues = new ContentValues();
                Double balanceQuantity = Double.parseDouble(printBeanItem.getBal_qty());
                System.out.println("@@Bal qty: " +balanceQuantity);
                Double requestQty = Double.parseDouble(printBeanItem.getCarry_over());
                System.out.println("@@Req qty: " +requestQty);
                contentValues.put("balanceEntitlement",printBeanItem.getBal_qty());
                int effeteddRows = sqLiteDatabase.update("KeyRegister",contentValues,"rcId=? AND commCode=? AND allocationType=? AND allotedMonth=? AND allotedYear=?",
                        new String[]{rcNumber,printBeanItem.getCommCode(),printBeanItem.getAllocationType(),printBeanItem.getAllotedMonth(),printBeanItem.getAllotedYear()});
                if(effeteddRows >= 1)
                {
                    System.out.println("@@effected 1 row of keyregister in updateOfflineData");
                    //Log.d("[updateOfflineData]",String.format(" update KeyRegister set balanceEntitlement = %Lf where rcId=%s AND commCode=%s AND allocationType=%s AND allotedMonth=%s AND allotedYear=%s ==> effected Rows :: %d",
                    //rationModel.getBalanceQty()-rationModel.getReq(),rcNumber,rationModel.getCommodityCode(),rationModel.getType(),rationModel.getMonth(),rationModel.getYear(),effeteddRows));
                    Double closingQty = Double.parseDouble(printBeanItem.getClosingBal());
                    contentValues.clear();

                    contentValues.put("closingBalance",closingQty-requestQty);
                    effeteddRows = sqLiteDatabase.update("pos_ob",contentValues,"commCode=?",new String[]{printBeanItem.getCommCode()});
                    if(effeteddRows >=1)
                    {
                        //Log.d("[updateOfflineData]",String.format("update pos_ob set closingBalance = %Lf where commCode = %s ==> Effected Rows :: %d",rationModel.getClosing()-rationModel.getReq(),rationModel.getCommodityCode(),effeteddRows));
                        System.out.println("@@effected 1 row of pos_ob in updateOfflineData");
                        Double price = Double.parseDouble(printBeanItem.getRetail_price());
                        Double commodityAmount = price*requestQty;

                        contentValues.clear();
                        contentValues.put("FpsId",partialOnlineData.getOffPassword());//// Where it exists in Database?
                        contentValues.put("RcId",rcNumber);
                        contentValues.put("SchemeId",excessData.getSchemeId());//
                        contentValues.put("CommCode",printBeanItem.getCommCode());
                        contentValues.put("TotQty",printBeanItem.getTotal_quantity());
                        contentValues.put("BalQty",balanceQuantity+requestQty);
                        contentValues.put("IssuedQty",requestQty);
                        contentValues.put("Rate",printBeanItem.getRetail_price());
                        contentValues.put("commAmount",printBeanItem.getRetail_price());////Q Diff between commAmount and TotAmt?
                        contentValues.put("TotAmt",commodityAmount);//
                        contentValues.put("RecptId",deviceTxnId);//Q How to generete
                        contentValues.put("MemberName",membername);//Q Not showing members in Offline ,then what shoud take?
                        contentValues.put("DateTime",orderDateTime);
                        contentValues.put("MemberId",memberId);
                        System.out.println("@@Data in TxnUploadSts123456: " +txnType);
                        contentValues.put("TxnUploadSts",txnType.equalsIgnoreCase("Online")?"Y":"N");
                        if(txnType.equalsIgnoreCase("Online"))
                        {
                            System.out.println("@@Online updating O");
                            contentValues.put("TxnType","O");
                        }
                        else if(txnType.equalsIgnoreCase("Partial"))
                        {
                            System.out.println("@@Online updating P");
                            contentValues.put("TxnType","P");
                        }
                        else{
                            System.out.println("@@Oflfine updating Q");
                            contentValues.put("TxnType","Q");
                        }

                        //contentValues.put("TxnUploadSts","N");
                        //contentValues.put("TxnType","Q");
                        //O,Q,P
                        contentValues.put("AllotMonth",printBeanItem.getAllotedMonth());
                        contentValues.put("AllotYear",printBeanItem.getAllotedYear());
                        contentValues.put("allocationType",printBeanItem.getAllocationType());

                        long x = sqLiteDatabase.insert("BenfiaryTxn",null,contentValues);
                        if(x > 0)
                        {
                            System.out.println("@@x>0");
                            databaseUpdateCount++;
                            System.out.println("@@Data in dbupdatecount: " +databaseUpdateCount);
                        }
                        else break;
                    }
                    else break;
                }
                else break;
            }
            if(databaseUpdateCount == printData.printBeans.size())
            {
                System.out.println("@@Transaction success");
                sqLiteDatabase.setTransactionSuccessful();
                isSuccessful = true;
            }
            else
            {
                System.out.println("@@Updation is not succesfull");
                Log.d("[updateOfflineData]"," Update not Successful databaseUpdateCount :: "+databaseUpdateCount+",rationModels.size() :: "+printData.printBeans.size());
            }

        }
        catch (Exception e)
        {
            System.out.println("@@Exception in DATABASEHELPER updateOfflineData method: " +e.toString());
            e.printStackTrace();
        }
        finally {
            sqLiteDatabase.endTransaction();
            if(sqLiteDatabase.isOpen())
                sqLiteDatabase.close();
            helper.close();
        }
        return isSuccessful;
    }

    public PartialOnlineData getPartialOnlineData() {

        PartialOnlineData partialOnlineData = null;
        SQLiteDatabase db = this.getReadableDatabase();
        try
        {
            String query = "select OffPassword,OfflineLogin,OfflineTxnTime,Duration,leftOfflineTime,lastlogindate,lastlogintime,lastlogoutdate,lastlogouttime,AllotMonth,AllotYear,pOfflineStoppedDate from PartialOnlineData";
            Cursor res = db.rawQuery(query,null);
            System.out.println("RESULT>>>>>>>>"+res);
            if(res.moveToFirst())
            {
                partialOnlineData = new PartialOnlineData();
                System.out.println("+++++++++++Data available in partialOnlineData");
                partialOnlineData.setOffPassword(res.getString(0));
                partialOnlineData.setOfflineLogin(res.getString(1));
                System.out.println("@@Data in offline login: " +res.getString(1));
                partialOnlineData.setOfflineTxnTime(res.getString(2));
                partialOnlineData.setDuration(res.getString(3));
                partialOnlineData.setLeftOfflineTime(res.getString(4));
                partialOnlineData.setLastlogindate(res.getString(5));
                partialOnlineData.setLastlogintime(res.getString(6));
                partialOnlineData.setLastlogoutdate(res.getString(7));
                partialOnlineData.setLastlogouttime(res.getString(8));
                partialOnlineData.setAllotMonth(res.getString(9));
                partialOnlineData.setAllotYear(res.getString(10));
                partialOnlineData.setpOfflineStoppedDate(res.getString(11));
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            return partialOnlineData;
        }
    }

    public  int checkForOfflineDistribution()
    {
        PartialOnlineData partialOnlineData = getPartialOnlineData();

        if(partialOnlineData == null ||  partialOnlineData.getOfflineLogin()==null)
            return -1;
        else if(partialOnlineData.getOfflineLogin().equals("Y"))
            return 0;
        else if(partialOnlineData.getOfflineLogin().equals("N"))
            return 1;
        else return -2;
    }

    public List<commDetails> getCommodityDetails(String rcNumber)
    {
        List<commDetails> commodityDetails= new ArrayList<>();
        db = this.getReadableDatabase();
        try {
            String query = "SELECT KR.allocationType, KR.allotedMonth, KR.allotedYear, KR.totalEntitlement - KR.balanceEntitlement, KR.balanceEntitlement, PO.closingBalance, KR.commNameEn, KR.commNameLl, KR.commcode, KR.Unit, 0.1, commPrice, 0.0 requiredQty, KR.totalEntitlement totQty, 'N' weighing, 0 FROM KeyRegister KR, Pos_ob PO WHERE rcId = '"+rcNumber+"' AND KR.commCode = PO.commCode GROUP BY KR.allocationType, KR.allotedMonth,KR.allotedYear,KR.AllotMonth, KR.AllotYear, KR.rcId, KR.commCode;";
            Cursor res = db.rawQuery( query,null,null);
            res.moveToFirst();
            System.out.println("query :: "+query+", COUNT :: " + res.getCount());
            while (!res.isAfterLast()) {

                commDetails commodity = new commDetails();
                commodity.setAllocationType(res.getString(0));
                commodity.setAllotedMonth(res.getString(1));
                commodity.setAllotedYear(res.getString(2));
                commodity.setAvailedQty(res.getString(3));
                commodity.setBalQty(res.getString(4));
                commodity.setClosingBal(res.getString(5));
                commodity.setCommName(res.getString(6));
                commodity.setCommNamell(res.getString(7));
                commodity.setCommcode(res.getString(8));
                commodity.setMeasureUnit(res.getString(9));
                commodity.setMinQty(res.getString(10));
                commodity.setPrice(res.getString(11));
                commodity.setRequiredQty(res.getString(12));
                commodity.setTotQty(res.getString(13));
                commodity.setWeighing(res.getString(14));
                commodity.setTotalPrice(res.getString(15));
                commodityDetails.add(commodity);
                res.moveToNext();
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            return commodityDetails;
        }
    }

    public ArrayList<com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetURLDetails.memberdetails> getMemberDetails(String rcNumber){
        ArrayList<memberdetails> memberDetails= new ArrayList<>();
        db = this.getReadableDatabase();
        try{
            String query = "select memberNameLl,memberNameEn,memberId FROM memDetails WHERE rcId = '"+rcNumber+"' GROUP BY memberId";
            //String query = "select memberNameLl, memberNameEn FROM KeyRegister WHERE rcId = '"+rcNumber+"'GROUP BY memberNameLl";
            Cursor res = db.rawQuery( query,null,null);
            res.moveToFirst();
            System.out.println("query :: "+query+", COUNT :: " + res.getCount());
            while (!res.isAfterLast()) {
                memberdetails memberListModel_ = new memberdetails();
                memberListModel_.memberNamell = res.getString(0);
                memberListModel_.memberName = res.getString(1);
                memberListModel_.zmemberId = res.getString(2);
                memberListModel_.uid = "";
                memberDetails.add(memberListModel_);
                res.moveToNext();
            }
            res.close();
        }catch(Exception e){

            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
           return memberDetails;
        }
    }


    public ExcessData getExcessData(String rcNumber)
    {
        ExcessData excessData = new ExcessData();
        excessData.setCount(0);
        db = this.getReadableDatabase();
        try
        {
            Cursor res = db.rawQuery("SELECT schemeId, schemeName, memberNameLl, memberNameEn FROM KeyRegister WHERE rcId = '"+rcNumber+"' LIMIT 1; ",null,null);
            res.moveToFirst();
            System.out.println("COUNT#####" + res.getCount());
            excessData.setCount(res.getCount());
            while (!res.isAfterLast()) {
                excessData.setSchemeId(res.getString(0));
                excessData.setSchemeName(res.getString(1));
                excessData.setSchemeNameLocal(res.getString(1));
                excessData.setMemberName(res.getString(3));
                excessData.setMemberNameLocal(res.getString(2));
                res.moveToNext();
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            return excessData;
        }
    }

    public int getExcessPortableData(String rcNumber)
    {
        int result=0;
        SQLiteDatabase db = this.getReadableDatabase();
        try
        {
            Cursor res = db.rawQuery("SELECT schemeId, schemeName, memberNameLl, memberNameEn FROM KeyRegister WHERE rcId = '"+rcNumber+"' LIMIT 1; ",null,null);
            result=res.getCount();
            System.out.println("COUNT#####" + res.getCount());
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
        }
        return result;
    }



    public Print getPrintDataFromLocal(String rationCardNumber)
    {
        Print print = new Print();
        db = this.getReadableDatabase();
        try
        {
            Cursor res = db.rawQuery("SELECT bal_qty, carry_over, commIndividualAmount, comm_name, comm_name_ll," +
                    " member_name, member_name_ll, reciept_id, retail_price, scheme_desc_en, scheme_desc_ll," +
                    " tot_amount, total_quantity, transaction_time, uid_refer_no,allocationType ,allotedMonth ,allotedYear ,commCode ,closingBalance  FROM Print_Table;",null,null);
            res.moveToFirst();
            System.out.println("COUNT" + res.getCount());
            while (!res.isAfterLast()) {
                printBeans printData = new printBeans();
                printData.setBal_qty(res.getString(0));
                printData.setCarry_over(res.getString(1));
                printData.setCommIndividualAmount(res.getString(2));
                printData.setComm_name(res.getString(3));
                printData.setComm_name_ll(res.getString(4));
                printData.setMember_name(res.getString(5));
                printData.setMember_name_ll(res.getString(6));
                printData.setReciept_id(res.getString(7));
                printData.setRetail_price(res.getString(8));
                printData.setScheme_desc_en(res.getString(9));
                printData.setScheme_desc_ll(res.getString(10));
                printData.setTot_amount(res.getString(11));
                printData.setTotal_quantity(res.getString(12));
                printData.setTransaction_time(res.getString(13));
                printData.setUid_refer_no(res.getString(14));
                printData.setAllocationType(res.getString(15));
                printData.setAllotedMonth(res.getString(16));
                printData.setAllotedYear(res.getString(17));
                printData.setCommCode(res.getString(18));
                printData.setClosingBal(res.getString(19));
                print.printBeans.add(printData);
                res.moveToNext();
            }
            res.close();
            print.rcId = rationCardNumber;
            print.receiptId = print.printBeans.get(0).getReciept_id();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            return print;
        }
    }

    public void updatePosOB(String comNames,String qts) {

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "update Pos_Ob set closingBalance = closingBalance + "+qts +" where commNameEn='" +comNames +"'";
        System.out.println("@@Query: " +query);
        sqLiteDatabase.execSQL(query);

    }

    public int updatePosOB(Context context,String recQty,String commCode)
    {
        int result = 0;
        db = this.getWritableDatabase();
        System.out.println("TEJJ RECEIVE GOODS recQty :: "+recQty+", commCode :: "+commCode);

        try {
            db.execSQL("update POS_OB set closingBalance = closingBalance+? where commCode = ?",new String[]{recQty,commCode});
            result = 1;

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            List<StockData> stock = getPendingStock();
            Log.e("Hello","                    ");
            return result;
        }
    }


    public long insertOfflineStockReceive(Context context,ContentValues contentValues)
    {
        long result = 0L;
        db = this.getWritableDatabase();

        try {
            result = db.insert("OfflineStockReceive",null,contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            return result;
        }
    }

    public class ExcessData
    {
        private String schemeId;
        private String schemeName;
        private String schemeNameLocal;
        private String memberName;
        private String memberNameLocal;
        private int count;

        public String getSchemeId() {
            return schemeId;
        }

        public void setSchemeId(String schemeId) {
            this.schemeId = schemeId;
        }

        public String getSchemeName() {
            return schemeName;
        }

        public void setSchemeName(String schemeName) {
            this.schemeName = schemeName;
        }

        public String getSchemeNameLocal() {
            return schemeNameLocal;
        }

        public void setSchemeNameLocal(String schemeNameLocal) {
            this.schemeNameLocal = schemeNameLocal;
        }

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }

        public String getMemberNameLocal() {
            return memberNameLocal;
        }

        public void setMemberNameLocal(String memberNameLocal) {
            this.memberNameLocal = memberNameLocal;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public List<drBean> getofflineSaleRecords(String date)
    {
        System.out.println("@@In getofflineSaleRecords...");
        List<drBean> drBeanList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String query = "SELECT commNameLl, commNameEn, SM.schemeName, sum(IssuedQty) sale, count( * ) total_cards FROM BenfiaryTxn BFT, commodityMaster CM, schemeMaster SM WHERE Date(DateTime) = Date('"+date+"') AND CM.commCode = BFT.commCode AND BFT.SchemeId = SM.SchemeId GROUP BY BFT.SchemeId, BFT.commCode;";
            Cursor res = db.rawQuery(query,null);
            System.out.println("RESULT>>>>>>>>"+res);
            res.moveToFirst();
            while(!res.isAfterLast())
            {
                drBean drBeanItem = new drBean();
                System.out.println("+++++++++++Data available in partialOnlineData");
                drBeanItem.setCommNamell(res.getString(0));
                drBeanItem.setComm_name(res.getString(1));
                drBeanItem.setSchemeName(res.getString(2));
                drBeanItem.setSale(res.getString(3));
                drBeanItem.setTotal_cards(res.getString(4));
                drBeanList.add(drBeanItem);
                res.moveToNext();
            }
            res.close();
        }
        catch (Exception e)
        {
            System.out.println("@@Exception(DatabaseHlper) in getofflineSaleRecords: "+e.toString());
            e.printStackTrace();
        }
        finally {
            if (db.isOpen())
                db.close();
            return drBeanList;
        }
    }

    public String getOfflineDealerName()
    {
        String dealerName = "";
        SQLiteDatabase db = this.getReadableDatabase();
        try
        {
            String query = "SELECT Dealers_Names FROM dealers_table;";
            Cursor res = db.rawQuery(query,null);
            System.out.println("RESULT>>>>>>>>"+res);
            res.moveToFirst();
            if(!res.isAfterLast())
            {
                dealerName = res.getString(0);
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            return dealerName;
        }
    }

    public  ArrayList<astockBean> getOfflineCurrentStock()
    {
        ArrayList<astockBean> astockBeans = new ArrayList<>();
        String query = "SELECT A.commNameEn, B.SchemeId, A.closingBalance + ifnull(B.issueQty,0), ifnull(B.issueQty,0), A.closingBalance FROM ( SELECT commCode, commNameEn, closingBalance FROM Pos_Ob ) A LEFT JOIN ( SELECT CommCode, SchemeId, sum(IssuedQty) issueQty FROM BenfiaryTxn GROUP BY CommCode ) B ON A.commCode = B.CommCode;";
        SQLiteDatabase db = this.getReadableDatabase();
        try
        {
            Cursor res = db.rawQuery(query,null);
            System.out.println("RESULT>>>>>>>>"+res);
            res.moveToFirst();
            while (!res.isAfterLast())
            {
                astockBean astockBeanItem = new astockBean();
                astockBeanItem.setComm_name(res.getString(0));
                astockBeanItem.setScheme_desc_en("-");
                astockBeanItem.setTotal_quantity(res.getString(2));
                astockBeanItem.setIssued_qty(res.getString(3));
                astockBeanItem.setClosing_balance(res.getString(4));
                astockBeans.add(astockBeanItem);
                res.moveToNext();
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            return astockBeans;
        }
    }

    public int clearPrintData(Context context)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
        int numOfRowsDeleted = 0;
        try
        {
            numOfRowsDeleted = sqLiteDatabase.delete("Print_Table",null,null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen())
                sqLiteDatabase.close();
            helper.close();
            Log.d("[clearPrintData]","Number Of Rows Deleted :: "+numOfRowsDeleted);
            return  numOfRowsDeleted;
        }
    }

    public long insertPrintItem(Context context,ContentValues contentValues)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
        long numOfRowsEffected = 0;
        try
        {
            numOfRowsEffected = sqLiteDatabase.insert("Print_Table",null,contentValues);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(sqLiteDatabase != null && sqLiteDatabase.isOpen())
                sqLiteDatabase.close();
            helper.close();
            Log.d("[insertPrintItem]","Number Of Rows effected :: "+numOfRowsEffected);
            return  numOfRowsEffected;
        }
    }

    public int checkBalanceEntitlement(Context context,String rcNumber)
    {

        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        int count = 0;
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();

        try
        {
            String query = "SELECT A.carry_over, A.allocationType, A.allotedMonth, A.allotedYear, A.commCode, ifnull(B.balanceEntitlement,0.0) balanceQty, A.carry_over issueQty FROM Print_Table A LEFT JOIN ( SELECT commCode, balanceEntitlement, allocationType, allotedMonth, allotedYear FROM KeyRegister" +
                    " WHERE rcId = '"+rcNumber+"' ) B ON A.commCode = B.commCode AND A.allocationType = B.allocationType AND A.allotedMonth = B.allotedMonth AND A.allotedYear = B.allotedYear WHERE balanceQty + 0 < issueQty + 0; ";
            Log.e("checkBalanceEntitlement","query :: "+query);

            Cursor res = sqLiteDatabase.rawQuery(query,null);
            System.out.println("RESULT>>>>>>>>"+res);
            res.moveToFirst();
            while (!res.isAfterLast())
            {
                count++;
                res.moveToNext();
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            count = -1;
        }

        finally {
            if(sqLiteDatabase != null && sqLiteDatabase.isOpen())
                sqLiteDatabase.close();
            databaseHelper.close();
            return count;
        }
    }

    public  List<CommWiseData> getPendingOfflineData(String rcNumber)
    {
        List<CommWiseData> fpsOfflineTransResponses = new ArrayList<>();
        String query = "select RcId,CommCode,TotQty,BalQty,SchemeId,IssuedQty,RecptId,commAmount,TotAmt,Rate,MemberName,DateTime,TxnType,AllotMonth,AllotYear,allocationType,memberId from BenfiaryTxn where TxnUploadSts = 'N' and RcId = '"+rcNumber+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        try
        {
            Cursor res = db.rawQuery(query,null);
            System.out.println("RESULT>>>>>>>>"+res);
            res.moveToFirst();
            while (!res.isAfterLast())
            {
                String txnTime = res.getString(11);

                DateFormat fromDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                DateFormat toDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date orderDate = fromDateFormat.parse(txnTime);
                txnTime = toDateFormat.format(orderDate);
                CommWiseData commWiseData = new CommWiseData();
                commWiseData.setRcId(res.getString(0));
                commWiseData.setCommCode(res.getString(1));

                /*String temp = res.getString(2);
                int x = Integer.parseInt(temp)-1;
                commWiseData.setTotalEntitlement(String.valueOf(x));

                temp = res.getString(3);
                x = Integer.parseInt(temp)-1;
                commWiseData.setBalanceEntitlement(String.valueOf(x));*/

                commWiseData.setTotalEntitlement(res.getString(2));
                commWiseData.setBalanceEntitlement(res.getString(3));

                commWiseData.setSchemeId(res.getString(4));
                commWiseData.setIssueQty(res.getString(5));
                commWiseData.setReceiptId(res.getString(6));
                commWiseData.setCommAmount(res.getString(7));
                commWiseData.setTotalAmount(res.getString(8));
                commWiseData.setCommPrice(res.getString(9));
                commWiseData.setHeadOfTheFamily(res.getString(10));
                commWiseData.setTransactionTime(txnTime);
                commWiseData.setTransMode(res.getString(12));
                commWiseData.setAllotedMonth(res.getString(13));
                commWiseData.setAllotedYear(res.getString(14));
                commWiseData.setAllocationType(res.getString(15));
                commWiseData.setMemberId(res.getString(16));
//commWiseData.set(res.getString(4));
                fpsOfflineTransResponses.add(commWiseData);
                res.moveToNext();
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            return fpsOfflineTransResponses;
        }
    }

    public  List<CommWiseData> getPendingOfflineData(int limit)
    {
        System.out.println("@@In getPendingOfflineData...");
        List<CommWiseData> fpsOfflineTransResponses = new ArrayList<>();
        String limitClause;
        if(limit == 20)
        {
            limitClause = " LIMIT 20";
        }
        else
            limitClause = "";

        String query = "select RcId,CommCode,TotQty,BalQty,SchemeId,IssuedQty,RecptId,commAmount,TotAmt,Rate,MemberName,DateTime,TxnType,AllotMonth,AllotYear,allocationType,memberId from BenfiaryTxn where TxnUploadSts = 'N' "+limitClause;
        SQLiteDatabase db = this.getReadableDatabase();
        try
        {
            Cursor res = db.rawQuery(query,null);
            System.out.println("RESULT>>>>>>>>"+res);
            res.moveToFirst();
            while (!res.isAfterLast())
            {
                System.out.println("@@Data found");
                String txnTime = res.getString(11);

                DateFormat fromDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                DateFormat toDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date orderDate = fromDateFormat.parse(txnTime);
                txnTime = toDateFormat.format(orderDate);
                CommWiseData commWiseData = new CommWiseData();
                commWiseData.setRcId(res.getString(0));
                commWiseData.setCommCode(res.getString(1));
                commWiseData.setTotalEntitlement(res.getString(2));
                commWiseData.setBalanceEntitlement(res.getString(3));
                commWiseData.setSchemeId(res.getString(4));
                commWiseData.setIssueQty(res.getString(5));
                commWiseData.setReceiptId(res.getString(6));
                commWiseData.setCommAmount(res.getString(7));
                commWiseData.setTotalAmount(res.getString(8));
                commWiseData.setCommPrice(res.getString(9));
                commWiseData.setHeadOfTheFamily(res.getString(10));
                commWiseData.setTransactionTime(txnTime);
                commWiseData.setTransMode(res.getString(12));
                commWiseData.setAllotedMonth(res.getString(13));
                commWiseData.setAllotedYear(res.getString(14));
                commWiseData.setAllocationType(res.getString(15));
                commWiseData.setMemberId(res.getString(16));
                //commWiseData.set(res.getString(4));
                fpsOfflineTransResponses.add(commWiseData);
                res.moveToNext();
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            return fpsOfflineTransResponses;
        }
    }

    public List<StockData> getPendingStock()
    {
        List<StockData> penStockData = new ArrayList<>();
        String query = "select commCode,closingBalance from Pos_Ob";
        SQLiteDatabase db = this.getReadableDatabase();
        try
        {
            Cursor res = db.rawQuery(query,null);
            System.out.println("@@RESULT in getPendingStock>>>>>>>>"+res);
            res.moveToFirst();
            while (!res.isAfterLast())
            {
                System.out.println("@@In while... cOMMCODE: " +res.getString(0));
                StockData stockData = new StockData();
                stockData.setCommCode(res.getString(0));
                stockData.setClosingBalance(res.getString(1));
                penStockData.add(stockData);
                res.moveToNext();
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            return penStockData;
        }
    }

    public int getTotCommodityTxns()
    {
        int count = 0;
        String query = "select count(*) from BenfiaryTxn";// where TxnType IN('Y','N')";
        SQLiteDatabase db = this.getReadableDatabase();
        try
        {
            Cursor res = db.rawQuery(query,null);
            System.out.println("RESULT>>>>>>>>"+res);
            res.moveToFirst();
            if (!res.isAfterLast())
            {
                count = res.getInt(0);
                System.out.println("@@Data in count: " +count);
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            System.out.println("@@Returning count: " +count);
            return count;
        }
    }

    public int getPendingTxnCount()
    {
        int count = 0;
        String query = "select count(*) from BenfiaryTxn where TxnUploadSts ='N'";
        SQLiteDatabase db = this.getReadableDatabase();
        try
        {
            Cursor res = db.rawQuery(query,null);
            System.out.println("RESULT>>>>>>>>"+res);
            res.moveToFirst();
            if (!res.isAfterLast())
            {
                count = res.getInt(0);
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            return count;
        }
    }

    public int[] getSaleRecordAgrregateCounts()
    {
        int offlineCount = 0,onlineCount = 0,uploadedCount = 0,pendingCount = 0;
        String query = "SELECT count( * ), TxnType,TxnUploadSts FROM BenfiaryTxn GROUP BY TxnType,TxnUploadSts;";
        SQLiteDatabase db = this.getReadableDatabase();
        try
        {
            Cursor res = db.rawQuery(query,null);
            System.out.println("RESULT>>>>>>>>"+res);
            res.moveToFirst();
            while (!res.isAfterLast())
            {
                String txnType = res.getString(1);
                int count = res.getInt(0);
                if(txnType.equals("O"))
                    onlineCount += count;
                else
                {
                    String txnUploadSts = res.getString(2);
                    if(txnUploadSts.equals("Y"))
                        uploadedCount += count;
                    else
                        pendingCount += count;
                    offlineCount += count;
                }
                res.moveToNext();
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            return new int[]{onlineCount,offlineCount,uploadedCount,pendingCount};
        }
    }

    public List<String> getRcNumber()
    {

        List<String> rcnumbersdata = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try
        {
            String query = "select distinct rcId from KeyRegister";
            Cursor res = db.rawQuery(query,null);
            //System.out.println();
            res.moveToFirst();
            while (!res.isAfterLast())
            {
                System.out.println("@@In while... RCNUMBERS: " +res.getString(0));
                rcnumbersdata.add(res.getString(0));
                res.moveToNext();
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            return rcnumbersdata;
        }
    }

    public void forceDelteKeyRegNPosOb() {
        System.out.println("<<<<<FORCE DELETETING>>>>");
        int pendingTxns = this.getPendingTxnCount();
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        if (pendingTxns == 0) {
            sqLiteDatabase.execSQL("DELETE FROM KeyRegister");
            sqLiteDatabase.execSQL("DELETE FROM Pos_Ob");
        }
    }

    public int getTotalRationCardCount()
    {
        int count = 0;
        String query = "select count (distinct rcId) from KeyRegister";
        SQLiteDatabase db = this.getReadableDatabase();
        try
        {
            Cursor res = db.rawQuery(query,null);
            System.out.println("RESULT>>>>>>>>"+res);
            res.moveToFirst();
            if (!res.isAfterLast())
            {
                count = res.getInt(0);
                System.out.println("@@Data in count: " +count);
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            System.out.println("@@Returning count: " +count);
            return count;
        }
    }


    public int getOfflineTxnPendingRationCard()
    {
        int count = 0;
        //String query = "select count (distinct rcId) from KeyRegister";
        String query = "select count(distinct RcId)from BenfiaryTxn where TxnType IN ('P','Q')";
        SQLiteDatabase db = this.getReadableDatabase();
        try
        {
            Cursor res = db.rawQuery(query,null);
            System.out.println("RESULT>>>>>>>>"+res);
            res.moveToFirst();
            if (!res.isAfterLast())
            {
                count = res.getInt(0);
                System.out.println("@@Data in count: " +count);
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            System.out.println("@@Returning count: " +count);
            return count;
        }
    }


    public double getEntitlementAlreadyTakenRc(String rcNumber){

        double count = 0.0;
        String query = "select sum(balanceEntitlement) from KeyRegister where rcId = '"+rcNumber+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        try
        {
            Cursor res = db.rawQuery(query,null);
            System.out.println("RESULT>>>>>>>>"+res);
            res.moveToFirst();
            if (!res.isAfterLast())
            {
                count = res.getDouble(0);
                System.out.println("@@Data in count: " +count);
            }
            res.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
            System.out.println("@@Returning count: " +count);
            return count;
        }

    }




}