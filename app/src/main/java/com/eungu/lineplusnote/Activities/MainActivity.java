package com.eungu.lineplusnote.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eungu.lineplusnote.DBManager.DBData;
import com.eungu.lineplusnote.DBManager.DBManager;
import com.eungu.lineplusnote.MemoList.MainListMaker.MemoListAdapter;
import com.eungu.lineplusnote.MemoList.MainListMaker.MemoListItem;
import com.eungu.lineplusnote.R;
import com.eungu.lineplusnote.StaticMethod.ImageCompute;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    public static int ADD_REQUEST_CODE = 1;

    ArrayList<MemoListItem> list;
    MemoListAdapter listAdapter;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().setElevation(0f);
        getSupportActionBar().setTitle("라인 메모");

        init_list();
        setList();

    }

    private void init_list(){
        list = new ArrayList<>();
        listAdapter = new MemoListAdapter(this, list);

        recyclerView = findViewById(R.id.r_memo_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(listAdapter);
    }

    private void setList(){
        final DBManager dbManager = new DBManager(this);
        list.clear();
        for(int i = 0; i < dbManager.getItemsCount(); i++){
            MemoListItem item = new MemoListItem();
            DBData dbData = dbManager.getData(i);

            ArrayList<String> imageList = ImageCompute.imageListStringToArray(dbData.getImageList());

            item.setTitle(dbData.getTitle());
            item.setContent(dbData.getContent());

            if(isMemoDateIsToday(dbData.getTime())){
                item.setDate(new SimpleDateFormat("HH:mm").format(dbData.getTime().getTime()));
            }
            else{
                item.setDate(new SimpleDateFormat("MM/dd").format(dbData.getTime().getTime()));
            }

            if(imageList.size() > 0) item.setthumbnailPath(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + imageList.get(0) + "_icon");

            list.add(item);
        }

        listAdapter.notifyDataSetChanged();
    }

    private boolean isMemoDateIsToday(Calendar memoDate){
        Calendar today = Calendar.getInstance();
        if(today.get(Calendar.YEAR) == memoDate.get(Calendar.YEAR) && today.get(Calendar.DAY_OF_YEAR) == memoDate.get(Calendar.DAY_OF_YEAR)){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ADD_REQUEST_CODE && resultCode == RESULT_OK){
            setList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_add_memo:
                Intent i = new Intent(getApplicationContext(), AddMemoActivity.class);
                i.putExtra("ADD", true);
                startActivityForResult(i, ADD_REQUEST_CODE);
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }
}
