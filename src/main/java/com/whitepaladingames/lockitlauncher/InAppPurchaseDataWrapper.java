package com.whitepaladingames.lockitlauncher;

import java.io.Serializable;
import java.util.ArrayList;

public class InAppPurchaseDataWrapper implements Serializable {

    private ArrayList<LockItInAppPurchase> lockItInAppPurchases;

    public InAppPurchaseDataWrapper(ArrayList<LockItInAppPurchase> data) {
        this.lockItInAppPurchases = data;
    }

    public ArrayList<LockItInAppPurchase> getLockItInAppPurchases() {
        return this.lockItInAppPurchases;
    }

}
