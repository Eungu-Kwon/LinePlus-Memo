package com.eungu.lineplusnote.StaticMethod;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ImageFileManager {

    public static boolean copyFile(File file , String save_file){
        if(file != null && file.exists()){
            try {
                FileInputStream fis = new FileInputStream(file);
                FileOutputStream newfos = new FileOutputStream(save_file);

                int readcount=0;
                byte[] buffer = new byte[1024];
                while((readcount = fis.read(buffer,0,1024))!= -1){
                    newfos.write(buffer,0,readcount);
                }
                newfos.close();
                fis.close();

            } catch (Exception e) {
                return false;
            }

            return true;
        }

        else{
            return false;
        }
    }

    public static boolean copyFile(Bitmap file , String save_file){
        try {
            FileOutputStream newfos = new FileOutputStream(save_file);

            file.compress(Bitmap.CompressFormat.JPEG, 100, newfos);
            newfos.close();

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void saveImageFromCache(final Context c) {
        new Thread(){
            @Override
            public void run() {
                File[] files = c.getExternalCacheDir().listFiles();
                for(File f : files){
                    copyFile(f, c.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + f.getName());
                }
            }
        }.start();
    }

    public static void deleteCache(Context c){
        File[] files = c.getExternalCacheDir().listFiles();
        for(File f : files){
            if(f.exists()) f.delete();
        }
    }

    public static void saveImageIcon(Context c, File f){
        String iconFile;
        iconFile = c.getExternalCacheDir().getAbsolutePath() + "/" + f.getName() + "_icon";
        File tempFile = new File(iconFile);
        try {
            FileOutputStream out = new FileOutputStream(tempFile);

            Bitmap bmp = ImageCompute.getBmpFromPathWithResize(f.getAbsolutePath(), 200);
            bmp = ImageCompute.getCroppedImage(bmp);

            bmp.compress(Bitmap.CompressFormat.JPEG, 20, out);
            out.close();
            bmp.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File createImageFile(Context c) {
        // Create an image file name
        String timeStamp = getTimeStamp();
        String imageFileName = timeStamp;
        File storageDir = c.getExternalCacheDir();
        File image = new File(storageDir + "/" + imageFileName + ".jpg");
        return image;
    }

    public static String getTimeStamp(){
        return new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
    }
}
