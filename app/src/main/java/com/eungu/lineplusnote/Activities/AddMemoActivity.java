package com.eungu.lineplusnote.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.EditText;
import android.widget.TextView;
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
import com.eungu.lineplusnote.MemoHandler.HandlerListener;
import com.eungu.lineplusnote.MemoHandler.WorkHandler;
import com.eungu.lineplusnote.MemoList.ImageListMaker.ImageListAdapter;
import com.eungu.lineplusnote.MemoList.ImageListMaker.ImageListListener;
import com.eungu.lineplusnote.R;
import com.eungu.lineplusnote.StaticMethod.ImageCompute;
import com.eungu.lineplusnote.StaticMethod.ImageFileManager;
import com.eungu.lineplusnote.StaticMethod.ImageOpener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AddMemoActivity extends AppCompatActivity implements ImageListListener, HandlerListener {
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int MY_PERMISSIONS_REQUEST_CAMERA = 123;
    private static final int ADD_IMAGE_FROM_GALLERY = 100;

    String imageNameBuffer;

    TextView dateTextView = null;
    EditText titleEdit = null;
    EditText contentEdit = null;

    MenuItem editMenu, saveMenu, addImageMenu;

    WorkHandler handler;

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

        ImageFileManager.deleteCache(this);

        handler = new WorkHandler(getApplicationContext());
        handler.setListener(this);

        initView();
    }

    @Override
    public void messageFromHandler(Bundle b) {
        String result = b.getString("RESULT");
        if(result == "OK"){
            isModified = true;
            setImageList();
            showToast("다운로드 완료!", Toast.LENGTH_LONG);
        }
        else if(result == "FAIL"){
            createDialog("오류", "주소로부터 이미지를 읽을 수 없습니다.", "확인", null, null, null).show();
            showToast("다운로드 실패", Toast.LENGTH_SHORT);
        }
    }

    // 뷰 초기 설정
    private void initView(){
        // 현재 메모의 index를 불러온다; 새로 만드는 메모일 경우 -1을 반환
        Intent intent = getIntent();
        dbIdx = intent.getExtras().getInt("idx", -1);

        dateTextView = findViewById(R.id.memo_recent_modified);

        titleEdit = findViewById(R.id.edit_title);
        titleEdit.addTextChangedListener(watcher);
        contentEdit = findViewById(R.id.edit_content);
        contentEdit.addTextChangedListener(watcher);

        // 화면을 세로 고정하고 actionbar 을 커스텀
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setElevation(0f);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // 이미지 리스트를 초기화
        initImageList();

        // 메모를 불러온것이면 기존 데이터를 채워넣는다
        if(dbIdx != -1){
            inputEditData();
            isReadOnly = true;
        }
        else {
            isReadOnly = false;
            dateTextView.setVisibility(View.GONE);
        }

        isModified = false;
    }

    // 기존 메모를 TextView에 채워넣는 메소드; 메모를 새로 만드는 경우에는 호출하지 않는다
    private void inputEditData(){
        DBManager dbManager = new DBManager(this);
        DBData data = dbManager.getData(dbIdx);

        titleEdit.setText(data.getTitle());
        contentEdit.setText(data.getContent());
        imageName = ImageCompute.imageListStringToArray(data.getImageList());

        String dateString = "작성일 : " + new SimpleDateFormat("yyyy.MM.dd HH:mm").format(data.getTime().getTime());
        if(data.getLastTime() != null && !data.getTime().equals(data.getLastTime())) dateString += "\n" + "수정일 : " + new SimpleDateFormat("yyyy.MM.dd HH:mm").format(data.getLastTime().getTime());

        dateTextView.setText(dateString);
    }

    // onResume()가 호출되면 이미지 리스트를 갱신
    @Override
    protected void onResume() {
        super.onResume();
        if(!imageName.isEmpty() || !imageInCacheName.isEmpty()) {
            setImageList();
        }
    }

    // Recycler View 초기화
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

    // Recycler View 갱신
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

    // 수정중 이미지를 누르면 Dialog를 통해 물어본 후 이미지 삭제
    @Override
    public void onClickedItem(final String path) {
        DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteImage(path);
                imageListAdapter.notifyDataSetChanged();
                isModified = true;
            }
        };
        createDialog("이미지 삭제", "이미지를 삭제하시겠습니까?", "예", positive, "아니요", null).show();
    }

    // 액션바에 있는 메뉴 설정; 메모를 처음 만들경우 WritableMode, 기존 메모를 확인하는 경우 ReadOnlyMoce
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_appbar_action, menu);
        editMenu = menu.findItem(R.id.m_edit_memo);
        saveMenu = menu.findItem(R.id.m_save_memo);
        addImageMenu = menu.findItem(R.id.m_add_image);

        if(dbIdx == -1){        // when adding memo
            changeToWritableMode();
        }
        else{                   //when seeing memo
            changeToReadOnlyMode();
        }
        return super.onCreateOptionsMenu(menu);
    }

    // 상단 ActionBar의 메뉴의 기능 설정
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
                        // 메모가 수정되었고 저장 가능하면 저장 후 ReadOnlyMode
                        saveMemo();
                        changeToReadOnlyMode();
                    }
                    else{
                        // 저장 불가시 (제목 또는 내용이 비어있을 때)
                        showToast("제목과 내용을 확인해주세요.", Toast.LENGTH_SHORT);
                    }
                }
                else {
                    // 수정되지 않았으면 바로 ReadOnlyMode
                    showToast("변경사항이 없어 저장되지 않았습니다.", Toast.LENGTH_SHORT);
                    changeToReadOnlyMode();
                }
                return true ;

            case R.id.m_add_image:
                askToSaveImage();
                return true;

            case R.id.m_delete_memo :
                DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(dbIdx != -1) {
                            DBManager dbManager = new DBManager(getApplicationContext());
                            dbManager.deleteRow(dbIdx);
                            setResult(RESULT_OK);
                        }
                        deleteImage(null);
                        showToast("메모를 삭제하였습니다.", Toast.LENGTH_SHORT);
                        finish();
                    }
                };
                createDialog("메모 삭제", "메모를 삭제하시겠습니까?", "예", positive, "아니요", null).show();
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    //메모 상세보기 <-> 메모 수정
    private void changeToReadOnlyMode(){
        hideKeyboard();
        editMenu.setVisible(true);
        saveMenu.setVisible(false);
        addImageMenu.setVisible(false);
        titleEdit.setFocusable(false);
        contentEdit.setFocusable(false);
        isReadOnly = true;
        imageListAdapter.setEditingMode(false);
        imageListAdapter.notifyDataSetChanged();
        getSupportActionBar().setTitle("상세보기");
    }

    private void changeToWritableMode(){
        titleEdit.setFocusableInTouchMode(true);
        contentEdit.setFocusableInTouchMode(true);
        titleEdit.setFocusable(true);
        contentEdit.setFocusable(true);

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        titleEdit.requestFocus();
        imm.showSoftInput(titleEdit, InputMethodManager.SHOW_IMPLICIT);

        editMenu.setVisible(false);
        saveMenu.setVisible(true);
        addImageMenu.setVisible(true);
        isReadOnly = false;
        imageListAdapter.setEditingMode(true);
        imageListAdapter.notifyDataSetChanged();
        if(dbIdx != -1) getSupportActionBar().setTitle("메모 수정");
        else getSupportActionBar().setTitle("메모 추가");
    }

    // 제목 또는 내용이 비어있으면 저장 불가; 그 외에는 저장가능
    private int checkCanSave(){
        if(titleEdit.getText().toString().equals("")) return 1;
        else if(contentEdit.getText().toString().equals("")) return 2;
        else return 0;
    }

    // 메모를 저장하는 메소드
    private void saveMemo(){
        int canSave = checkCanSave();
        if(canSave != 0){
            return;
        }

        // Cache에 저장된 이미지를 옮긴다
        ImageFileManager.saveImageFromCache(this);
        isModified = false;
        isSaved = true;
        imageName.addAll(imageInCacheName);
        imageInCacheName.clear();

        // DB에 메모를 저장; 작성일 이외의 데이터를 업데이트
        Calendar toSaveCalendar = Calendar.getInstance(), lastDate = Calendar.getInstance();
        DBManager dbManager = new DBManager(getApplicationContext());
        if(dbIdx != -1){
            DBData oldData = dbManager.getData(dbIdx);
            toSaveCalendar = oldData.getTime();
            lastDate = Calendar.getInstance();
        }
        DBData data = new DBData(toSaveCalendar, lastDate, titleEdit.getText().toString(), contentEdit.getText().toString(), ImageCompute.imageListArrayToString(imageName));

        // 메모가 처음 만든것이면 DB에 추가; 수정중이면 업데이트
        if(dbIdx == -1) {
            dbIdx = dbManager.getItemsCount();
            dbManager.addData(data);
        }
        else{
            dbManager.updateData(data, dbIdx);
        }

        String dateString = "작성일 : " + new SimpleDateFormat("yyyy.MM.dd HH:mm").format(data.getTime().getTime());
        if(data.getLastTime() != null && !data.getTime().equals(data.getLastTime())) dateString += "\n" + "수정일 : " + new SimpleDateFormat("yyyy.MM.dd HH:mm").format(data.getLastTime().getTime());

        dateTextView.setVisibility(View.VISIBLE);
        dateTextView.setText(dateString);
        showToast("메모를 저장하였습니다.", Toast.LENGTH_SHORT);
        return;
    }

    // 이미지 삭제 메소드; path == null 이면 현재 메모내 모든 이미지 제거
    public void deleteImage(String path){
        if(imageName.isEmpty()) return;

        if (path == null) {
            for(int i = 0; i < imageName.size(); ++i) {
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageName.get(i));
                File iconFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageName.get(i) + "_icon");
                if(file.exists()){
                    file.delete();
                    iconFile.delete();
                }
            }
        }
        else{
            File file = new File(path);
            File iconFile = new File(path + "_icon");
            imageName.remove(file.getName());
            file.delete();
            iconFile.delete();
        }
    }

    // 현재 액티비티 종료 관련
    @Override
    public void onBackPressed() {
        hideKeyboard();
        String message = "";
        int canSave = checkCanSave();
        if(canSave == 1) message = "제목";
        else if (canSave == 2) message = "내용";

        // 메모 내용이 수정되었을때 저장가능한지 체크하고 저장 / 폐기한다
        if (isModified) {

            // 저장할 수 없을때
            if(canSave != 0){
                DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                };
                createDialog("나가기", message + "이 비어있어 저장할 수 없습니다.\n저장하지 않고 나가시겠습니까?", "예", positive, "아니요", null).show();
            }

            // 저장 가능할 때
            else {
                onModiFiedAndCanSave();
            }
        }

        // 수정되지 않았을 시 현재 액티비티를 종료
        else {
            exitActivity();
        }
    }

    // 메모가 수정 & 저장 되었는지 체크 후 종료
    private void exitActivity(){
        if(isSaved) { setResult(RESULT_OK); }
        else if (!isSaved && !isReadOnly){
            showToast("변경사항이 없어 저장되지 않았습니다.", Toast.LENGTH_SHORT);
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    // 메모가 수정되었고 저장가능할 때 호출
    private void onModiFiedAndCanSave(){
        // 메모를 처음 만들었으면 저장하고 나간다
        if(dbIdx == -1) {
            saveMemo();
            setResult(RESULT_OK);
            finish();
        }

        // 메모를 수정중이면 물어보고 결정
        else {
            final CharSequence[] items =  {"저장하고 나가기", "저장하지 않고 나가기", "취소"};
            AlertDialog.Builder oDialog = new AlertDialog.Builder(this)
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

    // 키보드를 내리는 메소드
    private void hideKeyboard(){
        if(getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    // Toast를 띄우는 메소드
    private void showToast(String str, int duration){
        Toast t = Toast.makeText(getApplicationContext(), str, duration);
        t.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, t.getYOffset());
        t.show();
    }

    // Dialog를 띄우는 메소드
    private AlertDialog createDialog(String title, String msg, String posMsg, DialogInterface.OnClickListener positive, String nagMsg, DialogInterface.OnClickListener negative){
        AlertDialog.Builder oDialog = new AlertDialog.Builder(this);
        oDialog.setTitle(title)
                .setMessage(msg)
                .setNegativeButton(posMsg, positive)
                .setCancelable(false);
        if(nagMsg != null) oDialog.setPositiveButton(nagMsg, negative);
        return oDialog.create();
    }

    // 이미지 추가버튼 관련
    public void askToSaveImage() {
        hideKeyboard();
        final CharSequence[] items =  {"사진 촬영", "갤러리에서 선택", "URL에서 선택"};
        AlertDialog.Builder oDialog = new AlertDialog.Builder(AddMemoActivity.this)
                .setTitle("이미지 불러오기")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int pos) {
                        addImage(pos);
                    }
                });
        oDialog.show();
    }

    private void addImage(int pos){
        switch (pos) {
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

    // 카메라 권한 요청 메소드
    private void callCameraActivity(){
        // 권한이 없으면 요청
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AddMemoActivity.this, Manifest.permission.CAMERA)) {
                DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(AddMemoActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                };
                // 전에 권한을 거부한 적이 있으면 설명을 띄운다
                createDialog("권한 요청", "카메라를 이용하기 위해 권한이 필요합니다.", "확인", positive, null, null).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }
        // 권한이 있으면 카메라 호출
        else{
            dispatchTakePictureIntent();
        }
    }

    // 갤러리에서 이미지 선택
    private void addFromGallery() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "이미지 추가"), ADD_IMAGE_FROM_GALLERY);
    }

    // URL로부터 이미지를 다운로드
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
                .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new Thread(){
                            @Override
                            public void run() {
                                File image = ImageOpener.openImage(getApplicationContext(), editText.getText().toString());

                                Bundle bun = new Bundle();
                                bun.putString("REQUEST", WorkHandler.HANDLE_IN_SAVE_FROM_URL);
                                if(image != null){
                                    bun.putString("RESULT", "OK");
                                    imageInCacheName.add(image.getName());
                                    ImageFileManager.saveImageIcon(getApplicationContext(), image);
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
                .setPositiveButton("취소", null);
        urlDialog.show();
    }

    // 카메라 권한이 있으면 Intent 호출
    // 출처 : https://developer.android.com/training/camera/photobasics#java
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            photoFile = ImageFileManager.createImageFile(this);
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

        // 갤러리 Intent에서 결과를 받을 때
        if(requestCode == ADD_IMAGE_FROM_GALLERY && resultCode == RESULT_OK){
            Uri uri = data.getData();
            try {
                // URI에서 이미지와 아이콘을 저장
                File image = ImageOpener.openImage(this, uri);
                ImageFileManager.saveImageIcon(getApplicationContext(), image);
                imageInCacheName.add(image.getName());
                isModified = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 카메라 Intent에서 결과를 받을 때
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if(imageNameBuffer != null && !imageNameBuffer.equals("")) {
                imageInCacheName.add(imageNameBuffer);
                ImageFileManager.saveImageIcon(this, new File(getExternalCacheDir().getAbsolutePath() + "/" + imageNameBuffer));
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
