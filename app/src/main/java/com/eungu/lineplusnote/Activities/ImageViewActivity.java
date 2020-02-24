package com.eungu.lineplusnote.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.eungu.lineplusnote.R;
import com.eungu.lineplusnote.StaticMethod.ImageCompute;
import com.eungu.lineplusnote.StaticMethod.ImageFileManager;
import com.eungu.lineplusnote.MemoHandler.WorkHandler;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageViewActivity extends AppCompatActivity  {

    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 121;
    private String fileName;
    private File imageFile;
    private boolean isFullmode;
    private Bitmap bmp;

    Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view_layout);

        // 파일 path을 전달받는다.
        Intent intent = getIntent();
        fileName = intent.getExtras().getString("path", "");

        // 전체모드 활성화
        isFullmode = true;
        hideSystemUI();

        // PhotoView by https://github.com/chrisbanes/PhotoView
        PhotoView pv = findViewById(R.id.photo_view);
        pv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFullmode){
                    isFullmode = false;
                    showSystemUI();
                }
                else {
                    isFullmode = true;
                    hideSystemUI();
                }
            }
        });

        imageFile = new File(fileName);

        // Bitmap 객체를 전달받고 Set
        bmp = ImageCompute.getBmpFromPathWithRotate(imageFile.getAbsolutePath());

        pv.setImageDrawable(new BitmapDrawable(this.getResources(), bmp));

        getSupportActionBar().setTitle("");
        getSupportActionBar().setElevation(0f);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        handler = new WorkHandler(getApplicationContext());
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        getSupportActionBar().hide();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        getSupportActionBar().show();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_view, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.m_download_image:
                checkPermissionAndSave();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // 저장소 접근 권한 체크 (Android P 이하)
    public void checkPermissionAndSave(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ImageViewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(ImageViewActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE);
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ImageViewActivity.this)
                        .setTitle("권한 요청")
                        .setMessage("갤러리에 이미지를 저장하기 위해\n" +
                                "저장소 접근 권한이 필요합니다.")
                        .setPositiveButton("확인", positive);
                builder.show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE);
            }
        }
        else{
            addImageToGallery();
        }
    }

    // 사진을 저장하는 메소드
    // Android P 이하에서는 Picture 내 하위폴더에 저장하기 위해 Deprecated 된 API 이용 (버전 Handle 필수)
    // Android Q 이상은 MediaStore.Images.Media.RELATIVE_PATH 이용가능 (저장소 접근권한 불필요)
    @SuppressWarnings("deprecation")
    public void addImageToGallery() {
        final Bundle bun = new Bundle();
        bun.putString("REQUEST", WorkHandler.HANDLE_IN_SAVE_TO_GALLERY);

        Uri collection;
        ContentValues values = new ContentValues();
        ContentResolver contentResolver = getContentResolver();

        String fileName = ImageFileManager.getTimeStamp() + ".jpg";

        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/*");

        // Android Q 이상일때
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LineMemo");
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

            Uri item = contentResolver.insert(collection, values);

            ParcelFileDescriptor pdf = null;
            try {
                pdf = contentResolver.openFileDescriptor(item, "w", null);
                if (pdf != null) {
                    InputStream inputStream = new FileInputStream(imageFile);
                    byte[] strToByte = ImageCompute.inputStreamToByteArray(inputStream);
                    FileOutputStream fos = new FileOutputStream(pdf.getFileDescriptor());
                    fos.write(strToByte);
                    fos.close();
                    inputStream.close();
                    pdf.close();
                    contentResolver.update(item, values, null, null);
                    bun.putString("RESULT", "OK");
                }
            } catch (IOException e) {
                bun.putString("RESULT", "FAIL");
            } catch (Exception e) {
                bun.putString("RESULT", "FAIL");
            }

            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            contentResolver.update(item, values, null, null);

            Message msg = handler.obtainMessage();
            msg.setData(bun);
            handler.sendMessage(msg);
        }

        // Android P 이하일 때
        // 파일이 크면 시간이 걸릴 수 있으니 다른 Thread 에서 실행
        else{
            File saveDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/LineMemo");
            final File saveFile = new File(saveDir, fileName);
            if (!saveDir.exists()) saveDir.mkdir();

            new Thread(){
                @Override
                public void run() {
                    boolean result = ImageFileManager.saveBitmapToFile(bmp, saveFile.getAbsolutePath());

                    if(result){
                        galleryAddPic(saveFile.getAbsolutePath());
                        bun.putString("RESULT", "OK");
                    }
                    else{
                        bun.putString("RESULT", "FAIL");
                    }

                    Message msg = handler.obtainMessage();
                    msg.setData(bun);
                    handler.sendMessage(msg);
                }
            }.start();

            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            values.put(MediaStore.Images.Media.DATA, saveDir.getAbsolutePath() + "/" + fileName);
            contentResolver.insert(collection, values);
        }
    }

    // 사진을 갤러리에 추가하는 메소드
    // 시간이 지나면 자동으로 추가되지만 직접 추가
    private void galleryAddPic(String currentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addImageToGallery();
                } else {
                    Toast.makeText(getApplicationContext(), "저장소 접근 권한이 없어 사진을 저장할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    // Bitmap 객체는 크기때문에 OOM (Out Of Memory)를 일으킬 수 있음
    // recycle()로 release
    @Override
    protected void onDestroy() {
        super.onDestroy();
        bmp.recycle();
    }
}
