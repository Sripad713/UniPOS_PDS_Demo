package com.visiontek.Mantra.Utils;


import android.os.Parcel;
import android.os.Parcelable;

public class AvailedCommodity implements Parcelable {

    String commCode;
    String rerqQty;
    String commAmt;
    String price;

    public AvailedCommodity(String commCode, String rerqQty, String commAmt, String price) {
        this.commCode = commCode;
        this.rerqQty = rerqQty;
        this.commAmt = commAmt;
        this.price = price;
    }

    protected AvailedCommodity(Parcel in) {
        commCode = in.readString();
        rerqQty = in.readString();
        commAmt = in.readString();
        price = in.readString();
    }

    public static final Creator<AvailedCommodity> CREATOR = new Creator<AvailedCommodity>() {
        @Override
        public AvailedCommodity createFromParcel(Parcel in) {
            return new AvailedCommodity(in);
        }

        @Override
        public AvailedCommodity[] newArray(int size) {
            return new AvailedCommodity[size];
        }
    };

    public String getCommCode() {
        return commCode;
    }

    public void setCommCode(String commCode) {
        this.commCode = commCode;
    }

    public String getRerqQty() {
        return rerqQty;
    }

    public void setRerqQty(String rerqQty) {
        this.rerqQty = rerqQty;
    }

    public String getCommAmt() {
        return commAmt;
    }

    public void setCommAmt(String commAmt) {
        this.commAmt = commAmt;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(commCode);
        dest.writeString(rerqQty);
        dest.writeString(commAmt);
        dest.writeString(price);
    }
}
