package com.visiontek.Mantra.Utils;


import android.os.Parcel;
import android.os.Parcelable;

public class CommodityTransactionData implements Parcelable {
    String commPDF;
    String unitPDF;
    String pricePDF;
    String totqtyPDF;

    public String getUnitPDF() {
        return unitPDF;
    }

    public void setUnitPDF(String unitPDF) {
        this.unitPDF = unitPDF;
    }

    public String getTotqtyPDF() {
        return totqtyPDF;
    }

    public void setTotqtyPDF(String totqtyPDF) {
        this.totqtyPDF = totqtyPDF;
    }

    public CommodityTransactionData(String commPDF, String unitPDF, String pricePDF, String totqtyPDF) {
        this.commPDF = commPDF;
        this.unitPDF = unitPDF;
        this.pricePDF = pricePDF;
        this.totqtyPDF = totqtyPDF;
    }

    protected CommodityTransactionData(Parcel in) {
        commPDF = in.readString();
        unitPDF = in.readString();
        pricePDF = in.readString();
        totqtyPDF = in.readString();
    }

    public static final Creator<CommodityTransactionData> CREATOR = new Creator<CommodityTransactionData>() {
        @Override
        public CommodityTransactionData createFromParcel(Parcel in) {
            return new CommodityTransactionData(in);
        }

        @Override
        public CommodityTransactionData[] newArray(int size) {
            return new CommodityTransactionData[size];
        }
    };

    public String getCommPDF() {
        return commPDF;
    }

    public void setCommPDF(String commPDF) {
        this.commPDF = commPDF;
    }


    public String getPricePDF() {
        return pricePDF;
    }

    public void setPricePDF(String pricePDF) {
        this.pricePDF = pricePDF;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(commPDF);
        dest.writeString(unitPDF);
        dest.writeString(pricePDF);
        dest.writeString(totqtyPDF);
    }
}
