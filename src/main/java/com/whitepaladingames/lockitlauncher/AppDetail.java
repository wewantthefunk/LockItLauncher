package com.whitepaladingames.lockitlauncher;

/**
 * Created by Chris-laptop on 9/29/2015.
 */
import android.graphics.drawable.Drawable;

public class AppDetail implements Comparable<AppDetail> {
    String label;
    String name;
    Drawable icon;
    String type;

    public int compareTo(AppDetail two) {
        return label.toString().compareToIgnoreCase(two.label.toString());
    }
}
