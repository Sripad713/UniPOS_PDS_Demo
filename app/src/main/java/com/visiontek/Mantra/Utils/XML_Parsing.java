package com.visiontek.Mantra.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.visiontek.Mantra.Models.DealerDetailsModel.GetURLDetails.Dealer;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetURLDetails.fpsCommonInfoModel.fpsCommonInfo;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetURLDetails.fpsCommonInfoModel.fpsDetails;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetURLDetails.fpsURLInfo;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetURLDetails.reasonBeanLists;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetURLDetails.stateBean;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetURLDetails.transactionMode;
import com.visiontek.Mantra.Models.IssueModel.LastReceipt;
import com.visiontek.Mantra.Models.IssueModel.LastReceiptComm;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.Ekyc;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetURLDetails.Member;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetURLDetails.carddetails;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetURLDetails.commDetails;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetURLDetails.memberdetails;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.Print;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.printBeans;
import com.visiontek.Mantra.Models.MenuDetailsModel.Menus;
import com.visiontek.Mantra.Models.MenuDetailsModel.fpsPofflineToken;
import com.visiontek.Mantra.Models.MenuDetailsModel.mBean;
import com.visiontek.Mantra.Models.ReportsModel.DailySalesDetails.SaleDetails;
import com.visiontek.Mantra.Models.ReportsModel.DailySalesDetails.drBean;
import com.visiontek.Mantra.Models.ReportsModel.Stockdetails.StockDetails;
import com.visiontek.Mantra.Models.ReportsModel.Stockdetails.astockBean;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.memberConstants;
import static com.visiontek.Mantra.Models.AppConstants.menuConstants;


