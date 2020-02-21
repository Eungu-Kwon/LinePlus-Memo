package com.eungu.lineplusnote.MemoList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eungu.lineplusnote.DBManager.DBData;
import com.eungu.lineplusnote.DBManager.DBManager;
import com.eungu.lineplusnote.ImageCompute;
import com.eungu.lineplusnote.MemoList.MainListMaker.MemoListAdapter;
import com.eungu.lineplusnote.MemoList.MainListMaker.MemoListItem;
import com.eungu.lineplusnote.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static int ADD_REQUEST_CODE = 1;

    ArrayList<MemoListItem> list;
    MemoListAdapter listAdapter;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init_list();
        setList();

        Button b = findViewById(R.id.temp_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), AddMemoActivity.class);
                i.putExtra("ADD", true);
                startActivityForResult(i, ADD_REQUEST_CODE);
            }
        });
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
            item.setDate(new SimpleDateFormat("MM/dd").format(dbData.getTime().getTime()));
            if(imageList.size() > 0) item.setthumbnailPath(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + imageList.get(0) + "_icon");

            list.add(item);
        }

        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ADD_REQUEST_CODE && resultCode == RESULT_OK){
            setList();
        }
    }
}
