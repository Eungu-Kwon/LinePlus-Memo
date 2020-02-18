package com.eungu.lineplusnote.MemoList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.eungu.lineplusnote.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ImageViewActivity extends AppCompatActivity {
    String _id;
    boolean isFullmode;
    Toolbar tb;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view_layout);

        Intent intent = getIntent();
        _id = intent.getExtras().getString("_id", "");

        tb = findViewById(R.id.image_view_toolbar);
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(tb);

        isFullmode = true;
        hideSystemUI();

        // PhotoView by https://github.com/chrisbanes/PhotoView
        PhotoView pv = findViewById(R.id.photo_view);
        Uri uri_item = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/" + _id);
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
        pv.setImageDrawable(new BitmapDrawable(this.getResources(), getBmpFromUriWithRotate(uri_item)));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    private Bitmap getBmpFromUriWithRotate(Uri uri){
        InputStream is = null;
        try {
            is = getContentResolver().openInputStream(uri);

            Bitmap bmp = BitmapFactory.decodeStream(new BufferedInputStream(getContentResolver().openInputStream(uri)));

            if(bmp.getHeight() < bmp.getWidth()) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);

                Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                bmp.recycle();
                return resizedBitmap;
            }

            else return bmp;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
//        tb.setVisibility(View.GONE);
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
//        tb.setVisibility(View.VISIBLE);
        getSupportActionBar().show();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
