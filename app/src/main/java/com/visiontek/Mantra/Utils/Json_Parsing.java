package com.visiontek.Mantra.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;


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


import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;

public class Json_Parsing {

    String myResponse;
    String msg, code;
    Object object;

    private OnResultListener onResultListener;
    Context context;
    public Json_Parsing(Context context, String frame, int type)  {
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
                } else {
                    url = "http://epos.nic.in/ePosCommonServiceCTG/eposCommon/getFpsStockDetails";
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

                        System.out.println("OUTPUT"+myResponse);
                        if (type == 1) {
                            //Util.generateNoteOnSD(context, "RecivegoodsRes.txt", myResponse);
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
                            //Util.generateNoteOnSD(context, "CancelRequestRes.txt", myResponse);
                            parse_cancelReq(myResponse);
                        } else if (type == 3) {
                            //Util.generateNoteOnSD(context, "ConsentFormRes.txt", myResponse);
                            parse_consentform(myResponse);
                        } else if (type == 4) {
                            //Util.generateNoteOnSD(context, "LogoutRes.txt", myResponse);
                            parse_logout(myResponse);
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
}
