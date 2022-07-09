package com.visiontek.Mantra.Models.UploadingModels;

import java.util.List;

public class DataDownloadAckRequest {
    /*
    * RequestedData["fpsId"] = FpsId;
    RequestedData["stateCode"] = stateCode.toStdString().c_str();
    RequestedData["token"] = RECEIVEGOODS_TOKEN;
    RequestedData["sessionId"] =query.value(0).toString().toAscii().data();
    RequestedData["terminalId"]=cmachineID;
    RequestedData["keyregisterDataDeleteStatus"]=PartialTxnDeleteFlag.toAscii().data();
    RequestedData["dataDownloadStatus"]="Y";
    RequestedData["distributionMonth"]=AllotmentMonth.toAscii().data();
    RequestedData["distributionYear"]=AllotmentYear.toAscii().data();

    RequestedData["fpsOfflineTransResponses"]=ArrayList;
    RequestedData["totalRecords"]=0;
    RequestedData["uploadingRecords"]=0;
    RequestedData["pendingRecords"]=0;*/

    private String fpsId;
    private String stateCode;
    private String token;
    private String sessionId;
    private String terminalId;
    private String keyregisterDataDeleteStatus;
    private String dataDownloadStatus;
    private String distributionMonth;
    private String distributionYear;
    private List<CommWiseData> fpsOfflineTransResponses;
    private int totalRecords;
    private int uploadingRecords;
    private int pendingRecords;

    public String getFpsId() {
        return fpsId;
    }

    public void setFpsId(String fpsId) {
        this.fpsId = fpsId;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getKeyregisterDataDeleteStatus() {
        return keyregisterDataDeleteStatus;
    }

    public void setKeyregisterDataDeleteStatus(String keyregisterDataDeleteStatus) {
        this.keyregisterDataDeleteStatus = keyregisterDataDeleteStatus;
    }

    public String getDataDownloadStatus() {
        return dataDownloadStatus;
    }

    public void setDataDownloadStatus(String dataDownloadStatus) {
        this.dataDownloadStatus = dataDownloadStatus;
    }

    public String getDistributionMonth() {
        return distributionMonth;
    }

    public void setDistributionMonth(String distributionMonth) {
        this.distributionMonth = distributionMonth;
    }

    public String getDistributionYear() {
        return distributionYear;
    }

    public void setDistributionYear(String distributionYear) {
        this.distributionYear = distributionYear;
    }

    public List<CommWiseData> getFpsOfflineTransResponses() {
        return fpsOfflineTransResponses;
    }

    public void setFpsOfflineTransResponses(List<CommWiseData> fpsOfflineTransResponses) {
        this.fpsOfflineTransResponses = fpsOfflineTransResponses;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getUploadingRecords() {
        return uploadingRecords;
    }

    public void setUploadingRecords(int uploadingRecords) {
        this.uploadingRecords = uploadingRecords;
    }

    public int getPendingRecords() {
        return pendingRecords;
    }

    public void setPendingRecords(int pendingRecords) {
        this.pendingRecords = pendingRecords;
    }
}
