package com.visiontek.Mantra.Utils;


import android.os.Parcel;
import android.os.Parcelable;

public class DataIssuedCommodity implements Parcelable {
    String Commodity;
    String totalQty;
    String price;
    String commCode;
    String cb;
    String availed_qty;
    String weighStatus;

    public String getWeighStatus() {
        return weighStatus;
    }

    public void setWeighStatus(String weighStatus) {
        this.weighStatus = weighStatus;
    }

    public String getAvailed_qty() {
        return availed_qty;
    }

    public void setAvailed_qty(String availed_qty) {
        this.availed_qty = availed_qty;
    }

    public String getCb() {
        return cb;
    }

    public void setCb(String cb) {
        this.cb = cb;
    }

    public DataIssuedCommodity(String commodity, String totalQty, String price, String commCode, String cb, String availed_qty,
                               String weighStatus) {
        Commodity = commodity;
        this.totalQty = totalQty;
        this.price = price;
        this.commCode=commCode;
        this.cb=cb;
        this.availed_qty=availed_qty;
        this.weighStatus=weighStatus;
    }

    protected DataIssuedCommodity(Parcel in) {
        Commodity = in.readString();
        totalQty = in.readString();
        commCode = in.readString();
        price = in.readString();
        cb = in.readString();
        availed_qty=in.readString();
        weighStatus=in.readString();
    }

    public static final Creator<DataIssuedCommodity> CREATOR = new Creator<DataIssuedCommodity>() {
        @Override
        public DataIssuedCommodity createFromParcel(Parcel in) {
            return new DataIssuedCommodity(in);
        }

        @Override
        public DataIssuedCommodity[] newArray(int size) {
            return new DataIssuedCommodity[size];
        }
    };

    public String getCommodity() {
        return Commodity;
    }

    public void setCommodity(String commodity) {
        Commodity = commodity;
    }

    public String getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(String totalQty) {
        this.totalQty = totalQty;
    }


    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }


    public String getCommCode() {
        return commCode;
    }

    public void setCommCode(String commCode) {
        this.commCode = commCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Commodity);
        dest.writeString(totalQty);
        dest.writeString(cb);
        dest.writeString(price);
        dest.writeString(commCode);
        dest.writeString(availed_qty);
        dest.writeString(weighStatus);
    }

}