@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class XML_Parsing extends AsyncTask<String, Void, Void> {
    private final String xmlformat;
    private final int type;
    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private String code;
    private HttpURLConnection urlConnection;
    private OnResultListener onResultListener;
    private String msg;
    private String ref;
    private String flow;
    Object object;

    public XML_Parsing(Context context, String xmlformat, int type) {
        this.context = context;
        this.xmlformat = xmlformat;
        this.type = type;
    }

    public void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... param) {
        String url = "http://epos.nic.in/ePosServiceJDN2_3/jdCommoneposServiceRes?wsdl";
        //String url = "http://epos.nic.in/ePosServiceCTG/jdCommoneposServiceRes?wsdl";
        runRequest(xmlformat, url, type);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (onResultListener != null) {
            onResultListener.onCompleted(code, msg, ref, flow, object);
        }
    }

    private void runRequest(String hit, String url, int type) {
        try {
            System.out.println("INPUT=" + hit);
            URL Url = new URL(url);
            urlConnection = (HttpURLConnection) Url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "text/xml");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setConnectTimeout(60*1000);
            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.write(hit.getBytes());
            outputStream.flush();
            outputStream.close();
            urlConnection.connect();
            System.out.println("++++++++++" + (urlConnection.getResponseCode()));
            String result ;
            if (urlConnection.getResponseCode() == 200) {
                BufferedInputStream bis = new BufferedInputStream(urlConnection.getInputStream());
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                int result2 = bis.read();
                while (result2 != -1) {
                    buf.write((byte) result2);
                    result2 = bis.read();
                }
                result = buf.toString();
                if (result != null && result.length() > 0) {
                    System.out.println("OUTPU = " + result);
                    if (type == 1) {
                        Util.generateNoteOnSD(context, "DDetailsRes.txt", result);
                        dealerConstants = parseXml_dealer(result);
                    } else if (type == 2) {
                        Util.generateNoteOnSD(context, "DealerAuthRes.txt", result);
                        parseXml_dealer_login(result);
                    } else if (type == 3) {
                        Util.generateNoteOnSD(context, "MDetailsRes.txt", result);
                        memberConstants = parseXml_member(result);
                    } else if (type == 4) {
                        Util.generateNoteOnSD(context, "MemberAuthRes.txt", result);
                        parseXml_member_login(result);
                    } else if (type == 5) {
                        Util.generateNoteOnSD(context, "SaleDetailsRes.txt", result);
                        object = parseXml_sale_details(result);
                    } else if (type == 6) {
                        Util.generateNoteOnSD(context, "StockDetailsRes.txt", result);
                        object = parseXml_stock_details(result);
                    } else if (type == 7) {
                        Util.generateNoteOnSD(context, "MenuRes.txt", result);
                        menuConstants = parseXml_menu_details(result);
                    } else if (type == 8) {
                        Util.generateNoteOnSD(context, "MembereKycRes.txt", result);
                        object = parseXml_eKyc(result);
                    } else if (type == 9) {
                        Util.generateNoteOnSD(context, "LastReciptRes.txt", result);
                        object = parseXml_LastRecipt(result);
                    } else if (type == 10) {
                        Util.generateNoteOnSD(context, "ManualRes.txt", result);
                        parseXml_Manual(result);
                    } else if (type == 11) {
                        Util.generateNoteOnSD(context, "RationRes.txt", result);
                        object = parseXml_printer(result);
                    } else if (type == 15) {
                        Util.generateNoteOnSD(context, "RGDealerAuthRes.txt", result);
                        parseXml_RCDealer(result);
                    } else {
                        Util.generateNoteOnSD(context, "ERRORR.txt", result);
                    }

                } else {
                    code = "2";
                    msg = "PARSING Error";
                }
            }else if (urlConnection.getResponseCode() == 400){
                code = "400";
                msg = "Bad Request";
            }

        } catch (Exception e) {
            e.printStackTrace();
            code = "1";
            msg = e.toString();
            msg = "Server Connection Failed";

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private void parseXml_RCDealer(String result) {

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(result));
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {

                    if (xpp.getName().equals("respCode")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            code = (xpp.getText());
                            System.out.println("Resp CODE *****************************" + xpp.getText());
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());
                                System.out.println("Resp MSG *****************************" + xpp.getText());
                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("transaction_flow")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                flow = (xpp.getText());
                                System.out.println("transaction_flow *****************************" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("auth_transaction_code")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                ref = (xpp.getText());
                                System.out.println("auth_transaction_code *****************************" + xpp.getText());
                            }
                        }
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            code = "1";
            msg = String.valueOf(e);
        } catch (IOException e) {
            e.printStackTrace();
            code = "2";
            msg = String.valueOf(e);
        }
    }

    private void parseXml_Manual(String result) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(result));
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {

                    if (xpp.getName().equals("respCode")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            code = (xpp.getText());
                            System.out.println("Resp CODE *****************************" + xpp.getText());
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());
                                System.out.println("Resp MSG *****************************" + xpp.getText());
                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("transaction_flow")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                flow = (xpp.getText());
                                System.out.println("transaction_flow *****************************" + xpp.getText());
                            }
                        }
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            code = "1";
            msg = String.valueOf(e);
        } catch (IOException e) {
            e.printStackTrace();
            code = "2";
            msg = String.valueOf(e);
        }
    }

    private LastReceipt parseXml_LastRecipt(String result) {
        LastReceipt lastReceipt = new LastReceipt();
        LastReceiptComm lastReceiptComm = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(result));
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("availedFps")) {
                        lastReceiptComm = new LastReceiptComm();
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            lastReceiptComm.availedFps = (xpp.getText());
                            System.out.println("availedFps 1 =================" + xpp.getText());
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("bal_qty")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceiptComm.bal_qty = (xpp.getText());
                                System.out.println("bal_qty 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("carry_over")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceiptComm.carry_over = (xpp.getText());
                                System.out.println("carry_over 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("commIndividualAmount")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceiptComm.commIndividualAmount = (xpp.getText());
                                System.out.println("commIndividualAmount 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("comm_name")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceiptComm.comm_name = (xpp.getText());
                                System.out.println("comm_name 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("comm_name_ll")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceiptComm.comm_name_ll = (xpp.getText());
                                System.out.println("comm_name_ll 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("member_name")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceiptComm.member_name = (xpp.getText());
                                System.out.println("member_name 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("member_name_ll")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceiptComm.member_name_ll = (xpp.getText());
                                System.out.println("member_name_ll 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("rcId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceiptComm.rcId = (xpp.getText());
                                System.out.println("rcId 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("reciept_id")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceiptComm.reciept_id = (xpp.getText());
                                System.out.println("reciept_id 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("retail_price")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                if (lastReceiptComm != null) {
                                    lastReceiptComm.retail_price = (xpp.getText());
                                    System.out.println("retail_price 2 =================" + xpp.getText());
                                }
                            }

                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("scheme_desc_en")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceiptComm.scheme_desc_en = (xpp.getText());
                                System.out.println("scheme_desc_en 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("scheme_desc_ll")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceiptComm.scheme_desc_ll = (xpp.getText());
                                System.out.println("scheme_desc_ll 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("tot_amount")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceiptComm.tot_amount = (xpp.getText());
                                System.out.println("tot_amount 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("total_quantity")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceiptComm.total_quantity = (xpp.getText());
                                System.out.println("total_quantity 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("transaction_time")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceiptComm.transaction_time = (xpp.getText());
                                System.out.println("transaction_time 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("uid_refer_no")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceiptComm.uid_refer_no = (xpp.getText());
                                System.out.println("uid_refer_no 2 =================" + xpp.getText());
                                lastReceipt.lastReceiptComm.add(lastReceiptComm);
                            }
                        }
                    }
                    /*if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("rcId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceipt.rcId = (xpp.getText());
                                System.out.println("rcId 2 =================" + xpp.getText());
                            }
                        }
                    }*/
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("retail_price")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                lastReceipt.retail_price = (xpp.getText());
                                System.out.println("retail_price 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                code = (xpp.getText());
                                System.out.println("respCode 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());
                                System.out.println("respMessage 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                code = (xpp.getText());
                                System.out.println("respCode 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());
                                System.out.println("respMessage 2 =================" + xpp.getText());
                            }
                        }
                    }
                    // }
                }
                eventType = xpp.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
            code = "1";
            msg = String.valueOf(e);
        } catch (IOException e) {
            e.printStackTrace();
            code = "2";
            msg = String.valueOf(e);
        }
        return lastReceipt;
    }

    private Ekyc parseXml_eKyc(String result) {
        Ekyc ekyc = new Ekyc();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(result));
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {

                    if (xpp.getName().equals("respCode")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            code = (xpp.getText());
                            System.out.println("Resp CODE *****************************" + xpp.getText());
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());
                                System.out.println("Resp MSG *****************************" + xpp.getText());
                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("transaction_flow")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                flow = (xpp.getText());
                                System.out.println("transaction_flow *****************************" + xpp.getText());
                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("zdistrTxnId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                ekyc.zdistrTxnId = (xpp.getText());
                                System.out.println("zdistrTxnId *****************************" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("eKYCDOB")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                ekyc.eKYCDOB = (xpp.getText());
                                System.out.println("eKYCDOB *****************************" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("eKYCGeneder")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                ekyc.eKYCGeneder = (xpp.getText());
                                System.out.println("eKYCGeneder *****************************" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("eKYCMemberName")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                ekyc.eKYCMemberName = (xpp.getText());
                                System.out.println("eKYCMemberName *****************************" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("eKYCPindCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                ekyc.eKYCPindCode = (xpp.getText());
                                System.out.println("eKYCPindCode *****************************" + xpp.getText());
                            }
                        }
                    }
                }
                eventType = xpp.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
            code = "1";
            msg = String.valueOf(e);
        } catch (IOException e) {
            e.printStackTrace();
            code = "2";
            msg = String.valueOf(e);
        }

        return ekyc;
    }

    private Menus parseXml_menu_details(String result) {
        Menus menus = new Menus();
        mBean mBean = null;
        fpsPofflineToken fpsPofflineToken = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(result));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("fpsPofflineToken")) {
                        fpsPofflineToken = new fpsPofflineToken();
                    }
                    if (xpp.getName().equals("mBean")) {
                        mBean = new mBean();
                    }

                    if (xpp.getName().equals("allocationMonth")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            fpsPofflineToken.allocationMonth = (xpp.getText());
                            System.out.println("allocationMonth 1 =================" + xpp.getText());
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("allocationYear")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsPofflineToken.allocationYear = (xpp.getText());
                                System.out.println("allocationYear 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("fpsToken")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsPofflineToken.fpsToken = (xpp.getText());
                                System.out.println("fpsToken 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("fpsTokenAllowdOrnotStatus")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsPofflineToken.fpsTokenAllowdOrnotStatus = (xpp.getText());
                                System.out.println("fpsTokenAllowdOrnotStatus 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("receiveGoodsOfflineEndDate")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsPofflineToken.receiveGoodsOfflineEndDate = (xpp.getText());
                                System.out.println("receiveGoodsOfflineEndDate 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("pOfflineDurationTimeInaDay")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsPofflineToken.pOfflineDurationTimeInaDay = (xpp.getText());
                                System.out.println("pOfflineDurationTimeInaDay 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("pOfflineStoppedDate")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsPofflineToken.pOfflineStoppedDate = (xpp.getText());
                                System.out.println("pOfflineStoppedDate 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("pOfflineTransactionTime")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsPofflineToken.pOfflineTransactionTime = (xpp.getText());
                                System.out.println("pOfflineTransactionTime 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                code = (xpp.getText());
                                System.out.println("respCode 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());
                                System.out.println("respMessage 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("skey")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                menus.skey = (xpp.getText());
                                System.out.println("respMessage 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("mainMenu")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                mBean.mainMenu = (xpp.getText());
                                System.out.println("mainMenu 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("menuName")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                mBean.menuName = (xpp.getText());
                                System.out.println("menuName 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("service")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                mBean.service = (xpp.getText());
                                System.out.println("service 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("slno")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                mBean.slno = (xpp.getText());
                                System.out.println("slno 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("status")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                mBean.status = (xpp.getText());
                                System.out.println("status 2 =================" + xpp.getText());

                            }
                        }
                    }

                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equals("fpsPofflineToken")) {
                        menus.fpsPofflineToken = fpsPofflineToken;
                    }
                    if (xpp.getName().equals("mBean")) {
                        menus.mBean.add(mBean);
                    }
                }
                eventType = xpp.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
            code = "1";
            msg = String.valueOf(e);
        } catch (IOException e) {
            e.printStackTrace();
            code = "2";
            msg = String.valueOf(e);
        }
        return menus;
    }

    private Dealer parseXml_dealer(String xmlString) {
        Dealer dealer = new Dealer();
        fpsCommonInfo fpsCommonInfo = null;
        fpsDetails fpsDetails = null;
        fpsURLInfo fpsURLInfo = null;
        reasonBeanLists reasonBeanLists = null;
        stateBean stateBean = null;
        transactionMode transactionMode = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlString));
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {

                    if (xpp.getName().equals("fpsCommonInfo")) {
                        fpsCommonInfo = new fpsCommonInfo();
                    }
                    if (xpp.getName().equals("fpsDetails")) {
                        fpsDetails = new fpsDetails();
                    }
                    if (xpp.getName().equals("fpsURLInfo")) {
                        fpsURLInfo = new fpsURLInfo();
                    }
                    if (xpp.getName().equals("reasonBeanLists")) {
                        reasonBeanLists = new reasonBeanLists();
                    }
                    if (xpp.getName().equals("stateBean")) {
                        stateBean = new stateBean();
                    }
                    if (xpp.getName().equals("transactionMode")) {
                        transactionMode = new transactionMode();
                    }

                    if (xpp.getName().equals("dealer_password")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            fpsCommonInfo.dealer_password = (xpp.getText());
                            System.out.println("dealer_password =================" + xpp.getText());
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("distCode")) {
                            System.out.println("333333333333333333333333");
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.distCode = (xpp.getText());
                                System.out.println("distCode =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("flasMessage1")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.flasMessage1 = (xpp.getText());
                                System.out.println("flasMessage1 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("flasMessage2")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.flasMessage2 = (xpp.getText());
                                System.out.println("flasMessage2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("fpsId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.fpsId = (xpp.getText());
                                System.out.println("fpsId =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("fpsSessionId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.fpsSessionId = (xpp.getText());
                                System.out.println("fpsSessionId =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("keyregisterDataDeleteStatus")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.keyregisterDataDeleteStatus = (xpp.getText());
                                System.out.println("keyregisterDataDeleteStatus =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("keyregisterDownloadStatus")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.keyregisterDownloadStatus = (xpp.getText());
                                System.out.println("keyregisterDownloadStatus =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("latitude")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.latitude = (xpp.getText());
                                System.out.println("latitude =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("loginRequestTime")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.loginRequestTime = (xpp.getText());
                                System.out.println("loginRequestTime =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("logoutTime")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.logoutTime = (xpp.getText());
                                System.out.println("logoutTime =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("longtude")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.longtude = (xpp.getText());
                                System.out.println("longtude =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("minQty")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.minQty = (xpp.getText());
                                System.out.println("minQty =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("partialOnlineOfflineStatus")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.partialOnlineOfflineStatus = (xpp.getText());
                                System.out.println("partialOnlineOfflineStatus =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("responseTimedOutTimeInSec")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.responseTimedOutTimeInSec = (xpp.getText());
                                System.out.println("responseTimedOutTimeInSec =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("routeOffEnable")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.routeOffEnable = (xpp.getText());
                                System.out.println("routeOffEnable =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("versionUpdateRequired")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.versionUpdateRequired = (xpp.getText());
                                System.out.println("versionUpdateRequired =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("wadhValue")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.wadhValue = (xpp.getText());
                                System.out.println("wadhValue =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("weighAccuracyValueInGms")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.weighAccuracyValueInGms = (xpp.getText());
                                System.out.println("weighAccuracyValueInGms =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("weighingStatus")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.weighingStatus = (xpp.getText());
                                System.out.println("weighingStatus =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("eKYCPrompt")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsCommonInfo.eKYCPrompt = (xpp.getText());
                                System.out.println("eKYCPrompt =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("fpsDetails")) {
                            fpsDetails = new fpsDetails();
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("authType")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsDetails.authType = (xpp.getText());
                                System.out.println("authType =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("dealerFusion")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsDetails.dealerFusion = (xpp.getText());
                                System.out.println("dealerFusion =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("dealer_type")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsDetails.dealer_type = (xpp.getText());
                                System.out.println("dealer_type =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("delBfd1")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsDetails.delBfd1 = (xpp.getText());
                                System.out.println("delBfd1 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("delBfd2")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsDetails.delBfd2 = (xpp.getText());
                                System.out.println("delBfd2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("delBfd3")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsDetails.delBfd3 = (xpp.getText());
                                System.out.println("delBfd3 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("delName")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsDetails.delName = (xpp.getText());
                                System.out.println("delName =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("delNamell")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsDetails.delNamell = (xpp.getText());
                                System.out.println("delNamell =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("delUid")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsDetails.delUid = (xpp.getText());

                                System.out.println("delUid =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("wadhStatus")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsDetails.wadhStatus = (xpp.getText());
                                System.out.println("wadhStatus =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("cardEntryLength")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.cardEntryLength = (xpp.getText());
                                System.out.println("cardEntryLength =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("virtualKeyPadType")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.virtualKeyPadType = (xpp.getText());
                                System.out.println("virtualKeyPadType =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("ceritificatePath")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.ceritificatePath = (xpp.getText());
                                System.out.println("ceritificatePath =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("commonCommodityFlag")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.commonCommodityFlag = (xpp.getText());
                                System.out.println("commonCommodityFlag =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("fpsCbDownloadStatus")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.fpsCbDownloadStatus = (xpp.getText());
                                System.out.println("fpsCbDownloadStatus =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("fusionAttempts")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.fusionAttempts = (xpp.getText());
                                System.out.println("fusionAttempts =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("helplineNumber")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.helplineNumber = (xpp.getText());
                                System.out.println("helplineNumber =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("impdsURL")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.impdsURL = (xpp.getText());
                                System.out.println("impdsURL =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("irisStatus")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.irisStatus = (xpp.getText());
                                System.out.println("irisStatus =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("messageEng")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.messageEng = (xpp.getText());
                                System.out.println("messageEng =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("messageLl")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.messageLl = (xpp.getText());
                                System.out.println("messageLl =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("paperRequiredFlag")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.paperRequiredFlag = (xpp.getText());
                                System.out.println("paperRequiredFlag =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("pdsClTranEng")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.pdsClTranEng = (xpp.getText());
                                System.out.println("pdsClTranEng =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("pdsClTranLl")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.pdsClTranLl = (xpp.getText());
                                System.out.println("pdsClTranLl =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("pdsTranEng")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.pdsTranEng = (xpp.getText());
                                System.out.println("pdsTranEng =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("pdsTransLl")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.pdsTransLl = (xpp.getText());
                                System.out.println("pdsTransLl =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("pmsMenuNameEn")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.pmsMenuNameEn = (xpp.getText());
                                System.out.println("pmsMenuNameEn =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("pmsMenuNameLL")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.pmsMenuNameLL = (xpp.getText());
                                System.out.println("pmsMenuNameLL =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("pmsURL")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.pmsURL = (xpp.getText());
                                System.out.println("pmsURL =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("pmsWadh")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.pmsWadh = (xpp.getText());
                                System.out.println("pmsWadh =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("token")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.token = (xpp.getText());
                                System.out.println("token =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("wsdlOffline")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.wsdlOffline = (xpp.getText());
                                System.out.println("wsdlOffline =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("wsdlURLAuth")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.wsdlURLAuth = (xpp.getText());
                                System.out.println("wsdlURLAuth =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("wsdlURLPDS")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.wsdlURLPDS = (xpp.getText());
                                System.out.println("wsdlURLPDS =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("wsdlURLReceive")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                fpsURLInfo.wsdlURLReceive = (xpp.getText());
                                System.out.println("wsdlURLReceive =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("reasonId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                reasonBeanLists.reasonId = (xpp.getText());
                                System.out.println("reasonId =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("reasonValue")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                reasonBeanLists.reasonValue = (xpp.getText());
                                System.out.println("reasonValue =================" + xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                code = (xpp.getText());
                                System.out.println("respCode =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());
                                System.out.println("respCode =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("consentHeader")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                stateBean.consentHeader = (xpp.getText());
                                System.out.println("consentHeader =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("stateCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                stateBean.stateCode = (xpp.getText());
                                System.out.println("stateCode =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("stateNameEn")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                stateBean.stateNameEn = (xpp.getText());
                                System.out.println("stateNameEn =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("stateNameLl")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                stateBean.stateNameLl = (xpp.getText());
                                System.out.println("stateNameLl =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("stateProfile")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                stateBean.stateProfile = (xpp.getText());
                                System.out.println("stateProfile =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("stateReceiptHeaderEn")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                stateBean.stateReceiptHeaderEn = (xpp.getText());
                                System.out.println("stateReceiptHeaderEn =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("stateReceiptHeaderLl")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                stateBean.stateReceiptHeaderLl = (xpp.getText());
                                System.out.println("stateReceiptHeaderLl =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("statefpsId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                stateBean.statefpsId = (xpp.getText());
                                System.out.println("statefpsId =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("idType")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                transactionMode.idType = (xpp.getText());
                                System.out.println("idType =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("idValue")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                transactionMode.idValue = (xpp.getText());
                                System.out.println("idValue =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("opeValue")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                transactionMode.opeValue = (xpp.getText());
                                System.out.println("opeValue =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("oprMode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                transactionMode.oprMode = (xpp.getText());
                                System.out.println("oprMode =================" + xpp.getText());

                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equals("fpsCommonInfo")) {
                        dealer.fpsCommonInfo = fpsCommonInfo;
                    }
                    if (xpp.getName().equals("fpsDetails")) {
                        if (fpsCommonInfo != null) {
                            fpsCommonInfo.fpsDetails.add(fpsDetails);
                        }
                    }
                    if (xpp.getName().equals("fpsURLInfo")) {
                        dealer.fpsURLInfo = fpsURLInfo;
                    }
                    if (xpp.getName().equals("reasonBeanLists")) {
                        dealer.reasonBeanLists.add(reasonBeanLists);
                    }
                    if (xpp.getName().equals("stateBean")) {
                        dealer.stateBean = stateBean;
                    }
                    if (xpp.getName().equals("transactionMode")) {
                        dealer.transactionMode.add(transactionMode);
                    }
                }
                eventType = xpp.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
            code = "1";
            msg = String.valueOf(e);
        } catch (IOException e) {
            e.printStackTrace();
            code = "2";
            msg = String.valueOf(e);
        }
        return dealer;
    }

    private void parseXml_dealer_login(String xmlString) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlString));
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {

                    if (xpp.getName().equals("respCode")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            code = (xpp.getText());
                            System.out.println("Resp CODE *****************************" + xpp.getText());
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());
                                System.out.println("Resp MSG *****************************" + xpp.getText());
                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("transaction_flow")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                flow = (xpp.getText());
                                System.out.println("transaction_flow *****************************" + xpp.getText());
                            }
                        }
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            code = "1";
            msg = String.valueOf(e);
        } catch (IOException e) {
            e.printStackTrace();
            code = "2";
            msg = String.valueOf(e);
        }
    }

    private Member parseXml_member(String xmlString) {
        Member member = new Member();
        carddetails carddetails = null;
        commDetails commDetails = null;
        memberdetails memberdetails = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlString));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("carddetails")) {
                        carddetails = new carddetails();
                    }
                    if (xpp.getName().equals("commDetails")) {
                        commDetails = new commDetails();
                    }
                    if (xpp.getName().equals("memberdetails")) {
                        memberdetails = new memberdetails();
                    }

                    if (xpp.getName().equals("address")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            carddetails.address = (xpp.getText());
                            System.out.println("address 1 =================" + xpp.getText());
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("cardHolderName")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.cardHolderName = (xpp.getText());
                                System.out.println("cardHolderName 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("cardType")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.cardType = (xpp.getText());
                                System.out.println("cardType 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("familyMemCount")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.familyMemCount = (xpp.getText());
                                System.out.println("familyMemCount 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("houseHoldCardNo")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.houseHoldCardNo = (xpp.getText());
                                System.out.println("houseHoldCardNo 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("lpgStatus")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.lpgStatus = (xpp.getText());
                                System.out.println("lpgStatus 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("lpgtype")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.lpgtype = (xpp.getText());
                                System.out.println("lpgtype 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("mobileNoUpdate")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.mobileNoUpdate = (xpp.getText());
                                System.out.println("mobileNoUpdate 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("officeName")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.officeName = (xpp.getText());
                                System.out.println("officeName 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("rcId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.rcId = (xpp.getText());
                                System.out.println("rcId 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("schemeId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.schemeId = (xpp.getText());
                                System.out.println("schemeId 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("surveyMessageEN")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.surveyMessageEN = (xpp.getText());
                                System.out.println("surveyMessageEN 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("surveyMessageLL")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.surveyMessageLL = (xpp.getText());
                                System.out.println("surveyMessageLL 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("surveyMinQuantity")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.surveyMinQuantity = (xpp.getText());
                                System.out.println("surveyMinQuantity 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("surveyStaus")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.surveyStaus = (xpp.getText());
                                System.out.println("surveyStaus 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("type_card")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.type_card = (xpp.getText());
                                System.out.println("type_card 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("zcommboCommCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.zcommboCommCode = (xpp.getText());
                                System.out.println("zcommboCommCode 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("zcommboStatus")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.zcommboStatus = (xpp.getText());
                                System.out.println("zcommboStatus 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("zheadmobileno")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.zheadmobileno = (xpp.getText());
                                System.out.println("zheadmobileno 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("znpnsBalance")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.znpnsBalance = (xpp.getText());
                                System.out.println("znpnsBalance 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("zwadh")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                carddetails.zwadh = (xpp.getText());
                                System.out.println("zwadh 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("allocationType")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                commDetails.allocationType = (xpp.getText());
                                System.out.println("allocationType 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("allotedMonth")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                commDetails.allotedMonth = (xpp.getText());
                                System.out.println("allotedMonth 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("allotedYear")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                commDetails.allotedYear = (xpp.getText());
                                System.out.println("allotedYear 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("availedQty")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                commDetails.availedQty = (xpp.getText());
                                System.out.println("availedQty 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("balQty")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                commDetails.balQty = (xpp.getText());
                                System.out.println("balQty 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("closingBal")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                commDetails.closingBal = (xpp.getText());
                                System.out.println("closingBal 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("commName")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                commDetails.commName = (xpp.getText());
                                System.out.println("commName 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("commNamell")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                commDetails.commNamell = (xpp.getText());
                                System.out.println("commNamell 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("commcode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                commDetails.commcode = (xpp.getText());
                                System.out.println("commcode 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("measureUnit")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                commDetails.measureUnit = (xpp.getText());
                                System.out.println("measureUnit 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("minQty")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                commDetails.minQty = (xpp.getText());
                                System.out.println("minQty 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("price")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                commDetails.price = (xpp.getText());
                                System.out.println("price 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("requiredQty")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                commDetails.requiredQty = (xpp.getText());
                                System.out.println("requiredQty 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("totQty")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                commDetails.totQty = (xpp.getText());
                                System.out.println("totQty 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("weighing")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                commDetails.weighing = (xpp.getText());
                                System.out.println("weighing 2 =================" + xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("bfd_1")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                memberdetails.bfd_1 = (xpp.getText());
                                System.out.println("bfd_1 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("bfd_2")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                memberdetails.bfd_2 = (xpp.getText());
                                System.out.println("bfd_2 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("bfd_3")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                memberdetails.bfd_3 = (xpp.getText());
                                System.out.println("bfd_3 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("memberName")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                memberdetails.memberName = (xpp.getText());
                                System.out.println("memberName 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("memberNamell")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                memberdetails.memberNamell = (xpp.getText());
                                System.out.println("memberNamell 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("member_fusion")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                memberdetails.member_fusion = (xpp.getText());
                                System.out.println("member_fusion 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("uid")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                memberdetails.uid = (xpp.getText());
                                System.out.println("uid 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("w_uid_status")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                memberdetails.w_uid_status = (xpp.getText());
                                System.out.println("w_uid_status 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("xfinger")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                memberdetails.xfinger = (xpp.getText());
                                System.out.println("xfinger 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("yiris")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                memberdetails.yiris = (xpp.getText());
                                System.out.println("yiris 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("zmanual")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                memberdetails.zmanual = (xpp.getText());
                                System.out.println("zmanual 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("zmemberId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                memberdetails.zmemberId = (xpp.getText());
                                System.out.println("zmemberId 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("zotp")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                memberdetails.zotp = (xpp.getText());
                                System.out.println("zotp 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("zwgenWadhAuth")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                memberdetails.zwgenWadhAuth = (xpp.getText());
                                System.out.println("zwgenWadhAuth 2 =================" + xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                code = (xpp.getText());
                                System.out.println("respCode 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());
                                System.out.println("respMessage 2 =================" + xpp.getText());
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equals("carddetails")) {
                        member.carddetails = carddetails;
                    }
                    if (xpp.getName().equals("commDetails")) {
                        member.commDetails.add(commDetails);
                    }
                    if (xpp.getName().equals("memberdetails")) {
                        member.memberdetails.add(memberdetails);
                    }
                }
                eventType = xpp.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
            code = "1";
            msg = String.valueOf(e);
        } catch (IOException e) {
            e.printStackTrace();
            code = "2";
            msg = String.valueOf(e);
        }
        return member;
    }

    private void parseXml_member_login(String xmlString) {

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlString));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {

                    if (xpp.getName().equals("respCode")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            code = (xpp.getText());
                            System.out.println("Resp CODE*****************************" + xpp.getText());
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());
                                System.out.println("Resp MSG*****************************" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("auth_transaction_code")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                ref = xpp.getText();
                                System.out.println("Ref =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("transaction_flow")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                flow = (xpp.getText());
                                System.out.println("transaction_flow*****************************" + xpp.getText());
                            }
                        }
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            code = "1";
            msg = String.valueOf(e);
        } catch (IOException e) {
            msg = String.valueOf(e);
            code = "2";
            msg = String.valueOf(e);
        }
    }

    private Print parseXml_printer(String xmlString) {
        Print print = new Print();
        printBeans printBeans = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlString));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("printBeans")) {
                        printBeans = new printBeans();
                    }
                    if (xpp.getName().equals("bal_qty")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            printBeans.bal_qty = (xpp.getText());
                            System.out.println("Bal Qty 0=================" + xpp.getText());
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("carry_over")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                printBeans.carry_over = (xpp.getText());
                                System.out.println("carry_over 1=================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("commIndividualAmount")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                printBeans.commIndividualAmount = (xpp.getText());
                                System.out.println("commIndividualAmount 1=================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("comm_name")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                printBeans.comm_name = (xpp.getText());
                                System.out.println("comm_name 1=================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("comm_name_ll")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                printBeans.comm_name_ll = (xpp.getText());
                                System.out.println("comm_name_ll 1=================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("member_name")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                printBeans.member_name = (xpp.getText());
                                System.out.println("member_name 1=================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("member_name_ll")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                printBeans.member_name_ll = (xpp.getText());
                                System.out.println("member_name_ll 1=================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("reciept_id")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                printBeans.reciept_id = (xpp.getText());
                                System.out.println("reciept_id 1=================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("retail_price")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                printBeans.retail_price = (xpp.getText());
                                System.out.println("retail_price 1=================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("scheme_desc_en")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                printBeans.scheme_desc_en = (xpp.getText());
                                System.out.println("scheme_desc_en 1=================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("scheme_desc_ll")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                printBeans.scheme_desc_ll = (xpp.getText());
                                System.out.println("scheme_desc_ll 1=================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("tot_amount")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                printBeans.tot_amount = (xpp.getText());
                                System.out.println("tot_amount 1=================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("total_quantity")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                printBeans.total_quantity = (xpp.getText());
                                System.out.println("total_quantity 1=================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("transaction_time")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                printBeans.transaction_time = (xpp.getText());
                                System.out.println("transaction_time 1=================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("uid_refer_no")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                printBeans.uid_refer_no = (xpp.getText());
                                System.out.println("uid_refer_no 1=================" + xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("rcId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                print.rcId = (xpp.getText());
                                System.out.println("rcId 1=================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("receiptId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                print.receiptId = (xpp.getText());
                                System.out.println("receiptId 1=================" + xpp.getText());
                            }
                        }
                    }

                    //-----------------------------------------------------------------

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                code = (xpp.getText());
                                System.out.println("Resp CODE*****************************" + xpp.getText());
                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());
                                System.out.println("Resp MSG*****************************" + xpp.getText());
                            }
                        }
                    }

                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equals("printBeans")) {
                        print.printBeans.add(printBeans);
                    }
                }
                eventType = xpp.next();
            }


        } catch (XmlPullParserException e) {
            e.printStackTrace();
            code = "1";
            msg = String.valueOf(e);
        } catch (IOException e) {
            e.printStackTrace();
            code = "2";
            msg = String.valueOf(e);
        }
        return print;
    }

    private SaleDetails parseXml_sale_details(String xmlString) {
        SaleDetails saleDetails = new SaleDetails();
        drBean drBean = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlString));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("drBean")) {
                        drBean = new drBean();
                        System.out.println("Object created drBean");
                    }

                    if (xpp.getName().equals("commNamell")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            drBean.commNamell = (xpp.getText());
                            System.out.println("commNamell 0 =================" + xpp.getText());
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("comm_name")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                drBean.comm_name = (xpp.getText());
                                System.out.println("comm_name 3 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("sale")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                drBean.sale = (xpp.getText());
                                System.out.println("sale 1 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("schemeName")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                drBean.schemeName = (xpp.getText());
                                System.out.println("schemeName 2 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("total_cards")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                drBean.total_cards = (xpp.getText());
                                System.out.println("total_cards 3 =================" + xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                code = (xpp.getText());
                                System.out.println("Resp CODE*****************************" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());
                                System.out.println("Resp MSG*****************************" + xpp.getText());
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equals("drBean")) {
                        saleDetails.drBean.add(drBean);
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            code = "1";
            msg = String.valueOf(e);
        } catch (IOException e) {
            e.printStackTrace();
            code = "2";
            msg = String.valueOf(e);
        }
        return saleDetails;
    }

    private StockDetails parseXml_stock_details(String xmlString) {
        StockDetails stockDetails = new StockDetails();
        astockBean astockBean = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlString));
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("astockBean")) {
                        astockBean = new astockBean();
                    }
                    if (xpp.getName().equals("allot_qty")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            astockBean.allot_qty = (xpp.getText());
                            System.out.println("allot_qty 0 =================" + xpp.getText());
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("closing_balance")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                astockBean.closing_balance = (xpp.getText());
                                System.out.println("closing_balance 1 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("commNamell")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                astockBean.commNamell = (xpp.getText());
                                System.out.println("commNamell 1 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("comm_name")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                astockBean.comm_name = (xpp.getText());
                                System.out.println("comm_name 1 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("commoditycode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                astockBean.commoditycode = (xpp.getText());
                                System.out.println("commoditycode 1 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("issued_qty")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                astockBean.issued_qty = (xpp.getText());
                                System.out.println("issued_qty 1 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("opening_balance")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                astockBean.opening_balance = (xpp.getText());
                                System.out.println("opening_balance 1 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("received_qty")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                astockBean.received_qty = (xpp.getText());
                                System.out.println("received_qty 1 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("scheme_desc_en")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                astockBean.scheme_desc_en = (xpp.getText());
                                System.out.println("scheme_desc_en 1 =================" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("scheme_desc_ll")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                astockBean.scheme_desc_ll = (xpp.getText());
                                System.out.println("scheme_desc_ll 1 =================" + xpp.getText());
                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("total_quantity")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                astockBean.total_quantity = (xpp.getText());
                                System.out.println("total_quantity 1 =================" + xpp.getText());

                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                code = (xpp.getText());
                                System.out.println("Resp CODE*****************************" + xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());
                                System.out.println("Resp MSG*****************************" + xpp.getText());
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equals("astockBean")) {
                        stockDetails.astockBean.add(astockBean);
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            code = "1";
            msg = String.valueOf(e);
        } catch (IOException e) {
            e.printStackTrace();
            code = "2";
            msg = String.valueOf(e);
        }
        return stockDetails;
    }

    public interface OnResultListener {
        void onCompleted(String error, String msg, String ref, String flow, Object object);
    }


}