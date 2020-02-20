package com.eungu.lineplusnote.MemoList;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eungu.lineplusnote.DBManager.DBData;
import com.eungu.lineplusnote.DBManager.DBManager;
import com.eungu.lineplusnote.MemoList.MainListMaker.MemoListAdapter;
import com.eungu.lineplusnote.MemoList.MainListMaker.MemoListItem;
import com.eungu.lineplusnote.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static int ADD_REQUEST_CODE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 98;

    ArrayList<MemoListItem> list;
    MemoListAdapter listAdapter;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder oDialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog);
                oDialog.setTitle("권한 요청")
                        .setMessage("메모를 내부저장소에 저장/읽기 위해 접근 권한이 필요합니다.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                            }
                        })
                        .setCancelable(false)
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }

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
            item.setTitle(dbData.getTitle());
            item.setContent(dbData.getContent());
            list.add(item);
        }

        listAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ADD_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                setList();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(getApplicationContext(), "권한이 없어 앱을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
}
