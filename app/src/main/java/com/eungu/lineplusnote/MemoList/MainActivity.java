package com.eungu.lineplusnote.MemoList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.eungu.lineplusnote.DBManager.DBData;
import com.eungu.lineplusnote.DBManager.DBManager;
import com.eungu.lineplusnote.MemoList.ListMaker.MemoListAdapter;
import com.eungu.lineplusnote.MemoList.ListMaker.MemoListItem;
import com.eungu.lineplusnote.R;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DBManager dbManager = new DBManager(this);
        ArrayList<MemoListItem> list = new ArrayList<>();
        MemoListAdapter listAdapter = new MemoListAdapter(this, list);

        RecyclerView recyclerView = findViewById(R.id.r_memo_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(listAdapter);
        for(int i = 0; i < dbManager.getItemsCount(); i++){
            MemoListItem item = new MemoListItem();
            DBData dbData = dbManager.getData(i);
            item.setTitle(dbData.getTitle());
            list.add(item);
        }

        Button b = findViewById(R.id.temp_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBData d = new DBData(Calendar.getInstance(), "title", "content");
                dbManager.addData(d);
            }
        });
    }
}
