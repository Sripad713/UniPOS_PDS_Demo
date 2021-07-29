package com.visiontek.Mantra.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.visiontek.Mantra.Models.AadhaarServicesModel.BeneficiaryVerification.GetURLDetails.BeneficiaryAuth;
import com.visiontek.Mantra.Models.AadhaarServicesModel.BeneficiaryVerification.GetURLDetails.BeneficiaryDetails;
import com.visiontek.Mantra.Models.AadhaarServicesModel.BeneficiaryVerification.GetURLDetails.rcMemberDetVerify;
import com.visiontek.Mantra.Models.AadhaarServicesModel.UIDSeeding.GetURLDetails.UIDAuth;
import com.visiontek.Mantra.Models.AadhaarServicesModel.UIDSeeding.GetURLDetails.UIDDetails;
import com.visiontek.Mantra.Models.AadhaarServicesModel.UIDSeeding.GetURLDetails.rcMemberDet;
import com.visiontek.Mantra.Models.InspectionModel.InspectionAuth;
import com.visiontek.Mantra.Models.InspectionModel.InspectionDetails;
import com.visiontek.Mantra.Models.InspectionModel.InspectioncommDetails;
import com.visiontek.Mantra.Models.InspectionModel.approvals;

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

public class Aadhaar_Parsing extends AsyncTask<String, Void, Void> {
    private final String xmlformat;
    private final int type;
    @SuppressLint("StaticFieldLeak")
    private final Context context;

    private String code;
    private HttpURLConnection urlConnection;
    private OnResultListener onResultListener;
    private String msg;
    private String ref;
    String flow;
    Object object;

    public Aadhaar_Parsing(Context context, String xmlformat, int type) {
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
        runRequest(url);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (onResultListener != null) {
            onResultListener.onCompleted(code, msg, ref, flow, object);
        }
    }

    private void runRequest(String url) {

        try {

            URL Url = new URL(url);
            urlConnection = (HttpURLConnection) Url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "text/xml");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.write(xmlformat.getBytes());
            outputStream.flush();
            outputStream.close();
            urlConnection.connect();
            Log.e(getClass().getName(), String.valueOf(urlConnection.getResponseCode()));
            String result = null;
            if (urlConnection.getResponseCode() == 200) {
                BufferedInputStream bis = new BufferedInputStream(urlConnection.getInputStream());
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                int result2 = bis.read();
                while (result2 != -1) {
                    buf.write((byte) result2);
                    result2 = bis.read();
                }
                result = buf.toString();
            }
            if (result != null && result.length() > 0) {

                if (type == 1) {
                    //Util.generateNoteOnSD(context, "UIDSeedingRes.txt", result);
                    object = parseXml_UIDSEEDING(result);
                } else if (type == 2) {
                    //Util.generateNoteOnSD(context, "UIDAuthRes.txt", result);
                    object = parseXml_UIDAuth(result);
                } else if (type == 3) {
                    //Util.generateNoteOnSD(context, "BenVerificationRes.txt", result);
                    object = parseXml_BENVERIFICATION(result);
                } else if (type == 4) {
                    //Util.generateNoteOnSD(context, "BenVerificationAuthRes.txt", result);
                    object = parseXml_BENVERIFICATIONLOGIN(result);
                } else if (type == 5) {
                    //Util.generateNoteOnSD(context, "InspectionDetailsRes.txt", result);
                    object = parseXml_INSPECTION(result);
                } else if (type == 6) {
                    //Util.generateNoteOnSD(context, "InspectionAuthRes.txt", result);
                    object = parseXml_INSPECTION_AUTH(result);
                } else if (type == 7) {
                    //Util.generateNoteOnSD(context, "InspectionPushRes.txt", result);
                    parseXml_INSPECTION_PUSH(result);
                } else if (type == 8) {
                   /* parseXml_INSPECTION(result);
                    Util.generateNoteOnSD(context, "InspectionPushRes.txt", result);*/
                } else if (type == 9) {
                    //Util.generateNoteOnSD(context, "StockUploadDetailsRes.txt", result);
                    parseXml_INSPECTION_PUSH(result);
                } else if (type == 10) {
                    //Util.generateNoteOnSD(context, "LogoutRes.txt", result);
                    parseXml_Logout(result);
                } else {
                    //Util.generateNoteOnSD(context, "ERRORR.txt", result);
                }
            } else {
                code = "error";
                msg = "PARSING Error";
            }
        } catch (Exception e) {
            e.printStackTrace();
            code = "1";
            msg = String.valueOf(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private void parseXml_Logout(String result) {
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

                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());

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

    private UIDAuth parseXml_UIDAuth(String result) {
        UIDAuth uidAuth = new UIDAuth();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(result));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("eKYCMemberName")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            uidAuth.eKYCMemberName = (xpp.getText());

                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("eKYCMemberFatherName")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                uidAuth.eKYCMemberFatherName = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("eKYCDOB")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                uidAuth.eKYCDOB = (xpp.getText());

                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("eKYCPindCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                uidAuth.eKYCPindCode = (xpp.getText());

                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("eKYCGeneder")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                uidAuth.eKYCGeneder = (xpp.getText());

                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("location")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                uidAuth.location = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("village")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                uidAuth.village = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("dist")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                uidAuth.dist = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("state")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                uidAuth.state = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("pincode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                uidAuth.pincode = (xpp.getText());

                            }
                        }
                    }


                    //-------------------------------------------------------------------------------------------
                    //---------------------------------------------------------------------------------------------

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                code = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());

                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("transaction_flow")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                flow = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("zdistrTxnId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                ref = (xpp.getText());

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
        return uidAuth;
    }

