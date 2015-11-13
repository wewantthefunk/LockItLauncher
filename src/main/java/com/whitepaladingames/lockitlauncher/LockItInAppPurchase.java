package com.whitepaladingames.lockitlauncher;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class LockItInAppPurchase implements Parcelable, Serializable {
    public String name;
    public String price;
    public String currency;
    public String description;
    public String productId;
    public String type;
    public int purchased;

    public LockItInAppPurchase() {
        name = AppConstants.EMPTY_STRING;
        price = AppConstants.EMPTY_STRING;
        currency = AppConstants.EMPTY_STRING;
        description = AppConstants.EMPTY_STRING;
        productId = AppConstants.EMPTY_STRING;
        type = AppConstants.EMPTY_STRING;
        purchased = 0;
    }

    public LockItInAppPurchase(Parcel in) {
        name = in.readString();
        price = in.readString();
        currency = in.readString();
        description = in.readString();
        productId = in.readString();
        type = in.readString();
        purchased = in.readInt();
    }

    public static final Parcelable.Creator<LockItInAppPurchase> CREATOR = new Parcelable.Creator<LockItInAppPurchase>()
    {
        public LockItInAppPurchase createFromParcel(Parcel in) {
            return new LockItInAppPurchase(in);
        }

        public LockItInAppPurchase[] newArray(int size) {
            return new LockItInAppPurchase[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(price);
        dest.writeString(currency);
        dest.writeString(description);
        dest.writeString(productId);
        dest.writeString(type);
        dest.writeInt(purchased);
    }

    private int convertBoolToInt(boolean b) {
        if (b) return 1;
        return 0;
    }

    private boolean convertIntToBool(int i) {
        if (i == 0) return false;
        return true;
    }
}
