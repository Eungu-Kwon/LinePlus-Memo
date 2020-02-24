package com.eungu.lineplusnote.DBManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MemoDBHelper extends SQLiteOpenHelper {

    public MemoDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DBData.MEMO_TABLE + "(_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBData.MEMO_TITLE + " TEXT, " + DBData.MEMO_CONTENT + " TEXT, " + DBData.MEMO_DATE + " TEXT, " + DBData.LAST_MEMO_DATE + " TEXT, " + DBData.MEMO_IMAGES + " TEXT )");

        // 설명서 저장
        String startContent = "메모 어플 기능에 대해 설명드리겠습니다.\n\n\n첫번째로 메모 추가입니다.\n\n메인 화면에서 추가 메뉴를 선택하면 메모를 새로 만들 수 있습니다.\n" +
                "메모는 제목과 내용이 필수로 입력되어야하며 사진은 넣어도 되고, 넣지않아도 됩니다.\n\n" +
                "사진은 카메라, 갤러리, 외부 URL을 통해 이미지를 저장할 수 있습니다.\n" +
                "추가한 사진은 메모를 수정중엔 언제든 삭제할 수 있습니다.\n\n" +
                "제거 메뉴를 이용해 언제든 메모를 삭제할 수도 있습니다.\n" +
                "뒤로가기 버튼을 눌러도 저장할지 물어보기 때문에 걱정하지 마세요.\n\n" +
                "다음으로 메뉴 확인입니다.\n\n" +
                "메뉴를 저장하면 언제든 다시 볼 수 있습니다.\n" +
                "메뉴 상세보기 상태에서 이미지를 터치하면 크게 볼 수 있습니다.\n" +
                "상세보기 중에 수정버튼을 누르면 다시 수정할 수 있습니다.\n\n" +
                "이미지를 저장한 후에는 메인화면에서 썸네일을 확인할 수 있습니다.\n\n\n\n";
        SimpleDateFormat format = new SimpleDateFormat(DBData.DATE_PAT);

        ContentValues values = new ContentValues();
        values.put(DBData.MEMO_TITLE, "안녕하세요");
        values.put(DBData.MEMO_CONTENT, startContent);
        values.put(DBData.MEMO_DATE, format.format(Calendar.getInstance().getTime()));
        values.put(DBData.LAST_MEMO_DATE, format.format(Calendar.getInstance().getTime()));
        values.put(DBData.MEMO_IMAGES, "");

        db.insert(DBData.MEMO_TABLE, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DBData.MEMO_TABLE);
        onCreate(db);
    }
}
