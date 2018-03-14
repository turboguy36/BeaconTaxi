package kr.sysgen.taxi.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.network.ConnectToServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUploadActivity extends AppCompatActivity {
    private final String TAG = ImageUploadActivity.class.getSimpleName();
    private ProgressDialog prgDialog;

    private String imgPath, fileName;
//    private RequestParams params = new RequestParams();
    private Bitmap bitmap;
    private String encodedString;
    private static int RESULT_LOAD_IMG = 1;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);
        mContext = getApplicationContext();

        prgDialog = new ProgressDialog(this);
        // Set Cancelable as False
        prgDialog.setCancelable(false);
    }

    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        try {
            // When an Image is picked

            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                // Move to first row
                try{
                    cursor.moveToFirst();
                }catch(NullPointerException e){
                    e.printStackTrace();
                }

                try {
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgPath = cursor.getString(columnIndex);
                    cursor.close();
                }catch(NullPointerException e){
                    e.printStackTrace();
                }

                ImageView imgView = (ImageView) findViewById(R.id.imgView);
                // Set the Image in ImageView
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    bitmap = GetRotatedBitmap(BitmapFactory.decodeFile(imgPath, options), getExifOrientation(imgPath));
                    imgView.setImageBitmap(bitmap);
                }catch(NullPointerException e){
                    e.printStackTrace();
                }

                // Get the Image's file name
                String fileNameSegments[] = imgPath.split("/");
                fileName = fileNameSegments[fileNameSegments.length - 1];
                // Put file name in Async Http Post Param which will used in Java web app
//                params.put("filename", fileName);

            } else {
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
//        } catch (NullPointerException ne) {
//            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
//        }
    }

    // When Upload button is clicked
    public void uploadImage(View v) {
        // When Image is selected from Gallery
        if (imgPath != null && !imgPath.isEmpty()) {
            prgDialog.setMessage("Converting Image to Binary Data");
            prgDialog.show();
            // Convert image to String using Base64
            encodeImagetoString();
            // When Image is not selected from Gallery
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "You must select image from gallery before you try to upload",
                    Toast.LENGTH_LONG).show();
        }
    }
    // AsyncTask - To convert Image to String
    public void encodeImagetoString() {
        new SendImageToServer().execute();
    }




    public synchronized int getExifOrientation(String filepath)
    {
        int degree = 0;
        ExifInterface exif = null;

        try
        {
            exif = new ExifInterface(filepath);
        }
        catch (IOException e)
        {
            Log.e(TAG, "cannot read exif");
            e.printStackTrace();
        }

        if (exif != null)
        {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

            if (orientation != -1)
            {
                // We only recognize a subset of orientation tag values.
                switch(orientation)
                {
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
    public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees)
    {
        if ( degrees != 0 && bitmap != null )
        {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2 );
            try
            {
                Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != b2)
                {
                    bitmap.recycle();
                    bitmap = b2;
                }
            }
            catch (OutOfMemoryError ex)
            {
                // We have no memory to rotate. Return the original bitmap.
            }
        }

        return bitmap;
    }

    private class SendImageToServer extends AsyncTask<Void, Void, String>{
        private ConnectToServer conn;
        private final String jspFile = getString(R.string.upload_image);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            conn = new ConnectToServer(mContext);
            prgDialog.setMessage("Invoking JSP");
        }

        @Override
        protected String doInBackground(Void... input) {
//                BitmapFactory.Options options = null;
//                options = new BitmapFactory.Options();
//                options.inSampleSize = 3;
//                bitmap = BitmapFactory.decodeFile(imgPath, options);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // Must compress the Image to reduce image size to make upload easy
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] byte_arr = stream.toByteArray();

            // Encode Image to String
            encodedString = Base64.encodeToString(byte_arr, Base64.DEFAULT);

            return conn.getJson(encodedString, jspFile);
        }

        @Override
        protected void onPostExecute(String msg) {
//            prgDialog.setMessage("Calling Upload");
            prgDialog.dismiss();
            // Put converted Image string into Async Http Post param
//                params.put("image", encodedString);
            // Trigger Image upload
//                triggerImageUpload();

        }
    }
}
