package com.eungu.lineplusnote.DBManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;

// 제작 과정에서 편의를 위한 DBManager
// DB의 추가, 제거, 업데이트를 수행
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
        values.put(DBData.LAST_MEMO_DATE, item.getLastTimeToText());
        values.put(DBData.MEMO_IMAGES, item.getImageList());

        db.insert(DBData.MEMO_TABLE, null, values);
        computeID(db);
        db.close();
    }

    public DBData getData(int id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBData.MEMO_TABLE, null);
        cursor.moveToFirst();

        // When DB doesn't have data : return null
        if(!cursor.move(id)) return null;

        DBData dbData = new DBData(Calendar.getInstance(), Calendar.getInstance(), cursor.getString(cursor.getColumnIndex(DBData.MEMO_TITLE)), cursor.getString(cursor.getColumnIndex(DBData.MEMO_CONTENT)), cursor.getString(cursor.getColumnIndex(DBData.MEMO_IMAGES)));
        dbData.setTimeFromText(cursor.getString(cursor.getColumnIndex(DBData.MEMO_DATE)));
        dbData.setLastTimeFromText(cursor.getString(cursor.getColumnIndex(DBData.LAST_MEMO_DATE)));
        db.close();

        return dbData;
    }

    public int updateData(DBData item, int id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBData.MEMO_TITLE, item.getTitle());
        values.put(DBData.MEMO_CONTENT, item.getContent());
        values.put(DBData.MEMO_DATE, item.getTimeToText());
        values.put(DBData.LAST_MEMO_DATE, item.getLastTimeToText());
        values.put(DBData.MEMO_IMAGES, item.getImageList());

        int ret = db.update(DBData.MEMO_TABLE, values, "_ID="+(id+1), null);
        computeID(db);
        db.close();
        return ret;
    }

    // DB데이터의 중간 Row를 제거됐을때 _id 값을 갱신
    public void computeID(SQLiteDatabase db){
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBData.MEMO_TABLE, null);

        if(cursor.moveToFirst() == false) {
            db.close();
            return;
        }
        int idx = 1;
        do{
            db.execSQL("UPDATE " + DBData.MEMO_TABLE + " SET _ID = " + idx + " WHERE _ID = " + cursor.getInt(cursor.getColumnIndex("_ID")) + ";");
            idx+=1;
        }while (cursor.moveToNext());
        db.close();
    }

    public boolean deleteRow(long id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int ret = db.delete(DBData.MEMO_TABLE, "_id="+(id+1), null);
        computeID(db);
        db.close();
        return ret > 0;
    }

    public int getItemsCount() {
        String countQuery = "SELECT * FROM " + DBData.MEMO_TABLE;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int ret = cursor.getCount();
        cursor.close();
        db.close();

        return ret;
    }
}
