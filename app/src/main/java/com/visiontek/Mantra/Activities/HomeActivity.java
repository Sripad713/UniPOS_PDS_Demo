package com.visiontek.Mantra.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Models.InspectionModel.InspectionDetails;
import com.visiontek.Mantra.Models.PartialOnlineData;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.ReceiveGoodsDetails;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Aadhaar_Parsing;
import com.visiontek.Mantra.Utils.Json_Parsing;
import com.visiontek.Mantra.Utils.Util;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import timber.log.Timber;
import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.memberConstants;
import static com.visiontek.Mantra.Models.AppConstants.txnType;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.diableMenu;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;

public class HomeActivity extends BaseActivity {

    public DatabaseHelper databaseHelper;
    String errorMessage = "";
    String token = "9f943748d8c1ff6ded5145c59d0b2ae7";
    String mode = "PDS";
    Button issue, inspection, aadhar, receive, reports, others, logout,offlinereceive,offlinercnumbers, upload;
    Intent i;
    Context context;
    ProgressDialog pd = null;

    private void logout(String logoutrequest) {
        try {
            Show(context.getResources().getString(R.string.Logout),
                    "");

            Aadhaar_Parsing request = new Aadhaar_Parsing(context, logoutrequest, 10);
            request.setOnResultListener(new Aadhaar_Parsing.OnResultListener() {

                @Override
                public void onCompleted(String code, String msg, String ref, String flow, Object object) {
                    Dismiss();
                    if (code == null || code.isEmpty()) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Logout),
                                context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                "",
                                2);
                        return;
                    }


                    if (!code.equals("00")) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Logout),
                                context.getResources().getString(R.string.ResponseCode) + code,
                                context.getResources().getString(R.string.ResponseMsg) + msg,
                                2);

                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Logout),
                                context.getResources().getString(R.string.ResponseCode) + code,
                                context.getResources().getString(R.string.ResponseMsg) + msg,
                                2);


                    }
                }

            });
            request.execute();
        } catch (Exception ex) {

            Timber.tag("Home-logout-").e(ex.getMessage(), "");
        }
    }

    private void FrameXMLforInspection() {
        try {

            String inspection = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n" +
                    "<soapenv:Envelope\n" +
                    "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                    "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "    xmlns:ser=\"http://service.fetch.rationcard/\">\n" +
                    "    <soapenv:Header/>\n" +
                    "    <soapenv:Body>\n" +
                    "        <ser:getInspectorDetails>\n" +
                    "            <fpsId>" + dealerConstants.stateBean.statefpsId + "</fpsId>\n" +
                    "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                    "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                    "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                    "        </ser:getInspectorDetails>\n" +
                    "    </soapenv:Body>\n" +
                    "</soapenv:Envelope>";
            if (networkConnected(context)) {
                System.out.println(inspection);
                //Util.generateNoteOnSD(context, "InspectionDetailsReq.txt", inspection);
                hit_Inspection(inspection);
            } else {
                show_AlertDialog(
                        context.getResources().getString(R.string.Home),
                        context.getResources().getString(R.string.Internet_Connection_Msg),
                        context.getResources().getString(R.string.Internet_Connection),
                        0);
            }
        } catch (Exception ex) {

            Timber.tag("Home-InspFrmt-").e(ex.getMessage(), "");
        }
    }

    private void FrameJsonforReceiveGoods() {
        try {

            if (mp != null) {
                releaseMediaPlayer(context, mp);
            }
            if (L.equals("hi")) {
            } else {
                mp = mp.create(context, R.raw.c100075);
                mp.start();
            }
            System.out.println("<<<<<<<<<REQUEST ADDED>>>>>>>>");
            String receiveGoods = "{\n" +
                    "  \"fps_id\" : " + "\"" + dealerConstants.stateBean.statefpsId + "\"" + ",\n" +
                    "  \"mode\" :" + "\"" + mode + "\"" + ",\n" +
                    "  \"stateCode\"  :" + "\"" + dealerConstants.stateBean.stateCode + "\"" + ",\n" +
                    "  \"token\" :  " + "\"" + token + "\"" + "\n" +
                    "}";
            System.out.println(receiveGoods);
            Util.generateNoteOnSD(context, "ReceiveGoodsReq.txt", receiveGoods);
            Timber.d("HomeActivity-ReceiveGoods  :"+receiveGoods);
            Show(context.getResources().getString(R.string.Receive_Goods),
                    context.getResources().getString(R.string.Processing)
            );


            Json_Parsing request = new Json_Parsing(context, receiveGoods, 1);

            request.setOnResultListener(new Json_Parsing.OnResultListener() {

                @Override
                public void onCompleted(String code, String msg, Object object) {
                    Dismiss();
                    if (code == null || code.isEmpty()) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Logout),
                                context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                "",
                                0);
                        return;
                    }


                    if (!code.equals("00")) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Receive_Goods),
                                context.getResources().getString(R.string.ResponseCode) + code,
                                context.getResources().getString(R.string.ResponseMsg) + msg,
                                0);
                    } else {
                        ReceiveGoodsDetails receiveGoodsDetails = (ReceiveGoodsDetails) object;
                        i = new Intent(context, ReceiveGoodsActivity.class);
                        System.out.println("<<<<<<<<<RECEIVE GOOD>>>>>>>>>");
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("OBJ", (Serializable) receiveGoodsDetails);
                        startActivityForResult(i, 1);
                    }
                }

            });
        } catch (Exception ex) {
            Timber.d("HomeActivity-FrameJsonforReceiveGoods() Exception ==>:"+ex.getLocalizedMessage());
            Timber.tag("Home-RGReq-").e(ex.getMessage(), "");
        }
    }

    private void hit_Inspection(String inspection) {
        try {

            if (mp != null) {
                releaseMediaPlayer(context, mp);
            }
            if (L.equals("hi")) {
            } else {
                mp = mp.create(context, R.raw.c100075);
                mp.start();
            }
            Show(context.getResources().getString(R.string.Inspection),
                    context.getResources().getString(R.string.Fetching_Details));

            Aadhaar_Parsing request = new Aadhaar_Parsing(context, inspection, 5);
            request.setOnResultListener(new Aadhaar_Parsing.OnResultListener() {

                @Override
                public void onCompleted(String code, String msg, String ref, String flow, Object object) {
                    Dismiss();
                    if (code == null || code.isEmpty()) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Logout),
                                context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                "",
                                0);
                        return;
                    }


                    if (!code.equals("00")) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.INSPECTION),
                                context.getResources().getString(R.string.Inspection_Details) + code,
                                context.getResources().getString(R.string.ResponseMsg) + msg,
                                0);
                    } else {
                        InspectionDetails inspectionDetails = (InspectionDetails) object;
                        Intent in = new Intent(context, InspectionActivity.class);
                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        in.putExtra("OBJ", (Serializable) inspectionDetails);
                        startActivity(in);
                    }
                }

            });
            request.execute();
        } catch (Exception ex) {

            Timber.tag("Home-InspReq-").e(ex.getMessage(), "");
        }
    }

    @Override
    public void initialize() {
        try {
            context = HomeActivity.this;
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_home, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();

            if(Util.networkConnected(context)) {
                if (diableMenu("getFpsStockDetails")) {
                    receive.setEnabled(false);
                }

                if (diableMenu("getInspectorDetails")) {
                    inspection.setEnabled(false);
                }
            }
            others.setEnabled(false);

            upload.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              preventTwoClick(v);

                                              HomeActivity.this.runOnUiThread(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                      if(Util.networkConnected(context)){
                                                          showTxnsdetails();
                                                      }else {
                                                          if (txnType == -1) {
                                                              show_AlertDialog(context.getResources().getString(R.string.Offline_Login), context.getResources().getString(R.string.Please_login_in_online_mode), "", 0);
                                                          }else {
                                                            if(!Util.networkConnected(context))

                                                              show_AlertDialog(context.getResources().getString(R.string.Internet_Connection), context.getResources().getString(R.string.Internet_Connection_Msg), "", 0);
                                                          }
                                                      }
                                                  }
                                              });


                                            /* if (txnType == -1) {
                                                  show_AlertDialog(context.getResources().getString(R.string.Offline_Login), context.getResources().getString(R.string.Please_login_in_online_mode), "", 0);
                                              } else if (!Util.networkConnected(context)) {
                                                  show_AlertDialog(context.getResources().getString(R.string.Internet_Connection), context.getResources().getString(R.string.Internet_Connection_Msg), "", 0);
                                              } else {
                                                  Show(context.getResources().getString(R.string.Upload_Data), context.getResources().getString(R.string.Please_Wait_Uploading_Pending_Records));
                                                  //pd = ProgressDialog.show(context, context.getResources().getString(R.string.Upload_Data), context.getResources().getString(R.string.Please_Wait_Uploading_Pending_Records), true, false);
                                                  new Thread(new Runnable() {
                                                      @Override
                                                      public void run() {
                                                          if (dealerConstants != null && dealerConstants.fpsCommonInfo != null) {
                                                              OfflineUploadNDownload offlineUploadNDownload = new OfflineUploadNDownload(context);
                                                              System.out.println("@@Calling ManualServerUploadPartialTxns in HOmeActivity");
                                                              int ret = offlineUploadNDownload.ManualServerUploadPartialTxns(dealerConstants.stateBean.statefpsId, dealerConstants.fpsCommonInfo.fpsSessionId);
                                                              if (ret == -2) {
                                                                  errorMessage = context.getResources().getString(R.string.Internet_Not_Available);
                                                              } else if (ret == 0) {
                                                                  errorMessage = context.getResources().getString(R.string.All_offline_txn_records_were_uploaded_to_server);
                                                              } else if(ret == 3){
                                                                  errorMessage = context.getResources().getString(R.string.No_Offline_txns);
                                                              }else{
                                                                  errorMessage = context.getResources().getString(R.string.Pending_Exists);
                                                              }
                                                          } else {
                                                              errorMessage = context.getResources().getString(R.string.Please_login_in_online_mode);
                                                          }
                                                          HomeActivity.this.runOnUiThread(new Runnable() {
                                                              @Override
                                                              public void run() {
                                                                  Dismiss();
                                                                  show_AlertDialog("ALERT", errorMessage, "", 0);
                                                              }
                                                          });
                                                      }

                                                  }).start();
                                              }*/
                                          }
                                      });



            issue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    memberConstants = null;
                    i = new Intent(context, IssueActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(i, 1);
                }
            });
            inspection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    memberConstants = null;
                    if (networkConnected(context)) {
                        FrameXMLforInspection();
                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Home),
                                context.getResources().getString(R.string.Internet_Connection_Msg),
                                context.getResources().getString(R.string.Internet_Connection),
                                0);
                    }

                }
            });
            aadhar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    memberConstants = null;
                    i = new Intent(context, AadhaarServicesActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(i, 1);
                }
            });
            receive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    memberConstants = null;
                    if (networkConnected(context)) {
                        FrameJsonforReceiveGoods();
                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Home),
                                context.getResources().getString(R.string.Internet_Connection_Msg),
                                context.getResources().getString(R.string.Internet_Connection),
                                0);
                    }
                }
            });

           /* offlinereceive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    preventTwoClick(v);
                    System.out.println("@@Clicked on offline receive goods");
                    Intent intent = new Intent(HomeActivity.this,offlineRecvGoods.class);
                    startActivity(intent);
                }
            });*/


            offlinercnumbers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    preventTwoClick(view);
                    Intent rcnumbers = new Intent(context, OfflineRcNumbers.class);
                    startActivity(rcnumbers);

                }
            });


            reports.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    i = new Intent(context, ReportsActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(i, 1);
                }
            });
            others.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                }
            });

            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    memberConstants = null;
                    if(txnType == -1 || !Util.networkConnected(context))
                    {
                        System.out.println("@@Offline logout");
                        //Offline transaction
                        Intent intent = new Intent(getApplicationContext(),StartActivity.class);
                        startActivity(intent);
                    }else {
                        String logoutreq = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<SOAP-ENV:Envelope\n" +
                                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                                "    xmlns:ser=\"http://service.fetch.rationcard/\">\n" +
                                "    <SOAP-ENV:Body>\n" +
                                "        <ser:logOut>\n" +
                                "            <fpsId>" + dealerConstants.stateBean.statefpsId + "</fpsId>\n" +
                                "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                                "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                                "            <deviceId>" + DEVICEID + "</deviceId>\n" +
                                "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                                "        </ser:logOut>\n" +
                                "    </SOAP-ENV:Body>\n" +
                                "</SOAP-ENV:Envelope>";
                        logout(logoutreq);
                    }
                }
            });
        } catch (Exception ex) {

            Timber.tag("Home-onCreate-").e(ex.getMessage(), "");
        }
    }

    @Override
    public void initializeControls() {
        pd = new ProgressDialog(context);
        databaseHelper = new DatabaseHelper(context);
        logout = findViewById(R.id.logout);
        issue = findViewById(R.id.issue);
        inspection = findViewById(R.id.inspection);
        aadhar = findViewById(R.id.aadhaar_services);
        receive = findViewById(R.id.receive_goods);
        upload = findViewById(R.id.uploaddata);
        //offlinereceive = findViewById(R.id.offlinereceivegoods);
        offlinercnumbers=findViewById(R.id.offline_rcnumbers);

        reports = findViewById(R.id.reports);
        others = findViewById(R.id.others);
        toolbarActivity.setText(context.getResources().getString(R.string.HOME));
        toolbarFpsid.setText("FPS ID");

        //offlinereceive.setVisibility(View.INVISIBLE);
        if(dealerConstants == null || dealerConstants.stateBean == null || dealerConstants.stateBean.statefpsId == null || dealerConstants.stateBean.statefpsId.length()<1) {
            //Get statefpsID from db
            System.out.println("@@.............................2");
            ArrayList<String> statefpsiD = databaseHelper.getStateDetails();
            System.out.println("@@.............................3");
            System.out.println("@@getStateDetails length: " +statefpsiD.size());
            toolbarFpsidValue.setText(statefpsiD.get(6));
            System.out.println("@@.............................4");

        }
        else {
            System.out.println("@@.............................5");
            toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
            System.out.println("@@.............................6");
        }

        if(txnType == -1 || !Util.networkConnected(context)){
            System.out.println("@@Offline transaction...");
            inspection.setEnabled(false);
            inspection.setClickable(false);
            inspection.setBackgroundColor(Color.LTGRAY);

            aadhar.setEnabled(false);
            aadhar.setClickable(false);
            aadhar.setBackgroundColor(Color.LTGRAY);

            others.setEnabled(false);
            others.setClickable(false);
            others.setBackgroundColor(Color.LTGRAY);

            receive.setEnabled(false);
            receive.setClickable(false);
            receive.setBackgroundColor(Color.LTGRAY);

            System.out.println("@@Modified");
        }else{
            inspection.setEnabled(true);
            aadhar.setEnabled(true);
            inspection.setClickable(true);
            others.setEnabled(true);
            others.setClickable(true);
            offlinercnumbers.setEnabled(false);
            offlinercnumbers.setClickable(false);
            offlinercnumbers.setBackgroundColor(Color.LTGRAY);
        }

    }
    //Offline
    public void showTxnsdetails() {
        System.out.println("@@In showTxnsdetails");
        int offlineCount = 0, onlineCount = 0, uploadedCount = 0, pendingCount = 0;
        try {
            int saleRecordCount[] = databaseHelper.getSaleRecordAgrregateCounts();
            onlineCount = saleRecordCount[0];
            offlineCount = saleRecordCount[1];
            uploadedCount = saleRecordCount[2];
            pendingCount = saleRecordCount[3];
        } catch (Exception e) {
            System.out.println("@@Exception cought: " + e.toString());
            e.printStackTrace();
        }
        //"Date : 15-08-1947 00:00:00 \n"

        PartialOnlineData partialOnlineData = databaseHelper.getPartialOnlineData();
        //DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this,
                AlertDialog.THEME_HOLO_LIGHT);
        alert.setTitle("Transaction Details");
        /*String details="PDS-P2.3.00 \n" +
                "Total Txn Records : " + (onlineCount + offlineCount) + "\n " +
                "Online Txn Records : " + onlineCount + " \n " +
                "Offline Txn Records : " + offlineCount + " \n" +
                "Uploaded Offline Records : " + uploadedCount + "\n" +
                "Pending Offline Records : " + pendingCount + "\n" +
                "Alloted Month&Year : " + partialOnlineData.getAllotMonth() + "-" + partialOnlineData.getAllotYear() + "\n" +
                "Date : " + formattedDate + "\n" +
                "Fps Id : " + dealerConstants.fpsCommonInfo.fpsId;*/
        String details="PDS-P2.3.00 \n" +
                context.getResources().getString(R.string.Total_Txn_Records) +" : "+ (onlineCount + offlineCount) + "\n " +
                context.getResources().getString(R.string.Online_Txn_Records) +" : "+ onlineCount + " \n " +
                context.getResources().getString(R.string.Offline_Txn_Records) +":"+ offlineCount + " \n" +
                context.getResources().getString(R.string.Uploaded_Offline_Records) +":"+ uploadedCount + "\n" +
                context.getResources().getString(R.string.Pending_Offline_Records) +":"+ pendingCount + "\n" +
                context.getResources().getString(R.string.Alloted_Month_Year)+":"+ partialOnlineData.getAllotMonth() + "-" + partialOnlineData.getAllotYear() + "\n" +
                context.getResources().getString(R.string.Date) + ":"+ formattedDate + "\n" +
                context.getResources().getString(R.string.Fps_Id) +" :"+ dealerConstants.fpsCommonInfo.fpsId;
        alert.setMessage(details);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("@@Going to home activity");
                dialog.dismiss();
                if (txnType == -1) {
                    show_AlertDialog(context.getResources().getString(R.string.Offline_Login), context.getResources().getString(R.string.Please_login_in_online_mode), "", 0);
                } else if (!Util.networkConnected(context)) {
                    show_AlertDialog(context.getResources().getString(R.string.Internet_Connection), context.getResources().getString(R.string.Internet_Connection_Msg), "", 0);
                } else {
                    Show(context.getResources().getString(R.string.Upload_Data), context.getResources().getString(R.string.Please_Wait_Uploading_Pending_Records));
                    //pd = ProgressDialog.show(context, context.getResources().getString(R.string.Upload_Data), context.getResources().getString(R.string.Please_Wait_Uploading_Pending_Records), true, false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (dealerConstants != null && dealerConstants.fpsCommonInfo != null) {
                                OfflineUploadNDownload offlineUploadNDownload = new OfflineUploadNDownload(context);
                                System.out.println("@@Calling ManualServerUploadPartialTxns in HOmeActivity");
                                int ret = offlineUploadNDownload.ManualServerUploadPartialTxns(dealerConstants.stateBean.statefpsId, dealerConstants.fpsCommonInfo.fpsSessionId);
                                System.out.println("RET ====="+ret);
                                Timber.d("HomeActivity-ManualServerUploadPartialTxns RET ==="+ret);
                                if (ret == -2) {
                                    errorMessage = context.getResources().getString(R.string.Internet_Not_Available);
                                } else if (ret == 0) {
                                    errorMessage = context.getResources().getString(R.string.All_offline_txn_records_were_uploaded_to_server);
                                } else if(ret == 3){
                                    errorMessage = context.getResources().getString(R.string.No_Offline_txns);
                                }else{
                                    errorMessage = context.getResources().getString(R.string.Pending_Exists);
                                }
                            } else {
                                errorMessage = context.getResources().getString(R.string.Please_login_in_online_mode);
                            }
                            HomeActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Dismiss();
                                    show_AlertDialog("ALERT", errorMessage, "", 0);
                                }
                            });
                        }

                    }).start();
                }

                /*Intent home = new Intent(context, HomeActivity.class);
                home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(home);
                finish();*/
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {

                dialog.dismiss();

            }
        });
        alert.show();
    }


    private void show_AlertDialog(String headermsg, String bodymsg, String talemsg, int i) {

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.alertdialog);
        Button confirm = (Button) dialog.findViewById(R.id.alertdialogok);
        TextView head = (TextView) dialog.findViewById(R.id.alertdialoghead);
        TextView body = (TextView) dialog.findViewById(R.id.alertdialogbody);
        TextView tale = (TextView) dialog.findViewById(R.id.alertdialogtale);
        head.setText(headermsg);
        body.setText(bodymsg);
        tale.setText(talemsg);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preventTwoClick(v);
                dialog.dismiss();
                if (i == 1) {
                    finish();
                }else if(i==2){
                    Intent intent = new Intent(getApplicationContext(),StartActivity.class);
                    startActivity(intent);
                }
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }


    public void Dismiss() {
        if (pd.isShowing()) {
            pd.dismiss();
        }
    }

    public void Show(String title, String msg) {
        SpannableString ss1 = new SpannableString(title);
        ss1.setSpan(new RelativeSizeSpan(2f), 0, ss1.length(), 0);
        SpannableString ss2 = new SpannableString(msg);
        ss2.setSpan(new RelativeSizeSpan(3f), 0, ss2.length(), 0);


        pd.setTitle(ss1);
        pd.setMessage(ss2);
        pd.setCancelable(false);
        pd.show();
    }


}
