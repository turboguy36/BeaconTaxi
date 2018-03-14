package kr.sysgen.taxi.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by leehg on 2016-07-20.
 */
public class BitmapControlUtil {
    private final String TAG = BitmapControlUtil.class.getSimpleName();

    private Context mContext;

    public BitmapControlUtil(Context c){
        mContext = c;
    }
    public String getFileName(String imagePath){
        String fileNameSegments[] = imagePath.split("/");
        return fileNameSegments[fileNameSegments.length - 1];
    }
    public Bitmap getBitmap(String imagePath) {
        Bitmap bitmap;

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            bitmap = GetRotatedBitmap(BitmapFactory.decodeFile(imagePath, options), getExifOrientation(imagePath));
//            imgView.setImageBitmap(bitmap);
            return bitmap;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getImagePath(Intent data) {
        String result;

        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        // Get the cursor
        Cursor cursor = mContext.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        // Move to first row
        try {
            cursor.moveToFirst();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        try {
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            result = cursor.getString(columnIndex);
            cursor.close();
            return result;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != b2) {
                    bitmap.recycle();
                    bitmap = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }

        return bitmap;
    }

    public synchronized int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(filepath);
        } catch (IOException e) {
            Log.e(TAG, "cannot read exif");
            e.printStackTrace();
        }

        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }

        return degree;
    }
    public String convertBitmapIntoString(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byte_arr = stream.toByteArray();

        Log.i(TAG, "byte_arr.length: " + byte_arr.length);

        return Base64.encodeToString(byte_arr, Base64.DEFAULT);
    }
}
