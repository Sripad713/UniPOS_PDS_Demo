package com.visiontek.Mantra.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.visiontek.Mantra.Activities.Device_Update;
import com.visiontek.Mantra.Models.AadhaarServicesModel.BeneficiaryVerification.GetURLDetails.BeneficiaryDetails;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetURLDetails.fpsCommonInfoModel.fpsCommonInfo;
import com.visiontek.Mantra.Models.PartialOnlineData;
import com.visiontek.Mantra.Models.ResponseData;
import com.visiontek.Mantra.Models.UploadingModels.CommWiseData;
import com.visiontek.Mantra.Models.UploadingModels.DataDownloadAckRequest;
import com.visiontek.Mantra.Models.UploadingModels.UploadDataModel;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Utils.Json_Parsing;
import com.visiontek.Mantra.Utils.Util;
import com.visiontek.Mantra.Utils.XML_Parsing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.OFFLINE_TOKEN;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;

public class OfflineUploadNDownload {

    Context context;
    OkHttpClient okHttpClient;
    DatabaseHelper databaseHelper;

    public OfflineUploadNDownload(Context context)
    {
        this.context = context;
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        databaseHelper = new DatabaseHelper(context);
    }

    public int ManualServerUploadPartialTxns(String fpsId,String fpsSessionId)
    {
        System.out.println("@@In ManualServerUploadPartialTxns");
        RequestBody body = null;

        System.out.println("@@Data in fpsId: " +fpsId);
        System.out.println("@@Data in fpsSessionId: " +fpsSessionId);
        System.out.println("BOdy.." + body);

        String url="http://epos.nic.in/ePosCommonServiceCTG/eposCommon/pushFpsOfflineData";
        url=dealerConstants.fpsURLInfo.wsdlOffline+"pushFpsOfflineData";
        Gson gson = new Gson();

        while(true) {
            if (!Util.networkConnected(context))
            {
                Log.e("[ManlSrUpldPartialTxns]","network not connected");
                return -2;
            }

            UploadDataModel uploadDataModel = new UploadDataModel();
            List<CommWiseData> commWiseData = databaseHelper.getPendingOfflineData(20);
            PartialOnlineData partialOnlineData = databaseHelper.getPartialOnlineData();
            int totalRecords = databaseHelper.getTotCommodityTxns();
            System.out.println("@@Total records: " +totalRecords);
            int uploadingRecords = commWiseData.size();
            System.out.println("@@Data in uploadig records: " +uploadingRecords);
            if (uploadingRecords == 0) {
                System.out.println("@@No offline data found...");
                return 3;
            }
            uploadDataModel.setFpsId(fpsId);
            uploadDataModel.setSessionId(fpsSessionId);//
            uploadDataModel.setStateCode("22");
            uploadDataModel.setTerminalId(DEVICEID);//
            uploadDataModel.setToken(OFFLINE_TOKEN);
            uploadDataModel.setFpsOfflineTransResponses(commWiseData);
            uploadDataModel.setFpsCbs(databaseHelper.getPendingStock());
            uploadDataModel.setUploadingRecords(uploadingRecords);
            uploadDataModel.setTotalRecords(totalRecords);
            uploadDataModel.setPendingRecords(Math.abs(totalRecords - uploadingRecords));
            uploadDataModel.setDistributionMonth(partialOnlineData.getAllotMonth());
            uploadDataModel.setDistributionYear(partialOnlineData.getAllotYear());

            String finalPayload = gson.toJson(uploadDataModel);
            System.out.println("@@Data in finalpAYLOAD: " +finalPayload);
            Timber.d("OfflineUploadNDownload-ManualServerUploadPartialTxns FinalPayLoad :"+finalPayload);

            body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),finalPayload );
            final Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            Response response = null;
            try {
                response = okHttpClient.newCall(request).execute();
                System.out.println("RESPONE ======###"+response);
                Timber.d("ManualServerUploadPartialTxns RESPONSE : "+response);
                if(response.isSuccessful()){
                    System.out.println("@@Response success....");
                    String responseData = response.body().string();
                    System.out.println("@@Response data: " +responseData);
                    Timber.d("ManualServerUploadPartialTxns RESPONSE : "+response);
                    int updateRes= insertPosObRecords(responseData);
                    System.out.println("POS_OB===="+updateRes);

                    if(updateRes != 0)
                    {
                        System.out.println("@@Insertion failed");
                        Log.e("insertPosObRecords","returned : "+responseData);
                        return -1;
                    }

                    System.out.println("@@Going to update updateBenfiaryTxnRecords");
                    System.out.println("@@Data to upload: " +responseData);
                    Timber.d("ManualServerUploadPartialTxns-Going to update updateBenfiaryTxnRecords :"+responseData);
                    updateRes= updateBenfiaryTxnRecords(responseData);
                    if(updateRes != 0)
                    {
                        System.out.println("@@Benificiary updation failed");
                        Log.e("updateBnfryTxnRecords","returned : "+responseData);
                        return -1;
                    }

                    if(dealerConstants.fpsCommonInfo.partialOnlineOfflineStatus != null) {
                        int ret = updateTransStatus(fpsId, fpsSessionId, dealerConstants.fpsCommonInfo.partialOnlineOfflineStatus);
                        System.out.println("updateTransStatus RET ===="+ret);
                        Timber.d("ManualServerUploadPartialTxns-updateTransStatus RET  "+ret);
                        return ret;
                    }
                    //Send Acknowledgement

                }else {

                    Log.e("[ManualServerUploadTxn]","Failed ==> "+response.message());
                    System.out.println("ManualServerUploadPartialTxns  FAILED ====>"+response.message());
                    Timber.d("ManualServerUploadPartialTxns-postDataDownloadAck RESPONSE FAILED : "+response.message());
                }
            } catch (IOException e) {
                e.printStackTrace();
                Timber.e("OfflineUploadNDownload-ManualServerUploadPartialTxns Exception ==> "+e.getLocalizedMessage());
            }
        }
    }

    public int updateBenfiaryTxnRecords(String strJson)
    {
        System.out.println("@@In updateBenificiaryTxn recorrds");
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        int ret = 0;
        try {
            JSONObject jsonRootObject = new JSONObject(strJson);
            String msg = jsonRootObject.getString("respMessage");
            String respcode = jsonRootObject.getString("respCode");

            System.out.println("@@Data in msg: " +msg);
            System.out.println("@@Data in respCode: " +respcode);

            if(respcode.equals("00"))
            {
                JSONArray jsonArray1 = jsonRootObject.optJSONArray("updatedReceipts");
                int size = jsonArray1.length();
                System.out.println("[updateBenfiaryTxnRecords]  updatedReceipts.size :: " +size);
                if(size == 0)
                {
                    ret = -1;
                }
                for(int i=0; i < size; i++) {
                    JSONObject jsonObject = jsonArray1.getJSONObject(i);
                    String receiptId = jsonObject.optString("receiptId").toString();
                    String rcId = jsonObject.optString("rcId").toString();
                    String commCode = jsonObject.optString("commCode").toString();

                    String query = String.format("update BenfiaryTxn set TxnUploadSts = 'Y' where RecptId='%s' and RcId='%s' and CommCode='%s'",receiptId,rcId,commCode);
                    System.out.println("Query: " +query);
                    sqLiteDatabase.execSQL(query);
                }
            }
            else
                ret = -1;

        } catch (JSONException e) {
            System.out.println("@@JSONException: " +e.toString());
            e.printStackTrace();
            ret = -1;
        }
        finally {
            System.out.println("@@In finally");
            if(sqLiteDatabase != null && sqLiteDatabase.isOpen())
                sqLiteDatabase.close();
            return ret;
        }
    }

    public int insertPosObRecords(String strJson)
    {
        System.out.println("@@In insertPosObRecords");
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        int ret = 0;
        try {
            JSONObject jsonRootObject = new JSONObject(strJson);
            String msg = jsonRootObject.getString("respMessage");
            String respcode = jsonRootObject.getString("respCode");

            System.out.println("@@Message: " +msg);
            System.out.println("@@REsponse: "+respcode);
            if(respcode.equals("00"))
            {
                JSONArray jsonArray1 = jsonRootObject.optJSONArray("fpsCb");
                int size = jsonArray1.length();
                System.out.println("[insertPosObRecords]  fpsCb.size :: " +size);
                if(size == 0)
                {
                    ret = -1;
                }
                else
                {
                    sqLiteDatabase.delete("pos_ob",null,null);
                    for(int i=0; i < size; i++) {
                        JSONObject jsonObject = jsonArray1.getJSONObject(i);
                        String commCode = jsonObject.optString("commCode").toString();
                        String commNameEn = jsonObject.optString("commNameEn").toString();
                        String commNameLl = jsonObject.optString("commNameLl").toString();
                        String closingBalance = String.format("%0.3lf",jsonObject.getDouble("closingBalance"));

                        String query = String.format("insert into Pos_Ob(commCode,commNameEn,commNameLl,closingBalance) VALUES(%s,%s,%s,%s)",commCode,commNameEn,commNameLl,closingBalance);
                        System.out.println("Query: " +query);
                        sqLiteDatabase.execSQL(query);
                    }
                }
            }
            else
                ret = -1;

        } catch (JSONException e) {
            e.printStackTrace();
            ret = -1;
        }
        finally {
            if(sqLiteDatabase != null && sqLiteDatabase.isOpen())
                sqLiteDatabase.close();
            return ret;
        }
    }

    public int updateTransStatus(String fpsId,String fpsSessionId,String partialDataDownloadFlag)
    {
        RequestBody body = null;

        System.out.println("BOdy.." + body);
        String url="http://epos.nic.in/ePosCommonServiceCTG/eposCommon/dataDownloadACK";
        url=dealerConstants.fpsURLInfo.wsdlOffline+"dataDownloadACK";
        Gson gson = new Gson();

        UploadDataModel uploadDataModel = new UploadDataModel();
        List<CommWiseData> commWiseData = databaseHelper.getPendingOfflineData(1000);
        PartialOnlineData partialOnlineData = databaseHelper.getPartialOnlineData();
        int totalRecords = databaseHelper.getTotCommodityTxns();
        int uploadingRecords = commWiseData.size();
        uploadDataModel.setFpsId(fpsId);
        uploadDataModel.setSessionId(fpsSessionId);//
        uploadDataModel.setStateCode("22");
        uploadDataModel.setTerminalId(DEVICEID);//
        uploadDataModel.setToken(OFFLINE_TOKEN);
        uploadDataModel.setFpsOfflineTransResponses(commWiseData);
        uploadDataModel.setUploadingRecords(uploadingRecords);
        uploadDataModel.setTotalRecords(totalRecords);
        uploadDataModel.setPendingRecords(Math.abs(totalRecords - uploadingRecords));
        uploadDataModel.setDistributionMonth(partialOnlineData.getAllotMonth());
        uploadDataModel.setDistributionYear(partialOnlineData.getAllotYear());
        uploadDataModel.setDataDownloadStatus(partialDataDownloadFlag);
        uploadDataModel.setKeyregisterDataDeleteStatus("U");
        if(partialDataDownloadFlag.equals("Y"))
            uploadDataModel.setFullDataUploadedStatus("Y");
        else
            uploadDataModel.setFullDataUploadedStatus("N");

        String finalPayload = gson.toJson(uploadDataModel);

        body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),finalPayload );
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            System.out.println("Update Transaction Status ====="+response);
            Timber.d("UpdateTransactionStatus RESPONSE :"+response);
            if(response.isSuccessful()){

                String responseData = response.body().string();
                System.out.println("RESPONSE ===="+responseData);
                Timber.d("UpdateTransactionStatus RESPONSEDATA :"+response);

                return parseDataDownloadAckresponse(responseData);
            }else {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int parseDataDownloadAckresponse(String response)
    {
        System.out.println("Parse DataDownload Ack====");
        PartialOnlineData partialOnlineData = databaseHelper.getPartialOnlineData();
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        int ret = -1;
        try {
            JSONObject jsonRootObject = new JSONObject(response);
            String msg = jsonRootObject.getString("respMessage");
            String respcode = jsonRootObject.getString("respCode");

            if(respcode.equals("00"))
            {
                sqLiteDatabase.execSQL("update BenfiaryTxn set TxnUploadSts = 'Y'");
                String deleteStatus = jsonRootObject.getString("deleteStatus");
                //JSONArray jsonArray1 = jsonRootObject.optJSONArray("fpsCb");
                if(deleteStatus.equals("Y") && partialOnlineData.getOfflineLogin().equals("N"))
                {
                    sqLiteDatabase.execSQL("DELETE FROM KeyRegister");
                    sqLiteDatabase.execSQL("DELETE FROM Pos_Ob");
                    sqLiteDatabase.execSQL("DELETE FROM CommodityMaster");
                    sqLiteDatabase.execSQL("DELETE FROM SchemeMaster");
                    sqLiteDatabase.execSQL("DELETE FROM BenfiaryTxn");
                }
                ret = 0;
            }
            else
                ret = -1;

        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e("parseDataDownloadAckresponse Exception ==> "+e.getLocalizedMessage());
            ret = -1;
        }
        finally {
            if(sqLiteDatabase != null && sqLiteDatabase.isOpen())
                sqLiteDatabase.close();
            return ret;
        }
    }

    public ResponseData postDataDownloadAck(String fpsId, String fpsSessionId, String partialDataDownloadFlag, String KeyregisterDataDeleteStatus)
    {
        System.out.println("@@In postDataDownloadAck");
        RequestBody body = null;
        ResponseData responseDataModel = new ResponseData();
        responseDataModel.setRespCode(-1);
        responseDataModel.setRespMessage(context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again));

        System.out.println("BOdy.." + body);
        String url="http://epos.nic.in/ePosCommonServiceCTG/eposCommon/dataDownloadACK";
        url=dealerConstants.fpsURLInfo.wsdlOffline+"dataDownloadACK";

        DataDownloadAckRequest dataDownloadAckRequest = new DataDownloadAckRequest();
        Gson gson = new Gson();
        PartialOnlineData partialOnlineData = databaseHelper.getPartialOnlineData();

        dataDownloadAckRequest.setDataDownloadStatus("Y");
        dataDownloadAckRequest.setDistributionMonth(partialOnlineData.getAllotMonth());
        dataDownloadAckRequest.setDistributionYear(partialOnlineData.getAllotYear());
        dataDownloadAckRequest.setFpsId(fpsId);
        dataDownloadAckRequest.setKeyregisterDataDeleteStatus(KeyregisterDataDeleteStatus);
        dataDownloadAckRequest.setPendingRecords(0);
        dataDownloadAckRequest.setSessionId(fpsSessionId);
        dataDownloadAckRequest.setStateCode("22");
        dataDownloadAckRequest.setTerminalId(DEVICEID);
        dataDownloadAckRequest.setToken(OFFLINE_TOKEN);
        dataDownloadAckRequest.setTotalRecords(0);
        dataDownloadAckRequest.setUploadingRecords(0);

        if(KeyregisterDataDeleteStatus.equals("Y"))
        {
            List<CommWiseData> commWiseData = databaseHelper.getPendingOfflineData(1000);
            dataDownloadAckRequest.setFpsOfflineTransResponses(commWiseData);
        }

        String finalPayload = gson.toJson(dataDownloadAckRequest);

        body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),finalPayload );
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            Log.e("[postDataDownloadAck]","HTTP response code ==> "+response.code());
            Timber.d("OfflineUploadNDownload-postDataDownloadAck RESPONSE :"+response);
            if(response.isSuccessful()){

                String responseData = response.body().string();
                Timber.d("OfflineUploadNDownload-postDataDownloadAck RESPONSE DATA :"+responseData);
                JSONObject jsonRootObject = new JSONObject(responseData);
                String respcode = jsonRootObject.getString("respCode");
                Log.e("[postDataDownloadAck]","response respCode ==> "+respcode);
                responseDataModel.setRespCode(Integer.parseInt(respcode));
                responseDataModel.setRespMessage(jsonRootObject.getString("respMessage"));
            }else {
                Log.e("[postDataDownloadAck]","Failed ==> "+response.message());
                Timber.d("OfflineUploadNDownload-postDataDownloadAck RESPONSE FAILED : "+response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            Timber.e("postDataDownloadAck Exception "+e.getLocalizedMessage());
            e.printStackTrace();
        }
        return responseDataModel;
    }

    public ResponseData ManualServerUploadsByCardNumber(String fpsId,String fpsSessionId,String rcNumber)
    {
        System.out.println("@@In ManualServerUploadsByCardNumber");
        System.out.println("@@FPS ID: " +fpsId);
        System.out.println("@@FPS session ID: " +fpsSessionId);
        System.out.println("@@rcNNumber: " +rcNumber);
        RequestBody body = null;
        ResponseData responseDataModel = new ResponseData();
        responseDataModel.setRespCode(-1);
        responseDataModel.setRespMessage(context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again));

        System.out.println("BOdy.." + body);
        String url="http://epos.nic.in/ePosCommonServiceCTG/eposCommon/pushFpsOfflineData";
        url=dealerConstants.fpsURLInfo.wsdlOffline+"pushFpsOfflineData";
        Gson gson = new Gson();

        if (!Util.networkConnected(context))
        {
            Log.e("[ManlSrUpldPartialTxns]","network not connected");
            responseDataModel.setRespCode(-1);
            responseDataModel.setRespMessage("network not connected");
            return responseDataModel;
        }
        UploadDataModel uploadDataModel = new UploadDataModel();
        List<CommWiseData> commWiseData = databaseHelper.getPendingOfflineData(rcNumber);
        int uploadingRecords = commWiseData.size();
        if(uploadingRecords <= 0 )
        {
            System.out.println("@@No pending records to upload");
        }

        PartialOnlineData partialOnlineData = databaseHelper.getPartialOnlineData();
        int totalRecords = databaseHelper.getTotCommodityTxns();

        System.out.println("@@Data in totalRecords: " +totalRecords);
        System.out.println("@@Data in uploading Records: " +uploadingRecords);

        List stock = databaseHelper.getPendingStock();
        if(stock.size() <= 0)
        {
            System.out.println("@@No details in DB... please download details first");
            return null;
        }

        uploadDataModel.setFpsId(fpsId);//
        uploadDataModel.setSessionId(fpsSessionId);//
        uploadDataModel.setStateCode("22");//
        uploadDataModel.setTerminalId(DEVICEID);//
        uploadDataModel.setToken(OFFLINE_TOKEN);
        uploadDataModel.setFpsOfflineTransResponses(commWiseData);
        uploadDataModel.setFpsCbs(databaseHelper.getPendingStock());
        uploadDataModel.setUploadingRecords(uploadingRecords);
        uploadDataModel.setTotalRecords(totalRecords);
        uploadDataModel.setPendingRecords(Math.abs(totalRecords - uploadingRecords));
        uploadDataModel.setDistributionMonth(partialOnlineData.getAllotMonth());//
        uploadDataModel.setDistributionYear(partialOnlineData.getAllotYear());//

        String finalPayload = gson.toJson(uploadDataModel);
        System.out.println("@@Data in finalpayload: " +finalPayload);

        body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),finalPayload );
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            Timber.d("ManualServerUploadsByCardNumber RESPONSE :"+response);
            System.out.println("@@Data in response: " +response.toString());
            System.out.println("@@Response1: "+response.message().toString());
            System.out.println("@@Response2: " +response.networkResponse().toString());
            System.out.println("@@Response3: " +response.body().toString());
            if(response.isSuccessful()){
                System.out.println("@@Response success");
                String responseData = response.body().string();
                System.out.println("@@Data in responsedata: " +responseData);
                Timber.d("ManualServerUploadsByCardNumber RESPONSE DATA :"+responseData);
                JSONObject jsonRootObject = new JSONObject(responseData);
                String msg = jsonRootObject.getString("respMessage");
                String respcode = jsonRootObject.getString("respCode");

                System.out.println("@@Data in msg: " +msg);
                System.out.println("@@Data in resp code: " +respcode);
                if(respcode != null)
                    responseDataModel.setRespCode(Integer.parseInt(respcode));
                if(msg!=null)
                    responseDataModel.setRespMessage(msg);

                if(respcode.equals("00"))
                {
                    if(uploadingRecords > 0)
                    {
                        Timber.d("ManualServerUploadsByCardNumber-Going to update updateBenfiaryTxnRecords :"+responseData);
                        int updateRes= updateBenfiaryTxnRecords(responseData);
                        if(updateRes != 0)
                        {
                            Log.e("updateBnfryTxnRecords","returned : "+responseData);
                            return responseDataModel;
                        }
                        return responseDataModel;
                    }
                    else
                        return responseDataModel;
                }
                return  responseDataModel;
            }else {
                return  responseDataModel;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e("ManualServerUploadsByCardNumber Exception =="+e.getLocalizedMessage());
        }
        return responseDataModel;
    }
}