    private void parseXml_INSPECTION_PUSH(String result) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(result));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("receiptId")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            ref = (xpp.getText());

                        }
                    }


                    //-------------------------------------------------------------------------------------------
                    //---------------------------------------------------------------------------------------------

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                code = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());

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

    private BeneficiaryAuth parseXml_BENVERIFICATIONLOGIN(String result) {
        BeneficiaryAuth beneficiaryAuth = new BeneficiaryAuth();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(result));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("eKYCMemberName")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            beneficiaryAuth.eKYCMemberName = (xpp.getText());

                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("eKYCMemberFatherName")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                beneficiaryAuth.eKYCMemberFatherName = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("eKYCDOB")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                beneficiaryAuth.eKYCDOB = (xpp.getText());

                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("eKYCPindCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                beneficiaryAuth.eKYCPindCode = (xpp.getText());

                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("eKYCGeneder")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                beneficiaryAuth.eKYCGeneder = (xpp.getText());

                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("location")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                beneficiaryAuth.location = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("village")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                beneficiaryAuth.village = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("dist")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                beneficiaryAuth.dist = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("state")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                beneficiaryAuth.state = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("pincode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                beneficiaryAuth.pincode = (xpp.getText());

                            }
                        }
                    }

                    //-------------------------------------------------------------------------------------------
                    //---------------------------------------------------------------------------------------------

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                code = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());

                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("transaction_flow")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                flow = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("zdistrTxnId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                ref = (xpp.getText());

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
            code = "1";
            msg = String.valueOf(e);
        }
        return beneficiaryAuth;
    }

    private InspectionAuth parseXml_INSPECTION_AUTH(String result) {
        InspectionAuth inspectionAuth = new InspectionAuth();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(result));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("auth_transaction_code")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            inspectionAuth.auth_transaction_code = (xpp.getText());

                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("inspectorDesignation")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                inspectionAuth.inspectorDesignation = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("inspectorName")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                inspectionAuth.inspectorName = (xpp.getText());

                            }
                        }
                    }

                    //-------------------------------------------------------------------------------------------
                    //---------------------------------------------------------------------------------------------

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                code = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());

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
            code = "";
            msg = String.valueOf(e);
        }
        return inspectionAuth;
    }

    private UIDDetails parseXml_UIDSEEDING(String xmlString) {
        UIDDetails uidDetails = new UIDDetails();
        rcMemberDet rcMemberDet = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlString));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("bfd_1")) {
                        rcMemberDet = new rcMemberDet();
                    }
                    if (xpp.getName().equals("bfd_1")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            rcMemberDet.bfd_1 = (xpp.getText());

                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("bfd_2")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDet.bfd_2 = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("bfd_3")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDet.bfd_3 = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("memberId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDet.memberId = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("memberName")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDet.memberName = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("memberNamell")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDet.memberNamell = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("member_fusion")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDet.member_fusion = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("uid")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDet.uid = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("w_uid_status")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDet.w_uid_status = (xpp.getText());

                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                code = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("zwadh")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                uidDetails.zwadh = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("rationCardId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                uidDetails.rationCardId = (xpp.getText());

                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equals("bfd_1")) {
                        uidDetails.rcMemberDet.add(rcMemberDet);
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
        return uidDetails;
    }

    private InspectionDetails parseXml_INSPECTION(String xmlString) {
        InspectionDetails inspectionDetails = new InspectionDetails();
        approvals approvals = null;
        InspectioncommDetails inspectioncommDetails = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlString));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("approvals")) {
                        approvals = new approvals();
                    }
                    if (xpp.getName().equals("commDetails")) {
                        inspectioncommDetails = new InspectioncommDetails();
                    }
                    if (xpp.getName().equals("closingBalance")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            inspectioncommDetails.closingBalance = (xpp.getText());

                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("commCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                inspectioncommDetails.commCode = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("commNameEn")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                inspectioncommDetails.commNameEn = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("commNamell")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                inspectioncommDetails.commNamell = (xpp.getText());


                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("approveKey")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                approvals.approveKey = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("approveValue")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                approvals.approveValue = (xpp.getText());


                            }
                        }
                    }
                    //-------------------------------------------------------------------------------------------
                    //---------------------------------------------------------------------------------------------

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                code = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());

                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equals("approvals")) {
                        inspectionDetails.approvals.add(approvals);
                    }
                    if (xpp.getName().equals("commDetails")) {
                        inspectionDetails.commDetails.add(inspectioncommDetails);
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
            code = "1";
            msg = String.valueOf(e);
        }
        return inspectionDetails;
    }

    private BeneficiaryDetails parseXml_BENVERIFICATION(String xmlString) {
        BeneficiaryDetails beneficiaryDetails = new BeneficiaryDetails();
        rcMemberDetVerify rcMemberDetVerify = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlString));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("rcMemberDetVerify")) {
                        rcMemberDetVerify = new rcMemberDetVerify();
                    }
                    if (xpp.getName().equals("memberId")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            rcMemberDetVerify.memberId = (xpp.getText());

                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("memberName")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDetVerify.memberName = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("memberNamell")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDetVerify.memberNamell = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("member_fusion")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDetVerify.member_fusion = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("uid")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDetVerify.uid = (xpp.getText());

                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("verification")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDetVerify.verification = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("verifyStatus_en")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDetVerify.verifyStatus_en = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("verifyStatus_ll")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDetVerify.verifyStatus_ll = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("w_uid_status")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rcMemberDetVerify.w_uid_status = (xpp.getText());


                            }
                        }
                    }
                    //-------------------------------------------------------------------------------------------
                    //---------------------------------------------------------------------------------------------

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respCode")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                code = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("respMessage")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                msg = (xpp.getText());

                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("wadh")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                beneficiaryDetails.wadh = (xpp.getText());

                            }
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("rationCardId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                beneficiaryDetails.rationCardId = (xpp.getText());
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equals("rcMemberDetVerify")) {
                        beneficiaryDetails.rcMemberDetVerify.add(rcMemberDetVerify);
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
        return beneficiaryDetails;
    }

    public interface OnResultListener {
        void onCompleted(String error, String msg, String ref, String flow, Object object);
    }
}
