package com.visiontek.Mantra.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;


import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.ReceiveGoodsDetails;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.infoTCDetails;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.tcCommDetails;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;


import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;

public class Json_Parsing {
    DatabaseHelper databaseHelper;
    String myResponse;
    String msg, code;
    String otptxnId,transactionFlow,authTransactionId;
    Object object;

    private OnResultListener onResultListener;
    Context context;
    public Json_Parsing(Context context, String frame, int type)
    {
        databaseHelper = new DatabaseHelper(context);
        Parsing(frame, type);
    }

    public void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    public void Parsing(String frame, final int type)  {
        try {
            System.out.println("@@ Parsing Data..." + frame);
            OkHttpClient okHttpClient;
            okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            RequestBody body = null;
            body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), frame);
            System.out.println("BOdy.." + body);
            String url = null;
            if (!dealerConstants.fpsURLInfo.wsdlOffline.isEmpty() && !dealerConstants.fpsURLInfo.wsdlOffline.equals(null)
                    && !dealerConstants.fpsURLInfo.wsdlOffline.equals("NA")) {
                if (type == 2 || type == 3) {
                    url = dealerConstants.fpsURLInfo.wsdlOffline + "reasonConsent";
                    System.out.println("REASONConsent ======"+url);
                } else if (type==5){
                    url=dealerConstants.fpsURLInfo.wsdlOffline+"getFpsOfflineData";
                    //url="http://epos.nic.in/ePosCommonServiceCTG/eposCommon/getFpsOfflineData";
                }else if (type==6){
                    url=dealerConstants.fpsURLInfo.wsdlOffline+"getCbUpdate";
                    //url="http://epos.nic.in/ePosCommonServiceCTG/eposCommon/getCbUpdate";
                }else if(type == 7){
                    url=dealerConstants.fpsURLInfo.wsdlOffline+"getFpsStockDetails";
                    //url="http://epos.nic.in/ePosCommonServiceCTG/eposCommon/getFpsStockDetails";
                } else if(type == 10)
                {

                    url=dealerConstants.fpsURLInfo.aadhaarOTPURL+"uidOTPRequest";
                }
                else if(type == 11){

                    url=dealerConstants.fpsURLInfo.aadhaarOTPURL+"uidOTPAuthRequest";

                }
                else {
                    url=dealerConstants.fpsURLInfo.wsdlOffline+"getFpsStockDetails";
                    //url = "http://epos.nic.in/ePosCommonServiceCTG/eposCommon/getFpsStockDetails";
                }
                final Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        final String myResponse = e.toString();
                        System.out.println("@@ Failure Response...." + myResponse);
                        JSONObject reader;
                        try {
                            reader = new JSONObject(myResponse);
                            msg = reader.getString("respMessage");
                            code = reader.getString("respCode");
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    callback();
                                } catch (SQLException throwables) {
                                    throwables.printStackTrace();
                                }
                            }
                        });

                        System.out.println("error " + e);
                    }

                    @Override
                    public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response) throws IOException {

                        myResponse = response.body().string();

                        System.out.println("OUTPUT of onResponse"+myResponse);
                        if (type == 1) {
                            Util.generateNoteOnSD(context, "RecivegoodsRes.txt", myResponse);
                            System.out.println("RECEIVE GOODS RESPONSE >>>>>>>>>>"+myResponse);
                            Timber.d("JsonParsing-RecivegoodsRes "+myResponse);
                            object = parse_recivegoods(myResponse);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        callback();
                                    } catch (SQLException throwables) {
                                        throwables.printStackTrace();
                                    }
                                }
                            });
                        } else if (type == 2) {
                            Util.generateNoteOnSD(context, "CancelRequestRes.txt", myResponse);
                            parse_cancelReq(myResponse);
                            Timber.d("JsonParsing-CancelRequestRes :"+myResponse);
                        } else if (type == 3) {
                            Util.generateNoteOnSD(context, "ConsentFormRes.txt", myResponse);
                            parse_consentform(myResponse);
                            System.out.println("JsonParsing-ConsentFormRes===="+myResponse);
                            Timber.d("JsonParsing-ConsentFormRes :"+myResponse);
                        } else if (type == 4) {
                            Util.generateNoteOnSD(context, "LogoutRes.txt", myResponse);
                            parse_logout(myResponse);
                            Timber.d("JsonParsing-LogoutRes :"+myResponse);
                        }else if (type==5){
                            System.out.println("@@ generateNoteOnSD");
                            Util.generateNoteOnSD(context, "KeyRegestration.txt", myResponse);
                            try {
                                parsegetFpsOfflineData(myResponse,context);
                                System.out.println("KeyResponse ========="+myResponse);
                                Timber.d("JsonParsing-KeyRegestrationRes :"+myResponse);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else if (type==6){
                            Util.generateNoteOnSD(context, "CBDownload.txt", myResponse);
                            try {
                                parsegetCbUpdate(myResponse,context);
                                Timber.d("JsonParsing-CBDownloadRes :"+myResponse);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else if (type==7){
                            Util.generateNoteOnSD(context, "PartialOnlineData.txt", myResponse);
                            try {
                                parsePartialOnlineData(myResponse,context);
                                Timber.d("JsonParsing-PartialOnlineDataRes :"+myResponse);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else if(type == 8)
                        {
                            parseOfflineUploadResult(myResponse,context);

                            Timber.d("JsonParsing-parseOfflineUploadResultRe :"+myResponse);

                        }
                        else if(type == 9)
                        {
                            parseDataDownloadAckResult(myResponse,context);
                            Timber.d("JsonParsing-parseDataDownloadAckResultRes:"+myResponse);


                        }else if(type == 10){
                            parse_AadhaarRequest(myResponse,context);
                            System.out.println("AadhaarREQUEST============"+myResponse);
                            Timber.d("JsonParsing-AadharOTPRequestRes :"+myResponse);


                        }else if(type ==11){

                            parse_AadhaarOtpVerify(myResponse,context);
                            System.out.println("AadhaarOTPVerify============"+myResponse);
                            Timber.d("JsonParsing-AadharOTPVerifyRes :"+myResponse);

                        }

                    }

                });
            } else {
                code = "1";
                msg = "Requesting on Empty or Invalid URL Tag";
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callback();
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                });
            }

       /* if (type==17 || type==18){

            System.out.println(url);
        }else if (type==3){
            url="http://epos.nic.in/ePosCommonServiceCTG/eposCommon/getFpsOfflineData";
        }else if (type==4){
            url="http://epos.nic.in/ePosCommonServiceCTG/eposCommon/getCbUpdate";
        }else  {
            url="http://epos.nic.in/ePosCommonServiceCTG/eposCommon/getFpsStockDetails";
        }*/


        }catch (Exception e) {
            e.printStackTrace();
            code = "1";
            msg = String.valueOf(e);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        callback();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            });

        }

    }

    private void parse_consentform(String myResponse) {

        try {
            JSONObject reader;
            reader = new JSONObject(myResponse);

            msg = reader.getString("respMessage");
            code = reader.getString("respCode");
            System.out.println(code);

        } catch (JSONException e) {
            code="1";
            msg="Parsing Error";
            e.printStackTrace();
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    callback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }

    private void parse_logout(String myResponse) {
        try {
            JSONObject reader;
            reader = new JSONObject(myResponse);

            msg = reader.getString("respMessage");
            code = reader.getString("respCode");

        } catch (JSONException e) {
            code="1";
            msg="Parsing Error";
            e.printStackTrace();
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    callback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });

    }

    private void parse_cancelReq(String myResponse) {
        try {
            JSONObject reader= new JSONObject(myResponse);
            msg = reader.getString("respMessage");
            code = reader.getString("respCode");
        } catch (JSONException e) {
            code="1";
            msg="Parsing Error";
            System.out.println("----------------------");
            e.printStackTrace();
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    callback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }
    private void parse_AadhaarRequest(String myResponse,Context context){
        try {
            JSONObject reader= new JSONObject(myResponse);
            System.out.println("========AadhaarRequest===========");
            msg = reader.getString("respMessage");
            code = reader.getString("respCode");
            otptxnId= reader.getString("otpTxnId");
            object = otptxnId;
        } catch (JSONException e) {
            System.out.println("========AadhaarRequest Exception===========");
            code="1";
            msg="Parsing Error";
            System.out.println("----------------------");
            e.printStackTrace();
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    callback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }

    private void parse_AadhaarOtpVerify(String myResponse,Context context){

        try{
            JSONObject reader= new JSONObject(myResponse);
            msg = reader.getString("respMessage");
            code = reader.getString("respCode");
            transactionFlow= reader.getString("transactionFlow");
            //authTransactionId =reader.getString("authTransactionId");
             object = transactionFlow;
            //object =authTransactionId;

        } catch (JSONException e) {
            code="1";
            msg="Parsing Error";
            System.out.println("----------------------");
            e.printStackTrace();
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    callback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }

    private ReceiveGoodsDetails parse_recivegoods(String myResponse) {
        ReceiveGoodsDetails receiveGoodsDetails=new ReceiveGoodsDetails();
        infoTCDetails infoTCDetails=null;
        tcCommDetails tcCommDetails=null;
        try {
            JSONObject reader= new JSONObject(myResponse);
            System.out.println(myResponse);
            msg = reader.getString("respMessage");
            code = reader.getString("respCode");
            if (code.equals("00")) {
                JSONArray jsonArray = reader.getJSONArray("infoTCDetails");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jb = jsonArray.getJSONObject(i);
                    infoTCDetails =new infoTCDetails();
                    infoTCDetails.fpsId=jb.getString("fpsId");
                    infoTCDetails.allotedMonth=(jb.getString("allotedMonth"));
                    infoTCDetails.allotedYear=(jb.getString("allotedYear"));
                    infoTCDetails.truckChitNo=(jb.getString("truckChitNo"));
                    infoTCDetails.challanId=(jb.getString("challanId"));
                    infoTCDetails.allocationOrderNo=(jb.getString("allocationOrderNo"));
                    infoTCDetails.truckNo=(jb.getString("truckNo"));
                    JSONArray CommDetails = jb.getJSONArray("tcCommDetails");

                    infoTCDetails.CommLength=(String.valueOf(CommDetails.length()));
                    for (int j = 0; j < CommDetails.length(); j++) {
                        tcCommDetails=new tcCommDetails();
                        JSONObject commDetails = CommDetails.getJSONObject(j);
                        tcCommDetails.releasedQuantity=(commDetails.getString("releasedQuantity"));
                        tcCommDetails.allotment=(commDetails.getString("allotment"));
                        tcCommDetails.commCode=(commDetails.getString("commCode"));
                        tcCommDetails.commName=(commDetails.getString("commName"));
                        tcCommDetails.schemeId=(commDetails.getString("schemeId"));
                        tcCommDetails.schemeName=(commDetails.getString("schemeName"));
                        tcCommDetails.enteredvalue="0.0";
                        infoTCDetails.tcCommDetails.add(tcCommDetails);
                    }
                    receiveGoodsDetails.infoTCDetails.add(infoTCDetails);
                }
            }

        } catch (JSONException e) {
            code="1";
            msg="Parsing Error";
            e.printStackTrace();
        }

        return receiveGoodsDetails;
    }
    private void callback() throws SQLException {
        System.out.println("@@ In callback()");

        if (onResultListener != null) {
            onResultListener.onCompleted(code, msg,object);
        }
    }
    public interface OnResultListener {
        void onCompleted(String code, String msg, Object object) throws SQLException;
    }
    public interface OnResultListener1 {
        void onCompleted(String code, String msg,String ref,String flow, Object object) throws SQLException;
    }

    /***************** OFFLINE APIS************************/
    public void parsegetFpsOfflineData(String strJson, Context context) throws Exception
    {
        System.out.println("@@ parsegetFpsOfflineData");
        System.out.println("@@Data in strJson: " +strJson);
        JSONObject jsonRootObject = new JSONObject(strJson);
        JSONArray jsonArray = jsonRootObject.optJSONArray("keyRegister");

        msg = jsonRootObject.getString("respMessage");
        code = jsonRootObject.getString("respCode");

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try
        {
            db.delete("KeyRegister",null,null);
            db.delete("Pos_Ob",null,null);
            db.delete("schemeMaster",null,null);
            db.delete("commodityMaster",null,null);
            db.delete("memDetails",null,null);
            System.out.println(jsonArray.length());
            for(int i=0; i < jsonArray.length(); i++){
                System.out.println("----------"+i);
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String rcId = jsonObject.optString("rcId").toString();
                String commNameEn = jsonObject.optString("commNameEn").toString();
                String commNameLl = jsonObject.optString("commNameLl").toString();
                String commCode = jsonObject.optString("commCode").toString();
                String totalEntitlement = jsonObject.optString("totalEntitlement").toString();
                String balanceEntitlement = jsonObject.optString("balanceEntitlement").toString();
                String schemeId = jsonObject.optString("schemeId").toString();
                String schemeName = jsonObject.optString("schemeName").toString();
                String commPrice = jsonObject.optString("commPrice").toString();
                String measurmentUnit = jsonObject.optString("measurmentUnit").toString();
                String memberNameLl = jsonObject.optString("memberNameLl").toString();
                String memberNameEn = jsonObject.optString("memberNameEn").toString();
                String month = jsonObject.optString("month").toString();
                String year = jsonObject.optString("year").toString();
                String allocationType = jsonObject.optString("allocationType").toString();
                String allotedMonth = jsonObject.optString("allotedMonth").toString();
                String allotedYear = jsonObject.optString("allotedYear").toString();
                //String memberId = jsonObject.optString("memberId").toString();
                //System.out.println();

                System.out.println("@@ parsegetFpsOfflineDataQ");
                String query = "insert into KeyRegister values('"+rcId+"','"+commNameEn+"','"+commNameLl+"','"+commCode+"','"+totalEntitlement+"','"+balanceEntitlement+"','"+schemeId+"','"+schemeName+"','"+commPrice+"','"+measurmentUnit+"','"+memberNameLl+"','"+memberNameEn+"','"+month+"','"+year+"','"+allocationType+"','"+allotedMonth+"','"+allotedYear+"');";
                System.out.println("Query: " +query);
                //SQLiteDatabase db = databaseHelper.getWritableDatabase();
                db.execSQL(query);
                //db.close();
            }

            JSONArray jsonArray1 = jsonRootObject.optJSONArray("fpsCb");
            for(int i=0; i < jsonArray1.length(); i++) {
                JSONObject jsonObject = jsonArray1.getJSONObject(i);
                String commCode = jsonObject.optString("commCode").toString();
                String commNameEn = jsonObject.optString("commNameEn").toString();
                String commNameLl = jsonObject.optString("commNameLl").toString();
                String closingBalance = jsonObject.optString("closingBalance").toString();

                String query = "insert into Pos_Ob values('" +commCode +"','"+commNameEn +"','" +commNameLl +"','" +closingBalance +"');";
                System.out.println("Query: " +query);
                //SQLiteDatabase db = databaseHelper.getWritableDatabase();
                db.execSQL(query);
                //db.close();
            }

            JSONArray jsonArray2 = jsonRootObject.optJSONArray("schemeMaster");
            for(int i=0; i < jsonArray2.length(); i++) {
                JSONObject jsonObject = jsonArray2.getJSONObject(i);
                String schemeId = jsonObject.optString("schemeId").toString();
                String schemeName = jsonObject.optString("schemeName").toString();

                String query = "insert into schemeMaster values('" +schemeId +"','"+schemeName +"');";
                System.out.println("Query: " +query);
                //SQLiteDatabase db = databaseHelper.getWritableDatabase();
                db.execSQL(query);
                //db.close();
            }

            JSONArray jsonArray3 = jsonRootObject.optJSONArray("commodityMaster");
            for(int i=0; i < jsonArray3.length(); i++) {
                JSONObject jsonObject = jsonArray3.getJSONObject(i);
                String commCode = jsonObject.optString("commCode").toString();
                String commNameLl = jsonObject.optString("commNameLl").toString();
                String commNameEn = jsonObject.optString("commNameEn").toString();
                String measurmentUnit = jsonObject.optString("measurmentUnit").toString();
                String commonCommCode = jsonObject.optString("commonCommCode").toString();
                String query = "insert into commodityMaster values('" +commCode +"','"+commNameLl +"','" +commNameEn +"','" +measurmentUnit +"','" +commonCommCode +"');";
                System.out.println("Query: " +query);
                //SQLiteDatabase db = databaseHelper.getWritableDatabase();
                db.execSQL(query);
                //db.close();
            }
            JSONArray jsonArray4 = jsonRootObject.optJSONArray("memDetails");
            for(int i=0;i<jsonArray4.length();i++){

                JSONObject jsonObject = jsonArray4.getJSONObject(i);
                String memberId = jsonObject.optString("memberId").toString();
                String memberNameEn = jsonObject.optString("memberNameEn").toString();
                String memberNameLl = jsonObject.optString("memberNameLl").toString();
                String rcId = jsonObject.optString("rcId").toString();
                String query = "insert into memDetails values('" +memberId +"','"+memberNameEn +"','" +memberNameLl +"','" +rcId +"');";
                System.out.println("Query_MemberDetails : "+query);
                db.execSQL(query);
            }
        }
        catch (Exception e)
        {
            System.out.println("@@Exception: "+e.toString());
            e.printStackTrace();
        }
        finally {
            if(db.isOpen())
                db.close();
        }
        System.out.println("@@Done!!!! fps download");

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    callback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }

    public void parsegetCbUpdate(String strJson, Context context) throws Exception {

        JSONObject jsonRootObject = new JSONObject(strJson);

        msg = jsonRootObject.getString("respMessage");
        code = jsonRootObject.getString("respCode");

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        db.delete("Pos_Ob",null,null);

        JSONArray jsonArray1 = jsonRootObject.optJSONArray("fpsCbs");
        for(int i=0; i < jsonArray1.length(); i++) {
            JSONObject jsonObject = jsonArray1.getJSONObject(i);
            String commCode = jsonObject.optString("commCode").toString();
            String commNameEn = jsonObject.optString("commNameEn").toString();
            String commNameLl = jsonObject.optString("commNameLl").toString();
            String closingBalance = jsonObject.optString("closingBalance").toString();

            String query = "insert into Pos_Ob values('" +commCode +"','"+commNameEn +"','" +commNameLl +"','" +closingBalance +"');";
            System.out.println("Query: " +query);
            db.execSQL(query);
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    callback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }
    public void parsePartialOnlineData(String response,Context context)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        String OffPassword = "";
        String OfflineLogin = "";
        String OfflineTxnTime = "";
        String Duration = "";
        String leftOfflineTime = "";
        String lastlogindate = "";
        String lastlogintime = "";
        String lastlogoutdate = "";
        String lastlogouttime = "";
        String AllotMonth = "";
        String AllotYear = "";
        String pOfflineStoppedDate = "";

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(response));
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("fpsToken")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {

                            OffPassword = (xpp.getText());
                            System.out.println("dealer_password 1 =================" + xpp.getText());
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("OfflineLogin")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {

                                OfflineLogin = (xpp.getText());
                                System.out.println("OfflineLogin 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("OfflineTxnTime")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {

                                OfflineTxnTime = (xpp.getText());
                                System.out.println("OfflineTxnTime 3 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("pOfflineDurationTimeInaDay")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {

                                Duration = (xpp.getText());
                                System.out.println("Duration 4 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("leftOfflineTime")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {

                                leftOfflineTime = (xpp.getText());
                                System.out.println("leftOfflineTime 5 =================" + xpp.getText());
                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("allocationMonth")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {

                                AllotMonth = (xpp.getText());
                                System.out.println("AllotMonth 8 =================" + xpp.getText());
                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("allocationYear")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {

                                AllotYear = (xpp.getText());
                                System.out.println("AllotYear 8 =================" + xpp.getText());
                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("pOfflineStoppedDate")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {

                                pOfflineStoppedDate = (xpp.getText());
                                System.out.println("pOfflineStoppedDate 8 =================" + xpp.getText());
                            }
                        }
                    }


                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Date currentTime = (Date) Calendar.getInstance().getTime();
        lastlogindate = currentTime.toString();
        lastlogintime = currentTime.toString();
        lastlogoutdate = currentTime.toString();
        lastlogouttime = currentTime.toString();
        leftOfflineTime = "NODATAFOUND";
        String query = "insert into PartialOnlineData values ('" +OffPassword +"','" +OfflineLogin +"','" +OfflineTxnTime +"','" +Duration +"','" +leftOfflineTime +"','" +lastlogindate +"'','" +lastlogintime +"','" +lastlogoutdate +"','" +lastlogouttime +"','" +AllotMonth +"','" +AllotYear +"','" +pOfflineStoppedDate +"');";
        query = "INSERT INTO PartialOnlineData (\n" +
                "                                  OffPassword,\n" +
                "                                  OfflineLogin,\n" +
                "                                  OfflineTxnTime,\n" +
                "                                  Duration,\n" +
                "                                  leftOfflineTime,\n" +
                "                                  lastlogindate,\n" +
                "                                  lastlogintime,\n" +
                "                                  lastlogoutdate,\n" +
                "                                  lastlogouttime,\n" +
                "                                  AllotMonth,\n" +
                "                                  AllotYear,\n" +
                "                                  pOfflineStoppedDate\n" +
                "                              )\n" +
                "                              VALUES (\n" +
                "                                  '"+OffPassword+"',\n" +
                "                                  '"+OfflineLogin+"',\n" +
                "                                  '"+OfflineTxnTime+"',\n" +
                "                                  '"+Duration+"',\n" +
                "                                  '"+leftOfflineTime+"',\n" +
                "                                  '"+lastlogindate+"',\n" +
                "                                  '"+lastlogintime+"',\n" +
                "                                  '"+lastlogoutdate+"',\n" +
                "                                  '"+lastlogouttime+"',\n" +
                "                                  '"+AllotMonth+"',\n" +
                "                                  '"+AllotYear+"',\n" +
                "                                  '"+pOfflineStoppedDate+"'\n" +
                "                              );";
        System.out.println("Query: " +query);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(query);
    }

    public void parseOfflineUploadResult(String strJson, Context context)
    {
        SQLiteDatabase db = null;
        try {
            JSONObject jsonRootObject = new JSONObject(strJson);
            msg = jsonRootObject.getString("respMessage");
            code = jsonRootObject.getString("respCode");

            if(code.equals("00"))
            {
                db = databaseHelper.getWritableDatabase();

                JSONArray jsonArray1 = jsonRootObject.optJSONArray("updatedReceipts");
                for(int i=0; i < jsonArray1.length(); i++) {
                    JSONObject jsonObject = jsonArray1.getJSONObject(i);
                    String receiptId = jsonObject.optString("receiptId").toString();
                    String rcId = jsonObject.optString("rcId").toString();
                    String commCode = jsonObject.optString("commCode").toString();

                    String query = String.format("update BenfiaryTxn set TxnUploadSts = 'Y' where RecptId='%s' and RcId='%s' and CommCode='%s'",receiptId,rcId,commCode);
                    System.out.println("Query: " +query);
                    db.execSQL(query);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
            if(db != null && db.isOpen())
                db.close();
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    callback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }

    public void parseDataDownloadAckResult(String strJson, Context context)
    {
        SQLiteDatabase db = null;
        try {
            JSONObject jsonRootObject = new JSONObject(strJson);
            msg = jsonRootObject.getString("respMessage");
            code = jsonRootObject.getString("respCode");

            if(code.equals("00"))
            {
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
            if(db != null && db.isOpen())
                db.close();
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    callback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }
}