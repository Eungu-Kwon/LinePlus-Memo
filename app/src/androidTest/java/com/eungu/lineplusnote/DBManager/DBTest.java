package com.eungu.lineplusnote.DBManager;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DBTest {

    DBManager dbManager;

    // 삭제 혹은 업데이트 테스트를 위해 미리 데이터를 저장해둔다
    @Before
    public void setUp(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dbManager = new DBManager(appContext);

        dbManager.addData(new DBData(Calendar.getInstance(), Calendar.getInstance(),"Title1", "Content1", "imageList"));
        dbManager.addData(new DBData(Calendar.getInstance(), Calendar.getInstance(),"Title2", "Content2", "imageList"));
    }

    // DB 추가 테스트 : DB 숫자가 늘었는지 체크
    @Test
    public void addData1() {
        int countPast = dbManager.getItemsCount();
        dbManager.addData(new DBData(Calendar.getInstance(), Calendar.getInstance(),"Title3", "Content3", "imageList"));
        assertEquals(countPast + 1, dbManager.getItemsCount());
    }

    // DB 추가 테스트 : 추가된 정보의 정확성 테스트
    @Test
    public void addData2(){
        String newDataName = "Title4";
        String newDataContent = "Content4";
        String newDataImageList = "NewImageList";
        int countPast = dbManager.getItemsCount();
        dbManager.addData(new DBData(Calendar.getInstance(), Calendar.getInstance(), newDataName, newDataContent, newDataImageList));
        DBData data = dbManager.getData(dbManager.getItemsCount() - 1);
        assertEquals(newDataName + newDataContent + newDataImageList, data.getTitle() + data.getContent() + data.getImageList());
    }

    // DB 업데이트 테스트 : 같은 index의 정보가 수정되었는지 체크
    @Test
    public void updateData() {
        DBData dbData = dbManager.getData(0);

        String title = dbData.getTitle();
        dbManager.updateData(new DBData(Calendar.getInstance(), Calendar.getInstance(),"UpdatedTitle", "UpdatedContent", "UpdatedImageList"), 0);
        assertTrue(!title.equals(dbManager.getData(0).getTitle()));
    }

    // DB 삭제 테스트 : 지정된 데이터가 지워졌는지, Row 수가 줄었는지 체크
    @Test
    public void deleteRow() {
        int count = dbManager.getItemsCount();
        String title = dbManager.getData(0).getTitle();
        dbManager.deleteRow(0);
        assertTrue((!title.equals(dbManager.getData(0)) && (count == dbManager.getItemsCount() + 1)));
    }

    // 테스트가 끝나면 Table을 비운다
    @After
    public void endUp(){
        int count = dbManager.getItemsCount();
        for(int i = 0; i < count; i++){
            dbManager.deleteRow(0);
        }
        Log.d("imlog", dbManager.getItemsCount() + "");
    }
}