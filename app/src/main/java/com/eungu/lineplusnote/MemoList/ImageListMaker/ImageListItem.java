package com.eungu.lineplusnote.MemoList.ImageListMaker;

import android.graphics.Bitmap;

public class ImageListItem {
    Bitmap image;
    String name;

    public ImageListItem() {
    }

    public ImageListItem(Bitmap image, String path) {
        this.image = image;
        this.name = path;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
