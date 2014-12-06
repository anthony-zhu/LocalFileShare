package com.kcdev.android.localfileshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.kcdev.android.getimage.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity implements OnClickListener {

    Button btnSelectImage;
    Button btnSendImage;
    ImageView imageView;
    Button btnTakePhoto;
    String currPhotoPath;
    String currPhotoPathAbsolute;

    private static final int CAM_REQUEST = 1313;
    private ShareActionProvider mShareActionProvider;
    Uri globalUri;

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    class buttonTakePhotoClick implements Button.OnClickListener
    {
        @Override
        public void onClick(View v) {

            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Toast toast = Toast.makeText(getApplicationContext(), "Failed to create photo", Toast.LENGTH_SHORT);
                    toast.show();
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(cameraIntent, CAM_REQUEST);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get reference to views
        btnSelectImage = (Button) findViewById(R.id.btnSelectImage);
        btnTakePhoto = (Button) findViewById(R.id.btnTakePhoto);
        btnSendImage = (Button) findViewById(R.id.btnSendImage);
        imageView = (ImageView) findViewById(R.id.imgView);

        // add click listener to button
        btnSelectImage.setOnClickListener(this);
        btnSendImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v){
                sendImage(v);
            }
        });
        btnTakePhoto.setOnClickListener(new buttonTakePhotoClick());

    }

    public void sendImage(View view){
        Intent shareImageIntent = new Intent();
        shareImageIntent.setAction(Intent.ACTION_SEND);
        shareImageIntent.putExtra(Intent.EXTRA_STREAM, globalUri);
        shareImageIntent.setType("image/jpeg");
        setShareIntent(shareImageIntent);
        startActivity(Intent.createChooser(shareImageIntent, "Send Photo..."));
    }

    @Override
    public void onClick(View view) {

        // 1. on Upload click call ACTION_GET_CONTENT intent
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // 2. pick image only
        intent.setType("image/*");
        // 3. start activity
        startActivityForResult(intent, 0);

        // define onActivityResult to do something with picked image

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if(resCode == Activity.RESULT_OK && data != null && reqCode != CAM_REQUEST){
            String realPath;
            // SDK < API11
            if (Build.VERSION.SDK_INT < 11)
                realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, data.getData());

                // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19)
                realPath = RealPathUtil.getRealPathFromURI_API11to18(this, data.getData());

                // SDK > 19 (Android 4.4)
            else
                realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());

            //setTextViews(data.getData().getPath(),realPath);
            setTextViews(realPath);
        }
        else if (reqCode == CAM_REQUEST && resCode == Activity.RESULT_OK ) {
            //simplified code for displaying image after photo capture
            setTextViews(currPhotoPathAbsolute);
            //function to add photo to media provider
            addPhotoToGallery();
        }
    }


    //function to save image
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imgName = "JPEG_" + timeStamp + "_";
        File storagePath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File photo = File.createTempFile(
                imgName,  /* prefix */
                ".jpg",         /* suffix */
                storagePath      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currPhotoPath = "file:" + photo.getAbsolutePath();
        currPhotoPathAbsolute = String.valueOf(photo.getAbsoluteFile());
        return photo;
    }

    private void addPhotoToGallery() {
        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currPhotoPathAbsolute);
        Uri photoUri = Uri.fromFile(f);
        mediaScannerIntent.setData(photoUri);
        this.sendBroadcast(mediaScannerIntent);
    }

    //private void setTextViews(int sdk, String uriPath,String realPath){
    private void setTextViews(String realPath){
        Uri uriFromPath = Uri.fromFile(new File(realPath));
        globalUri = uriFromPath;

        // you have two ways to display selected image

        // ( 1 ) imageView.setImageURI(uriFromPath);
        // ( 2 ) imageView.setImageBitmap(bitmap);


        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriFromPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);


        //imageView.setImageURI(uriFromPath);


        ExifInterface exif = null;
        try {
            exif = new ExifInterface(currPhotoPathAbsolute);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        //rotate image if not in proper orientation
        if(orientation == 6) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(rotatedBitmap);
        }
        else if(orientation == 3) {
                Matrix matrix = new Matrix();
                matrix.postRotate(180);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                imageView.setImageBitmap(rotatedBitmap);
            }
        else
        {
            imageView.setImageBitmap(bitmap);
        }

    }

}
