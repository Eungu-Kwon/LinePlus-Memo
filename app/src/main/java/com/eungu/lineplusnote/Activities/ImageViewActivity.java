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
import android.media.ExifInterface;
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
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageViewActivity extends AppCompatActivity {

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

        Intent intent = getIntent();
        fileName = intent.getExtras().getString("path", "");

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

        bmp = ImageCompute.getBmpFromPathWithRotate(imageFile.getAbsolutePath());

        pv.setImageDrawable(new BitmapDrawable(this.getResources(), bmp));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        handler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle bun = msg.getData();
                String result = bun.getString("RESULT");
                String request = bun.getString("REQUEST");
                if(result == "OK"){
                    Toast.makeText(getApplicationContext(), "이미지를 저장했습니다.", Toast.LENGTH_LONG).show();
                }
                else if(result == "FAIL"){
                    Toast.makeText(getApplicationContext(), "이미지를 저장하지 못했습니다.", Toast.LENGTH_LONG).show();
                }
            }
        };
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
                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext())
                        .setTitle("권한 요청")
                        .setMessage("카메라를 이용하기 위해 권한이 필요합니다.")
                        .setPositiveButton("확인", positive);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE);
            }
        }
        else{
            addImageToGallery();
        }
    }

    @SuppressWarnings("deprecation")
    public void addImageToGallery() {
        final Bundle bun = new Bundle();
        bun.putString("REQUEST", "100");

        Uri collection;
        ContentValues values = new ContentValues();
        ContentResolver contentResolver = getContentResolver();

        String fileName = imageFile.getName() + ".jpg";

        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/*");

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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                contentResolver.update(item, values, null, null);
            }

            Message msg = handler.obtainMessage();
            msg.setData(bun);
            handler.sendMessage(msg);
        }

        else{
            File saveDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/LineMemo");
            final File saveFile = new File(saveDir, fileName);
            if (!saveDir.exists()) saveDir.mkdir();

            new Thread(){
                @Override
                public void run() {
                    boolean result = ImageFileManager.copyFile(bmp, saveFile.getAbsolutePath());

                    if(result){
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bmp.recycle();
    }
}
