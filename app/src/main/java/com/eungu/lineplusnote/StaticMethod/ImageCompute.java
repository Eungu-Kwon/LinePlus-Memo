package com.eungu.lineplusnote.StaticMethod;

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

    public static int calculateInSampleSize(int imageWidth, int imageHeight, int reqWidth, int reqHeight) {
        int inSampleSize = 1;

        if (imageHeight > reqHeight || imageWidth > reqWidth) {

            final int halfHeight = imageHeight / 2;
            final int halfWidth = imageWidth / 2;

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

        Bitmap rotatedBmp = getBmpWithRotate(bmp, orientation);
        return rotatedBmp;
    }

    public static Bitmap getBmpFromPathWithResize(String path, int size){
        if(path == null) return null;

        File f = new File(path);
        if(!f.exists()) return null;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getAbsolutePath(), options);

        int orientation = getOrientationOfImage(path);

        if(size != NO_RESIZE) options.inSampleSize = ImageCompute.calculateInSampleSize(options.outWidth, options.outHeight, size, size);
        options.inJustDecodeBounds = false;

        Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath(), options);

        bmp = getBmpWithRotate(bmp, orientation);

        return bmp;
    }

    public static Bitmap getCroppedImage(Bitmap bmp) {
        int padding = 10;
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int offsetX = 0;
        int offsetY = 0;

        if(width < 10 || height < 10) {
            return bmp;
        }

        if(((height * 4) / 3) < width){
            padding = height / 3;
            offsetX = (width - height - padding) / 2;
            width = height + padding;
        }
        else {
            padding = width / 3;
            offsetY = (height - width + padding) / 2;
            height = width - padding;
        }

        Bitmap croppedBmp = Bitmap.createBitmap(bmp, offsetX, offsetY, width, height);
        bmp.recycle();

        return croppedBmp;
    }

    public static Bitmap getBmpWithRotate(Bitmap bmp, int orientation){
        if(orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            bmp.recycle();
            return resizedBitmap;
        }
        return bmp;
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
