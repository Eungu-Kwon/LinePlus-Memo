package com.eungu.lineplusnote.DBManager;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DBData implements Serializable {
    public static final String MEMO_TABLE = "MEMO_TABLE";
    public static final String MEMO_TITLE = "MEMO_TITLE";
    public static final String MEMO_CONTENT = "MEMO_CONTENT";
    public static final String MEMO_DATE = "MEMO_DATE";
    public static final String MEMO_IMAGES = "MEMO_IMAGES";

    private final static String DATE_PAT = "yyyy-MM-dd HH:mm:ss";

    Calendar time;
    String title, content;
    String ImageList;

    public DBData() {
        time = Calendar.getInstance();
    }

    public DBData(Calendar _time, String title, String _content, String ImageList) {
        this.time = _time;
        this.title = title;
        this.content = _content;
        this.ImageList = ImageList;
    }

    public void updateData(Calendar _time, String title, String _content, String ImageList) {
        this.time = _time;
        this.title = title;
        this.content = _content;
        this.ImageList = ImageList;
    }

    public String getTimeToText(){
        SimpleDateFormat format = new SimpleDateFormat(DATE_PAT);
        String ret = format.format(time.getTime());
        return ret;
    }

    public int setTimeFromText(String str){
        SimpleDateFormat format = new SimpleDateFormat(DATE_PAT);
        Date dateTime = null;
        try {
            dateTime = format.parse(str);
        } catch (ParseException e) {
            return -1;
        }
        this.time.setTime(dateTime);
        return 0;
    }

    public Calendar getTime() {
        return time;
    }

    public void setTime(Calendar time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageList() {
        return ImageList;
    }

    public void setImageList(String imageList) {
        ImageList = imageList;
    }
}
