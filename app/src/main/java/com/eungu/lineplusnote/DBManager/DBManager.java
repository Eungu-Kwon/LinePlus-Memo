package com.eungu.lineplusnote.DBManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;

public class DBManager {
    MemoDBHelper dbHelper = null;
    public DBManager(Context context) {
        dbHelper = new MemoDBHelper(context, DBData.MEMO_TABLE, null, 1);
    }

    public void addData(DBData item){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBData.MEMO_TITLE, item.getTitle());
        values.put(DBData.MEMO_CONTENT, item.getContent());
        values.put(DBData.MEMO_DATE, item.getTimeToText());

        db.insert(DBData.MEMO_TABLE, null, values);
        db.close();
    }

    public DBData getData(int id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBData.MEMO_TABLE, null);
        cursor.moveToFirst();

        // When DB doesn't have data : return null
        if(!cursor.move(id)) return null;

        DBData dbData = new DBData(Calendar.getInstance(), cursor.getString(cursor.getColumnIndex(DBData.MEMO_TITLE)), cursor.getString(cursor.getColumnIndex(DBData.MEMO_CONTENT)));
        dbData.setTimeFromText(cursor.getString(cursor.getColumnIndex(DBData.MEMO_DATE)));

        return dbData;
    }

    public int updateData(DBData item, int id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBData.MEMO_TITLE, item.getTitle());
        values.put(DBData.MEMO_CONTENT, item.getContent());
        values.put(DBData.MEMO_DATE, item.getTimeToText());

        int ret = db.update(DBData.MEMO_TABLE, values, "_ID="+(id+1), null);
        db.close();
        return ret;
    }

    // sort index
    public void computeID(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBData.MEMO_TABLE, null);

        if(cursor.moveToFirst() == false) {
            db.close();
            return;
        }
        int idx = 1;
        do{
            db.execSQL("UPDATE ALARM_TABLE SET _ID = " + idx + " WHERE _ID = " + cursor.getInt(cursor.getColumnIndex("_ID")) + ";");
            idx+=1;
        }while (cursor.moveToNext());
        db.close();
    }

    public boolean deleteColumn(long id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int ret = db.delete(DBData.MEMO_TABLE, "_id="+(id+1), null);

        db.close();
        return ret > 0;
    }

    public int getItemsCount() {
        String countQuery = "SELECT  * FROM " + DBData.MEMO_TABLE;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int ret = cursor.getCount();
        cursor.close();

        return ret;
    }
}
