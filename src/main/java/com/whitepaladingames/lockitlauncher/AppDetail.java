package com.whitepaladingames.lockitlauncher;

import android.graphics.drawable.Drawable;

public class AppDetail implements Comparable<AppDetail> {
    String label;
    String name;
    Drawable icon;
    String type;
    Boolean added = false;
    String launchActivity;

    public int compareTo(AppDetail another) {
        if (another == null) return -1;
        return label.compareToIgnoreCase(another.label);
    }

    public AppDetail copy() {
        AppDetail result = new AppDetail();
        result.label = this.label;
        result.name = this.name;
        result.icon = this.icon;
        result.type = this.type;
        result.added = this.added;
        result.launchActivity = this.launchActivity;
        return result;
    }
}
