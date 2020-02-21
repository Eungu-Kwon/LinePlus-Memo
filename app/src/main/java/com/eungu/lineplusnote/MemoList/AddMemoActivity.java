package com.eungu.lineplusnote.MemoList;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eungu.lineplusnote.DBManager.DBData;
import com.eungu.lineplusnote.DBManager.DBManager;
import com.eungu.lineplusnote.ImageCompute;
import com.eungu.lineplusnote.MemoList.ImageListMaker.ImageListAdapter;
import com.eungu.lineplusnote.MemoList.ImageListMaker.ImageListListener;
import com.eungu.lineplusnote.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class AddMemoActivity extends AppCompatActivity implements ImageListListener, View.OnClickListener {
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int MY_PERMISSIONS_REQUEST_CAMERA = 123;
    private static final int ADD_IMAGE_FROM_GALLERY = 100;
    String imageNameBuffer;

    EditText title_edit = null;
    EditText content_edit = null;
    Button add_image_button = null;

    MenuItem edit_menu, save_menu;

    Handler handler;

    private int dbIdx;

    private boolean isModified = false, isSaved = false;
    private boolean isReadOnly;

    private ArrayList<String> imageName;
    private ArrayList<String> imageInCacheName;
    ArrayList<File> imageListItems;

    ImageListAdapter imageListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_or_modify_memo);

        ImageCompute.deleteCache(this);

        handler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle bun = msg.getData();
                String result = bun.getString("RESULT");
                if(result == "OK"){
                    isModified = true;
                    setImageList();
                }
                else if(result == "FAIL"){
                    AlertDialog.Builder errorDialog = new AlertDialog.Builder(AddMemoActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog)
                            .setTitle("오류")
                            .setMessage("주소로부터 이미지를 읽을 수 없습니다.")
                            .setPositiveButton("확인", null);
                    errorDialog.show();
                }
            }
        };

        initView();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // 뷰 초기 설정
    private void initView(){
        Intent intent = getIntent();
        dbIdx = intent.getExtras().getInt("idx", -1);

        title_edit = findViewById(R.id.edit_title);
        title_edit.addTextChangedListener(watcher);
        content_edit = findViewById(R.id.edit_content);
        content_edit.addTextChangedListener(watcher);
        add_image_button = findViewById(R.id.edit_add_image);

        add_image_button.setOnClickListener(this);

        initImageList();

        if(dbIdx != -1){
            inputEditData();
            isReadOnly = true;
        }
        else {
            isReadOnly = false;
        }

        isModified = false;
    }

    private void inputEditData(){
        DBManager dbManager = new DBManager(this);
        DBData data = dbManager.getData(dbIdx);

        title_edit.setText(data.getTitle());
        content_edit.setText(data.getContent());
        imageName = ImageCompute.imageListStringToArray(data.getImageList());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!imageName.isEmpty() || !imageInCacheName.isEmpty()) {
            setImageList();
        }
    }

    private void initImageList(){
        RecyclerView imageList = findViewById(R.id.image_list);
        imageListItems = new ArrayList<>();
        imageName = new ArrayList<>();
        imageInCacheName = new ArrayList<>();

        imageListAdapter = new ImageListAdapter(this, imageListItems, (dbIdx == -1));
        imageListAdapter.setListener(this);
        imageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageList.setAdapter(imageListAdapter);

        setImageList();
    }

    private void setImageList() {
        imageListItems.clear();
        for(int i = 0; i < imageName.size(); ++i) {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageName.get(i));
            if(file != null){
                imageListItems.add(file);
            }
        }

        for(int i = 0; i < imageInCacheName.size(); ++i) {
            File fileInCache = new File(getExternalCacheDir(), imageInCacheName.get(i));
            if(fileInCache != null){
                imageListItems.add(fileInCache);
            }
        }
        imageListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClickedItem(final String path) {
        AlertDialog.Builder oDialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
                .setTitle("이미지 삭제")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteImage(path);
                        imageListAdapter.notifyDataSetChanged();
                        isModified = true;
                    }
                })
                .setNeutralButton("아니요", null)
                .setCancelable(false);
        oDialog.show();
    }

    // 액션바에 있는 메뉴 설정
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_appbar_action, menu);
        edit_menu = menu.findItem(R.id.m_edit_memo);
        save_menu = menu.findItem(R.id.m_save_memo);

        if(dbIdx == -1){        // when adding memo
            changeToWritableMode();
        }
        else{                   //when seeing memo
            changeToReadOnlyMode();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        hideKeyboard();
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.m_edit_memo :
                changeToWritableMode();
                return true;
            case R.id.m_save_memo :
                if(isModified) {
                    if(checkCanSave() == 0){
                        saveMemo();
                        changeToReadOnlyMode();
                    }
                    else{
                        showToast("제목과 내용을 확인해주세요.", Toast.LENGTH_SHORT);
                    }
                }
                else {
                    showToast("변경사항이 없어 저장되지 않았습니다.", Toast.LENGTH_SHORT);
                    changeToReadOnlyMode();
                }
                return true ;
            case R.id.m_delete_memo :
                AlertDialog.Builder oDialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog);
                oDialog.setTitle("메모 삭제")
                        .setMessage("메모를 삭제하시겠습니까?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(dbIdx != -1) {
                                    DBManager dbManager = new DBManager(getApplicationContext());
                                    dbManager.deleteColumn(dbIdx);
                                    setResult(RESULT_OK);
                                }
                                deleteImage(null);
                                showToast("메모를 삭제하였습니다.", Toast.LENGTH_SHORT);
                                finish();
                            }
                        })
                        .setNeutralButton("아니요", null)
                        .setCancelable(false)
                        .show();
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    //메모 상세보기 <-> 메모 수정
    private void changeToReadOnlyMode(){
        hideKeyboard();
        edit_menu.setVisible(true);
        save_menu.setVisible(false);
        title_edit.setFocusable(false);
        content_edit.setFocusable(false);
        add_image_button.setVisibility(View.GONE);
        isReadOnly = true;
        imageListAdapter.setEditingMode(false);
        imageListAdapter.notifyDataSetChanged();
    }

    private void changeToWritableMode(){
        title_edit.setFocusableInTouchMode(true);
        content_edit.setFocusableInTouchMode(true);
        title_edit.setFocusable(true);
        content_edit.setFocusable(true);

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        title_edit.requestFocus();
        imm.showSoftInput(title_edit, InputMethodManager.SHOW_IMPLICIT);

        add_image_button.setVisibility(View.VISIBLE);
        edit_menu.setVisible(false);
        save_menu.setVisible(true);
        isReadOnly = false;
        imageListAdapter.setEditingMode(true);
        imageListAdapter.notifyDataSetChanged();
    }

    // 메모 추가 & 이미지 제거 관련
    private int checkCanSave(){
        if(title_edit.getText().toString().equals("")) return 1;
        else if(content_edit.getText().toString().equals("")) return 2;
        else return 0;
    }

    private void saveMemo(){
        int canSave = checkCanSave();
        if(canSave != 0){
            return;
        }
        ImageCompute.saveImageFromCache(this);
        isModified = false;
        isSaved = true;
        imageName.addAll(imageInCacheName);
        imageInCacheName.clear();
        DBData data = new DBData(Calendar.getInstance(), title_edit.getText().toString(), content_edit.getText().toString(), ImageCompute.imageListArrayToString(imageName));
        DBManager dbManager = new DBManager(getApplicationContext());
        if(dbIdx == -1) {
            dbIdx = dbManager.getItemsCount();
            dbManager.addData(data);
        }
        else{
            dbManager.updateData(data, dbIdx);
        }
        showToast("메모를 저장하였습니다.", Toast.LENGTH_SHORT);
        return;
    }

    private void deleteImage(String path){
        if(imageName.isEmpty()) return;

        if (path == null) {
            for(int i = 0; i < imageName.size(); ++i) {
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageName.get(i));
                if(!file.exists()) file = new File(getExternalCacheDir(), imageName.get(i));
                file.delete();
            }
        }
        else{
            File file = new File(path);
            imageName.remove(file.getName());
            file.delete();
        }
    }

    // 현재 액티비티 종료 관련
    @Override
    public void onBackPressed() {
        hideKeyboard();
        AlertDialog.Builder oDialog;
        String message = "";
        int canSave = checkCanSave();
        if(canSave == 1) message = "제목";
        else if (canSave == 2) message = "내용";
        if(dbIdx == -1) {
            if (isModified) {
                if(canSave != 0){
                    oDialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog);
                    oDialog.setTitle("나가기")
                            .setMessage(message + "이 비어있어 저장할 수 없습니다.\n저장하지 않고 나가시겠습니까?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setResult(RESULT_CANCELED);
                                    finish();
                                }
                            })
                            .setNeutralButton("아니요",  new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
                else {
                    saveMemo();
                    setResult(RESULT_OK);
                    finish();
                }
            }
            else {
                exitActivity();
            }
        }
        else {
            if(isReadOnly){
                exitActivity();
            }
            else{
                if(isModified){
                    if(checkCanSave() != 0){
                        oDialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog);
                        oDialog.setTitle("나가기")
                                .setMessage(message + "이 비어있어 저장할 수 없습니다.\n저장하지 않고 나가시겠습니까?")
                                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        setResult(RESULT_CANCELED);
                                        finish();
                                    }
                                })
                                .setNeutralButton("아니요",  new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        return;
                                    }
                                })
                                .setCancelable(false)
                                .show();
                    }
                    else {
                        final CharSequence[] items =  {"저장하고 나가기", "저장하지 않고 나가기", "취소"};
                        oDialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
                                .setTitle("메모를 저장하시겠습니까?")
                                .setItems(items, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int pos) {
                                        switch (pos){
                                            case 0:
                                                saveMemo();
                                                setResult(RESULT_OK);
                                                finish();
                                                break;
                                            case 1:
                                                if(isSaved)
                                                    setResult(RESULT_OK);
                                                else
                                                    setResult(RESULT_CANCELED);
                                                finish();
                                                break;
                                        }
                                    }
                                })
                                .setCancelable(false);
                        oDialog.show();
                    }
                }
                else {
                    changeToReadOnlyMode();
                }
            }
        }
    }

    private void exitActivity(){
        if(isSaved) { setResult(RESULT_OK); }
        else if (!isSaved && !isReadOnly){
            showToast("변경사항이 없어 저장되지 않았습니다.", Toast.LENGTH_SHORT);
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    // 시스템 관련
    private void hideKeyboard(){
        if(getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void showToast(String str, int duration){
        Toast t = Toast.makeText(getApplicationContext(), str, duration);
        t.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, t.getYOffset());
        t.show();
    }

    // 이미지 추가버튼 관련
    @Override
    public void onClick(View view) {
        hideKeyboard();
        final CharSequence[] items =  {"사진 촬영", "갤러리에서 선택", "URL에서 선택"};
        AlertDialog.Builder oDialog = new AlertDialog.Builder(AddMemoActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog)
                .setTitle("이미지 불러오기")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int pos) {
                        switch (pos){
                            case 0:
                                callCameraActivity();
                                break;
                            case 1:
                                addFromGallery();
                                break;
                            case 2:
                                downloadAndSetImageFromURL();
                                break;
                        }
                    }
                });
        oDialog.show();
    }

    private void callCameraActivity(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AddMemoActivity.this, Manifest.permission.CAMERA)) {
                AlertDialog.Builder oDialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog);
                oDialog.setTitle("권한 요청")
                        .setMessage("카메라를 이용하기 위해 권한이 필요합니다.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(AddMemoActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                            }
                        })
                        .setCancelable(false)
                        .show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }
        else{
            dispatchTakePictureIntent();
        }
    }

    private void addFromGallery() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "이미지 추가"), ADD_IMAGE_FROM_GALLERY);
    }

    private void downloadAndSetImageFromURL() {
        final EditText editText = new EditText(AddMemoActivity.this);
        final ConstraintLayout container = new ConstraintLayout(AddMemoActivity.this);
        final ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.edittext_in_dialog_margin);
        params.rightMargin =getResources().getDimensionPixelSize(R.dimen.edittext_in_dialog_margin);
        editText.setLayoutParams(params);
        container.addView(editText);

        AlertDialog.Builder urlDialog = new AlertDialog.Builder(AddMemoActivity.this);
        urlDialog.setView(container)
                .setTitle("URL 입력")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new Thread(){
                            @Override
                            public void run() {
                                String result = ImageCompute.openImage(getApplicationContext(), editText.getText().toString());
                                Bundle bun = new Bundle();
                                if(result != null){
                                    bun.putString("RESULT", "OK");
                                    imageInCacheName.add(result);
                                }
                                else{
                                    bun.putString("RESULT", "FAIL");
                                }
                                Message msg = handler.obtainMessage();
                                msg.setData(bun);
                                handler.sendMessage(msg);
                            }
                        }.start();
                    }
                })
                .setNegativeButton("취소", null);
        urlDialog.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            photoFile = ImageCompute.createImageFile(this);
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.eungu.lineplusnote.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                imageNameBuffer = photoFile.getName();
            }
        }
    }

    // 이미지를 불러오는 인텐트를 실행했을 시 결과
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ADD_IMAGE_FROM_GALLERY && resultCode == RESULT_OK){
            Uri uri = data.getData();
            try {
                imageInCacheName.add(ImageCompute.openImage(this, uri));
                isModified = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if(imageNameBuffer != null && !imageNameBuffer.equals("")) {
                imageInCacheName.add(imageNameBuffer);
                ImageCompute.saveImageIcon(this, new File(getExternalCacheDir().getAbsolutePath() + "/" + imageNameBuffer));
                isModified = true;
            }
        }
    }

    TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            isModified = true;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    // 카메라 접근권한 관련
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(getApplicationContext(), "카메라 권한이 없어 사진을 찍을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
