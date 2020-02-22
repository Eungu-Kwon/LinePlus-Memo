package com.eungu.lineplusnote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ImageCompute {

    public static final int NO_RESIZE = -1;

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
        if(path == null) return null;

        File f = new File(path);
        if(!f.exists()) return null;

        Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
        int orientation = getOrientationOfImage(f.getAbsolutePath());

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
        if(path == null) return null;
        
        File f = new File(path);
        if(!f.exists()) return null;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getAbsolutePath(), options);

        int orientation = getOrientationOfImage(path);

        if(size != NO_RESIZE) options.inSampleSize = ImageCompute.calculateInSampleSize(options, size, size);
        options.inJustDecodeBounds = false;

        Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath(), options);

        if(orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            bmp.recycle();
            bmp = resizedBitmap;
        }

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int offsetX = 0;
        int offsetY = 0;

        if(width > height){
            offsetX = (width - height) / 2;
            width = height;
        }
        else if(width < height){
            offsetY = (height - width) / 2;
            height = width;
        }

        Bitmap croppedBmp = Bitmap.createBitmap(bmp, offsetX, offsetY, width, height);
        bmp.recycle();
        return croppedBmp;
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

}
