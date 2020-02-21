package com.eungu.lineplusnote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ImageCompute {

    public static final int NO_RESIZE = -1;

    public static boolean copyFile(File file , String save_file){
        boolean result;
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
                e.printStackTrace();
            }
            result = true;
        }else{
            result = false;
        }
        return result;
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

    public static int getOrientationOfImage(String filepath) {
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(filepath);
        } catch (IOException e) {
            return -1;
        }

        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

        if (orientation != -1) {
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
            }
        }

        return 0;
    }

    public static Bitmap getBmpFromPathWithRotate(String path){
        Bitmap bmp = BitmapFactory.decodeFile(path);
        int orientation = getOrientationOfImage(path);

        if(orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            Bitmap resizedBitmap;
            resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            bmp.recycle();
            return resizedBitmap;
        }

        else return bmp;
    }

    public static Bitmap getBmpFromPathWithResize(String path, int size){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int orientation = getOrientationOfImage(path);

        if(size != NO_RESIZE) options.inSampleSize = ImageCompute.calculateInSampleSize(options, size, size);
        options.inJustDecodeBounds = false;

        Bitmap bmp = BitmapFactory.decodeFile(path, options);

        if(orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            bmp.recycle();
            return resizedBitmap;
        }

        else return bmp;
    }

    public static ArrayList<String> imageListStringToArray(String str){
        ArrayList<String> list = new ArrayList<>();
        String[] str_list = str.split(",");
        for(String i : str_list){
            if (i == null || i.equals("")) continue;
            list.add(i);
        }
        return list;
    }

    public static String imageListArrayToString(ArrayList<String> arr){
        String str = "";
        for(String i : arr){
            str += i + ",";
        }
        return str;
    }

    public static void saveImageFromCache(Context c) {
        //TODO make run in new Thread
        File[] files = c.getExternalCacheDir().listFiles();
        for(File f : files){
            ImageCompute.copyFile(f, c.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + f.getName());
        }
    }

    public static void deleteCache(Context c){
        File[] files = c.getExternalCacheDir().listFiles();
        for(File f : files){
            if(f.exists()) f.delete();
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

    //open Image from internet
    public static String openImage(Context c, final String src) {
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
        File file = new File(c.getExternalCacheDir(), fileName);

        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            String contentType = connection.getHeaderField("Content-Type");
            if(!contentType.startsWith("image/")) {
                return null;
            }
            InputStream input = connection.getInputStream();
            byte[] strToByte = inputStreamToByteArray(input);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(strToByte);
            fos.close();
            input.close();

        } catch (IOException e) {
            return null;
        }

        saveImageIcon(c, file);
        return fileName;
    }

    public static String openImage(Context c, Uri uri) throws IOException {
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
        InputStream inputStream = c.getContentResolver().openInputStream(uri);
        byte[] strToByte = inputStreamToByteArray(inputStream);

        File file = new File(c.getExternalCacheDir(), fileName);

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(strToByte);
        fos.close();
        inputStream.close();

        saveImageIcon(c, file);

        return fileName;
    }

    public static void saveImageIcon(Context c, File f){
        String iconFile;
        iconFile = c.getExternalCacheDir().getAbsolutePath() + "/" + f.getName() + "_icon";
        File tempFile = new File(iconFile);
        try {
            FileOutputStream out = new FileOutputStream(tempFile);
            Bitmap bmp = getBmpFromPathWithResize(f.getAbsolutePath(), 100);
            bmp.compress(Bitmap.CompressFormat.JPEG, 20, out);
            out.close();
            bmp.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
