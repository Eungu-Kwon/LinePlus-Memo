package com.eungu.lineplusnote.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.eungu.lineplusnote.StaticMethod.ImageCompute;
import com.eungu.lineplusnote.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;

public class ImageViewActivity extends AppCompatActivity {

    private String fileName;
    private File imageFile;
    private boolean isFullmode;
    private Bitmap bmp;

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bmp.recycle();
    }
}
