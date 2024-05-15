package com.example.nearbynest;

public class ListItem {
    private int iconResId;
    private String text;

    public ListItem(int iconResId, String text) {
        this.iconResId = iconResId;
        this.text = text;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getText() {
        return text;
    }
}

