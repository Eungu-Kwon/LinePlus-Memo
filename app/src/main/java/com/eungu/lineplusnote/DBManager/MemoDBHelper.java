package com.eungu.lineplusnote.DBManager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class MemoDBHelper extends SQLiteOpenHelper {

    public MemoDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DBData.MEMO_TABLE + "(_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBData.MEMO_TITLE + " TEXT, " + DBData.MEMO_CONTENT + " TEXT, " + DBData.MEMO_DATE + " TEXT, " + DBData.LAST_MEMO_DATE + " TEXT, " + DBData.MEMO_IMAGES + " TEXT )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ALARM_TABLE");
        onCreate(db);
    }
}
