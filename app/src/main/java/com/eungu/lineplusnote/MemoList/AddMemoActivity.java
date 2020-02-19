package com.eungu.lineplusnote.MemoList;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eungu.lineplusnote.DBManager.DBData;
import com.eungu.lineplusnote.DBManager.DBManager;
import com.eungu.lineplusnote.MemoList.ImageListMaker.ImageListAdapter;
import com.eungu.lineplusnote.MemoList.ImageListMaker.ImageListItem;
import com.eungu.lineplusnote.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AddMemoActivity extends AppCompatActivity {
    EditText title_edit = null;
    EditText content_edit = null;
    Button add_image_button = null;

    MenuItem edit_menu, save_menu;

    private int dbIdx;

    private boolean isModified = false, isSaved = false;
    private boolean isReadOnly;

    private ArrayList<String> imageName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_or_modify_memo);

        initView();
        setToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!imageName.isEmpty()) {
            setImageList();
        }
    }

    private void initView(){
        Intent intent = getIntent();
        dbIdx = intent.getExtras().getInt("idx", -1);

        title_edit = findViewById(R.id.edit_title);
        title_edit.addTextChangedListener(watcher);
        content_edit = findViewById(R.id.edit_content);
        content_edit.addTextChangedListener(watcher);
        add_image_button = findViewById(R.id.edit_add_image);

        add_image_button.setOnClickListener(new View.OnClickListener() {
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
                                        //TODO add camera system
                                        break;
                                    case 1:
                                        Intent i = new Intent();
                                        i.setType("image/*");
                                        i.setAction(Intent.ACTION_GET_CONTENT);
                                        startActivityForResult(Intent.createChooser(i, "이미지 추가"), 100);
                                        break;
                                    case 2:
                                        //TODO make add image from URL
                                        break;
                                }
                            }
                        });
                oDialog.show();
            }
        });

        if(dbIdx != -1){
            inputEditData();
            isReadOnly = true;
        }
        else {
            isReadOnly = false;
            imageName = new ArrayList<>();
        }

        isModified = false;
    }

    private void inputEditData(){
        DBManager dbManager = new DBManager(this);
        DBData data = dbManager.getData(dbIdx);

        title_edit.setText(data.getTitle());
        content_edit.setText(data.getContent());
        imageName = imageListStringToArray(data.getImageList());
    }

    private void setToolbar(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setImageList(){
        RecyclerView imageList = findViewById(R.id.image_list);
        ArrayList<ImageListItem> imageListItems = new ArrayList<>();

        for(int i = 0; i < imageName.size(); ++i) {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageName.get(i));
            if(file != null){
                imageListItems.add(new ImageListItem(getBmpFromUriWithResize(file.getAbsolutePath()), imageName.get(i)));
            }
        }

        ImageListAdapter imageListAdapter = new ImageListAdapter(this, imageListItems);
        imageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageList.setAdapter(imageListAdapter);
    }

    private void deleteImage(String name){
        if(imageName.isEmpty()) return;

        if (name == null) {
            for(int i = 0; i < imageName.size(); ++i) {
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageName.get(i));
                file.delete();
            }
        }
        else{
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), name);
            file.delete();
        }
    }

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

    private void changeToReadOnlyMode(){
        hideKeyboard();
        edit_menu.setVisible(true);
        save_menu.setVisible(false);
        title_edit.setFocusable(false);
        content_edit.setFocusable(false);
        add_image_button.setVisibility(View.GONE);
        isReadOnly = true;
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
    }

    private int checkCanSave(){
        if(title_edit.getText().toString().equals("")) return 1;
        else if(content_edit.getText().toString().equals("")) return 2;
        else return 0;
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

    private boolean saveMemo(){
        int canSave = checkCanSave();
        if(canSave != 0){
            return false;
        }
        isModified = false;
        isSaved = true;
        DBData data = new DBData(Calendar.getInstance(), title_edit.getText().toString(), content_edit.getText().toString(), imageListArrayToString(imageName));
        DBManager dbManager = new DBManager(getApplicationContext());
        if(dbIdx == -1) {
            dbIdx = dbManager.getItemsCount();
            dbManager.addData(data);
        }
        else{
            dbManager.updateData(data, dbIdx);
        }
        showToast("메모를 저장하였습니다.", Toast.LENGTH_SHORT);
        return true;
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == RESULT_OK){
            Uri uri = data.getData();
            try {
                saveImage(uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            isModified = true;
        }
    }

    public static byte[] inputStreamToByteArray(InputStream is) {

        byte[] resBytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int read = -1;
        try {
            while ( (read = is.read(buffer)) != -1 ) {
                bos.write(buffer, 0, read);
            }

            resBytes = bos.toByteArray();
            bos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return resBytes;
    }

    private void saveImage(Uri uri) throws IOException {
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
        InputStream inputStream = getContentResolver().openInputStream(uri);
        byte[] strToByte = inputStreamToByteArray(inputStream);

        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(strToByte);
        fos.close();
        inputStream.close();

        imageName.add(fileName);
    }

    private static ArrayList<String> imageListStringToArray(String str){
        ArrayList<String> list = new ArrayList<>();
        String[] str_list = str.split(",");
        for(String i : str_list){
            if (i == null || i.equals("")) continue;
            list.add(i);
        }
        return list;
    }

    private static String imageListArrayToString(ArrayList<String> arr){
        String str = "";
        for(String i : arr){
            str += i + ",";
        }
        return str;
    }

    private Bitmap getBmpFromUriWithResize(String path){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int width = options.outWidth;
        int height = options.outHeight;

        options.inSampleSize = calculateInSampleSize(options, 100, 100);
        options.inJustDecodeBounds = false;

        Bitmap bmp = BitmapFactory.decodeFile(path, options);

        if(height < width) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);

            Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            bmp.recycle();
            return resizedBitmap;
        }

        else return bmp;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
