package com.berry.second.secondprojectclient.gallery;

import android.graphics.Bitmap;

/**
 * Created by q on 2016-06-29.
 */
public class ImageItem {
    private Bitmap image;
    private String title;
    private int position;

    public ImageItem(Bitmap image, String title, int position) {
        super();
        this.image = image;
        this.title = title;
        this.position = position;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
