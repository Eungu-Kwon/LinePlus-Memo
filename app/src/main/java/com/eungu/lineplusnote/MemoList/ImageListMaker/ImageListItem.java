package com.eungu.lineplusnote.MemoList.ImageListMaker;

import android.graphics.Bitmap;

public class ImageListItem {
    Bitmap image;
    String _id;

    public ImageListItem() {
    }

    public ImageListItem(Bitmap image, String _id) {
        this.image = image;
        this._id = _id;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }
}
